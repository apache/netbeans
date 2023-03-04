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
package org.netbeans.tax.io;

import java.lang.reflect.*;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.PipedWriter;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import java.text.MessageFormat;

import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeNode;
import org.netbeans.tax.TreeChild;
import org.netbeans.tax.TreeObjectList;
import org.netbeans.tax.TreeParentNode;

import org.netbeans.tax.TreeAttlistDecl;
import org.netbeans.tax.TreeAttlistDeclAttributeDef;
import org.netbeans.tax.TreeAttribute;
import org.netbeans.tax.TreeCDATASection;
import org.netbeans.tax.TreeCharacterReference;
import org.netbeans.tax.TreeCharacterData;
import org.netbeans.tax.TreeComment;
import org.netbeans.tax.TreeConditionalSection;
import org.netbeans.tax.TreeDocumentFragment;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeDocumentType;
import org.netbeans.tax.TreeDTD;
import org.netbeans.tax.TreeElementDecl;
import org.netbeans.tax.TreeElement;
import org.netbeans.tax.TreeEntityDecl;
import org.netbeans.tax.TreeGeneralEntityReference;
import org.netbeans.tax.TreeNotationDecl;
import org.netbeans.tax.TreeParameterEntityReference;
import org.netbeans.tax.TreeProcessingInstruction;
import org.netbeans.tax.TreeText;
import org.netbeans.tax.TreeUtilities;

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
 * We should avoid MessageFormat usage, it is probably the slowest method
 * for constructing output.
 * <p>
 * Fast implementation would write directly to steam/StringBuffer without
 * construction so many auxiliary Strings.
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public class TreeStreamResult implements TreeOutputResult {
    
    /** */
    private TreeStreamWriter writer;
    
    
    //
    // init
    //
    
    /** Creates new TreeStreamResult. */
    public TreeStreamResult (OutputStream outputStream) {
        this.writer = new TreeStreamWriter (outputStream);
    }
    
    /** Creates new TreeStreamResult. */
    public TreeStreamResult (StringWriter writer) {
        this.writer = new TreeStreamWriter (writer);
    }
    
    public TreeStreamResult (PipedWriter writer) {
        this.writer = new TreeStreamWriter (writer);
    }
    
    //
    // itself
    //
    
    /**
     */
    public final TreeWriter getWriter (TreeDocumentRoot document) {
        writer.setDocument (document);
        return writer;
    }
    
    
    //
    // Writer
    //
    
    /**
     *
     */
    public static final class TreeStreamWriter
    implements TreeWriter,
    AttlistDecl.Writer,
    Attribute.Writer,
    CDATASection.Writer,
    CharacterReference.Writer,
    Comment.Writer,
    ConditionalSection.Writer,
    DocumentFragment.Writer,
    Document.Writer,
    DocumentType.Writer,
    DTD.Writer,
    ElementDecl.Writer,
    Element.Writer,
    EntityDecl.Writer,
    GeneralEntityReference.Writer,
    NotationDecl.Writer,
    ParameterEntityReference.Writer,
    ProcessingInstruction.Writer,
    Text.Writer {
        
        private static final char LESS_THAN    = '<';
        private static final char GREAT_THAN   = '>';
        private static final char AMPERSAND    = '&';
        private static final char SEMICOLON    = ';';
        private static final char APOSTROPHE   = '\'';
        private static final char QUOTE        = '"';
        private static final char PER_CENT     = '%';
        private static final char ASSIGN       = '=';
        private static final char BRACKET_LEFT = '[';
        private static final char SPACE        = ' ';
        
        private static final String PI_START = "<?"; // NOI18N
        private static final String PI_END   = "?>"; // NOI18N
        private static final String COMMENT_START = "<!--"; // NOI18N
        private static final String COMMENT_END   = "-->"; // NOI18N
        private static final String ELEMENT_EMPTY_END = " />"; // NOI18N
        private static final String ELEMENT_END_START = "</"; // NOI18N
        private static final String CDATA_START = "<![CDATA["; // NOI18N
        private static final String CDATA_END   = "]]>"; // NOI18N
        private static final String DOCTYPE_START = "<!DOCTYPE "; // NOI18N
        private static final String DOCTYPE_INTERN_END = "]]>"; // NOI18N
        private static final String CHAR_REF_START     = "&#"; // NOI18N
        private static final String CHAR_REF_HEX_START = "&#x"; // NOI18N
        
        private static final String ELEMENT_DECL_START  = "<!ELEMENT "; // NOI18N
        private static final String ATTLIST_DECL_START  = "<!ATTLIST "; // NOI18N
        private static final String ENTITY_DECL_START   = "<!ENTITY "; // NOI18N
        private static final String NOTATION_DECL_START = "<!NOTATION "; // NOI18N
        
        private static final String XML_HEADER     = "<?xml "; // NOI18N
        private static final String XML_VERSION    = "version"; // NOI18N
        private static final String XML_ENCODING   = "encoding"; // NOI18N
        private static final String XML_STANDALONE = "standalone"; // NOI18N
        
        private static final String PUBLIC = "PUBLIC "; // NOI18N
        private static final String SYSTEM = "SYSTEM "; // NOI18N
        
        
        /** */
        private OutputStream outputStream;
        
        /** */
        private Writer writer;
        
        /** */
        private TreeDocumentRoot document;
        
        /** */
        private int indent = 0;
        /** */
        private int indent_step = 4;
        
        
        //
        // init
        //
        
        /** Creates new TreeStreamWriter. */
        public TreeStreamWriter (OutputStream outputStream) {
            this.outputStream = outputStream;
        }
        
        /** Creates new TreeStreamWriter. */
        public TreeStreamWriter (StringWriter writer) {
            this.writer = writer;
        }
        
        /**
         * Create it writing result in pipe (convertable to Reader).
         */
        public TreeStreamWriter (PipedWriter writer) {
            this.writer = writer;
        }
        
        //
        // itself
        //
        
        /**
         */
        public OutputStream getOutputStream () {
            return outputStream;
        }
        
        /**
         */
        public Writer getWriter () {
            return writer;
        }
        
        /**
         */
        public void setDocument (TreeDocumentRoot document) {
            this.document = document;
        }
        
        /**
         */
        public void writeDocument () throws TreeException {
            String encoding = document.getEncoding ();
            if ( outputStream != null ) {
                try {
                    
                    if (encoding != null) {
                        encoding = TreeUtilities.iana2java (encoding);
                    }
                    
                    writer = (encoding == null ? new OutputStreamWriter(outputStream): new OutputStreamWriter (outputStream, encoding));
                } catch (UnsupportedEncodingException exc) {
                    throw new TreeException (exc);
                }
            }
            writeNode ((TreeNode)document);
            
            try {
                writer.flush ();
            } catch (IOException ex) {
                throw new TreeException (ex);
            }
        }
        
        /**
         */
        public void writeNode (TreeNode node) throws TreeException {
            //if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug("wriritng " + node); // NOI18N
            if ( node instanceof TreeAttlistDecl ) {
                writeAttlistDecl ((TreeAttlistDecl)node);
            } else if ( node instanceof TreeAttribute ) {
                writeAttribute ((TreeAttribute)node);
            } else if ( node instanceof TreeCDATASection ) {
                writeCDATASection ((TreeCDATASection)node);
            } else if ( node instanceof TreeCharacterReference ) {
                writeCharacterReference ((TreeCharacterReference)node);
            } else if ( node instanceof TreeComment ) {
                writeComment ((TreeComment)node);
            } else if ( node instanceof TreeConditionalSection ) {
                writeConditionalSection ((TreeConditionalSection)node);
            } else if ( node instanceof TreeDocumentFragment ) {
                writeDocumentFragment ((TreeDocumentFragment)node);
            } else if ( node instanceof TreeDocument ) {
                writeDocument ((TreeDocument)node);
            } else if ( node instanceof TreeDocumentType ) {
                writeDocumentType ((TreeDocumentType)node);
            } else if ( node instanceof TreeDTD ) {
                writeDTD ((TreeDTD)node);
            } else if ( node instanceof TreeElementDecl ) {
                writeElementDecl ((TreeElementDecl)node);
            } else if ( node instanceof TreeElement ) {
                writeElement ((TreeElement)node);
            } else if ( node instanceof TreeEntityDecl ) {
                writeEntityDecl ((TreeEntityDecl)node);
            } else if ( node instanceof TreeGeneralEntityReference ) {
                writeGeneralEntityReference ((TreeGeneralEntityReference)node);
            } else if ( node instanceof TreeNotationDecl ) {
                writeNotationDecl ((TreeNotationDecl)node);
            } else if ( node instanceof TreeParameterEntityReference ) {
                writeParameterEntityReference ((TreeParameterEntityReference)node);
            } else if ( node instanceof TreeProcessingInstruction ) {
                writeProcessingInstruction ((TreeProcessingInstruction)node);
            } else if ( node instanceof TreeText ) {
                writeText ((TreeText)node);
            }
        }
        
        
        //
        // from <Node>.Writer
        //
        
        /**
         */
        public void writeAttlistDecl (TreeAttlistDecl attlistDecl) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            sb.append (ATTLIST_DECL_START).append (attlistDecl.getElementName ());
            
            List attrdefs = attlistDecl.getAttributeDefs ();
            Iterator it = attrdefs.iterator ();
            while (it.hasNext ()) {
                TreeAttlistDeclAttributeDef attrDef = (TreeAttlistDeclAttributeDef)it.next ();
                
                sb.append ("\n\t").append (attrDef.getName ()).append (SPACE); // NOI18N
                if (attrDef.getType () != attrDef.TYPE_ENUMERATED) {
                    sb.append (attrDef.getTypeName ()).append (SPACE);
                }
                if ((attrDef.getType () == TreeAttlistDeclAttributeDef.TYPE_ENUMERATED) ||
                (attrDef.getType () == TreeAttlistDeclAttributeDef.TYPE_NOTATION)) {
                    sb.append (attrDef.getEnumeratedTypeString ()).append (SPACE);
                }
                if (attrDef.getDefaultType () != TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL) {
                    sb.append (attrDef.getDefaultTypeName ()).append (SPACE);
                }
                if ((attrDef.getDefaultType () == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED) ||
                (attrDef.getDefaultType () == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL)) {
                    sb.append ("\"").append (attrDef.getDefaultValue ()).append ("\""); // NOI18N
                }
            }
            
            sb.append (GREAT_THAN);
            write (sb.toString ());
        }
        
        /**
         * Write down the attribute if it was specified in document otherwise nothing.
         */
        public void writeAttribute (TreeAttribute attribute) throws TreeException {
            if (attribute.isSpecified () == false)
                return;
            write (createValueString (attribute.getQName (), attribute.getNonNormalizedValue ()));
            
        }
        
        /**
         */
        public void writeCDATASection (TreeCDATASection cdataSection) throws TreeException {
            String cdataData = cdataSection.getData ();
            String cdataString = MessageFormat.format ("<![CDATA[{0}]]>", new Object [] { cdataData }); // NOI18N
            write (cdataString);
        }
        
        /**
         */
        public void writeCharacterReference (TreeCharacterReference characterReference) throws TreeException {
            String refName = characterReference.getName ();
            String refString = MessageFormat.format ("&{0};", new Object [] { refName }); // NOI18N
            write (refString);
        }
        
        /**
         */
        public void writeComment (TreeComment comment) throws TreeException {
            String comName = comment.getData ();
            String comString = MessageFormat.format ("<!--{0}-->", new Object [] { comName }); // NOI18N
            write (comString);
        }
        
        /**
         */
        public void writeConditionalSection (TreeConditionalSection conditionalSection) throws TreeException {
            if ( conditionalSection.isInclude () ) {
                write ("<![ INCLUDE [\n"); // NOI18N
                writeObjectList (conditionalSection);
            } else {
                write ("<![ IGNORE ["); // NOI18N
                write (conditionalSection.getIgnoredContent ());
            }
            write ("]]>"); // NOI18N
        }
        
        /**
         */
        public void writeDocumentFragment (TreeDocumentFragment documentFragment) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            
            StringBuffer header = null;
            if (documentFragment.getVersion () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (createValueString (XML_VERSION, documentFragment.getVersion ())).append (SPACE);
            }
            if (documentFragment.getEncoding () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (createValueString (XML_ENCODING, documentFragment.getEncoding ()));
            }
            if (header != null) {
                sb.append (XML_HEADER).append (header).append (PI_END);
            }
            write (sb.toString () + "\n\n"); // NOI18N
            
            indent -= indent_step;
            writeObjectList (documentFragment);
        }
        
        /**
         */
        public void writeDocument (TreeDocument document) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            
            StringBuffer header = null;
            if (document.getVersion () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (createValueString (XML_VERSION, document.getVersion ()));
            }
            if (document.getEncoding () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (SPACE).append (createValueString (XML_ENCODING, document.getEncoding ()));
            }
            if (document.getStandalone () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (SPACE).append (createValueString (XML_STANDALONE, document.getStandalone ()));
            }
            if (header != null) {
                sb.append (XML_HEADER).append (header).append (PI_END);
            }
            write(sb.toString() + "\n"); // NOI18N
            
            indent -= indent_step;
            writeObjectList (document);
        }
        
        /**
         */
        public void writeDocumentType (TreeDocumentType documentType) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            
            sb.append (DOCTYPE_START).append (documentType.getElementName ());
            if (documentType.getPublicId () != null) {
                sb.append (SPACE).append (PUBLIC);
                sb.append (createQuoteString (documentType.getPublicId ())).append (SPACE);
                String systemId = documentType.getSystemId ();
                sb.append (createQuoteString (systemId == null ? "" : systemId)); // NOI18N
            } else if (documentType.getSystemId () != null) {
                sb.append (SPACE).append (SYSTEM);
                sb.append (createQuoteString (documentType.getSystemId ()));
            }
            write (sb.toString ());
            
            if ( documentType.hasChildNodes () ) {
                write (" ["); // NOI18N
                
                //!!! use introspection to get internal DTD
                
                try {
                    if (documentType == null) return;
                    Class klass = documentType.getClass ();
                    Field field = klass.getDeclaredField ("internalDTDText");  // NOI18N
                    field.setAccessible (true);
                    
                    String internalDTDText = (String)field.get (documentType);
                    
                    if ( internalDTDText != null ) {
                        write (internalDTDText);
                    } else {
                        // use tradition method instead (however it will resolve refs)
                        write ("\n");    // NOI18N
                        writeObjectList (documentType);
                    }
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    // use tradition method instead (however it will resolve refs)
                    write ("\n");    // NOI18N
                    writeObjectList (documentType);
                }
                
                write ("]"); // NOI18N
            }
            
            write (GREAT_THAN); // NOI18N
        }
        
        /**
         */
        public void writeDTD (TreeDTD dtd) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            
            StringBuffer header = null;
            if (dtd.getVersion () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (createValueString (XML_VERSION, dtd.getVersion ())).append (SPACE);
            }
            if (dtd.getEncoding () != null) {
                if (header == null)
                    header = new StringBuffer ();
                header.append (createValueString (XML_ENCODING, dtd.getEncoding ()));
            }
            if (header != null) {
                sb.append (XML_HEADER).append (header).append (PI_END);
            }
            write (sb.toString () + "\n\n"); // NOI18N
            
            indent -= indent_step;
            writeObjectList (dtd);
        }
        
        /**
         */
        public void writeElementDecl (TreeElementDecl elementDecl) throws TreeException {
            StringBuffer sb = new StringBuffer ();
            sb.append (ELEMENT_DECL_START).append (elementDecl.getName ()).append (SPACE);
            sb.append (elementDecl.getContentType ().toString ());
            sb.append (GREAT_THAN);
            write (sb.toString ());
        }
        
        /**
         */
        public void writeElement (TreeElement element) throws TreeException {
            String elemName = element.getQName ();
            write ("<" + elemName); // NOI18N
            
            Iterator it = element.getAttributes ().iterator ();
            while ( it.hasNext () ) {
                TreeAttribute attr = (TreeAttribute)it.next ();
                if (attr.isSpecified ()) {
                    write (SPACE);
                    writeAttribute (attr);
                }
            }
            
            if (element.isEmpty ()) {
                
                write ("/>"); // NOI18N
                
            } else {
                write (">"); // NOI18N
                
                // content
                writeObjectList (element);
                
                //                  startIndent();
                String endElemString = MessageFormat.format ("</{0}>", new Object [] { elemName }); // NOI18N
                write (endElemString);
            }
        }
        
        /**
         */
        public void writeEntityDecl (TreeEntityDecl entityDecl) throws TreeException {
            String entParam = entityDecl.isParameter () ? "% " : ""; // NOI18N
            String entName = entityDecl.getName ();
            String entType = ""; // NOI18N
            switch (entityDecl.getType ()) {
                case TreeEntityDecl.TYPE_INTERNAL:
                    entType = "\"" + entityDecl.getInternalText () + "\""; // NOI18N
                    break;
                case TreeEntityDecl.TYPE_EXTERNAL:
                    entType = createExternalIdString (entityDecl.getPublicId (), entityDecl.getSystemId ());
                    break;
                case TreeEntityDecl.TYPE_UNPARSED:
                    entType = createExternalIdString (entityDecl.getPublicId (), entityDecl.getSystemId ()) +
                    " NDATA " + entityDecl.getNotationName (); // NOI18N
                    break;
            }
            String entString = MessageFormat.format ("<!ENTITY {0}{1} {2}>", new Object [] { entParam, entName, entType }); // NOI18N
            write (entString);
        }
        
        /**
         */
        public void writeGeneralEntityReference (TreeGeneralEntityReference generalEntityReference) throws TreeException {
            String refName = generalEntityReference.getName ();
            String refString = MessageFormat.format
            ("&{0};", new Object [] { refName }); // NOI18N
            write (refString);
        }
        
        /**
         */
        public void writeNotationDecl (TreeNotationDecl notationDecl) throws TreeException {
            String notName = notationDecl.getName ();
            String notSysId = notationDecl.getSystemId ();
            String notPubId = notationDecl.getPublicId ();
            String notExtId = createExternalIdString (notPubId, notSysId);
            String notString = MessageFormat.format
            ("<!NOTATION {0} {1}>", new Object [] { notName, notExtId }); // NOI18N
            write (notString);
        }
        
        /**
         */
        public void writeParameterEntityReference (TreeParameterEntityReference parameterEntityReference) throws TreeException {
            String refName = parameterEntityReference.getName ();
            String refString = MessageFormat.format
            ("%{0};", new Object [] { refName }); // NOI18N
            write (refString);
        }
        
        /**
         */
        public void writeProcessingInstruction (TreeProcessingInstruction processingInstruction) throws TreeException {
            String piTarget = processingInstruction.getTarget ();
            String piData = processingInstruction.getData ();
            String piString = MessageFormat.format
            ("<?{0} {1}?>", new Object [] { piTarget, piData }); // NOI18N
            write (piString);
        }
        
        /**
         */
        public void writeText (TreeText text) throws TreeException {
            String textString = text.getData ();
            write (textString);
        }
        
        
        //
        // itself
        //
        
        private void write (String string) throws TreeException {
            try {
                writer.write (string);
            } catch (IOException exc) {
                throw new TreeException (exc);
            }
        }
        
        private void write (char ch) throws TreeException {
            try {
                writer.write (ch);
            } catch (IOException exc) {
                throw new TreeException (exc);
            }
        }
        
        private void startIndent () throws TreeException {
            StringBuffer sb = new StringBuffer ();
            for (int i = 0; i < indent; i++) {
                sb.append (' ');
            }
            try {
                writer.write (sb.toString ());
            } catch (IOException exc) {
                throw new TreeException (exc);
            }
        }
        
        private void endIndent () throws TreeException {
            write ("\n"); // NOI18N
        }
        
        private void writeObjectList (TreeParentNode parentNode) throws TreeException {
            indent += indent_step;
            
            boolean notElementChild = ( parentNode instanceof TreeElement ) == false;
            boolean documentChild = ( parentNode instanceof TreeDocument ) == true;
            
            Iterator it = parentNode.getChildNodes ().iterator ();
            while ( it.hasNext () ) {
                TreeNode node = (TreeNode)it.next ();
                //                  boolean isNotCharData = ( node instanceof TreeCharacterData ) == false;
                
                if ( notElementChild ) {
                    //  		if ( isNotCharData ) {
                    startIndent ();
                }
                
                writeNode (node);
                
                if ( notElementChild ) {
                    //  		if ( isNotCharData ) {
                    endIndent ();
                }
            }
            indent -= indent_step;
        }
        
        
        /**
         * Writes name value pair (attribute, encoding, standalone,...)
         */
        private String createValueString (String name, String value) {
            String valueString = MessageFormat.format
            ("{0}={1}", new Object [] { name, createQuoteString (value) }); // NOI18N
            return valueString;
        }
        
        /**
         * Autodetect quoting char giving highets priority to '"'.
         */
        private String createQuoteString (String value) {
            Character quote = QUOTE;
            if ( value.indexOf (QUOTE) != -1 ) {
                quote = APOSTROPHE;
            }
            return createQuoteString (value, quote);
        }
        
        /**
         */
        private String createQuoteString (String value, Character quote) {
            String valueString = MessageFormat.format
            ("{1}{0}{1}", new Object [] { value, quote }); // NOI18N
            return valueString;
        }
        
        /**
         */
        private String createExternalIdString (String publicId, String systemId) {
            String externId;
            if (publicId == null) {
                externId = MessageFormat.format
                ("SYSTEM {0}", new Object [] { createQuoteString (systemId) }); // NOI18N
            } else if (systemId == null) {
                externId = MessageFormat.format
                ("PUBLIC {0}", new Object [] { createQuoteString (publicId) }); // NOI18N
            } else {
                externId = MessageFormat.format
                ("PUBLIC {0} {1}", new Object [] { createQuoteString (publicId), createQuoteString (systemId) }); // NOI18N
            }
            return externId;
        }
        
    } // end: class TreeStreamWriter
    
}
