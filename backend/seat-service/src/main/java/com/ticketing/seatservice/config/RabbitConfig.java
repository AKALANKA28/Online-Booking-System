package com.ticketing.seatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange ticketingExchange(@Value("${app.rabbitmq.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue eventCreatedQueue(@Value("${app.rabbitmq.queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding eventCreatedBinding(Queue eventCreatedQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(eventCreatedQueue).to(ticketingExchange).with("event.created");
    }

    @Bean
    public Queue eventCancelledQueue(@Value("${app.rabbitmq.cancelled-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean  
    public Binding eventCancelledBinding(Queue eventCancelledQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(eventCancelledQueue).to(ticketingExchange).with("event.cancelled");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
