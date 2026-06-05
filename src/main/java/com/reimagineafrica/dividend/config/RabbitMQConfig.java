package com.reimagineafrica.dividend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange dividendExchange() {
        return new TopicExchange("dividend.exchange");
    }

    @Bean
    public Queue dividendApprovedQueue() {
        return QueueBuilder.durable("dividend.approved").build();
    }

    @Bean
    public Binding dividendApprovedBinding() {
        return BindingBuilder.bind(dividendApprovedQueue())
            .to(dividendExchange()).with("dividend.approved");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
