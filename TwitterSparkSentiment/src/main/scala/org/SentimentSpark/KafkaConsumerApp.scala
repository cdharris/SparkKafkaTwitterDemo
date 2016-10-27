package org.SentimentSpark

import org.SentimentSpark.utils.{NLTK_loader, ParsingTweets, PropertiesLoader}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Durations, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


/**
  * Created by dan.dixey on 14/10/2016.
  */
object KafkaConsumerApp {

  def main(args: Array[String]): Unit = {

    // Setup the Spark Environment
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("KafkaListener")
      .set("spark.serializer", classOf[KryoSerializer].getCanonicalName)
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Durations.seconds(PropertiesLoader.microBatchTimeInSeconds))

    // Load the training Naive Bayes Model
    val naiveBayesModel = NaiveBayesModel.load(ssc.sparkContext, PropertiesLoader.ModelPath)
    val stopWordsList = ssc.sparkContext.broadcast(NLTK_loader.loadStopWords(PropertiesLoader.nltkStopWords))

    // Connect to this Kakfa Topic
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "example",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topic = PropertiesLoader.kafka_topic
    val topicMap = Map[String, Int](topic -> 4)

    // Create the stream
    val kafkaStream = KafkaUtils.createStream(ssc, "localhost:2181", "Dan", topicMap)

    // Whenever this Kafka stream produces data the resulting RDD will be printed
    kafkaStream.foreachRDD(r => {
      if (r.count() > 0) {
        println("*** Got RDD, size = " + r.count())
        r.foreach(s => println(ParsingTweets.extractData(s._1, stopWordsList, naiveBayesModel)))
      }
    })

    // Start the Spark Streaming Context
    ssc.start()

    println("*** Start Listening to Kafka Monitoring")
    // Give the stream time to initialize
    Thread.sleep(2000)

    // Auto-kill after processing rawTweets for n mins.
    ssc.awaitTerminationOrTimeout(PropertiesLoader.totalRunTimeInMinutes * 60 * 1000)

    println("*** Stopped Listening to Kafka Monitoring")
    ssc.stop(stopSparkContext = true, stopGracefully = true)
    println("*** Script Shutting Down")
  }
}
