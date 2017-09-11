/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.xml.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;

import org.netbeans.api.xml.services.UserCatalog;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.NamespaceContext;
import org.xml.sax.InputSource;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Collection of static support methods.
 *
 * @author Petr Kuzel
 */
public class Util extends AbstractUtil {
    private static final Logger LOG = Logger.getLogger(Util.class.getName()); // NOI18N
    private static final String PREFIX_SCHEMA = "SCHEMA:"; // NOI18N
    private static final String PROTOCOL_FILE = "file:"; // NOI18N

    // last catalog directory
    private static File lastDirectory;
    
    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();
    public static final String NO_NAME_SPACE = "NO_NAME_SPACE"; //NOI18N

    /** Nobody can create instance of it, just me. */
    private Util () {
    }
    
    /**
     * Prompts user for a Schema file.
     * @param extensions a space separated list of file extensions
     * @return filename or null if operation was cancelled.
     */
    public static File selectSchemaFile(final String extensions) {
        JFileChooser chooser = new JFileChooser();

        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                if (f.isDirectory()) return true;
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return true;
                }
                return false;
            }
            public String getDescription() {
                return Util.THIS.getString(Util.class, "PROP_schema_mask"); // NOI18N
            }
        });

        if (lastDirectory != null) {
            chooser.setCurrentDirectory(lastDirectory);
        }

        chooser.setDialogTitle(Util.THIS.getString(Util.class, "PROP_schema_dialog_name"));
        while (chooser.showDialog(WindowManager.getDefault().getMainWindow(),
                               Util.THIS.getString(Util.class, "PROP_schema_select_button"))
               == JFileChooser.APPROVE_OPTION)
        {
            File f = chooser.getSelectedFile();
            lastDirectory = chooser.getCurrentDirectory();
            if (f != null && f.isFile()) {
                StringTokenizer token = new StringTokenizer(extensions, " ");  // NOI18N
                while (token.hasMoreElements()) {
                    if (f.getName().endsWith(token.nextToken())) return f;
                }
                     }

            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                Util.THIS.getString(Util.class, "MSG_inValidFile"), NotifyDescriptor.WARNING_MESSAGE));
        }
        return null;
    }    
    
    /**
     * Obtain all known DTD public IDs.
     */
    public static String[] getKnownDTDPublicIDs() {
        UserCatalog catalog = UserCatalog.getDefault();
        if (catalog != null) {
            Set idSet = new TreeSet();
            for (Iterator it = catalog.getPublicIDs(); it.hasNext(); ) {
                String next = (String) it.next();
                // exclude schema publicIDs
                String nextLowerCase = next.toLowerCase();
                if (!nextLowerCase.startsWith("schema:") && !nextLowerCase.endsWith(".xsd")) { // NOI18N
                    idSet.add(next);
                }
            }
            return (String[]) idSet.toArray(new String[idSet.size()]);
        } else {
            Util.THIS.debug("Note SourceResolver not found!");            // NOI18N
            return new String[0];
        }        
    }    
    
    public static String getDocumentType() {
        return "xsd";
    }
    
     public static Map getFiles2NSMappingInProj(File rootFile, String docType){
        List fileList = getFilesWithExtension(rootFile, docType, new ArrayList());
        Map result = new HashMap();
        String xpathQuery = "//xsd:schema/@targetNamespace";
        
        for(int i=0; i < fileList.size();i++){
            File file = (File)fileList.get(i);
        
            if(Thread.currentThread().isInterrupted())
                //if interrupted by the client dump the result and immediately return
                break;
            List targetNSList = null;
            try {
                targetNSList = runXPathQuery(file, xpathQuery);
                String targetNS = null;
                FileObject fobj = FileUtil.toFileObject(file);
                if(targetNSList.size() > 0){
                    //just take the first and ignore rest
                    targetNS = (String)targetNSList.get(0);
                } else{
                    targetNS = NO_NAME_SPACE;
                }
                if( (targetNS == NO_NAME_SPACE))
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
     
     public static FileObject toFileObject(InputSource src) {
        try {
            String sysId = src.getSystemId();
            return FileUtil.toFileObject(new File(new URI(sysId)));
        } catch (URISyntaxException ex) {
            LOG.log(Level.WARNING, "File URI malformed", ex);
            return null;
        }
     }
     
     public static Map<InputSource, String>  getCatalogSchemaNSMappings() {
         UserCatalog cat = UserCatalog.getDefault();
         Iterator it = cat.getPublicIDs();
         Map<InputSource, String> result = new HashMap<InputSource, String>();
         while (it.hasNext()) {
             String uri = (String)it.next();
             if (uri.startsWith(PREFIX_SCHEMA)) {
                uri = uri.substring(PREFIX_SCHEMA.length());
                try {
                    InputSource src = cat.getEntityResolver().resolveEntity(null, uri);
                    if (src == null) {
                        continue;
                    }
                    String sysId = src.getSystemId();
                    // TODO: should work for other protocols, too
                    if (!sysId.startsWith(PROTOCOL_FILE)) {
                        continue;
                    }
                    FileObject fo = toFileObject(src);
                    if (fo != null) {
                        result.put(src, uri);
                    }
                } catch (SAXException ex) {
                    LOG.log(Level.FINE, "Resolution failed", ex); // NOI18N
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Resolution failed", ex); // NOI18N
                }
             }
         }
         return result;
     }
     
     public static List getFilesWithExtension(File startFile, String fileExtension, List curList) {
        if(Thread.currentThread().isInterrupted())
            //if interrupted by the client dump the result and immediately return
            return curList;
        if(curList == null)
            curList = new ArrayList();
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
                for(int i=0; i < children.length; i++ ){
                    File child = (File) children[i];
                    getFilesWithExtension(child, fileExtension, curList);
                }
            }
        }
        return curList;
    }
    
    public static List runXPathQuery(File parsedFile, String xpathExpr) throws Exception{
        List result = new ArrayList();
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(getNamespaceContext());
        
        InputSource inputSource = new InputSource(new FileInputStream(parsedFile));
        NodeList nodes = (NodeList) xpath.evaluate(xpathExpr, inputSource, XPathConstants.NODESET);
        if((nodes != null) && (nodes.getLength() > 0)){
            for(int i=0; i<nodes.getLength();i++){
                org.w3c.dom.Node node = nodes.item(i);
                result.add(node.getNodeValue());
            }
        }
        return result;
    }
    
    public  static String getRelativePath(File file, File relativeTo) throws IOException {
        File origFile = file;
        File origRelativeTo = relativeTo;
        List filePathStack = new ArrayList();
        
        //are they on the same drive?
        String origFilePath = file.getAbsolutePath();
        String relativeFile = relativeTo.getAbsolutePath();
        
        StringTokenizer str = new StringTokenizer(origFilePath, ":");
        String drive = null, rdrive = null;
        if(str.hasMoreTokens())
            drive = str.nextToken();
        
        str = new StringTokenizer(relativeFile, ":");
        if(str.hasMoreTokens())
            rdrive = str.nextToken();
        if(drive != null && rdrive != null) {
            if(!drive.equals(rdrive)){
                return file.toURI().toString();
            }
        }
        
        List relativeToPathStack = new ArrayList();
        // build the path stack info to compare it afterwards
        file = file.getCanonicalFile();
        while (file!=null) {
            filePathStack.add(0, file);
            file = file.getParentFile();
        }
        relativeTo = relativeTo.getCanonicalFile();
        while (relativeTo!=null) {
            relativeToPathStack.add(0, relativeTo);
            relativeTo = relativeTo.getParentFile();
        }
        // compare as long it goes
        int count = 0;
        file = (File)filePathStack.get(count);
        relativeTo = (File)relativeToPathStack.get(count);
        while ( (count < filePathStack.size()-1) && (count <relativeToPathStack.size()-1) && file.equals(relativeTo)) {
            count++;
            file = (File)filePathStack.get(count);
            relativeTo = (File)relativeToPathStack.get(count);
        }
        if (file.equals(relativeTo)) count++;
        // up as far as necessary
        
        StringBuffer relString = new StringBuffer();
        for (int i = count; i < relativeToPathStack.size(); i++) {
            //hard code to front slash otherwise code completion doesnt work
             relString.append(".."+"/");
        }
        // now back down to the file
        for (int i = count; i <filePathStack.size()-1; i++) {
            //hard code to front slash otherwise code completion doesnt work
            relString.append(((File)filePathStack.get(i)).getName()+"/");
        }
            relString.append(((File)filePathStack.get(filePathStack.size()-1)).getName());
        // just to test
     //   File relFile = new File(origRelativeTo.getAbsolutePath()+File.separator+relString.toString());
     //   if (!relFile.getCanonicalFile().equals(origFile.getCanonicalFile())) {
      //      throw new IOException("Failed to find relative path.");
     //   }
        return relString.toString();
        }
    
    private static Map namespaces = new HashMap();
    private static Map prefixes = new HashMap();
    
    private static NamespaceContext getNamespaceContext() {
        //schema related
        namespaces.put("xsd","http://www.w3.org/2001/XMLSchema");  // NOI18N
        prefixes.put("http://www.w3.org/2001/XMLSchema", "xsd"); // NOI18N
        
       return new HashNamespaceResolver(namespaces, prefixes);
    }
           
    public static final class HashNamespaceResolver implements NamespaceContext {
        private Map prefixes; // namespace, prefix
        private Map namespaces;  // prefix, namespace
        
        public HashNamespaceResolver(Map nsTable) {
            namespaces = nsTable;
            prefixes = new HashMap();
            Set set = namespaces.entrySet();
            Iterator it = set.iterator();
            while(it.hasNext()){
            //for (Entry<String,String> e : namespaces.entrySet()) {
                Entry e = (Entry)it.next();
                prefixes.put(e.getValue(), e.getKey());
            }
        }
        
        public HashNamespaceResolver(Map namespaces, Map prefixes) {
            this.namespaces = namespaces;
            this.prefixes = prefixes;
        }
        
        public Iterator getPrefixes(String namespaceURI) {
            return Collections.singletonList(getPrefix(namespaceURI)).iterator();
        }
        
        public String getPrefix(String namespaceURI) {
            return (String)prefixes.get(namespaceURI);
        }
        
        public String getNamespaceURI(String prefix) {
            return (String)namespaces.get(prefix);
        }
    }
    
}
