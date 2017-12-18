package kic.kafka.pipeliet.bolts.services;

import kic.kafka.pipeliet.bolts.persistence.entities.BoltsState;
import kic.kafka.pipeliet.bolts.persistence.repositories.BoltsStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Deprecated
public class KafkaBoltingService {
    // this is th class where the magic should happen
    // we should have a threadpool here and subscribe to topics and
    // procuce messages tot he target topic as source events are arriving
    // lets also have an error topic ... there would be a kafka lig4j appender but prob we want our own format
    private final ExecutorService process = Executors.newFixedThreadPool(1); // TODO use http://www.baeldung.com/spring-factorybean public class ToolFactory implements FactoryBean<Tool> {
    private final ExecutorService retry = Executors.newFixedThreadPool(1); // TODO use http://www.baeldung.com/spring-factorybean public class ToolFactory implements FactoryBean<Tool> {
    private final BoltsStateRepository repository;
    private final KafkaClientService kafkaClient;

    @Autowired
    public KafkaBoltingService(BoltsStateRepository repository, KafkaClientService kafkaClient) {
        this.repository = repository;
        this.kafkaClient = kafkaClient;
    }

    public Set<String> fetchTopics() throws ExecutionException, InterruptedException {
        return kafkaClient.listTopics();
    }

    public void boltPipelet(String pipeline, String sourceTopic, String targetTopic, URL pipeletLambda) {
        // check for duplicate key
        // create the new target topic
        /*kafkaServiceFactory.createTopic(targetTopic, 1, 1);

        // create a new BoltState


        // connectPipelet
        KafkaConsumer<Long, String> consumer = kafkaServiceFactory.createConsumer(pipeline, sourceTopic, 0);

        // nur mal als grob konzept
        new Thread() {
            RestTemplate rt = new RestTemplate();
            String state = null;

            @Override
            public void run() {

                while (true) {
                    ConsumerRecords<Long, String> records = consumer.poll(300);
                    for (ConsumerRecord<Long, String> record : records) {
                        try {
                            // state = rt.postForObject(pipeletLambda.toURI(), record.value(), String.class);
                            // save state
                            // todo save state

                            // push state
                            kafkaProducer.send(new ProducerRecord<Long, String>(targetTopic, record.key(), "lala: " + record.value()));

                            // commit sync
                            consumer.commitSync();
                        } catch (Exception e) {
                            e.printStackTrace();
                            // do not commit ...
                        }
                    }
                }
            }
        }.start();*/
    }

    public void resumePipeline() {
        //repository.findAll().forEach(KafkaBoltingService::connectPipelet);
        System.out.println("done resume");
    }

    private static void connectPipelet(BoltsState state) {
        System.out.println(state);
        // create new kafka kafkaConsumer, forward every event to the webservice, pass the webserice result to the target topic
        // what if the webservice is down? how can we retry?
        // if target is empty then this is basically a sink
        // what if we recreaed a new kafka without any history? what if the topic is missing? we would need to recreate topics and re-initialize all the states


    }

    public void createTestTopic() {
        // Test if topic is readable
        // C:\kafka_2.11-1.0.0\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic Bolting-Server-Test --from-beginning
        String testTopic = "Bolting-Server-Test";
        kafkaClient.createTopic(testTopic);

        for (long i=0; i<5; i++) {
            kafkaClient.send(testTopic, i, "hello server test " + i);
        }

        //kafkaClient.poll()
    }

}
