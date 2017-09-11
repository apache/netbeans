/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project.ui.support;

import javax.swing.event.ChangeListener;
import org.netbeans.modules.project.uiapi.BuildExecutionSupportImplementation;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.filesystems.FileObject;

/**
 * Register running and finished build/run/debug tasks for use
 * by generic project UI, like "Repeat Build" action or "Stop Build" action
 * in main menu.
 *
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.32
 */
public final class BuildExecutionSupport {

    private BuildExecutionSupport() {
        
    }
    /**
     * Notify that the build job was started and pass a reference to it.
     * It is assumed that the given job instance is registered only once.
     * @param item
     */
    public static void registerRunningItem(BuildExecutionSupport.Item item) {
        Utilities.getBuildExecutionSupportImplementation().registerRunningItem(item);
    }

    /**
     * Notify that the build job finished. The instance passed shall be the same
     * to the one passed to registerRunningItem().
     * It is assumed that the given job instance is registered only once and only after the
     * registerRunningItem() method was called.
     * @param item
     */
    public static void registerFinishedItem(BuildExecutionSupport.Item item) {
        Utilities.getBuildExecutionSupportImplementation().registerFinishedItem(item);
    }

    /**
     * Returns the last registered finished item. 
     * 
     * @return item
     * @since 1.78
     */
    public static BuildExecutionSupport.Item getLastFinishedItem() {
        return Utilities.getBuildExecutionSupportImplementation().getLastItem();
    }
  
    /**
     * Registers a change listener on BuildExecutionSupport
     * 
     * @param listener 
     * @since 1.78
     */
    public static void addChangeListener(ChangeListener listener) {
        BuildExecutionSupportImplementation besi = Utilities.getBuildExecutionSupportImplementation();
        besi.addChangeListener(listener);
    }

    /**
     * Unregisters a change listener on BuildExecutionSupport
     * 
     * @param listener 
     * @since 1.78
     */
    public static void removeChangeListener(ChangeListener listener) {
        BuildExecutionSupportImplementation besi = Utilities.getBuildExecutionSupportImplementation();
        besi.removeChangeListener(listener);
    }
        
    /**
     * Wrapper for the build job used by <code>BuildExecutionSupport</code>
     */
    public static interface Item {
        /**
         * Display name of the build job
         * @return
         */
        String getDisplayName();
        /**
         * Trigger re-execution of the build job.
         */
        void repeatExecution();

        /**
         * Check wheather the build job has finished or not.
         * @return
         */
        boolean isRunning();

        /**
         * Request to stop the execution of the build job.
         */
        void stopRunning();

    }
    /**
     * Wrapper for the build job used by <code>BuildExecutionSupport</code>, extending <code>Item</code>,
     * allows collecting history of executions keyed by the <code>getAction()</code> values.
     * Implement <code>equals</code> and <code>hashcode</code> for advanced duplicate item resolution.
     * @since 1.69
     */
    public static interface ActionItem extends Item {
        /**
         * <code>ActionProvider</code> constants, used for grouping the history items, as of 1.69 only <code>ActionProvider.COMMAND_RUN</code> is supported in UI.
         * @return never null
         */
        String getAction();
        
        /**
         * used for memory releasing purposes, all items from given project will be removed when the project is no longer opened.
         * @return directory fileobject or null when not part of a project execution 
         */
        FileObject getProjectDirectory();
    }
}
