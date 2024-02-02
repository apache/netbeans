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
package org.netbeans.tests.xml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.xml.tax.cookies.TreeDocumentCookie;
import org.netbeans.tax.TreeDocument;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeNode;
import org.netbeans.tax.io.XMLStringResult;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.XMLDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Lookup.Template;


/**
 * Provides the basic support for XML API tests.
 * @author mschovanek
 */
public abstract class AbstractTestUtil {
    protected static boolean DEBUG = true;
    
    public static final String CATALOG_PACKAGE    = "org.netbeans.modules.xml.catalog";
    public static final String CORE_PACKAGE       = "org.netbeans.modules.xml.core";
    public static final String TAX_PACKAGE        = "org.netbeans.modules.xml.tax";
    public static final String TEXT_PACKAGE       = "org.netbeans.modules.xml.text";
    public static final String TOOLS_PACKAGE      = "org.netbeans.modules.xml.tools";
    public static final String TREE_PACKAGE       = "org.netbeans.modules.xml.tree";
    
    //--------------------------------------------------------------------------
    //                         * * *  X M L * * *
    //--------------------------------------------------------------------------
    
    /**
     *  Converts the TreeNode to a string.
     */
    public static String nodeToString(TreeNode node) {
        try {
            return XMLStringResult.toString(node);
        } catch (TreeException te) {
            return null;
        }
    }
    
    /**
     *  Converts the node to a string.
     */
    public static String nodeToString(Object node) {
        return node.toString();
    }
    
    //--------------------------------------------------------------------------
    //                      * * *  S T R I N G S * * *
    //--------------------------------------------------------------------------
    
    /** Search-and-replace string matches to expression "begin.*end"
     * @param original the original string
     * @param begin the begin of substring to be find
     * @param end the end of substring to be find
     * @param replaceTo the substring to replace it with
     * @return a new string with 1st occurrence replaced
     */
    public static String replaceString(String original, String begin, String end, String replaceTo) {
        int bi = original.indexOf(begin);
        int ei = original.indexOf(end, bi) + end.length();
        if (bi < 0 || ei < 0) {
            return original;
        } else {
            return original.substring(0, bi) + replaceTo + original.substring(ei);
        }
    }
    
    /**
     * Removes first character occurence from string.
     */
    public static String removeChar(String str, char ch) {
        int index = str.indexOf(ch);
        
        if (index > -1) {
            StringBuffer sb = new StringBuffer(str).deleteCharAt(str.indexOf(ch));
            return new String(sb);
        } else {
            return str;
        }
    }
    
    /**
     * Joins elemets delimited by delim.
     */
    public static String joinElements(String[] elements, String delim) {
        if (elements == null) {
            return null;
        }
        
        String path = elements[0];
        for (int i = 1; i < elements.length; i++) {
            path += (delim + elements[i]);
        }
        return path;
    }
    
    /**
     * Returns last element.
     */
    public static final String lastElement(String string, String delim) {
        int index = string.lastIndexOf(delim);
        if (index == -1) {
            return string;
        } else {
            return string.substring(index + 1);
        }
    }
    
    //--------------------------------------------------------------------------
    //                      * * *  S T R I N G   L O C A L I Z A T I O N * * *
    //--------------------------------------------------------------------------
    
