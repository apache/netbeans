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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogAttribute;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author girix
 */
public class CatalogFileWrapperDOMImpl  implements EntityResolver, CatalogFileWrapper, DocumentListener{
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Document catDoc = null;
    private Element catalog = null;
    private boolean isItMyOwnEvent = false;
    public static javax.swing.text.Document backendCatalogSwingDocument = null;
    private static final Logger logger = Logger.getLogger(CatalogFileWrapperDOMImpl.class.getName());
    
    private DocumentModel.State currentStateOfCatalog = null;
    
    public static boolean TEST_ENVIRONMENT = false;
    
    private FileObject backendCatalogFileObj = null;
    
    
    boolean rawFileSaveStrategy = false;
    
    SaveCookie saveCookie = null;
    
    private CatalogFileWrapperDOMImpl(FileObject backendCatalogFileObj, boolean rawFileSaveStrategy) throws IOException{
        this.rawFileSaveStrategy = rawFileSaveStrategy;
        this.backendCatalogFileObj = backendCatalogFileObj;
        assert(backendCatalogFileObj != null);
        //this.backendCatalogSwingDocument.addDocumentListener(this);
    }
    
    private synchronized void bootstrap(){
        try {
            this.backendCatalogSwingDocument = getDocument(backendCatalogFileObj);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        assert(backendCatalogFileObj != null);
        try {
            sync();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        if(currentStateOfCatalog == DocumentModel.State.NOT_WELL_FORMED)
            throw new IllegalStateException("Catalog File Not wellformed");
    }
    
    private synchronized void tearDown(){
        this.backendCatalogSwingDocument = null;
        catalog = null;
        catDoc = null;
    }
    
    static WeakHashMap<FileObject, CatalogFileWrapper> fo2wrapMap = new WeakHashMap<FileObject, CatalogFileWrapper>();
    
    public static synchronized CatalogFileWrapper getInstance(FileObject backendCatalogFileObj, boolean rawFileSaveStrategy) throws IOException{
        CatalogFileWrapper result = fo2wrapMap.get(backendCatalogFileObj);
        if(result == null){
            result = new CatalogFileWrapperDOMImpl(backendCatalogFileObj, rawFileSaveStrategy);
            if(result != null){
                fo2wrapMap.put(backendCatalogFileObj, result);
                return result;
            }
        }
        return result;
    }
    
    public static synchronized CatalogFileWrapper getInstance(FileObject backendCatalogFileObj) throws IOException{
        return getInstance(backendCatalogFileObj, false);
    }
    
    public synchronized void cleanInstance(){
        /*fo2wrapMap.remove(backendCatalogFileObj);
        try {
           backendCatalogFileObj.delete();
        } catch (IOException ex) {
        }*/
    }
    
    private javax.swing.text.Document getDocument(FileObject backendCatalogFileObj) throws IOException{
        logger.finer("ENTER FileObject "+backendCatalogFileObj.toString());
        DataObject dobj = DataObject.find(backendCatalogFileObj);
        EditorCookie thisDocumentEditorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
        StyledDocument sd = thisDocumentEditorCookie.openDocument();
        logger.finer("RETURN");
        return sd;
    }
    
    private boolean shouldParse(String docContent){
        if((docContent != null) && (docContent.toLowerCase().indexOf("catalog") != -1))
            return true;
        return false;
    }
    
    public List<CatalogEntry> getSystems() {
        return getEntriesByTagName(CatalogElement.system,
                CatalogAttribute.systemId, CatalogAttribute.uri);
    }
    
    public void setSystem(int index, CatalogEntry catEnt) throws IOException {
        setEntryInCatalogFile(index, catEnt,
                CatalogAttribute.systemId, CatalogAttribute.uri);
    }
    
    public void deleteSystem(int index) throws IOException {
        deleteEntryFromCatalogFile(index, CatalogElement.system);
    }
    
    public void addSystem(CatalogEntry catEnt) throws IOException {
        addEntryToCatFile(catEnt,
                CatalogAttribute.systemId, CatalogAttribute.uri);
    }
    
    
    public List<CatalogEntry> getDelegateSystems() {
        return getEntriesByTagName(CatalogElement.delegateSystem,
                CatalogAttribute.systemIdStartString, CatalogAttribute.catalog);
    }
    
    public void setDelegateSystem(int index, CatalogEntry catEnt) throws IOException {
        setEntryInCatalogFile(index, catEnt,
                CatalogAttribute.systemIdStartString, CatalogAttribute.catalog);
    }
    
    public void deleteDelegateSystem(int index) throws IOException {
        deleteEntryFromCatalogFile(index, CatalogElement.delegateSystem);
    }
    
    public void addDelegateSystem(CatalogEntry catEnt) throws IOException {
        addEntryToCatFile(catEnt,
                CatalogAttribute.systemIdStartString, CatalogAttribute.catalog);
    }
    
    
    public List<CatalogEntry> getRewriteSystems() {
        return getEntriesByTagName(CatalogElement.rewriteSystem,
                CatalogAttribute.systemIdStartString, CatalogAttribute.rewritePrefix);
    }
    
    public void setRewriteSystem(int index, CatalogEntry catEnt) throws IOException {
        setEntryInCatalogFile(index, catEnt,
                CatalogAttribute.systemIdStartString, CatalogAttribute.rewritePrefix);
    }
    
    public void deleteRewriteSystem(int index) throws IOException {
        deleteEntryFromCatalogFile(index, CatalogElement.rewriteSystem);
    }
    
    public void addRewriteSystem(CatalogEntry catEnt) throws IOException {
        addEntryToCatFile(catEnt,
                CatalogAttribute.systemIdStartString, CatalogAttribute.rewritePrefix);
    }
    
    private void initCatFile() throws IOException {
        logger.finer("ENTER");
        NodeList nl = catDoc.getElementsByTagName(CatalogElement.catalog.toString());
        if(nl.getLength() <= 0){
            //catalog element does not exists. Create
            catalog = catDoc.createElement(CatalogElement.catalog.toString());
            catalog.setAttribute(CatalogAttribute.prefer.toString(), "system");
            catalog.setAttribute(CatalogAttribute.xmlns.toString(),"urn:oasis:names:tc:entity:xmlns:xml:catalog");
            catDoc.appendChild(catalog);
            flush();
        } else {
            catalog = (Element) nl.item(0);
        }
        logger.finer("RETURN");
    }
    
    private List<CatalogEntry> getEntriesByTagName(CatalogElement tagName,
            CatalogAttribute mappingEntityKey, CatalogAttribute mappedEntityKey){
        bootstrap();
        Object [] obj = {
            tagName.toString(), mappingEntityKey.toString(),
            mappedEntityKey.toString()
        };
        logger.entering("CatalogModelWrapperDOMImpl","getEntriesByTagName", obj);
        
        NodeList nl = catalog.getElementsByTagName(tagName.toString());
        int len = nl.getLength();
        if(len < 1)
            return null;
        List<CatalogEntry> result = new ArrayList<CatalogEntry> (nl.getLength());
        for(int i=0; i<len;i++){
            String mappingEntity = "";
            String mappedEntity = "";
            Element elm = (Element) nl.item(i);
            mappingEntity = elm.getAttribute(mappingEntityKey.toString());
            mappedEntity = elm.getAttribute(mappedEntityKey.toString());
            String strArry[] = {mappingEntityKey.toString(),mappedEntityKey.toString()};
            HashMap<String,String> extraAttrs = getOtherAttributes(elm, strArry);
            CatalogEntry catEnt;
            if(extraAttrs != null)
                catEnt = new CatalogEntryImpl(tagName, mappingEntity, mappedEntity, extraAttrs);
            else
                catEnt = new CatalogEntryImpl(tagName, mappingEntity, mappedEntity);
            result.add(catEnt);
        }
        logger.exiting("CatalogModelWrapperDOMImpl","getEntriesByTagName", result);
        tearDown();
        return result;
    }
    
    private void addEntryToCatFile(CatalogEntry catEnt,
            CatalogAttribute mappingEntityKey, CatalogAttribute mappedEntityKey) throws IOException{
        bootstrap();
        /*Object obj[] = {
            catEnt.toString(), mappingEntityKey.toString(), mappedEntityKey.toString()
        };
        logger.entering("CatalogModelWrapperDOMImpl", "addEntryToCatFile", obj);*/
        
        Element elm = catDoc.createElement(catEnt.getEntryType().toString());
        if(mappedEntityKey != null)
            elm.setAttribute(mappedEntityKey.toString(), catEnt.getTarget());
        if(mappingEntityKey != null)
            elm.setAttribute(mappingEntityKey.toString(), catEnt.getSource());
        HashMap <String, String> extraAttribMap = catEnt.getExtraAttributeMap();
        if(extraAttribMap != null)
            addOtherAttributesToElement(elm, extraAttribMap);
        
        catalog.appendChild(elm);
        flush();
        logger.exiting(this.toString(), "addEntryToCatFile");
        tearDown();
    }
    
    private void setEntryInCatalogFile(int index, CatalogEntry catEnt,
            CatalogAttribute mappingEntityKey, CatalogAttribute mappedEntityKey) throws IOException{
        bootstrap();
        
        Object obj[] = {
            Integer.valueOf(index), catEnt.toString(), mappingEntityKey.toString(), mappedEntityKey.toString()
        };
        logger.entering("CatalogModelWrapperDOMImpl", "setEntryInCatalogFile", obj);
        
        Element elm = catDoc.createElement(catEnt.getEntryType().toString());
        elm.setAttribute(mappedEntityKey.toString(), catEnt.getTarget());
        elm.setAttribute(mappingEntityKey.toString(), catEnt.getSource());
        HashMap <String, String> extraAttribMap = catEnt.getExtraAttributeMap();
        if(extraAttribMap != null)
            addOtherAttributesToElement(elm, extraAttribMap);
        
        NodeList nl = catalog.getElementsByTagName(catEnt.getEntryType().toString());
        int len = nl.getLength();
        if((index >= len) || (index < 0))
            throw new IndexOutOfBoundsException("Error: Catalog entry does not exists");
        
        Node oldNode = nl.item(index);
        
        catalog.replaceChild(elm, oldNode);
        
        flush();
        logger.exiting(this.toString(), "setEntryInCatalogFile");
        tearDown();
    }
    
    private void deleteEntryFromCatalogFile(int index, CatalogElement tagName) throws IOException{
        logger.entering(this.toString(),"deleteEntryFromCatalogFile");
        bootstrap();
        NodeList nl = catalog.getElementsByTagName(tagName.toString());
        int len = nl.getLength();
        if((index >= len) || (index < 0))
            throw new IndexOutOfBoundsException("Error: Catalog entry does not exists");
        
        Node oldNode = nl.item(index);
        
        catalog.removeChild(oldNode);
        
        flush();
        logger.exiting(this.toString(), "deleteEntryFromCatalogFile");
        tearDown();
    }
    
    private void addOtherAttributesToElement(Element elm, HashMap<String,String> extraAttribMap) {
        if(extraAttribMap == null)
            return;
        Set<String> keys = extraAttribMap.keySet();
        if(keys == null)
            return;
        for(String key: keys){
            String value = (String) extraAttribMap.get(key);
            if(value != null){
                elm.setAttribute(key, value);
            }
        }
    }
    
    private HashMap<String,String> getOtherAttributes(Element elm, String[] strArry) {
        HashMap<String, String> result = new HashMap<String,String>();
        NamedNodeMap attrs = elm.getAttributes();
        for(int i = 0; i<attrs.getLength();i++){
            String key = attrs.item(i).getNodeName();
            boolean isMainAttrib = false;
            for(String str: strArry){
                if(str.equals(key)){
                    isMainAttrib = true;
                    break;
                }
            }
            if(!isMainAttrib){
                String value = attrs.item(i).getNodeValue();
                if((key != null) && (value != null))
                    result.put(key,value);
            }
        }
        if(result.isEmpty())
            return null;
        return result;
    }
    
    public synchronized void sync() throws IOException {
        logger.finer("ENTER");
        DocumentBuilderFactory dBuilderFact = DocumentBuilderFactory.newInstance();
        //dBuilderFact.setValidating(true);
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dBuilderFact.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            assignStateAndFirePropChangeEvent(DocumentModel.State.NOT_WELL_FORMED);
            throw new IOException(ex.getMessage());
        }
        String docContent = null;
        try {
            
            docContent = backendCatalogSwingDocument.getText(0, backendCatalogSwingDocument.getLength());
        } catch (BadLocationException ex) {
            assignStateAndFirePropChangeEvent(DocumentModel.State.NOT_WELL_FORMED);
            throw new IOException(ex.getMessage());
        }
        logger.finer("Trying to sync this data to model:"+docContent);
        if(shouldParse(docContent)){
            InputStream catIS = new ByteArrayInputStream(docContent.getBytes());
            try {
                //dBuilder.setEntityResolver(this);
                catDoc = dBuilder.parse(catIS);
                logger.finer("Just synced this data :"+docContent);
            }  catch (SAXException ex) {
                assignStateAndFirePropChangeEvent(DocumentModel.State.NOT_WELL_FORMED);
                throw new IOException(ex.getMessage());
            }
        } else{
            catDoc = dBuilder.newDocument();
        }
        if(catDoc == null){
            assignStateAndFirePropChangeEvent(DocumentModel.State.NOT_WELL_FORMED);
            throw new IllegalStateException("Catalog File Not wellformed");
        }
        assignStateAndFirePropChangeEvent(DocumentModel.State.VALID);
        initCatFile();
        logger.finer("RETURN");
        
    }
    
    private void assignStateAndFirePropChangeEvent(DocumentModel.State currentStateOfCatalog){
        DocumentModel.State prevState = this.currentStateOfCatalog;
        this.currentStateOfCatalog = currentStateOfCatalog;
        pcs.firePropertyChange("CatalogWraperObject",prevState, currentStateOfCatalog);
    }
    
    public boolean isValidState(){
        if(currentStateOfCatalog == DocumentModel.State.VALID)
            return true;
        return false;
    }
    
    public synchronized void flush() throws IOException {
        logger.finer("ENTER");
        isItMyOwnEvent = true;
        try {
            TransformerFactory trFactory = TransformerFactory.newInstance();
            Transformer transformer = trFactory.newTransformer();
            DOMSource domSource = new DOMSource(catDoc);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult(baos);
            transformer.setOutputProperty("indent","yes");
            transformer.transform(domSource, streamResult);
            String fileContent = new String(baos.toByteArray());
            String prevContent = backendCatalogSwingDocument.getText(0, backendCatalogSwingDocument.getLength());
            if(fileContent.equals(prevContent)){
                isItMyOwnEvent = false;
                return;
            }
            backendCatalogSwingDocument.remove(0, backendCatalogSwingDocument.getLength());
            backendCatalogSwingDocument.insertString(0, fileContent, null);
            logger.finer("Just Flushed this data :"+backendCatalogSwingDocument.getText(0,backendCatalogSwingDocument.getLength()));
            save(fileContent, prevContent);
        } catch(Exception ex){
            throw new IOException(ex.getMessage());
        } finally{
            isItMyOwnEvent = false;
        }
        logger.finer("RETURN");
    }
    
    public void insertUpdate(DocumentEvent e) {
        //showStackTrace();
        logger.entering("CatalogModelWrapperDOMImpl","insertUpdate",Boolean.valueOf(isItMyOwnEvent));
        if(!isItMyOwnEvent){
            try {
                sync();
            } catch (IOException ex) {
                //ignore this
                //ex.printStackTrace();
            }
            return;
        }
    }
    
    void showStackTrace(){
        try{
            throw new Exception();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public void removeUpdate(DocumentEvent e) {
        showStackTrace();
        logger.entering("CatalogModelWrapperDOMImpl","removeUpdate",Boolean.valueOf(isItMyOwnEvent));
        insertUpdate(e);
    }
    
    public void changedUpdate(DocumentEvent e) {
        logger.entering("CatalogModelWrapperDOMImpl","changedUpdate",Boolean.valueOf(isItMyOwnEvent));
        //insertUpdate(e);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    
    public InputStream getCatalogAsStream() throws IOException{
        try {
            String docContent = backendCatalogSwingDocument.getText(0, backendCatalogSwingDocument.getLength());
            InputStream bis = new ByteArrayInputStream(docContent.getBytes());
            logger.finer("In getCatalogAsStream gona return:"+docContent);
            return bis;
        } catch (BadLocationException ex) {
            throw new IOException(ex.getMessage());
        }
    }
    
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        logger.finest("ENTRY PublicID:"+publicId+" SystemID:"+systemId);
        InputSource isrc = new InputSource();
        InputStreamReader isr = new InputStreamReader(org.apache.xml.resolver.apps.resolver.class.getResourceAsStream("/org/apache/xml/resolver/etc/catalog.dtd"));
        isrc.setCharacterStream(isr);
        if((systemId != null) && systemId.equals("urn:oasis:names:tc:entity:xmlns:xml:catalog")){
            logger.finest("RETURN the DTD");
            return isrc;
        }
        if((publicId != null) && publicId.equals("urn:oasis:names:tc:entity:xmlns:xml:catalog")){
            logger.finest("RETURN the DTD");
            return isrc;
        }
        //parser asked for some other resource
        logger.finest("RETURN null");
        return null;
    }
    
    private void save(String fileContent, String previousFileContent) {
        //if(rawFileSaveStrategy)
        //if(!saveByRawStreamByFO(fileContent, previousFileContent))
        //saveByRawStreamByFile(fileContent, previousFileContent);
        //else
        if(TEST_ENVIRONMENT)
            saveByRawStreamByFile(fileContent, previousFileContent);
        else
            saveBySaveCookie();
        //saveByDocumentEditorCookie();
    }
    
    boolean saveBySaveCookie(){
        try {
            DataObject dobj = DataObject.find(backendCatalogFileObj);
            SaveCookie saveCookie = (SaveCookie) dobj.getCookie(SaveCookie.class);
            assert(saveCookie != null);
            saveCookie.save();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    boolean saveByRawStreamByFO(String fileContent, String previousFileContent){
        OutputStream os =null;
        FileLock lock = null;
        boolean noException = true;
        try {
            lock = backendCatalogFileObj.lock();
            os = backendCatalogFileObj.getOutputStream(lock);
            os.write(fileContent.getBytes());
            os.flush();
            os.close();
            lock.releaseLock();
            os = null;
        } catch (IOException ex) {
            ex.printStackTrace();
            noException = false;
        } finally {
            if(lock != null)
                lock.releaseLock();
            if(os != null){
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
            return noException;
        }
    }
    
    
    
    boolean saveByRawStreamByFile(String fileContent, String previousFileContent){
        OutputStream os =null;
        try {
            File catFile = FileUtil.toFile(backendCatalogFileObj);
            os = new FileOutputStream(catFile);
            os.write(fileContent.getBytes());
            os.flush();
            os.close();
            os = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if(os != null){
                try {
                    os.close();
                    return false;
                } catch (IOException ex) {
                }
            }
        }
        return true;
    }
    
    
    
    void saveByDocumentEditorCookie(){
        try {
            DataObject dobj = DataObject.find(backendCatalogFileObj);
            EditorCookie thisDocumentEditorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
            thisDocumentEditorCookie.saveDocument();
        } catch (IOException ex) {
        }
    }
    
    private boolean unsuccessfulSave(String fileContent, String previousFileContent) {
        try {
            if(backendCatalogSwingDocument.getText(0, backendCatalogSwingDocument.getLength()).length() != fileContent.length()){
                return true;
            }
        } catch (BadLocationException ex) {
        }
        return false;
    }
    
    public void close() {
        //thisDocumentEditorCookie.close();
        //backendCatalogSwingDocument.removeDocumentListener(this);
    }
    
    public DocumentModel.State getCatalogState() {
        bootstrap();
        tearDown();
        return currentStateOfCatalog;
    }
    
    protected void finalize() throws Throwable {
        try {
            DataObject dobj = DataObject.find(backendCatalogFileObj);
            EditorCookie thisDocumentEditorCookie = (EditorCookie)dobj.getCookie(EditorCookie.class);
            backendCatalogSwingDocument.removeDocumentListener(this);
            thisDocumentEditorCookie.close();
        } finally {
            super.finalize();
        }
    }
    
    public void addNextCatalog(CatalogEntry catEnt) throws IOException {
        addEntryToCatFile(catEnt, CatalogAttribute.catalog, null);
    }

    public void deleteNextCatalog(int index) throws IOException {
        deleteEntryFromCatalogFile(index, CatalogElement.nextCatalog);
    }

    public List<CatalogEntry> getNextCatalogs() {
        return getEntriesByTagName(CatalogElement.nextCatalog,
                CatalogAttribute.catalog, CatalogAttribute.catalog);
    }
    
}
