/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        "        return super.clone(); //To change body of generated methods, choose Tools | Templates.\n" +
        "    }\n" +
        "}"
        );
    }
}
