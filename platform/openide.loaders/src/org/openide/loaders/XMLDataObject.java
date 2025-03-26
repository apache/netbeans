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

package org.openide.loaders;


import java.io.*;
import java.lang.ref.*;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;
import java.util.logging.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.openide.loaders.RuntimeCatalog;
import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.DataEditorSupport;
import org.openide.util.*;
import org.openide.util.lookup.AbstractLookup;
import org.openide.windows.CloneableOpenSupport;
import org.openide.xml.*;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/** 
 * Object that provides main functionality for xml documents.
 * These objects are recognized by the <code>xml</code> extension and
 * <code>text/xml</code> MIME type. 
 * <p>
 * It is declaratively extensible by an {@link Environment}. 
 * The <code>Environment</code> is assigned to document instances using a provider
 * registered by DOCTYPE's public ID in the system filesystem under
 * <code>xml/lookups/{Transformed-DOCTYPE}</code> where the DOCTYPE transformation
 * is the same as that defined for {@link EntityCatalog} registrations.
 * 
 * @see XMLUtil
 * @see EntityCatalog
 *
 * @author  Libor Kramolis, Jaroslav Tulach, Petr Kuzel
 */
public class XMLDataObject extends MultiDataObject {
     /** generated Serialized Version UID */
    static final long serialVersionUID = 8757854986453256578L;
    
   /** Public ID of xmlinfo dtd. 
    * @deprecated replaced with Lookup
    */
    @Deprecated
    public static final String XMLINFO_DTD_PUBLIC_ID_FORTE = "-//Forte for Java//DTD xmlinfo//EN"; // NOI18N
    /** @deprecated replaced with Lookup
     */
    @Deprecated
    public static final String XMLINFO_DTD_PUBLIC_ID = "-//NetBeans IDE//DTD xmlinfo//EN"; // NOI18N

    /** Mime type of XML documents. */
    public static final String MIME = "text/xml";  //NOI18N
    //public static final String MIME2 = "application/xml"; //NOI18N
    
    /** PROP_DOCUMENT not parsed yet. Constant for getStatus method. */
    public static final int STATUS_NOT     = 0;
    /** PROP_DOCUMENT parsed ok. Constant for getStatus method. */
    public static final int STATUS_OK      = 1;
    /** PROP_DOCUMENT parsed with warnings. Constant for getStatus method. */
    public static final int STATUS_WARNING = 2;
    /** PROP_DOCUMENT parsed with errors. Constant for getStatus method. */
    public static final int STATUS_ERROR   = 3;
    
    /** property name of DOM document property */
    public static final String PROP_DOCUMENT = "document"; //??? it is not bound well // NOI18N

    /** property name of info property 
     * @deprecated info is not supported anymore. Replaced with lookup.
     */
    @Deprecated
    public static final String PROP_INFO = "info"; // NOI18N

    /** Default XML parser error handler */
    private static ErrorPrinter errorHandler = new ErrorPrinter();
        
    /**
     * Chain of resolvers contaning all EntityResolvers registred by a user.
     */
    @Deprecated
    private static XMLEntityResolverChain chainingEntityResolver;
    
    /** map of DTD publicID => Info. */
    private static HashMap<String, Info> infos = new HashMap<String, Info>();
    // the lock can be seamlesly shared by all instances
    private static Object emgrLock = new Object ();
    
    
    
    
    
    // 
    // Instance variables
    // 
    
    
    /** the XML document we delegate to */
    private DelDoc doc;
    

    /** the result of parsing */
    private int status;  //??? why it is not a bound property?
                         // it if often out-of date (e.g. garbage collection)

    /** @deprecated EditorCookie provided by subclass support 
     * need to prevail build in cookies.
     */
    @Deprecated
    private EditorCookie editor = null;

    /** 
     * Task body triggered by file change (primaryFile() or xmlinfo) parsing document
     * for extension (info) assigment information (xmlinfo or public id)
     */
    private XMLDataObjectInfoParser infoParser;

    /* For logging and debugging. */
    static final Logger ERR = Logger.getLogger(XMLDataObject.class.getName());

               
    /** 
     * Create new XMLDataObject. It is usually called by a loader.
     * A user can get existing XMLDataObject by calling {@link DataObject#find(FileObject) 
     * DataObject.find(FileObject f)} instead.
     *
     * @param fo the primary file object, never <code>null</code>
     * @param loader loader of this data object, never <code>null</code>
     */
    public XMLDataObject (FileObject fo, MultiFileLoader loader)
    throws DataObjectExistsException {
        this(fo, loader, true);
    }

    /**
     * Constructs XMLDataObject without any registered cookies (for editor,
     * open, etc.). Useful for subclasses.
     *
     * @param fo the primary file object, never <code>null</code>
     * @param loader loader of this data object, never <code>null</code>
     * @param registerEditor call with false to skip registrations of various
     *   editor related cookies
     * @since 7.10
     */
    protected XMLDataObject (FileObject fo, MultiFileLoader loader, boolean registerEditor)
    throws DataObjectExistsException {
        super (fo, loader);
        
        status = STATUS_NOT;

        if (registerEditor) {
            registerEditor();
        }
    }

