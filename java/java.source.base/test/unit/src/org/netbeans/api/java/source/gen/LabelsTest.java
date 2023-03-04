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
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "SetLabelTestClass.java");
        //System.err.println(testFile.getAbsoluteFile().toString());
    }
    
    public void testIdentifiers() throws IOException {
        process(new LabelVisitor());
        assertFiles("testIdentifiers.pass");
    }
    
    public void testNoop() {}

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

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/LabelsTest/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/labels/";
    }
    
}
