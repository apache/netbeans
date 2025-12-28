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

package org.netbeans.modules.xml.retriever.impl;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.DocumentParserFactory;
import org.netbeans.modules.xml.retriever.DocumentTypeParser;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.RetrieverEngine;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 * @author girix
 */
public class RetrieverEngineImpl extends RetrieverEngine {
    
    private LinkedList<RetrieveEntry> currentRetrievalList = new LinkedList<RetrieveEntry>();
    
    private File currentSaveRootFile = null;
    
    private File fixedSaveRootFolder = null;
    
    boolean startNewThread = true;
    
    private boolean showErrorPopup = true;
    
    public RetrieverEngineImpl(File fixedSaveRootFolder){
        this.fixedSaveRootFolder = fixedSaveRootFolder;
        this.currentSaveRootFile = fixedSaveRootFolder;
    }
    
    public RetrieverEngineImpl(File fixedSaveRootFolder, boolean startNewThread){
        this.fixedSaveRootFolder = fixedSaveRootFolder;
        this.currentSaveRootFile = fixedSaveRootFolder;
        this.startNewThread = startNewThread;
    }
    
    public boolean canShowErrorPopup() {
        return showErrorPopup;
    }
    
    public void setShowErrorPopup(boolean show) {
        this.showErrorPopup = show;
    }
    
    public void addResourceToRetrieve(RetrieveEntry rent) {
        currentRetrievalList.add(rent);
    }
    
    Thread taskThread = null;
    public void start(){
        if(startNewThread){
            taskThread = new Thread(this);
            taskThread.start();
        }else{
            run();
        }
    }
    
