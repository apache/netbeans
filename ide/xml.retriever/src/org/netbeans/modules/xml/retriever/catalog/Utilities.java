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

package org.netbeans.modules.xml.retriever.catalog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.retriever.catalog.impl.*;
import org.netbeans.modules.xml.retriever.catalog.impl.XAMCatalogWriteModelImpl;
import org.netbeans.modules.xml.retriever.impl.Util;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.UserQuestionException;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author girix
 */
public class Utilities {
    public static final String NO_NAME_SPACE = "NO_NAME_SPACE"; //NOI18N
    private static final Logger logger = Logger.getLogger(Utilities.class.getName());
    
    public static URL appendURL(URL prefixURL, String suffixStr){
        String str = prefixURL.toString();
        String newurl = null;
        if(str.endsWith("/"))
            newurl = str+suffixStr;
        else
            newurl = str+"/"+suffixStr;
        try {
            return new URL(newurl);
        } catch (MalformedURLException ex) {
        }
        return null;
    }
    
    public static boolean localResourceExists(URL localURL){
        File locaFile = null;
        try {
            locaFile = new File(localURL.toURI());
            return locaFile.exists();
        } catch (URISyntaxException ex) {
        }
        return false;
        
    }
    
    public static File toFile(URL url){
        URI uri;
        try {
            uri = url.toURI();
        } catch (URISyntaxException ex) {
            return null;
        }
        return new File(uri);
    }
    
    public static void deleteRecursively(File aDir){
        if(aDir.isDirectory()){
            //get all children and delete them too
            File[] children = aDir.listFiles();
            for(File file: children)
                deleteRecursively(file);
            //System.out.printf("Deleting: %s (%s)\n", aDir.getAbsolutePath(), (aDir.isFile())?"File":"Dir");
            aDir.delete();
        }else{
            //normal file just delete
            //System.out.printf("Deleting: %s (%s)\n", aDir.getAbsolutePath(), (aDir.isFile())?"File":"Dir");
            aDir.delete();
        }
    }
    
    public static String normalizeURI(String uriref) {
        StringBuilder newRef = new StringBuilder();
        byte[] bytes;
        
        if (uriref == null) {
            return null;
        }
        
        bytes = uriref.getBytes(StandardCharsets.UTF_8);
        
        for (int count = 0; count < bytes.length; count++) {
            int ch = bytes[count] & 0xFF;
            
            if ((ch <= 0x20)    // ctrl
            || (ch > 0x7F)  // high ascii
            || (ch == 0x22) // "
            || (ch == 0x3C) // <
            || (ch == 0x3E) // >
            || (ch == 0x5C) // \
            || (ch == 0x5E) // ^
            || (ch == 0x60) // `
            || (ch == 0x7B) // {
            || (ch == 0x7C) // |
            || (ch == 0x7D) // }
            || (ch == 0x7F)) {
                newRef.append(encodedByte(ch));
            } else {
                newRef.append((char) bytes[count]);
            }
        }
        
        return newRef.toString();
    }
    
    public static String encodedByte(int b) {
        String hex = Integer.toHexString(b).toUpperCase();
        if (hex.length() < 2) {
            return "%0" + hex;
        } else {
            return "%" + hex;
        }
    }
    
    /**
     * This method will work ONLY for the files that are there in the local file system and
     * that have common URI up to server level.
     * Result is a relative URI of the slave file WRT the master file
     */
    public static String relativize(URI master, URI slave) {
        String masterStr = master.toString();
        String slaveStr = slave.toString();
        StringTokenizer masterTok = new StringTokenizer(masterStr, "/");
        StringTokenizer slaveTok = new StringTokenizer(slaveStr, "/");
        String masterLast = null;
        String slaveLast = null;
        int iteration = -1;
        while(true){
            iteration++;
            if(masterTok.hasMoreTokens() && slaveTok.hasMoreTokens()){
                masterLast = masterTok.nextToken();
                slaveLast = slaveTok.nextToken();
                if(masterLast.equals(slaveLast))
                    continue;
                else
                    break;
            }
            break;
        }
        //if even the server part is changing then just return the slave totally as absoulte URI
        if(iteration < 2)
            return slave.toString();
        //count number of ../'es
        int dirCount = masterTok.countTokens();
        if ( masterStr.endsWith("/") && dirCount != 0 ){
            dirCount++;
        }
        //generate prefix
        String pathPrefix = "";
        StringBuffer pathPrefixBuff = new StringBuffer("");
        for(int i=0; i<dirCount;i++){
            pathPrefixBuff.append("../");
        }
        pathPrefix = pathPrefixBuff.toString();
        //collect rest of slave tokens
        StringBuilder slaveResult = masterLast.equals(slaveLast)?
            new StringBuilder():new StringBuilder(slaveLast);
        while(slaveTok.hasMoreTokens()) {
            if(slaveResult.length() != 0)
                slaveResult.append('/');
            slaveResult.append(slaveTok.nextToken());
        }
        //add prefix to slaveStr
        return pathPrefix + slaveResult.toString();
    }
        
