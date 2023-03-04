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

package org.netbeans.installer.utils.system.cleaner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.ErrorLevel;

/**
 *
 * @author Dmitry Lipin
 */


public abstract class ProcessOnExitCleanerHandler extends SystemPropertyOnExitCleanerHandler {
    protected List <String> runningCommand;
    private String cleanerFileName ;
    private static final String DELETING_FILES_LIST = "deleteNbiFiles";

    protected ProcessOnExitCleanerHandler(String cleanerFileName) {        
        this.cleanerFileName = cleanerFileName;        
    }
    protected File getCleanerFile() throws IOException{
        String name = cleanerFileName;
        int idx = name.lastIndexOf(".");
        String ext = "";
        if(idx > 0) {
            ext = name.substring(idx);
            name = name.substring(0, idx);
        }
        return File.createTempFile(name, ext, SystemUtils.getTempDirectory());        
    }
    
    protected File createTempFileWithFilesList() throws IOException{
        return File.createTempFile(DELETING_FILES_LIST,null, SystemUtils.getTempDirectory());        
    }
    
    protected abstract void writeCleaningFileList(File listFile, List <String> files) throws IOException;
    protected abstract void writeCleaner(File cleanerFile) throws IOException;

    public void init(){        
        List <String> fileList = getFilesList();

        if(fileList.size() > 0) {            
            try {
                List<String> paths = new ArrayList<String>();
                for (String s : fileList) {
                    if (!paths.contains(s)) {
                            paths.add(s);
                    }
                }
                Collections.sort(paths, Collections.reverseOrder());
                File listFile = createTempFileWithFilesList();
                writeCleaningFileList(listFile, paths);                
                File cleanerFile = getCleanerFile();
                writeCleaner(cleanerFile);
                SystemUtils.correctFilesPermissions(cleanerFile);
                runningCommand = new ArrayList <String> ();
                runningCommand.add(cleanerFile.getCanonicalPath());
                runningCommand.add(listFile.getCanonicalPath());
            } catch  (IOException e) {
                // do nothing then..
            }
        }
    }
    
    public void run() {
        init();
        if(runningCommand!=null ) {
            try {                
                ProcessBuilder builder= new ProcessBuilder(runningCommand);
                builder.directory(SystemUtils.getUserHomeDirectory());
                builder.start();
                LogManager.log(ErrorLevel.DEBUG, "... cleaning process has been started ");
            } catch (IOException ex) {
                LogManager.log(ex);
            }
        }
    }
}
