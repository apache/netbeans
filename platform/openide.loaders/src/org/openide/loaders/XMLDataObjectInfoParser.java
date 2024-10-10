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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.xml.XMLUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

final class XMLDataObjectInfoParser extends DefaultHandler
implements FileChangeListener, LexicalHandler, LookupListener {
    
    //~~~~~~~~~~~~~~~~~~~~~~~~ PARSER ----------------------------------

    // internally stops documet parsing when looking for public id
    private static class StopSaxException extends SAXException {
        public StopSaxException() { super("STOP"); } //NOI18N
    }

    // static fields that that are logically a part of InfoParser
    
    private static final StopSaxException STOP = new StopSaxException();
    
    /** We are guaranteed to be executed in one thread let reuse parser, etc. */
    private static XMLReader sharedParserImpl = null;
   
    static {
        try {
            sharedParserImpl = XMLUtil.createXMLReader();        
            sharedParserImpl.setEntityResolver(new EmptyEntityResolver());
        } catch (SAXException ex) {
            Exceptions.attachLocalizedMessage(ex,
                                              "System does not contain JAXP 1.1 compliant parser!"); // NOI18N
            Logger.getLogger(XMLDataObject.class.getName()).log(Level.WARNING, null, ex);
        }
        
        
        //initialize stuff possibly needed by libs that do not use 
        //JAXP but SAX 2 directly
        try {
            final Properties props = System.getProperties();
            final String SAX2_KEY = "org.xml.sax.driver";  //NOI18N
            if (props.getProperty(SAX2_KEY) == null) {
                props.put(SAX2_KEY, sharedParserImpl.getClass().getName());                
            }
        } catch (RuntimeException ex) {
            //ignore it (we did the best efford)
        }
    }
    /** a string to signal null value for parsedId */
    private static final String NULL = ""; // NOI18N
   

    private Reference<XMLDataObject> xml;
    private String parsedId;
    private Lookup lookup;
    private Lookup.Result<?> result;
    private ThreadLocal<Class<?>> QUERY = new ThreadLocal<Class<?>>();

    XMLDataObjectInfoParser(XMLDataObject xml) {
        this.xml = new WeakReference<XMLDataObject>(xml);
    }

    public String getPublicId() {
        String id = waitFinished();
        Object nu = NULL;
        return id == nu ? null : id;
    }

    public Object lookupCookie(final Class<?> clazz) {
        if (QUERY.get() == clazz) {
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Cyclic deps on queried class: " + clazz + " for " + getXml());
            }
            return null;
        }
        Class<?> previous = QUERY.get();
        try {
            QUERY.set(clazz);
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Will do query for class: " + clazz + " for " + getXml());
            }
            Lookup l;
            for (;;) {
                String id = waitFinished();
                synchronized (this) {
                    if (lookup != null) {
                        l = lookup;
                    } else {
                        l = null;
                    }
                }
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("Lookup is " + l + " for id: " + id);
                }
                if (l == null) {
                    l = updateLookup(getXml(), null, id);
                    if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                        XMLDataObject.ERR.fine("Updating lookup: " + l);
                    }
                }
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("Wait lookup is over: " + l + getXml());
                }
                if (l != null) {
                    break;
                }
                if (parsedId == null) {
                    l = Lookup.EMPTY;
                    break;
                }
            }
            Lookup.Result<?> r = result;
            if (r != null) {
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("Querying the result: " + r);
                }
            } else {
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("No result for lookup: " + lookup);
                }
            }
            Object ret = l.lookup(clazz);
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Returning value: " + ret + " for " + getXml());
            }
            return ret;
        } finally {
            QUERY.set(previous);
        }
    }

    public String waitFinished() {
        return waitFinished(null);
    }

    private String waitFinished(String ignorePreviousId) {
        if (sharedParserImpl == null) {
            XMLDataObject.ERR.fine("No sharedParserImpl, exiting");
            return NULL;
        }
        XMLReader parser = sharedParserImpl;
        XMLDataObject realXML = getXml();
        if (realXML == null) {
            return NULL;
        }
        
        FileObject myFileObject = realXML.getPrimaryFile();
        String newID = null;
        if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
            XMLDataObject.ERR.fine("Going to read parsedId for " + realXML);
        }
        String previousID;
        synchronized (this) {
            previousID = parsedId;
        }
        if (previousID != null) {
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Has already been parsed: " + parsedId + " for " + realXML);
            }
            return previousID;
        }

        URL url = null;
        InputStream in = null;
        url = myFileObject.toURL();

        synchronized (this) {
            try {
                if (!myFileObject.isValid()) {
                    if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                        XMLDataObject.ERR.fine("Invalid file object: " + myFileObject);
                    }
                    return NULL;
                }
                parsedId = NULL;
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("parsedId set to NULL for " + realXML);
                }
                try {
                    in = myFileObject.getInputStream();
                } catch (IOException ex) {
                    warning(ex, "I/O exception while opening " + myFileObject);
                    return NULL;
                }
                try {
                    synchronized (sharedParserImpl) {
                        configureParser(parser, false, this);
                        parser.setContentHandler(this);
                        parser.setErrorHandler(this);
                        InputSource input = new InputSource(url.toExternalForm());
                        input.setByteStream(in);
                        parser.parse(input);
                    }
                    if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                        XMLDataObject.ERR.fine("Parse finished for " + realXML);
                    }
                } catch (StopSaxException stopped) {
                    newID = parsedId;
                    XMLDataObject.ERR.fine("Parsing successfully stopped: " + parsedId + " for " + realXML);
                } catch (SAXException checkStop) {
                    if (STOP.getMessage().equals(checkStop.getMessage())) {
                        newID = parsedId;
                        XMLDataObject.ERR.fine("Parsing stopped with STOP message: " + parsedId + " for " + realXML);
                    } else {
                        String msg = "Thread:" + Thread.currentThread().getName();
                        XMLDataObject.ERR.warning("DocListener should not throw SAXException but STOP one.\n" + msg);
                        XMLDataObject.ERR.log(Level.WARNING, null, checkStop);
                        Exception ex = checkStop.getException();
                        if (ex != null) {
                            XMLDataObject.ERR.log(Level.WARNING, null, ex);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    XMLDataObject.ERR.log(Level.INFO, null, ex);
                } catch (IOException ex) {
                    XMLDataObject.ERR.log(Level.INFO, null, ex);
                } finally {
                    if (Boolean.getBoolean("netbeans.profile.memory")) {
                        parser.setContentHandler(XMLDataObject.NullHandler.INSTANCE);
                        parser.setErrorHandler(XMLDataObject.NullHandler.INSTANCE);
                        try {
                            parser.setProperty("http://xml.org/sax/properties/lexical-handler", XMLDataObject.NullHandler.INSTANCE);
                        } catch (SAXException ignoreIt) {
                        }
                        try {
                            parser.parse((InputSource) null);
                        } catch (Exception ignoreIt) {
                        }
                    }
                    parser = null;
                }
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    XMLDataObject.ERR.log(Level.WARNING, null, ex);
                }
            }
        }
        if (ignorePreviousId != null && ignorePreviousId.equals(newID)) {
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("No update to ID: " + ignorePreviousId + " for " + realXML);
            }
            return newID;
        }
        if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
            XMLDataObject.ERR.fine("New id: " + newID + " for " + realXML);
        }
        if (newID != null) {
            updateLookup(realXML, previousID, newID);
        }
        return newID;
    }

    private Lookup updateLookup(XMLDataObject realXML, String previousID, String id) {
        if (realXML == null) {
            return lookup;
        }
        
        synchronized (this) {
            if (previousID != null && previousID.equals(id) && lookup != null) {
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("No need to update lookup: " + id + " for " + realXML);
                }
                return lookup;
            }
        }
        Lookup newLookup;
        @SuppressWarnings("deprecation")
        XMLDataObject.Info info = XMLDataObject.getRegisteredInfo(id);
        if (info != null) {
            newLookup = XMLDataObject.createInfoLookup(realXML,info);
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Lookup from info: " + newLookup + " for " + realXML);
            }
        } else {
            newLookup = Environment.findForOne(realXML);
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Lookup from env: " + newLookup + " for " + realXML);
            }
            if (newLookup == null) {
                newLookup = Lookup.EMPTY;
            }
        }
        synchronized (this) {
            Lookup.Result<?> prevRes = result;
            lookup = newLookup;
            if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                XMLDataObject.ERR.fine("Shared lookup updated: " + lookup + " for " + realXML);
            }
            result = lookup.lookupResult(Node.Cookie.class);
            result.addLookupListener(this);
            if (prevRes != null) {
                prevRes.removeLookupListener(this);
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("Firing property change for " + realXML);
                }
                realXML.firePropertyChange(DataObject.PROP_COOKIE, null, null);
                if (XMLDataObject.ERR.isLoggable(Level.FINE)) {
                    XMLDataObject.ERR.fine("Firing done for " + realXML);
                }
            }
            return newLookup;
        }
    }

    private void configureParser(XMLReader parser, boolean validation, LexicalHandler lex) {
        try {
            parser.setFeature("http://xml.org/sax/features/validation", validation);
        } catch (SAXException sex) {
            XMLDataObject.ERR.fine("Warning: XML parser does not support validation feature.");
        }
        try {
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", lex);
        } catch (SAXException sex) {
            XMLDataObject.ERR.fine("Warning: XML parser does not support lexical-handler feature.");
        }
    }

    public void warning(Throwable ex) {
        warning(ex, null);
    }

    public void warning(Throwable ex, String annotation) {
        XMLDataObject.ERR.log(Level.INFO, annotation, ex);
    }

    public void startDTD(String root, String pID, String sID) throws SAXException {
        parsedId = pID == null ? NULL : pID;
        XMLDataObject.ERR.fine("Parsed to " + parsedId);
        stop();
    }

    public void endDTD() throws SAXException {
        stop();
    }

    public void startEntity(String name) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "startEntity {0}", name);
    }

    public void endEntity(String name) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "endEntity {0}", name);
    }

    public void startCDATA() throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "startCDATA");
    }

    public void endCDATA() throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "endCDATA");
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "comment len: {0}", length);
    }

    @Override
    public void error(final SAXParseException p1) throws org.xml.sax.SAXException {
        stop();
    }

    @Override
    public void fatalError(final SAXParseException p1) throws org.xml.sax.SAXException {
        stop();
    }

    @Override
    public void endDocument() throws SAXException {
        stop();
    }

    public void startElement(String uri, String lName, String qName, Attributes atts) throws SAXException {
        stop();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "characters len: {0}", length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "endElement: {0}", qName);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "endPrefix: {0}", prefix);
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "ignorableWhitespace: {0}", length);
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "notationDecl: {0}", name);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "processingInstruction: {0}", target);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "resolveEntity: {0}", publicId);
        return super.resolveEntity(publicId, systemId);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "skippedEntity: {0}", name);
    }

    @Override
    public void startDocument() throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "startDocument");
    }

    @Override
    public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "startElement: {0}", qName);
        stop();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "startPrefixMapping: {0}", prefix);
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "unparsedEntityDecl: {0}", name);
    }

    private void stop() throws SAXException {
        XMLDataObject.ERR.log(Level.FINEST, "stop");
        throw STOP;
    }

    public void fileFolderCreated(FileEvent fe) {
    }

    public void fileDataCreated(FileEvent fe) {
    }

    private void fileCreated(FileObject fo) {
    }

    public void fileChanged(FileEvent fe) {
        XMLDataObject realXML = getXml();
        if (realXML == null) {
            return;
        }
        
        if (realXML.getPrimaryFile().equals(fe.getFile())) {
            realXML.clearDocument();
            String prevId = parsedId;
            parsedId = null;
            XMLDataObject.ERR.fine("cleared parsedId");
            waitFinished(prevId);
        }
    }

    public void fileDeleted(FileEvent fe) {
    }

    public void fileRenamed(FileRenameEvent fe) {
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
    }

    public void resultChanged(LookupEvent lookupEvent) {
        XMLDataObject realXML = getXml();
        if (realXML == null) {
            return;
        }
        realXML.firePropertyChange(DataObject.PROP_COOKIE, null, null);
        Node n = realXML.getNodeDelegateOrNull();
        if (n instanceof XMLDataObject.XMLNode) {
            ((XMLDataObject.XMLNode) n).update();
        }
    }
    /** Avoid Internet connections */
    private static class EmptyEntityResolver implements EntityResolver {
        EmptyEntityResolver() {}
        public InputSource resolveEntity(String publicId, String systemID) {
            InputSource ret = new InputSource(new StringReader(""));  //??? we should tolerate file: and nbfs: // NOI18N
            ret.setSystemId("StringReader");  //NOI18N
            return ret;
        }
    }

    private XMLDataObject getXml() {
        return xml.get();
    }
}
