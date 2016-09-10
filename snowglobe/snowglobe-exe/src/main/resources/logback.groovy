package com.nirima
// LOGBACK LOGGING CONFIGURATION
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import com.nirima.snowglobe.utils.ThreadBoundLogger

import static ch.qos.logback.classic.Level.WARN

def consolePattern = "SG-EXE %d{HH:mm:ss.SSS} %-5level %logger{36} - %msg%n"

scan("30 seconds")

println "LOGBACK : snowglobe-exe";

appender("STDERR", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = consolePattern;
    }
    target = "System.err"
}


appender("Client", ThreadBoundLogger ) {
    encoder(PatternLayoutEncoder) {
        pattern = "SGSE2 %d [%thread] - %m%n"
    }
}


logger("com.github.dockerjava", DEBUG, ["Client"])


//logger("org.hibernate.cache", TRACE, ["STDOUT"])
//logger("org.hibernate.impl", DEBUG, ["STDOUT"])

//logger("jdbc.sqlonly", INFO, ["STDOUT"], false)
//logger("log4jdbc.debug", DEBUG, ["STDOUT"], false)
//logger("jdbc.audit", DEBUG, ["STDOUT"], false)

logger("com.nirima", INFO, ["STDERR"])

root(WARN,["STDOUT"])
