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

package org.netbeans.spi.tasklist;

import java.util.List;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.openide.filesystems.FileObject;

/**
 * Task Scanner that can provide new Tasks for specified resources (files/folders) 
 * when asked by the Task List framework.
 * 
 * @author S. Aubrecht
 */
public abstract class FileTaskScanner {
    
    private String displayName;
    private String description;
    private String optionsPath;
    
    /**
     * Creates a new instance of FileTaskScanner
     * 
     * @param displayName Scanner's display name, will appear in Task List's filter window.
     * @param description Scanner's description, will be used for tooltips.
     * @param optionsPath Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings. When scanner's settings changed the 
     * scanner must notify - {@link FileTaskScanner.Callback#refreshAll} - the Task List framework that a re-scan is needed.
     */
    public FileTaskScanner( String displayName, String description, String optionsPath ) {
        assert null != displayName;
        this.displayName = displayName;
        this.description = description;
        this.optionsPath = optionsPath;
    }
    
    /**
     * Scanner's display name.
     * @return Scanner's display name.
     */
    final String getDisplayName() {
        return displayName;
    }
    
    /**
     * Scanner's description (e.g. for tooltips).
     * @return Scanner's description (e.g. for tooltips).
     */
    final String getDescription() {
        return description;
    }
    
    /**
     * Path to the global options panel.
     * @return Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings.
     */
    final String getOptionsPath() {
        return optionsPath;
    }
    
    /**
     * Notification from the Task List framework that the scanning phase is about to begin.
     * (Time to create expensive parsers, compilers etc...)
     */
    public void notifyPrepare() {
    }
    
    /**
     * Notification from the Task List framework that the scanning phase ended.
     * (Time to release expensive parsers, compilers etc...)
     */
    public void notifyFinish() {
    }
    
    /**
     * Scan the given resource (file or folder?) for tasks. This method is always
     * called within {@link #notifyPrepare} and {@link #notifyFinish} calls. Depending on Task
     * List's current scope this method may be called repeatedly for different resources.
     * 
     * @param resource Resource to be scanned.
     * 
     * @return List of scanned Tasks or null if the provider is busy at the moment and 
     * previously scanned tasks are to be used instead.
     */
    public abstract List<? extends Task> scan( FileObject resource );
    
    /**
     * Called by the framework when this Task type is enabled/disabled in Task List's filter window.
     * @param callback Callback into Task List's framework, null value indicates that user has disabled
     * this type of Tasks.
     */
    public abstract void attach( Callback callback );

    /**
     * Callback into Task List's framework.
     */
    public static final class Callback {

        private FileTaskScanner scanner;
        private TaskManager tm;
        
        /** Creates a new instance of FileBasedTaskScannerCallback */
        Callback( TaskManager tm, FileTaskScanner scanner ) {
            this.tm = tm;
            this.scanner = scanner;
        }

        /**
         * Notify the Task List framework that the given resources need to be
         * rescanned by this scanner.
         * @param resources Resources to be rescanned.
         */
        public void refresh( FileObject... resources ) {
            tm.refresh( scanner, resources );
        }

        /**
         * Notify the Task List framework that all resources 
         * under the current scope need to be rescanned by this scanner.
         */
        public void refreshAll() {
            tm.refresh( scanner );
        }
    }
}
