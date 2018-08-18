/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#ifndef _JNI_TYPES_H_
#define _JNI_TYPES_H_

/**
 * @file
 * Types used in JNI and OPEN interfaces.
 *
 * For the most part JNI types are defined by Sun specification on
 * Java Native Interface (JNI). See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html">specification</a>
 * for details.
 */

/* The following documentation is both for windows and linux, so it
 * resides outside of ifdef-endif block */
/**
 * @def JNIEXPORT
 * Function attribute to make native JNI function exportable
 */
/**
 * @def JNIIMPORT
 * Function attribute used when building VM from sources
 */
/**
 * @def JNICALL
 * Function attribute to specify calling conventions that should be
 * used for native JNI functions
 */
/**
 * @typedef jlong
 * Signed 64-bit long type equivalent to Java "long" type
 */
#if defined (_WIN32) || defined (__WIN32__) || defined (WIN32)

#define JNIEXPORT __declspec(dllexport)
#define JNIIMPORT __declspec(dllimport)
#define JNICALL __stdcall

typedef signed __int64 jlong;

#else

#define JNIEXPORT
#define JNIIMPORT
#define JNICALL

typedef signed long long jlong;

#endif

/*
 * Primitive types
 */
/**
 * Unsigned 8-bit primitive boolean type equivalent to Java "boolean"
 * type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef unsigned char jboolean;
/**
 * Signed 8-bit primitive byte type equivalent to Java "byte" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef signed char jbyte;
/**
 * Unsigned 16-bit primitive char type equivalent to Java "char" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef unsigned short jchar;
/**
 * Signed 16-bit primitive short type equivalent to Java "short" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef signed short jshort;
/**
 * Signed 32-bit primitive integer type equivalent to Java "int" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef signed int jint;
/**
 * Signed 32-bit primitive floating point type equivalent to Java
 * "float" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef float jfloat;
/**
 * Signed 64-bit primitive floating point type equivalent to Java
 * "double" type
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef double jdouble;
/**
 * Signed 32-bit primitive integer type used to describe sizes
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp428">specification</a>
 * for details.
 */
typedef jint jsize;

/*
 * Java types
 */
struct _jobject;
/**
 * Reference type which describes a general Java object in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
typedef struct _jobject*  jobject;
/**
 * Reference type which describes a java.lang.Class instance object in
 * native function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
typedef jobject           jclass;
/**
 * Reference type which describes a java.lang.String instance object
 * in native function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
typedef jobject           jstring;
/**
 * Reference type which describes a generic array instance object in
 * native function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
typedef jobject           jarray;
/**
 * Reference type which describes an array of java.lang.Object
 * instances in native function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jobjectArray;
/**
 * Reference type which describes an array of booleans in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jbooleanArray;
/**
 * Reference type which describes an array of bytes type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jbyteArray;
/**
 * Reference type which describes an array of chars type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jcharArray;
/**
 * Reference type which describes an array of shorts type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jshortArray;
/**
 * Reference type which describes an array of ints type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jintArray;
/**
 * Reference type which describes an array of longs type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jlongArray;
/**
 * Reference type which describes an array of floats type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jfloatArray;
/**
 * Reference type which describes an array of doubles type in native
 * function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
    typedef jarray        jdoubleArray;
/**
 * Reference type which describes a java.lang.Throwable instance
 * object in native function
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html#wp15954">specification</a>
 * for details.
 */
typedef jobject           jthrowable;
/**
 * Reference type which describes a weak reference to a general object
 *
 * This type is the same as #jobject but the reference held in it
 * is weak, so if the referred object is weakly reacheable, it may be
 * garbage collected to VM.
 */
typedef jobject           jweak;

/**
 * This union used to pass arguments to native functions when <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/functions.html#wp4256">Call&lt;type&gt;MethodA</a>
 * and <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/functions.html#wp4796">CallStatic&lt;type&gt;MethodA</a>
 * functions are used
 *
 * It consists of all possible primitive Java types plus #jobject, so
 * it is possible to pass any kind of argument type through it.
 */
typedef union jvalue {
    jboolean z;
    jbyte    b;
    jchar    c;
    jshort   s;
    jint     i;
    jlong    j;
    jfloat   f;
    jdouble  d;
    jobject  l;
} jvalue;

/**
 * Type which describes an identfier of a field inside of class
 *
 * This type together with a #jclass reference uniquily identifies a
 * field inside of the class described by #jclass.
 */
typedef struct _jfieldID* jfieldID;

/**
 * Type which describes an identfier of a method inside of class
 *
 * This type together with a #jclass reference uniquily identifies a
 * method inside of the class described by #jclass.
 */
typedef struct _jmethodID* jmethodID;

/*
 * Constants
 */

/*
 * Boolean constants
 */
/**
 * Constant which defines boolean truth in native Java functions. It
 * is equivalent to Java constant "true"
 */
#define JNI_FALSE  0
/**
 * Constant which defines boolean false in native Java functions. It
 * is equivalent to Java constant "false"
 */
#define JNI_TRUE   1

/*
 * Return values
 */
/**
 * Constant which describes success when returned by JNI API functions
 */
#define JNI_OK     0
/**
 * Constant which describes an error when returned by JNI API
 * functions
 */
#define JNI_ERR    (-1)
/**
 * Constant which describes a deatached thread condition when returned
 * by JNI API functions
 */
#define JNI_EDETACHED (-2)
/**
 * Constant which describes wrong JNI interface verions when returned
 * by JNI API functions
 */
#define JNI_EVERSION  (-3)
/**
 * Constant which describes out of memory condition when returned by
 * JNI API functions
 */
#define JNI_ENOMEM    (-4)
/**
 * Constant which means that a limited resource already exists when
 * returned by JNI API functions
 */
#define JNI_EEXIST    (-5)
/**
 * Constant which means that an illegal argument value was passed to a
 * JNI function
 */
#define JNI_EINVAL    (-6)

/*
 * Release modes for working with arrays.
 */
/**
 * Constant which means that an array region should be committed into
 * memory. Used in Release<primitive type>ArrayElements functions
 */
#define JNI_COMMIT 1
/**
 * Constant which means that an array region should be discarded. Used
 * in Release<primitive type>ArrayElements functions
 */
#define JNI_ABORT  2

/*
 * Used as a generic pointer to a function.
 */
/**
 * Structure which describes a generic pointer to a native
 * function. Used in <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/functions.html#wp17734">RegisterNatives</a>
 * function.
 */
typedef struct {
    char *name;
    char *signature;
    void *fnPtr;
} JNINativeMethod;

/*
 * JNI Native Method Interface
 */
struct JNINativeInterface_;
struct JNIEnv_External;

#ifdef __cplusplus
/**
 * JNI API interface table type for usage in C++
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/design.html#wp16696">specification</a>
 * for details. */
typedef JNIEnv_External JNIEnv;
#else
/**
 * JNI API interface table type for usage in C
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/design.html#wp16696">specification</a>
 * for details. */
typedef const struct JNINativeInterface_ *JNIEnv;
#endif

/*
 * JNI Invocation Interface
 */
struct JNIInvokeInterface_;
struct JavaVM_External;

#ifdef __cplusplus
/**
 * Java VM interface table type for usage in C++
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/invocation.html#invocation_api_functions">specification</a>
 * for details
 */
typedef JavaVM_External JavaVM;
#else
/**
 * Java VM interface table type for usage in C
 *
 * See <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/invocation.html#invocation_api_functions">specification</a>
 * for details
 */
typedef const struct JNIInvokeInterface_ *JavaVM;
#endif

#endif /* _JNI_TYPES_H_ */