    private void registerEditor() {
        // register provided cookies
        // EditorCookie must be for back compatability consulted with subclasses
        //
        // In new model subclasses should directly provide its CookieSet.Factory that
        // uses last prevails order instead of old CookieSet first prevails order.
        // It completely prevails over this factory :-)

        CookieSet.Factory factory = new CookieSet.Factory() {
            public <T extends Node.Cookie> T createCookie(Class<T> klass) {
                if (klass.isAssignableFrom(EditorCookie.class)
                   || klass.isAssignableFrom(OpenCookie.class)
                   || klass.isAssignableFrom(CloseCookie.class)
                   || klass.isAssignableFrom(PrintCookie.class) ) {

                    if (editor == null) editor = createEditorCookie();  // the first pass
                    if (editor == null) return null;                    //??? gc unfriendly

                    return klass.isAssignableFrom(editor.getClass()) ? klass.cast(editor) : null;
                } else {
                    return null;
                }
            }
        };

        CookieSet cookies = getCookieSet();
        // EditorCookie.class must be synchronized with
        // XMLEditor.Env->findCloneableOpenSupport
        cookies.add(EditorCookie.class, factory);
        cookies.add(OpenCookie.class, factory);
        cookies.add(CloseCookie.class, factory);
        cookies.add(PrintCookie.class, factory);

        // set info for this file
        //getIP ().resolveInfo ();        #16045
        cookies.assign( SaveAsCapable.class, new SaveAsCapable() {
            public void saveAs( FileObject folder, String fileName ) throws IOException {
                EditorCookie ec = getCookieSet().getCookie( EditorCookie.class );
                if( ec instanceof DataEditorSupport ) {
                    ((DataEditorSupport)ec).saveAs( folder, fileName );
                } else {
                    Logger.getLogger( XMLDataObject.class.getName() ).log( Level.FINE, "'Save As' requires DataEditorSupport" ); //NOI18N
                }
            }
        });
    }

    /** Getter for info parser. Initializes the infoparser in "lazy" way so it is accessble even before
        * the constructor finishes.
        */
    private final XMLDataObjectInfoParser getIP () {
        synchronized (emgrLock) {
            if (infoParser == null) {
                infoParser = new XMLDataObjectInfoParser (this);
            }
        }
        return infoParser;
    }



     /** If the Info associated with this data object (if any) provides 
    * a subclass of Node, then this object is created to represent the
    * XML data object, otherwise DataNode is created.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    @Override
    protected Node createNodeDelegate () {
        return new XMLNode(this);
    }



    /** Called when the info file is parsed and the icon should change.
    * @param res resource for the icon
    * @deprecated it is better to listen on properties
    */
    @Deprecated
    protected void updateIconBase (String res) {
        //??? we could add default behaviour, taking status into account
    }

    /*
     * Wait until background parsing terminates to avoid concurent file access.
     * It should terminate very early if just running, we can wait for it.
     */
    protected void handleDelete() throws IOException {

        getIP ().waitFinished();         // too late wait for finnish
        super.handleDelete();
    }

    public HelpCtx getHelpCtx () {
        // help for fix #23528, objects represents 'settings' nodes in Options dialog
        // returns DEFAULT_HELP for next processing
        try {
            if (getPrimaryFile ().getFileSystem ().isDefault ()) {
                if (getCookie (InstanceCookie.class)!=null) {
                    return HelpCtx.DEFAULT_HELP;
                }
            }
        } catch (FileStateInvalidException fsie) {
            // cannot determine type of this file object ==> return help id as normal
        }
        return new HelpCtx (XMLDataObject.class);
    }

