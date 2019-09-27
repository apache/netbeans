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

package org.netbeans.spi.settings;

import java.io.IOException;
import java.util.Map;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** DOMConvertor extends the Convertor to allow to compose output of several
 *  convertors into one xml document. For more info see
 *  <a href='./doc-files/api.html#use-composed'>Composed Content</a>.
 *
 * @author  Jan Pokorsky
 * @since 1.1
 */
public abstract class DOMConvertor extends Convertor {
    /** an attribute containing public ID of DTD describing nested elements */
    private final static String ATTR_PUBLIC_ID = "dtd_public_id"; // NOI18N
    
    private final static String ATTR_ID = "id"; // NOI18N
    private final static String ATTR_IDREF = "idref"; // NOI18N
    /** element used to wrap output from non-DOMConvertor or to reference
     * already written object.
     * @see #ATTR_ID
     * @see #ATTR_IDREF
     */
    // Usage:
    // <domconvertor dtd_public_id='...'><[!CDATA[...]]></domconvertor>
    // or
    // ...
    // <foo id='1' dtd_public_id='...'>...</foo>
    // ...
    // <domconvertor idref='1'/>

    private final static String ELM_DELEGATE = "domconvertor"; // NOI18N
    
    private final static java.util.Map<Document, Map<Object,CacheRec>> refsCache = 
            new java.util.HashMap<Document, Map<Object,CacheRec>>();
    /** cache of contexts <Document, Lookup>*/
    private final static java.util.Map<Document, Lookup> ctxCache = new java.util.HashMap<Document, Lookup>();
    
    private String publicID;
    private String systemID;
    private String rootElement;
    
    /** Creat a DOMConvertor
     * @param publicID public ID of DOCTYPE
     * @param systemID system ID of DOCTYPE
     * @param rootElement qualified name of root element
     */
    protected DOMConvertor(String publicID, String systemID, String rootElement) {
        this.publicID = publicID;
        this.systemID = systemID;
        this.rootElement = rootElement;
        if (publicID == null) throw new NullPointerException("publicID"); // NOI18N
        if (systemID == null) throw new NullPointerException("systemID"); // NOI18N
        if (rootElement == null) throw new NullPointerException("rootElement"); // NOI18N
    }
    
