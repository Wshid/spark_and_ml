/*
	This code is intended to be run in the Scala shell. 
	Launch the Scala Spark shell by running ./bin/spark-shell from the Spark directory.
	You can enter each line in the shell and see the result immediately.
	The expected output in the Spark console is presented as commented lines following the
	relevant code

	The Scala shell creates a SparkContex variable available to us as 'sc'

	Ensure you you start your Spark shell with enough memory:
		./bin/spark-shell --driver-memory 4g
*/

// spark-shell --driver-memory 512m // 실제 가용용량에 따라 할당 후 실행

/* Replace 'PATH' with the path to the 20 Newsgroups Data */
/* val path = "/home/ubuntu/workspace/practice/code/Chapter_09/20_newsgroups/*" */
val path = "/home/ubuntu/workspace/practice/code/Chapter_09/mini_newsgroups/*"

// 해당 링크가 사라져 train/test로 나누어진 원본을 찾을 수 없었다.
// 단, 설명페이지에서 해당 데이터를 추출할 수 있었다.
val rdd = sc.wholeTextFiles(path)

/* 이런식으로 사용해서, 교차검증을 위해 train과 test를 구분 지을 수 있을 듯,
 * randomsplit의 반환값은 결국 Array형이기 때문에 그에 맞춰서 train과 test를 할당 해주면 됨 */
val allset=rdd.randomSplit(Array(0.6, 0.4), 100)
val trainset=allset(0)
val testset=allset(1)
// count the number of records in the dataset
val text=trainset.map{case(file, text) => text}
println(trainset.count)
/*
...
14/10/12 14:27:54 INFO FileInputFormat: Total input paths to process : 11314
...
mini : 11314
All : 19997
*/
val newsgroups = trainset.map { case (file, text) => file.split("/").takeRight(2).head }
val countByGroup = newsgroups.map(n => (n, 1)).reduceByKey(_ + _).collect.sortBy(-_._2).mkString("\n")
println(countByGroup)
/* Mini
(sci.electronics,100)
(rec.motorcycles,100)
(alt.atheism,100)
(comp.graphics,100)
(comp.windows.x,100)
(rec.sport.baseball,100)
(talk.politics.mideast,100)
(rec.autos,100)
(talk.politics.guns,100)
(misc.forsale,100)
(sci.space,100)
(sci.crypt,100)
(comp.sys.ibm.pc.hardware,100)
(soc.religion.christian,100)
(sci.med,100)
(talk.politics.misc,100)
(talk.religion.misc,100)
(comp.sys.mac.hardware,100)
(rec.sport.hockey,100)
(comp.os.ms-windows.misc,100)
*/

/* All
(sci.electronics,1000)
(rec.motorcycles,1000)
(alt.atheism,1000)
(comp.graphics,1000)
(comp.windows.x,1000)
(rec.sport.baseball,1000)
(talk.politics.mideast,1000)
(rec.autos,1000)
(talk.politics.guns,1000)
(misc.forsale,1000)
(sci.space,1000)
(sci.crypt,1000)
(comp.sys.ibm.pc.hardware,1000)
(sci.med,1000)
(talk.politics.misc,1000)
(talk.religion.misc,1000)
(comp.sys.mac.hardware,1000)
(rec.sport.hockey,1000)
(comp.os.ms-windows.misc,1000)
(soc.religion.christian,997)
*/

// Tokenizing the text data
val text = trainset.map { case (file, text) => text }
val whiteSpaceSplit = text.flatMap(t => t.split(" ").map(_.toLowerCase))
println(whiteSpaceSplit.distinct.count) // 
// mini : 122296

// inspect a look at a sample of tokens - note we set the random seed to get the same results each time
println(whiteSpaceSplit.sample(true, 0.3, 42).take(100).mkString(",")) //distinct를 쓰면 아마 중복 제거되서 나오겠지?
/*
(laurence,battin)
subject:,holes,,1.1,<c5tx38.av8@usenet.ucs.indiana.edu>
date:,wed,,10:57:46,69

on,this,,i,something,to,comments...

please,me,isn't,i,as,as,
>,kv07@iastate.edu,(warren,writes:

[snip,,,always,,see,>anything,pass,event,>,the,,let's,say,we,we,marble,marble,into,into,hole.,,races,,ever,ever,>faster,,towards,space,>caused,by,by,by,event,>,
>,time,
>,of,>infinity.,,the,that,nothing,can,can,hole.

it,are,physical,physical,here.,,point,is
that,is
that,about,is,as,time.,our,that,is,,we,things,the,being,earth.
this,be,earth.,this,if,travelling,the,earth,rate,even,even,the,events,events,the
*/

