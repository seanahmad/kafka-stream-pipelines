package kic.kafka.simpleclient.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import kic.kafka.simpleclient.PropertiesExtender;
import kic.kafka.simpleclient.objectserialization.ObjectDeSerializer;
import kic.kafka.simpleclient.objectserialization.ObjectSerializer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CacheFactory {

    public static LoadingCache<ProducerCacheKey, KafkaProducer> newProducerCache(Properties properties){
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(getExpirationSetting(properties), TimeUnit.MILLISECONDS)
                .removalListener(CacheFactory::closeKafkaProducer)
                .build(key -> makeProducer(key, properties));
    }

    public static LoadingCache<ConsumerCacheKey, KafkaConsumer> newConsumerCache(Properties properties){
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(getExpirationSetting(properties), TimeUnit.MILLISECONDS)
                .removalListener(CacheFactory::closeKafkaConsumer)
                .build(key -> makeConsumer(key, properties));
    }

    private static long getExpirationSetting(Properties properties) {
        // should be less then kafka setting of "heartbeat.interval.ms" so that we can close the consumer
        return Math.max(Long.parseLong(properties.getOrDefault("heartbeat.interval.ms", "60000").toString()) - 1000L,
                        1000);
    }


    private static KafkaProducer makeProducer(ProducerCacheKey cacheKey, Properties properties) {
        String keySerializer = getKeySerializer(properties, cacheKey.keyClass);
        String valueSerializer = getValueSerializer(properties, cacheKey.valueClass);
        String clientId = randomUuidString();

        // we also need a default serializer -> serialize object to bytes -> use byte-de-serializer
        KafkaProducer producer = new KafkaProducer(new PropertiesExtender(properties)
                .with("key.serializer", keySerializer)
                .with("value.serializer", valueSerializer)
                .with("client.id", clientId)
                .extend());

        return producer;
    }

    private static KafkaConsumer makeConsumer(ConsumerCacheKey cacheKey, Properties properties) {
        // we need to maintain the de-serializers via properties file
        // we also need a default serializer -> use byte-de-serializer -> de-serialize object from bytes
        // and we need a random group id
        String keyDeSerializer = getKeyDeserializer(properties, cacheKey.keyClass);
        String valueDeSerializer = getValueDeserializer(properties, cacheKey.valueClass);
        String groupId = randomUuidString();

        KafkaConsumer consumer = new KafkaConsumer(new PropertiesExtender(properties)
                .with("key.deserializer", keyDeSerializer)
                .with("value.deserializer", valueDeSerializer)
                .with("group.id", groupId)
                .extend());

        return consumer;
    }

    private static void closeKafkaProducer(ProducerCacheKey key, KafkaProducer producer, RemovalCause cause) {
        producer.close();
    }

    private static void closeKafkaConsumer(ConsumerCacheKey key, KafkaConsumer consumer, RemovalCause cause) {
        consumer.close();
    }

    private static String getKeySerializer(Properties properties, String className) {
        return properties.getOrDefault("key.serializer." + className, ObjectSerializer.class.getName()).toString();
    }

    private static String getValueSerializer(Properties properties, String className) {
        return properties.getOrDefault("value.serializer." + className, ObjectSerializer.class.getName()).toString();
    }

    private static String getKeyDeserializer(Properties properties, String className) {
        return properties.getOrDefault("key.deserializer." + className, ObjectDeSerializer.class.getName()).toString();
    }

    private static String getValueDeserializer(Properties properties, String className) {
        return properties.getOrDefault("value.deserializer." + className, ObjectDeSerializer.class.getName()).toString();
    }

    private static String randomUuidString() {
        return UUID.randomUUID().toString();
    }
}
