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

package org.netbeans.installer.downloader;

import java.io.File;
import org.netbeans.installer.downloader.queue.DispatchedQueue;
import org.netbeans.installer.utils.helper.FinishHandler;

/**
 * It's main downloader class. It's singleton.
 * Only from here client can access download service and register there own listeners.
 * Also from here managed execution of downloding process.
 * 
 * @author Danila_Dugurov
 */
public class DownloadManager {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static final DownloadManager instance = new DownloadManager();
    
    public static DownloadManager getInstance() {
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File          localDirectory;
    private FinishHandler finishHandler;
    
    private File defaultFolder;
    private DispatchedQueue queue;
    private File wd;
    
    private DownloadManager() {
    }
    
    public void init() {
        defaultFolder = new File(localDirectory, DOWNLOADS_DIR_NAME);
        defaultFolder.mkdirs();
        
        wd = new File(localDirectory, WD_DIR_NAME);
        wd.mkdirs();
        
        queue = new DispatchedQueue(new File(wd, STATE_FILE_NAME));
        queue.reset();
    }
    
    public PumpingsQueue queue() {
        return queue;
    }
    
    public void registerListener(final DownloadListener listener) {
        queue.addListener(listener);
    }
    
    public void invoke() {
        queue.invoke();
    }
    
    public void terminate() {
        queue.terminate();
    }
    
    public boolean isActive() {
        return queue.isActive();
    }
    
    public File getWd() {
        return wd;
    }
    
    public File defaultFolder() {
        return defaultFolder;
    }
    
    public File getLocalDirectory() {
        return localDirectory;
    }
    
    public void setLocalDirectory(final File localDirectory) {
        this.localDirectory = localDirectory;
    }
    
    public FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    public void setFinishHandler(final FinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DOWNLOADS_DIR_NAME = 
            "downloads"; // NOI18N
    
    public static final String WD_DIR_NAME = 
            "wd"; // NOI18N
    
    public static final String STATE_FILE_NAME = 
            "state.xml"; // NOI18N
}
