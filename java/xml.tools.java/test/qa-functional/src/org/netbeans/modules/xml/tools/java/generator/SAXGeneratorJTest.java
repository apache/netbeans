/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.xml.tools.java.generator;

import java.util.Arrays;
import java.util.Comparator;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.xml.actions.SAXDocumentHandlerWizardAction;
import org.netbeans.jellytools.modules.xml.saxwizard.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.util.FolderContext;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.tests.xml.JXTest;
import org.openide.cookies.SourceCookie;
import org.openide.loaders.DataObject;


/** Checks XSL Transformation action. */

public class SAXGeneratorJTest extends JXTest {
    
    /** Creates new XMLNodeTest */
    public SAXGeneratorJTest(String testName) {
        super(testName);
    }
    
    // TESTS
    
    /** Performs 'XSL Transformation...' action and checks output. */
    public void test() throws Exception {
        // clear output and display Transformation Dialog
        FolderContext df = FolderContext.getDataFolder(this);
        df.deleteDataObject("sax/BooksSAXBindings.xml");
        df.deleteDataObject("sax/BHImpl.java");
        df.deleteDataObject("sax/BHandler.java");
        df.deleteDataObject("sax/BPImpl.java");
        df.deleteDataObject("sax/BParser.java");
        df.deleteDataObject("sax/BParslet.java");
        
        // perform SAX Wizard Action
        SAXDocumentHandlerWizardAction  saxAction = new SAXDocumentHandlerWizardAction();
        try {
            Node node = findDataNode("sax" + DELIM + "books");
            saxAction.perform(node);
        } catch (Exception ex) {
            fail("Cannot perform SAX Wizard Action.", ex);
        }
        
        // Page-1
        try {
            SAXDocumentHandlerWizardPage1 page1 = new SAXDocumentHandlerWizardPage1();
            page1.verify();
            sleepTest(2000); //!!!
            page1.cboJAXPVersion().selectItem(page1.ITEM_JAXP10);
            page1.cboSAXParserVersion().selectItem(page1.ITEM_SAX20);
            page1.checkPropagateSAXEventsToGeneratedHandler(true);
            if (DEBUG) sleepTest(2000);
            page1.next();
        } catch (Exception ex) {
            fail("Test fails on Page-1.", ex);
        }
        
        // Page-2
        try {
            SAXDocumentHandlerWizardPage2 page2 = new SAXDocumentHandlerWizardPage2();
            page2.verify();
            JTableOperator table = page2.tabElementMappings();
            int row = table.findCellRow("inc-level-1");
            page2.selectHandlerType(row, page2.IGNORE);
            row = table.findCellRow("Product");
            page2.selectHandlerType(row, page2.CONTAINER);
            page2.setHandlerMethod(row, "productHandlerMethod");
            page2.setHandlerMethod(row, "product Handler Method");
            assertEquals("Invalid method name.", "productHandlerMethod", page2.getHandlerMethod(row));
            if (DEBUG) sleepTest(2000);
            page2.next();
        } catch (Exception ex) {
            fail("Test fails on Page-2.", ex);
        }
        
        // Page-3
        try {
            SAXDocumentHandlerWizardPage3 page3 = new SAXDocumentHandlerWizardPage3();
            page3.verify();
            JTableOperator table = page3.tabDataConvertors();
            int row = table.findCellRow("inc-level-1");
            assertEquals("Invalid initial method.", page3.NO_METHOD, page3.getConvertorMethod(row));
            page3.setConvertorMethod(row, "getText");
            assertEquals("Invalid return type.", "java.lang.String", page3.getReturnType(row));
            
            row = table.findCellRow("Price");
            page3.setConvertorMethod(row, "priceToInt");
            page3.selectReturnType(row, "int");
            
            row = table.findCellRow("Image");
            page3.setConvertorMethod(row, "getImage");
            page3.setReturnType(row, "java.awt.Image");
            
            row = table.findCellRow("Product");
            page3.selectConvertorMethod(row, "priceToInt");
            assertEquals("Invalid return type.", "int", page3.getReturnType(row));
            page3.selectConvertorMethod(row, "getImage");
            assertEquals("Invalid return type.", "java.awt.Image", page3.getReturnType(row));
            page3.selectConvertorMethod(row, "getText");
            assertEquals("Invalid return type.", "java.lang.String", page3.getReturnType(row));
            page3.setReturnType(row, "java*lang*String");
            assertEquals("Invalid return type.", "java.lang.String", page3.getReturnType(row));
            page3.setReturnType(row, "java.lang.StringBuffer");
            
            // getText() should have consistent return type for "Product" and "inc-level-1" elements
            int row1 = table.findCellRow("inc-level-1");
            assertEquals("Invalid return type.", page3.getReturnType(row1), page3.getReturnType(row));
            if (DEBUG) sleepTest(2000);
            page3.next();
        } catch (Exception ex) {
            fail("Test fails on Page-3.", ex);
        }
        
        // Page-4
        try {
            SAXDocumentHandlerWizardPage4 page4 = new SAXDocumentHandlerWizardPage4();
            page4.verify();
            page4.setHandlerInterface("BHandler");
            page4.setHandlerImplementation("BHImpl");
            page4.setGeneratedParser("BParser");
            page4.setDataConvertorInterface("BParslet");
            page4.setDataConvertorImplementation("BPImpl");
            page4.checkSaveCustomizedBindings(false);
            page4.checkSaveCustomizedBindings(true);
            assertEquals("Invalid bindings location.", "BooksSAXBindings", page4.getBindingsLocation());
            if (DEBUG) sleepTest(2000);
            page4.finish();
        } catch (Exception ex) {
            fail("Test fails on Page-4.", ex);
        }
        
        // confirm changes - Propagate SAXE vents was checked
        try {
            new ConfirmChangesDialog().processAll();
        } catch (Exception ex) {
            fail("Test fails on Confirm Changes dialog.", ex);
        }
        
        // save handler implementation dump generated classes
        try {
            Node implNode = findDataNode("sax" + DELIM + "BHImpl");
            new OpenAction().perform(implNode);
            // force editor to reload document
            EditorWindowOperator ewo = new EditorWindowOperator();
            EditorOperator eo = ewo.getEditor("BHImpl");
            eo.setCaretPositionToLine(1);
            eo.insert("\n");
            eo.waitModified(true);
            eo.deleteLine(1);
            eo.save();
            
            ref("\n=== Handler Interface:\n");
            ref(dumpFile(TestUtil.THIS.findData("sax/BHandler.java")));
            ref("=== Handler Implementation:\n");
            ref(dumpFile(TestUtil.THIS.findData("sax/BHImpl.java")));
            ref("\n=== Generated Parser:\n");
            ref(dumpFile(TestUtil.THIS.findData("sax/BParser.java")));
            ref("\n=== Data Convertor Interfaces:\n");
            ref(dumpFile(TestUtil.THIS.findData("sax/BParslet.java")));
            ref("\n=== Data Convertor Implementation:\n");
            ref(dumpFile(TestUtil.THIS.findData("sax/BPImpl.java")));
        } catch (Exception ex) {
            fail("Test fails during dumping generated classes.", ex);
        }
        compareReferenceFiles();
    }
    
