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

import com.sun.source.tree.VariableTree;
import java.io.IOException;
import junit.textui.TestRunner;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author  Pavel Flaska
 */
public class FieldTest2 extends GeneratorTestBase {

    /** Creates a new instance of FieldTest2 */
    public FieldTest2(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(FieldTest2.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "FieldTest2.java");
    }

    /**
     * Test move field from field group to the single field declaration.
     */
    public void testMoveField() throws IOException {
        System.err.println("testMoveField");
        process(
            new Transformer<Void, Object>() {
                public Void visitVariable(VariableTree node, Object p) {
                    if ("nerudova".contentEquals(node.getName())) {
                        System.err.println(node);
                    }
                    /*
                Field nerudova = null;
                for (Iterator fIt = clazz.getContents().iterator(); fIt.hasNext(); ) {
                    Object feature = fIt.next();
                    if (feature instanceof FieldGroup) {
                        FieldGroup group = (FieldGroup) feature;
                        nerudova = (Field) group.getFields().remove(2);
                        nerudova.setTypeName((TypeReference) ((TypeReferenceImpl) group.getTypeName()).duplicate());
                        nerudova.setModifiers(Modifier.PRIVATE);
                    }
                }
                clazz.getFeatures().add(nerudova);*/
                    return null;
                }
            }
        );
        assertFiles("FieldTest2.pass");
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
        return "org/netbeans/jmi/javamodel/codegen/FieldTest2/";
    }
}
