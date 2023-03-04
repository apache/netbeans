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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import junit.textui.TestRunner;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Tests more transaction on file with fields. Tests fields generating and
 * also update.
 *
 * @author  Pavel Flaska
 */
public class FieldTest4 extends GeneratorTestBase {
    
    /** Creates a new instance of FieldTest4 */
    public FieldTest4(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest4.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest4.java");
    }

    /**
     * Tests inital value for fields in field group.
     */
    public void testAddField() throws IOException {
        process(
            new Transformer<Void, Object>() {
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    VariableTree vtecko = make.Variable(
                            make.Modifiers(Collections.singleton(Modifier.PROTECTED)),
                            "newField",
                            make.Identifier("String"),
                            null
                    );
                    List<Tree> memberDecl = new ArrayList<Tree>(node.getMembers());
                    memberDecl.add(vtecko);
                    ClassTree ct = make.Class(node.getModifiers(),
                            node.getSimpleName(),
                            node.getTypeParameters(),
                            node.getExtendsClause(),
                            (List<ExpressionTree>) node.getImplementsClause(),
                            memberDecl);
                    model.setElement(ct, model.getElement(node));
                    model.setType(ct, model.getType(node));
                    copyCommentTo(node, ct);
                    copy.rewrite(node, ct);
                    return null;
                }
            }
        );
        assertFiles("testAddField_FieldTest4.pass");
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/FieldTest4/";
    }
}