    // LIB

    /**  @author David Kaspar */
    public String dumpFile(DataObject dao) {
        SourceCookie sc = (SourceCookie) dao.getCookie(SourceCookie.class);
        SourceElement se = sc.getSource();
        ClassElement[] cea = se.getClasses();
        if (cea == null)
            return "";
        ClassElement[] newcea = new ClassElement[cea.length];
        for (int a = 0; a < cea.length; a ++)
            newcea[a] = (ClassElement) cea[a].clone();
        newcea = sortClasses(newcea);
        String str = "";
        for (int a = 0; a < newcea.length; a ++)
            str += newcea[a].toString();
        return str;
    }
    
    /**  @author David Kaspar */
    public ClassElement[] sortClasses(ClassElement[] cea) {
        Arrays.sort(cea, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((ClassElement) o1).getName().getName().compareTo(((ClassElement) o2).getName().getName());
            }
        });
        for (int a = 0; a < cea.length; a ++) {
            ClassElement ce = cea[a];
            try {
                ce.setInterfaces(sortInterfaces(ce.getInterfaces()));
                ce.setFields(sortFields(ce.getFields()));
                ce.setInitializers(sortInitializers(ce.getInitializers()));
                ce.setConstructors(sortConstructors(ce.getConstructors()));
                ce.setMethods(sortMethods(ce.getMethods()));
                ce.setClasses(sortClasses(ce.getClasses()));
            } catch (Exception e) {
                log("ERROR: Exception while normalizing class: ClassElement: " + ce.getName() + " | " + ce.getSignature(), e);
                throw new AssertionFailedErrorException(e);
            }
        }
        return cea;
    }
    
    /**  @author David Kaspar */
    public static Identifier[] sortInterfaces(Identifier[] ar) {
        Arrays.sort(ar, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Identifier) o1).getName().compareTo(((Identifier) o2).getName());
            }
        });
        return ar;
    }
    
    /**  @author David Kaspar */
    public static FieldElement[] sortFields(FieldElement[] ar) {
        Arrays.sort(ar, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((FieldElement) o1).getName().getName().compareTo(((FieldElement) o2).getName().getName());
            }
        });
        return ar;
    }
    
    /**  @author David Kaspar */
    public static InitializerElement[] sortInitializers(InitializerElement[] ar) {
        Arrays.sort(ar, new Comparator() {
            public int compare(Object o1, Object o2) {
                InitializerElement s1 = (InitializerElement) o1;
                InitializerElement s2 = (InitializerElement) o2;
                if (s1.isStatic() == s2.isStatic())
                    return 0;
                return (s1.isStatic()) ? -1 : 1;
            }
        });
        return ar;
    }
    
    /**  @author David Kaspar */
    public static ConstructorElement[] sortConstructors(ConstructorElement[] ar) {
        Arrays.sort(ar, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((ConstructorElement) o1).getName().getName().compareTo(((ConstructorElement) o2).getName().getName());
            }
        });
        return ar;
    }
    
    /**  @author David Kaspar */
    public static MethodElement[] sortMethods(MethodElement[] ar) {
        Arrays.sort(ar, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((MethodElement) o1).getName().getName().compareTo(((MethodElement) o2).getName().getName());
            }
        });
        return ar;
    }
    
    // MAIN
    
    public static void main(String[] args) throws Exception {
        DEBUG = false;
        System.setProperty("xmltest.dbgTimeouts", "true");
        TestRunner.run(SAXGeneratorJTest.class);
        //TestRunner.run(suite());
    }
}
