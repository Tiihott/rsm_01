# Build a libtool library, libJavaLognorm.la.
pkglib_LTLIBRARIES = libJavaLognorm.la
libJavaLognorm_la_SOURCES = JavaPcre.c
libJavaLognorm_la_CPPFLAGS = -I$(top_srcdir) $(PTHREADS_CFLAGS) $(RSRT_CFLAGS) $(JAVA_INCLUDES)
libJavaLognorm_la_LDFLAGS = -module -avoid-version $(JAVA_LIBS)
# liblognorm doesn't seem to have a flag like this: libJavaLognorm_la_LIBADD = -liblognorm