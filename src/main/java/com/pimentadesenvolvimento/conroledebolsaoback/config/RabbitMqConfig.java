package com.pimentadesenvolvimento.conroledebolsaoback.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true")
public class RabbitMqConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.queue.user-created}")
    private String userCreatedQueueName;

    @Value("${app.rabbitmq.routing-key.user-created}")
    private String userCreatedRoutingKey;

    @Bean
    public Exchange appExchange() {
        return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Queue userCreatedQueue() {
        return new Queue(userCreatedQueueName, true);
    }

    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, Exchange appExchange) {
        return BindingBuilder.bind(userCreatedQueue).to(appExchange).with(userCreatedRoutingKey).noargs();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
