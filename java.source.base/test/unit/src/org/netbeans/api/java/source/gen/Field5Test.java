/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.modules.java.source.transform.Transformer;

/**
 * Test name change.
 * 
 * @author Pavel Flaska
 */
public class Field5Test extends GeneratorTestBase {
    
    /**
     * Creates a new instance of Field5Test
     */
    public Field5Test(String testName) {
        super(testName);
    }
    
    public void testChangeParName() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test {\n" +
            "    public void hierbasDelLitoral(Test[] arrFile) {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package yerba.mate;\n\n" +
            "import java.io.File;\n\n" +
            "public class Test2 {\n" +
            "    public void hierbasDelLitoral(Test2[] arrFile) {\n" +
            "    }\n" +
            "}\n";

        process(
            new Transformer<Void, Object>() {
            
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    if ("Test".contentEquals(node.getSimpleName())) {
                        System.err.println("visitClass");
                        copy.rewrite(node, make.setLabel(node, "Test2"));
                    }
                    return null;
                }
                
                public Void visitIdentifier(IdentifierTree node, Object p) {
                    super.visitIdentifier(node, p);
                    if ("Test".contentEquals(node.getName())) {
                        System.err.println("visitIdentifier");
                        copy.rewrite(node, make.setLabel(node, "Test2"));
                    }
                    return null;
                }
            }
        );
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
