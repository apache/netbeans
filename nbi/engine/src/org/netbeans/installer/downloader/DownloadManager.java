/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
