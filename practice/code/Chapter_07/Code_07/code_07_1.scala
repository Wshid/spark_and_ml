/*
	This code is intended to be run in the Scala shell. 
	Launch the Scala Spark shell by running ./bin/spark-shell from the Spark directory.
	You can enter each line in the shell and see the result immediately.
	The expected output in the Spark console is presented as commented lines following the
	relevant code

	The Scala shell creates a SparkContex variable available to us as 'sc'
*/

/* Replace 'PATH' with the path to the MovieLens data */

// load movie data
val movies = sc.textFile("../ml-100k/u.item")
println(movies.first)
// 1|Toy Story (1995)|01-Jan-1995||http://us.imdb.com/M/title-exact?Toy%20Story%20(1995)|0|0|0|1|1|1|0|0|0|0|0|0|0|0|0|0|0|0|0
val genres = sc.textFile("../ml-100k/u.genre")
genres.take(5).foreach(println)
/*
unknown|0
Action|1
Adventure|2
Animation|3
Children's|4
*/
val genreMap = genres.filter(!_.isEmpty).map(line => line.split("\\|")).map(array => (array(1), array(0))).collectAsMap
println(genreMap)
// Map(2 -> Adventure, 5 -> Comedy, 12 -> Musical, 15 -> Sci-Fi, 8 -> Drama, 18 -> Western, ...

val titlesAndGenres = movies.map(_.split("\\|")).map { array =>
val genres = array.toSeq.slice(5, array.size) // 5부터 마지막까지 장르 관련 바이너리 배열을 가져옴
val genresAssigned = genres.zipWithIndex.filter {t => 
t._1 == "1" // 튜플형태인 t의 첫번째 인자가 1인것으로 필터링(해당 장르가 맞다는 의미)
}.map {t => 
genreMap((t._2).toString) //이전에 만들었던 genreMap에서 Mapping하여 해당 장르의 문자열을 가져옴
}
(array(0).toInt, (array(1), genresAssigned))
}
println(titlesAndGenres.first)
// (1,(Toy Story (1995),ArrayBuffer(Animation, Children's, Comedy)))

// Run ALS model to generate movie and user factors
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating
val rawData = sc.textFile("/home/ubuntu/workspace/practice/code/Chapter_07/ml-100k/u.data")
val rawRatings = rawData.map(_.split("\t").take(3))
val ratings = rawRatings.map{ case Array(user, movie, rating) => Rating(user.toInt, movie.toInt, rating.toDouble) }
ratings.cache
val alsModel = ALS.train(ratings, 50, 10, 0.1)

// extract factor vectors
import org.apache.spark.mllib.linalg.Vectors
// 클러스터 모델의 입력으로 사용하기 위해, 모든 특징 추출 후 Mllib의 Vector형으로 변환해야 함
val movieFactors = alsModel.productFeatures.map { case (id, factor) => (id, Vectors.dense(factor)) }
val movieVectors = movieFactors.map(_._2)
val userFactors = alsModel.userFeatures.map { case (id, factor) => (id, Vectors.dense(factor)) }
val userVectors = userFactors.map(_._2)

// investigate distribution of features // 정규화 필요 유무를 확인하기 위해 사용함
import org.apache.spark.mllib.linalg.distributed.RowMatrix
val movieMatrix = new RowMatrix(movieVectors)
val movieMatrixSummary = movieMatrix.computeColumnSummaryStatistics()
val userMatrix = new RowMatrix(userVectors)
val userMatrixSummary = userMatrix.computeColumnSummaryStatistics()
println("Movie factors mean: " + movieMatrixSummary.mean)
println("Movie factors variance: " + movieMatrixSummary.variance)
println("User factors mean: " + userMatrixSummary.mean)
println("User factors variance: " + userMatrixSummary.variance)
// Movie factors mean: [0.28047737659519767,0.26886479057520024,0.2935579964446398,0.27821738264113755, ... 
// Movie factors variance: [0.038242041794064895,0.03742229118854288,0.044116961097355877,0.057116244055791986, ...
// User factors mean: [0.2043520841572601,0.22135773814655782,0.2149706318418221,0.23647602029329481, ...
// User factors variance: [0.037749421148850396,0.02831191551960241,0.032831876953314174,0.036775110657850954, ...
// 각 값들을 비교해보았을때, 크게 값 차이가 넘어가는 값들은 확인되지 않음 => 정규화 필요 x


/* user, movie, rating을 기반으로 해서 ALSmodel을 training 한 후,
 * userVectors와 movieVectors라는 특징 배열을 각각 추출해 낸다.
 * 이후 KMeans라는 클러스터 모델에 사용할 건데, 클러스터 모델의 기반은
 * 지도 학습이 아닌 자율 학습이므로 특정 Label을 사용하는 LabeledPoint객체를 사용하지 않는다.
 * user,movie,rating 자료를 기반으로 한 특징배열로 어떻게 연관없는 genre를 추측하지..?
	-> 이게 자율학습의 힘.
	-> 나중에 movie.id를 기준으로 vector와 기존 데이터를 합쳐서 유추함
 */
 
