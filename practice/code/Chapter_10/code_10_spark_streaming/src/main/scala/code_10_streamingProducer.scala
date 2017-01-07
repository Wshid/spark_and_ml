import java.io.PrintWriter
import java.net.ServerSocket
import scala.util.Random
import java.text.{SimpleDateFormat, DateFormat}
import java.util.Date

import org.apache.spark.SparkContext._
import org.apache.spark.streaming.{Seconds, StreamingContext};

// 일종의 서버 역할을 동행함, 나머지 object는 전부 client
object StreamingProducer{ // 생산자 어플리케이션, 요청을 받으면 정보를 생산하여 리턴한다
    def main(args:Array[String]){
        val random=new Random()
        val maxEvents=6
        val nameResource=this.getClass.getResourceAsStream("names.csv") // java.io.BufferedInputStream@26d7893b
        //println(nameResource);// this.getClass / getClass() 큰 차이는 없었음
        val names=scala.io.Source.fromInputStream(nameResource).getLines().toList.head.split(",").toSeq
        println(names);
        val products=Seq("iPhone Cover"->9.99, "Headphones"->5.49, "Galaxy Cover"->8.95, "iPad Cover"->7.49)
    
        def generateProductEvents(n:Int)={
            (1 to n).map{i=>
                val (product, price)=products(random.nextInt(products.size)) // (String, Double)혀을 리턴
                val user=random.shuffle(names).head // name를 랜덤으로 섞어서 첫번째 인자를 리턴한다.
                (user, product, price)
            }
        }
        
        val listener=new ServerSocket(8082) // 8082 포트로 서버 소켓을 연다.
        println("Listening on port : 8082")
        while(true){
            val socket=listener.accept() // 요청한 소켓을 잡는다.
            new Thread(){
                override def run={
                    println("Got client connect from : "+socket.getInetAddress)
                    val out=new PrintWriter(socket.getOutputStream(), true)
                    while(true){
                        Thread.sleep(1000) // 1초마다 한개씩 만들어냄
                        val num=random.nextInt(maxEvents)
                        val productEvents=generateProductEvents(num)
                        productEvents.foreach{event=>
                            out.write(event.productIterator.mkString(",")) // ,로 구분하여 내보냄
                            out.write("\n")
                        }
                        out.flush()
                        println(s"Create $num events...")
                    }
                    socket.close()
                }
            }.start() // Thread를 시작시키는 명령어
        }
    }
}

object SimpleStreamingApp{ // 단순 스트리밍 어플리케이션
    def main(args: Array[String]){ // local[N] : N threads on local
        val ssc=new StreamingContext("local[2]", "First Streaming Application", Seconds(10))
        val stream=ssc.socketTextStream("localhost", 8082); //서버의 정보를 넣는다.
        
        stream.print(); // RDD.take(10) 명령어와 유사하다.
        ssc.start();
        ssc.awaitTermination(); // Ctrl+C나 SIG와 같은 상황을 기다리도록 한다.
    }
}

object StreamingAnalyticsApp{
    def main(args:Array[String]){
        val ssc=new StreamingContext("local[2]", "First Streaming App", Seconds(10));
        val stream=ssc.socketTextStream("localhost", 8082); //서버 소켓의 정보
        
        val events=stream.map{record=>
            val event=record.split(",") // PrintWriter로 ,구분하여 보내는 것을 확인
                                        // 리턴값이 Array인 것을 확인할 수 있음
            (event(0), event(1), event(2))
        } // events 자체는 Dstream 객체
        // 아마 10초마다 새로운 RDD가 들어올 것 같은데?
        
        // foreachRDD : 스트림의 모든 RDD를 이용해서 임의의 프로세스를 적용하고,
        //  콘솔에 결과를 출력한다.
        events.foreachRDD{(rdd, time)=> // rdd와 실행 시간을 인자로 한다.
            val numPurchases=rdd.count()
            val uniqueUsers=rdd.map{case(user, product, price)=>user}.distinct().count()
            val totalRevenue=rdd.map{case(_,_,price)=>price.toDouble}.sum()
            val productByPopularity=rdd.map{case(user, product, price)=>(product,1)}
                                            .reduceByKey(_+_)
                                            .collect()
                                            .sortBy(-_._2)
            val mostPopular=productByPopularity(0)
            
            val formatter=new SimpleDateFormat
            val dateStr=formatter.format(new Date(time.milliseconds))
            println(s"== Batch start time: $dateStr ==")
            println("Total Purchases: "+numPurchases)
            println("Unique users : "+uniqueUsers)
            println("Total revenue : "+totalRevenue)
            println("Most popular product: %s with %d purchases".format(mostPopular._1, mostPopular._2))
        }
        
        ssc.start()
        ssc.awaitTermination()
    }
}

object StreamingStateApp{
    import org.apache.spark.streaming.StreamingContext._
    
    //(product, price) => (제품 개수, 수입)으로 상태 변경
    def updateState(prices:Seq[(String, Double)], currentTotal:Option[(Int, Double)])={
        val currentRevenue=prices.map(_._2).sum // 현재 모든 상태가 들어올때, 합친다
        val currentNumberPurchases=prices.size
        val state=currentTotal.getOrElse((0, 0.0)) // 받아오거나, 비어있으면 초기값을 가져옴
        
        Some((currentNumberPurchases+state._1, currentRevenue+state._2))
            // 실제 연산 진행 부분
            // Option의 하위 객체로, 값이 존재할 수는 있으나 형태는 미정
    }
    
    def main(args: Array[String]){
        val ssc=new StreamingContext("local[2]", "First Stateful Streaming App", Seconds(10))
        // stateful일 경우, 체크포인트 지정이 필요
        ssc.checkpoint("/home/ubuntu/workspace/practice/code/Chapter_10/output/") 
        val stream=ssc.socketTextStream("localhost", 8082)
        
        val events=stream.map{record=>
            val event=record.split(",")
            (event(0), event(1), event(2).toDouble) // (user, product, price)
        }
        
        val users=events.map{case(user, product, price)=>(user, (product, price))} // Array=>tuple
            // 키 : user , 값 : (product, price)
        val revenuePerUser=users.updateStateByKey(updateState)
        revenuePerUser.print()
        
        ssc.start()
        ssc.awaitTermination()
    }
}