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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.codegeneration;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.editor.jelly.GenerateEqualsAndHashCodeOperator;
import org.netbeans.test.java.editor.jelly.GenerateEqualsOperator;

/**
 *
 * @author Jiri Prox
 */
public class CreateEqualsHashcodeTest extends GenerateCodeTestCase {

    public CreateEqualsHashcodeTest(String testMethodName) {
        super(testMethodName);
    }

    public void testEqualsOnly() {
        openSourceFile("org.netbeans.test.java.editor.codegeneration", "testEqualsHashcode");
        editor = new EditorOperator("testEqualsHashcode");
        txtOper = editor.txtEditorPane();
        try {
            editor.requestFocus();
            editor.setCaretPosition(14, 5);
            GenerateCodeOperator.openDialog(GenerateCodeOperator.GENERATE_EQUALS, editor);
            GenerateEqualsOperator geo = new GenerateEqualsOperator();
            JTreeOperator jto = geo.equalsTreeOperator();
            jto.selectRow(0);
            jto.selectRow(2);
            geo.generate();
            String expected = "" +
                    "    @Override\n" +
                    "    public boolean equals(Object obj) {\n" +
                    "        if (obj == null) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        if (getClass() != obj.getClass()) {\n" +
                    "            return false;\n" +
                    "        }\n" +
                    "        final testEqualsHashcode other = (testEqualsHashcode) obj;\n" +
                    "        if (!Objects.equals(this.a, other.a)) {\n"+
                    "            return false;\n"+
                    "        }\n"+
                    "        if (!Objects.equals(this.c, other.c)) {\n"+
                    "            return false;\n"+
                    "        }\n"+                    
                    "        return true;\n" +
                    "    }\n" +
                    "\n";
            waitAndCompare(expected);
        } finally {
            editor.close(false);
        }
    }

    public static void main(String[] args) {
        TestRunner.run(CreateEqualsHashcodeTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(CreateEqualsHashcodeTest.class).enableModules(".*").clusters(".*"));
    }
}