// run K-means model on movie factor vectors
import org.apache.spark.mllib.clustering.KMeans
val numClusters = 5
val numIterations = 10
val numRuns = 3
val movieClusterModel = KMeans.train(movieVectors, numClusters, numIterations, numRuns)
/*
...
14/09/02 22:16:45 INFO SparkContext: Job finished: collectAsMap at KMeans.scala:193, took 0.02043 s
14/09/02 22:16:45 INFO KMeans: Iterations took 0.300 seconds.
14/09/02 22:16:45 INFO KMeans: KMeans reached the max number of iterations: 10.
14/09/02 22:16:45 INFO KMeans: The cost for the best run is 2585.6805358546403.
...
movieClusterModel: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@2771ccdc
*/
// convergence example
val movieClusterModelConverged = KMeans.train(movieVectors, numClusters, 100)
/*
...
14/09/02 22:04:38 INFO SparkContext: Job finished: collectAsMap at KMeans.scala:193, took 0.040685 s
14/09/02 22:04:38 INFO KMeans: Run 0 finished in 34 iterations
14/09/02 22:04:38 INFO KMeans: Iterations took 0.812 seconds.
14/09/02 22:04:38 INFO KMeans: KMeans converged in 34 iterations.
14/09/02 22:04:38 INFO KMeans: The cost for the best run is 2584.9354332904104.
...
movieClusterModelConverged: org.apache.spark.mllib.clustering.KMeansModel = org.apache.spark.mllib.clustering.KMeansModel@6bb28fb5
*/

// train user model
val userClusterModel = KMeans.train(userVectors, numClusters, numIterations, numRuns)

// predict a movie cluster for movie 1
val movie1 = movieVectors.first
val movieCluster = movieClusterModel.predict(movie1)
println(movieCluster)
// 4
// predict clusters for all movies
val predictions = movieClusterModel.predict(movieVectors)
println(predictions.take(10).mkString(","))
// 0,0,1,1,2,1,0,1,1,1

// inspect the movie clusters, by looking at the movies that are closest to each cluster center

// define Euclidean distance function
import breeze.linalg._
import breeze.numerics.pow
def computeDistance(v1: DenseVector[Double], v2: DenseVector[Double]): Double = pow(v1 - v2, 2).sum

// join titles with the factor vectors, and compute the distance of each vector from the assigned cluster center
val titlesWithFactors = titlesAndGenres.join(movieFactors)
val moviesAssigned = titlesWithFactors.map { case (id, ((title, genres), vector)) => 
val pred = movieClusterModel.predict(vector) /* vector로 예측을 하면서, cluster 값을 반환 */
val clusterCentre = movieClusterModel.clusterCenters(pred) /* clusterCentre 값은 클러스터값의 중심 좌표로 고차원의 값 */
val dist = computeDistance(DenseVector(clusterCentre.toArray), DenseVector(vector.toArray))
(id, title, genres.mkString(" "), pred, dist) 
}
val clusterAssignments = moviesAssigned.groupBy { case (id, title, genres, cluster, dist) => cluster }.collectAsMap 

for ( (k, v) <- clusterAssignments.toSeq.sortBy(_._1)) {
println(s"Cluster $k:")
val m = v.toSeq.sortBy(_._5)
println(m.take(20).map { case (_, title, genres, _, d) => (title, genres, d) }.mkString("\n")) 
println("=====\n")
}

// clustering mathematical evaluation

// compute the cost (WCSS) on for movie and user clustering
val movieCost = movieClusterModel.computeCost(movieVectors)
val userCost = userClusterModel.computeCost(userVectors)
println("WCSS for movies: " + movieCost)
println("WCSS for users: " + userCost)
// WCSS for movies: 2586.0777166339426
// WCSS for users: 1403.4137493396831

// cross-validation for movie clusters
val trainTestSplitMovies = movieVectors.randomSplit(Array(0.6, 0.4), 123)
val trainMovies = trainTestSplitMovies(0)
val testMovies = trainTestSplitMovies(1)
val costsMovies = Seq(2, 3, 4, 5, 10, 20).map { k => (k, KMeans.train(trainMovies, numIterations, k, numRuns).computeCost(testMovies)) }
println("Movie clustering cross-validation:")
costsMovies.foreach { case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }
/*
Movie clustering cross-validation:
WCSS for K=2 id 942.06
WCSS for K=3 id 942.67
WCSS for K=4 id 950.35
WCSS for K=5 id 948.20
WCSS for K=10 id 943.26
WCSS for K=20 id 947.10
*/

// cross-validation for user clusters
val trainTestSplitUsers = userVectors.randomSplit(Array(0.6, 0.4), 123)
val trainUsers = trainTestSplitUsers(0)
val testUsers = trainTestSplitUsers(1)
val costsUsers = Seq(2, 3, 4, 5, 10, 20).map { k => (k, KMeans.train(trainUsers, numIterations, k, numRuns).computeCost(testUsers)) }
println("User clustering cross-validation:")
costsUsers.foreach { case (k, cost) => println(f"WCSS for K=$k id $cost%2.2f") }
/*
User clustering cross-validation:
WCSS for K=2 id 544.02
WCSS for K=3 id 542.18
WCSS for K=4 id 542.38
WCSS for K=5 id 542.33
WCSS for K=10 id 539.68
WCSS for K=20 id 541.21
*/

