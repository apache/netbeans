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

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.modules.java.source.transform.Transformer;

/**
 *
 * @author Pavel Flaska
 */
public class LabelsTest extends GeneratorTestBase {
    
    /** Creates a new instance of LabelsTest */
    public LabelsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new LabelsTest("testIdentifiers"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "SetLabelTestClass.java");
        System.err.println(testFile.getAbsoluteFile().toString());
    }
    
    public void testIdentifiers() throws IOException {
        process(new LabelVisitor());
        assertFiles("testIdentifiers.pass");
    }
    
    class LabelVisitor<Void, Object> extends Transformer<Void, Object> {

        public Void visitMethod(MethodTree node, Object p) {
            System.err.println("visitMethod: " + node.getName());
            super.visitMethod(node, p);
            MethodTree copy = make.setLabel(node, node.getName() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitBreak(BreakTree node, Object p) {
            System.err.println("visitBreak: " + node.getLabel());
            super.visitBreak(node, p);
            BreakTree copy = make.setLabel(node, node.getLabel() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitContinue(ContinueTree node, Object p) {
            System.err.println("visitContinue: " + node.getLabel());
            super.visitContinue(node, p);
            ContinueTree copy = make.setLabel(node, node.getLabel() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitClass(ClassTree node, Object p) {
            System.err.println("visitClass: " + node.getSimpleName());
            super.visitClass(node, p);
            ClassTree copy = make.setLabel(node, node.getSimpleName() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitLabeledStatement(LabeledStatementTree node, Object p) {
            System.err.println("visitLabeledStatement: " + node.getLabel());
            super.visitLabeledStatement(node, p);
            LabeledStatementTree copy = make.setLabel(node, node.getLabel() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitMemberSelect(MemberSelectTree node, Object p) {
            System.err.println("visitMemberSelect: " + node.getIdentifier());
            super.visitMemberSelect(node, p);
            MemberSelectTree copy = make.setLabel(node, node.getIdentifier() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }
        
        public Void visitIdentifier(IdentifierTree node, Object p) {
            System.err.println("visitIdentifier: " + node.getName());
            super.visitIdentifier(node, p);
            System.err.println("I: " + node);
            IdentifierTree copy = make.setLabel(node, node.getName() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitTypeParameter(TypeParameterTree node, Object p) {
            System.err.println("visitTypeParameter: " + node.getName());
            super.visitTypeParameter(node, p);
            TypeParameterTree copy = make.setLabel(node, node.getName() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

        public Void visitVariable(VariableTree node, Object p) {
            System.err.println("visitVariable: " + node.getName());
            super.visitVariable(node, p);
            VariableTree copy = make.setLabel(node, node.getName() + "0");
            this.copy.rewrite(node, copy);
            return null;
        }

    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/LabelsTest/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/labels/";
    }
    
}
