import java.io.PrintWriter
import java.net.ServerSocket

import breeze.linalg.DenseVector
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{StreamingLinearRegressionWithSGD, LabeledPoint}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Random

object StreamingModelProducer{
    import breeze.linalg._
    
    def main(args:Array[String]){
        val maxEvents=100 // 초당 최대 이벤트 횟수
        val numFeatures=100 // 특징 벡터의 특징 개수
        
        
        val random=new Random()
        
        def generateRandomArray(n:Int)={ // 모든 엔트리가 정규 분포에서 임의로 생성도니 특정 크기의 배열 생성
            Array.tabulate(n)(n=>random.nextGaussian())
        }
        
        val w=new DenseVector(generateRandomArray(numFeatures)) // 가중치 벡터
            // 특징 벡터의 특징 개수만큼을 가지는 DenseVector 생성
        val intercept=random.nextGaussian()*10 // w와 함께 스트림 데이터 포인트 생성시 사용
        
        def generateNoisyData(n:Int)={ // 임의로 정의된 데이터 포인트 개수 생성
            (1 to n).map{i=>
                val x=new DenseVector(generateRandomArray(numFeatures))
                val y:Double=w.dot(x) // 가중치 벡터와 내적
                val noisy=y+intercept
                (noisy, x)
            }
            // intercept : 가중치 벡터에 적용하고, 타켓 변수의 평균값을 효과적으로 설명하는 상수 용어
        }
        
        val listener=new ServerSocket(8082)
        println("Listening on port : 8082")
        
        while(true){
            val socket=listener.accept() // 서버 소켓을 통해 요청을 받을 시,
            new Thread(){ // 새로운 thread를 생성하여 연산을 진행한다
                override def run{
                    println("Got client connected from : "+socket.getInetAddress)
                    val out=new PrintWriter(socket.getOutputStream(), true)
                    
                    while(true){
                        Thread.sleep(1000)
                        val num=random.nextInt(maxEvents) // 랜덤 데이터(max : 100) 생성
                        val data=generateNoisyData(num)
                        data.foreach{case(y,x)=>
                            val xStr=x.data.mkString(",") //x는 DenseVector 형
                            val eventStr=s"$y\t$xStr"
                            out.write(eventStr)
                            out.write("\n")
                        }
                        out.flush()
                        println(s"Created $num events...")
                    }
                    socket.close()
                }
            }.start() // start는 항상 Thread에 묶어주어야 함
        }
    }
}

object SimpleStreamingModel{
    def main(args:Array[String]){
        val ssc=new StreamingContext("local[2]", "First Streaming App", Seconds(10))
        val stream=ssc.socketTextStream("localhost", 8082)
        
        val numFeatures=100
        val zeroVector=DenseVector.zeros[Double](numFeatures) // 제로 벡터 생성
        val model=new StreamingLinearRegressionWithSGD().setInitialWeights(Vectors.dense(zeroVector.data)).setNumIterations(1).setStepSize(0.01)
        val labeledStream=stream.map{event=>
            val split=event.split("\t") //data를 뿌려줄때 y 탭 x,x,x,x 이런식이었음
            val y=split(0).toDouble
            val features=split(1).split(",").map(_.toDouble)
            LabeledPoint(label=y, features=Vectors.dense(features)) //꼭 이렇게 선언해야하나?
        }
        
        model.trainOn(labeledStream)
        model.predictOn(labeledStream.map{lp=>lp.features}).print()
        
        ssc.start()
        ssc.awaitTermination()
    }
}

object MoniotringStreamingModel{
    import org.apache.spark.SparkContext._
    
    def main(args:Array[String]){
        val ssc=new StreamingContext("local[2]", "First Streaming Model App", Seconds(10))
        val stream=ssc.socketTextStream("localhost", 8082)
        
        val numFeatures=100
        val zeroVector=DenseVector.zeros[Double](numFeatures)
        val model1=new StreamingLinearRegressionWithSGD().setInitialWeights(Vectors.dense(zeroVector.data)).setNumIterations(1).setStepSize(0.01)
        val model2=new StreamingLinearRegressionWithSGD().setInitialWeights(Vectors.dense(zeroVector.data)).setNumIterations(1).setStepSize(1.0)
         
        val labeledStream=stream.map{event=>
            val split=event.split("\t")
            val y=split(0).toDouble
            val features=split(1).split(",").map(_.toDouble)
            // 이미 ,인 라인을 가져오므로, map(_.split(",")을 하게 되면 쉼표로 구분된 각 원소마다 쉼표로 나누는 작업을 진행함
            LabeledPoint(y, Vectors.dense(features))
        }
        
        model1.trainOn(labeledStream)
        model2.trainOn(labeledStream)
        
        val predAndTrue=labeledStream.transform{rdd=> //rdd로 의미하는 거 보니, DStream 내부 각 rdd에 대해
            val latest1=model1.latestModel()
            val latest2=model2.latestModel()
            rdd.map{point=> //각 rdd에 있는 내부 데이터에 대해
                val pred1=latest1.predict(point.features)
                val pred2=latest2.predict(point.features)
                (pred1-point.label, pred2-point.label)
            }
            
        }
        
        /* 집합 자체가 Dstream 내부에 rdd 안에 특정 값 */
        
        // predAndTrue의 경우 Dstream 객체
        predAndTrue.foreachRDD{(rdd,time)=>
            val mse1=rdd.map{case(err1, err2)=>err1*err1}.mean() // rdd 자체로 바로 연산이 불가능
            val mse2=rdd.map{case(err1, err2)=>err2*err2}.mean()
            val rmse1=math.sqrt(mse1)
            val rmse2=math.sqrt(mse2)
            
            println(
                s"""
                |============================================
                |Time : $time
                |============================================
                """.stripMargin)
            println(s"MSE current batch : Model 1 : $mse1, Model2 : $mse2")
            println(s"RMSE current batch : Model 1 : $rmse1, Model2 : $rmse2")
            println("....\n")
            
        }
        
        ssc.start();
        ssc.awaitTermination()
        
    }
}