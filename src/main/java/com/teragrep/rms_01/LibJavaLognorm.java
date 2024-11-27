package com.teragrep.rms_01;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

public interface LibJavaLognorm  extends Library {
    LibJavaLognorm INSTANCE = Native.load("com.teragrep.rsm_01.JavaLognorm", LibJavaLognorm.class);

    @FieldOrder({ "CTXOPT_ALLOW_REGEX", "CTXOPT_ADD_EXEC_PATH", "CTXOPT_ADD_ORIGINALMSG", "CTXOPT_ADD_RULE", "CTXOPT_ADD_RULE_LOCATION"})
    class OptionsStruct extends Structure {
        public boolean CTXOPT_ALLOW_REGEX;
        public boolean CTXOPT_ADD_EXEC_PATH;
        public boolean CTXOPT_ADD_ORIGINALMSG;
        public boolean CTXOPT_ADD_RULE;
        public boolean CTXOPT_ADD_RULE_LOCATION;
    }

    // Returns the version of the currently used library.
    public abstract String version();

    // init initializes the liblognorm context. deinit() must be called on the produced context when it is not needed anymore.
    public abstract Pointer initCtx();

    // Inherit control attributes from a library context. deinit() must be called on the produced context when it is not needed anymore.
    public abstract Pointer inherittedCtx(Pointer ctx);

    // To prevent memory leaks, deinit() must be called on a library context that is no longer needed.
    public abstract void exitCtx(Pointer ctx);

    // Set options on ctx.
    public abstract void setCtxOpts(Pointer ctx, OptionsStruct opts);

    // Return 1 if library is build with advanced statistics activated, 0 if not.
    public abstract int hasAdvancedStats();

    // Loads the rulebase from a file to the context.
    public abstract void loadSamples(Pointer ctx, String filename);

    // Loads the rulebase via a string to the context.
    public abstract void loadSamplesFromString(Pointer ctx, String string);

    // normalize gets the log string that is being normalized as input argument, along with the library context. Returns pointer to a json object.
    public abstract Pointer normalize(Pointer ctx, String text);

    // Reads the results of the normalization.
    public abstract String readResult(Pointer jref);

    // Releases the results of the normalization from memory.
    public abstract void destroyResult(Pointer jref);
}
