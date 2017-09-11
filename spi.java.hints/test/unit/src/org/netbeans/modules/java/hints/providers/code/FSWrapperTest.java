/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.providers.code;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.sound.sampled.LineListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.AnnotatableWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.ClassWrapper;
import org.netbeans.modules.java.hints.providers.code.FSWrapper.MethodWrapper;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TestAnnotations.TestAnnotation1;
import org.netbeans.spi.java.hints.TestAnnotations.TestAnnotation2;
import org.netbeans.spi.java.hints.TestAnnotations.TestAnnotation3;
import org.netbeans.spi.java.hints.TestAnnotations.TestEnum;

/**
 *
 * @author lahvac
 */
public class FSWrapperTest {

    public FSWrapperTest() {
    }

    @Test
    public void testWrappedClasses() throws Exception {
        Class[] classes = new Class[] {TestClass.class};
        Iterable<? extends ClassWrapper> wrapped = FSWrapper.listClasses();

        OUTER: for (Class<?> c : classes) {
            for (ClassWrapper w : wrapped) {
                if (w.getName().equals(c.getName())) {
                    checkClassWrapper(c, w);
                    continue OUTER;
                }
            }

            fail(c.getName());
        }
    }

    private static void checkClassWrapper(Class<?> c, ClassWrapper cw) throws Exception {
        checkAnnotations(c, cw);

        OUTER: for (Method m : c.getDeclaredMethods()) {
            if (m.getAnnotations().length == 0) continue;
            
            for (MethodWrapper wrapped : cw.getMethods()) {
                if (wrapped.getName().equals(m.getName())) {
                    assertEquals(m, FSWrapper.resolveMethod(wrapped.getClazz().getName(), wrapped.getName()));
                    checkAnnotations(m, wrapped);
                    continue OUTER;
                }
            }
            
            fail(m.getName());
        }
    }

    private static void checkAnnotations(AnnotatedElement el, AnnotatableWrapper aw) throws Exception {
        for (Annotation ann : el.getAnnotations()) {
            Annotation wrapper = aw.getAnnotation(ann.annotationType());

            assertNotNull(ann.annotationType().getName(), wrapper);

            checkAnnotation(ann, wrapper);
        }
    }

    private static void checkAnnotation(Annotation real, Annotation wrapped) throws Exception {
        for (Method m : real.annotationType().getDeclaredMethods()) {
            Object realValue = m.invoke(real);
            Object wrappedValue = m.invoke(wrapped);

            checkValue(realValue, wrappedValue);
        }
    }

    private static void checkValue(Object o1, Object o2) throws Exception {
        assertEquals(o1.getClass().isAnnotation(), o2.getClass().isAnnotation());
        if (o1.getClass().isAnnotation()) {
            assertEquals(((Annotation) o1).annotationType(), ((Annotation) o2).annotationType());
        } else {
            assertEquals(o1.getClass(), o2.getClass());
        }

        if (o1.getClass().isArray()) {
            assertEquals(Array.getLength(o1), Array.getLength(o2));

            for (int c = 0; c < Array.getLength(o1); c++) {
                checkValue(Array.get(o1, c), Array.get(o2, c));
            }
        } else if (o1.getClass().isAnnotation()) {
            checkAnnotation((Annotation) o1, (Annotation) o2);
        } else {
            assertEquals(o1, o2);
        }
    }

    @Hint(displayName="foo", description="bar", category="")
    @TestAnnotation1(
        b1=true,
        b3=true,
        i1=42,
        i3=84,
        s1="a42",
        s3="a84",
        a1=@TestAnnotation2(a1=@TestAnnotation3(as1={"u1a2", "u1b2"}, as3="u1c2")),
//        a3=@TestAnnotation2(a1=@TestAnnotation3(as1={"u1a2", "u1b2"}, as3="u1c2")),
        c1=String.class,
        c3=String.class,
        e1=TestEnum.C,
        e3=TestEnum.D,
        ab1={false, true},
        ab3={false, true, false},
        ai1={84, 42},
        ai3={84, 42, 84},
        as1={"c42", "c84"},
        as3={"c42", "c84", "c42"},
        aa1={@TestAnnotation2(a1=@TestAnnotation3(as1={"u3a2", "u3b2"}, as3="u3c2")), @TestAnnotation2(a1=@TestAnnotation3(as1={"u4a2", "u4b2"}, as3="u4c2"))},
//        aa3={@TestAnnotation2(a1=@TestAnnotation3(as1={"u5a2", "u5b2"}, as3="u5c2")), @TestAnnotation2(a1=@TestAnnotation3(as1={"u6a2", "u6b2"}, as3="u6c2"))},
        ac1={List.class, Integer.class},
        ac3={LineListener.class, LinkedList.class},
        ae1={TestEnum.C, TestEnum.D},
        ae3={TestEnum.D, TestEnum.C}
    )
    public static class TestClass {
        @TestAnnotation1(
            b1=false,
            b2=true,
            i1=43,
            i2=85,
            s1="w42",
            s2="w84",
            a1=@TestAnnotation2(a1=@TestAnnotation3(as1={"u7a2", "u7b2"}, as3="u7c2")),
//            a2=@TestAnnotation2(a1=@TestAnnotation3(as1={"u8a2", "u8b2"}, as3="u8c2")),
            c1=String.class,
            c2=String.class,
            e1=TestEnum.C,
            e3=TestEnum.D,
            ab1={false, true},
            ab2={false, true, false},
            ai1={85, 43},
            ai2={85, 43, 85},
            as1={"d42", "d84"},
            as2={"e42", "e84", "e42"},
            aa1={@TestAnnotation2(a1=@TestAnnotation3(as1={"u9a2", "u9b2"}, as3="u9c2")), @TestAnnotation2(a1=@TestAnnotation3(as1={"uaa2", "u4b2"}, as3="uac2"))},
//            aa2={@TestAnnotation2(a1=@TestAnnotation3(as1={"uba2", "ubb2"}, as3="ubc2")), @TestAnnotation2(a1=@TestAnnotation3(as1={"uca2", "ucb2"}, as3="ucc2"))},
            ac1={ArrayList.class, Float.class},
            ac2={StringBuilder.class, Map.class},
            ae1={TestEnum.C, TestEnum.D},
            ae3={TestEnum.D, TestEnum.C}
        )
        public static void test(HintContext ctx) {
            
        }
    }

    public static class CustomizerImpl implements CustomizerProvider {
        @Override public JComponent getCustomizer(Preferences prefs) {
            return new JPanel();
        }
    }

}
