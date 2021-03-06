### Kafka consumer to analyse tweets through Spark (Part 2)

*Dan Dixey*

*Thursday 27th October*

*Applied use of Scala using Spark, Twitter4j and Kafka*

The purpose of this project is to pull tweets from twitter and push into Kafka topic.

### Pre-Requisites for this project 

Part 1 and 2 should aleady be running but if not.

1. [Download](http://kafka.apache.org/downloads.html) the 0.8.2.1 version, unzip it and make (if required).

2.  Run the following command to Start zookeeper & Kafka:
    
         $ bin/zookeeper-server-start.sh config/zookeeper.properties 
         $ bin/kafka-server-start.sh config/server.properties

3.  This project has been built using sbt assembly, so a .jar file has been generated that you can run with a one-liner. To run this execute the following command:

        $ cd TwitterKafkaProducer
        $ java -jar target/scala-2.11/TwitterSparkSentiment-assembly-1.0.jar
        
                           
### Re-building the .jar file

Shouldn't require rebuilding the .jar file to often after the first build. If it is required then take these steps:

4. Navigate to the TwitterSparkSentiment folder in your terminal

5. Run the following command:

    $ sbt assembly

*The output is shared within the target/scala-2.11/ folder*     
