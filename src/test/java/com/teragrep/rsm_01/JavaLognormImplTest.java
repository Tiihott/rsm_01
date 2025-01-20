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

import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class JavaLognormImplTest {

    @BeforeAll
    public static void log4jconfig() {
        // log4j2 configuration
        Path log4j2Config = Paths.get("src/test/resources/log4j2.properties");
        Configurator.reconfigure(log4j2Config.toUri());
    }

    @Test
    public void versionTest() {
        String s = new JavaLognorm.Smart().liblognormVersionCheck();
        Assertions.assertEquals("2.0.6", s);
    }

    @Test
    public void loadSamplesTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            String samplesPath = "src/test/resources/sample.rulebase";
            javaLognormImpl.liblognormLoadSamples(samplesPath); // Throws exception if fails to load samples
            javaLognormImpl.close();
        });
    }

    @Test
    public void loadSamplesExceptionTest() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            IllegalArgumentException e = Assertions
                    .assertThrows(
                            IllegalArgumentException.class,
                            () -> javaLognormImpl.liblognormLoadSamples("src/test/resources/invalid.rulebase")
                    );
            Assertions.assertEquals("Load samples returned 1 instead of 0", e.getMessage());
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            javaLognormImpl.liblognormLoadSamplesFromString("rule=:%all:rest%"); // Throws exception if fails to load samples
            javaLognormImpl.close();
        });
    }

    @Test
    public void hasAdvancedStatsTest() {
        boolean i = new JavaLognorm.Smart().liblognormHasAdvancedStats();
        assertFalse(i);
    }

    @Test
    public void normalizeTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            String samplesString = "rule=:%all:rest%";

            javaLognormImpl.liblognormLoadSamplesFromString(samplesString);
            String s = javaLognormImpl.liblognormNormalize("offline"); // Throws exception if fails.
            Assertions.assertEquals("{ \"all\": \"offline\" }", s);

            // cleanup
            javaLognormImpl.close();
        });
    }

    @Test
    public void normalizeExceptionTest() {
        assertDoesNotThrow(() -> {
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            LognormFactory lognormFactory = new LognormFactory(opts);
            JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
            String samplesString = "invalidRulebase"; // load rulebase that will cause exception

            javaLognormImpl.liblognormLoadSamplesFromString(samplesString);
            IllegalArgumentException e = Assertions
                    .assertThrows(IllegalArgumentException.class, () -> javaLognormImpl.liblognormNormalize("offline"));
            Assertions
                    .assertEquals("ln_normalize() failed to perform extraction with error code: -1000", e.getMessage());

            // cleanup
            javaLognormImpl.close();
        });
    }

    @Test
    public void normalizeExceptionTest2() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = false;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            String samplesString = "rule=tag1:Quantity: %N:number%"; // load rulebase that will cause exception

            javaLognormImpl.liblognormLoadSamplesFromString(samplesString);
            IllegalArgumentException e = Assertions
                    .assertThrows(
                            IllegalArgumentException.class, () -> javaLognormImpl.liblognormNormalize("Quantity: 555a")
                    );
            Assertions
                    .assertEquals("ln_normalize() failed to perform extraction with error code: -1000", e.getMessage());

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void normalizeExceptionTest3() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            javaLognormImpl.liblognormSetErrMsgCB();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = false;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            javaLognormImpl.liblognormSetDebugCB();
            String samplesPath = "src/test/resources/json.rulebase";
            javaLognormImpl.liblognormLoadSamples(samplesPath); // Throws exception if fails to load samples

            IllegalArgumentException e = Assertions
                    .assertThrows(
                            IllegalArgumentException.class, () -> javaLognormImpl.liblognormNormalize("Quantity: 555a")
                    );
            Assertions
                    .assertEquals("ln_normalize() failed to perform extraction with error code: -1000", e.getMessage());

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void normalizeExceptionTest4() {
        assertDoesNotThrow(() -> {
            JavaLognormImpl javaLognormImpl = new JavaLognormImpl();
            javaLognormImpl.liblognormSetErrMsgCB();
            LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
            opts.CTXOPT_ADD_EXEC_PATH = false;
            opts.CTXOPT_ADD_ORIGINALMSG = false;
            opts.CTXOPT_ADD_RULE = false;
            opts.CTXOPT_ADD_RULE_LOCATION = false;
            opts.CTXOPT_ALLOW_REGEX = false;
            javaLognormImpl.liblognormSetCtxOpts(opts);
            javaLognormImpl.liblognormSetDebugCB();
            String samplesPath = "src/test/resources/jsonv1.rulebase";
            javaLognormImpl.liblognormLoadSamples(samplesPath); // Throws exception if fails to load samples

            // Exception handling for normalization completely breaks when rulebase is using JSON while rulebase v2 is not enabled. Issue origin is in liblognorm library.
            String s = javaLognormImpl.liblognormNormalize("Quantity: 555a");
            Assertions
                    .assertEquals("{ \"originalmsg\": \"Quantity: 555a\", \"unparsed-data\": \"Quantity: 555a\" }", s);
            // As seen in the assertion, liblognorm flags the normalization as a success but outputs a JSON holding error information.

            // cleanup
            javaLognormImpl.liblognormExitCtx();
        });
    }

    @Test
    public void setDebugCBTest() {
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        LognormFactory lognormFactory = new LognormFactory(opts);
        JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();

        Assertions.assertDoesNotThrow(javaLognormImpl::liblognormSetDebugCB); // Throws if ln_setDebugCB doesn't return zero.

        // cleanup
        javaLognormImpl.close();
    }

    @Test
    public void setErrMsgCBTest() {
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        LognormFactory lognormFactory = new LognormFactory(opts);
        JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();

        Assertions.assertDoesNotThrow(javaLognormImpl::liblognormSetErrMsgCB); // Throws if ln_errMsgCB doesn't return zero.

        // cleanup
        javaLognormImpl.close();
    }

    @Test
    public void exitCtxTest() {
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        LognormFactory lognormFactory = new LognormFactory(opts);
        JavaLognormImpl javaLognormImpl = lognormFactory.lognorm();
        Assertions.assertDoesNotThrow(javaLognormImpl::close); // Throws if ln_exitCtx doesn't return zero.
    }

}
