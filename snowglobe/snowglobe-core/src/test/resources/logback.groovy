package com.nirima
// LOGBACK LOGGING CONFIGURATION

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.*



def consolePattern = "TEST: %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

scan("30 seconds")



appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = consolePattern;
    }
}


//logger("org.hibernate.cache", TRACE, ["STDOUT"])
//logger("org.hibernate.impl", DEBUG, ["STDOUT"])

//logger("jdbc.sqlonly", INFO, ["STDOUT"], false)
//logger("log4jdbc.debug", DEBUG, ["STDOUT"], false)
//logger("jdbc.audit", DEBUG, ["STDOUT"], false)

root(DEBUG, ["STDOUT", "logfile"])
