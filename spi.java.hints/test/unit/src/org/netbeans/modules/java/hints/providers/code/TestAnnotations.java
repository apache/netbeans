/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        public Class   c1();
        public Class   c2() default Void.class;
        public Class   c3() default Void.class;
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
        public Class[]   ac1();
        public Class[]   ac2() default Void.class;
        public Class[]   ac3() default Void.class;
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
