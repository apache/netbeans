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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.text.MessageFormat;
import java.lang.reflect.*;

import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

import org.apache.xerces.xni.*;
import org.apache.xerces.xni.parser.XMLDTDSource;
import org.apache.xerces.xni.parser.XMLDTDContentModelSource;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.parsers.*;

import org.netbeans.tax.*;
import org.netbeans.tax.io.*;
import org.netbeans.tax.decl.*;
import java.util.List;

/**
 * Xerces Native Interface ("XNI") based implementation. It sets
 * namespace non-aware and non-validating features.
 * <p>
 * Do instantiate it directly, prefer TreeBuilder interface loaded  by TreeStreamSource
 * (i.e. ParserLoader). It will do necessary implementation isolation.
 * <p>
 * Every well-formed source must be possible to convert to tree structure.
 * //!!! A mechanism of supressing particular implemenation constrains will
 * be needed (JAXP validation on request could be a good approach).
 *
 * @author  Petr Kuzel
 * @version rewritten to XNI 2.4.0
 */
public final class XNIBuilder implements TreeBuilder {
        
    private static final boolean ASSERT = false;
    
    //      private static final PrintStream dbg = System.err;
    
    private static final String DTD_WRAPPER = "<!DOCTYPE DTD PUBLIC \"{0}\" \"{1}\">"; // NOI18N
    
    // TreeStreamSource defines
    private Class buildClass;  //DTD or XML [or Fragment]
    
    private InputSource inputSource;
    
    // interface for reporting errors during the tree construction
    private TreeStreamBuilderErrorHandler errorHandler;
    
    // do not forget to set to the parser
    private EntityResolver entityResolver;
    
    
    /** Creates new TreeStreamBuilderXercesImpl */
    public XNIBuilder (Class buildClass, InputSource inputSource, EntityResolver entityResolver, TreeStreamBuilderErrorHandler errorHandler) {
        init (buildClass, inputSource, entityResolver, errorHandler);
    }
    
    /** Initialize it */
    private void init (Class buildClass, InputSource inputSource, EntityResolver entityResolver, TreeStreamBuilderErrorHandler errorHandler) {
        this.inputSource    = inputSource;
        this.buildClass     = buildClass;
        this.errorHandler   = errorHandler;
        this.entityResolver = entityResolver;
    }
    
    /**
     * Build new TreeDocument by delegating to private class (hiding its
     * public XNI interfaces implementation).
     */
    public TreeDocumentRoot buildDocument () throws TreeException {
        
        boolean buildXML = true;
        InputSource builderSource = inputSource;
        EntityResolver builderResolver = entityResolver;
        
        /*
         * We are building DTD so wrap into auxiliary InputSource that
         * can be passed to XML parser.
         */
        if (buildClass == TreeDTD.class) {
            
            String src = MessageFormat.format (DTD_WRAPPER, new Object[] {
                DTDEntityResolver.DTD_ID,
                inputSource.getSystemId ()
            });
            
            builderSource = new InputSource (inputSource.getSystemId ());
            builderSource.setCharacterStream (new StringReader (src));
            
            builderResolver = new DTDEntityResolver ();
            buildXML = false;
        }
        
        XMLBuilder builder = this.new XMLBuilder (buildXML);
        
        try {
            final String SAX_FEATURE = "http://xml.org/sax/features/"; // NOI18N
            final String XERCES_FEATURE = "http://apache.org/xml/features/"; // NOI18N
            
            builder.setFeature (SAX_FEATURE + "namespaces", false); //!!! // NOI18N
            builder.setFeature (SAX_FEATURE + "validation", false);  //!!! // NOI18N
            builder.setFeature (SAX_FEATURE + "external-general-entities", true); // NOI18N
            builder.setFeature (SAX_FEATURE + "external-parameter-entities", true); // NOI18N
            builder.setFeature (XERCES_FEATURE + "validation/warn-on-duplicate-attdef", true); // NOI18N
            // unrecognized in Xerces 2.4.0
            //builder.setFeature (XERCES_FEATURE + "validation/warn-on-undeclared-elemdef", true); // NOI18N
            builder.setFeature (XERCES_FEATURE + "allow-java-encodings", true); // NOI18N
            builder.setFeature (XERCES_FEATURE + "scanner/notify-char-refs", true); // NOI18N
            builder.setFeature (XERCES_FEATURE + "scanner/notify-builtin-refs", true); // NOI18N
            
            //            final String XERCES_PROPERTY = "http://apache.org/xml/properties/"; // NOI18N
            //            builder.setProperty(XERCES_PROPERTY + "internal/entity-resolver", builderResolver); // NOI18N
            
            builder.setEntityResolver (builderResolver);
            
            // the builder extends XNIDocumentParser that receives
            // error events directly
            
            builder.setErrorHandler (new ErrorHandler () {
                public void error (org.xml.sax.SAXParseException e) {}
                public void warning (org.xml.sax.SAXParseException e) {}
                public void fatalError (org.xml.sax.SAXParseException e) {}
            });
            builder.parse (builderSource);
            
        } catch (DTDStopException stop) {
            
            // we just stopped the parser at the end of standalone DTD
            
        } catch (SAXException sax) {
            
            // test whether wrapped exception is XNI one
            // if so it wrrap actual exception
            
            Exception exception = sax.getException ();
            
            if ((exception instanceof DTDStopException) == false ) {
                
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("sax", sax); // NOI18N
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("exception", exception); // NOI18N
                
                if (exception instanceof XNIException) {
                    exception = ((XNIException)exception).getException ();
                }
                if (exception != null) {
                    if (!!! (exception instanceof TreeException)) {
                        exception = new TreeException (sax);
                    }
                } else {
                    exception = new TreeException (sax);
                }
                throw (TreeException) exception;
            }
            
        } catch (IOException exc) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("exc", exc); // NOI18N

            throw new TreeException (exc);
        }
        