    public static List<FileObject> getFilesOfNSInProj(Project prj, DocumentTypesEnum docType, String nameSpace, List<String> sourceGroupTypeList){
        List<FileObject> result = new ArrayList<FileObject>();
        Map<FileObject, String> fobj2nsMap = getFiles2NSMappingInProj(prj, docType, sourceGroupTypeList);
        Set<FileObject> fileList = fobj2nsMap.keySet();
        for(FileObject fobj: fileList){
            if(Thread.currentThread().isInterrupted())
                //if interrupted by the client dump the result and immediately return
                break;
            if(fobj2nsMap.get(fobj).equals(nameSpace))
                result.add(fobj);
        }
        return result;
    }
    
    public static List<FileObject> getFilesOfNoNSInProj(Project prj, DocumentTypesEnum docType, List<String> sourceGroupTypeList){
        return getFilesOfNSInProj(prj, docType, NO_NAME_SPACE, sourceGroupTypeList);
    }
    
    
    public static Map<FileObject, String> getFiles2NSMappingInProj(Project prj, DocumentTypesEnum docType, List<String> sourceGroupTypeList){
        Map<FileObject, String> result = new HashMap<FileObject, String>();
        List<FileObject> rootList = getAllSourceRoots(prj, sourceGroupTypeList);
        for(FileObject fob : rootList){
            result.putAll(getFiles2NSMappingInProj(FileUtil.toFile(fob), docType));
        }
        return result;
    }
    
    private static List<FileObject> getAllSourceRoots(Project prj, List<String> sourceGroupTypeList){
        List<FileObject> result = new ArrayList<FileObject>();
        for(String type: sourceGroupTypeList){
            SourceGroup[] srcGrps = ProjectUtils.getSources(prj).getSourceGroups(type);
            if(srcGrps != null){
                for(SourceGroup srcGrp : srcGrps)
                    result.add(srcGrp.getRootFolder());
            }
        }
        return result;
    }
    
    public static Map<FileObject, String> getFiles2NSMappingInProj(File rootFile, DocumentTypesEnum docType){
        List<File> fileList = getFilesWithExtension(rootFile, docType.toString(), new ArrayList<File>());
        Map<FileObject, String> result = new HashMap<FileObject, String>();
        String xpathQuery = null;
        if(docType == docType.schema)
            xpathQuery = "//xsd:schema/@targetNamespace";
        else
            xpathQuery = "//wsdl:definitions/@targetNamespace";
        
        for(File file: fileList){
            if(Thread.currentThread().isInterrupted())
                //if interrupted by the client dump the result and immediately return
                break;
            List<String> targetNSList = null;
            try {
                targetNSList = runXPathQuery(file, xpathQuery);
                String targetNS = null;
                FileObject fobj = FileUtil.toFileObject(file);
                if(targetNSList.size() > 0){
                    //just take the first and ignore rest
                    targetNS = targetNSList.get(0);
                } else{
                    targetNS = NO_NAME_SPACE;
                }
                if((docType == docType.wsdl) && (targetNS == NO_NAME_SPACE))
                    //this is wsdl and it must have NS so ignore this file
                    continue;
                result.put(fobj, targetNS);
            } catch (Exception ex) {
                //ex.printStackTrace();
                //ignore this route
            }
        }
        return result;
    }
    
    
    private static boolean firstRoot = true;
    public static List<File> getFilesWithExtension(File startFile, String fileExtension, List<File> curList) {
        //if interrupted by the client dump the result and immediately return
        if(Thread.currentThread().isInterrupted()) {
            firstRoot = true; //reset before returning
            return curList;
        }
        if(curList == null)
            curList = new ArrayList<File>();
        if(startFile.isFile()){
            int index = startFile.getName().lastIndexOf(".");
            if(index != -1){
                String extn = startFile.getName().substring(index+1);
                if((extn != null) && (extn.equalsIgnoreCase(fileExtension)))
                    curList.add(startFile);
            }
        }
        if(startFile.isDirectory()){
            File[] children = startFile.listFiles();
            if(children != null){
                for(File child: children){
                    //exclude "build" dir since that is generated one.
                    //cannot think of a better solution.
                    if(firstRoot && child.getName().endsWith("build")) //NOI18N
                        continue;
                    firstRoot = false;
                    getFilesWithExtension(child, fileExtension, curList);
                }
            }
        }
        //reset before returning.
        firstRoot = true;
        return curList;
    }
    
