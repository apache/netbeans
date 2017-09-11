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
package org.netbeans.tax;

import org.netbeans.tax.TreeElementDecl.ContentType;
import org.netbeans.tests.xml.XTest;
import org.openide.util.Utilities;

abstract class AbstractFactoryTest extends XTest {
    final private static String NOT_EXCEPTION = "The InvalidArgumetException wasn't throwed ";

    public AbstractFactoryTest(String testName) {
        super(testName);
    }


    //--------------------------------------------------------------------------


    static TreeAttlistDecl createAttlistDecl(java.lang.String string, String view) throws Exception {
        TreeAttlistDecl node = new TreeAttlistDecl(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createAttlistDeclInvalid(java.lang.String string) throws Exception {
        try {
            new TreeAttlistDecl(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeAttlistDecl(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeAttribute createAttribute(java.lang.String string, java.lang.String string1, boolean boolean_val, String view) throws Exception {
        TreeAttribute node = new TreeAttribute(string, string1, boolean_val);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createAttributeInvalid(java.lang.String string, java.lang.String string1, boolean boolean_val) throws Exception {
        try {
            new TreeAttribute(string, string1, boolean_val);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeAttribute(string, string1, boolean_val)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeAttribute createAttribute(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeAttribute node = new TreeAttribute(string, string1);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createAttributeInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeAttribute(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeAttribute(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeCDATASection createCDATASection(java.lang.String string, String view) throws Exception {
        TreeCDATASection node = new TreeCDATASection(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createCDATASectionInvalid(java.lang.String string) throws Exception {
        try {
            new TreeCDATASection(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeCDATASection(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeCharacterReference createCharacterReference(java.lang.String string, String view) throws Exception {
        TreeCharacterReference node = new TreeCharacterReference(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createCharacterReferenceInvalid(java.lang.String string) throws Exception {
        try {
            new TreeCharacterReference(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeCharacterReference(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeComment createComment(java.lang.String string, String view) throws Exception {
        TreeComment node = new TreeComment(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createCommentInvalid(java.lang.String string) throws Exception {
        try {
            new TreeComment(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeComment(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeConditionalSection createConditionalSection(boolean boolean_val, String view) throws Exception {
        TreeConditionalSection node = new TreeConditionalSection(boolean_val);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeDTD createDTD(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeDTD node = new TreeDTD(string, string1);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createDTDInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeDTD(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeDTD(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeDocument createDocument(java.lang.String string, java.lang.String string1, java.lang.String string2, String view) throws Exception {
        TreeDocument node = new TreeDocument(string, string1, string2);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createDocumentInvalid(java.lang.String string, java.lang.String string1, java.lang.String string2) throws Exception {
        try {
            new TreeDocument(string, string1, string2);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeDocument(string, string1, string2)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeDocumentFragment createDocumentFragment(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeDocumentFragment node = new TreeDocumentFragment(string, string1);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createDocumentFragmentInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeDocumentFragment(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeDocumentFragment(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeDocumentType createDocumentType(java.lang.String string, java.lang.String string1, java.lang.String string2, String view) throws Exception {
        TreeDocumentType node = new TreeDocumentType(string, string1, string2);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createDocumentTypeInvalid(java.lang.String string, java.lang.String string1, java.lang.String string2) throws Exception {
        try {
            new TreeDocumentType(string, string1, string2);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeDocumentType(string, string1, string2)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeDocumentType createDocumentType(java.lang.String string, String view) throws Exception {
        TreeDocumentType node = new TreeDocumentType(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createDocumentTypeInvalid(java.lang.String string) throws Exception {
        try {
            new TreeDocumentType(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeDocumentType(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeElement createElement(java.lang.String string, boolean boolean_val, String view) throws Exception {
        TreeElement node = new TreeElement(string, boolean_val);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createElementInvalid(java.lang.String string, boolean boolean_val) throws Exception {
        try {
            new TreeElement(string, boolean_val);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeElement(string, boolean_val)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeElement createElement(java.lang.String string, String view) throws Exception {
        TreeElement node = new TreeElement(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createElementInvalid(java.lang.String string) throws Exception {
        try {
            new TreeElement(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeElement(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeElementDecl createElementDecl(java.lang.String string, ContentType treeelementdecl$contenttype, String view) throws Exception {
        TreeElementDecl node = new TreeElementDecl(string, treeelementdecl$contenttype);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createElementDeclInvalid(java.lang.String string, ContentType treeelementdecl$contenttype) throws Exception {
        try {
            new TreeElementDecl(string, treeelementdecl$contenttype);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeElementDecl(string, treeelementdecl$contenttype)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
/*
    static TreeElementDecl createElementDecl(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeElementDecl node = new TreeElementDecl(string, string1);
 
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
 
    static void createElementDeclInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeElementDecl(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeElementDecl(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
 
 */
    //--------------------------------------------------------------------------
    
    
    static TreeEntityDecl createEntityDecl(boolean boolean_val, java.lang.String string, java.lang.String string2, String view) throws Exception {
        TreeEntityDecl node = new TreeEntityDecl(boolean_val, string, string2);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createEntityDeclInvalid(boolean boolean_val, java.lang.String string, java.lang.String string2) throws Exception {
        try {
            new TreeEntityDecl(boolean_val, string, string2);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeEntityDecl(boolean_val, string, string2)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeEntityDecl createEntityDecl(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeEntityDecl node = new TreeEntityDecl(string, string1);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createEntityDeclInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeEntityDecl(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeEntityDecl(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeEntityDecl createEntityDecl(boolean boolean_val, java.lang.String string, java.lang.String string2, java.lang.String string3, String view) throws Exception {
        TreeEntityDecl node = new TreeEntityDecl(boolean_val, string, string2, string3);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createEntityDeclInvalid(boolean boolean_val, java.lang.String string, java.lang.String string2, java.lang.String string3) throws Exception {
        try {
            new TreeEntityDecl(boolean_val, string, string2, string3);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeEntityDecl(boolean_val, string, string2, string3)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeEntityDecl createEntityDecl(java.lang.String string, java.lang.String string1, java.lang.String string2, String view) throws Exception {
        TreeEntityDecl node = new TreeEntityDecl(string, string1, string2);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createEntityDeclInvalid(java.lang.String string, java.lang.String string1, java.lang.String string2) throws Exception {
        try {
            new TreeEntityDecl(string, string1, string2);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeEntityDecl(string, string1, string2)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeEntityDecl createEntityDecl(java.lang.String string, java.lang.String string1, java.lang.String string2, java.lang.String string3, String view) throws Exception {
        TreeEntityDecl node = new TreeEntityDecl(string, string1, string2, string3);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createEntityDeclInvalid(java.lang.String string, java.lang.String string1, java.lang.String string2, java.lang.String string3) throws Exception {
        try {
            new TreeEntityDecl(string, string1, string2, string3);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeEntityDecl(string, string1, string2, string3)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeGeneralEntityReference createGeneralEntityReference(java.lang.String string, String view) throws Exception {
        TreeGeneralEntityReference node = new TreeGeneralEntityReference(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createGeneralEntityReferenceInvalid(java.lang.String string) throws Exception {
        try {
            new TreeGeneralEntityReference(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeGeneralEntityReference(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeNotationDecl createNotationDecl(java.lang.String string, java.lang.String string1, java.lang.String string2, String view) throws Exception {
        TreeNotationDecl node = new TreeNotationDecl(string, string1, string2);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createNotationDeclInvalid(java.lang.String string, java.lang.String string1, java.lang.String string2) throws Exception {
        try {
            new TreeNotationDecl(string, string1, string2);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeNotationDecl(string, string1, string2)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeParameterEntityReference createParameterEntityReference(java.lang.String string, String view) throws Exception {
        TreeParameterEntityReference node = new TreeParameterEntityReference(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createParameterEntityReferenceInvalid(java.lang.String string) throws Exception {
        try {
            new TreeParameterEntityReference(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeParameterEntityReference(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeProcessingInstruction createProcessingInstruction(java.lang.String string, java.lang.String string1, String view) throws Exception {
        TreeProcessingInstruction node = new TreeProcessingInstruction(string, string1);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createProcessingInstructionInvalid(java.lang.String string, java.lang.String string1) throws Exception {
        try {
            new TreeProcessingInstruction(string, string1);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeProcessingInstruction(string, string1)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    //--------------------------------------------------------------------------
    
    
    static TreeText createText(java.lang.String string, String view) throws Exception {
        TreeText node = new TreeText(string);
        
        assertEquals(node, view);
        cloneNodeTest(node, view);
        return node;
    }
    
    static void createTextInvalid(java.lang.String string) throws Exception {
        try {
            new TreeText(string);
            // Fail if previous line doesn't trhow exception.
            fail(NOT_EXCEPTION + "from: new TreeText(string)");
        } catch (InvalidArgumentException e) {
            // OK
        }
    }
    
    
    private static void cloneNodeTest(TreeParentNode node, String view) throws Exception {
        TreeParentNode clone = (TreeParentNode) node.clone(true);
        assertNotEquals(clone, node);
        assertEquals(clone, view);
        
        clone = (TreeParentNode) node.clone(false);
        assertNotEquals(clone, node);
        assertEquals(clone, view);
    }
    
    private static void cloneNodeTest(TreeNode node, String view) throws Exception {
        TreeNode clone = (TreeNode) node.clone();
        assertNotEquals(clone, node);
        assertEquals(clone, view);
    }
    
    private static void assertNotEquals(Object orig, Object clone) {
        if (orig == clone) {
            fail("Invalid clone.");
        }
    }
    
    private static void assertEquals(TreeNode node, String view) throws TreeException{
        String str = TestUtil.nodeToString(node).replace("\n", "");
        if (!!! str.equals(view)) {
            fail("Invalid node view \n is       : \"" + str + "\"\n should be: \"" + view + "\"");
        }
    }
}
