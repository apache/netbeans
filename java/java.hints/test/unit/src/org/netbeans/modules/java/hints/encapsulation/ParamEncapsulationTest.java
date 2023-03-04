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