// split text on any non-word tokens
val nonWordSplit = text.flatMap(t => t.split("""\W+""").map(_.toLowerCase))
println(nonWordSplit.distinct.count)
// 50681
// inspect a look at a sample of tokens
println(nonWordSplit.distinct.sample(true, 0.3, 42).take(100).mkString(","))
/*
schwabam,75974,jove,entitlements,250m,6j,goofed,dfuller,k6mf,anil,749,dangers,historians,ryerson,
compitantly,16832,comparing,comparing,contoler,930420,bombay,24240,uwash,crossman,dmitriev,
assemble,computational,subtleties,deterministic,hour,054056,interfere,1630cyl,zwar,nowadays,
nowadays,exhausting,exhausting,discernible,discernible,strives,barking,abstract,milliseconds,
milliseconds,blown,131045,projector,chama,austern,austern,austern,173759,g45,eps,00ecgillespie,
suspects,ii,funded,pdp11,canonical,birds,placing,21720,21720,10180,boon,uab,uab,committs,ysr,
178532,76704,76704,fo1,darden,27799,dynasties,43791,elizabeth,happy,rensselaer,xinto,suckers,
fundementalists,peterborough,c4zgam,210mb,sd24e,_yog,joni,145651,meth,spoils,fcc,fcc,somwehat,
symmetrical,symmetrical,diversions
*/

// filter out numbers
val regex = """[^0-9]*""".r
val filterNumbers = nonWordSplit.filter(token => regex.pattern.matcher(token).matches)
println(filterNumbers.distinct.count)
// 35988
println(filterNumbers.distinct.sample(true, 0.3, 42).take(100).mkString(","))
/*
ntuvax,glorifying,fuller,clients,tongued,husky,mch,ignore,supporter,energizing,oathes,robin,
erected,piracy,historians,ryerson,ocunix,ocunix,whit,pertinently,outlaw,cornerstone,brody,bernard,
enlarge,table,gasses,borg,ets,newsserver,preface,ndsu,go,ntx,gre,gre,anthropologists,anthropologists,
unbending,unbending,uwash,raffle,inaccuracies,armeniens,armeniens,amazement,badanes,unaskable,
outweigh,uwec,uwec,uwec,accept,explorers,shaky,inviting,uva,sportsdesk,immature,organism,shutdown,
interfere,teed,zwar,zwar,bravo,strives,calmly,calmly,barking,deceased,valve,abstract,abstract,opiniones,
mutated,tripe,projector,parenthesized,warms,poland,rezaei,graphing,marcc,zelda,mhs,uil,kipling,ii,panvalkar,canonical,gontier,xmtext,placing,differing,differing,vfei,committs,committs,widens
*/

// examine potential stopwords
val tokenCounts = filterNumbers.map(t => (t, 1)).reduceByKey(_ + _)
val oreringDesc = Ordering.by[(String, Int), Int](_._2)
println(tokenCounts.top(20)(oreringDesc).mkString("\n"))
/*
(the,26858)
(edu,16556)
(to,14006)
(of,12513)
(a,11510)
(and,10439)
(i,8991)
(in,8790)
(is,8063)
(that,7235)
(it,6086)
(for,5395)
(cmu,5322)
(com,5178)
(you,5028)
(cs,4559)
(from,4074)
(s,3926)
(on,3682)
(this,3657)
*/

// filter out stopwords
val stopwords = Set(
"the","a","an","of","or","in","for","by","on","but", "is", "not", "with", "as", "was", "if",
"they", "are", "this", "and", "it", "have", "from", "at", "my", "be", "that", "to"
)
val tokenCountsFilteredStopwords = tokenCounts.filter { case (k, v) => !stopwords.contains(k) }
println(tokenCountsFilteredStopwords.top(20)(oreringDesc).mkString("\n"))
/*
(edu,16556)
(i,8991)
(cmu,5322)
(com,5178)
(you,5028)
(cs,4559)
(s,3926)
(news,3457)
(t,3272)
(srv,3243)
(cantaloupe,2629)
(net,2546)
(subject,2207)
(can,2182)
(message,2171)
(lines,2118)
(date,2100)
(apr,2075)
(id,2068)
(newsgroups,2042)
*/

