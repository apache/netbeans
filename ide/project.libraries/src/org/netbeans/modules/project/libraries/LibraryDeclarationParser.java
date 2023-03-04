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

package org.netbeans.modules.project.libraries;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The class reads XML documents according to specified DTD and
 * translates all related events into LibraryDeclarationHandler events.
 * <p>Usage sample:
 * <pre>
 *    LibraryDeclarationParser parser = new LibraryDeclarationParser(...);
 *    parser.parse(new InputSource("..."));
 * </pre>
 * <p><b>Warning:</b> the class is machine generated. DO NOT MODIFY</p>
 *
 */
public class LibraryDeclarationParser implements ContentHandler, EntityResolver {

    private static final String LIBRARY_DEF_1 = "-//NetBeans//DTD Library Declaration 1.0//EN"; //NOI18N
    private static final String LIBRARY_DTD_1 = "http://www.netbeans.org/dtds/library-declaration-1_0.dtd"; //NOI18N
    static final String LIBRARY_NS2 = "http://www.netbeans.org/ns/library-declaration/2";    //NOI18N
    static final String LIBRARY_NS3 = "http://www.netbeans.org/ns/library-declaration/3";    //NOI18N
    static final String VER_1 = "1.0";  //NOI18N
    static final String VER_2 = "2.0";  //NOI18N
    static final String VER_3 = "3.0";  //NOI18N
    private static final String LIBRARY = "library";    //NOI18N
    private static final String VERSION = "version";    //NOI18N
    private static final String VOLUME = "volume";  //NOI18N
    private static final String DESCRIPTION = "description";    //NOI18N
    private static final String TYPE = "type";      //NOI18N
    private static final String RESOURCE = "resource";   //NOI18N
    private static final String NAME = "name";  //NOI18N
    private static final String BUNDLE = "localizing-bundle";   //NOI18N
    private static final String DISPLAY_NAME = "display-name";  //NOI18N
    private static final String PROPERTIES = "properties";   //NOI18N
    private static final String PROPERTY = "property";  //NOI18N
    private static final String VALUE = "value";    //NOI18N

    private StringBuffer buffer;
    private final LibraryDeclarationConvertor parslet;
    private final LibraryDeclarationHandler handler;
    private Stack<Object[]> context;
    private String expectedNS;
    private final AtomicBoolean used = new AtomicBoolean();

