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

/*
 * RetrieverTask.java
 *
 * Created on January 9, 2006, 6:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author girix
 */
public class RetrieverTask {
    
    private static final Logger LOG = Logger.getLogger(RetrieverTask.class.getName());
    
    private File saveFile;
    
    private String baseAddress;
    
    private String sourceToBeGot;
    
    private RetrieveEntry rent;
    
    private RetrieverEngineImpl retEngine = null;
    
    /** Creates a new instance of RetrieverTask */
    public RetrieverTask(RetrieveEntry rent, RetrieverEngine retEngine){
        this.retEngine = (RetrieverEngineImpl)retEngine;
        this.sourceToBeGot = rent.getCurrentAddress();
        this.baseAddress = rent.getBaseAddress();
        this.saveFile = rent.getSaveFile();
        this.rent = rent;
    }
    
    
    public HashMap<String,File> goGetIt() throws IOException, URISyntaxException{
        synchronized(RetrieverTask.class){
            if(( saveFile != null ) && (saveFile.isFile()) && (saveFile.length() != 0)){
                //String newfile = saveFile.getParentFile().toString()+File.pathSeparator+saveFile.getName()+System.currentTimeMillis();
                //saveFile = new File(newfile);
                
                //this will prevent a cycle in recursion.
                throw new IOException("File already exists"); //NOI18N
            }

            HashMap<String, InputStream> srcAddrNContent;
            ResourceRetriever rr;
            
            for (;;) {
                try {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Retrieving from: " + sourceToBeGot);
                    }
                    rr = ResourceRetrieverFactory.getResourceRetriever(baseAddress, sourceToBeGot);
                    if(rr == null )
                        throw new RuntimeException("No Retriever for this Resource address :"+sourceToBeGot); //NOI18N

                    if(isAlreadyDownloadedInThisSession(rr.getEffectiveAddress(baseAddress , sourceToBeGot))){
                        String fileExists = NbBundle.getMessage(RetrieverTask.class,
                                "EXCEPTION_CYCLIC_REFERENCE_INDICATOR");
                        throw new IOException(fileExists);
                    }

                    srcAddrNContent = rr.retrieveDocument(baseAddress , sourceToBeGot);
                    break;
                } catch (ResourceRedirectException ex) {
                    LOG.fine("Redirected to: " + ex.getRedirectedUrl());
                    sourceToBeGot = ex.getRedirectedUrl().toExternalForm();
                }
            }
    
            if(srcAddrNContent == null)
                return null;
            String effectiveSrcAddr = srcAddrNContent.keySet().iterator().next();
            InputStream is = srcAddrNContent.get(effectiveSrcAddr);
            rent.setEffectiveAddress(effectiveSrcAddr);
            if(saveFile == null)
                saveFile = guessSaveFile(rent);
            if(saveFile == null)
                throw new IOException("Could not determine the save file."); //NOI18N
            
            checkForCycle(saveFile, rr.getStreamLength(), is);
            
            if(retEngine.isSave2SingleFolder() && !retEngine.getFileOverwrite() ){
                //this stream is diff but same file name so give another name
                int i = 0;
                File curFile = saveFile;
                String fileName = saveFile.getName();
                while(curFile.isFile())
                    curFile = new File(retEngine.getCurrentSaveRootFile()+File.separator+"new"+i+++fileName);
                saveFile = curFile;
            }
            
            BufferedInputStream bis = new BufferedInputStream(is, 1024);
            FileObject saveFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(saveFile));
            //create datafile and also all the parents folders
            if (!saveFile.getParentFile().mkdirs()) {
                LOG.log(Level.INFO, "Unable to make parent directory. savefile={0}, url={1}, rootSaveFile={2}, parentFile={3}, normalized={4}", new Object[] {
                    saveFile,
                    rent.getEffectiveAddress(),
                    retEngine.getCurrentSaveRootFile(),
                    saveFile.getParentFile(),
                    FileUtil.normalizeFile(saveFile.getParentFile())
                });
            }
            FileObject parent = FileUtil.toFileObject(FileUtil.normalizeFile(saveFile.getParentFile()));
            saveFileObject = FileUtil.createData(parent, saveFile.getName());
            FileLock saveFileLock = saveFileObject.lock();
            try{
                OutputStream saveFileOutputStream = saveFileObject.getOutputStream(saveFileLock);
                BufferedOutputStream bos = new BufferedOutputStream(saveFileOutputStream, 1024);
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = bis.read(buffer)) != -1){
                    bos.write(buffer, 0, len);
                }
                bos.close();
                bis.close();
            }finally{
                //release the lock at any cost
                saveFileLock.releaseLock();
            }
            HashMap<String, File> result = new HashMap<String, File>();
            String modifiedFileExtn = null;
            try {
                modifiedFileExtn = new DocumentTypeSchemaWsdlParser().getFileExtensionByParsing(saveFile);
                if(modifiedFileExtn != null){
                    if(!saveFileObject.getNameExt().endsWith("."+modifiedFileExtn)){
                        String fileName = saveFileObject.getNameExt();
                        if(saveFileObject.getNameExt().endsWith("_"+modifiedFileExtn)){
                            fileName = fileName.substring(0, fileName.length() -
                                    ("_"+modifiedFileExtn).length());
                        }
                        
                        File newFile = new File(saveFile.getParent()+File.separator+fileName+"."+modifiedFileExtn);
                        if(newFile.isFile() && retEngine.getFileOverwrite())
                            newFile.delete();
                        
                        FileObject newsaveFileObject = FileUtil.copyFile(saveFileObject, saveFileObject.getParent(),
                                fileName, modifiedFileExtn);
                        saveFileObject.delete();
                        saveFileObject = newsaveFileObject;
                    }
                }
            } catch (Exception ex) {
                //this is just a rename. So, just ignore any exceptions.
            }
            
            result.put(effectiveSrcAddr, FileUtil.toFile(saveFileObject));
            //commented out the ref file generation
            //createReferenceFile(effectiveSrcAddr, saveFile);
            return result;
        }
    }
    
    
    private File guessSaveFile(RetrieveEntry rent) throws URISyntaxException, IOException{
        if(rent.getSaveFile() != null)
            return rent.getSaveFile();
        URI curUri = new URI(rent.getEffectiveAddress());
        //get file name
        String curAddr = rent.getEffectiveAddress();
        String curFileName = null;
        int index = curAddr.lastIndexOf('/'); //NOI18N
        if(index != -1){
            curFileName = curAddr.substring(index+1);
            // typically directores end with /. It's possible that some othe URI
            // in that dir will be used -> clash between dirr and file name.
            if ("".equals(curFileName)) {
                curFileName = "index";
            }
        }else{
            curFileName = curAddr;
        }
        
        if(retEngine.isSave2SingleFolder()){
            curFileName = convertAllSpecialChars(curFileName);
            return new File(retEngine.getCurrentSaveRootFile()+File.separator+curFileName);
        }
        
        File result = null;
        
        //get directory to be stored
        if(curUri.isAbsolute()){
            if("http".equalsIgnoreCase(curUri.getScheme()) || "https".equalsIgnoreCase(curUri.getScheme())) { //NOI18N
                //treat URLs differently
                result = getSaveFileForURL(curUri);
            } else {
                URI temp = new URI(rent.getCurrentAddress());
                if(temp.isAbsolute())
                    result = new File(new URI(retEngine.getFixedSaveRootFolder().toURI().toString()+"/"+curFileName));
                else if(rent.getLocalBaseFile() == null)
                    result = new File(new URI(retEngine.getFixedSaveRootFolder().toURI().toString()+"/"+temp.toString()));
                else
                    result = new File(new URI(rent.getLocalBaseFile().getParentFile().toURI().toString()+"/"+temp.toString()));
            }
        }else{
            File newFile = new File(new URI(rent.getLocalBaseFile().getParentFile().toURI().normalize().toString()+"/"+rent.getCurrentAddress())).getCanonicalFile();
            File newParentFile = getModifiedParentFile(rent.getLocalBaseFile(), newFile);
            if(rent.getLocalBaseFile() != newParentFile)
                result = new File(new URI(newParentFile.getParentFile().toURI().toString()+"/"+rent.getCurrentAddress()));
            else
                result = newFile;
        }
        return result;
    }
    
    private File getModifiedParentFile(File parentFile, File curSaveFile) {
        File result = parentFile;
        
        String curSaveStr = curSaveFile.toURI().toString();
        String saveRootStr = retEngine.getFixedSaveRootFolder().toURI().toString();
        
        if(curSaveStr.startsWith(saveRootStr)){
            return result;
        }
        int pushCount = Utilities.countPushdownFolders(curSaveFile.toURI(), retEngine.getFixedSaveRootFolder().toURI());
        retEngine.pushDownRoot(pushCount);
        result = retEngine.getNewFileForOld(parentFile, pushCount);
        return result;
    }
    
    private File getSaveFileForURL(URI absURI) {
        String rootFolderStr = retEngine.getFixedSaveRootFolder().toURI().toString();
        //replace http:// with the saverootfolder
        String resultStr = absURI.getSchemeSpecificPart().replace(':','_');
        resultStr = resultStr.replace('?', '.');
        if(resultStr.contains(".")){
            String fileExtension = resultStr.substring(resultStr.lastIndexOf('.'), resultStr.length());
            
            if(!fileExtension.equals(fileExtension.toLowerCase())){
                resultStr = resultStr.substring(0, resultStr.lastIndexOf('.'))+fileExtension.toLowerCase();
            }
        }
        resultStr = convertAllSpecialChars(resultStr);
        resultStr = rootFolderStr+"/"+resultStr;
        // trailing slash indicates a possible directory; subsequent retrievals
        // from that directory will clash on parent dir/file name -- experienced on WSDL schemas
        if (resultStr.endsWith("/")) {
            resultStr = resultStr + "index";
        }
        try {
            return new File(new URI(resultStr).normalize());
        } catch (URISyntaxException ex) {
            return null;
        }
    }
    
    
    public String convertAllSpecialChars(String resultStr){
        StringBuffer sb = new StringBuffer(resultStr);
        for(int i = 0; i < sb.length(); i++){
            char c = sb.charAt(i);
            if( Character.isLetterOrDigit(c) ||
                    (c == '/') ||
                    (c == '.') ||
                    (c == '_') ||
                    (c == ' ') ||
                    (c == '-')){
                continue;
            }else{
                sb.setCharAt(i, '_');
            }
        }
        return sb.toString();
    }
    
    
    private void checkForCycle(File saveFile, long l, InputStream is) throws IOException {
        String fileExists = NbBundle.getMessage(RetrieverTask.class,
                "EXCEPTION_CYCLIC_REFERENCE_INDICATOR");
        if(saveFile.isFile()){
            if( (isAlreadyDownloadedInThisSession(saveFile)) ||
                    ((saveFile.length() == l) && !retEngine.getFileOverwrite()) ) {
                //file is already there...Breaks cyclic link traversals
                is.close();
                throw new IOException(fileExists+" : "+saveFile);
            }
            if(retEngine.getFileOverwrite()){
                //let the retriever overwrite
                return;
            } else{
                //dont overwrite.
                is.close();
                throw new IOException(fileExists+" : "+saveFile);
            }
        }
        if(saveFile.isDirectory()){
            is.close();
            String dirExists = NbBundle.getMessage(RetrieverTask.class,
                    "EXCEPTION_DIRECTORY_ALREADY_EXISTS");
            throw new IOException(dirExists + " : "+saveFile.getCanonicalPath()); //NOI18N
        }
    }
    
    private boolean isAlreadyDownloadedInThisSession(File thisFile){
        for(RetrieveEntry rent : retEngine.getRetrievedList()){
            if(rent.getSaveFile().equals(thisFile))
                return true;
        }
        return false;
    }
    
    private boolean isAlreadyDownloadedInThisSession(String uri){
        for(RetrieveEntry rent : retEngine.getRetrievedList()){
            if(rent.getEffectiveAddress().equals(uri))
                return true;
        }
        return false;
    }
}
