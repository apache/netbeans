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
package org.netbeans.modules.javafx2.editor.parser;

import java.util.Collection;
import org.netbeans.modules.javafx2.editor.completion.model.FxTreeUtilities;
import org.netbeans.modules.javafx2.editor.ErrorMark;
import org.netbeans.modules.javafx2.editor.GoldenFileTestBase;
import org.netbeans.modules.javafx2.editor.completion.model.FxNewInstance;
import org.netbeans.modules.javafx2.editor.sax.XmlLexerParser;

/**
 *
 * @author sdedic
 */
public class FxmlBuilderTest extends GoldenFileTestBase {
    FxModelBuilder builder;
    
    public FxmlBuilderTest(String testName) {
        super(testName);
    }
    
//    public static TestSuite suite() {
//        TestSuite ts = new TestSuite();
//        ts.addTest(new FxmlBuilderTest("testBrokenHierarchy"));
//        return ts;
//    }
    
    public void testBaseLoad() throws Exception {
        final XmlLexerParser parser = new XmlLexerParser(hierarchy);
        builder = new FxModelBuilder();
        parser.setContentHandler(builder);
        
        
        final Exception[] exc = new Exception[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    parser.parse();
                } catch (Exception ex) {
                    exc[0] = ex;
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }

        PrintVisitor v = new PrintVisitor(new FxTreeUtilities(ModelAccessor.INSTANCE, builder.getModel(), hierarchy));
        builder.getModel().accept(v);
        
    }

    /**
     * Tests recovery in half-written processing instructions
     * @throws Exception 
     */
    public void testIncompletePi() throws Exception {
        defaultTestContents();
    }
    
    /**com
     * Tests recovery when writing attributes.
     * 
     * @throws Exception Test
     */
    public void testBrokenAttributes() throws Exception {
        defaultTestContents();
    }

    public void testBrokenElements() throws Exception {
        defaultTestContents();
    }
    
    public void testBrokenHierarchy() throws Exception {
        defaultTestContents();
    }

    private void defaultTestContents() throws Exception {
        final XmlLexerParser parser = new XmlLexerParser(hierarchy);
        builder = new FxModelBuilder();
        parser.setContentHandler(builder);
        
        final Exception[] exc = new Exception[1];
        document.render(new Runnable() {
            public void run() {
                try {
                    parser.parse();
                } catch (Exception ex) {
                    exc[0] = ex;
                }
            }
        });
        if (exc[0] != null) {
            throw exc[0];
        }

        PrintVisitor v = new PrintVisitor(
                new FxTreeUtilities(ModelAccessor.INSTANCE, builder.getModel(), hierarchy));
        builder.getModel().accept(v);
        
        StringBuilder sb = v.out;
        sb.append("\n\n");
        for (ErrorMark em : parser.getErrors()) {
            sb.append(em).append("\n");
        }
        for (ErrorMark em : builder.getErrors()) {
            sb.append(em).append("\n");
        }
        
        assertContents(v.out);
    }
    
    /**
     * Checks that fx:define is recognized, and that those beans are resolved
     * when referenced from fx:copy, fx:reference.
     * 
     * @throws Exception 
     */
    public void testDefinedBeans() throws Exception {
        defaultTestContents();
        Collection<FxNewInstance> defs = builder.getModel().getDefinitions();
        assertEquals(2, defs.size());
        for (FxNewInstance d : defs) {
            String id = d.getId();
            assertNotNull(id);
            assertTrue(id.equals("SubmitButtonX") || id.equals("OptOutCheckBox"));
        }
    }
    
}