    /** Get localized string, removes '&' and cuts parameters like{0} from the end.
     * @param key key of localized value.
     * @return localized value.
     */
    public final String getStringTrimmed(String key) {
        return Bundle.getStringTrimmed(getBundel(), key);
    }
    
    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    public final String getString(String key) {
        return NbBundle.getMessage(this.getClass(), key);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    public final String getString(String key, Object param) {
        return NbBundle.getMessage(this.getClass(), key, param);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param1 argument to use when formating the message
     * @param param2 the second argument to use for formatting
     * @return localized value.
     */
    public final String getString(String key, Object param1, Object param2) {
        return NbBundle.getMessage(this.getClass(), key, param1, param2);
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    public final char getChar(String key) {
        return NbBundle.getMessage(this.getClass(), key).charAt(0);
    }
    
    private String getBundel() {
        return this.getClass().getPackage().getName() + ".Bundle";
    }
    
    //--------------------------------------------------------------------------
    //                  * * *  D A T A   O B J E C T S  * * *
    //--------------------------------------------------------------------------
    
    /** Converts DataObject to String.
     */
    public static String dataObjectToString(DataObject dataObject) throws IOException, BadLocationException {
        EditorCookie editorCookie = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        
        if (editorCookie != null) {
            StyledDocument document = editorCookie.openDocument();
            if (document != null) {
                return  document.getText(0, document.getLength());
            }
        }
        return null;
    }
    
    /** Saves DataObject
     */
    public static void saveDataObject(DataObject dataObject) throws IOException {
        SaveCookie cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
        if (cookie == null) throw new IllegalStateException("Cannot save document without SaveCookie.");
        cookie.save();
    }
    
    //--------------------------------------------------------------------------
    //                  * * *  F I L E S Y S T E M S  * * *
    //--------------------------------------------------------------------------
    
    /**
     * Mounts local directory
     */
    public static LocalFileSystem mountDirectory(File dir) throws PropertyVetoException, IOException {
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(dir);
        Repository rep = Repository.getDefault();
        FileSystem ffs = rep.findFileSystem(fs.getSystemName());
        if (ffs != null) {
            rep.removeFileSystem(ffs);
        }
        rep.addFileSystem(fs);
        return fs;
    }
    
    /**
     * Opens the XML Document with the given package, name and extension
     */
    public static TreeDocument openXMLDocument(String aPackage, String name, String ext) throws IOException {
        DataObject dao = findDataObject(aPackage, name, ext);
        
        if (dao == null) {
            throw new IOException(aPackage + "." + name + "." + ext + " data object not found.");
        }
        
        XMLDataObject xmlDataObject;
        if (XMLDataObject.class.isInstance(dao)) {
            xmlDataObject = (XMLDataObject) dao;
        } else {
            throw new IOException(aPackage + "." + name + "." + ext + " data object is not XMLDataObject.");
        }
        
        TreeDocumentCookie cookie = (TreeDocumentCookie) xmlDataObject.getCookie(TreeDocumentCookie.class);
        if (cookie == null) {
            throw new IOException("Missing TreeDocumentCookie at " + aPackage + "." + name + "." + ext);
        }
        
        TreeDocument document = (TreeDocument) cookie.getDocumentRoot();
        if (document == null) {
            throw new IOException("Ivalid XML data object" + aPackage + "." + name + "." + ext);
        }
        
        return document;
    }
    
    /**
     * Deletes FileObject.
     */
    public static void deleteFileObject(FileObject fo) throws IOException {
        DataObject dataObject = DataObject.find(fo);
        dataObject.getNodeDelegate().destroy();
    }
    
    /**
     * Finds DataFolder.
     */
    public static DataFolder findFolder(String aPackage) throws Exception {
        return (DataFolder) findDataObject(aPackage, null, null);
    }
    
    /**
     * Finds absolut path for FileObject.
     */
    public static String toAbsolutePath(FileObject fo) {
        return FileUtil.toFile(fo).getAbsolutePath();
    }
    
    /**
     * Finds the DataObject with the given package, name and extension
     */
    public static DataObject findDataObject(String aPackage, String name, String ext) throws DataObjectNotFoundException {
        FileObject fo = null;
        fo = Repository.getDefault().find(aPackage, name, ext);
        if (fo == null) {
            return null;
        } else {
            return DataObject.find(fo);
        }
    }
    
    /**
     * Finds the DataObject with the given package, name and extension
     */
    public static FileObject findFileObject(String aPackage, String name, String ext) {
        return Repository.getDefault().find(aPackage, name, ext);
    }
    
    /**
     * Finds the DataObject with the given name in test's 'data' folder. The name of a resource is
     * a "/"-separated path name that identifies the resource relatively to 'data' folder.<p />
     * <i>e.g. "sub_dir/data.xml"</i>
     */
    public DataObject findData(String name) throws DataObjectNotFoundException {
        //Repository.getDefault().
        String resName = this.getClass().getPackage().getName();
        resName = resName.replace('.', '/');
        resName += "/data/" + name;
        FileObject fo = ClassPath.getClassPath(null, ClassPath.EXECUTE).findResource(resName);
        if (fo == null) {
            if (DEBUG) {
                System.err.println("Cannot find FileObject: " + resName);
            }
            return null;
        } else {
            return DataObject.find(fo);
        }
    }
    
    /**
     * Finds the DataObject with the given name. The name of a resource is
     * a "/"-separated path name that identifies the resource or Nbfs URL.
     */
    public static DataObject findDataObject(String name) throws DataObjectNotFoundException {
        FileObject fo = findFileObject(name);
        if (fo == null) {
            if (DEBUG) {
                System.err.println("Cannot find FileObject: " + name);
            }
            return null;
        } else {
            return DataObject.find(fo);
        }
    }
    
    /**
     * Finds the FileObject with the given name. The name of a resource is
     * a "/"-separated path name that identifies the resource or Nbfs URL.
     */
    public static FileObject findFileObject(String name) {
        FileObject fo = null;
        if (name.startsWith("nbfs:")) {
            try {
                fo = URLMapper.findFileObject(new URL(name));
            } catch (MalformedURLException mue) {};
        } else {
            fo = Repository.getDefault().findResource(name);
        }
        return fo;
    }
    
    /**
     * Finds the template with the given name.
     */
    public static DataObject getTemplate(String tname) throws DataObjectNotFoundException {
        FileObject fileObject = Repository.getDefault().findResource("Templates/" + tname);
        if (fileObject == null) {
            throw new IllegalArgumentException("Cannot find template: " + tname);
        }
        return DataObject.find(fileObject);
    }
    
    /**
     * Creates new DataObject at the folder with given name from the template
     * with the given tname.
     */
    public static DataObject newFromTemplate(String tname, String folder, String name) throws IOException {
        DataObject dataObject = getTemplate(tname);
        DataFolder dataFolder = (DataFolder) findDataObject(folder);
        return dataObject.createFromTemplate(dataFolder, name);
    }
    
    /**
     * Removes the DataObject with the given name. The name of a resource is
     * a "/"-separated path name that identifies the resource or Nbfs URL.
     */
    public static boolean removeDocument(String name) throws IOException {
        DataObject  dataObject = findDataObject(name);
        if (dataObject != null) {
            dataObject.delete();
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Creates new Data Object
     */
    public static DataObject createDataObject(DataFolder folder, final String name, final String extension, final String content) throws IOException {
        final FileObject targetFolder = folder.getPrimaryFile();
        FileSystem filesystem = targetFolder.getFileSystem();
        
        final FileObject[] fileObject = new FileObject[1];
        AtomicAction fsAction = new AtomicAction() {
            public void run() throws IOException {
                FileObject fo = targetFolder.createData(name, extension);
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    OutputStream out = fo.getOutputStream(lock);
                    out = new BufferedOutputStream(out, 999);
                    Writer writer = new OutputStreamWriter(out, "UTF8");        // NOI18N
                    writer.write(content + '\n');  // NOI18N                    
                    writer.flush();
                    writer.close();
                    
                    // return DataObject
                    lock.releaseLock();
                    lock = null;
                    
                    fileObject[0] = fo;
                    
                } finally {
                    if (lock != null) lock.releaseLock();
                }
            }
        };
        
        filesystem.runAtomicAction(fsAction);
        return DataObject.find(fileObject[0]);
    }
    
    
    
    //--------------------------------------------------------------------------
    //                        * * *  O T H E R  * * *
    //--------------------------------------------------------------------------
    
    /**
     * Enbles <code>enable = true</code>  or disables <code>enable = false</code> the module.
     */
    public static void switchModule(String codeName, boolean enable) throws Exception {
        String statusFile = "Modules/" + codeName.replace('.', '-') + ".xml";
        ModuleInfo mi = getModuleInfo(codeName);
/*
        FileObject fo = findFileObject(statusFile);
        Document document = XMLUtil.parse(new InputSource(fo.getInputStream()), false, false, null, EntityCatalog.getDefault());
        //Document document = XMLUtil.parse(new InputSource(data.getPrimaryFile().getInputStream()), false, false, null, EntityCatalog.getDefault());
        NodeList list = document.getElementsByTagName("param");
 
        for (int i = 0; i < list.getLength(); i++) {
            Element ele = (Element) list.item(i);
            if (ele.getAttribute("name").equals("enabled")) {
                ele.getFirstChild().setNodeValue(enable ? "true" : "false");
                break;
            }
        }
 
        FileLock lock = fo.lock();
        OutputStream os = fo.getOutputStream(lock);
        XMLUtil.write(document, os, "UTF-8");
        lock.releaseLock();
        os.close();
 */
        
        // module is switched
        if (mi.isEnabled() == enable) {
            return;
        }
        
        DataObject data = findDataObject(statusFile);
        EditorCookie ec = (EditorCookie) data.getCookie(EditorCookie.class);
        StyledDocument doc = ec.openDocument();
        
        // Change parametr enabled
        String stag = "<param name=\"enabled\">";
        String etag = "</param>";
        String enabled = enable ? "true" : "false";
        String result;
        
        String str = doc.getText(0,doc.getLength());
        int sindex = str.indexOf(stag);
        int eindex = str.indexOf(etag, sindex);
        if (sindex > -1 && eindex > sindex) {
            result = str.substring(0, sindex + stag.length()) + enabled + str.substring(eindex);
            //System.err.println(result);
        } else {
            //throw new IllegalStateException("Invalid format of: " + statusFile + ", missing parametr 'enabled'");
            // Probably autoload module
            return;
        }
        
        // prepare synchronization and register listener
        final Waiter waiter = new Waiter();
        final PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("enabled")) {
                    waiter.notifyFinished();
                }
            }
        };
        mi.addPropertyChangeListener(pcl);
        
        // save document
        doc.remove(0,doc.getLength());
        doc.insertString(0,result,null);
        ec.saveDocument();
        
        // wait for enabled propety change and remove listener
        waiter.waitFinished();
        mi.removePropertyChangeListener(pcl);
    }
    
    /**
     * Switch on all XML modules and returns <code>true</code> if change state of any module else <code>false</code>.
     */
    public static boolean switchAllXMLModules(boolean enable) throws Exception {
        boolean result = false;
        Iterator it = Lookup.getDefault().lookup(new Template(ModuleInfo.class)).allInstances().iterator();
        
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            if (mi.getCodeNameBase().startsWith("org.netbeans.modules.xml.") && (mi.isEnabled() != enable)) {
                switchModule(mi.getCodeNameBase(), enable);
                result = true;
            }
        }
        return result;
    }
    