// filter out tokens less than 2 characters
val tokenCountsFilteredSize = tokenCountsFilteredStopwords.filter { case (k, v) => k.size >= 2 }
println(tokenCountsFilteredSize.top(20)(oreringDesc).mkString("\n"))
/*
(edu,16556)
(cmu,5322)
(com,5178)
(you,5028)
(cs,4559)
(news,3457)
(srv,3243)
(cantaloupe,2629)
(net,2546)
(subject,2207)
(can,2182)
(message,2171)
(lines,2118)
(date,2100)
(apr,2075)
(id,2068)
(newsgroups,2042)
(path,2030)
(organization,2009)
(re,1937)
*/

// examine tokens with least occurrence
val oreringAsc = Ordering.by[(String, Int), Int](-_._2)
println(tokenCountsFilteredSize.top(20)(oreringAsc).mkString("\n"))
/*
(johnr,1)
(controling,1)
(compleat,1)
(feh,1)
(oqy,1)
(therefor,1)
(overstimulation,1)
(onerous,1)
(multimeters,1)
(edits,1)
(racking,1)
(bunuel,1)
(wqod,1)
(islamiyah,1)
(tracer,1)
(usaf,1)
(turnout,1)
(carcinogenesis,1)
(tense,1)
(sophistication,1)
*/

// filter out rare tokens with total occurence < 2
val rareTokens = tokenCounts.filter{ case (k, v) => v < 2 }.map { case (k, v) => k }.collect.toSet
val tokenCountsFilteredAll = tokenCountsFilteredSize.filter { case (k, v) => !rareTokens.contains(k) }
println(tokenCountsFilteredAll.top(20)(oreringAsc).mkString("\n"))
/*
(loren,2)
(bellevue,2)
(jcarey,2)
(rolled,2)
(pig,2)
(whit,2)
(goofed,2)
(gunning,2)
(upsets,2)
(smits,2)
(subscriptions,2)
(mop,2)
(hwrvo,2)
(lmc,2)
(modifies,2)
(_novidor,2)
(dfuller,2)
(baarnie,2)
(erroneous,2)
(gasses,2)
*/
println(tokenCountsFilteredAll.count)
// 21582

// create a function to tokenize each document
def tokenize(line: String): Seq[String] = {
line.split("""\W+""") // 문자가 아닌것들로 split
.map(_.toLowerCase) // 소문자화
.filter(token => regex.pattern.matcher(token).matches) // 숫자 포함되는 것들 전부 필터링
.filterNot(token => stopwords.contains(token)) // 중지 단어(the, a, is, at 등) 필터링
.filterNot(token => rareTokens.contains(token)) // 1글자인것 필터링
.filter(token => token.size >= 2)
.toSeq
}

// check that our tokenizer achieves the same result as all the steps above
println(text.flatMap(doc => tokenize(doc)).distinct.count)
// 21582
// tokenize each document
val tokens = text.map(doc => tokenize(doc)) // 각 문서(doc, line)별 단어 wrapped Array 된 듯한데
println(tokens.first.take(20))
/*
WrappedArray(xref, cantaloupe, srv, cs, cmu, edu, alt, atheism, talk, religion,
			misc, talk, origins, newsgroups, alt, atheism, talk, religion, misc, talk)
*/

// === train TF-IDF model === //

import org.apache.spark.mllib.linalg.{ SparseVector => SV }
import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib.feature.IDF
// set the dimensionality of TF-IDF vectors to 2^18
val dim = math.pow(2, 18).toInt
val hashingTF = new HashingTF(dim)

/* tf(t,d) : d문서에서 t 단어의 빈도
 * idf(t) : 말뭉치(corpus)에서 t 단어의 역문서 빈도(log(N/d))
 * N : 전체 문서수 // d : t 단어가 포함된 문서 수
 */
 
// 여기서부터 다시 시작

val tf = hashingTF.transform(tokens)
// cache data in memory
tf.cache
val v = tf.first.asInstanceOf[SV] // 첫번째 문서에 대해서 SV형으로?
println(v.size)
// 262144
println(v.values.size)
// 273
println(v.values.take(10).toSeq)
// WrappedArray(1.0, 5.0, 1.0, 1.0, 1.0, 2.0, 1.0, 1.0, 1.0, 1.0)
println(v.indices.take(10).toSeq)
// WrappedArray(14, 170, 925, 1330, 1968, 2992, 3164, 4081, 5785, 5795)

