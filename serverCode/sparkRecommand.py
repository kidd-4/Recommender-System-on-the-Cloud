import csv
from pyspark.sql import SparkSession
from pyspark.sql import Row
from pyspark.sql.functions import coalesce,first
from pyspark.mllib.stat import Statistics
from pyspark.ml.feature import HashingTF,IDF,Tokenizer
import numpy as np
import pandas as pd
from sklearn.metrics.pairwise import linear_kernel


def getCFRecommand(userProfile, inputMatrix, inputMovieDict, inputMovieNameRDD, inputMovieRatingRDD, spark, sc):
    userMovies = list()
    for i in range(0, len(userProfile)):
        userMovies.append(userProfile[i][0])

    for i in range(0, len(userProfile)):
        print("add similarity for " + userProfile[i][0] + " .....")
        sims = inputMatrix[inputMovieDict[userProfile[i][0]]]
        simsRDD = sc.parallelize(sims)
        simsRDD = simsRDD.map(lambda x: x * userProfile[i][1])
        TempRDD = inputMovieNameRDD.zip(simsRDD)
        inputMovieRatingRDD = inputMovieRatingRDD.union(TempRDD).reduceByKey(lambda a, b: a + b)

    inputMovieRatingRDD = inputMovieRatingRDD.filter(lambda x: x[0] not in userMovies)
    recomMovies = inputMovieRatingRDD.map(lambda metadata: Row(MovieTitle=metadata[0], similarity=float(metadata[1])))
    recomMoviesDF = spark.createDataFrame(recomMovies)
    recomMoviesDF.createOrReplaceTempView("recomMovies")
    result = spark.sql("select MovieTitle,similarity from recomMovies order by similarity desc")
    return result


def getCBRecommand(userProfile, tfidf, inputTtoI):
    t = tfidf[inputTtoI[userProfile[0][0]]] * userProfile[0][1]
    for i in range(1, len(userProfile)):
        t = t + tfidf[inputTtoI[userProfile[i][0]]] * userProfile[i][1]

    t_2l = np.array([t])

    cos = linear_kernel(t_2l, tfidf)
    cos = cos[0]
    sim_scores = list(enumerate(cos))
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    sim_scores = sim_scores[1:10]
    movie_indices = [i[0] for i in sim_scores]

    return inputTtoI.iloc[movie_indices]


def getRecommand(userProfile):
    reMovieList=[]
    global matrix, movieDict, movieNameRDD, movieRatingRDD, tfidfMatrix, TtoI, spark, sc
    resultCF = getCFRecommand(userProfile, matrix, movieDict, movieNameRDD, movieRatingRDD, spark, sc)
    resultCB = getCBRecommand(userProfile, tfidfMatrix, TtoI)

    listByCF = resultCF.select('MovieTitle').toPandas()
    for i in range(0, 10):
        reMovieList.append((listByCF[i:i + 1].tolist())[0][0])

    reMovieList = reMovieList + resultCB.index.tolist()
    print(reMovieList)
    return reMovieList


spark = SparkSession \
        .builder \
        .appName("sparkRecommandAlgo") \
        .config("spark.some.config.option", "some-value") \
        .getOrCreate()

sc = spark.sparkContext

# data reading and pre-processing
ratingFile = sc.textFile("movieData/ratings_small.csv")
ratingrdd = ratingFile.mapPartitions(lambda x: csv.reader(x))
ratingheader = ratingrdd.first()
ratingrdd = ratingrdd.filter(lambda x: x != ratingheader)
ratings = ratingrdd.map(
    lambda metadata: Row(userId=int(metadata[0]), movieId=int(metadata[1]), rating=float(metadata[2])))
ratingDF = spark.createDataFrame(ratings)
smallRatingDF = ratingDF.limit(2500)

linkFile = sc.textFile("movieData/links_small.csv")
linkrdd = linkFile.mapPartitions(lambda x: csv.reader(x))
linkheader = linkrdd.first()
linkrdd = linkrdd.filter(lambda x: x != linkheader)
linkrdd = linkrdd.filter(lambda x: x[0] != '' and x[2] != '')
linkMovieId = linkrdd.map(lambda metadata: Row(movieId=int(metadata[0]), tmdbId=int(metadata[2])))
filterMovieId = linkrdd.map(lambda x: int(x[2])).collect()
linkDataFrame = spark.createDataFrame(linkMovieId)