    /**     
     * Cookies from assigned Environment are not placed into 
     * protected CookieSet and can be obtained only by invoking this method.
     * <p>
     * Cookie order for Info environments are handled  consistently with
     * CookieSet i.e. FIFO.
     * @return a cookie (instanceof cls) that has been found in info or
     * super.getCookie(cls).
     */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cls) {
        getIP ().waitFinished();

        Object cake = getIP().lookupCookie(cls);
       
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("Query for " + cls + " for " + this); // NOI18N
            ERR.fine("Gives a cake " + cake + " for " + this); // NOI18N
        }
        
        if (cake instanceof InstanceCookie) {
            cake = ofCookie ((InstanceCookie)cake, cls);
        }

        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("After ofCookie: " + cake + " for " + this); // NOI18N
        }
        
        if (cake == null) {
            cake = super.getCookie (cls);
        }
        
        if (ERR.isLoggable(Level.FINE)) {
            ERR.fine("getCookie returns " + cake + " for " + this); // NOI18N
        }
        if (cake instanceof Node.Cookie) {
            assert cake == null || cls.isInstance(cake) : "Cannot return " + cake + " for " + cls + " from " + this;
            return cls.cast(cake);
        }
        return null;
    }

    @Override
    public Lookup getLookup() {
        if (getClass() == XMLDataObject.class) {
            Node n = getNodeDelegateOrNull();
            if (n == null) {
                setNodeDelegate(n = createNodeDelegate());
            }
            return n.getLookup();
        }
        return super.getLookup();
    }

    /** Special support of InstanceCookie.Of. If the Info class
     * provides InstanceCookie but not IC.Of, we add the extra interface to
     * this data object.
     *
     * @param ic instance cookie
     * @param cls constraining class
     * @return instance of InstanceCookie.Of
     */
    private InstanceCookie ofCookie (InstanceCookie ic, Class<?> cls) {
        if (ic instanceof InstanceCookie.Of) {
            return ic;
        } else if (! cls.isAssignableFrom (ICDel.class)) {
            // Someone was looking for, and a processor etc. was
            // providing, some specialization which ICDel cannot
            // provide. Return the real implementation and forget
            // about making this a IC.Of.
            return ic;
        } else {
            ICDel d = new ICDel (this, ic);
            return d;
        }
    }

    private void notifyEx(Exception e) {
        Exceptions.attachLocalizedMessage(e,
                                          "Cannot resolve following class in xmlinfo."); // NOI18N
        Exceptions.printStackTrace(e);
    }
    
    /** Allows subclasses to provide their own editor cookie.
     * @return an editor cookie to be used as a result of <code>getCookie(EditorCookie.class)</code>
     *
     * @deprecated CookieSet factory should be used by subclasses instead.
     */
    @Deprecated
    protected EditorCookie createEditorCookie () {
        return new XMLEditorSupport (this);
    }
    
    // Vertical CookieManager
    private final void addSaveCookie (SaveCookie save) {
        getCookieSet ().add (save);
    }
    private final void removeSaveCookie (SaveCookie save) {
        getCookieSet ().remove (save);
    }

    //??? we ahould add it into class comment to make it public
    // or should we introduce second layer XMLDataObject extending this one
    // and having documented this functionality  (we cannot because of 
    // so this huge DataObject will survive createEditorCookie())
    /*
     * Really simple implementation of OpenCookie, EditorCookie, PrintCookie,
     * CloseCookie and managing SaveCookie.
     */
    private static class XMLEditorSupport extends DataEditorSupport implements OpenCookie, EditorCookie.Observable, PrintCookie, CloseCookie {
        public XMLEditorSupport (XMLDataObject obj) {
            super (obj, new XMLEditorEnv (obj));
            //when undelying fileobject has a mimetype defined,
            //don't enforce text/xml on the editor document.
            //be conservative and apply the new behaviour only when the mimetype is xml like..
            if (obj.getPrimaryFile().getMIMEType().indexOf("xml") == -1) { // NOI18N
                setMIMEType ("text/xml"); // NOI18N
            }
        }
        class Save implements SaveCookie {
            public void save () throws IOException {
                saveDocument ();
                getDataObject ().setModified (false);
            }
        }
        protected boolean notifyModified () {
            if (! super.notifyModified ()) {
                return false;
            }
            if (getDataObject ().getCookie (SaveCookie.class) == null) {
                ((XMLDataObject) getDataObject ()).addSaveCookie (new Save ());
                getDataObject ().setModified (true);
            }
            return true;
        }
        protected void notifyUnmodified () {
            super.notifyUnmodified ();
            SaveCookie save = getDataObject().getCookie(SaveCookie.class);
            if (save != null) {
                ((XMLDataObject) getDataObject ()).removeSaveCookie (save);
                getDataObject ().setModified (false);
            }
        }
        
        @Override
        protected Pane createPane() {
            if (MultiDOEditor.isMultiViewAvailable()) {
                MultiDataObject mdo = (MultiDataObject) getDataObject();
                return MultiDOEditor.createMultiViewPane("text/plain", mdo); // NOI18N
            }
            return super.createPane();
        }
        
        //!!! it also stays for SaveCookie however does not understand
        // encoding declared in XML header => need to be rewritten.
        private static class XMLEditorEnv extends DataEditorSupport.Env {
            private static final long serialVersionUID = 6593415381104273008L;
            
            public XMLEditorEnv (DataObject obj) {
                super (obj);
            }
            protected FileObject getFile () {
                return getDataObject ().getPrimaryFile ();
            }
            protected FileLock takeLock () throws IOException {
                return ((XMLDataObject) getDataObject ()).getPrimaryEntry ().takeLock ();
            }
            public CloneableOpenSupport findCloneableOpenSupport () {
                // must be sync with cookies.add(EditorCookie.class, factory);
                // #12938 XML files do not persist in Source editor
                return (CloneableOpenSupport) getDataObject ().getCookie (EditorCookie.class);
            }
        }
    }

    /** Creates w3c's document for the xml file. Either returns cached reference
    * or parses the file and creates new document.
    * 
    * @return the parsed document
    * @exception SAXException if there is a parsing error
    * @exception IOException if there is an I/O error
    */
    public final Document getDocument () throws IOException, SAXException {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("getDocument" + " for " + this);
        synchronized (this) {
            DelDoc d = doc;
            if (d == null) {
                d = new DelDoc(this);
                doc = d;
            }
            return d.getProxyDocument();
        }
    }
    
    /** Clears the document. Called when the document file is changed.
     */
    final void clearDocument () {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("clearDocument" + " for " + this);
        //err.notify (ErrorManager.INFORMATIONAL, new Throwable ("stack dump"));
        doc = null;
        firePropertyChange (PROP_DOCUMENT, null, null);
    }

    @Override
    void notifyFileChanged(FileEvent fe) {
        super.notifyFileChanged(fe);
        getIP().fileChanged(fe);
    }

    /** 
     * @return one of STATUS_XXX constants representing PROP_DOCUMENT state. 
     */
    public final int getStatus () {
        return status;
    }

    /** @deprecated not used anymore
     * @return null
     */
    @Deprecated
    public final Info getInfo () {
        return null;
    }

    /** @deprecated does not do anything useful
     */
    @Deprecated
    public final synchronized void setInfo (Info ii) throws IOException {
    }

    /** Parses the primary file of this data object.
    * and provide different implementation.
    *
    * @return the document in the primary file
    * @exception IOException if error during parsing occures
    */
    final Document parsePrimaryFile () throws IOException, SAXException {
        if (ERR.isLoggable(Level.FINE)) ERR.fine("parsePrimaryFile" + " for " + this);
        String loc = getPrimaryFile().toURL().toExternalForm();
        try {
            return XMLUtil.parse(new InputSource(loc), false, /* #36295 */true, errorHandler, getSystemResolver());
        } catch (IOException e) {
            // Perhaps this document was not on a mounted filesystem.
            // Try again with an input stream - no relative URLs will work, but this
            // is extremely unlikely to matter. Cf. #36340.
            InputStream is = getPrimaryFile().getInputStream();
            try {
                return XMLUtil.parse(new InputSource(is), false, true, errorHandler, getSystemResolver());
            } finally {
                is.close();
            }
        }
    }


    // ~~~~~~~~~~~~~~~~~~~~ Start of Utilities ~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting null error handler and validation to false.
    */
    @Deprecated
    public static Document parse (URL url) throws IOException, SAXException {
        return parse (url, errorHandler, false);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    * @param validate if true validating parser is used
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting null handler.
    */
    @Deprecated
    public static Document parse (URL url, boolean validate) throws IOException, SAXException {
        return parse (url, errorHandler, validate);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead
    * setting validation to false.
    */
    @Deprecated
    public static Document parse (URL url, ErrorHandler eh) throws IOException, SAXException {
        return parse (url, eh, false);
    }

    /** Factory a DocumentBuilder and let it create a org.w3c.dom.Document
    * This method takes URL. After successful finish the
    * document tree is returned.
    * A parser producing the Document has
    * set entity resolver to system entity resolver chain.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    * @param validate if true validating parser is used
    * @throws SAXException annotated if thrown due to configuration problem
    * @throws FactoryConfigurationError
    * @return org.w3c.dom.Document
    * @deprecated Use {@link XMLUtil#parse(InputSource, boolean, boolean, ErrorHandler, EntityResolver) XMLUtil} instead.     
    */
    @Deprecated
    public static Document parse (URL url, ErrorHandler eh, boolean validate) throws IOException, SAXException {
        
        return XMLUtil.parse (new InputSource(url.toExternalForm()),validate, false, eh, getChainingEntityResolver());
    }

    /** Creates SAX parse that can be used to parse XML files.
     * @return sax parser
     * @deprecated Use {@link XMLUtil#createXMLReader() XMLUtil} instead.
     * It will create a SAX XMLReader that is SAX Parser replacement.
     * You will have to replace DocumentHandler by ContentHandler
     * besause XMLReader accepts just ContentHandler. 
     * <p>Alternatively if not interested in new callbacks defined by
     * SAX 2.0 you can wrap returned XMLReader into XMLReaderAdapter
     * that implements Parser.
     */
    @Deprecated
    public static Parser createParser () {
        return createParser (false);
    }
   
    
    /** Factory SAX parser that can be used to parse XML files.
     * The factory is created according to javax.xml.parsers.SAXParserFactory property.
     * The parser has set entity resolver to system entity resolver chain.
     * @param validate if true validating parser is returned
     * @throws FactoryConfigurationError 
     * @return sax parser or null if no parser can be created
     * @deprecated Use {@link XMLUtil#createXMLReader(boolean,boolean)} instead
     * setting ns to false.
     * For more details see {@link #createParser() createParser}
     */
    @Deprecated
    public static Parser createParser (boolean validate) {
        try {
            Parser parser = new org.xml.sax.helpers.XMLReaderAdapter (XMLUtil.createXMLReader(validate));
            parser.setEntityResolver(getChainingEntityResolver());
            return parser;
        } catch (SAXException ex) {
            Exceptions.attachLocalizedMessage(ex,
                                          "Can not create a SAX parser!\nCheck javax.xml.parsers.SAXParserFactory property features and the parser library presence on classpath."); // NOI18N
            Exceptions.printStackTrace(ex);
            return null;
        }
    }


    /** 
     * Creates empty DOM Document using JAXP factoring.
     * @return Document or null on problems with JAXP factoring
     * @deprecated Replaced with {@link XMLUtil#createDocument(String,String,String,String) XMLUtil}
     *             It directly violates DOM's root element reference read-only status.
     *             If you can not move to XMLUtil for compatabilty reasons please
     *             replace with following workaround:
     * <pre>{@code
     * String templ = "<myroot/>";
     * InputSource in = new InputSource(new StringReader(templ));
     * in.setSystemId("StringReader");  //workaround
     * DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
     * Document doc = builder.parse(in);
     * }</pre>
     */
    @Deprecated
    public static Document createDocument() {
        
        try {
            DocumentBuilder builder;
            DocumentBuilderFactory factory;

            //create factory according to javax.xml.parsers.SAXParserFactory property
            //or platform default (i.e. com.sun...)
            try {
                factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setNamespaceAware(false);
            } catch (FactoryConfigurationError err) {
                Exceptions.attachLocalizedMessage(err,
                                                  "Can not create a factory!\nCheck " +
                                                  "javax.xml.parsers.DocumentBuilderFactory" +
                                                  "  property and the factory library presence on classpath."); // NOI18N
                Exceptions.printStackTrace(err);
                return null;
            }

            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                SAXException sex = new SAXException("Configuration exception."); // NOI18N
                sex.initCause(ex);
                Exceptions.attachLocalizedMessage(sex,
                        "Can not create a DOM builder!\nCheck javax.xml.parsers.DocumentBuilderFactory property and the builder library presence on classpath."); // NOI18N
                throw sex;
            }

            return builder.newDocument();
        } catch (SAXException ex) {
            return null;
        }
    }

    /** 
     * Writes DOM Document to writer. 
     *
     * @param doc DOM Document to be written
     * @param writer OutoutStreamWriter preffered otherwise
     *        encoding will be left for implementation specific autodection
     *
     * @deprecated Encoding used by Writer
     * may be in direct conflict with encoding
     * declared in document. Replaced with {@link XMLUtil#write(Document, OutputStream, String) Util}.
     */
    @Deprecated
    public static void write (Document doc, Writer writer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        writer.write(baos.toString("UTF-8"));
    }

    /**
     * Write Document into OutputStream using given encoding. 
     * It is a shortcut for writing configurations etc. It guarantee 
     * just that data will be written. Structure and indentation
     * may change.
     *
     * @param doc DOM Document to be written
     * @param out data sink     
     * @param enc - XML defined encoding name (i.e. IANA defined, one of UTF-8, UNICODE, ASCII).
     * @deprecated Moved to {@link XMLUtil#write(Document, OutputStream, String) XMLUtil}.
     */
    @Deprecated
    public static void write(Document doc, OutputStream out, String enc) throws IOException {
        XMLUtil.write(doc, out, enc);
    }
    
    
    /** 
     * Creates SAX InputSource for specified URL 
     * @deprecated Deprecated as it was a workaround method. Replace
     * with <code>new InputSource(url.toExternalForm())</code>.
     */
    @Deprecated
    public static InputSource createInputSource(URL url) throws IOException {                
        return new InputSource(url.toExternalForm());
    }

    /**
     * Registers the given public ID as corresponding to a particular 
     * URI, typically a local copy.  This URI will be used in preference
     * to ones provided as system IDs in XML entity declarations.  This
     * mechanism would most typically be used for Document Type Definitions
     * (DTDs), where the public IDs are formally managed and versioned.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param uri The URI of the preferred copy of that entity
     *
     * @deprecated Do not rely on global (non-modular) resolvers.
     *             Use {@link EntityCatalog} and {@link XMLUtil}
     *             instead.
     */
    @Deprecated
    public static void registerCatalogEntry (String publicId, String uri) {
        Lookup.getDefault().lookup(RuntimeCatalog.class).registerCatalogEntry(publicId, uri);
    }

    /**
     * Registers a given public ID as corresponding to a particular Java
     * resource in a given class loader, typically distributed with a
     * software package.  This resource will be preferred over system IDs
     * included in XML documents.  This mechanism should most typically be
     * used for Document Type Definitions (DTDs), where the public IDs are
     * formally managed and versioned.
     *
     * <P> If a mapping to a URI has been provided, that mapping takes
     * precedence over this one.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param resourceName The name of the Java resource
     * @param loader The class loader holding the resource, or null if
     *  it is a system resource.
     *
     * @deprecated Do not rely on global (non-modular) resolvers.
     *             Use {@link EntityCatalog} and {@link XMLUtil}
     *             instead.
     */
    @Deprecated
    public static void registerCatalogEntry (String publicId, String resourceName, ClassLoader loader) {
        Lookup.getDefault().lookup(RuntimeCatalog.class).registerCatalogEntry(publicId, resourceName, loader);
    }

    /**
     * Add a given entity resolver to the NetBeans resolver chain.
     * The resolver chain is searched by private chaining resolver
     * until some registered resolver succed.
     *
     * <P>Every created parser use global entity resolver and then chain.
     *
     * @deprecated EntityResolver is a parser user responsibility. 
     *             Every time set a EntityResolver to an XML parser you use.
     *             The OpenIDE now defines a system {@link EntityCatalog}.
     *
     * @param resolver non null resolver to be added
     *
     * @return true if successfully added
     */
    @Deprecated
    public static boolean addEntityResolver(EntityResolver resolver) {
        // return false; Is is deprecated :-)
        return getChainingEntityResolver().addEntityResolver(resolver);
    }

    /**
     * Remove a given entity resolver from the NetBeans resolver chain.
     *
     * <P>Every created parser use global entity resolver and then chain.
     *
     * @deprecated EntityResolver is a parser user responsibility.
     *
     * @param resolver non null resolver to be removed
     * @return removed resolver instance or null if not present
     */    
    @Deprecated
    public static EntityResolver removeEntityResolver(EntityResolver resolver) {
        return getChainingEntityResolver().removeEntityResolver(resolver);
    }


    /** Accessor method for chaining entity resolver implementation. */
    @Deprecated
    private static synchronized XMLEntityResolverChain getChainingEntityResolver() {

        if (chainingEntityResolver == null) {                    
            chainingEntityResolver = new XMLEntityResolverChain();
            chainingEntityResolver.addEntityResolver(getSystemResolver());
        }
        
        return chainingEntityResolver;
        
    }
    
    /** Lazy initialized system resolver. */
    private static EntityResolver getSystemResolver() {        
        return  EntityCatalog.getDefault();
    }
    
    /**
     * Registers new Info to particular XML document content type as 
     * recognized by DTD public id. The registration is valid until JVM termination.
     *
     * @param publicId used as key
     * @param info associated value or null to unregister
     *
     * @deprecated Register an {@link Environment} via lookup, see
     * {@link XMLDataObject some details}.
     */    
    @Deprecated
    public static void registerInfo (String publicId, Info info) {  //!!! to be replaced by lookup
        synchronized (infos) {
            if (info == null) {
                infos.remove(publicId);
            } else {
                infos.put(publicId, info);
            }
        }
    }

    /**
    * Obtain registered Info for particular DTD public ID.
    *
    * @param publicId key which value is required
    * @return Info clone that is used for given publicId or null
     *
     * @deprecated Register via lookup
    */
    @Deprecated
    public static Info getRegisteredInfo(String publicId) {  //!!! to be replaced by lookup
        synchronized (infos) {
            Info ret = infos.get(publicId);
            return ret == null ? null : (Info)ret.clone ();
        }
    }

    /**
     * Default ErrorHandler reporting to log.
     */
    static class ErrorPrinter implements ErrorHandler {
        
        private void message(final String level, final SAXParseException e) {
            
            if (!LOG.isLoggable(Level.FINE)) {
                return;
            }
            
            final String msg = NbBundle.getMessage(
                XMLDataObject.class,
                "PROP_XmlMessage",  //NOI18N
                new Object [] {
                    level,
                    e.getMessage(),
                    e.getSystemId() == null ? "" : e.getSystemId(), // NOI18N
                    "" + e.getLineNumber(), // NOI18N
                    "" + e.getColumnNumber() // NOI18N
                }
            );
                
            LOG.fine(msg);
        }

        public void error(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlError"), e);  //NOI18N
        }

        public void warning(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlWarning"), e); //NOI18N
        }

        public void fatalError(SAXParseException e) {
            message (NbBundle.getMessage(XMLDataObject.class, "PROP_XmlFatalError"), e); //NOI18N
        }
    } // end of inner class ErrorPrinter
    
    
    /**
     * It simulates null that forbiden by SAX specs.
     */
    static class NullHandler extends DefaultHandler implements LexicalHandler {

        static final NullHandler INSTANCE = new NullHandler();
        
        NullHandler() {}
        
        // LexicalHandler

        public void startDTD(String root, String pID, String sID) throws SAXException {
        }

        public void endDTD() throws SAXException {
        }

        public void startEntity(String name) throws SAXException {
        }

        public void endEntity(String name) throws SAXException {
        }

        public void startCDATA() throws SAXException {
        }

        public void endCDATA() throws SAXException {
        }

        public void comment(char[] ch, int start, int length) throws SAXException {
        }
    }

    
    
    
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~ private Loader ~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
        
    /** The DataLoader for XmlDataObjects.
     */
    static final class Loader extends MultiFileLoader {
        static final long serialVersionUID =3917883920409453930L;
        /** Creates a new XMLDataLoader */
        public Loader () {
	    super ("org.openide.loaders.XMLDataObject");  //!!! so the relation loader data object is fixed // NOI18N
            //super (XMLDataObject.class);                // nothing like looks loader can be constructed
        }                                                 // can it produce subclasses?

        protected String actionsContext () {
            return "Loaders/text/xml/Actions"; // NOI18N
        }
        
        /** Get the default display name of this loader.
        * @return default display name
        */
        protected String defaultDisplayName () {
            return NbBundle.getMessage (XMLDataObject.class, "PROP_XmlLoader_Name");
        }
        
        /** For a given file finds a primary file.
        * @param fo the file to find primary file for
        *
        * @return the primary file for the file or null if the file is not
        *   recognized by this loader
        */
        protected FileObject findPrimaryFile (FileObject fo) {
            String mime = fo.getMIMEType ();
            if (mime.endsWith("/xml") || mime.endsWith("+xml")) { // NOI18N
                return fo;
            }
            // not recognized
            return null;            
        }
        
        /** Creates the right data object for given primary file.
        * It is guaranteed that the provided file is realy primary file
        * returned from the method findPrimaryFile.
        *
        * @param primaryFile the primary file
        * @return the data object for this file
        * @exception DataObjectExistsException if the primary file already has data object
        */
        protected MultiDataObject createMultiObject (FileObject primaryFile)
        throws DataObjectExistsException {
            return new XMLDataObject (primaryFile, this);
        }

        /** Creates the right primary entry for given primary file.
        *
        * @param primaryFile primary file recognized by this loader
        * @return primary entry for that file
        */
        protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        /** Creates right secondary entry for given file. The file is said to
        * belong to an object created by this loader.
        *
        * @param secondaryFile secondary file for which we want to create entry
        * @return the entry
        */
        protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
            // JST: We do not have secondary entries anymore, but it probably does not matter...
            return new FileEntry (obj, secondaryFile);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~ extension support via info ~~~~~~~~~~~~~~~~~~~~~~~~
    
    // i would like to throw it away sometimes in future and replace it by better one

    /** This class has to be implemented by all processors in the
    * xmlinfo file. It is cookie, so after parsing such class is instantiated
    * and put into data objects cookie set.
    *
    * @deprecated Use {@link org.openide.loaders.Environment.Provider} instead.
    */
    @Deprecated
    public static interface Processor extends Node.Cookie {
        /** When the XMLDataObject creates new instance of the processor,
        * it uses this method to attach the processor to the data object.
        *
        * @param xmlDO XMLDataObject
        */
        public void attachTo (XMLDataObject xmlDO);
    }
    
    
    /** @deprecated use Lookup
     * Representation of xmlinfo file holding container of Processors.
     */
    @Deprecated
    public static final class Info implements Cloneable {
        List<Class<?>> processors;
        String iconBase;

        /** Create info */
        public Info () {
            processors = new ArrayList<Class<?>> ();
            iconBase = null;
        }

        @Override
        public Object clone () {
            Info ii = new Info();
            for (Class<?> proc: processors) {
                ii.processors.add (proc);
            }
            ii.iconBase = iconBase;
            return ii;
        }

        /** Add processor class to info. 
        * The class should be public and either implement the Processor 
        * interface or should
        * have public constructor with one argument (DataObject or XMLDataObject).
        *
        * @param proc the class to add to this info
        * @exception IllegalArgumentException if the class does not seem to be valid
        */
        public synchronized void addProcessorClass(Class<?> proc) {
            if (!Processor.class.isAssignableFrom (proc)) {
                Constructor[] arr = proc.getConstructors();
                for (int i = 0; i < arr.length; i++) {
                    Class<?>[] params = arr[i].getParameterTypes();
                    if (params.length == 1) {
                        if (
                            params[0] == DataObject.class || 
                            params[0] == XMLDataObject.class 
                        ) {
                            arr = null;
                            break;
                        }
                    }
                }
                
                if (arr != null) {
                    // no suitable constructor
                    throw new IllegalArgumentException();
                }
            }
            
            processors.add (proc);
        }

        /** Remove processor class from info.
         * @return true if removed
         */
        public boolean removeProcessorClass(Class<?> proc) {
            return processors.remove (proc);
        }

        public Iterator<Class<?>> processorClasses() {
            return processors.iterator();
        }

        /** Set icon base */
        public void setIconBase (String base) {
            iconBase = base;
        }

        /** @return icon base */
        public String getIconBase () {
            return iconBase;
        }

        /** Write specified info to writer */
        public void write (Writer writer) throws IOException {
            throw new IOException ("Not supported anymore"); // NOI18N
        }
        
        public boolean equals (Object obj) {
            if (obj == null) return false;
            if (obj instanceof Info == false) return false;
            
            Info i = (Info) obj;
            
            return ((iconBase != null && iconBase.equals(i.iconBase)) || (i.iconBase == iconBase)) 
                    && processors.equals(i.processors);
        }
    } // end of inner class Info
    
    
    /** A method for backward compatibility to create a lookup from data object and info
     * @param obj xml data object
     * @param info the info that should be associated
     */
    static Lookup createInfoLookup (XMLDataObject obj, Info info) {
        return new InfoLkp (obj, info);
    }

    
    /** A backward compatibility class that converts the content of 
     * an Info object into a Lookup class.
     */
    private static final class InfoLkp extends AbstractLookup {
        public final Info info;
        
        public InfoLkp (XMLDataObject obj, Info info) {
            this.info = info;
            
            Iterator<Class<?>> it = info.processorClasses ();
            ArrayList<InfoPair> arr = new ArrayList<InfoPair> (info.processors.size ());
            while (it.hasNext ()) {
                Class<?> c = it.next ();
                arr.add (new InfoPair (obj, c));
            }
            
            setPairs (arr);
        }
        
        /** A pair that receives a class and can create its instance either
         * using default constructor or by passing data object into one 
         * argument constructor.
         */
        private static final class InfoPair extends AbstractLookup.Pair {
            /** the class to use or null if object has already been created */
            private Class<?> clazz;
            /** XMLDataObject associated or object created */
            private Object obj;
            
            /** For use by subclasses. */
            protected InfoPair (XMLDataObject obj, Class<?> c) {
                this.obj = obj;
                this.clazz = c;
            }

            /** Tests whether this item can produce object
            * of class c.
            */
            protected boolean instanceOf (Class c) {
                Class<?> cc = c;
                Class<?> temp = clazz;
                if (temp == null) {
                    return cc.isInstance (obj);
                } else {
                    return cc.isAssignableFrom (temp);
                }
            }

            /** Method that can test whether an instance of a class has been created
             * by this item.
             *
             * @param obj the instance
             * @return if the item has already create an instance and it is the same
             *   as obj.
             */
            protected boolean creatorOf (Object obj) {
                return this.obj == obj;
            }

            /** The class of the result item.
             * @return the instance of the object.
             */
            public synchronized Object getInstance () {
                if (clazz == null) {
                    // already created an object
                    return obj;
                }
                
                // after this method the obj or null will contain the created object
                // instead of reference to XMLDataObject
                XMLDataObject xmlDataObject = (XMLDataObject)obj;
                obj = null;
                
                // the clazz will be null to signal, that an instance
                // of object has been created
                Class<?> next = clazz;
                clazz = null;

                try {
                    if (Processor.class.isAssignableFrom (next)) {
                        // the class implements Processor interface, so use
                        // default constructor to construct instance
                        obj = next.getDeclaredConstructor().newInstance ();
                        Processor proc = (Processor) obj;
                        proc.attachTo (xmlDataObject);
                        return obj;
                    } else {
                        // does not implement processor, try to search
                        // for constructor with one argument of DataObject or
                        // XMLDataObject

                        Constructor[] arr = next.getConstructors();
                        for (int i = 0; i < arr.length; i++) {
                            Class<?>[] params = arr[i].getParameterTypes();
                            if (params.length == 1) {
                                if (
                                    params[0] == DataObject.class || 
                                    params[0] == XMLDataObject.class 
                                ) {
                                    obj = arr[i].newInstance(
                                        new Object[] { xmlDataObject }
                                    );
                                    return obj;
                                }
                            }
                        }
                    }
                    throw new InternalError ("XMLDataObject processor class " + next + " invalid"); // NOI18N
                } catch (ReflectiveOperationException e) {
                    xmlDataObject.notifyEx(e);
                }
                
                return obj;
            }

            /** The class of the result item.
             * @return the class of the item
             */
            public Class getType () {
                Class<?> temp = clazz;
                return temp != null ? temp : obj.getClass ();
            }

            /** A persistent indentifier of the item. Can be stored and use 
             * in next run of the system.
             *
             * @return a string id of the item
             */
            public String getId () {
                return "Info[" + getType ().getName (); // NOI18N
            }
            
            /** The best display name is probably the name of type...
             */
            public String getDisplayName () {
                return getType ().getName ();
            }
        }
    }
    

    /** Computes correct node for given XMLDataObject.
     */
    private Node findNode () {
        Node n = (Node)getIP ().lookupCookie (Node.class);

        if (n == null) {
            return new PlainDataNode();
        } else {
            return n;
        }
    }
    
    private final class PlainDataNode extends DataNode {
        public PlainDataNode() {
            super(XMLDataObject.this, Children.LEAF);
            setIconBaseWithExtension("org/openide/loaders/xmlObject.gif"); // NOI18N
        }
    }
        
    
    /** Node that delegates either to data node or to a node provided by
     * the data object itself.
     */
    final class XMLNode extends FilterNode {
        public XMLNode (XMLDataObject obj) {
            this (obj.findNode ());
        }
        private XMLNode (Node del) {
            super (del, new FilterNode.Children (del));            
            //setShortDescription("XML FILE");
        }
        final void update () {
            changeOriginal (XMLDataObject.this.findNode (), true);
        }
        
    }
    
    /** A special delegator that adds InstanceCookie.Of to objects that miss it
     */
    private static class ICDel extends Object implements InstanceCookie.Of {
        /** object we belong to
         */
        private XMLDataObject obj;
        /** cookie we delegate to */
        private InstanceCookie ic;

        public ICDel (XMLDataObject obj, InstanceCookie ic) {
            this.obj = obj;
            this.ic = ic;
        }


        public String instanceName () {
            return ic.instanceName ();
        }

        public Class<?> instanceClass ()
        throws java.io.IOException, ClassNotFoundException {
            return ic.instanceClass ();
        }

        public Object instanceCreate ()
        throws java.io.IOException, ClassNotFoundException {
            return ic.instanceCreate ();
        }

        public boolean instanceOf (Class<?> cls2) {
            if (ic instanceof InstanceCookie.Of) {
                return ((InstanceCookie.Of) ic).instanceOf (cls2);
            } else {
                try {
                    return cls2.isAssignableFrom (instanceClass ());
                } catch (IOException ioe) {
                    // ignore exception
                    return false;
                } catch (ClassNotFoundException cnfe) {
                    // ignore exception
                    return false;
                }
            }
        }
        
        public int hashCode () {
            return 2 * obj.hashCode () + ic.hashCode ();
        }
        
        public boolean equals (Object obj) {
            if (obj instanceof ICDel) {
                ICDel d = (ICDel)obj;
                return d.obj == obj && d.ic == ic;
            }
            return false;
        }
    } // end of ICDel    
    
    static final Constructor<?> cnstr;
    static {
        try {
            Class<?> proxy = Proxy.getProxyClass(XMLDataObject.class.getClassLoader(), Document.class, DocumentType.class);
            cnstr = proxy.getConstructor(InvocationHandler.class);
            new DelDoc(null);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }    /** Delegating DOM document that provides fast implementation of
     * getDocumentType and getPublicID methods.
     */
    private static final class DelDoc implements InvocationHandler {
        private final XMLDataObject obj;
        private Reference<Document> xmlDocument;
        private final Document proxyDocument;
        
        DelDoc(XMLDataObject obj) {
            this.obj = obj;
            try {
                proxyDocument = (Document) cnstr.newInstance(this);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

        /** Creates w3c's document for the xml file. Either returns cached reference
        * or parses the file and creates new document.
        * 
        * @param force really create the document if it does not exists yet?
        * @return the parsed document or null if not forced
        */
        private final Document getDocumentImpl (boolean force) {
            synchronized (this) {
                Document doc = xmlDocument == null ? null : xmlDocument.get ();
                if (doc != null) {
                    return doc;
                }
                
                if (!force) {
                    return null;
                }

                obj.status = STATUS_OK;
                try {
                    Document d = obj.parsePrimaryFile();
                    xmlDocument = new SoftReference<Document> (d);
                    return d;
                } catch (SAXException e) {
                    ERR.log(Level.WARNING, null, e);
                } catch (IOException e) {
                    ERR.log(Level.WARNING, null, e);
                }
                
                obj.status = STATUS_ERROR;
                Document d = XMLUtil.createDocument("brokenDocument", null, null, null); // NOI18N
                
                xmlDocument = new SoftReference<Document> (d);
                
                // fire property change, because the document is errornous
                obj.firePropertyChange (PROP_DOCUMENT, null, null);
                
                return d;
            }
        }
        
        /**
         * Get the externally usable, lazy document.
         * Delegates everything to the parsed document on disk (parsing as necessary),
         * except that getDoctype().getPublicId() is specially implemented so as to
         * not require loading the whole document.
         */
        public Document getProxyDocument() {
            return proxyDocument;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("getDoctype") && args == null) { // NOI18N
                return (DocumentType)proxyDocument;
            } else if (method.getName().equals("getPublicId") && args == null) { // NOI18N
                Document d = getDocumentImpl(false);
                if (d != null) {
                    DocumentType doctype = d.getDoctype();
                    return doctype == null ? null : doctype.getPublicId();
                } else {
                    return obj.getIP().getPublicId();
                }
            } else {
                return method.invoke(getDocumentImpl(true), args);
            }
        }
        
    }
    
}
