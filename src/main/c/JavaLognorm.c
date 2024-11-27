
#include <stddef.h>
#include <string.h>
#include <liblognorm/liblognorm.h>
#include <liblognorm/lognorm.h>

/* compiler commands for this file, may need additional compiler flags for liblognorm like pcre2 did:
 gcc -c -Wall -Werror -fpic src/main/c/JavaLognorm.c -o src/main/c/JavaLognorm.o -I/usr/include/liblognorm -I/usr/include/json-c
 gcc -shared -Wl,-soname,JavaLognorm.so -o src/main/c/JavaLognorm.so src/main/c/JavaLognorm.o

 Autotools setup commands for this file:
 autoreconf --install
 ./configure
 make

 None of the above matters if maven works as intended and executes the autotools scripts automatically.*/

typedef struct OptionsStruct_TAG {
    int CTXOPT_ALLOW_REGEX;
    int CTXOPT_ADD_EXEC_PATH;
    int CTXOPT_ADD_ORIGINALMSG;
    int CTXOPT_ADD_RULE;
    int CTXOPT_ADD_RULE_LOCATION;
}OptionsStruct;

const char *version() {
    return ln_version();
}

void *initCtx() {
    ln_ctx *ctx = malloc(sizeof(ln_ctx));
    if((*ctx = ln_initCtx()) == NULL) {
        // add exception handling here. ln_ returns null if error occurred.
        return NULL;
    }
    return ctx;
}

void *inherittedCtx(ln_ctx parent) {
    ln_ctx *ctx = malloc(sizeof(ln_ctx));
    if((*ctx = ln_inherittedCtx(parent)) == NULL) {
        // add exception handling here. ln_ returns null if error occurred.
        return NULL;
    }
    return ctx;
}

void exitCtx(ln_ctx *context) {
    if (*context) {
        ln_exitCtx(*context);
    }
    free(context);
}

int loadSamples(ln_ctx *context, char *filename) {
    return ln_loadSamples(*context, filename);
}

int loadSamplesFromString(ln_ctx *context, char *string) {
    return ln_loadSamplesFromString(*context, string);
}

