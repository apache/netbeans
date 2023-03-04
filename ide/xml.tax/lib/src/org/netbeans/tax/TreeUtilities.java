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

import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.netbeans.tax.spec.AttlistDecl;
import org.netbeans.tax.spec.Attribute;
import org.netbeans.tax.spec.CDATASection;
import org.netbeans.tax.spec.CharacterReference;
import org.netbeans.tax.spec.Comment;
import org.netbeans.tax.spec.ConditionalSection;
import org.netbeans.tax.spec.DocumentFragment;
import org.netbeans.tax.spec.Document;
import org.netbeans.tax.spec.DocumentType;
import org.netbeans.tax.spec.DTD;
import org.netbeans.tax.spec.ElementDecl;
import org.netbeans.tax.spec.Element;
import org.netbeans.tax.spec.EntityDecl;
import org.netbeans.tax.spec.GeneralEntityReference;
import org.netbeans.tax.spec.NotationDecl;
import org.netbeans.tax.spec.ParameterEntityReference;
import org.netbeans.tax.spec.ProcessingInstruction;
import org.netbeans.tax.spec.Text;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public final class TreeUtilities {
    
    /** */
    private static Constraints constraints = new Constraints ();
    
    
    //
    // Node.Constraints
    //
    
    /**
     */
    public static final void checkAttributeName (TreeName treeName) throws InvalidArgumentException {
        constraints.checkAttributeName (treeName);
    }
    
    /**
     */
    public static final boolean isValidAttributeName (TreeName treeName) {
        return constraints.isValidAttributeName (treeName);
    }
    
    /**
     */
    public static final void checkElementTagName (TreeName elementTreeName) throws InvalidArgumentException {
        constraints.checkElementTagName (elementTreeName);
    }
    
    /**
     */
    public static final boolean isValidElementTagName (TreeName elementTreeName) {
        return constraints.isValidElementTagName (elementTreeName);
    }
    
    /**
     */
    public static final void checkNotationDeclSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkNotationDeclSystemId (systemId);
    }
    
    /**
     */
    public static final boolean isValidNotationDeclSystemId (String systemId) {
        return constraints.isValidNotationDeclSystemId (systemId);
    }
    
    /**
     */
    public static final void checkDocumentEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDocumentEncoding (encoding);
    }
    
    /**
     */
    public static final boolean isValidDocumentEncoding (String encoding) {
        return constraints.isValidDocumentEncoding (encoding);
    }
    
    /**
     */
    public static final void checkDTDEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDTDEncoding (encoding);
    }
    
    /**
     */
    public static final boolean isValidDTDEncoding (String encoding) {
        return constraints.isValidDTDEncoding (encoding);
    }
    
    /**
     */
    public static final void checkCharacterReferenceName (String name) throws InvalidArgumentException {
        constraints.checkCharacterReferenceName (name);
    }
    
    /**
     */
    public static final boolean isValidCharacterReferenceName (String name) {
        return constraints.isValidCharacterReferenceName (name);
    }
    
    /**
     */
    public static final void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException {
        constraints.checkEntityDeclInternalText (internalText);
    }
    
    /**
     */
    public static final boolean isValidEntityDeclInternalText (String internalText) {
        return constraints.isValidEntityDeclInternalText (internalText);
    }
    
    /**
     */
    public static final void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException {
        constraints.checkAttlistDeclElementName (elementName);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclElementName (String elementName) {
        return constraints.isValidAttlistDeclElementName (elementName);
    }
    
    /**
     */
    public static final void checkDTDVersion (String version) throws InvalidArgumentException {
        constraints.checkDTDVersion (version);
    }
    
    /**
     */
    public static final boolean isValidDTDVersion (String version) {
        return constraints.isValidDTDVersion (version);
    }
    
    /**
     */
    public static final void checkDocumentTypeSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkDocumentTypeSystemId (systemId);
    }
    
    /**
     */
    public static final boolean isValidDocumentTypeSystemId (String systemId) {
        return constraints.isValidDocumentTypeSystemId (systemId);
    }
    
    /**
     */
    public static final void checkDocumentTypeElementName (String elementName) throws InvalidArgumentException {
        constraints.checkDocumentTypeElementName (elementName);
    }
    
    /**
     */
    public static final boolean isValidDocumentTypeElementName (String elementName) {
        return constraints.isValidDocumentTypeElementName (elementName);
    }
    
    /**
     */
    public static final void checkDocumentStandalone (String standalone) throws InvalidArgumentException {
        constraints.checkDocumentStandalone (standalone);
    }
    
    /**
     */
    public static final boolean isValidDocumentStandalone (String standalone) {
        return constraints.isValidDocumentStandalone (standalone);
    }
    
    /**
     */
    public static final void checkEntityDeclName (String name) throws InvalidArgumentException {
        constraints.checkEntityDeclName (name);
    }
    
    /**
     */
    public static final boolean isValidEntityDeclName (String name) {
        return constraints.isValidEntityDeclName (name);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeEnumeratedType (enumeratedType);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType) {
        return constraints.isValidAttlistDeclAttributeEnumeratedType (enumeratedType);
    }
    
    /**
     */
    public static final void checkProcessingInstructionData (String data) throws InvalidArgumentException {
        constraints.checkProcessingInstructionData (data);
    }
    
    /**
     */
    public static final boolean isValidProcessingInstructionData (String data) {
        return constraints.isValidProcessingInstructionData (data);
    }
    
    /**
     */
    public static final void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException {
        constraints.checkEntityDeclNotationName (notationName);
    }
    
    /**
     */
    public static final boolean isValidEntityDeclNotationName (String notationName) {
        return constraints.isValidEntityDeclNotationName (notationName);
    }
    
    /**
     */
    public static final void checkElementDeclName (String name) throws InvalidArgumentException {
        constraints.checkElementDeclName (name);
    }
    
    /**
     */
    public static final boolean isValidElementDeclName (String name) {
        return constraints.isValidElementDeclName (name);
    }
    
    /**
     */
    public static final void checkGeneralEntityReferenceName (String name) throws InvalidArgumentException {
        constraints.checkGeneralEntityReferenceName (name);
    }
    
    /**
     */
    public static final boolean isValidGeneralEntityReferenceName (String name) {
        return constraints.isValidGeneralEntityReferenceName (name);
    }
    
    /**
     */
    public static final void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException {
        constraints.checkEntityDeclSystemId (systemId);
    }
    
    /**
     */
    public static final boolean isValidEntityDeclSystemId (String systemId) {
        return constraints.isValidEntityDeclSystemId (systemId);
    }
    
    /**
     */
    public static final void checkProcessingInstructionTarget (String target) throws InvalidArgumentException {
        constraints.checkProcessingInstructionTarget (target);
    }
    
    /**
     */
    public static final boolean isValidProcessingInstructionTarget (String target) {
        return constraints.isValidProcessingInstructionTarget (target);
    }
    
    /**
     */
    public static final void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException {
        constraints.checkEntityDeclPublicId (publicId);
    }
    
    /**
     */
    public static final boolean isValidEntityDeclPublicId (String publicId) {
        return constraints.isValidEntityDeclPublicId (publicId);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeDefaultValue (defaultValue);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue) {
        return constraints.isValidAttlistDeclAttributeDefaultValue (defaultValue);
    }
    
    /**
     */
    public static final void checkDocumentFragmentVersion (String version) throws InvalidArgumentException {
        constraints.checkDocumentFragmentVersion (version);
    }
    
    /**
     */
    public static final boolean isValidDocumentFragmentVersion (String version) {
        return constraints.isValidDocumentFragmentVersion (version);
    }
    
    /**
     */
    public static final void checkNotationDeclName (String name) throws InvalidArgumentException {
        constraints.checkNotationDeclName (name);
    }
    
    /**
     */
    public static final boolean isValidNotationDeclName (String name) {
        return constraints.isValidNotationDeclName (name);
    }
    
    /**
     */
    public static final void checkAttributeValue (String value) throws InvalidArgumentException {
        constraints.checkAttributeValue (value);
    }
    
    /**
     */
    public static final boolean isValidAttributeValue (String value) {
        return constraints.isValidAttributeValue (value);
    }
    
    /**
     */
    public static final void checkParameterEntityReferenceName (String name) throws InvalidArgumentException {
        constraints.checkParameterEntityReferenceName (name);
    }
    
    /**
     */
    public static final boolean isValidParameterEntityReferenceName (String name) {
        return constraints.isValidParameterEntityReferenceName (name);
    }
    
    /**
     */
    public static final void checkDocumentFragmentEncoding (String encoding) throws InvalidArgumentException {
        constraints.checkDocumentFragmentEncoding (encoding);
    }
    
    /**
     */
    public static final boolean isValidDocumentFragmentEncoding (String encoding) {
        return constraints.isValidDocumentFragmentEncoding (encoding);
    }
    
    /**
     */
    public static final void checkTextData (String data) throws InvalidArgumentException {
        constraints.checkTextData (data);
    }
    
    /**
     */
    public static final boolean isValidTextData (String data) {
        return constraints.isValidTextData (data);
    }
    
    /**
     */
    public static final void checkDocumentTypePublicId (String publicId) throws InvalidArgumentException {
        constraints.checkDocumentTypePublicId (publicId);
    }
    
    /**
     */
    public static final boolean isValidDocumentTypePublicId (String publicId) {
        return constraints.isValidDocumentTypePublicId (publicId);
    }
    
    /**
     */
    public static final void checkElementDeclContentType (TreeElementDecl.ContentType contentType) throws InvalidArgumentException {
        constraints.checkElementDeclContentType (contentType);
    }
    
    /**
     */
    public static final boolean isValidElementDeclContentType (TreeElementDecl.ContentType contentType) {
        return constraints.isValidElementDeclContentType (contentType);
    }
    
    /**
     */
    public static final void checkDocumentVersion (String version) throws InvalidArgumentException {
        constraints.checkDocumentVersion (version);
    }
    
    /**
     */
    public static final boolean isValidDocumentVersion (String version) {
        return constraints.isValidDocumentVersion (version);
    }
    
    /**
     */
    public static final void checkCDATASectionData (String data) throws InvalidArgumentException {
        constraints.checkCDATASectionData (data);
    }
    
    /**
     */
    public static final boolean isValidCDATASectionData (String data) {
        return constraints.isValidCDATASectionData (data);
    }
    
    /**
     */
    public static final void checkNotationDeclPublicId (String publicId) throws InvalidArgumentException {
        constraints.checkNotationDeclPublicId (publicId);
    }
    
    /**
     */
    public static final boolean isValidNotationDeclPublicId (String publicId) {
        return constraints.isValidNotationDeclPublicId (publicId);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeName (attributeName);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeName (String attributeName) {
        return constraints.isValidAttlistDeclAttributeName (attributeName);
    }
    
    /**
     */
    public static final void checkCommentData (String data) throws InvalidArgumentException {
        constraints.checkCommentData (data);
    }
    
    /**
     */
    public static final boolean isValidCommentData (String data) {
        return constraints.isValidCommentData (data);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeType (type);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeType (short type) {
        return constraints.isValidAttlistDeclAttributeType (type);
    }
    
    /**
     */
    public static final void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException {
        constraints.checkAttlistDeclAttributeDefaultType (defaultType);
    }
    
    /**
     */
    public static final boolean isValidAttlistDeclAttributeDefaultType (short defaultType) {
        return constraints.isValidAttlistDeclAttributeDefaultType (defaultType);
    }
    
    
    
    //
    // Constraints
    //
    
    /**
     *
     */
    private static final class Constraints extends UnicodeClasses
    implements AttlistDecl.Constraints,
    Attribute.Constraints,
    CDATASection.Constraints,
    CharacterReference.Constraints,
    Comment.Constraints,
    ConditionalSection.Constraints,
    DocumentFragment.Constraints,
    Document.Constraints,
    DocumentType.Constraints,
    DTD.Constraints,
    ElementDecl.Constraints,
    Element.Constraints,
    EntityDecl.Constraints,
    GeneralEntityReference.Constraints,
    NotationDecl.Constraints,
    ParameterEntityReference.Constraints,
    ProcessingInstruction.Constraints,
    Text.Constraints {
        
        
        //
        // itself
        //
        
        /**
         */
        private static void checkNullArgument (String argName, Object argValue) throws InvalidArgumentException {
            if ( argValue == null ) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_null_value"));
            }
        }
        
        /**
         */
        private static void checkEmptyString (String argName, String string, boolean trim) throws InvalidArgumentException {
            if ( (string.length () == 0) || (trim && (string.trim ().equals (""))) ) { // NOI18N
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_empty_value"));
            }
        }
        
        
        
        //
        // global constraints
        //
        
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Name
         */
        private static void checkXMLName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);
            
            char first = name.charAt (0);
            if (!!! isXMLNameStartChar (first)) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }
            
            for (int i = 0, len = name.length (); i < len; i++) {
                char c = name.charAt (i);
                if (!!! isXMLNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
        }
        
        /**
         * @see http://www.w3.org/TR/REC-xml#NT-Nmtoken
         */
        private static void checkNmToken (String argName, String token) throws InvalidArgumentException {
            
            checkNullArgument (argName, token);
            checkEmptyString (argName, token, true);
            
            for (int i = 0, len = token.length (); i < len; i++) {
                char c = token.charAt (i);
                if (!!! isXMLNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
            
        }
        
        /**
         * @see http://www.w3.org/TR/REC-xml-names/#NT-NCName
         */
        private static void checkXMLNCName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);
            
            char first = name.charAt (0);
            if (!!! isXMLNCNameStartChar (first)) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }
            
            for (int i = 0, len = name.length (); i < len; i++) {
                char c = name.charAt (i);
                if (!!! isXMLNCNameChar (c)) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }
        }
        
        /**
         */
        private static void checkNamespacePrefix (String prefix) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_NamespacePrefix");
            checkXMLNCName (argName, prefix);
        }
        
        /**
         */
        private static void checkNamespaceURI (String uri) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_NamespaceURI");
            checkAttributeValue (argName, uri);
        }
        
        
        
        /**
         */
        private static void checkElementName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }
        
        /**
         */
        private static void checkAttributeName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }
        
        /**
         */
        private static void checkAttributeValue (String argName, String value) throws InvalidArgumentException {
            checkNullArgument (argName, value);
            checkCharacterData (argName, value);
            
            int index = value.indexOf ('<');
            if ( index != -1 ) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_invalid_attribute_value", value));
            }
            index = value.indexOf ('&');
            if ( index != -1 ) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_invalid_attribute_value", value));
            }
            
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = value.length (); i < len; i++) {
                char c = value.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_invalid_attribute_value", value));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_invalid_attribute_value", value));
                    else
                        quoteFound = true;
            }
        }
        
        /**
         */
        private static void checkCharacterData (String argName, String text) throws InvalidArgumentException {
            checkNullArgument (argName, text);
            
            // do check
            for (int i = 0, len = text.length (); i < len; i++) {
                char c = text.charAt (i);
                if (!!! isXMLChar (c)) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", "0x" + Integer.toHexString (c))); // NOI18N
                }
            }
        }
        
        /**
         */
        private static void checkSystemId (String argName, String systemId) throws InvalidArgumentException {
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = systemId.length (); i < len; i++) {
                char c = systemId.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_system_id", systemId));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_system_id", systemId));
                    else
                        quoteFound = true;
            }
        }
        
        /**
         */
        private static void checkPublicId (String argName, String publicId) throws InvalidArgumentException {
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = publicId.length (); i < len; i++) {
                char c = publicId.charAt (i);
                if (c == '\'') {
                    if (quoteFound) {
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_public_id",  publicId));
                    } else {
                        apostrofFound = true;
                    }
                } else if (c == '"') {
                    if (apostrofFound) {
                        throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_public_id",  publicId));
                    } else {
                        quoteFound = true;
                    }
                } else if ( isXMLPubidLiteral (c) == false ) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_public_id",  publicId));
                }
            }
        }
        
        /**
         */
        private static void checkNotationName (String argName, String name) throws InvalidArgumentException {
            checkNullArgument (argName, name);
            checkXMLName (argName, name);
        }
        
        
        /**
         */
        private static void checkEncoding (String argName, String encoding) throws InvalidArgumentException {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeUtilities::checkEncoding: encoding = " + encoding); // NOI18N
            
            ByteArrayInputStream stream = new ByteArrayInputStream (new byte[0]);
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("      ::checkEncoding: stream = " + stream); // NOI18N
            
            try {
                InputStreamReader reader = new InputStreamReader (stream, iana2java (encoding));
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("      ::checkEncoding: reader = " + reader); // NOI18N
            } catch (IOException exc) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("      ::checkEncoding: IOException !!!", exc); // NOI18N

                throw new InvalidArgumentException (argName, Util.THIS.getString ("EXC_Invalid_encoding", encoding));
            }
        }
        
        
        /**
         */
        public void checkAttributeName (TreeName treeName) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_AttributeName");
            checkAttributeName (argName, treeName.getQualifiedName ());
        }
        
        /**
         */
        public boolean isValidAttributeName (TreeName treeName) {
            try {
                checkAttributeName (treeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkElementTagName (TreeName elementTreeName) throws InvalidArgumentException {
            checkElementName (Util.THIS.getString ("PROP_ElementTagName"), elementTreeName.getQualifiedName ());
        }
        
        /**
         */
        public boolean isValidElementTagName (TreeName elementTreeName) {
            try {
                checkElementTagName (elementTreeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkNotationDeclSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.THIS.getString ("PROP_NotationDeclSystemId"), systemId);
        }
        
        /**
         */
        public boolean isValidNotationDeclSystemId (String systemId) {
            try {
                checkNotationDeclSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.THIS.getString ("PROP_DocumentEncoding"), encoding);
        }
        
        /**
         */
        public boolean isValidDocumentEncoding (String encoding) {
            try {
                checkDocumentEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDTDEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.THIS.getString ("PROP_DTDEncoding"), encoding);
        }
        
        /**
         */
        public boolean isValidDTDEncoding (String encoding) {
            try {
                checkDTDEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkCharacterReferenceName (String name) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_CharacterReferenceName");
            
            checkNullArgument (argName, name);
            checkEmptyString (argName, name, true);
            
            int i = 0;
            char first = name.charAt (i);
            if ( first != '#' ) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_first_char", String.valueOf (first)));
            }
            
            i++;
            if ( name.length () <= i ) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_empty_value"));
            }
            
            char second = name.charAt (i);
            int radix = 10;
            if ( second == 'x' ) {
                radix = 16;
                
                i++;
                if ( name.length () <= i ) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_empty_value"));
                }
            }
            String number = name.substring (i);
            try {
                Short.parseShort (number, radix);
            } catch (NumberFormatException exc) {
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", number));
            }
            
/*            for (int len = name.length(); i < len; i++) {
                char c = name.charAt (i);
                if ( Character.digit (c, radix) == -1 ) {
                    throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", String.valueOf (c)));
                }
            }*/
        }
        
        /**
         */
        public boolean isValidCharacterReferenceName (String name) {
            try {
                checkCharacterReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkEntityDeclInternalText (String internalText) throws InvalidArgumentException {
            checkNullArgument (Util.THIS.getString ("PROP_EntityDeclInternalText"), internalText);
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = internalText.length (); i < len; i++) {
                char c = internalText.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (Util.THIS.getString ("PROP_EntityDeclInternalText"), Util.THIS.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (Util.THIS.getString ("PROP_EntityDeclInternalText"), Util.THIS.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
                    else
                        quoteFound = true;
                // todo
                //    if (c == '%' || c == '&')
                //    throw new InvalidArgumentException ("EntityDeclInternalText", Util.THIS.getString ("EXC_Invalid_Entity_Decl_Internal_text", internalText));
            }
        }
        
        
        /**
         */
        public boolean isValidEntityDeclInternalText (String internalText) {
            try {
                checkEntityDeclInternalText (internalText);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkAttlistDeclElementName (String elementName) throws InvalidArgumentException {
            checkElementName (Util.THIS.getString ("PROP_AttlistDeclElementName"), elementName);
        }
        
        /**
         */
        public boolean isValidAttlistDeclElementName (String elementName) {
            try {
                checkAttlistDeclElementName (elementName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDTDVersion (String version) throws InvalidArgumentException {
            if (version == null)
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.THIS.getString ("PROP_DTDVersion");
                String msg = Util.THIS.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
        
        /**
         */
        public boolean isValidDTDVersion (String version) {
            try {
                checkDTDVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentTypeSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.THIS.getString ("PROP_DocumentTypeSystemId"), systemId);
        }
        
        /**
         */
        public boolean isValidDocumentTypeSystemId (String systemId) {
            try {
                checkDocumentTypeSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentTypeElementName (String elementName) throws InvalidArgumentException {
            checkElementName (Util.THIS.getString ("PROP_DocumentTypeElementName"), elementName);
        }
        
        /**
         */
        public boolean isValidDocumentTypeElementName (String elementName) {
            try {
                checkDocumentTypeElementName (elementName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentStandalone (String standalone) throws InvalidArgumentException {
            if (standalone == null)
                return;
            if (standalone.equals ("yes")) // NOI18N
                return;
            if (standalone.equals ("no")) // NOI18N
                return;
            throw new InvalidArgumentException (standalone, standalone + Util.THIS.getString ("PROP_is_not_valid_standalone_value"));
        }
        
        /**
         */
        public boolean isValidDocumentStandalone (String standalone) {
            try {
                checkDocumentStandalone (standalone);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkEntityDeclName (String name) throws InvalidArgumentException {
            checkXMLName (Util.THIS.getString ("PROP_EntityDeclName"), name);
        }
        
        /**
         */
        public boolean isValidEntityDeclName (String name) {
            try {
                checkEntityDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkAttlistDeclAttributeEnumeratedType (String[] enumeratedType) throws InvalidArgumentException {
            if ( enumeratedType == null ) {
                return;
            }
            for (int i = 0, len = enumeratedType.length; i < len; i++)
                checkNmToken (Util.THIS.getString ("PROP_AttlistDeclAttributeEnumeratedType"), enumeratedType[i]);
        }
        
        /**
         */
        public boolean isValidAttlistDeclAttributeEnumeratedType (String[] enumeratedType) {
            try {
                checkAttlistDeclAttributeEnumeratedType (enumeratedType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkProcessingInstructionData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.THIS.getString ("PROP_ProcessingInstructionData"), data);
            
            int index = data.indexOf ("?>"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_processing_instruction_data"));
            }
        }
        
        /**
         */
        public boolean isValidProcessingInstructionData (String data) {
            try {
                checkProcessingInstructionData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        /**
         */
        public void checkEntityDeclNotationName (String notationName) throws InvalidArgumentException {
            if ( notationName == null ) {
                return;
            }
            checkNotationName (Util.THIS.getString ("PROP_EntityDeclNotationName"), notationName);
        }
        
        /**
         */
        public boolean isValidEntityDeclNotationName (String notationName) {
            try {
                checkEntityDeclNotationName (notationName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkElementDeclName (String name) throws InvalidArgumentException {
            checkElementName (Util.THIS.getString ("PROP_ElementDeclName"), name);
        }
        
        /**
         */
        public boolean isValidElementDeclName (String name) {
            try {
                checkElementDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkGeneralEntityReferenceName (String name) throws InvalidArgumentException {
            checkXMLName (Util.THIS.getString ("PROP_GeneralEntityReferenceName"), name);
        }
        
        /**
         */
        public boolean isValidGeneralEntityReferenceName (String name) {
            try {
                checkGeneralEntityReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkEntityDeclSystemId (String systemId) throws InvalidArgumentException {
            if ( systemId == null ) {
                return;
            }
            checkSystemId (Util.THIS.getString ("PROP_EntityDeclSystemId"), systemId);
        }
        
        /**
         */
        public boolean isValidEntityDeclSystemId (String systemId) {
            try {
                checkEntityDeclSystemId (systemId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkProcessingInstructionTarget (String target) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_ProcessingInstructionTarget");
            checkXMLName (argName, target);
            
            if (target.equalsIgnoreCase ("xml")) { // NOI18N
                throw new InvalidArgumentException (argName, Util.THIS.getString ("PROP_invalid_content_char", target));
            }
        }
        
        /**
         */
        public boolean isValidProcessingInstructionTarget (String target) {
            try {
                checkProcessingInstructionTarget (target);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkEntityDeclPublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.THIS.getString ("PROP_EntityDeclPublicId"), publicId);
        }
        
        
        /**
         */
        public boolean isValidEntityDeclPublicId (String publicId) {
            try {
                checkEntityDeclPublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkAttlistDeclAttributeDefaultValue (String defaultValue) throws InvalidArgumentException {
            if ( defaultValue == null ) {
                return;
            }
            boolean apostrofFound = false;
            boolean quoteFound = false;
            for (int i = 0, len = defaultValue.length (); i < len; i++) {
                char c = defaultValue.charAt (i);
                if (c == '\'')
                    if (quoteFound)
                        throw new InvalidArgumentException (Util.THIS.getString ("PROP_AttlistDeclAttributeDefaultValue"), Util.THIS.getString ("EXC_invalid_attribute_default_value", defaultValue));
                    else
                        apostrofFound = true;
                if (c == '"')
                    if (apostrofFound)
                        throw new InvalidArgumentException (Util.THIS.getString ("PROP_AttlistDeclAttributeDefaultValue"), Util.THIS.getString ("EXC_invalid_attribute_default_value", defaultValue));
                    else
                        quoteFound = true;
                // todo
                //    if (c == '%' || c == '&')
                //    throw new InvalidArgumentException ("AttlistDeclAttributeDefaultValue", Util.THIS.getString ("EXC_invalid_attribute_default_value", defaultValue));
            }
        }
        
        /**
         */
        public boolean isValidAttlistDeclAttributeDefaultValue (String defaultValue) {
            try {
                checkAttlistDeclAttributeDefaultValue (defaultValue);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentFragmentVersion (String version) throws InvalidArgumentException {
            if ( version == null )
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.THIS.getString ("PROP_DocumentFragmentVersion");
                String msg = Util.THIS.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
        
        /**
         */
        public boolean isValidDocumentFragmentVersion (String version) {
            try {
                checkDocumentFragmentVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkNotationDeclName (String name) throws InvalidArgumentException {
            checkXMLName (Util.THIS.getString ("PROP_NotationDeclName"), name);
        }
        
        /**
         */
        public boolean isValidNotationDeclName (String name) {
            try {
                checkNotationDeclName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkAttributeValue (String value) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_AttributeValue");
            checkAttributeValue (argName, value);
        }
        
        /**
         */
        public boolean isValidAttributeValue (String value) {
            try {
                checkAttributeValue (value);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkParameterEntityReferenceName (String name) throws InvalidArgumentException {
            checkXMLName (Util.THIS.getString ("PROP_ParameterEntityReferenceName"), name);
        }
        
        /**
         */
        public boolean isValidParameterEntityReferenceName (String name) {
            try {
                checkParameterEntityReferenceName (name);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentFragmentEncoding (String encoding) throws InvalidArgumentException {
            if ( encoding == null )
                return;
            checkEncoding (Util.THIS.getString ("PROP_DocumentFragmentEncoding"), encoding);
        }
        
        /**
         */
        public boolean isValidDocumentFragmentEncoding (String encoding) {
            try {
                checkDocumentFragmentEncoding (encoding);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkTextData (String data) throws InvalidArgumentException {
            String argName = Util.THIS.getString ("PROP_TextData");
            checkCharacterData (argName, data);
            checkEmptyString (argName, data, false);
            
            int index = data.indexOf ('<');
            if ( index != -1 ) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_text_data"));
            }
            index = data.indexOf ('&');
            if ( index != -1 ) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_text_data"));
            }
            index = data.indexOf ("]]>");
            if ( index != -1 ) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_text_data"));
            }
        }
        
        /**
         */
        public boolean isValidTextData (String data) {
            try {
                checkTextData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentTypePublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.THIS.getString ("PROP_DocumentTypePublicId"), publicId);
        }
        
        /**
         */
        public boolean isValidDocumentTypePublicId (String publicId) {
            try {
                checkDocumentTypePublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkElementDeclContentType (TreeElementDecl.ContentType contentType) throws InvalidArgumentException {
            checkNullArgument (Util.THIS.getString ("PROP_ElementDeclContentType"), contentType);
            //       if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeUtilities::TreeConstraints.checkElementDeclContentType"); // NOI18N
        }
        
        /**
         */
        public boolean isValidElementDeclContentType (TreeElementDecl.ContentType contentType) {
            try {
                checkElementDeclContentType (contentType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkDocumentVersion (String version) throws InvalidArgumentException {
            if ( version == null )
                return;
            if (!!! version.equals ("1.0")) { // NOI18N
                String arg = Util.THIS.getString ("PROP_DocumentVersion");
                String msg = Util.THIS.getString ("PROP_invalid_version_number", version);
                throw new InvalidArgumentException (arg, msg);
            }
        }
        
        /**
         */
        public boolean isValidDocumentVersion (String version) {
            try {
                checkDocumentVersion (version);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkCDATASectionData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.THIS.getString ("PROP_CDATASectionData"), data);
            
            int index = data.indexOf ("]]>"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_cdata_section_data"));
            }
        }
        
        /**
         */
        public boolean isValidCDATASectionData (String data) {
            try {
                checkCDATASectionData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkNotationDeclPublicId (String publicId) throws InvalidArgumentException {
            if ( publicId == null ) {
                return;
            }
            checkPublicId (Util.THIS.getString ("PROP_NotationDeclPublicId"), publicId);
        }
        
        /**
         */
        public boolean isValidNotationDeclPublicId (String publicId) {
            try {
                checkNotationDeclPublicId (publicId);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkAttlistDeclAttributeName (String attributeName) throws InvalidArgumentException {
            checkAttributeName (Util.THIS.getString ("PROP_AttlistDeclAttributeName"), attributeName);
        }
        
        /**
         */
        public boolean isValidAttlistDeclAttributeName (String attributeName) {
            try {
                checkAttlistDeclAttributeName (attributeName);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        
        /**
         */
        public void checkCommentData (String data) throws InvalidArgumentException {
            checkCharacterData (Util.THIS.getString ("PROP_CommentData"), data);
            
            int index = data.indexOf ("--"); // NOI18N
            if (index != -1) {
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_comment_data"));
            }
            if (data.endsWith ("-")) { // NOI18N
                throw new InvalidArgumentException (data, Util.THIS.getString ("PROP_invalid_comment_data_end"));
            }
        }
        
        /**
         */
        public boolean isValidCommentData (String data) {
            try {
                checkCommentData (data);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        /**
         */
        public void checkAttlistDeclAttributeType (short type) throws InvalidArgumentException {
            if (( type != TreeAttlistDeclAttributeDef.TYPE_CDATA ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_ID ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_IDREF ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_IDREFS ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_ENTITY ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_ENTITIES ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_NMTOKEN ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_NMTOKENS ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_ENUMERATED ) &&
            ( type != TreeAttlistDeclAttributeDef.TYPE_NOTATION ) ) {
                throw new InvalidArgumentException (type, Util.THIS.getString ("PROP_invalid_attribute_list_declaration_type"));
            }
        }
        
        /**
         */
        public boolean isValidAttlistDeclAttributeType (short type) {
            try {
                checkAttlistDeclAttributeType (type);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
        /**
         */
        public void checkAttlistDeclAttributeDefaultType (short defaultType) throws InvalidArgumentException {
            if (( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL ) &&
            ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_REQUIRED ) &&
            ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_IMPLIED ) &&
            ( defaultType != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED ) ) {
                throw new InvalidArgumentException (defaultType, Util.THIS.getString ("PROP_invalid_attribute_list_declaration_default_type"));
            }
        }
        
        /**
         */
        public boolean isValidAttlistDeclAttributeDefaultType (short defaultType) {
            try {
                checkAttlistDeclAttributeDefaultType (defaultType);
            } catch (InvalidArgumentException exc) {
                return false;
            }
            return true;
        }
        
    } // end: class Constraints
    
    
    //
    // Encoding
    //
    
    /**
     */
    public static final Collection getSupportedEncodings () {
        return EncodingUtil.getIANA2JavaMap ().keySet ();
    }
    
    /**
     */
    public static final String iana2java (String iana) {
        String java = (String) EncodingUtil.getIANA2JavaMap ().get (iana.toUpperCase ());
        return java == null ? iana : java;
    }


    /**
     //!!! this code is copy pasted to xml.core.lib.Convertors!
     */
    static class EncodingUtil {
        
        /** IANA to Java encoding mappings */
        protected static final Map encodingIANA2JavaMap = new TreeMap ();
        
        /** */
        protected static final Map encodingIANADescriptionMap = new TreeMap ();
        
        /** */
        protected static final Map encodingIANAAliasesMap = new TreeMap ();
        
        //
        // Static initialization
        //
        
        static {
            encodingIANA2JavaMap.put       ("BIG5", "Big5"); // NOI18N
            encodingIANADescriptionMap.put ("BIG5", Util.THIS.getString ("NAME_BIG5")); // NOI18N
            encodingIANAAliasesMap.put     ("BIG5", "BIG5"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM037",       "CP037");  // NOI18N
            encodingIANADescriptionMap.put ("IBM037",       Util.THIS.getString ("NAME_IBM037")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM037",       "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-US", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CA", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NL", "IBM037"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-WT", "IBM037"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM277",       "CP277");  // NOI18N
            encodingIANADescriptionMap.put ("IBM277",       Util.THIS.getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM277",       "IBM277"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-DK", "IBM277"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-NO", "IBM277"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM278",       "CP278");  // NOI18N
            encodingIANADescriptionMap.put ("IBM278",       Util.THIS.getString ("NAME_IBM277")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM278",       "IBM278"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FI", "IBM278"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-SE", "IBM278"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM280",       "CP280");  // NOI18N
            encodingIANADescriptionMap.put ("IBM280",       Util.THIS.getString ("NAME_IBM280")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM280",       "IBM280"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IT", "IBM280"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM284",       "CP284");  // NOI18N
            encodingIANADescriptionMap.put ("IBM284",       Util.THIS.getString ("NAME_IBM284")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM284",       "IBM284"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ES", "IBM284"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM285",       "CP285");  // NOI18N
            encodingIANADescriptionMap.put ("IBM285",       Util.THIS.getString ("NAME_IBM285")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM285",       "IBM285"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-GB", "IBM285"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM297",       "CP297");  // NOI18N
            encodingIANADescriptionMap.put ("IBM297",       Util.THIS.getString ("NAME_IBM297")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM297",       "IBM297"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-FR", "IBM297"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM424",       "CP424");  // NOI18N
            encodingIANADescriptionMap.put ("IBM424",       Util.THIS.getString ("NAME_IBM424")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM424",       "IBM424"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-HE", "IBM424"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM500",       "CP500");  // NOI18N
            encodingIANADescriptionMap.put ("IBM500",       Util.THIS.getString ("NAME_IBM500")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM500",       "IBM500"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-CH", "IBM500"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-BE", "IBM500"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM870",   "CP870");  // NOI18N
            encodingIANADescriptionMap.put ("IBM870",   Util.THIS.getString ("NAME_IBM870")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM870",   "IBM870"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-ROECE", "IBM870"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-YU",    "IBM870"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM871",       "CP871");  // NOI18N
            encodingIANADescriptionMap.put ("IBM871",       Util.THIS.getString ("NAME_IBM871")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM871",       "IBM871"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-IS", "IBM871"); // NOI18N
            
            encodingIANA2JavaMap.put       ("IBM918", "CP918");  // NOI18N
            encodingIANADescriptionMap.put ("IBM918", Util.THIS.getString ("NAME_IBM918")); // NOI18N
            encodingIANAAliasesMap.put     ("IBM918", "IBM918"); // NOI18N
            encodingIANAAliasesMap.put     ("EBCDIC-CP-AR2", "IBM918"); // NOI18N
            
            encodingIANA2JavaMap.put       ("EUC-JP", "EUCJIS"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-JP", Util.THIS.getString ("NAME_EUC-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-JP", "EUC-JP"); // NOI18N
            
            encodingIANA2JavaMap.put       ("EUC-KR", "KSC5601"); // NOI18N
            encodingIANADescriptionMap.put ("EUC-KR", Util.THIS.getString ("NAME_EUC-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("EUC-KR", "EUC-KR");  // NOI18N
            
            encodingIANA2JavaMap.put       ("GB2312", "GB2312"); // NOI18N
            encodingIANADescriptionMap.put ("GB2312", Util.THIS.getString ("NAME_GB2312")); // NOI18N
            encodingIANAAliasesMap.put     ("GB2312", "GB2312"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-2022-JP", "JIS");  // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-JP", Util.THIS.getString ("NAME_ISO-2022-JP")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-JP", "ISO-2022-JP"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-2022-KR", "ISO2022KR");   // NOI18N
            encodingIANADescriptionMap.put ("ISO-2022-KR", Util.THIS.getString ("NAME_ISO-2022-KR")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-2022-KR", "ISO-2022-KR"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-1", "8859_1");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-1", Util.THIS.getString ("NAME_ISO-8859-1")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-1", "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN1",     "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("L1",  "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("IBM819",     "ISO-8859-1"); // NOI18N
            encodingIANAAliasesMap.put     ("CP819",      "ISO-8859-1"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-2", "8859_2");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-2", Util.THIS.getString ("NAME_ISO-8859-2")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-2", "ISO-8859-2"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN2",     "ISO-8859-2"); // NOI18N
            encodingIANAAliasesMap.put     ("L2",  "ISO-8859-2"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-3", "8859_3");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-3", Util.THIS.getString ("NAME_ISO-8859-3")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-3", "ISO-8859-3"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN3",     "ISO-8859-3"); // NOI18N
            encodingIANAAliasesMap.put     ("L3",  "ISO-8859-3"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-4", "8859_4");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-4", Util.THIS.getString ("NAME_ISO-8859-4")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-4", "ISO-8859-4"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN4",     "ISO-8859-4"); // NOI18N
            encodingIANAAliasesMap.put     ("L4",  "ISO-8859-4"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-5", "8859_5");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-5", Util.THIS.getString ("NAME_ISO-8859-5")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-5", "ISO-8859-5"); // NOI18N
            encodingIANAAliasesMap.put     ("CYRILLIC",   "ISO-8859-5"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-6", "8859_6");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-6", Util.THIS.getString ("NAME_ISO-8859-6")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-6", "ISO-8859-6"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-7", "8859_7");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-7", Util.THIS.getString ("NAME_ISO-8859-7")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-7", "ISO-8859-7"); // NOI18N
            encodingIANAAliasesMap.put     ("GREEK",      "ISO-8859-7"); // NOI18N
            encodingIANAAliasesMap.put     ("GREEK8",     "ISO-8859-7"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-8", "8859_8");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-8", Util.THIS.getString ("NAME_ISO-8859-8")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-8", "ISO-8859-8"); // NOI18N
            encodingIANAAliasesMap.put     ("HEBREW",     "ISO-8859-8"); // NOI18N
            
            encodingIANA2JavaMap.put       ("ISO-8859-9", "8859_9");     // NOI18N
            encodingIANADescriptionMap.put ("ISO-8859-9", Util.THIS.getString ("NAME_ISO-8859-9")); // NOI18N
            encodingIANAAliasesMap.put     ("ISO-8859-9", "ISO-8859-9"); // NOI18N
            encodingIANAAliasesMap.put     ("LATIN5",     "ISO-8859-9"); // NOI18N
            encodingIANAAliasesMap.put     ("L5",  "ISO-8859-9"); // NOI18N
            
            encodingIANA2JavaMap.put       ("KOI8-R", "KOI8_R"); // NOI18N
            encodingIANADescriptionMap.put ("KOI8-R", Util.THIS.getString ("NAME_KOI8-R")); // NOI18N
            encodingIANAAliasesMap.put     ("KOI8-R", "KOI8-R"); // NOI18N
            
            encodingIANA2JavaMap.put       ("US-ASCII",     "8859_1"); // NOI18N
            encodingIANADescriptionMap.put ("US-ASCII",     Util.THIS.getString ("NAME_ASCII")); // NOI18N
            encodingIANAAliasesMap.put     ("ASCII",     "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("US-ASCII",  "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("ISO646-US", "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("IBM367",    "US-ASCII");  // NOI18N
            encodingIANAAliasesMap.put     ("CP367",     "US-ASCII");  // NOI18N
            
            encodingIANA2JavaMap.put       ("UTF-8", "UTF8");  // NOI18N
            encodingIANADescriptionMap.put ("UTF-8", Util.THIS.getString ("NAME_UTF-8")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-8", "UTF-8"); // NOI18N
            
            encodingIANA2JavaMap.put       ("UTF-16", "Unicode"); // NOI18N
            encodingIANADescriptionMap.put ("UTF-16", Util.THIS.getString ("NAME_UTF-16")); // NOI18N
            encodingIANAAliasesMap.put     ("UTF-16", "UTF-16");  // NOI18N
        }
        
        
        /**
         */
        public static Map getIANA2JavaMap () {
            return encodingIANA2JavaMap;
        }
        
    } // end: class EncodingUtil
    
}
