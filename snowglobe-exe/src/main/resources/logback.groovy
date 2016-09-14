package com.nirima
// LOGBACK LOGGING CONFIGURATION
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.WARN

def consolePattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

scan("30 seconds")



appender("STDERR", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = consolePattern;
    }
    target = "System.err"
}


//logger("org.hibernate.cache", TRACE, ["STDOUT"])
//logger("org.hibernate.impl", DEBUG, ["STDOUT"])

//logger("jdbc.sqlonly", INFO, ["STDOUT"], false)
//logger("log4jdbc.debug", DEBUG, ["STDOUT"], false)
//logger("jdbc.audit", DEBUG, ["STDOUT"], false)

root(WARN, ["STDERR"])
