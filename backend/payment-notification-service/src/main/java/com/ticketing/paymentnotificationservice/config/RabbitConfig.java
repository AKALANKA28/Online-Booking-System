package com.ticketing.paymentnotificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange ticketingExchange(@Value("${app.rabbitmq.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue bookingConfirmedQueue(@Value("${app.rabbitmq.confirmed-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue bookingFailedQueue(@Value("${app.rabbitmq.failed-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue).to(ticketingExchange).with("booking.confirmed");
    }

    @Bean
    public Binding bookingFailedBinding(Queue bookingFailedQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(bookingFailedQueue).to(ticketingExchange).with("booking.failed");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        // Prefer @RabbitListener parameter type over sender __TypeId__ (other services use different packages).
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}
