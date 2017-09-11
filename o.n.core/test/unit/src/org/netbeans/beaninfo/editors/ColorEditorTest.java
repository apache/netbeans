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

package org.netbeans.beaninfo.editors;

import java.awt.Color;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import junit.framework.TestCase;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Radim
 */
public class ColorEditorTest extends TestCase {

    public ColorEditorTest (String testName) {
        super (testName);
    }

    public void testStoreToXml() throws Exception {
        ColorEditor.SuperColor sc = new ColorEditor.SuperColor(
                "TextField.inactiveBackground", 
                ColorEditor.SWING_PALETTE, 
                Color.BLUE);
        System.out.println("original "+sc);
        XMLPropertyEditor propEd = new ColorEditor();
        propEd.setValue(sc);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node element = propEd.storeToXML(doc);

//        NamedNodeMap nodeMap = element.getAttributes();
//        for (int i = 0; i < nodeMap.getLength(); i++) {
//            System.out.println("attr "+i+", "+nodeMap.item(i));
//        }
        
        propEd.readFromXML(element);
        Color restoredColor = (Color)propEd.getValue();
        System.out.println("restoredColor "+restoredColor);
        assertEquals("Restored value has to be the same", sc, restoredColor);
        assertTrue("It is SuperColor", restoredColor instanceof ColorEditor.SuperColor);
        assertEquals ("Generate Java source with UI color.", "javax.swing.UIManager.getDefaults().getColor(\"TextField.inactiveBackground\")", propEd.getJavaInitializationString ());
    }
    
    public void testStoreToXmlWithVaryingLocale() throws Exception {
        Locale loc = Locale.getDefault();
        Locale.setDefault(new Locale("cs", "CZ"));
        ColorEditor.SuperColor sc = new ColorEditor.SuperColor(
                NbBundle.getMessage(ColorEditor.class, "LAB_Blue"), 
                ColorEditor.AWT_PALETTE, 
                Color.BLUE);
        XMLPropertyEditor propEd = new ColorEditor();
        propEd.setValue(sc);
        System.out.println("original "+sc + " Java "+ propEd.getJavaInitializationString ());
        String javaCode = propEd.getJavaInitializationString ();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node element = propEd.storeToXML(doc);

        NamedNodeMap nodeMap = element.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            System.out.println("attr "+i+", "+nodeMap.item(i));
        }
        
        Locale.setDefault(new Locale("en"));
        ColorEditor.awtColorNames = null; // clear the cache of localized names
        
        propEd.readFromXML(element);
        Color restoredColor = (Color)propEd.getValue();
        System.out.println("restoredColor "+restoredColor + " Java "+ propEd.getJavaInitializationString ());
        assertEquals(sc.getBlue(), restoredColor.getBlue());
        assertEquals(sc.getGreen(), restoredColor.getGreen());
        assertEquals(sc.getBlue(), restoredColor.getBlue());
        assertTrue("It is SuperColor", restoredColor instanceof ColorEditor.SuperColor);
        assertEquals("Java code works", javaCode, propEd.getJavaInitializationString ());
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        element = propEd.storeToXML(doc);

        Locale.setDefault(new Locale("cs", "CZ"));
        ColorEditor.awtColorNames = null; // clear the cache of localized names
        
        // back to czech is even identical
        propEd.readFromXML(element);
        restoredColor = (Color)propEd.getValue();
        assertTrue("It is SuperColor", restoredColor instanceof ColorEditor.SuperColor);
        assertEquals("Java code works", javaCode, propEd.getJavaInitializationString ());
        assertEquals("Restored value has to be the same", sc, restoredColor);
        
        Locale.setDefault(loc);
    }
    
    public void testStoreToXmlWhenUIResourceIsMissing() throws Exception {
        ColorEditor.SuperColor sc = new ColorEditor.SuperColor(
                "Fake.inactiveBackground", 
                ColorEditor.SWING_PALETTE, 
                Color.BLUE);
        System.out.println("original "+sc);
        XMLPropertyEditor propEd = new ColorEditor();
        propEd.setValue(sc);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node element = propEd.storeToXML(doc);

        NamedNodeMap nodeMap = element.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            System.out.println("attr "+i+", "+nodeMap.item(i));
        }
        
        propEd.readFromXML(element);
        Color restoredColor = (Color)propEd.getValue();
        System.out.println("restoredColor "+restoredColor);
        assertEquals("Restored value has to be the same", sc, restoredColor);
        assertTrue("It is SuperColor", restoredColor instanceof ColorEditor.SuperColor);
        assertEquals ("Generate Java source with UI color.", "javax.swing.UIManager.getDefaults().getColor(\"Fake.inactiveBackground\")", propEd.getJavaInitializationString ());
    }
    
    public void testGetValue () throws Exception {
        Color c = new Color (16, 16, 16);
        System.out.println("original "+c);
        XMLPropertyEditor propEd = new ColorEditor();
        propEd.setValue(c);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Node element = propEd.storeToXML(doc);

        NamedNodeMap nodeMap = element.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            System.out.println("attr "+i+", "+nodeMap.item(i));
        }
        
        propEd.readFromXML(element);
        Color restoredColor = (Color)propEd.getValue();
        System.out.println("restoredColor "+restoredColor);
        assertEquals("Restored value has to be the same", c, restoredColor);
        assertTrue("It is Color", restoredColor instanceof Color);
        assertFalse("It is not SuperColor", restoredColor instanceof ColorEditor.SuperColor);
        System.out.println("GENERATE: " + propEd.getJavaInitializationString ());
        assertEquals ("Generate Java source with UI color.", "new java.awt.Color(16, 16, 16)", propEd.getJavaInitializationString ());
    }
}
