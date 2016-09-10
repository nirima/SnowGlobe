// LOGBACK LOGGING CONFIGURATION

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import com.nirima.snowglobe.utils.ThreadBoundLogger

import static ch.qos.logback.classic.Level.*

def consolePattern = "SGW %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

scan("30 seconds")

println "LOGBACK : snowglobe-war";

appender("STDOUT", ConsoleAppender) {
    withJansi = true;
    encoder(PatternLayoutEncoder) {
        pattern = "SGSW [%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n";
    }
}

appender("Client", ThreadBoundLogger ) {
    encoder(PatternLayoutEncoder) {
        pattern = "SGSW %d [%thread] - %m%n"
    }
}


//logger("com.github.dockerjava", DEBUG, ["Client"])

// Uncomment this for tracing of calls
//logger("net.realtimehealth.realtime.services.log.Logger", TRACE, ["tracer"], false)

logger( "com.github.dockerjava.core.command", DEBUG, ["Client"])

logger("org.quartz.simpl.PropertySettingJobFactory", ERROR)

logger("org.hibernate.engine.StatefulPersistenceContext.ProxyWarnLog", ERROR)


// Turn down the 'authentication success' warning
logger("org.springframework.security.event.authentication.LoggerListener", ERROR)



root(INFO, ["STDOUT", "Client"])
