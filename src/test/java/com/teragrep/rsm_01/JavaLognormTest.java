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

import com.sun.jna.Pointer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaLognormTest {

    @Test
    public void versionTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        String s = javaLognorm.liblognormVersionCheck();
        Assertions.assertEquals("2.0.6", s);
    }

    @Test
    public void ctxTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void setCtxOptsTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        opts.CTXOPT_ADD_EXEC_PATH = false;
        opts.CTXOPT_ADD_ORIGINALMSG = false;
        opts.CTXOPT_ADD_RULE = false;
        opts.CTXOPT_ADD_RULE_LOCATION = false;
        opts.CTXOPT_ALLOW_REGEX = false;
        javaLognorm.liblognormSetCtxOpts(opts);
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void loadSamplesTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        String samplesPath = "src/test/resources/sample.rulebase";
        int i = javaLognorm.liblognormLoadSamples(samplesPath);
        assertEquals(0, i);
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void loadSamplesFromStringTest() {
        assertDoesNotThrow(() -> {
            JavaLognorm javaLognorm = new JavaLognorm();
            int i = javaLognorm.liblognormLoadSamplesFromString("rule=:%all:rest%");
            assertEquals(0, i);
            javaLognorm.liblognormExitCtx();
        });
    }

    @Test
    public void hasAdvancedStatsTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        int i = javaLognorm.liblognormHasAdvancedStats();
        assertEquals(0, i);
    }

    @Test
    public void normalizeTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        LibJavaLognorm.OptionsStruct opts = new LibJavaLognorm.OptionsStruct();
        opts.CTXOPT_ADD_EXEC_PATH = false;
        opts.CTXOPT_ADD_ORIGINALMSG = false;
        opts.CTXOPT_ADD_RULE = false;
        opts.CTXOPT_ADD_RULE_LOCATION = false;
        opts.CTXOPT_ALLOW_REGEX = false;
        javaLognorm.liblognormSetCtxOpts(opts);
        String samplesString = "rule=:%all:rest%";

        int i = javaLognorm.liblognormLoadSamplesFromString(samplesString);
        assertEquals(0, i); // 0 means successful normalization, anything else means an error happened.
        Pointer jref = javaLognorm.liblognormNormalize("offline");

        // cleanup
        javaLognorm.liblognormDestroyResult(jref);
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void readResultTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        String samplesString = "rule=:%all:rest%";
        int i = javaLognorm.liblognormLoadSamplesFromString(samplesString);
        assertEquals(0, i);
        Pointer jref = javaLognorm.liblognormNormalize("offline");

        String s = javaLognorm.liblognormReadResult(jref);
        Assertions.assertEquals("{ \"all\": \"offline\" }", s);

        // cleanup
        javaLognorm.liblognormDestroyResult(jref);
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void destroyResultTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        String samplesString = "rule=:%all:rest%";
        int i = javaLognorm.liblognormLoadSamplesFromString(samplesString);
        assertEquals(0, i);
        Pointer jref = javaLognorm.liblognormNormalize("offline");

        javaLognorm.liblognormDestroyResult(jref);
        Assertions.assertNotNull(jref);

        // cleanup
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void enableDebugTest() {
        JavaLognorm javaLognorm = new JavaLognorm();

        assertDoesNotThrow(() -> {
            javaLognorm.liblognormEnableDebug(1);
        });

        // cleanup
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void setDebugCBTest() {
        JavaLognorm javaLognorm = new JavaLognorm();

        int a = javaLognorm.liblognormSetDebugCB();
        Assertions.assertEquals(0, a); // 0 if setting debug message handler was a success.
        javaLognorm.liblognormEnableDebug(1);

        // Assert debug log messages here. Logs can include memory information so they are not identical in each run.

        // cleanup
        javaLognorm.liblognormExitCtx();
    }

    @Test
    public void setErrMsgCBTest() {
        JavaLognorm javaLognorm = new JavaLognorm();
        int a = javaLognorm.liblognormSetErrMsgCB();
        Assertions.assertEquals(0, a); // 0 if setting error message handler was a success.

        // Assert error log messages here.

        // cleanup
        javaLognorm.liblognormExitCtx();
    }

}
