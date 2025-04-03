package com.mycompany.gamestatapp;

import com.mycompany.gamestatapp.config.FileStorageConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // Necessario per FileStorageConfig

/**
 *
 * @author Strix89
 */
@SpringBootApplication
@EnableConfigurationProperties({FileStorageConfig.class}) // Abilita la lettura delle properties custom
public class GameStatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameStatsApplication.class, args);
    }
}