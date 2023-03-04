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
package org.netbeans.tax;

import java.util.Arrays;
import java.util.List;
import junit.textui.TestRunner;
import org.netbeans.tax.TreeElementDecl.ContentType;
import org.netbeans.tax.decl.*;

/**
 *
 * @author  ms113234
 */
public class FactoryTest extends AbstractFactoryTest {

    /** Creates new CoreSettingsTest */
    public FactoryTest(String testName) {
        super(testName);
    }

    //==========================
    // = = =   T E S T S   = = =
    //==========================

    public void testAttlistDecl() throws Exception {
        createAttlistDecl("elementName", "<!ATTLIST elementName>");
        
        createAttlistDeclInvalid(null);
    }
    
    public void testAttribute() throws Exception {
        createAttribute("name", "value", "name=\"value\"");
        //createAttribute("name", "C&lt;K", "name=\"C&lt;K\""); // issue #15785
        
        createAttributeInvalid(null, null);
        createAttributeInvalid("name", null);
        createAttributeInvalid(null, "value");
        createAttributeInvalid("na me", "value");
        createAttributeInvalid("na;me", "value");
        createAttributeInvalid("1name", "value");
    }
    
    public void testCDATASection() throws Exception {
        createCDATASection("<cdata ]] > &lt; &#x0; data>", "<![CDATA[<cdata ]] > &lt; &#x0; data>]]>");
        
        createCDATASectionInvalid(null);
        createCDATASectionInvalid("aa ]]> bbb");
    }
    
    public void testCharacterReference() throws Exception {
        createCharacterReference("#35", "&#35;");
        createCharacterReference("#x35", "&#x35;");
        
        createCharacterReferenceInvalid(null);
        createCharacterReferenceInvalid("");
        createCharacterReferenceInvalid("#");
        createCharacterReferenceInvalid("#x");
        createCharacterReferenceInvalid("#xg");
        createCharacterReferenceInvalid("#;");
        createCharacterReferenceInvalid("# ");
    }
    
    public void testComment() throws Exception {
        createComment("comment - comment", "<!--comment - comment-->");
        createComment("", "<!---->");
        
        createCommentInvalid(null);
        createCommentInvalid("bad--bad");
    }
    
    public void testConditionalSection() throws Exception {
        createConditionalSection(true, "<![ INCLUDE []]>");
        createConditionalSection(false, "<![ IGNORE []]>");
    }
    
    public void testDTD() throws Exception {
        createDTD("1.0", "UTF-16", "<?xml version=\"1.0\" encoding=\"UTF-16\"?>");
        createDTD(null, "UTF-8", "<?xml encoding=\"UTF-8\"?>");
        createDTD(null, null, "");
        
        createDTDInvalid(null, "encodig");
        createDTDInvalid("version", null);
        createDTDInvalid("1.0", "encodig");
        createDTDInvalid("version", "UTF-16");
    }
    
    public void testDocument() throws Exception {
        createDocument(null, null, null, "");
        createDocument("1.0", "ISO-8859-2", "no", "<?xml version=\"1.0\" encoding=\"ISO-8859-2\" standalone=\"no\"?>");
        createDocument("1.0", null, "yes", "<?xml version=\"1.0\" standalone=\"yes\"?>");
        createDocument("1.0", "ASCII", null, "<?xml version=\"1.0\" encoding=\"ASCII\"?>");
        createDocument("1.0", null, null, "<?xml version=\"1.0\"?>");
        
        createDocumentInvalid("2.0", "ISO-8859-2", "no");
        createDocumentInvalid("10", "ISO-8859-2", "no");
        createDocumentInvalid("", "ISO-8859-2", "no");
        createDocumentInvalid("2.0", null, "no");
        createDocumentInvalid(".0", "ISO-8859-2", null);
        createDocumentInvalid(null, "ISO-8859-2", "no");
        createDocumentInvalid(null, null, "no");
        createDocumentInvalid(null, "ISO-8859-2", null);
    }
    
    public void testDocumentFragment() throws Exception {
        createDocumentFragment(null, null, "");
        createDocumentFragment("1.0", "ISO-8859-2", "<?xml version=\"1.0\" encoding=\"ISO-8859-2\"?>");
        createDocumentFragment(null, "ISO-8859-2", "<?xml encoding=\"ISO-8859-2\"?>");
        
        createDocumentFragmentInvalid("1.0", null);
        createDocumentFragmentInvalid("3.0", "UTF-8");
        createDocumentFragmentInvalid("1.0", "UTF-80");
    }
    
