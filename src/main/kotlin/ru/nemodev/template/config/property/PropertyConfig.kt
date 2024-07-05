package ru.nemodev.template.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AppProperties::class)
class PropertyConfig

@ConfigurationProperties("awesome-template")
data class AppProperties(
    val test: String
)