        return builder.getDocumentRoot ();
    }
    
    
    /*
     * Resolve DTD to original InputSource, forward others.
     * DTD builder uses wrapping InputSource so XML parser can be used as DTD one.
     */
    private class DTDEntityResolver implements EntityResolver {
        
        static final String DTD_ID = "PRIVATE//AUXILIARY DTD ID//PRIVATE"; // NOI18N
        
        public InputSource resolveEntity (String publicId, String systemId) throws SAXException, IOException {
            
            if (DTD_ID.equals (publicId)) {
                return inputSource;
            } else {
                return entityResolver.resolveEntity (publicId, systemId);
            }
        }
        
    }
    
    /*
     * It is used to signal that we are parsing a DTD and we reached end of it.
     * So we can stop the parser by throwing it.
     */
    private class DTDStopException extends XNIException {
        
        /** Serial Version UID */
        private static final long serialVersionUID =4994054007367982021L;
        
        public DTDStopException () {
            super ("This exception is used to signal end of DTD."); // NOI18N
        };
        
        //
        // Look like wrapping exception, so it be converted so SAXException
        // that wraps this one.
        //
        public Exception getException () {
            return this;
        }
        
        public Throwable fillInStackTrace () {
            return this;
        }
    }
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /*
     * A pipeline of document components starts with a document source; is
     * followed by zero or more document filters; and ends with a document
     * handler.
     *
     * The document handler follows.
     */
    
    
    /**
     * Listens on XNI creating XML structure. It uses mini XNI pipe
     * featuring just with scanner. A validator is used but validity
     * are discarded since tree must be just well-formed ("WF").
     */
    private class XMLBuilder extends SAXParser implements XMLDTDContentModelHandler, XMLDocumentHandler, XMLDTDHandler {
        
        private TreeDocumentRoot returnDocument;    // initial parent
        private TreeDocumentRoot document;          // tmp variable
        
        private TreeDocumentType doctype;   // it will become parent node of DTD content
        private TreeNode tempNode;          // current working node
        
        private Stack   parentObjectListStack;        // parents' child lists stack
        private TreeObjectList parentObjectList;      // top of the stack
        private Stack parentNodeStack;      // some times we need nodes directly
        
        private Stack   elementStack;           // ??? it could be avoided
        private int entityCounter;              // how deep we entered
        
        private boolean isXMLDocument;          // do we parser XML or standalone DTD
        private boolean inCDATASection;         // we are in the middle of CDATA
        private boolean inDTD;                  // we are in DTD
        private boolean isCorrect;              // builder internal error
        private boolean inCharacterRef;         //
        
        private StringBuffer cdataSectionBuffer;    // working CDATA section buffer
        private QName tmpQName = new QName ();       // working Qname
        private TreeAttlistDecl attlistDecl = null; // latest attlistdecl
        
        private int errors = 0;  // fatal error counter
        
        private final String XML_ENTITY = "[xml]"; // name of entity that precedes startDocument call // NOI18N
        private final String DTD_ENTITY = "[dtd]"; // external DTD entity name // NOI18N
        
        private XMLLocator locator;
        
        private boolean hasExternalDTD = false;
        
        private RememberingReader rememberingReader;

        private XMLDTDSource xmldtdSource;  // XMLDTDHandler 2.4.0

        private XMLDTDContentModelSource xmldtdContentModelSource; // XMLDTDContentModelHandler 2.4.0

        private XMLDocumentSource xmlDocumentSource; // XMLDocumentHanlder 2.4.0

        /**
         * Create a parser with standard configuration.
         * @param xmlDocument false if building standalone DTD
         */
        public XMLBuilder (boolean xmlDocument) {
            isXMLDocument = xmlDocument;
            entityCounter = 0;
            isCorrect = false;
            inCDATASection = false;
            inDTD = false;
            parentObjectListStack = new Stack ();
            parentNodeStack = new Stack ();
            elementStack = new Stack ();  //stacks all non-empty elements
            cdataSectionBuffer = new StringBuffer ();
            inCharacterRef = false;
        }
        
        
        /**
         * Sample user reader replacing it by remebering one suitable for
         * internal DTD remebering.
         */
        public void parse (InputSource in) throws IOException, SAXException {
            Reader reader = in.getCharacterStream ();
            if (reader != null) {
                rememberingReader = new RememberingReader (reader);
                in.setCharacterStream (rememberingReader);
                rememberingReader.startRemembering ();  //remember internal DTD see startElement for end
            }
            
            super.parse (in);
        }
        

        //
        // XMLDocumentHandler methods
        //
        
        public XMLDocumentSource getDocumentSource() {
            return xmlDocumentSource;
        }

        public void setDocumentSource(XMLDocumentSource src) {
            xmlDocumentSource = src;
        }

        // XMLDocumentHandler 2.4.0
        public void startDocument (XMLLocator locator, String encoding, NamespaceContext nsCtx, Augmentations a) {
            startDocument(locator, encoding, a);
        }

        /**
         * The start of the document.
         *
         * @throws SAXException Thrown by handler to signal an error.
         */
        // XMLDocumentHandler 2.0.0b4
        public void startDocument (XMLLocator locator, String encoding, Augmentations a) {
            
            trace ("startDocument()"); // NOI18N
            
            this.locator = locator;
            try {
                returnDocument = document = new TreeDocument (null,null,null);
                pushParentNode ((TreeDocument)document);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // startDocument()
        
        /**
         * Notifies of the presence of an XMLDecl line in the document. If
         * present, this method will be called immediately following the
         * startDocument call.
         *
         * @param version    The XML version.
         * @param encoding   The IANA encoding name of the document, or null if
         *                   not specified.
         * @param standalone The standalone value, or null if not specified.
         *
         * @throws SAXException Thrown by handler to signal an error.
         */
        public void xmlDecl (String version, String encoding, String standalone, Augmentations a) {
            
            trace ("xmlDecl()"); // NOI18N
            
            try {
                ((TreeDocument)document).setHeader (version, encoding, standalone);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // xmlDecl(String,String,String)
        
        
        // XMLDTDHandler 2.4.0 and XMLDocumentHandler > 2.0.0b4
        public void textDecl (String version, String encoding, Augmentations a) {
            
            trace ("textDecl()"); // NOI18N
            
            // if we are DTD parser scanning base DTD document entity
            if (isXMLDocument == false && inDTD && inEntity () == false) {
                try {
                    ((TreeDTD)document).setHeader (version, encoding);
                } catch (TreeException ex) {
                    throw new XNIException (ex);
                }
            }
        }

        // XMLDTDHAndler 2.0.0b4
        public void textDecl (String version, String encoding) {
            textDecl(version, encoding, null);
        }
        
        /**
         * Notifies of the presence of the DOCTYPE line in the document.
         */
        public void doctypeDecl (String rootElement, String publicId, String systemId, Augmentations a) {
            
            trace ("doctypeDecl(" + rootElement + "," + publicId + ")"); // NOI18N
            
            try {
                TreeDocumentType _doctype =
                new TreeDocumentType (rootElement, publicId, systemId);
                setBeginPosition (_doctype);
                ((TreeDocument)document).setDocumentType (_doctype);
                
                doctype = _doctype;
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // doctypeDecl(String,String,String)
        
        
        /**
         * The start of an element.
         */
        public void startElement (QName element, XMLAttributes attributes, Augmentations a) {
            
            trace ("startElement(" + element + ")"); // NOI18N
            
            try {
                tempNode = new TreeElement (element.rawname);
                startElementImpl ((TreeElement) tempNode, attributes);
                
                pushParentNode ((TreeElement)tempNode);
                elementStack.push (tempNode);
                
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // startElement(QName,XMLAttributes)
        
        /**
         * This callback represents &lt;.....<b>/</b>&gt;.
         */
        public void emptyElement (QName qName, XMLAttributes attributes, Augmentations a) {
            
            trace ("emptyElement(" + qName + ")"); // NOI18N
            
            try {
                tempNode = new TreeElement (qName.rawname, true);
                startElementImpl ((TreeElement) tempNode, attributes);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        }
        
        /**
         * Insert element and its attributes at hiearchy
         */
        private void startElementImpl (TreeElement elem, XMLAttributes attributes) throws TreeException {
            
            setBeginPosition (elem);
            
            //??? is it really neccessary
            if (currentParentNode () instanceof TreeDocument) {
                ((TreeDocument)currentParentNode ()).setDocumentElement (elem);
            } else {
                appendChild (elem);
            }
            
            // handle attributes
            
            int attrCount = attributes.getLength ();
            for (int i = 0; i < attrCount; i++) {
                boolean specified = attributes.isSpecified (i);
                
                if ( specified == true ) { // TEMPORARY -- not specified nodes will not be added into element
                    
                    attributes.getName (i, tmpQName);      //fill tmpQName
                    String val = attributes.getNonNormalizedValue (i);  //???getNonNormalizedValue
                    
                    TreeAttribute attr;  // to be filled
                    
                    if (val.indexOf ('&') < 0) {
                        
                        attr = new TreeAttribute (tmpQName.rawname, val, specified);
                        
                    } else {
                        
                        attr = new TreeAttribute (tmpQName.rawname, "", specified); // NOI18N
                        List list = attr.getValueList ();
                        list.clear ();
                        
                        // build attribute value, split content as refs and text
                        
                        int lastOffset = 0;  // offset
                        for (int offset = val.indexOf ('&'); offset >= 0; offset = val.indexOf ('&', offset + 1)) {
                            
                            int endOffset = val.indexOf (';', offset);
                            String name = val.substring (offset + 1,  endOffset);
                            
                            if (offset > lastOffset) {
                                // insert text
                                TreeText text =
                                new TreeText (val.substring (lastOffset, offset));
                                list.add (text);
                            }
                            
                            
                            if (name.startsWith ("#")) { // NOI18N
                                TreeCharacterReference chref =
                                new TreeCharacterReference (name);
                                list.add (chref);
                            } else {
                                TreeGeneralEntityReference gref =
                                new TreeGeneralEntityReference (name);
                                list.add (gref);
                            }
                            
                            lastOffset = endOffset + 1;
                        }
                        
                        if (val.length () > lastOffset) {
                            String lastText = val.substring (lastOffset);
                            list.add (new TreeText (lastText));
                        }
                    }
                    
                    if ( !!! specified ) {
                        setReadOnly (attr);
                    }
                    elem.addAttribute (attr);
                    
                } // if ( specified == true )
            }
            
            // recall remenbered internal DTD  //!!!
            
            if (rememberingReader == null) {
                return;
            }
            StringBuffer mem = rememberingReader.stopRemembering ();
            if (mem == null) return;
            
            String idtd = mem.toString ();
            int start = -1, end = -1;  // results
            int now, last = -1;  // tmps
            char delimiter;
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamBuilderXercesImpl: going to inspect:\n" + idtd);

            // find out DOCTYPE declaration
            // #23197 eliminate doctypes in comment (simple aproximation)
            
            final String DOCTYPE = "<!DOCTYPE";                                 // NOI18N
            int pos = -1;
DOCTYPE_LOOP:
            while (true) {
                pos = idtd.indexOf (DOCTYPE, ++pos);
                if (pos == -1) {
                    Util.THIS.debug ("XNIBuilder: no DOCTYPE detected.");       // NOI18N
                    return;
                } else {
                    int comment = -1;
                    while (true) {
                        comment = idtd.indexOf("<!--", ++comment);                // NOI18N
                        if (comment != -1 && comment < pos) {
                            if (idtd.indexOf("-->", comment) > pos) {           // NOI18N
                                // it is commented out, try another
                                break;
                            } else {
                                // commentd ends before, but it does not proof anything
                                continue;
                            }
                        } else {
                            break DOCTYPE_LOOP;
                        }
                    }
                }
            }
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nlast index = " + pos);

            // skip root element name
            
            pos += DOCTYPE.length ();
            for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
            for (; StringUtil.isWS (idtd.charAt (pos)) == false; pos ++);
            for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
            

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nafter process index = " + pos);

            // SYSTEM or PUBLIC or [
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\nTesting DOCTYPE kind-----\n" + idtd.substring (pos));
            
            if (idtd.charAt (pos) == '[') {  // just internal dtd
                start = ++pos;
            } else if (idtd.charAt (pos) == 'S') { //SYSTEM "" [
                for (; StringUtil.isWS (idtd.charAt (pos)) == false; pos ++);
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                delimiter = idtd.charAt (pos++);
                for (; idtd.charAt (pos) != delimiter; pos ++);
                pos++;
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                if (idtd.charAt (pos) == '[') {
                    start = ++pos;
                }
            } else if (idtd.charAt (pos) == 'P') {  // PUBLIC "" "" [
                for (; StringUtil.isWS (idtd.charAt (pos)) == false; pos ++);
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                delimiter = idtd.charAt (pos++);
                for (; idtd.charAt (pos) != delimiter; pos ++);
                pos++;
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                delimiter = idtd.charAt (pos++);
                for (; idtd.charAt (pos) != delimiter; pos ++);
                pos++;
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                if (idtd.charAt (pos) == '[') {
                    start = ++pos;
                }
            }
            
            if (start == -1) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamBuilderXercesImpl: it does not have internal DTD.");

                return;
            } else {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n---Analyzing internal DTD:\n" + idtd.substring (start));
            }
            
            // search for internal DTD end
            
            for (last = pos-1; idtd.startsWith ("]>", pos) == false && last < pos;) {
                
                last = pos;
                
                // skip comments and WS
                for (; StringUtil.isWS (idtd.charAt (pos)); pos ++);
                
                now = StringUtil.skipDelimited (idtd, pos, "<!--", "-->");
                if (now != -1) {
                    pos = now;
                    continue;
                }
                
                // skip PIs
                now = StringUtil.skipDelimited (idtd, pos, "<?", "?>");
                if (now != -1) {
                    pos = now;
                    continue;
                }
                
                // skip decls
                now = StringUtil.skipDelimited (idtd, pos, '<', '>' , "\"'");
                if (now != -1) {
                    pos = now;
                    continue;
                }
                
                // skip references
                now = StringUtil.skipDelimited (idtd, pos, '%', ';' , "");
                if (now != -1) {
                    pos = now;
                    continue;
                }
                
            }
            
            if (last == pos) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamBuilderXercesImpl: end not reached");

                return;
            }
            
            String internalDTDText = idtd.substring (start, pos);

            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Internal DTD:" + internalDTDText + "\n--");
            
            // use introspectio to set it
            
            try {
                if (doctype == null) return;
                Class klass = doctype.getClass ();
                Field field = klass.getDeclaredField ("internalDTDText");
                field.setAccessible (true);
                field.set (doctype, internalDTDText);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                // ignore introspection exceptions
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamBuilderXercesImpl.settingInternaDTDText", ex);
            }
            
        }
        
        
        /**
         * Character content.
         */
        public void characters (XMLString text, Augmentations a) {
            
            try {
                if (inCharacterRef == true) return; // ignore resolved
                
                if (inDTD) {
                    if (currentParentNode () instanceof TreeConditionalSection) {
                        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("\n*** TreeStreamBuilderXercesImpl::characters: XMLString = '" + text + "'"); // NOI18N
                        
                        ((TreeConditionalSection)currentParentNode ()).setIgnoredContent (
                        text.toString ()
                        );
                    }
                } else if (inCDATASection) {
                    cdataSectionBuffer.append (text.toString ());
                } else {
                    tempNode = new TreeText (text.toString ());
                    setBeginPosition (tempNode);
                    appendChild ((TreeText)tempNode);
                }
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // characters(XMLString)
  
        
        // XMLDTDHandler 2.4.0
        public void ignoredCharacters (XMLString text, Augmentations a) {
            characters( text, null);
        }

        // XMLDTDHandler 2.0.0b4
        public void characters (XMLString text) {
            characters( text, null);
        }
        
        /**
         * Ignorable whitespace.
         */
        public void ignorableWhitespace (XMLString text, Augmentations a) {
            try {
                tempNode = new TreeText (text.toString ());  //???
                setBeginPosition (tempNode);
                appendChild ((TreeText)tempNode);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // ignorableWhitespace(XMLString)
        
        /**
         * The end of an element.
         */
        public void endElement (QName element, Augmentations a) {
            trace ("endElement(" + element + ")"); // NOI18N
            
            try {
                TreeElement el = (TreeElement) elementStack.pop ();
                el.normalize ();  //??? parser return multiline text as multiple characters()
                popParentNode ();
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // endElement(QName)
        
        /**
         * The start of a CDATA section. Buffer its content.
         */
        public void startCDATA (Augmentations a) {
            inCDATASection = true;
            cdataSectionBuffer.delete (0, cdataSectionBuffer.length ());
            //!!! save position
        } // startCDATA()
        
        /**
         * The end of a CDATA section.
         */
        public void endCDATA (Augmentations a) {
            
            inCDATASection = false;
            
            try {
                tempNode = new TreeCDATASection (cdataSectionBuffer.toString ());
                setBeginPosition (tempNode);  //!!! error
                appendChild ((TreeCDATASection)tempNode);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // endCDATA()
        
        /**
         * The end of the document.
         */
        public void endDocument (Augmentations a) {
            trace ("endDocument()"); // NOI18N
            
            if (parentObjectListStack.isEmpty () == false) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Inconsistency at parentStack: " + parentObjectListStack ); // NOI18N
            } else if (elementStack.isEmpty () == false) {
                if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("Inconsistency at elementStack: " + parentObjectListStack ); // NOI18N
            } else {
                isCorrect = true;
            }
        } // endDocument()
        
        //
        // XMLDocumentHandler and XMLDTDHandler methods
        //
        
        public void endPrefixMapping (String prefix, Augmentations a) {
            // not interested
        }
        
        public void startPrefixMapping (String prefix, String uri, Augmentations a) {
            // not interested
        }
        
        // XMLDTDHandler 2.4.0
        public void startExternalSubset(XMLResourceIdentifier entity, Augmentations a) {
            startEntity(
                "[dtd]", entity.getPublicId(), entity.getLiteralSystemId(),
                entity.getBaseSystemId(), null, a
            );
        }

        public void startGeneralEntity(String name, XMLResourceIdentifier entity, String encoding, Augmentations a) {
            startEntity(
                name, entity.getPublicId(), entity.getLiteralSystemId(),
                entity.getBaseSystemId(), encoding, a
            );
        }

        /**
         * This method notifies of the start of an entity. The document entity
         * has the pseudo-name of "[xml]"; The DTD has the pseudo-name of "[dtd];
         * parameter entity names start with '%'; and general entity names are
         * just the entity name.
         *
         * @param encoding special value of "IGNORE" markg parameter
         *        entities in DTD markup (these are ignored)
         */
        private void startEntity (String name, String publicId, String systemId,
        String baseSystemId, String encoding, Augmentations a) {
            
            trace ("startEntity(" + name + ")"); // NOI18N
            
            try {
                
                // do not theat these as external entities
                // DTD is wrapped intentionally
                
                if (XML_ENTITY.equals (name)) return;
                if (isXMLDocument == false && DTD_ENTITY.equals (name)) return;
                
                
                if (DTD_ENTITY.equals (name) && isXMLDocument) {
                    
                    hasExternalDTD = true;
                    
                    // we are entering external DTD attach all to DOCTYPE ObjectList
                    // There is performance optimalization: External DTD model
                    // can be shared among several instances referring it.
                    // It's currently managed by the TreeDocumentType class
                    TreeObjectList external = doctype.getExternalDTD ();
                    if (external == null) {
                        TreeDTDFragment entity = new TreeDTDFragment();
                        TreeObjectList holder = entity.getChildNodes();
                        pushParentObjectList (holder);
                        doctype.setExternalDTD(entity);
                    } else {
                        // It was already parsed, ignore its content
                        pushParentObjectList(null);
                    }

                } else if (name.startsWith ("#")) { // NOI18N
                    
                    tempNode = new TreeCharacterReference (name);
                    appendChild (tempNode);
                    setBeginPosition (tempNode);
                    inCharacterRef = true;
                    
                } else if ( "lt".equals (name) || "gt".equals (name) || "amp".equals (name) // NOI18N
                || "apos".equals (name) || "quot".equals (name)) { // NOI18N
                    
                    tempNode = new TreeGeneralEntityReference (name);
                    appendChild (tempNode);
                    setBeginPosition (tempNode);
                    inCharacterRef = true;
                    
                } else if (name.startsWith ("%")) { // NOI18N
                    
                    if ("IGNORE".equals (encoding)) { // NOI18N
                        // skip entities in markup, place the into unattached list
                        name = name.substring (1);
                        pushParentNode (new TreeParameterEntityReference (name));
                        
                    } else {
                        name = name.substring (1);
                        tempNode = new TreeParameterEntityReference (name);  //??? external entities
                        appendChild ((TreeParameterEntityReference)tempNode);
                        setBeginPosition (tempNode);
                        pushParentNode ((TreeEntityReference)tempNode);
                    }
                    
                } else {
                    
                    tempNode = new TreeGeneralEntityReference (name);  //??? external entities
                    appendChild ((TreeGeneralEntityReference)tempNode);
                    setBeginPosition (tempNode);
                    pushParentNode ((TreeEntityReference)tempNode);
                    
                }
                
                enterEntity ();
                
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // startEntity(String,String,String,String)
        

        // XMLDTDHandler 2.0.0b4
        public void startEntity (String name, String publicId, String systemId,
        String baseSystemId, String encoding) {
            startEntity(name, publicId, systemId, baseSystemId, encoding, null);
        }
        
        // XMLDTDHanlder 2.4.0
        public void startParameterEntity(String name, XMLResourceIdentifier entity, String encoding, Augmentations a) {
            String pname = name;
            if (false == name.startsWith("%")) {
                pname = "%" + name;
            }
            startEntity(
                pname, entity.getPublicId(), entity.getLiteralSystemId(),
                entity.getBaseSystemId(), encoding, a
            );
        }

        /**
         * A comment.
         */
        // XMLDTDHandler 2.4.0 and XMLDocumentHandler
        public void comment (XMLString text, Augmentations a) {
            
            trace ("comment()"); // NOI18N
            
            try {
                tempNode = new TreeComment (text.toString ());
                setBeginPosition (tempNode);
                appendChild ((TreeComment)tempNode);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // comment(XMLString)
        
        // XMLDTDHandler 2.0.0b4
        public void comment (XMLString text) {
            comment(text, null);
        }
        
        /**
         * A processing instruction. Processing instructions consist of a
         * target name and, optionally, text data. The data is only meaningful
         * to the application.
         */
        // XMLDTDHandler 2.4.0 and XMLDocumentHandler > 2.0.0b4
        public void processingInstruction (String target, XMLString data, Augmentations a) {
            
            trace ("processingInstruction(" + target + ")"); // NOI18N
            
            try {
                tempNode = new TreeProcessingInstruction (target, data.toString ());
                setBeginPosition (tempNode);
                appendChild ((TreeProcessingInstruction)tempNode);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // processingInstruction(String,XMLString)

        // XMLDTDHandler 2.0.0b4
        public void processingInstruction (String target, XMLString data) {
            processingInstruction( target, data, null);
        }
        
        // XMLDTDHandler 2.4.0
        public void endExternalSubset(Augmentations a) {
            endEntity("[dtd]", a);
        }

        // XMLDTDHandler 2.4.0
        public void endParameterEntity(String name, Augmentations a) {
            String pname = name;
            if (false == name.startsWith("%")) {
                pname = "%" + name;
            }
            endEntity(pname, a);
        }

        public void endGeneralEntity(String name, Augmentations a) {
            endEntity(name, a);
        }

        /**
         * This method notifies the end of an entity. The document entity has
         * the pseudo-name of "[xml]"; the DTD has the pseudo-name of "[dtd];
         * parameter entity names start with '%'; and general entity names are
         * just the entity name.
         */
        private void endEntity (String name, Augmentations a) {
            trace ("endEntity(" + name + ")");  // NOI18N
            
            // skip for root entities of XML documents and
            // standalone DTDs parsed by DTD parser
            
            if (XML_ENTITY.equals (name)) return;
            if (isXMLDocument == false && DTD_ENTITY.equals (name)) return;
            
            exitEntity ();
            
            if (inCharacterRef == true) {
                inCharacterRef = false;
                return;
            }
            
            if (isXMLDocument && DTD_ENTITY.equals (name)) {
                popParentObjectList ();  // DOCTYPE ObjectList
            } else {
                popParentNode ();
            }
            
        } // endEntity(String)

        
        //??? DTDHandler
        public void endEntity (String name) {
            endEntity(name, null);
        }
        
        // XMLDTDHandler methods ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        // XMLDTDHandler 2.4.0
        public XMLDTDSource getDTDSource() {
            return xmldtdSource;
        }

        // XMLDTDHandler 2.4.0
        public void setDTDSource( XMLDTDSource src) {
            xmldtdSource = src;
        }

        // XMLDTDHandler 2.0.0b4
        public void startDTD ( XMLLocator locator, Augmentations a) {
            startDTD(locator);
        }
        
        /**
         * The start of the DTD (external part of it is reported by startEntity).
         */
        // XMLDTDHandler 2.0.0b4
        public void startDTD ( XMLLocator locator) {
            trace ("startDTD()");  // NOI18N
            
            try {
                inDTD = true;
                
                if (isXMLDocument) {
                    
                    pushParentNode (doctype);
                    
                } else {
                    
                    // replace returnDocument
                    returnDocument = document = new TreeDTD (null,null);
                    pushParentNode ((TreeDTD)document);
                }
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // startDTD()
        
        // XMLDTDHandler 2.4.0
        public void elementDecl (String name, String contentModel, Augmentations a) {
            elementDecl(name, contentModel);
        }

        /**
         * An element declaration.
         */
        // XMLDTDHandler 2.0.0b4
        public void elementDecl (String name, String cM) {
            trace ("elementDecl(" + name + ")"); // NOI18N
            if (ASSERT)
                doAssert (inDTD);
            
            try {
                appendChild (new TreeElementDecl (name, this.contentModel));
                this.contentModel = null;
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
            
        } // elementDecl(String,String)
        
        // XMLDTDHandler 2.4.0
        public void startAttlist (String elementName, Augmentations a) {
            startAttlist(elementName);
        }

        /**
         * The start of an attribute list.
         */
        // XMLDTDHandler 2.0.0b4
        public void startAttlist (String elementName) {
            
            trace ("startAttlist(" + elementName + ")"); // NOI18N
            
            try {
                tempNode = new TreeAttlistDecl (elementName);
                attlistDecl = (TreeAttlistDecl) tempNode;
                appendChild (attlistDecl);
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // startAttlist(String)
        
        // XMLDTDHandler 2.4.0
        public void attributeDecl(String elementName, String attributeName,
                                  String type, String[] enumeration,
                                  String defaultType, XMLString defaultValue,
                                  XMLString nonNormalizedDefaultValue, Augmentations a) {
            attributeDecl(
                elementName, attributeName, type,
                enumeration, defaultType, defaultValue
            );
        }

        /**
         * An attribute declaration.
         */
        // XMLDTDHandler 2.0.0b4
        public void attributeDecl (String elementName, String attributeName,
                                   String type, String[] enumeration,
                                   String defaultType, XMLString defaultValue) {
            
            trace ("attributeDecl(" + attributeName + ")"); // NOI18N
            
            try {
                TreeAttlistDecl list;
                
                if (attlistDecl != null) {
                    list = attlistDecl;
                } else {
                    list = new TreeAttlistDecl (elementName);
                }
                if ( type.equals ("ENUMERATION") ) { // NOI18N
                    type = null;
                }

                short shortDefaultType = TreeAttlistDeclAttributeDef.findDefaultType (defaultType);
                String newDefaultValue = null;
                if ( ( shortDefaultType == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_NULL ) ||
                     ( shortDefaultType == TreeAttlistDeclAttributeDef.DEFAULT_TYPE_FIXED ) ) {
                    newDefaultValue = defaultValue.toString ();
                }
                TreeAttlistDeclAttributeDef decl =
                new TreeAttlistDeclAttributeDef (attributeName, TreeAttlistDeclAttributeDef.findType (type),
                                                 enumeration, shortDefaultType, newDefaultValue);
                
                list.setAttributeDef (decl);
            } catch (TreeException exc) {
                //Util.dumpContext("TreeAttlistDecl.setReadOnly(true)"); // NOI18N
                throw new XNIException (exc);
            }
        } // attributeDecl(String,String,String,String[],String,XMLString)
        
        // XMLDTDHandler 2.4.0
        public void endAttlist (Augmentations a) {
            endAttlist();
        }

        /**
         * The end of an attribute list.
         */
        // XMLDTDHandler 2.0.0b4
        public void endAttlist () {
            
            trace ("endAttlist()"); // NOI18N
            
            attlistDecl = null;
        } // endAttlist()
        
        // XMLDTDHandler 2.4.0
        public void internalEntityDecl (String name, XMLString text, XMLString nonNormalizedText, Augmentations a)  {
            internalEntityDecl(name, text, nonNormalizedText);
        }

        /**
         * An internal entity declaration.
         *
         * @param name The name of the entity. Parameter entity names start with
         *             '%', whereas the name of a general entity is just the
         *             entity name.
         */
        // XMLDTDHandler 2.0.0b4
        public void internalEntityDecl (String name, XMLString text, XMLString nonNormalizedText)  {
            
            trace ("internalEntityDecl(" + name + ")"); // NOI18N
            
            try {
                boolean par = name.startsWith ("%"); // NOI18N
                if (par) {
                    name = name.substring (1);
                }
                appendChild (new TreeEntityDecl (par, name, text.toString ()));
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // internalEntityDecl(String,XMLString)
        
        // XMLDTDHandler 2.4.0
        public void externalEntityDecl (String name, String publicId,
        String systemId, String baseSystemId, Augmentations a) {
            externalEntityDecl( name, publicId, systemId, baseSystemId);
        }

        /**
         * An external entity declaration.
         *
         * @param name     The name of the entity. Parameter entity names start
         *                 with '%', whereas the name of a general entity is just
         *                 the entity name.
         */
        // XMLDTDHandler 2.0.0b4
        public void externalEntityDecl (String name, String publicId,
        String systemId, String baseSystemId) {
            
            trace ("externalEntityDecl(" + name + ")"); // NOI18N
            
            try {
                boolean par = name.startsWith ("%"); // NOI18N
                if (par) {
                    name = name.substring (1);
                }
                
                appendChild (new TreeEntityDecl (par, name, publicId, systemId));
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // externalEntityDecl(String,String,String)
        
        // XMLDTDHAnlder 2.4.0
        public void unparsedEntityDecl (String name,
        String publicId, String systemId,
        String notation, Augmentations a) {
            unparsedEntityDecl(name, publicId, systemId, notation);
        }

        /**
         * An unparsed entity declaration.
         */
        // XMLDTDHAnlder 2.0.0b4
        public void unparsedEntityDecl (String name,
        String publicId, String systemId,
        String notation) {
            
            trace ("unparsedEntityDecl(" + name + ")"); // NOI18N
            
            try {
                appendChild (new TreeEntityDecl (name, publicId, systemId, notation));
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // unparsedEntityDecl(String,String,String,String)
        
        // XMLDTDHandler 2.4.0
        public void notationDecl (String name, String publicId, String systemId, Augmentations a) {
            notationDecl(name, publicId, systemId);
        }

        /**
         * A notation declaration
         */
        // XMLDTDHandler 2.0.0b4
        public void notationDecl (String name, String publicId, String systemId) {
            
            trace ("notationDecl(" + name + ")"); // NOI18N
            
            try {
                appendChild (new TreeNotationDecl (name, publicId, systemId));
            } catch (TreeException exc) {
                throw new XNIException (exc);
            }
        } // notationDecl(String,String,String)
        
        // XMLDTDHandler 2.4.0
        public void startConditional (short type, Augmentations a) {
            startConditional(type);
        }

        /**
         * The start of a conditional section.
         *
         * @param type The type of the conditional section. This value will
         *             either be CONDITIONAL_INCLUDE or CONDITIONAL_IGNORE.
         */
        // XMLDTDHandler 2.0.0b4
        public void startConditional (short type) {
            trace ("startConditional(" + type + ")"); // NOI18N
            if (ASSERT)
                doAssert (inDTD);
            
            if (type == CONDITIONAL_INCLUDE) {
                tempNode = new TreeConditionalSection (true);
            } else {
                tempNode = new TreeConditionalSection (false);
            }
            
            appendChild ((TreeConditionalSection) tempNode);
            setBeginPosition (tempNode);
            pushParentNode ((TreeConditionalSection) tempNode);
            
        } // startConditional(short)
        
        // XMLDTDHandler 2.4.0
        public void endConditional (Augmentations a) {
            endConditional();
        }

        /**
         * The end of a conditional section.
         */
        // XMLDTDHandler 2.0.0b4
        public void endConditional () {
            trace ("endConditional()");  // NOI18N
            
            popParentNode ();
        } // endConditional()
        
        // XMLDTDHandler 2.4.0
        public void endDTD (Augmentations a) {
            endDTD();
        }

        /**
         * The end of the DTD.
         *
         * @throws SAXException Thrown by handler to signal an error.
         */
        // XMLDTDHandler 2.0.0b4
        public void endDTD () {
            trace ("endDTD()");  // NOI18N
            
            if (isXMLDocument) {
                
                popParentNode ();
                
            } else {
                
                popParentNode ();
                
                //??? Xerces miss '<' at the end of entity
                // so such documents are reported as correct
                
                isCorrect = errors == 0;
                throw new DTDStopException ();
                
            }
            
            inDTD = false;
        } // endDTD()
        
        
        // Content Model parser ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        private TreeElementDecl.ContentType lastType;     // occurence operators are applied on this
        private TreeElementDecl.ContentType contentModel; // OUTPUT result field
        private Stack contentModelMembersStack;           // stack of parent group members
        
        public XMLDTDContentModelSource getDTDContentModelSource() {
            return xmldtdContentModelSource;
        }

        public void setDTDContentModelSource(XMLDTDContentModelSource src) {
            xmldtdContentModelSource = src;
        }

        public void startContentModel (String elementName, Augmentations a) {
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("startContentModel(" + elementName + ")"); // NOI18N
            
            lastType = null;
            contentModelMembersStack = new Stack ();
            
        }
        
        public void any (Augmentations a) {
            contentModel = new ANYType ();
        }
        
        public void empty (Augmentations a) {
            contentModel = new EMPTYType ();
        }
        
        public void pcdata (Augmentations a) {
            setMembersType (new MixedType ());
        }
        
        
        // it is not called for mixed type
        public void startGroup (Augmentations a) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("startGroup()"); // NOI18N

            startMembers ();
        }
        
        public void element (String elementName, Augmentations a) {
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("element(" + elementName + ")"); // NOI18N
            
            lastType = new NameType (elementName);
            addMember (lastType);
        }
        
        // determine type of content model group
        public void separator (short separator, Augmentations a) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("childrenSeparator()"); // NOI18N
            
            switch (separator) {
                case SEPARATOR_SEQUENCE:
                    setMembersType (new SequenceType ());
                    break;
                case SEPARATOR_CHOICE:
                    setMembersType (new ChoiceType ());
                    break;
                default:
                    doAssert (false);
            }
        }
        
        //
        // INPUT lastType field
        //
        public void occurrence (short occurrence, Augmentations a) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("childrenOccurrence()"); // NOI18N
            
            switch (occurrence) {
                case OCCURS_ZERO_OR_ONE:
                    lastType.setMultiplicity ('?');
                    break;
                case OCCURS_ZERO_OR_MORE:
                    lastType.setMultiplicity ('*');
                    break;
                case OCCURS_ONE_OR_MORE:
                    lastType.setMultiplicity ('+');
                    break;
                default:
                    doAssert (false);
            }
            
        }
        
        public void endGroup (Augmentations a) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("childrenEndGroup()"); // NOI18N

            ChildrenType group = getMembersType ();
            group.addTypes (endMembers ());
            lastType = group;
            addMember (lastType);
        }
        
        public void endContentModel (Augmentations a) {
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("endContentModel()"); // NOI18N

            if (contentModel == null && lastType == null) { // #PCDATA
                contentModel = new MixedType ();
            } else if (contentModel == null) {  // we are of CHILDREN_TYPE or mixed type
                contentModel = lastType;
                if (contentModel instanceof MixedType) {
                    contentModel.setMultiplicity ('*');
                }
            }
        }
        
        
        
        private void startMembers () {
            contentModelMembersStack.push (new Members (13));
        }
        
        private void addMember (TreeElementDecl.ContentType child) {
            
            // we are at top level of content model, lastType becomes it
            if (contentModelMembersStack.isEmpty ()) return;
            
            Collection members = (Collection) contentModelMembersStack.peek ();
            members.add (child);
        }
        
        private Collection endMembers () {
            return (Collection) contentModelMembersStack.pop ();
        }
        
        // we can predict member group now, if know balk it
        private void setMembersType (ChildrenType group) {
            
            // we are at top level of content model, lastType becomes it
            if (contentModelMembersStack.isEmpty ()) return;
            
            Members members = (Members) contentModelMembersStack.peek ();
            if (members.group == null) members.group = group;
        }
        
        private ChildrenType getMembersType () {
            Members members = (Members) contentModelMembersStack.peek ();
            if (members.group == null) {
                return new ChoiceType ();
            } else {
                return members.group;
            }
        }
        
        //
        // Hold additional information about group that holds these members
        //
        private class Members extends ArrayList {
            
            private ChildrenType group;
            
            private static final long serialVersionUID =4614355994187952965L;
            
            public Members (int initSize) {
                super (initSize);
                group = null;
            }
        }
        
        // ~~~~~~~~~~~~~~~~~ ERROR HANDLER ~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        
        public void error (org.xml.sax.SAXParseException e) {
            trace (e.getMessage ());

            errorHandler.message (TreeStreamBuilderErrorHandler.ERROR_ERROR, e);
        }
        
        public void warning (org.xml.sax.SAXParseException e) {
            trace (e.getMessage ());

            errorHandler.message (TreeStreamBuilderErrorHandler.ERROR_WARNING, e);
        }
        
        public void fatalError (org.xml.sax.SAXParseException e) {
            trace (e.getMessage ());

            errors++;
            errorHandler.message (TreeStreamBuilderErrorHandler.ERROR_FATAL_ERROR, e);
        }
        
        // ~~~~~~~~~~~~~~~~~~ UTILITY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        /**
         */
        private void setReadOnly (TreeObject treeObject) {
            setReadOnly (treeObject, true);
        }
        
        
        private void setReadOnly (TreeObject treeObject, boolean value) {
            try {
                Method setReadOnlyMethod = TreeObject.class.getDeclaredMethod ("setReadOnly", new Class[] { Boolean.TYPE }); // NOI18N
                setReadOnlyMethod.setAccessible (true);
                setReadOnlyMethod.invoke (treeObject, new Object[] { value == true ? Boolean.TRUE : Boolean.FALSE});
            } catch (NoSuchMethodException exc) {
            } catch (IllegalAccessException exc) {
            } catch (InvocationTargetException exc) {
            }
        }
        
        /**
         * As positons will be supported
         */
        private void setBeginPosition (TreeNode n) {
            //!!!
        }
        
        
        /**
         * @return TreeDocument or null if fatal errors occured
         */
        private TreeDocumentRoot getDocumentRoot () {
            TreeDocumentRoot doc = (TreeDocumentRoot) (errors > 0 ? null : returnDocument);
            
            if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("TreeStreamBuilderXercesImpl returns: " + doc); // NOI18N
            
            return doc;
        }
        
        
        /**
         * Shortcut - retrieves child list and pushes it at stack
         */
        private void pushParentNode (TreeParentNode parent) {
            parentNodeStack.push (parent);
            pushParentObjectList (parent.getChildNodes ());
        }
        
        /**
         * Set new parent list pushing original one to node stack
         */
        private void pushParentObjectList (TreeObjectList parentList) {
            parentObjectListStack.push (parentObjectList);

            // inherit null parents (for nested parents)
            if (parentObjectList != null || parentObjectListStack.size() == 1) {
                parentObjectList = parentList;
            } else {
                parentObjectList = null;
            }
        }
        
        /**
         * Restore current children list poping it from stack.
         */
        private void popParentObjectList () {
            parentObjectList = (TreeObjectList) parentObjectListStack.pop ();
        }
        
        /**
         * Resotore parent node and its list from stack
         */
        private void popParentNode () {
            popParentObjectList ();
            TreeParentNode parentNode = (TreeParentNode) parentNodeStack.pop ();
            
            // referenced things and DTD things are read only
            
            if ( parentNode instanceof TreeGeneralEntityReference ) {  // entities in XML doc
                
                setReadOnly (parentNode.getChildNodes ());
                
            } else if ( parentNode instanceof TreeDTD ) {  // whole DTD
                
                setReadOnly (parentNode);
                
            } else if ( parentNode instanceof TreeDocumentType ) {
                
                setReadOnly (parentNode.getChildNodes ());

                // there can be pure internal DTD
                TreeObjectList externalDTD = ((TreeDocumentType)parentNode).getExternalDTD ();
                if (externalDTD != null) {
                    setReadOnly (externalDTD);
                }
            }
        }
        
        private TreeParentNode currentParentNode () {
            return (TreeParentNode) parentNodeStack.peek ();
        }
        
        /**
         * Add child to current parent list.
         */
        private void appendChild (TreeObject child) {
            if (parentObjectList != null) parentObjectList.add (child);
        }
        
        /**
         * Enter entity, following events origanes from entity resolution
         */
        private void enterEntity () {
            entityCounter++;
        }
        
        /**
         * Exit entity.
         */
        private void exitEntity () {
            entityCounter--;
        }
        
        /**
         * Test whether we are in entity, i.e. creating readonly nodes.
         */
        private boolean inEntity () {
            return entityCounter > 0;
        }
        
        private void trace (String msg) {
            if ( Util.THIS.isLoggable() ) {
                String location = "";
                if (locator != null) {
                    String entity = locator.getExpandedSystemId ();
                    int index = entity.lastIndexOf ('/');
                    entity = entity.substring (index > 0 ? index : 0);
                    location =  entity + "/" + locator.getLineNumber () + ":" + locator.getColumnNumber () ;
                }            
                Util.THIS.debug ("X2T " + location + " " + msg);  // NOI18N
            }
        }
        
        private void doAssert (boolean asrt) {
            if (asrt == false) {
                throw new IllegalStateException ("ASSERT"); // NOI18N
            }
        }
        
    }
    
    
    
}
