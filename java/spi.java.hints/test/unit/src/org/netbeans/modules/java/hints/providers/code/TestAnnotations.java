/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
//placed in this package intentionally: the JavaHintsAnnotationProcessor ignores all annotations outside this package
package org.netbeans.spi.java.hints;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author lahvac
 */
public class TestAnnotations {
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnnotation1 {
        public boolean b1();
        public boolean b2() default false;
        public boolean b3() default false;
        public int     i1();
        public int     i2() default 1;
        public int     i3() default 1;
        public String  s1();
        public String  s2() default "";
        public String  s3() default "";
        public TestAnnotation2 a1();
//        public TestAnnotation2 a2() default @TestAnnotation2(a1=@TestAnnotation3(as1={"a", "b"}, as3="c"));
//        public TestAnnotation2 a3() default @TestAnnotation2(a1=@TestAnnotation3(as1={"a2", "b2"}, as3="c2"));
        public Class<?>   c1();
        public Class<?>   c2() default Void.class;
        public Class<?>   c3() default Void.class;
        public TestEnum e1();
//        public TestEnum e2() default TestEnum.A;
        public TestEnum e3()/* default TestEnum.A*/;
        public boolean[] ab1();
        public boolean[] ab2() default false;
        public boolean[] ab3() default false;
        public int[]     ai1();
        public int[]     ai2() default 1;
        public int[]     ai3() default 1;
        public String[]  as1();
        public String[]  as2() default "";
        public String[]  as3() default "";
        public TestAnnotation2[] aa1();
//        public TestAnnotation2[] aa2() default @TestAnnotation2(a1=@TestAnnotation3(as1={"a", "b"}, as3="c"));
//        public TestAnnotation2[] aa3() default @TestAnnotation2(a1=@TestAnnotation3(as1={"a2", "b2"}, as3="c2"));
        public Class<?>[]   ac1();
        public Class<?>[]   ac2() default Void.class;
        public Class<?>[]   ac3() default Void.class;
        public TestEnum[] ae1();
//        public TestEnum[] ae2() default TestEnum.A;
        public TestEnum[] ae3()/* default TestEnum.A*/;
    }

    public @interface TestAnnotation2 {
        public TestAnnotation3 a1();
//        public TestAnnotation3 a2() default @TestAnnotation3(as1={"d", "e"}, as3="f");
//        public TestAnnotation3 a3() default @TestAnnotation3(as1={"g", "h"}, as3="i");
    }

    public @interface TestAnnotation3 {
        public String[] as1();
        public String[] as2() default {""};
        public String[] as3() default {""};
    }

    public enum TestEnum {
        A, B, C, D;
    }
}