    /** Read content from <code>r</code> and delegate to {@link #readElement}
     * passing parsed content as a root element of DOM document
     * @param r stream containing stored object
     * @return the read setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     * @since 1.1
     */
    public final Object read(java.io.Reader r) throws java.io.IOException, ClassNotFoundException {
        Document doc = null;
        try {
            InputSource is = new InputSource(r);
            doc = XMLUtil.parse(is, false, false, null, org.openide.xml.EntityCatalog.getDefault());
            setDocumentContext(doc, findContext(r));
            return readElement(doc.getDocumentElement());
        } catch (SAXException ex) {
            IOException ioe = new IOException(ex.getLocalizedMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            if (doc != null) {
                clearCashesForDocument(doc);
            }
        }
    }
    
    /** Write object described by DOM document filled by {@link #writeElement}
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception IOException if the object cannot be written
     * @since 1.1
     */
    public final void write(java.io.Writer w, Object inst) throws java.io.IOException {
        Document doc = null;
        try {
            doc = XMLUtil.createDocument(rootElement, null, publicID, systemID);
            setDocumentContext(doc, findContext(w));
            writeElement(doc, doc.getDocumentElement(), inst);
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream(1024);
            XMLUtil.write(doc, baos, "UTF-8"); // NOI18N
            w.write(baos.toString("UTF-8")); // NOI18N
        } catch (org.w3c.dom.DOMException ex) {
            IOException e = new IOException(ex.getLocalizedMessage());
            e.initCause(ex);
            throw e;
        } finally {
            if (doc != null) {
                clearCashesForDocument(doc);
            }
        }
    }
    
    /** Provide an object constructed from the element.
     * @param element represents a read object in a DOM document
     * @return an setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     * @since 1.1
     */
    protected abstract Object readElement(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException;
    
    /** Fill a DOM element describing <code>obj</code> with attributes or subelements.
     * @param doc a DOM document allowing to create elements describing passed object
     * @param element represents a written object in a DOM document
     * @param obj an object to convert
     * @exception IOException if the object cannot be written
     * @exception org.w3c.dom.DOMException if an element construction failed
     * @since 1.1
     */
    protected abstract void writeElement(
        org.w3c.dom.Document doc,
        org.w3c.dom.Element element,
        Object obj) throws java.io.IOException, org.w3c.dom.DOMException;

    /** delegate the read operation to a convertor referenced by <code>dtd_public_id</code>
     * @param element DOM element that should be read
     * @return an setting object
     * @exception IOException if the object cannot be read
     * @exception ClassNotFoundException if the object class cannot be resolved
     * @see #readElement
     * @since 1.1
     */
    protected final static Object delegateRead(org.w3c.dom.Element element) throws java.io.IOException, ClassNotFoundException {
        Object obj;
        
        // in case of a reference a cache of already read objects should
        // be consulted instead of delegating
        String idref = element.getAttribute(ATTR_IDREF);
        if (idref.length() != 0) {
            obj = getCache(element.getOwnerDocument(), idref.intern());
            if (obj != null) {
                return obj;
            } else {
                throw new IOException("broken reference: " + element + ", idref=" + idref); // NOI18N
            }
        }
        
        // lookup convertor
        String publicId = element.getAttribute(ATTR_PUBLIC_ID);
        Convertor c = ConvertorResolver.getDefault().getConvertor(publicId);
        if (c == null) throw new IOException("Convertor not found. publicId: " + publicId); // NOI18N
        
        // read
        if (element.getTagName().equals(ELM_DELEGATE)) {
            // read CDATA block
            org.w3c.dom.NodeList children = element.getChildNodes();
            String content = null;
            for (int i = 0, size = children.getLength(); i < size; i++) {
                org.w3c.dom.Node n = children.item(i);
                if (n.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) {
                    content = n.getNodeValue();
                    break;
                } else if (n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    // #62018: some serializers may not produce CDATA.
                    String text = n.getNodeValue().trim();
                    if (text.length() > 0) {
                        content = text;
                        break;
                    }
                }
            }
            
            if (content == null) {
                throw new IOException("Expected CDATA block under: " + // NOI18N
                                       element.getTagName());
            }
            obj = readFromString(c, content, findContext(element.getOwnerDocument()));
        } else if (c instanceof DOMConvertor) {
            DOMConvertor dc = (DOMConvertor) c;
            obj = dc.readElement(element);
        } else {
            throw new IOException(
                "Missing DOMConvertor for publicId: " + publicId); // NOI18N
        }
        
        // cache reference
        String id = element.getAttribute(ATTR_ID);
        if (id.length() != 0) {
            setCache(element.getOwnerDocument(), id, obj);
        }
        
        return obj;
    }
    
    /** delegate the write operation to a convertor able to write <code>obj<code>.
     * @param doc a DOM document allowing to create elements describing passed object
     * @param obj an object to convert
     * @return a DOM element representation
     * @exception IOException if the object cannot be written
     * @exception org.w3c.dom.DOMException if an element construction failed
     * @see #writeElement
     * @since 1.1
     */
    protected final static org.w3c.dom.Element delegateWrite(org.w3c.dom.Document doc, Object obj) throws java.io.IOException, org.w3c.dom.DOMException {
        // first lookup a cache of already written objects to prevent
        // storing of the same instance multiple times.
        CacheRec cache = setCache(doc, obj);
        if (cache.used) {
            return writeReference(doc, cache);
        }
        
        ConvertorResolver res = ConvertorResolver.getDefault();
        Class<?> clazz = obj.getClass();
        Convertor c = res.getConvertor(clazz);
        if (c == null) {
            throw new IOException("Convertor not found for object: " + obj); // NOI18N
        }
        
        org.w3c.dom.Element el;
        if (c instanceof DOMConvertor) {
            DOMConvertor dc = (DOMConvertor) c;
            el = doc.createElement(dc.rootElement);
            dc.writeElement(doc, el, obj);
            if (el.getAttribute(ATTR_PUBLIC_ID).length() == 0) {
                el.setAttribute(ATTR_PUBLIC_ID, res.getPublicID(clazz));
            }
        } else {
            // plain convertor -> wrap content to CDATA block
            el = doc.createElement(ELM_DELEGATE);
            el.setAttribute(ATTR_PUBLIC_ID, res.getPublicID(clazz));
            el.appendChild(doc.createCDATASection(writeToString(c, obj, findContext(doc))));
        }
        
        // bind cached object with original element
        cache.elm = el;
        return el;
    }
    
    /** get a context associated with the document <code>doc</code>. It can
     * contain various info like a file location of the read document etc.
     * @param doc a DOM document containing stored object
     * @return a context associated with the document
     * @since 1.2
     */
    protected static org.openide.util.Lookup findContext(org.w3c.dom.Document doc) {
        synchronized (ctxCache) {
            Lookup ctx = ctxCache.get(doc);
            return ctx == null? Lookup.EMPTY: ctx;
        }
    }
    
   
    // private impl //////////////////////////////////////////////////////////////
    
    /** remember context for document */
    private static void setDocumentContext(Document doc, Lookup ctx) {
        synchronized (ctxCache) {
            ctxCache.put(doc, ctx);
        }
    }
    
    /** write an object obj to String using Convertor.write() */
    private static String writeToString(Convertor c, Object obj, Lookup ctx) throws IOException {
        java.io.Writer caw = new java.io.CharArrayWriter(1024);
        java.io.Writer w = caw;
        
        FileObject fo = (FileObject) ctx.lookup(FileObject.class);
        if (fo != null) {
            w = org.netbeans.modules.settings.ContextProvider.createWriterContextProvider(caw, fo);
        }
        
        c.write(w, obj);
        w.close();
        return caw.toString();
    }
    
    /** read an object from String using Convertor.read() */
    private static Object readFromString(Convertor c, String s, Lookup ctx) throws IOException, ClassNotFoundException {
        java.io.Reader r = new java.io.StringReader(s);
        
        FileObject fo = (FileObject) ctx.lookup(FileObject.class);
        if (fo != null) {
            r = org.netbeans.modules.settings.ContextProvider.createReaderContextProvider(r, fo);
        }
        
        return c.read(r);
    }
    
    /** create an element referencing an already stored object */
    private static org.w3c.dom.Element writeReference(org.w3c.dom.Document doc, CacheRec cache) throws org.w3c.dom.DOMException {
        org.w3c.dom.Element el = doc.createElement(cache.elm.getTagName());
        el.setAttribute(ATTR_IDREF, (String) cache.value);
        cache.elm.setAttribute(ATTR_ID, (String) cache.value);
        return el;
    }
    
    /** remember an object obj being stored in document key and generate its ID
     * in scope of the document. Used during write operations.
     */
    private static CacheRec setCache(org.w3c.dom.Document key, Object obj) {
        synchronized (refsCache) {
            Map<Object, CacheRec> refs = refsCache.get(key);
            if (refs == null) {
                refs = new java.util.HashMap<Object, CacheRec>();
                refsCache.put(key, refs);
            }
            
            CacheRec cr = refs.get(obj);
            if (cr == null) {
                cr = new CacheRec();
                cr.key = obj;
                cr.value = "ID_" + String.valueOf(refs.size()); // NOI18N
                refs.put(obj, cr);
            }
            cr.used = cr.elm != null;
            return cr;
        }
    }
    
    /** remember an object obj and its ID being stored in document key to allow 
     * to resolve stored references in scope of the document.
     * Used during read operations.
     * @see #getCache
     */
    private static CacheRec setCache(org.w3c.dom.Document key, Object id, Object obj) {
        synchronized (refsCache) {
            Map<Object, CacheRec> refs = refsCache.get(key);
            if (refs == null) {
                refs = new java.util.HashMap<Object, CacheRec>();
                refsCache.put(key, refs);
            }
            
            CacheRec cr = refs.get(id);
            if (cr == null) {
                cr = new CacheRec();
                cr.key = id;
                cr.value = obj;
                refs.put(id, cr);
            }
            return cr;
        }
    }
    
    /** resolve a reference idref in scope of the document key.
     * Used during read operations.
     */
    private static Object getCache(org.w3c.dom.Document key, Object idref) {
        synchronized (refsCache) {
            java.util.Map refs = (java.util.Map) refsCache.get(key);
            if (refs == null) {
                return null;
            }
            
            CacheRec cr = (CacheRec) refs.get(idref);
            return cr.value;
        }
    }
    
    /** clears cashes per DOM document. Use when an object is converted. */
    private static void clearCashesForDocument(Document doc) {
        synchronized(refsCache) {
            refsCache.remove(doc);
        }
        synchronized(ctxCache) {
            ctxCache.remove(doc);
        }
    }
    
    private static class CacheRec {
        CacheRec() {}
        // key/value are paired as id/settings_object or settings_object/id
        // depends on performed operation (read/write)
        Object key;
        org.w3c.dom.Element elm;
        Object value;
        boolean used;
    }
}
