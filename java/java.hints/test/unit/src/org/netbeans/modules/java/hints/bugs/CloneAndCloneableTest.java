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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author
 * sdedic
 */
public class CloneAndCloneableTest extends NbTestCase {

    public CloneAndCloneableTest(String name) {
        super(name);
    }
    
    /**
     * Checks that super.clone will be found
     * @throws Exception 
     */
    public void testCloneCallsSuperClone() throws Exception {
        HintTest.create().input("CloneTest.java", 
        "package test;\n" +
        "public class CloneTest implements Cloneable {\n" +
        "\n" +
        "    @Override\n" +
        "    public Object clone() throws CloneNotSupportedException {\n" +
        "        return null;\n" +
        "    }\n" +
        "}"
        ).run(CloneAndCloneable.class).
        assertWarnings("4:18-4:23:verifier:clone() does not call super.clone()");
    }


    public void testCloneNoWarnings() throws Exception {
        HintTest.create().input("CloneTestN.java", 
        "package test;\n" +
        "public class CloneTestN implements Cloneable {\n" +
        "    @Override\n" +
        "    public Object clone() throws CloneNotSupportedException {\n" +
        "        return super.clone();\n" +
        "    }\n" +
        "}"
        ).run(CloneAndCloneable.class).assertWarnings();

        // check also that super.clone can be somewhere deep inside.
        HintTest.create().input("test/CloneTestN.java", 
        "package test;\n" +
        "public class CloneTestN implements Cloneable {\n" +
        "    @Override\n" +
        "    public Object clone() throws CloneNotSupportedException {\n" +
        "        if (Boolean.getBoolean(\"foo\")) { return super.clone(); } else { return super.clone(); } \n" +
        "    }\n" +
        "}"
        ).run(CloneAndCloneable.class).assertWarnings();
    }
    
    public void testCloneNotSupportedExceptionThrown() throws Exception {
        HintTest.create().input("test/CloneTest2.java",
        "package test;\n" +
        "public class CloneTest2 implements Cloneable {\n" +
        "\n" +
        "    @Override\n" +
        "    public Object clone() {\n" +
        "        return super.clone();\n" +
        "    }\n" +
        "}", false
        ).run(CloneAndCloneable.class).
        findWarning("4:18-4:23:verifier:clone() does not throw CloneNotSupportedException").applyFix().
        assertCompilable("test/CloneTest2.java").
        assertOutput(
            "test/CloneTest2.java",
            "package test;\n" +
            "public class CloneTest2 implements Cloneable {\n" +
            "\n" +
            "    @Override\n" +
            "    public Object clone() throws CloneNotSupportedException {\n" +
            "        return super.clone();\n" +
            "    }\n" +
            "}"
        );
    }
    
    /**
     * Checks a hint that suggest (and fixes) to implement Cloneable if the class
     * has a public clone() method.
     */
    public void testCloneWithoutCloneable() throws Exception {
        HintTest.create().input("test/CloneTest3.java",
            "package test;\n" +
            "public class CloneTest3 {\n" +
            "\n" +
            "    @Override\n" +
            "    public Object clone() throws CloneNotSupportedException {\n" +
            "        return super.clone();\n" +
            "    }\n" +
            "}"
        ).run(CloneAndCloneable.class).
        findWarning("1:13-1:23:verifier:clone() in non-Cloneable class").applyFix().assertCompilable("test/CloneTest3.java").
        assertOutput("test/CloneTest3.java", 
            "package test;\n" +
            "public class CloneTest3 implements Cloneable {\n" +
            "\n" +
            "    @Override\n" +
            "    public Object clone() throws CloneNotSupportedException {\n" +
            "        return super.clone();\n" +
            "    }\n" +
            "}"
        );
    }
    
    /**
     * Cloneable is recognized in multi-iface class
     */
    public void testCloneWithoutCloneableMultiIface() throws Exception {
        HintTest.create().input("test/CloneTest3.java",
            "package test;\n" +
            "public class CloneTest3 implements java.io.Serializable, Cloneable {\n" +
            "\n" +
            "    @Override\n" +
            "    public Object clone() throws CloneNotSupportedException {\n" +
            "        return super.clone();\n" +
            "    }\n" +
            "}"
        ).run(CloneAndCloneable.class).assertWarnings();
    }
    
    /**
     * The subclass gets Clonable from the inheritance
     * @throws Exception 
     */
    public void testCloneWithoutCloneableInSubclass() throws Exception {
        HintTest.create().input("test/CloneTest3N.java",
        "package test;\n" +
        "public class CloneTest3N implements java.io.Serializable, Cloneable {\n" +
        "    @Override\n" +
        "    public Object clone() throws CloneNotSupportedException {\n" +
        "        return super.clone();\n" +
        "    }\n" +
        "    public interface I extends Cloneable {}\n" +
        "    public static class Sub1 extends CloneTest3N {\n" +
        "        @Override\n" +
        "        public Object clone() throws CloneNotSupportedException {\n" +
        "            return super.clone();\n" +
        "        }\n" +
        "    }\n" +
        "    public static class Sub2 implements I {\n" +
        "        @Override\n" +
        "        public Object clone() throws CloneNotSupportedException {\n" +
        "            return super.clone();\n" +
        "        }\n" +
        "    \n" +
        "    }\n" +
        "}"
        ).run(CloneAndCloneable.class).assertWarnings();
    }

    /**
     * Will not suggest the clone-without-cloneable, as clone() is not public yet.
     */
    public void testCloneWithoutCloneableProtected() throws Exception {
        HintTest.create().input("test/CloneTest3.java",
        "package test;\n" +
        "public class CloneTest3 {\n" +
        "    @Override\n" +
        "    protected Object clone() throws CloneNotSupportedException {\n" +
        "        return super.clone();\n" +
        "    }\n" +
        "}"
        ).run(CloneAndCloneable.class).assertWarnings();
    }
    
    public void testCloneableWithoutClone() throws Exception {
        HintTest.create().input("test/CloneTest4.java",
        "package test;\n" +
        "public class CloneTest4 implements Cloneable {\n" +
        "}"
        ).run(CloneAndCloneable.class).
        findWarning("1:13-1:23:verifier:Cloneable class does not implement clone()").applyFix().assertCompilable("test/CloneTest4.java").
        assertOutput("test/CloneTest4.java",
        "package test;\n" +
        "public class CloneTest4 implements Cloneable {\n" +
        "    @Override\n" +
        "    public Object clone() throws CloneNotSupportedException {\n" +
        "        return super.clone(); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody\n" +
        "    }\n" +
        "}"
        );
    }
}
