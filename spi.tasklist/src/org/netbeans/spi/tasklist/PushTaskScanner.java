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
 * <p>Task Scanner that can push new Tasks into Task List window.</p>
 * 
 * <p>You should use this scanner type if it takes too long to compute your tasks
 * or if your tasks are calculated asynchronously. <br/>
 * In most cases it is easier to use {@link FileTaskScanner} instead.</p>
 * 
 * @author S. Aubrecht
 */
public abstract class PushTaskScanner {
    
    private String displayName;
    private String description;
    private String optionsPath;
    
    /**
     * Creates a new instance of PushTaskScanner
     * 
     * @param displayName Scanner's display name, will appear in Task List's filter window.
     * @param description Scanner's description, will be used for tooltips.
     * @param optionsPath Path that identifies panel in the global Options dialog window, 
     * or null if the scanner has no user settings. When scanner's settings changed the 
     * scanner must refresh its tasks the Task List window 
     * ({@link PushTaskScanner.Callback#clearAllTasks}, {@link PushTaskScanner.Callback#setTasks}).
     */
    public PushTaskScanner( String displayName, String description, String optionsPath ) {
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
     * Called by the framework when the user switches to a different scanning scope
     * or when the currently used scope needs to be refreshed.
     *
     * @param scope New scanning scope, null value indicates that task scanning is to be cancelled.
     * @param callback Callback into Task List framework.
     */
    public abstract void setScope( TaskScanningScope scope, Callback callback );

    
    /**
     * Callback into Task List framework
     */
    public static final class Callback {
        
        private PushTaskScanner scanner;
        private TaskManager tm;
        
        /** Creates a new instance of SimpleTaskScannerCallback */
        Callback( TaskManager tm, PushTaskScanner scanner ) {
            this.tm = tm;
            this.scanner = scanner;
        }

        /**
         * Notify the framework that the scanner started looking for available Tasks.
         */
        public void started() {
            tm.started( scanner );
        }

        /**
         * Add/remove Tasks for the given file/folder.
         * @param file Resource (file or folder) the tasks are associated with.
         * @param tasks Tasks associated with the given resource or an empty list to remove previously provided Tasks.
         */
        public void setTasks( FileObject file, List<? extends Task> tasks ) {
            tm.setTasks( scanner, file, tasks );
        }

        /**
         * Add/remove Tasks that aren't associated with a particular resource.
         * @param tasks Tasks associated with this TaskScanner or an empty list to remove previously provided Tasks.
         * @since 1.6
         */
        public void setTasks( List<? extends Task> tasks ) {
            tm.setTasks( scanner, tasks );
        }
        
        /**
         * Remove from the Task List window all Tasks that were provided by this scanner.
         */
        public void clearAllTasks() {
            tm.clearAllTasks( scanner );
        }

        /**
         * Notify the framework that the scanner has finished.
         */
        public void finished() {
            tm.finished( scanner );
        }

        /**
         * @returns true, if the framework is observed.
         * @since spi.tasklist/1.24
         */
        public boolean isObserved() {
            return tm.isObserved();
        }

        /**
         * @returns true, if the current editor scope is set in the framework.
         * Performance helper method.
         * @since spi.tasklist/1.24
         */
        public boolean isCurrentEditorScope() {
            return tm.isCurrentEditorScope();
        }
    }
}