val idf = new IDF().fit(tf)
val tfidf = idf.transform(tf)	
val v2 = tfidf.first.asInstanceOf[SV]
println(v2.values.size)
// 273 // 위에서 구한 tf의 사이즈와 동일해야한다.
println(v2.values.take(10).toSeq)
// WrappedArray(1.8942920698348578, 21.00102476460789, 5.521960792903897, 6.215107973463843, 6.215107973463843, 3.4698685553008732, 6.215107973463843, 2.91005445235459, 5.2988172415896875, 2.5138059993513493)
println(v2.indices.take(10).toSeq)
// WrappedArray(14, 170, 925, 1330, 1968, 2992, 3164, 4081, 5785, 5795)

// min and max tf-idf scores
val minMaxVals = tfidf.map { v => 
val sv = v.asInstanceOf[SV]
(sv.values.min, sv.values.max) 
}
val globalMinMax = minMaxVals.reduce { case ((min1, max1), (min2, max2)) => 
(math.min(min1, min2), math.max(max1, max2)) 
}
//grobalMinMax: (Double, Double) = (0.0,1830.6876158163038)
println(globalMinMax)
//(0.0,1830.6876158163038)

// test out a few common words 
val common = sc.parallelize(Seq(Seq("you", "do", "we")))
val tfCommon = hashingTF.transform(common)
val tfidfCommon = idf.transform(tfCommon)
val commonVector = tfidfCommon.first.asInstanceOf[SV]
// commonVector: org.apache.spark.mllib.linalg.SparseVector
//		= (262144,[37470,147489,252801],[0.9773371067838398,1.2435600680756338,0.48014988133919195])
println(commonVector.values.toSeq)
// WrappedArray(0.9773371067838398, 1.2435600680756338, 0.48014988133919195)

// test out a few uncommon words
val uncommon = sc.parallelize(Seq(Seq("telescope", "legislation", "investment")))
val tfUncommon = hashingTF.transform(uncommon)
val tfidfUncommon = idf.transform(tfUncommon)
val uncommonVector = tfidfUncommon.first.asInstanceOf[SV]
println(uncommonVector.values.toSeq)
// WrappedArray(5.809642865355678, 6.215107973463843, 5.404177757247514)

// === document similarity === //

val hockeyText = trainset.filter { case (file, text) => file.contains("hockey") }
// note that the 'transform' method used below is the one which works on a single document 
// in the form of a Seq[String], rather than the version which works on an RDD of documents
val hockeyTF = hockeyText.mapValues(doc => hashingTF.transform(tokenize(doc)))
// mapValues를 하게 되면 tuple 형태의 value 형태만 map 조건으로 사용이 가능함
val hockeyTfIdf = idf.transform(hockeyTF.map(_._2))

// compute cosine similarity using Breeze
import breeze.linalg._
val hockey1 = hockeyTfIdf.sample(true, 0.1, 42).first.asInstanceOf[SV]
	//mllib.linalg.SparseVector 형에서 cosineSim을 계산하기 위해 breeze로 새로 변경한다.
val breeze1 = new SparseVector(hockey1.indices, hockey1.values, hockey1.size)
val hockey2 = hockeyTfIdf.sample(true, 0.1, 43).first.asInstanceOf[SV]
val breeze2 = new SparseVector(hockey2.indices, hockey2.values, hockey2.size)
val cosineSim = breeze1.dot(breeze2) / (norm(breeze1) * norm(breeze2))
println(cosineSim)
// 1.0 // seed 값의 변화가 없었나?

// compare to comp.graphics topic
val graphicsText = trainset.filter { case (file, text) => file.contains("comp.graphics") }
val graphicsTF = graphicsText.mapValues(doc => hashingTF.transform(tokenize(doc)))
val graphicsTfIdf = idf.transform(graphicsTF.map(_._2))
val graphics = graphicsTfIdf.sample(true, 0.1, 42).first.asInstanceOf[SV]
// sample은 해당 크기만큼 랜덤으로 가져옴
val breezeGraphics = new SparseVector(graphics.indices, graphics.values, graphics.size)
val cosineSim2 = breeze1.dot(breezeGraphics) / (norm(breeze1) * norm(breezeGraphics))
println(cosineSim2)
// 0.004664850323792852