    /**
     * Creates a parser instance.
     * @param handler handler interface implementation (never <code>null</code>
     * It is recommended that it could be able to resolve at least the DTD.@param parslet convertors implementation (never <code>null</code>
     *
     */
    public LibraryDeclarationParser(final LibraryDeclarationHandler handler, final LibraryDeclarationConvertor parslet) {
        this.parslet = parslet;
        this.handler = handler;
        buffer = new StringBuffer(111);
        context = new Stack<Object[]>();
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void setDocumentLocator(Locator locator) {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void startDocument() throws SAXException {
        handler.startDocument();
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void endDocument() throws SAXException {
        handler.endDocument();
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void startElement(String ns, String name, String qname, Attributes attrs) throws SAXException {
        dispatch(true);
        context.push(new Object[] {qname, ns, new AttributesImpl(attrs)});
        if (VOLUME.equals(qname)) {
            handler.start_volume(attrs);
        } else if (LIBRARY.equals(qname)) {
            expectedNS = handler.start_library(ns, attrs);
        } else if (PROPERTIES.equals(qname) && supportsProperties(ns)) {
            handler.start_properties(attrs);
        } else if (PROPERTY.equals(qname) && supportsProperties(ns)) {
            handler.start_property(attrs);
        }
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void endElement(String ns, String name, String qname) throws SAXException {
        dispatch(false);
        context.pop();
        if (VOLUME.equals(qname)) {
            handler.end_volume();
        } else if (LIBRARY.equals(qname)) {
            handler.end_library();
        } else if (PROPERTIES.equals(qname) && supportsProperties(ns)) {
            handler.end_properties();
        } else if (PROPERTY.equals(qname) && supportsProperties(ns)) {
            handler.end_property();
        }
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void characters(char[] chars, int start, int len) throws SAXException {
        buffer.append(chars, start, len);
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void ignorableWhitespace(char[] chars, int start, int len) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void processingInstruction(String target, String data) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void startPrefixMapping(final String prefix, final String uri) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void endPrefixMapping(final String prefix) throws SAXException {
    }
    
    /**
     * This SAX interface method is implemented by the parser.
     *
     */
    @Override
    public final void skippedEntity(String name) throws SAXException {
    }
    
    private void dispatch(final boolean fireOnlyIfMixed) throws SAXException {
        if (fireOnlyIfMixed && buffer.length() == 0) return; //skip it
        
        final Object[] ctx = context.peek();
        final String here = (String) ctx[0];
        final String ns = (String) ctx[1];
        Attributes attrs = (Attributes) ctx[2];
        if (expectedNS == null || !expectedNS.equals(ns)) {
            throw new SAXException("Invalid librray descriptor namespace"); // NOI18N
        }
        if (DESCRIPTION.equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_description (buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if (TYPE.equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_type(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if (RESOURCE.equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_resource(parslet.parseResource(buffer.length() == 0 ? null : buffer.toString()), attrs);
        } else if (NAME.equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_name(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if (BUNDLE.equals(here)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_localizingBundle(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if (DISPLAY_NAME.equals(here) && supportsDisplayName(ns)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_displayName(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else if (VALUE.equals(here) && supportsProperties(ns)) {
            if (fireOnlyIfMixed) throw new IllegalStateException("Unexpected characters() event! (Missing DTD?)");
            handler.handle_value(buffer.length() == 0 ? null : buffer.toString(), attrs);
        } else {
            //do not care
        }
        buffer.delete(0, buffer.length());
    }
    
    /**
     * The recognizer entry method taking an InputSource.
     * @param input InputSource to be parsed.
     * @throws java.io.IOException on I/O error.
     * @throws SAXException propagated exception thrown by a DocumentHandler.
     * @throws javax.xml.parsers.ParserConfigurationException a parser satisfining requested configuration can not be created.
     * @throws javax.xml.parsers.FactoryConfigurationRrror if the implementation can not be instantiated.
     *
     */
    public void parse(final InputSource input) throws SAXException, ParserConfigurationException, IOException {
        if (used.getAndSet(true)) {
            throw new IllegalStateException("The LibraryDeclarationParser was already used, create a new instance");  //NOI18N
        }
        try {
            final XMLReader parser = XMLUtil.createXMLReader(false, true);
            parser.setContentHandler(this);
            parser.setErrorHandler(getDefaultErrorHandler());
            parser.setEntityResolver(this);
            parser.parse(input);
        } finally {
            //Recover recognizer internal state from exceptions to be reusable
            if (!context.empty()) {
                context.clear();
            }
            if (buffer.length() > 0) {
                buffer.delete(0, buffer.length());
            }
            expectedNS = null;
        }
    }
    
    /**
     * Creates default error handler used by this parser.
     * @return org.xml.sax.ErrorHandler implementation
     *
     */
    protected ErrorHandler getDefaultErrorHandler() {
        return new ErrorHandler() {
            @Override
            public void error(SAXParseException ex) throws SAXException  {
                throw ex;
            }
            
            @Override
            public void fatalError(SAXParseException ex) throws SAXException {
                throw ex;
            }
            
            @Override
            public void warning(SAXParseException ex) throws SAXException {
                // ignore
            }
        };
        
    }
    
    /** Implementation of entity resolver. Points to the local DTD
     * for our public ID */
    @Override
    public InputSource resolveEntity (String publicId, String systemId)
    throws SAXException {
        if (LIBRARY_DEF_1.equals(publicId)) {
            InputStream is = new ByteArrayInputStream(new byte[0]);
            return new InputSource(is);
        }
        return null; // i.e. follow advice of systemID
    }

    static void writeLibraryDefinition (
            final @NonNull FileObject definitionFile,
            final @NonNull LibraryImplementation library,
            final @NonNull LibraryTypeProvider libraryTypeProvider) throws IOException {
        validateLibraryContent(library, libraryTypeProvider);
        final Document doc = LibrariesSupport.supportsDisplayName(library) ?
                (LibrariesSupport.supportsProperties(library) ?
                    createLibraryDefinition3(library, libraryTypeProvider) :
                    createLibraryDefinition2(library, libraryTypeProvider)) :
                createLibraryDefinition1(library, libraryTypeProvider);
        try {
            FileLockManager.getDefault().writeAction(
                definitionFile,
                new Callable<Void>() {
                    @Override
                    public Void call() throws IOException {
                        final FileLock lck = definitionFile.lock();
                        try (OutputStream os = definitionFile.getOutputStream(lck)) {
                            XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                        } finally {
                            lck.releaseLock();
                        }
                        return null;
                    }
                });
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates {@link URL}s in in the library.
     * @param library to check
     * @param libraryTypeProvider library meta definition
     * @throws IllegalArgumentException if the library contains {@link URL}
     * which cannot be converted to {@link URI}.
     */
    private static void validateLibraryContent(
            @NonNull final LibraryImplementation library,
            @NonNull final LibraryTypeProvider libraryTypeProvider) {
        for (String vtype : libraryTypeProvider.getSupportedVolumeTypes()) {
            LibrariesSupport.convertURLsToURIs(
                library.getContent(vtype),
                LibrariesSupport.ConversionMode.FAIL);
        }
    }

    private static Document createLibraryDefinition1(
            final @NonNull LibraryImplementation library,
            final @NonNull LibraryTypeProvider libraryTypeProvider) {
        final Document doc = XMLUtil.createDocument(LIBRARY, null,
                LIBRARY_DEF_1,
                LIBRARY_DTD_1);
        final Element libraryE = doc.getDocumentElement();
        libraryE.setAttribute(VERSION, VER_1); // NOI18N
        libraryE.appendChild(doc.createElement(NAME)).appendChild(doc.createTextNode(library.getName())); // NOI18N
        libraryE.appendChild(doc.createElement(TYPE)).appendChild(doc.createTextNode(library.getType())); // NOI18N
        String description = library.getDescription();
        if (description != null && description.length() > 0) {
            libraryE.appendChild(doc.createElement(DESCRIPTION)).appendChild(doc.createTextNode(description)); // NOI18N
        }
        String localizingBundle = library.getLocalizingBundle();
        if (localizingBundle != null && localizingBundle.length() > 0) {
            libraryE.appendChild(doc.createElement(BUNDLE)).appendChild(doc.createTextNode(localizingBundle)); // NOI18N
        }
        String displayname = LibrariesSupport.getDisplayName(library);
        if (displayname != null) {
            libraryE.appendChild(doc.createElement(DISPLAY_NAME)).appendChild(doc.createTextNode(displayname)); // NOI18N
        }
        for (String vtype : libraryTypeProvider.getSupportedVolumeTypes()) {
            Element volumeE = (Element) libraryE.appendChild(doc.createElement(VOLUME)); // NOI18N
            volumeE.appendChild(doc.createElement(TYPE)).appendChild(doc.createTextNode(vtype)); // NOI18N
            List<URL> volume = library.getContent(vtype);
            if (volume != null) {
                //If null -> broken library, repair it.
                for (URL url : volume) {
                    volumeE.appendChild(doc.createElement(RESOURCE)).appendChild(doc.createTextNode(url.toString())); // NOI18N
                }
            }
        }
        return doc;
    }

    private static Document createLibraryDefinition2(
            final @NonNull LibraryImplementation library,
            final @NonNull LibraryTypeProvider libraryTypeProvider) {
        final Document doc = XMLUtil.createDocument(LIBRARY, LIBRARY_NS2, null, null);
        final Element libraryE = doc.getDocumentElement();
        libraryE.setAttribute(VERSION, VER_2); // NOI18N
        libraryE.appendChild(doc.createElementNS(LIBRARY_NS2, NAME)).appendChild(doc.createTextNode(library.getName())); // NOI18N
        libraryE.appendChild(doc.createElementNS(LIBRARY_NS2, TYPE)).appendChild(doc.createTextNode(library.getType())); // NOI18N
        String description = library.getDescription();
        if (description != null && description.length() > 0) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS2, DESCRIPTION)).appendChild(doc.createTextNode(description)); // NOI18N
        }
        String localizingBundle = library.getLocalizingBundle();
        if (localizingBundle != null && localizingBundle.length() > 0) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS2, BUNDLE)).appendChild(doc.createTextNode(localizingBundle)); // NOI18N
        }
        String displayname = LibrariesSupport.getDisplayName(library);
        if (displayname != null) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS2, DISPLAY_NAME)).appendChild(doc.createTextNode(displayname)); // NOI18N
        }
        for (String vtype : libraryTypeProvider.getSupportedVolumeTypes()) {
            Element volumeE = (Element) libraryE.appendChild(doc.createElementNS(LIBRARY_NS2,VOLUME)); // NOI18N
            volumeE.appendChild(doc.createElementNS(LIBRARY_NS2, TYPE)).appendChild(doc.createTextNode(vtype)); // NOI18N
            List<URL> volume = library.getContent(vtype);
            if (volume != null) {
                for (URL url : volume) {
                    volumeE.appendChild(doc.createElementNS(LIBRARY_NS2, RESOURCE)).appendChild(doc.createTextNode(url.toString())); // NOI18N
                }
            }
        }
        return doc;
    }
    
    private static Document createLibraryDefinition3(
            final @NonNull LibraryImplementation library,
            final @NonNull LibraryTypeProvider libraryTypeProvider) {
        final Document doc = XMLUtil.createDocument(LIBRARY, LIBRARY_NS3, null, null);
        final Element libraryE = doc.getDocumentElement();
        libraryE.setAttribute(VERSION, VER_3); // NOI18N
        libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, NAME)).appendChild(doc.createTextNode(library.getName())); // NOI18N
        libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, TYPE)).appendChild(doc.createTextNode(library.getType())); // NOI18N
        String description = library.getDescription();
        if (description != null && description.length() > 0) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, DESCRIPTION)).appendChild(doc.createTextNode(description)); // NOI18N
        }
        String localizingBundle = library.getLocalizingBundle();
        if (localizingBundle != null && localizingBundle.length() > 0) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, BUNDLE)).appendChild(doc.createTextNode(localizingBundle)); // NOI18N
        }
        String displayname = LibrariesSupport.getDisplayName(library);
        if (displayname != null) {
            libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, DISPLAY_NAME)).appendChild(doc.createTextNode(displayname)); // NOI18N
        }
        for (String vtype : libraryTypeProvider.getSupportedVolumeTypes()) {
            Element volumeE = (Element) libraryE.appendChild(doc.createElementNS(LIBRARY_NS3,VOLUME)); // NOI18N
            volumeE.appendChild(doc.createElementNS(LIBRARY_NS3, TYPE)).appendChild(doc.createTextNode(vtype)); // NOI18N
            List<URL> volume = library.getContent(vtype);
            if (volume != null) {
                for (URL url : volume) {
                    volumeE.appendChild(doc.createElementNS(LIBRARY_NS3, RESOURCE)).appendChild(doc.createTextNode(url.toString())); // NOI18N
                }
            }
        }
        final Map<String,String> properties = LibrariesSupport.getProperties(library);
        assert properties != null : "LibraryImplementation: " + library + " returned null properties."; //NOI18N
        final Element propertiesNode = (Element) libraryE.appendChild(doc.createElementNS(LIBRARY_NS3, PROPERTIES));
        for (Map.Entry<String,String> e : properties.entrySet()) {
            final Element propertyNode = (Element)propertiesNode.appendChild(doc.createElementNS(LIBRARY_NS3, PROPERTY));
            propertyNode.appendChild(doc.createElementNS(LIBRARY_NS3, NAME)).appendChild(doc.createTextNode(e.getKey()));
            propertyNode.appendChild(doc.createElementNS(LIBRARY_NS3, VALUE)).appendChild(doc.createTextNode(e.getValue()));
        }
        return doc;
    }
    
    private static boolean supportsDisplayName(@NullAllowed final String ns) {
        return LIBRARY_NS2.equals(ns) || LIBRARY_NS3.equals(ns);
    }
    
    private static boolean supportsProperties(@NullAllowed final String ns) {
        return LIBRARY_NS3.equals(ns);
    }
}

