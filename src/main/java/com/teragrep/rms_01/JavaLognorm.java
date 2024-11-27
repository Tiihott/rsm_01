package com.teragrep.rms_01;

import com.sun.jna.Pointer;

public class JavaLognorm {

    private Pointer ctx;;

    public JavaLognorm(Pointer ctx) {
        this.ctx = LibJavaLognorm.INSTANCE.inherittedCtx(ctx);
    }

    public JavaLognorm(String rulebase) {

    }

}