// compare to sport.baseball topic
val baseballText = trainset.filter { case (file, text) => file.contains("baseball") }
val baseballTF = baseballText.mapValues(doc => hashingTF.transform(tokenize(doc)))
val baseballTfIdf = idf.transform(baseballTF.map(_._2))
val baseball = baseballTfIdf.sample(true, 0.1, 42).first.asInstanceOf[SV]
val breezeBaseball = new SparseVector(baseball.indices, baseball.values, baseball.size)
val cosineSim3 = breeze1.dot(breezeBaseball) / (norm(breeze1) * norm(breezeBaseball))
println(cosineSim3)
// 0.05047395039466008

// === document classification === //

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.evaluation.MulticlassMetrics

val newsgroupsMap = newsgroups.distinct.collect().zipWithIndex.toMap
/* Map(talk.religion.misc -> 16, talk.politics.misc -> 15, rec.sport.hockey -> 18, 
misc.forsale -> 9, rec.motorcycles -> 1, sci.electronics -> 0, alt.atheism -> 2, 
talk.politics.mideast -> 6, comp.sys.ibm.pc.hardware -> 12, rec.sport.baseball -> 5, 
rec.autos -> 7, talk.politics.guns -> 8, sci.space -> 10, comp.sys.mac.hardware -> 17, 
sci.crypt -> 11, comp.os.ms-windows.misc -> 19, comp.graphics -> 3, comp.windows.x -> 4,
sci.med -> 14, soc.religion.christian -> 13)*/
// val newsgroupsMap2=newsgroups.distinct.zipWithIndex.collectAsMap 값 비교
/* newsgroupsMap2: scala.collection.immutable.Map[String,Int] = 
Map(rec.sport.hockey -> 18, sci.space -> 10, comp.graphics -> 3, sci.crypt -> 11, 
alt.atheism -> 2, sci.med -> 14, comp.windows.x -> 4, soc.religion.christian -> 13, 
talk.politics.mideast -> 6, misc.forsale -> 9, comp.sys.ibm.pc.hardware -> 12, 
talk.religion.misc -> 16, comp.sys.mac.hardware -> 17, rec.sport.baseball -> 5, 
rec.autos -> 7, rec.motorcycles -> 1, talk.politics.guns -> 8, talk.politics.misc -> 15, comp.os.ms-windows.misc -> 19, sci.electronics -> 0) */

// Indexing 되는 값만 다를 뿐 형태는 같다는 것을 알 수 있음(Map[String, Int])
val zipped = newsgroups.zip(tfidf)
val train = zipped.map { case (topic, vector) => LabeledPoint(newsgroupsMap(topic), vector) }
train.cache
val model = NaiveBayes.train(train, lambda = 0.1)

// 일단 여기까지 함

/* 실상 테스트 집합이 필요가 없었음. 이미 단독 집합으로 사용한 경우이기 때문에 */
//val testPath = "/PATH/20news-bydate-test/*" /* 위에서 만들었던 testset을 이용 */
//val testRDD = sc.wholeTextFiles(testPath) /* 그렇게 되면 이 두과정이 생략됨 */

val testLabels = testset.map { case (file, text) => 
val topic = file.split("/").takeRight(2).head
newsgroupsMap(topic)
}

/* 모델 학습은 되지만, memory 부족으로 인한 에러로 인해 더 이상 진행할 수 없었음
 * java.lang.OutOfMemoryError: Java heap space */

val testTf = testset.map { case (file, text) => hashingTF.transform(tokenize(text)) }
val testTfIdf = idf.transform(testTf)
val zippedTest = testLabels.zip(testTfIdf)
val test = zippedTest.map { case (topic, vector) => LabeledPoint(topic, vector) }

val predictionAndLabel = test.map(p => (model.predict(p.features), p.label))
val accuracy = 1.0 * predictionAndLabel.filter(x => x._1 == x._2).count() / test.count()
println(accuracy)
// 0.7915560276155071
val metrics = new MulticlassMetrics(predictionAndLabel)
println(metrics.weightedFMeasure)
// 0.7810675969031116

