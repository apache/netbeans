/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