    /**
     * Returns module's info or <code>null</null>.
     */
    public static ModuleInfo getModuleInfo(String codeName) {
        Iterator it = Lookup.getDefault().lookup(new Template(ModuleInfo.class)).allInstances().iterator();
        
        while (it.hasNext()) {
            ModuleInfo mi = (ModuleInfo) it.next();
            //            if (mi.getCodeNameBase().equals(codeName) && mi.isEnabled()) {
            if (mi.getCodeNameBase().equals(codeName)) {
                return mi;
            }
        }
        return null;
    }
    
    /**
     * Returns <code>true</code> if module is enabled else <code>false</code>.
     */
    public static boolean isModuleEnabled(String codeName) {
        ModuleInfo mi = getModuleInfo(codeName);
        if (mi == null) {
            throw new IllegalArgumentException("Invalid codeName: " + codeName);
        }
        
        return mi.isEnabled();
    }
    
    protected static Random randomGenerator = new Random();
    
    /**
     * Generates random integer.
     */
    public static int randomInt(int n) {
        return randomGenerator.nextInt(n);
    }
    
    // ************************
    // * * *  C L A S E S * * *
    // ************************
    
    static class Waiter {
        private boolean finished = false;
        
        /** Restarts Synchronizer.
         */
        public void start() {
            finished = false;
        }
        
        /** Wait until the task is finished.
         */
        public void waitFinished() {
            if (!finished) {
                synchronized (this) {
                    while (!finished) {
                        try {
                            wait();
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
        }
        
        /** Notify all waiters that this task has finished.
         */
        public void notifyFinished() {
            synchronized (this) {
                finished = true;
                notifyAll();
            }
        }
    }
}