// test on raw token features
val rawTokens = trainset.map { case (file, text) => text.split(" ") }
val rawTF = rawTokens.map(doc => hashingTF.transform(doc))
val rawTrain = newsgroups.zip(rawTF).map { case (topic, vector) => LabeledPoint(newsgroupsMap(topic), vector) }
val rawModel = NaiveBayes.train(rawTrain, lambda = 0.1)
val rawTestTF = testset.map { case (file, text) => hashingTF.transform(text.split(" ")) }
val rawZippedTest = testLabels.zip(rawTestTF)
val rawTest = rawZippedTest.map { case (topic, vector) => LabeledPoint(topic, vector) }
val rawPredictionAndLabel = rawTest.map(p => (rawModel.predict(p.features), p.label))
val rawAccuracy = 1.0 * rawPredictionAndLabel.filter(x => x._1 == x._2).count() / rawTest.count()
println(rawAccuracy)
// 0.7661975570897503
val rawMetrics = new MulticlassMetrics(rawPredictionAndLabel)
println(rawMetrics.weightedFMeasure)
// 0.7628947184990661

// === Word2Vec === /

import org.apache.spark.mllib.feature.Word2Vec
val word2vec = new Word2Vec()
word2vec.setSeed(42) // we do this to generate the same results each time
val word2vecModel = word2vec.fit(tokens)
/*
...
14/10/25 14:21:59 INFO Word2Vec: wordCount = 2133172, alpha = 0.0011868763094487506
14/10/25 14:21:59 INFO Word2Vec: wordCount = 2144172, alpha = 0.0010640806039941193
14/10/25 14:21:59 INFO Word2Vec: wordCount = 2155172, alpha = 9.412848985394907E-4
14/10/25 14:21:59 INFO Word2Vec: wordCount = 2166172, alpha = 8.184891930848592E-4
14/10/25 14:22:00 INFO Word2Vec: wordCount = 2177172, alpha = 6.956934876302307E-4
14/10/25 14:22:00 INFO Word2Vec: wordCount = 2188172, alpha = 5.728977821755993E-4
14/10/25 14:22:00 INFO Word2Vec: wordCount = 2199172, alpha = 4.501020767209707E-4
14/10/25 14:22:00 INFO Word2Vec: wordCount = 2210172, alpha = 3.2730637126634213E-4
14/10/25 14:22:01 INFO Word2Vec: wordCount = 2221172, alpha = 2.0451066581171076E-4
14/10/25 14:22:01 INFO Word2Vec: wordCount = 2232172, alpha = 8.171496035708214E-5
...
14/10/25 14:22:02 INFO SparkContext: Job finished: collect at Word2Vec.scala:368, took 56.585983 s
14/10/25 14:22:02 INFO MappedRDD: Removing RDD 200 from persistence list
14/10/25 14:22:02 INFO BlockManager: Removing RDD 200
14/10/25 14:22:02 INFO BlockManager: Removing block rdd_200_0
14/10/25 14:22:02 INFO MemoryStore: Block rdd_200_0 of size 9008840 dropped from memory (free 1755596828)
word2vecModel: org.apache.spark.mllib.feature.Word2VecModel = org.apache.spark.mllib.feature.Word2VecModel@2b94e480
*/
// evaluate a few words
word2vecModel.findSynonyms("hockey", 20).foreach(println)
/*
(sport,0.6828256249427795)
(ecac,0.6718048453330994)
(hispanic,0.6519884467124939)
(glens,0.6447514891624451)
(woofers,0.6351765394210815)
(boxscores,0.6009076237678528)
(tournament,0.6006366014480591)
(champs,0.5957855582237244)
(aargh,0.584071934223175)
(playoff,0.5834275484085083)
(ahl,0.5784651637077332)
(ncaa,0.5680188536643982)
(pool,0.5612311959266663)
(olympic,0.5552600026130676)
(champion,0.5549421310424805)
(filinuk,0.5528956651687622)
(yankees,0.5502706170082092)
(motorcycles,0.5484763979911804)
(calder,0.5481109023094177)
(rec,0.5432182550430298)
*/
word2vecModel.findSynonyms("legislation", 20).foreach(println)
/*
(accommodates,0.8149217963218689)
(briefed,0.7582570314407349)
(amended,0.7310371994972229)
(telephony,0.7139414548873901)
(aclu,0.7080780863761902)
(pitted,0.7062571048736572)
(licensee,0.6981208324432373)
(agency,0.6880651712417603)
(policies,0.6828961372375488)
(senate,0.6821110844612122)
(businesses,0.6814320087432861)
(permit,0.6797110438346863)
(cpsr,0.6764014959335327)
(cooperation,0.6733141541481018)
(surveillance,0.6670728325843811)
(restricted,0.6666574478149414)
(congress,0.6661365628242493)
(procure,0.6655452251434326)
(industry,0.6650314927101135)
(inquiry,0.6644254922866821)
*/