metaFile = sc.textFile("movieData/movies_metadata.csv")
rdd = metaFile.mapPartitions(lambda x: csv.reader(x))
header = rdd.first()
rdd = rdd.filter(lambda x: x != header)
rdd = rdd.filter(lambda x: x[5] != '')
rdd = rdd.filter(lambda x: "-" not in x[5])
moviesMetadata = rdd.map(lambda metadata: Row(tmdbId=int(metadata[5]), title=metadata[20]))
moviesMetadata = moviesMetadata.filter(lambda x: x[1] in filterMovieId)
moviesMetadataDF = spark.createDataFrame(moviesMetadata)

meta_data = pd.read_csv('movieData/movies_metadata.csv', encoding='mac_roman')
links = pd.read_csv('movieData/links_small.csv', encoding='mac_roman')
links = links[links['tmdbId'].notnull()]['tmdbId'].astype('int')

meta_data = meta_data.drop([4831, 19730, 29503, 35587])
meta_data = meta_data.drop_duplicates(['title'])
meta_data['id'] = meta_data['id'].astype('int')

lmd = meta_data[meta_data['id'].isin(links)]
lmd['description'] = lmd['overview']
lmd['description'] = lmd['description'].fillna('')
lmd['original_title'] = lmd['original_title'].fillna('')

# generating matrix for collaborative filtering
intermediateDF = linkDataFrame.join(moviesMetadataDF, linkDataFrame.tmdbId == moviesMetadataDF.tmdbId, 'inner')
intermediateDF = intermediateDF.sort('movieId', ascending=True)
intermediateDF = intermediateDF.drop('tmdbId')

finalDF = smallRatingDF.join(intermediateDF, smallRatingDF.movieId == intermediateDF.movieId, 'inner')
finalDF = finalDF.drop('movieId')

ratings_pivot = finalDF.groupBy("userId").pivot("title").agg(coalesce(first("rating")))
ratings_pivot = ratings_pivot.na.fill(0)

ratings_pivot_RDD = ratings_pivot.rdd.map(lambda x: (x[1:]))
ratings_pivot_head = ratings_pivot.schema.names[1:]
movieDict = dict()
movieNameRDD = sc.parallelize(ratings_pivot_head)
movieRatingRDD = movieNameRDD.map(lambda x: (x, 0))

for i in range(0, len(ratings_pivot_head)):
    movieDict[ratings_pivot_head[i]] = i

matrix = Statistics.corr(ratings_pivot_RDD, method="pearson")

# now generate the tfidf matrix by pyspark
lmdDescription = lmd['description']
indices = pd.Series(lmdDescription, index=lmd.index)
indices_reserve = indices.reset_index()


TtoI = pd.Series(lmd.index, index=lmd['title'])
TtoI.head()

descriptionData = spark.createDataFrame(indices_reserve, ['index', 'description'])

tokenizer = Tokenizer(inputCol='description', outputCol='words')
wordsData = tokenizer.transform(descriptionData)

hashingTF = HashingTF(inputCol="words", outputCol="rawFeatures", numFeatures=100)
# hashingTF = HashingTF(inputCol="words", outputCol="rawFeatures")
featurizedData = hashingTF.transform(wordsData)
# print(featurizedData.columns)

idf = IDF(inputCol="rawFeatures", outputCol="features")
idfModel = idf.fit(featurizedData)
rescaledData = idfModel.transform(featurizedData)

tfidfPdFrame = rescaledData.select('index', 'features').toPandas()
tfidfMatrix = np.zeros((tfidfPdFrame.shape[0], 100))

for i in range(0, tfidfPdFrame.shape[0]):
    a = tfidfPdFrame[i:i + 1].values.tolist()
    tfidfMatrix[i, :] = a[0][1].toArray()

testProfile=[['Toy Story', 5.0], ['I, Robot', 5.0], ['Harry Potter and the Chamber of Secrets', 3.0]]
resultTest = getRecommand(testProfile)