    boolean STOP_PULL = false;
    public void run() {
        ProgressHandle ph = ProgressHandle.createHandle(
                NbBundle.getMessage(RetrieverEngineImpl.class,"LBL_PROGRESSBAR_Retrieve_XML"),
                new Cancellable(){
            public boolean cancel() {
                synchronized(RetrieverEngineImpl.this){
                    if(!RetrieverEngineImpl.this.STOP_PULL){
                        RetrieverEngineImpl.this.STOP_PULL = true;
                        //taskThread.interrupt();
                    }
                }
                return true;
            }
        }, new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                getOPWindow().setOutputVisible(true);
                getOPWindow().select();
            }
        });
        ph.start();
        ph.switchToIndeterminate();
        try{
            pullRecursively();
        }finally{
            ph.finish();
        }
    }
    
    boolean firstTime = true;
    String firstAddressParentStr = null;
    private void pullRecursively() {
        synchronized(RetrieverEngineImpl.class){
            while(!currentRetrievalList.isEmpty() && !STOP_PULL){
                //System.out.println(currentRetrievalList);
                RetrieveEntry rent =currentRetrievalList.getFirst();
                //System.out.println("###"+rent.toString());
                currentRetrievalList.removeFirst();
                RetrieverTask rt = new RetrieverTask(rent, this);
                
                if(firstTime){
                    firstAddressParentStr = rent.getCurrentAddress().substring(0, rent.getCurrentAddress().lastIndexOf("/"));
                    firstTime = false;
                }
                
                updateDownloadingInfo(rent);
                
                HashMap<String,File> storedFileMap = null;
                try {
                    storedFileMap = rt.goGetIt();
                } catch (URISyntaxException ex) {
                    //This might an error in the file. Ignore
                    //ex.printStackTrace();
                    handleException(rent, ex);
                    continue;
                } catch (IOException ex) {
                    //This might have been thrown to indicate a cyclic reference.
                    //ex.printStackTrace();
                    handleException(rent, ex);
                    continue;
                }
                
                
                if(!rent.isRecursive())
                    continue;
                
                if(storedFileMap == null){
                    continue;
                }
                String effectiveSrcAddr = storedFileMap.keySet().iterator().next();
                File storedFile = storedFileMap.get(effectiveSrcAddr);
                
                rent.setSaveFile(storedFile);
                rent.setEffectiveAddress(effectiveSrcAddr);
                
                updateDownloadedInfo(rent);
                
                createCatalogIfRequired(rent);
                
                DocumentTypeParser dtp = DocumentParserFactory.getParser(rent.getDocType());
                List<String> thisFileRefs = null;
                try {
                    thisFileRefs = dtp.getAllLocationOfReferencedEntities(storedFile);
                    //System.out.println("Parsed:"+storedFile+" Got:"+thisFileRefs);
                } catch (Exception ex) {
                    //was not able to parse the doc. Currently ignore this.
                    //ex.printStackTrace();
                    continue;
                }
                for(String ref: thisFileRefs){
                    currentRetrievalList.addLast(new RetrieveEntry(effectiveSrcAddr,
                            ref, storedFile, null, rent.getDocType(), rent.isRecursive()));
                }
                printList();
            }
            closeOPOuts();
        }
    }
    
    private void printList() {
        for(RetrieveEntry rent: currentRetrievalList){
            //System.out.println("------"+rent);
        }
    }
    
    public File getCurrentSaveRootFile() {
        return currentSaveRootFile;
    }
    
    public void setCurrentSaveRootFile(File currentSaveRootFile) {
        this.currentSaveRootFile = currentSaveRootFile;
    }
    
    private String getCorrectFolderName(int folderIndex){
        StringTokenizer stok = new StringTokenizer(firstAddressParentStr, "/");
        Stack <String> stack = new Stack<String>();
        while(stok.hasMoreTokens())
            stack.push(stok.nextToken());
        for(int i = 1; i < folderIndex ; i++)
            stack.pop();
        return stack.pop();
    }
    
    int currentPushCount = 0;
    int previousPushCount = 0;
    
    public void pushDownRoot(int pushCount) {
        File newTmpRoot = new File(currentSaveRootFile.getParent()+File.separator+System.currentTimeMillis());
        File leafFolder = newTmpRoot;
        leafFolder.mkdirs();
        for(int i = pushCount;i >= 2; i--){
            leafFolder = new File(leafFolder.toString()+File.separator+getCorrectFolderName(currentPushCount+i));
            leafFolder.mkdirs();
        }
        leafFolder = new File(leafFolder.toString()+File.separator+getCorrectFolderName(currentPushCount+1));
        File movedRoot = leafFolder;
        //String rootFolderName = saveRootFile.getName();
        while(!currentSaveRootFile.renameTo(movedRoot)){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
        }
        while(!newTmpRoot.renameTo(currentSaveRootFile)){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
        }
        
        File newRoot = currentSaveRootFile;
        for(int i = pushCount;i >= 1; i--)
            newRoot = new File(newRoot.toString()+File.separator+getCorrectFolderName(currentPushCount+i));
        correctAllEntriesInTheList(newRoot);
        previousPushCount = currentPushCount;
        currentPushCount += pushCount;
    }
    
    public File getNewFileForOld(File oldFile, int pushCount) {
        File newRoot = currentSaveRootFile;
        for(int i = pushCount;i >= 1; i--)
            newRoot = new File(newRoot.toString()+File.separator+getCorrectFolderName(previousPushCount+i));
        String oldPath = oldFile.toString();
        String newPath = new String(new StringBuffer(oldPath).replace(0, currentSaveRootFile.toString().length(),newRoot.toString()));
        File newFile = new File(newPath);
        return newFile;
    }
    
    private void correctAllEntriesInTheList(File newRoot) {
        for(RetrieveEntry rent : currentRetrievalList){
            String oldPath = rent.getLocalBaseFile().toString();
            String newPath = new String(new StringBuffer(oldPath).replace(0, currentSaveRootFile.toString().length(),newRoot.toString()));
            File newLocalBaseFile = new File(newPath);
            rent.setLocalBaseFile(newLocalBaseFile);
        }
        for(RetrieveEntry rent : retrievedList){
            String oldPath = rent.getSaveFile().toString();
            String newPath = new String(new StringBuffer(oldPath).replace(0, currentSaveRootFile.toString().length(),newRoot.toString()));
            File newLocalBaseFile = new File(newPath);
            rent.setSaveFile(newLocalBaseFile);
        }
    }
    
    public File getFixedSaveRootFolder() {
        return fixedSaveRootFolder;
    }
    
    private void handleException(RetrieveEntry rent, Exception ex) {
        if(audits == null)
            audits = new HashMap<RetrieveEntry, Exception>();
        audits.put(rent, ex);
        //System.out.println(ex instanceof UnknownHostException);
        if(ex instanceof UnknownHostException){
            String errorMess = NbBundle.getMessage(RetrieverEngineImpl.class, "MSG_unknown_host_p1")+ex.getLocalizedMessage()+"\n"+NbBundle.getMessage(RetrieverEngineImpl.class, "MSG_unknownhost_p2");
            outputError(errorMess);
            if(showErrorPopup){
                NotifyDescriptor.Message ndm = new NotifyDescriptor.Message(errorMess, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndm);
            }
            return;
        }
        if(ex instanceof URISyntaxException){
            String errorMess = ex.getLocalizedMessage();
            outputError(errorMess);
            if(showErrorPopup){
                NotifyDescriptor.Message ndm = new NotifyDescriptor.Message(errorMess, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndm);
            }
            return;
        }
        if(ex instanceof FileNotFoundException){
            String errorMess = NbBundle.getMessage(RetrieverEngineImpl.class, "MSG_unknown_file", ex.getMessage());
            outputError(errorMess);
            if(showErrorPopup){
                NotifyDescriptor.Message ndm = new NotifyDescriptor.Message(errorMess, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndm);
            }
            return;
        }
        
        if(ex instanceof IOException){
            String exStr = NbBundle.getMessage(RetrieverEngineImpl.class, "EXCEPTION_CYCLIC_REFERENCE_INDICATOR");
            if(ex.getMessage().startsWith(exStr)){
                outputMessage(ex.getMessage()+":\n\t "+ NbBundle.getMessage(RetrieverEngineImpl.class,
                        "MSG_retrieving_location_found_in",rent.getCurrentAddress(),
                        rent.getBaseAddress()));
                return;
            }
            String errorMess = NbBundle.getMessage(RetrieverEngineImpl.class, "MSG_general_io_error", ex.getMessage());
            if(showErrorPopup){
                NotifyDescriptor.Message ndm = new NotifyDescriptor.Message(errorMess, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(ndm);
            }
            outputError(errorMess);
            return;
        }
        
        outputError(ex.getMessage());
        return;
        
    }
    
    String opTabTitle = NbBundle.getMessage(RetrieverEngineImpl.class,
            "TITLE_retriever_output_tab_title"); //NOI18N
    
    InputOutput iop  = null;
    private InputOutput getOPWindow(){
        if(iop == null){
            iop = IOProvider.getDefault().getIO(opTabTitle, false);
            iop.setErrSeparated(true);
            iop.setFocusTaken(false);
            /*iop.select();
            try {
                iop.getOut().reset();
            } catch (IOException ex) {
            }*/
            ioOut = iop.getOut();
            DateFormat dtf = DateFormat.getDateTimeInstance();
            ioOut.print("\n\n"+dtf.format(new Date(System.currentTimeMillis()))+" : ");
        }
        return iop;
    }
    
    private void closeOPOuts(){
        getErrOut().close();
        getOPOut().close();
    }
    
    OutputWriter ioOut;
    private OutputWriter getOPOut(){
        if(ioOut == null){
            ioOut = getOPWindow().getOut();
        }
        return ioOut;
    }
    
    OutputWriter ioError;
    private OutputWriter getErrOut(){
        if(ioError == null){
            ioError = getOPWindow().getErr();
        }
        return ioError;
    }
    
    private void outputError(String str){
        OutputWriter err = getErrOut();
        err.println(NbBundle.getMessage(RetrieverEngineImpl.class, "MSG_Error_str",
                str)); //NOI18N
        err.flush();
    }
    
    private void outputMessage(String str){
        OutputWriter err = getOPOut();
        err.println(str); //NOI18N
        err.flush();
    }
    
    private void updateDownloadingInfo(RetrieveEntry rent) {
        OutputWriter opt = getOPOut();
        if(rent.getBaseAddress() != null){
            opt.println(
                    NbBundle.getMessage(RetrieverEngineImpl.class,
                    "MSG_retrieving_location_found_in",rent.getCurrentAddress(),
                    rent.getBaseAddress())); //NOI18N
        }else{
            opt.println(
                    NbBundle.getMessage(RetrieverEngineImpl.class,
                    "MSG_retrieving_location",rent.getCurrentAddress())); //NOI18N
        }
        opt.flush();
    }
    
    List<RetrieveEntry> retrievedList = new ArrayList<RetrieveEntry>();
    
    public List<RetrieveEntry> getRetrievedList() {
        return retrievedList;
    }
    
    private void updateDownloadedInfo(RetrieveEntry rent) {
        retrievedList.add(rent);
        OutputWriter opt = getOPOut();
        String str = "   "+rent.getEffectiveAddress();
        opt.println(
                NbBundle.getMessage(RetrieverEngineImpl.class,
                "MSG_retrieved_saved_at",str, rent.getSaveFile())); //NOI18N
        opt.flush();
    }
    
    public File getSeedFileLocation(){
        if(retrievedList.size() > 0){
            RetrieveEntry rent = retrievedList.get(0);
            return rent.getSaveFile();
        }
        return null;
    }
    
    private void createCatalogIfRequired(RetrieveEntry rent) {
        URI curURI = null;
        String addr = rent.getEffectiveAddress();
        try {
            //check if this is the first entry and the connection was redirected. If yes, then
            //store the URI as the original URI instead of the redirected URI
            String tempStr = URLResourceRetriever.resolveURL(rent.getBaseAddress(), rent.getCurrentAddress());
            if(! (new URI(tempStr).equals(new URI(addr))) ){
                addr = tempStr;
            }
        } catch (URISyntaxException ex) {
            //ignore
        }
        if(isSave2SingleFolder()){
            if( !rent.getCurrentAddress().equals(rent.getEffectiveAddress()) )
                addr = rent.getCurrentAddress();
        }
        try {
            curURI = new URI(addr);
        } catch (URISyntaxException ex) {
            //this is not supposed to happen. But if it does, then just return
            return;
        }
        FileObject fobj = null;
        try{
            fobj = FileUtil.toFileObject(FileUtil.normalizeFile(rent.getSaveFile()));
        }catch(Exception e){
            return;
        }
        if(fobj == null)
            return;
        CatalogWriteModel dr = null;
        try {
            if(this.catalogFileObject == null) {
                Project project = FileOwnerQuery.getOwner(fobj);
                if (project == null) {
                    // See issue #176769
                    // In can happen if the file was saved outside of the project
                    return;
                }
                dr = CatalogWriteModelFactory.getInstance()
                .getCatalogWriteModelForProject(fobj);
            } else {
                dr = CatalogWriteModelFactory.getInstance()
                .getCatalogWriteModelForCatalogFile(this.catalogFileObject);
            }
        } catch (CatalogModelException ex) {
            //ignore this exception but return
            return;
        }
        //fobj = FileUtil.toFileObject(rent.getSaveFile());
        try {
            dr.addURI(curURI, fobj);
        } catch (Exception ex) {
            //ignore this exception but return
            ex = new Exception("Exception while writing in to catalog.", ex);
            handleException(rent, ex);
            return;
        }
    }
    
    boolean fileOverwrite = false;
    
    public void setFileOverwrite(boolean fileOverwrite){
        this.fileOverwrite = fileOverwrite;
    }
    
    public boolean getFileOverwrite() {
        return fileOverwrite;
    }
    
    Map<RetrieveEntry, Exception> audits;
    public Map<RetrieveEntry, Exception> getRetrievedResourceExceptionMap() {
        return audits;
    }
    
    
    FileObject catalogFileObject = null;
    public void setCatalogFile(FileObject catalogFileObject) {
        this.catalogFileObject = catalogFileObject;
    }
    
    
    private boolean save2SingleFolder = false;
    public void setSave2SingleFolder(boolean save2SingleFolder) {
        this.save2SingleFolder = save2SingleFolder;
    }
    
    public boolean isSave2SingleFolder() {
        return save2SingleFolder;
    }
}
