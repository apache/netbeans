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
package org.netbeans.modules.java.hints.encapsulation;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Tomas Zezula
 */
public class ParamEncapsulationTest extends NbTestCase {

    public ParamEncapsulationTest(final String name) {
        super(name);
    }

    public void testAssignToCollectionField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Collection l;\n" +
                       "    public void setList(java.util.Collection p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Collection Field from Parameter");
    }

    public void testAssignToListField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.List l;\n" +
                       "    public void setList(java.util.List p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Collection Field from Parameter");
    }

    public void testAssignToSetField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Set l;\n" +
                       "    public void setList(java.util.Set p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Collection Field from Parameter");
    }

    public void testAssignToMapField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map l;\n" +
                       "    public void setList(java.util.Map p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Collection Field from Parameter");
    }

    public void testAssignToCollectionLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void setList(java.util.Map p) {\n" +
                       "        java.util.Map l;\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToCollectionFromLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map l;\n" +
                       "    public void setList(java.util.Map p) {\n" +
                       "        java.util.Map x = null;\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToCollectionFromColection() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Map l;\n" +
                       "    private java.util.Map x;\n" +
                       "    public void setList(java.util.Map p) {\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToArrayField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Object[] l;\n" +
                       "    public void setArray(Object[] p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Array Field from Parameter");
    }

    public void testAssignToArrayLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void setArray(Object[] p) {\n" +
                       "        Object[] l;\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToArrayFromLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Object[] l;\n" +
                       "    public void setArray(Object[] p) {\n" +
                       "        Object[] x = null;\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToArrayFromArray() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private Object[] l;\n" +
                       "    private Object[] x;\n" +
                       "    public void setArray(Object[] p) {\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToDateField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Date l;\n" +
                       "    public void setDate(java.util.Date p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Date or Calendar Field from Parameter");
    }

    public void testAssignToDateLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void setDate(java.util.Date p) {\n" +
                       "        java.util.Date l;\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToDateFromLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Date l;\n" +
                       "    public void setDate(java.util.Date p) {\n" +
                       "        java.util.Date x = null;\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToDateFromDate() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Date l;\n" +
                       "    private java.util.Date x;\n" +
                       "    public void setDate(java.util.Date p) {\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToCalendarField() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Calendar l;\n" +
                       "    public void setCalendar(java.util.Calendar p) {\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings("4:8-4:9:verifier:Assignment to Date or Calendar Field from Parameter");
    }

    public void testAssignToCalendarLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void setCalendar(java.util.Calendar p) {\n" +
                       "        java.util.Calendar l;\n" +
                       "        l=p;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToCalendarFromLocal() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Calendar l;\n" +
                       "    public void setCalendar(java.util.Calendar p) {\n" +
                       "        java.util.Calendar x = null;\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }

    public void testAssignToCalendarFromCalendar() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private java.util.Calendar l;\n" +
                       "    private java.util.Calendar x;\n" +
                       "    public void setCalendar(java.util.Calendar p) {\n" +
                       "        l=x;\n" +
                       "    }\n" +
                       "}")
                .run(ParamEncapsulation.class)
                .assertWarnings();
    }
}
