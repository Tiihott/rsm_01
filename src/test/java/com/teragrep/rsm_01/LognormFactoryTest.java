/*
 * Record Schema Mapping Library for Java RSM-01
 * Copyright (C) 2021-2024 Suomen Kanuuna Oy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Additional permission under GNU Affero General Public License version 3
 * section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with other code, such other code is not for that reason alone subject to any
 * of the requirements of the GNU Affero GPL version 3 as long as this Program
 * is the same Program as licensed from Suomen Kanuuna Oy without any additional
 * modifications.
 *
 * Supplemented terms under GNU Affero General Public License version 3
 * section 7
 *
 * Origin of the software must be attributed to Suomen Kanuuna Oy. Any modified
 * versions must be marked as "Modified version of" The Program.
 *
 * Names of the licensors and authors may not be used for publicity purposes.
 *
 * No rights are granted for use of trade names, trademarks, or service marks
 * which are in The Program if any.
 *
 * Licensee must indemnify licensors and authors for any liability that these
 * contractual assumptions impose on licensors and authors.
 *
 * To the extent this program is licensed as part of the Commercial versions of
 * Teragrep, the applicable Commercial License may apply to this file if you as
 * a licensee so wish it.
 */
package com.teragrep.rsm_01;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class LognormFactoryTest {

    @BeforeAll
    public static void log4jconfig() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2Error.properties");
        Configurator.reconfigure(log4j2Config.toUri());
    }

    @Test
    public void defaultCtxOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct(); // All opts disabled by default
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that only normalized message is in the result.
                Assertions.assertEquals("{ \"all\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void originalMsgOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_ORIGINALMSG = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that original message is included in the result to see if opts are working
                Assertions.assertEquals("{ \"all\": \"offline\", \"originalmsg\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void ruleOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_RULE = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that rule is included in the result to see if opts are working
                Assertions
                        .assertEquals(
                                "{ \"all\": \"offline\", \"metadata\": { \"rule\": { \"mockup\": \"%all:rest%\" } } }",
                                s
                        );
            }
        });
    }

    @Test
    public void locationOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_RULE_LOCATION = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that rule file location information is included in the result to see if opts are working
                Assertions
                        .assertEquals(
                                "{ \"all\": \"offline\", \"metadata\": { \"rule\": { \"location\": { \"file\": \"--NO-FILE--\", \"line\": 0 } } } }",
                                s
                        );
            }
        });
    }

    @Test
    public void pathOptsV2Test() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=:%all:rest%"; // Loading rulebase as string is always V2.
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that CTXOPT_ADD_EXEC_PATH is no-op and doesn't affect the normalization result.
                Assertions.assertEquals("{ \"all\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void pathOptsV2ExceptionTest() {
        // Assert that CTXOPT_ADD_EXEC_PATH is no-op and doesn't affect the normalization result. Read logs to assert normalization result during exception.
        String samplesString = "rule=tag1:Quantity: %N:number%"; // Loading rulebase as string is always V2.
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2Error.properties");
        Configurator.reconfigure(log4j2Config.toUri());

        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        opts.CTXOPT_ADD_EXEC_PATH = true;
        LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
        JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();

        Logger loggerForTarget = (Logger) LogManager.getLogger(JavaLognormImpl.class);
        String expectedLogMessages = "ln_normalize() failed to perform extraction with error code <-1000>. Generated error information: <{ \"originalmsg\": \"unparseable\", \"unparsed-data\": \"unparseable\" }>";

        final Appender appender = mock(Appender.class);
        when(appender.getName()).thenReturn("Mock appender");
        when(appender.isStarted()).thenReturn(true);
        final ArgumentCaptor<LogEvent> logCaptor = ArgumentCaptor.forClass(LogEvent.class);
        final Level effectiveLevel = loggerForTarget.getLevel(); // Save the initial logger state
        // Attach our test appender and make sure the messages will be logged
        loggerForTarget.addAppender(appender);
        loggerForTarget.setLevel(Level.ERROR);
        try {
            // invoke error callback
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> javaLognormImpl.normalize("unparseable"));
            Assertions
                    .assertEquals("ln_normalize() failed to perform extraction with error code: -1000", e.getMessage());
            // Assert that the expected log messages are seen after the exception is thrown
            verify(appender, times(1)).append(logCaptor.capture());
            Arrays.stream(new String[] {
                    expectedLogMessages
            }
            )
                    .forEach(
                            expectedLogMessage -> Assertions
                                    .assertEquals(
                                            expectedLogMessage, logCaptor.getValue().getMessage().getFormattedMessage()
                                    )
                    );
            javaLognormImpl.close();
        }
        finally {
            // Restore logger state in case this affects other tests
            loggerForTarget.removeAppender(appender);
            loggerForTarget.setLevel(effectiveLevel);
        }
    }

    @Test
    public void pathOptsV1Test() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebase"; // V1 rulebase file
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = true;
            LognormFactory lognormFactory = new LognormFactory(opts, sampleFile);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("offline");
                // Assert that CTXOPT_ADD_EXEC_PATH is no-op and doesn't affect the normalization result.
                Assertions.assertEquals("{ \"all\": \"offline\" }", s);
            }
        });
    }

    @Test
    public void pathOptsV1ExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/regex.rulebase"; // V1 rulebase file
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = true;
            LognormFactory lognormFactory = new LognormFactory(opts, sampleFile);
            try (JavaLognormImpl javaLognormImpl = lognormFactory.lognorm()) {
                String s = javaLognormImpl.normalize("regex: 1234");
                // Assert that CTXOPT_ADD_EXEC_PATH is no-op and doesn't affect the normalization result.
                // FIXME: Something is very wrong with the V1 normalization engine of liblognorm. No errors are observed by the library but normalization has failed and the output is in error format.
                Assertions.assertEquals("{ \"originalmsg\": \"regex: 1234\", \"unparsed-data\": \"1234\" }", s);
            }
        });
    }

    @Test
    public void regexOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesString = "rule=regex:regex: %token:regex:abc.ef%";
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ALLOW_REGEX = true;
            LognormFactory lognormFactory = new LognormFactory(opts, samplesString);
            // Assert that using regex throws an exception when used with v2 engine with CTXOPT_ALLOW_REGEX option enabled.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebase";
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesRegexTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/regex.rulebase";
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesJsonTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/json.rulebase"; // rulebase in pure json format with v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesJsonLiteralTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/jsonLiteral.rulebase"; // rulebase in json using literal format with v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesWithOptsTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebase";
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, sampleFile);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/sample.rulebas"; // invalid path
            File sampleFile = new File(samplesPath);
            Assertions.assertFalse(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("ln_loadSamples() returned 1 instead of 0", e.getMessage());
        });
    }

    @Test
    public void loadSamplesJsonV1CallbackExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/jsonv1.rulebase"; // rulebase in pure json format without v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            // ln_loadSamples() doesn't return an error code but the liblognorm error callback logs an error during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesJsonLiteralCallbackExceptionTest() {
        assertDoesNotThrow(() -> {
            String samplesPath = "src/test/resources/jsonv1Literal.rulebase"; // rulebase in json using literal format without v2 engine tag
            File sampleFile = new File(samplesPath);
            Assertions.assertTrue(sampleFile.exists());
            LognormFactory lognormFactory = new LognormFactory(sampleFile);
            // ln_loadSamples() doesn't return an error code but the liblognorm error callback logs an error during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> lognormFactory.lognorm());
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "rule=:%all:rest%");
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringWithOptsTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "rule=:%all:rest%");
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringJsonTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            String testingRulebase = "rule=:%{\"type\":\"date-rfc3164\", \"name\":\"date\"}" + "      % %"
                    + "       {\"type\":\"char-to\", \"name\":\"host\", \"extradata\":\":\"}"
                    + "      % no longer listening on %" + "        {\"type\":\"ipv4\", \"name\":\"ip\"}" + "      %#%"
                    + "        {\"type\":\"number\", \"name\":\"port\"}" + "      %";
            LognormFactory lognormFactory = new LognormFactory(opts, testingRulebase);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm(); // throws if loading fails
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesFromStringExceptionTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "invalidRulebase");
            // ln_loadSamplesFromString() doesn't return an error code but the liblognorm error callback logs an error during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, lognormFactory::lognorm);
            Assertions.assertEquals("<1> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

    @Test
    public void loadSamplesFromStringMultipleExceptionTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts, "invalidRulebase\nmoreInvalidRules");
            // ln_loadSamplesFromString() doesn't return an error code but the liblognorm error callback logs 2 errors during rulebase loading triggering an exception.
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, lognormFactory::lognorm);
            Assertions.assertEquals("<2> liblognorm errors have occurred, see logs for details.", e.getMessage());
        });
    }

}