    public void testDocumentType() throws Exception {
        createDocumentType("elementName", "<!DOCTYPE elementName>");
        
        createDocumentType("element", "pub_id", "sys_id", "<!DOCTYPE element PUBLIC \"pub_id\" \"sys_id\">");
        createDocumentType("element", "pub_id", "", "<!DOCTYPE element PUBLIC \"pub_id\" \"\">");
        createDocumentType("element", "pub_id", null, "<!DOCTYPE element PUBLIC \"pub_id\" \"\">");
        createDocumentType("element", "", "sys_id", "<!DOCTYPE element PUBLIC \"\" \"sys_id\">");
        createDocumentType("element", null, "sys_id", "<!DOCTYPE element SYSTEM \"sys_id\">");
        
        createDocumentTypeInvalid(null);
        createDocumentTypeInvalid("");
        createDocumentTypeInvalid("a c");
        createDocumentTypeInvalid("a&gt;c");
        createDocumentTypeInvalid("a&#37;c");
        createDocumentTypeInvalid("a%ref;c");
        
        createDocumentTypeInvalid("", "pub_id", "sys_id");
        createDocumentTypeInvalid(null, "pub_id", "sys_id");
        createDocumentTypeInvalid("1element", "pub_id", "sys_id");
        createDocumentTypeInvalid("ele ment", "pub_id", "sys_id");
        createDocumentTypeInvalid("element", "pub_id", "\'\"");
        //createDocumentTypeInvalid("element", "pub_id&", "sys_id"); // issue #18112
        
        
    }
    
    public void testElement() throws Exception {
        createElement("element", "<element></element>");
        createElement("ns:element", "<ns:element></ns:element>");
        
        createElementInvalid(null);
        createElementInvalid("");
        createElementInvalid("1a");
        createElementInvalid("a b");
        createElementInvalid("a&b");
        createElementInvalid("a%b");
    }
    
    public void testElementDecl() throws Exception {
        List content;
        ContentType type;
        
        createElementDecl("element", new ANYType(), "<!ELEMENT element ANY>");
        createElementDecl("element", new EMPTYType(), "<!ELEMENT element EMPTY>");
        
        content = Arrays.asList(new Object[] {new NameType("ele1"), new NameType("ele2","")});
        type = new SequenceType(content);
        type.setMultiplicity("+");
        createElementDecl("element", type, "<!ELEMENT element ( ele1, ele2 )+>");
        
        type = new ChoiceType(content);
        type.setMultiplicity("*");
        createElementDecl("element", type, "<!ELEMENT element ( ele1 | ele2 )*>");
        
        type = new MixedType(content);
        type.setMultiplicity("+");
        createElementDecl("element", type, "<!ELEMENT element ( #PCDATA | ele1 | ele2 )+>");
        
        //createElementDecl("books", "(product+, price?, image,custom? )+", "<!ELEMENT books ( product+, price?, image, custom? )+>");
        //createElementDecl("dictionary-body", "(%div.mix;, %dict.mix;)*", "<!ELEMENT dictionary-body ( %div.mix;, %dict.mix; )*>");
        //createElementDecl("%name.para;", "%content.para;", "<!ELEMENT %name.para; %content.para; >");
        //createElementDecl("div", "( head, (p | list | note )*, div2* )", "<!ELEMENT div ( head, (p | list | note )*, div2* )>"); // issue #
        //createElementDecl("div", "(p | list | note )*", "<!ELEMENT div ( p | list | note )*>"); // issue #
        //createElementDecl("product", "(#PCDATA |descript)*", "<!ELEMENT product ( #PCDATA | descript )*>"); // issue #
        //createElementDecl("descript", "(#PCDATA)", "<!ELEMENT descript ( #PCDATA )>"); // issue #
        
        createElementDeclInvalid(null, (ContentType) null);
    }
    
