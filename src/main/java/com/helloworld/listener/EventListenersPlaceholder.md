# Event listeners (Kafka / RabbitMQ)

Add your listeners in this package once the corresponding feature is enabled.

## Kafka

- Set `app.features.kafka.enabled: true` in config.
- Add dependency: `spring-kafka`.
- In `config/KafkaConfig.java` add `ConsumerFactory` and `KafkaListenerContainerFactory` beans.
- In this package add classes with `@KafkaListener(topics = "your-topic")` on methods.

## RabbitMQ

- Set `app.features.rabbitmq.enabled: true` in config.
- Add dependency: `spring-boot-starter-amqp`.
- In `config/RabbitMQConfig.java` add `RabbitListenerContainerFactory` and optionally declare Queue/Exchange.
- In this package add classes with `@RabbitListener(queues = "your-queue")` on methods.

## Other brokers

Follow the same pattern: feature flag in `AppFeaturesProperties`, conditional config in `config/`, listener classes in `listener/`.