    public static List<String> runXPathQuery(File parsedFile, String xpathExpr) throws Exception{
        List<String> result = new ArrayList<String>();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext());
        
        InputSource inputSource = new InputSource(new FileInputStream(parsedFile));
        NodeList nodes = (NodeList) xpath.evaluate(xpathExpr, inputSource, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                Node node = nodes.item(i);
                result.add(node.getNodeValue());
            }
        }
        return result;
    }
    
    private static Map<String, String> namespaces = new HashMap<String,String>();
    private static Map<String, String> prefixes = new HashMap<String,String>();
    
    private static NamespaceContext getNamespaceContext() {
        //schema related
        namespaces.put("xsd","http://www.w3.org/2001/XMLSchema");
        prefixes.put("http://www.w3.org/2001/XMLSchema", "xsd");
        
        //wsdl related
        namespaces.put("wsdl", "http://schemas.xmlsoap.org/wsdl/");
        prefixes.put("http://schemas.xmlsoap.org/wsdl/", "wsdl");
        return new HashNamespaceResolver(namespaces, prefixes);
    }
    
    public static int countPushdownFolders(URI master, URI slave) {
        String masterStr = master.toString();
        String slaveStr = slave.toString();
        StringTokenizer masterTok = new StringTokenizer(masterStr, "/");
        StringTokenizer slaveTok = new StringTokenizer(slaveStr, "/");
        String masterLast = null;
        String slaveLast = null;
        while(true){
            if(masterTok.hasMoreTokens() && slaveTok.hasMoreTokens()){
                masterLast = masterTok.nextToken();
                slaveLast = slaveTok.nextToken();
                if(masterLast.equals(slaveLast))
                    continue;
                else
                    break;
            }
            break;
        }
        //count number of ../'es
        return slaveTok.countTokens()+1;
    }
    
    public static File downloadURLAndSave(URL downloadURL, File saveFile) throws IOException{
        return downloadURLUsingProxyAndSave(downloadURL, null, saveFile);
    }
    
    
    public static File downloadURLUsingProxyAndSave(URL downloadURL, Proxy proxy, File saveFile) throws IOException{
        IOException expn = null;
        URLConnection ucn = null;
        
        if(Thread.currentThread().isInterrupted())
            return null;
        if(proxy != null)
            ucn = downloadURL.openConnection(proxy);
        else
            ucn = downloadURL.openConnection();
        
        if(Thread.currentThread().isInterrupted())
            return null;
        ucn.connect();
        
        byte buffer[] = new byte[1024];
        BufferedInputStream bis = new BufferedInputStream(ucn.getInputStream());
        saveFile.getParentFile().mkdirs();
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(saveFile));
        } catch (FileNotFoundException ex) {
            bis.close();
            throw ex;
        }
        
        int len = 1024;
        while((len = bis.read(buffer, 0, 1024)) > 0) {
            try {
                if(Thread.currentThread().isInterrupted())
                    break;
                Thread.sleep(10);
            } catch (InterruptedException ex) {}
            try{
                bos.write(buffer, 0, len);
            } catch (IOException e){
                expn = e;
                break;
            }
        }
        try {
            bis.close();
        } catch (IOException ex) {
            //cant do much: ignore
        }
        try {
            bos.close();
        } catch (IOException ex) {
            //cant do much: ignore
        }
        if(expn != null)
            throw expn;
        return saveFile;
    }
    
    public static InputStream getInputStreamOfURL(URL downloadURL, Proxy proxy) throws IOException {
        
        URLConnection ucn = null;
        
        if(Thread.currentThread().isInterrupted())
            return null;
        if(proxy != null)
            ucn = downloadURL.openConnection(proxy);
        else
            ucn = downloadURL.openConnection();
        
        if(Thread.currentThread().isInterrupted())
            return null;
        ucn.connect();
        
        return ucn.getInputStream();
        
    }
    
    public static Document getDocument(FileObject modelSourceFileObject){
        Document result = null;
        try {
            DataObject dObject = DataObject.find(modelSourceFileObject);
            EditorCookie ec = (EditorCookie)dObject.getCookie(EditorCookie.class);
            Document doc = ec.openDocument();
            if(doc instanceof BaseDocument)
                return doc;            
            result = new org.netbeans.editor.BaseDocument(true, modelSourceFileObject.getMIMEType());
            String str = doc.getText(0, doc.getLength());
            result.insertString(0,str,null);            
        } catch (Exception dObjEx) {
            return null;
        }
        return result;
    }
    
    private static Document _getDocument(DataObject modelSourceDataObject)
    throws IOException {
        //
        // throw new IOException("Invalid FileObject"); // for testing issue #184306
        //
        Document result = null;
        if (modelSourceDataObject != null && modelSourceDataObject.isValid()) {
            EditorCookie ec = modelSourceDataObject.getCookie(EditorCookie.class);
            assert ec != null : "Data object "+modelSourceDataObject.getPrimaryFile().getPath()+" has no editor cookies.";
            if (ec != null) {
                Document doc = null;
                try {
                    doc = ec.openDocument();
                } catch (UserQuestionException uce) {
                    // this exception is thrown if the document is to large
                    // lets just confirm that it is ok
                    uce.confirmed();
                    doc = ec.openDocument();
                }
                assert(doc instanceof BaseDocument);
                result = doc;
            }
        }
        return result;
    }
    
    /**
     * This method must be overridden by the Unit testcase to return a special
     * Document object for a FileObject.
     */
    protected static Document _getDocument(FileObject modelSourceFileObject)
    throws DataObjectNotFoundException, IOException {
        DataObject dObject = DataObject.find(modelSourceFileObject);
        return _getDocument(dObject);
    }
    
    private static CatalogWriteModel testCatalogModel = null;
    public static CatalogWriteModel getTestCatalogWriteModel() throws IOException{
        if(testCatalogModel == null){
            CatalogWriteModel cm = null;
            try {
                cm = new XAMCatalogWriteModelImpl(new File(System.getProperty("java.io.tmpdir")));
            } catch (CatalogModelException ex) {
                return null;
            }
            File file = FileUtil.toFile(cm.getCatalogFileObject());
            file.deleteOnExit();
            return cm;
        }
        return testCatalogModel;
    }
    
    
    public static FileObject getProjectCatalogFileObject(Project prj) throws IOException {
        return Util.getProjectCatalogFileObject(prj, true);
    }
    
    /**
     * Choose the suitable catalog file for this file object considering XMLCatalogProvider
     * and return the correct choice
     **/
    /*public static FileObject getCatalogFile(FileObject fo) throws IOException{
        if(fo == null)
            return null;
        Project foprj = FileOwnerQuery.getOwner(fo);
        if(foprj == null)
            return null;
     
        XMLCatalogProvider xcp = (XMLCatalogProvider) foprj.getLookup().lookup(XMLCatalogProvider.class);
        if(xcp == null)
            return getProjectCatalogFileObject(foprj);
     
        //look for per file catalog
        URI catURI = xcp.getCatalog(fo);
        if(catURI == null)
            return getProjectCatalogFileObject(foprj);
     
        catURI = FileUtil.toFile(foprj.getProjectDirectory()).toURI().resolve(catURI);
        File catFile = new File(catURI);
        if(!catFile.isFile()){
            catFile.createNewFile();
        }
        FileObject result = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
     
        if(result == null)
            return getProjectCatalogFileObject(foprj);
     
        return result;
    }*/
    
    public static FileObject getFileObject(ModelSource ms){
        return (FileObject) ms.getLookup().lookup(FileObject.class);
    }
    
    public static CatalogModel getCatalogModel(ModelSource ms) throws CatalogModelException{
        return CatalogModelFactory.getDefault().getCatalogModel(ms);
    }
    
    public static ModelSource getModelSource(FileObject bindingHandlerFO, boolean editable){
        try {
            return createModelSource(bindingHandlerFO, editable);
        } catch (CatalogModelException ex) {
            return null;
        }
    }
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    public static ModelSource createModelSource(FileObject thisFileObj,
            boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final CatalogModel catalogModel = createCatalogModel(thisFileObj);
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
        Lookup proxyLookup = Lookups.proxy(
                new Lookup.Provider() {
            public Lookup getLookup() {
                Document document = null;
                try {
                    document = _getDocument(dobj);
                    if (document != null) {
                        return Lookups.fixed(new Object[] {
                            dobj.getPrimaryFile(),
                            document,
                            dobj,
                            DataObjectAdapters.source(dobj),
                            catalogModel
                        });
                    } else {
                        return Lookups.fixed(new Object[] {
                            dobj.getPrimaryFile(),
                            dobj,
                            catalogModel
                        });
                    }
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, ioe.getMessage());
                    return Lookups.fixed(new Object[] {
                        dobj,
                        catalogModel
                    });
                }
            }
        }
        );
        
        return new ModelSource(proxyLookup, editable);
    }    
    
    /**
     * This method could be overridden by the Unit testcase to return a special
     * ModelSource object for a FileObject with custom impl of classes added to the lookup.
     * This is optional if both getDocument(FO) and createCatalogModel(FO) are overridden.
     */
    /*public static ModelSource createModelSource(final FileObject thisFileObj,
        boolean editable) throws CatalogModelException{
        assert thisFileObj != null : "Null file object.";
        final CatalogModel catalogModel = createCatalogModel(thisFileObj);
        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
        } catch (DataObjectNotFoundException ex) {
            throw new CatalogModelException(ex);
        }
     
        ProxyLookup myLookup = new ProxyLookup() {
            protected void beforeLookup(Lookup.Template template) {
                if (Document.class.isAssignableFrom(template.getType())) {
                    Lookup l = Lookup.EMPTY;
                    try {
                        Document d = _getDocument(thisFileObj);
                        l = Lookups.singleton(d);
                    } catch (DataObjectNotFoundException ex) {
                        getLogger().log(Level.SEVERE, "Can't load data object from "+thisFileObj.getPath());
                    } catch (IOException ex) {
                        getLogger().log(Level.SEVERE, ex.getMessage());
                    }
     
                    setLookups(new Lookup[]{l});
                } else if (Source.class.isAssignableFrom(template.getType())) {
                    setLookups(new Lookup[]{Lookups.singleton(DataObjectAdapters.source(dobj))});
                } else {
                    Lookup l = Lookups.fixed(new Object[] {
                        thisFileObj,
                        dobj,
                        catalogModel
                    });
                    setLookups(new Lookup[]{l});
                }
            }
        };
     
        return new ModelSource(myLookup, editable);
    }*/
    
    public static CatalogModel createCatalogModel(FileObject fo) throws CatalogModelException{
        return new CatalogModelFactoryImpl().getCatalogModel(fo);
    }
        
    public static final String DEFAULT_PRIVATE_CATALOG_URI_STR = "private/cache/retriever/catalog.xml";
    public static final String DEFAULT_PRIVATE_CAHCE_URI_STR = "private/cache/retriever";
    
    public static final String PRIVATE_CATALOG_URI_STR = "retriever/catalog.xml";
    public static final String PRIVATE_CAHCE_URI_STR = "retriever";
    
    
    
    public static boolean retrieveAndCache(URI locationURI, FileObject sourceFileObject) {
        return Util.retrieveAndCache(locationURI, sourceFileObject, true, true);
    }
    
    public enum DocumentTypesEnum {
        schema,
        wsdl;
        
        public String toString(){
            if(name().equals("schema"))
                return "xsd";
            else
                return name();
        }
    }
    
    
    public static final class HashNamespaceResolver implements NamespaceContext {
        private Map<String, String> prefixes; // namespace, prefix
        private Map<String, String> namespaces;  // prefix, namespace
        
        public HashNamespaceResolver(Map<String,String> nsTable) {
            namespaces = nsTable;
            prefixes = new HashMap<String,String>();
            for (Entry<String,String> e : namespaces.entrySet()) {
                prefixes.put(e.getValue(), e.getKey());
            }
        }
        
        public HashNamespaceResolver(Map<String,String> namespaces, Map<String,String> prefixes) {
            this.namespaces = namespaces;
            this.prefixes = prefixes;
        }
        
        public Iterator getPrefixes(String namespaceURI) {
            return Collections.singletonList(getPrefix(namespaceURI)).iterator();
        }
        
        public String getPrefix(String namespaceURI) {
            return prefixes.get(namespaceURI);
        }
        
        public String getNamespaceURI(String prefix) {
            return namespaces.get(prefix);
        }
    }
}
