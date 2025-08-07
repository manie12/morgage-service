package io.bank.mortgage.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class R2dbcTxConfig {

    @Bean
    TransactionalOperator transactionalOperator(ConnectionFactory cf) {
        return TransactionalOperator.create(new R2dbcTransactionManager(cf));
    }
}