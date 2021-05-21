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
package org.netbeans.tax.test;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

import org.netbeans.tax.*;
import org.netbeans.tax.io.*;
import org.netbeans.tax.decl.*;

/**
 */
public class Simple {

    /** Creates new Tests. */
    public Simple () {
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws Exception {

        System.out.println ("Runnig_tests...");
        testEncodings();
//          testMerge("tests/merge/merge", "tests/merge/peer", "tests/merge/out"); // NOI18N
//        testDTDBuilder ("XMLsamples/ParserTest.dtd"); // NOI18N
//        testDTDBuilder ("xml/simple.dtd"); // NOI18N
//        testTree();
//  	  testAllTree();

        testUtilities();
    }

    private static boolean testEncodings () {
        System.out.println ("This test check all 'supported' encoding if they are realy supported!"); // NOI18N

        int invalid = 0;
        Iterator it = TreeUtilities.getSupportedEncodings().iterator();
        while (it.hasNext()) {
            String encoding = (String)it.next();
            boolean valid = TreeUtilities.isValidDocumentEncoding (encoding);
            String prefix;
            String suffix;
            if ( valid ) {
                prefix = "    "; // NOI18N
                suffix = "is valid."; // NOI18N
            } else {
                invalid++;
                prefix = "!!! "; // NOI18N
                suffix = "is *INVALID* !!!"; // NOI18N
            }
            StringBuffer sb = new StringBuffer();
            sb.append (prefix).append ("encoding = '").append (encoding).append ("'\t").append (suffix); // NOI18N
            System.out.println (sb.toString());
        }
        
        if ( invalid != 0 ) {
            System.out.println ("I found " + invalid + " invalid encodings!"); // NOI18N
        }

        return ( invalid == 0 );
    }


    private static void testTree () throws Exception {
        Class<?> clazz = Class.forName ("org.netbeans.tax.TreeAttlistDeclAttributeDef"); // NOI18N
        clazz = TreeAttlistDeclAttributeDef.class;
        System.out.println ("AttributeDef.class = " + clazz); // NOI18N

	TreeAttlistDecl attlistDecl = new TreeAttlistDecl ("elemName"); // NOI18N
        TreeAttlistDeclAttributeDef attrDef;
        attlistDecl.setAttributeDef (attrDef = new TreeAttlistDeclAttributeDef ("attr1", TreeAttlistDeclAttributeDef.TYPE_CDATA, null, TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED, "value")); // NOI18N
        println (attlistDecl);
        System.out.println ("AttributeDef.class = " + attrDef.getClass().getName()); // NOI18N


	TreeCharacterReference charRef = new TreeCharacterReference ("#35"); // NOI18N
        println (charRef);
	charRef = new TreeCharacterReference ("#x35"); // NOI18N
        println (charRef);

        TreeDocumentType doctype = createDocumentType();
        println (doctype);
        
        System.out.println("===create=document======="); // NOI18N
        TreeDocument doc = new TreeDocument("1.0", null, "no"); // NOI18N
        doc.appendChild (new TreeComment ("==============")); // NOI18N
        TreeElement elem = new TreeElement ("xxx"); // NOI18N
        doc.setDocumentElement (elem);

        elem.addAttribute (new TreeAttribute("attr", "value")); // NOI18N
        elem.addAttribute (new TreeAttribute("attr", "value2")); // NOI18N
        elem.addAttribute (new TreeAttribute("attr3", "value3")); // NOI18N
        System.out.println ("CYCLE: elem.appendChild (elem);"); // NOI18N
        
        elem.appendChild (new TreeComment ("Chtel bych vyzkouset get[Previous|Next]Sibling")); // NOI18N
        elem.appendChild (new TreeCDATASection ("<!-------------><BLA>")); // NOI18N
        elem.appendChild (new TreeElement ("BLA")); // NOI18N
        elem.appendChild (new TreeComment ("... tak jeste zaverecny komentar!")); // NOI18N
        
        println (doc);

        elem = doc.getDocumentElement();
        println (elem);
        TreeChild child = elem.item (1);
        println (child);
        child = child.getNextSibling();
        println (child);
        
        //
        // TreeDTD
        //
        TreeDTD dtd = new TreeDTD (null, null);
//         dtd.appendChild (new TreeElementDecl ("elem", "(xxx,yyy?)")); // NOI18N
        TreeAttlistDecl attlist = new TreeAttlistDecl ("elem"); // NOI18N
        attlist.setAttributeDef (new TreeAttlistDeclAttributeDef ("attr", TreeAttlistDeclAttributeDef.TYPE_ID, null, TreeAttlistDeclAttributeDef.DEFAULT_TYPE_REQUIRED, "bla")); // NOI18N
        dtd.appendChild (attlist);

//         dtd.appendChild (new TreeElementDecl ("elem2", "ANY")); // NOI18N
        attlist = new TreeAttlistDecl ("elem2"); // NOI18N
        attlist.setAttributeDef (new TreeAttlistDeclAttributeDef ("attr2", TreeAttlistDeclAttributeDef.TYPE_NOTATION, new String[] { "abc", "def" }, TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL, "bla")); // NOI18N
        dtd.appendChild (attlist);

        println (dtd);
        child = dtd.item (3);
        println (child);
        child = child.getPreviousSibling();
        println (child);
        
    }
    
    protected static TreeDocumentType createDocumentType() throws Exception {
        TreeDocumentType docType  = new TreeDocumentType ("element"); // NOI18N
        
        docType.setSystemId ("xml/simple.dtd"); // NOI18N
        
        TreeDTD treeDTD = null;
        System.out.println("TreeDTD: " + XMLStringResult.toString(treeDTD));//d // NOI18N
        System.out.println("LastChild: " + XMLStringResult.toString(treeDTD.getLastChild()));//d // NOI18N

        TreeChild child = treeDTD.getFirstChild();
        while (child != null) {
            //docType.appendChild((TreeChild) child.clone());
            System.out.println("Sibling: " + XMLStringResult.toString(child));//d // NOI18N
            child = child.getNextSibling();
            System.out.println("NextSibling: " + XMLStringResult.toString(child) + " :: " + child);//d // NOI18N
        }
        /*    
        TreeObjectList childList = treeDTD.getChildNodes();
        for (int i = 0; i < childList.size(); i++) {
            TreeChild child = (TreeChild) ((TreeObject)childList.get(i)).clone();
            docType.appendChild(child);
        }
        */
        // How I can created DTD from String??? - pretahat nodes z DTDdataObject
        //docType.setInternalDtdDecl(dtdString);
        return docType;
    }


    /**
     * Test DTD builder.
     * Check missing attributes.
     */
    static void testDTDBuilder(String dtd) throws Exception {
        Iterator it;

        TreeDTD tree = null;

        it = tree.getChildNodes().iterator();
        while (it.hasNext()) {
            System.out.println (">" + it.next());
        }

        it = tree.getElementDeclarations().iterator();
        while (it.hasNext()) {
            System.out.println("#" + ((TreeNode)it.next()).toString()); // NOI18N
        }

        System.out.println("Listing Any attributes"); // NOI18N

//          TreeElementDecl elemDecl = tree.findElementDecl("Any"); // NOI18N
//          it = elemDecl.getDeclAttrs();
//          while (it.hasNext()) {
//              TreeAttributeDecl next = (TreeAttributeDecl) it.next();
//              System.out.println(next.toString());
//          }

        System.out.println("-------------------"); // NOI18N
        println (tree);
        TreeChild child = tree.getFirstChild();
        println (child);
        child = child.getNextSibling();
        println (child);
        System.out.println("==================="); // NOI18N
    }

    /**
     * Try to merge two trees.
     * @param target repository resource prefix
     * @param src reource resource prefix
     * @param output file prefix
     */
    static void testMerge(String target, String src, String logg) throws Exception {
    }


    //
    // API tests
    //

    /**
     */
    public static void println (TreeNode node) throws TreeException {
        System.out.println("###################"); // NOI18N
        System.out.println("node: " + node); // NOI18N
        System.out.println("node.ownerDocument: " + node.getOwnerDocument()); // NOI18N
        System.out.println("-=#BEGIN#=-"); // NOI18N
	String string = XMLStringResult.toString (node);
	System.out.println (string);
        System.out.println("-=# END #=-"); // NOI18N
    }


    /**
     */
    public static void testAttlistDecl () throws TreeException {
	TreeAttlistDecl attlistDecl = new TreeAttlistDecl ("elemName"); // NOI18N
        attlistDecl.setAttributeDef (new TreeAttlistDeclAttributeDef ("attr1", TreeAttlistDeclAttributeDef.TYPE_CDATA, null, TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED, "value")); // NOI18N
        attlistDecl.setAttributeDef (new TreeAttlistDeclAttributeDef ("attr2", TreeAttlistDeclAttributeDef.TYPE_NOTATION, new String[] { "abc", "def", "ghi" }, TreeAttlistDeclAttributeDef.DEFAULT_TYPE_IMPLIED, null)); // NOI18N
	println (attlistDecl);
    }

    /**
     */
    public static void testAttribute () throws TreeException {
	TreeAttribute attribute = new TreeAttribute ("name", "value"); // NOI18N
	println (attribute);
    }

    /**
     */
    public static void testCDATASection () throws TreeException {
	TreeCDATASection cdataSect = new TreeCDATASection ("<cdata data>"); // NOI18N
	println (cdataSect);
    }

    /**
     */
    public static void testCharacterReference () throws TreeException {
	TreeCharacterReference charRef = new TreeCharacterReference ("#35"); // NOI18N
	println (charRef);
    }

    /**
     */
    public static void testComment () throws TreeException {
	TreeComment comment = new TreeComment ("komentar - komentar"); // NOI18N
        println (comment);
    }

    /**
     */
    public static void testConditionalSection () throws TreeException {
	TreeConditionalSection condSect = new TreeConditionalSection (true);
	println (condSect);
    }

    /**
     */
    public static void testDTD () throws TreeException {
	TreeDTD dtd = new TreeDTD (null, null);
	println (dtd);
    }

    /**
     */
    public static void testDocument () throws TreeException {
	TreeDocument document = new TreeDocument (null, null, null);
	println (document);
    }

    /**
     */
    public static void testDocumentFragment () throws TreeException {
	TreeDocumentFragment docFrag = new TreeDocumentFragment (null, null);
	println (docFrag);
    }

    /**
     */
    public static void testDocumentType () throws TreeException {
	TreeDocumentType docType = new TreeDocumentType ("element"); // NOI18N
	println (docType);
    }

    /**
     */
    public static void testElement () throws TreeException {
	TreeElement element = new TreeElement ("ns:element"); // NOI18N
	println (element);
    }

    /**
     */
    public static void testElementDecl () throws TreeException {
	TreeElementDecl elemDecl = new TreeElementDecl ("element", new ANYType()); // NOI18N
	println (elemDecl);
    }

    /**
     */
    public static void testEntityDecl () throws TreeException {
	TreeEntityDecl entDecl = new TreeEntityDecl ("ent1", "text"); // NOI18N
	println (entDecl);
	entDecl = new TreeEntityDecl ("ent2", "pub1", "sys1"); // NOI18N
	println (entDecl);
	entDecl = new TreeEntityDecl (TreeEntityDecl.PARAMETER_DECL, "ent3", null, "sys2"); // NOI18N
	println (entDecl);
	entDecl = new TreeEntityDecl ("ent4", "pub2", "sys3", "not1"); // NOI18N
	println (entDecl);
	entDecl = new TreeEntityDecl ("ent5", "pub3", "sys4", null); // NOI18N
	println (entDecl);
    }

    /**
     */
    public static void testGeneralEntityReference () throws TreeException {
	TreeGeneralEntityReference geRef = new TreeGeneralEntityReference ("ge-ref"); // NOI18N
	println (geRef);
    }

    /**
     */
    public static void testNotationDecl () throws TreeException {
	TreeNotationDecl notDecl = new TreeNotationDecl ("not1", "pub1", "sys1"); // NOI18N
	println (notDecl);
	notDecl = new TreeNotationDecl ("not2", "pub2", null); // NOI18N
	println (notDecl);
	notDecl = new TreeNotationDecl ("not3", null, "sys2"); // NOI18N
	println (notDecl);
    }

    /**
     */
    public static void testParameterEntityReference () throws TreeException {
	TreeParameterEntityReference peRef = new TreeParameterEntityReference ("pe-ref"); // NOI18N
	println (peRef);
    }

    /**
     */
    public static void testProcessingInstruction () throws TreeException {
	TreeProcessingInstruction pi = new TreeProcessingInstruction ("target", "pi-data"); // NOI18N
	println (pi);
    }

    /**
     */
    public static void testText () throws TreeException {
	TreeText text = new TreeText ("text"); // NOI18N
	println (text);
    }


    /**
     */
    public static void testAllTree () throws ClassNotFoundException, NoSuchMethodException {
	String[] methods = new String [] {
	    "testAttlistDecl", // NOI18N
	    "testAttribute", // NOI18N
	    "testCDATASection", // NOI18N
	    "testCharacterReference", // NOI18N
	    "testComment", // NOI18N
	    "testConditionalSection", // NOI18N
	    "testDTD", // NOI18N
	    "testDocument", // NOI18N
	    "testDocumentFragment", // NOI18N
	    "testDocumentType", // NOI18N
	    "testElement", // NOI18N
	    "testElementDecl", // NOI18N
	    "testEntityDecl", // NOI18N
	    "testGeneralEntityReference", // NOI18N
	    "testNotationDecl", // NOI18N
	    "testParameterEntityReference", // NOI18N
	    "testProcessingInstruction", // NOI18N
	    "testText" // NOI18N
	};
	Class testsClass = Class.forName ("org.netbeans.tax.Tests"); // NOI18N
        System.out.println ("clazz = " + testsClass); // NOI18N
        for (int i = 0; i < methods.length; i++) {
            Method testMethod = testsClass.getMethod (methods[i], null);
//              System.out.println ("    method = " + testMethod); // NOI18N
//              System.out.println ("<!-- v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v -->"); // NOI18N
            try {
                testMethod.invoke (null, null);
            } catch (Exception exc) {
                System.out.println ("<!--!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!-->\n"); // NOI18N
                System.out.println ("Exception [ " + exc.getClass().getName() + " ] : " + exc.getMessage()); // NOI18N
                exc.printStackTrace (System.out);
            }
	    System.out.println ("<!--#######################################################################################-->"); // NOI18N
//              System.out.println ("<!-- ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ -->\n"); // NOI18N
        }
    }


    public static void testUtilities () {
        System.out.println ("\nTest TreeUtilities ...");

        char[] PUBLIC_ID = "\n\r -'()+,./:=?;!*#@$_%0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        char ac = (char) 0;
        for (int i = 0; i < PUBLIC_ID.length; i++) {
            char nc = PUBLIC_ID[i];
            if ( ac > nc ) {
                System.out.println ("    Precondiction failed: '" + ac + "' > '" + nc + "' !!!");
            }
            if ( UnicodeClasses.isXMLPubidLiteral (nc) == false ) {
                System.out.println ("    Char '" + nc + "' is not correct Pubid Literal !!!");
            } else {            
                ac = nc;
                continue;
            }
            break;
        }
    }
    
}