    public void testEntityDecl() throws Exception {
        boolean parameter;
        String name, text;
        String publicId, systemId;
        String notationName;
        
        // General Entity Decl
        createEntityDecl("name", "text", "<!ENTITY name \"text\">");
        createEntityDeclInvalid(null, null);
        createEntityDeclInvalid("name", null);
        createEntityDeclInvalid(null, "text");
        
        // Parametr Entity Decl
        createEntityDecl(true, "name", "text", "<!ENTITY % name \"text\">");
        
        for (int i = 0; i < 8; i++) {
            parameter = ((i & 1) == 0) ? false : true;
            name = ((i & 2) == 0) ? null : "name";
            text = ((i & 4) == 0) ? null : "text";
            if (name == null || text == null) {
                createEntityDeclInvalid(parameter, name, text);
            }
        }
        
        // External Entity Decl
        createEntityDecl("name", "publicId", "systemId", "<!ENTITY name PUBLIC \"publicId\" \"systemId\">");
        createEntityDecl("name", null, "systemId", "<!ENTITY name SYSTEM \"systemId\">");
        createEntityDecl(false, "name", "publicId", "systemId", "<!ENTITY name PUBLIC \"publicId\" \"systemId\">");
        createEntityDecl(false, "name", null, "systemId", "<!ENTITY name SYSTEM \"systemId\">");
        
        for (int i = 0; i < 8; i++) {
            name = ((i & 1) == 0) ? null : "name";
            publicId = ((i & 2) == 0) ? null : "publicId";
            systemId = ((i & 4) == 0) ? null : "systemId";
            
            if (name == null || systemId == null) {
                createEntityDeclInvalid(name, publicId, systemId);
            }
        }
        
        // Parametr External Entity Decl
        createEntityDecl(true, "name", "publicId", "systemId",  "<!ENTITY % name PUBLIC \"publicId\" \"systemId\">");
        createEntityDecl(true, "name", null, "systemId",  "<!ENTITY % name SYSTEM \"systemId\">");
        
        for (int i = 0; i < 16; i++) {
            name = ((i & 1) == 0) ? null : "name";
            publicId = ((i & 2) == 0) ? null : "publicId";
            systemId = ((i & 4) == 0) ? null : "systemId";
            parameter = ((i & 8) == 0) ? true : false;
            
            if (name == null || systemId == null) {
                createEntityDeclInvalid(parameter, name, publicId, systemId);
            }
        }
        
        // Unparsed Entity Decl
        createEntityDecl("name", "publicId", "systemId", "notationName", "<!ENTITY name PUBLIC \"publicId\" \"systemId\" NDATA notationName>");
        createEntityDecl("name", null, "systemId", "notationName",  "<!ENTITY name SYSTEM \"systemId\" NDATA notationName>");
        
        for (int i = 0; i < 16; i++) {
            name = ((i & 1) == 0) ? null : "name";
            publicId = ((i & 2) == 0) ? null : "publicId";
            systemId = ((i & 4) == 0) ? null : "systemId";
            notationName = ((i & 8) == 0) ? null : "notationName";
            
            if (name == null || systemId == null || notationName == null) {
                createEntityDeclInvalid(name, publicId, systemId, notationName);
            }
        }
    }
    
    public void testGeneralEntityReference() throws Exception {
        createGeneralEntityReference(":g1e_r-e.f", "&:g1e_r-e.f;");
        
        createGeneralEntityReferenceInvalid(null);
        createGeneralEntityReferenceInvalid("&");
        createGeneralEntityReferenceInvalid(";");
        createGeneralEntityReferenceInvalid("%");
    }
    
    public void testNotationDecl() throws Exception {
        String name;
        String publicId;
        String systemId;
        TreeNotationDecl node;
        
        createNotationDecl("name", "publicId", "systemId", "<!NOTATION name PUBLIC \"publicId\" \"systemId\">");
        createNotationDecl("name", null, "systemId", "<!NOTATION name SYSTEM \"systemId\">");
        createNotationDecl("name", "publicId", null, "<!NOTATION name PUBLIC \"publicId\">");
        
        for (int i = 0; i < 8; i++) {
            name = ((i & 1) == 0) ? null : "name";
            publicId = ((i & 2) == 0) ? null : "publicId";
            systemId = ((i & 4) == 0) ? null : "systemId";
            
            if (name == null || (systemId == null && publicId == null)) {
                createNotationDeclInvalid(name, publicId, systemId);
            }
        }
    }
    
    public void testParameterEntityReference() throws Exception {
        createParameterEntityReference(":p1e_r-e.f", "%:p1e_r-e.f;");
        
        createParameterEntityReferenceInvalid(null);
        createParameterEntityReferenceInvalid("&");
        createParameterEntityReferenceInvalid(";");
        createParameterEntityReferenceInvalid("%");
    }
    
    public void testProcessingInstruction() throws Exception {
        createProcessingInstruction("target", "pi-data", "<?target pi-data?>");
        createProcessingInstruction("target", "&%4;?<<>>]]>--> <!--", "<?target &%4;?<<>>]]>--> <!--?>");
        
        createProcessingInstructionInvalid(null, null);
        createProcessingInstructionInvalid("target", null);
        createProcessingInstructionInvalid("xml", "pi-data");
        createProcessingInstructionInvalid("XmL", "pi-data");
        createProcessingInstructionInvalid("&", "pi-data");
    }
    
    public void testText() throws Exception {
        createText(" text >--> % @ # $ ", " text >--> % @ # $ ");
        createTextInvalid("<");
        createTextInvalid("&");
        //createTextInvalid("]]>"); // ISSUE #18445
    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        TestRunner.run(FactoryTest.class);
    }
}
