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

package org.netbeans.api.progress;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.netbeans.modules.progress.spi.UIInternalHandle;
import org.openide.util.Cancellable;

/**
 * Factory to create various ProgressHandle instances that allow long lasting
 * tasks to show their progress using various progress UIs. This class adds the 
 * ability to provide a custom Swing UI (JPanel) and manipulate with Swing components 
 * to the {@link org.netbeans.api.progress.ProgressHandle}.
 * @author Milos Kleint (mkleint@netbeans.org)
 * @author Svata Dedic
 */
public final class ProgressHandleFactory {

    /** Creates a new instance of ProgressIndicatorFactory */
    private ProgressHandleFactory() {
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String)}
     */
    public static ProgressHandle createHandle(String displayName) {
        return createHandle(displayName, (Action)null);
    }
    
     /**
      * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     * @deprecated Use {@link ProgressHandle#createHandle(java.lang.String, org.openide.util.Cancellable)}.
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel) {
        return createHandle(displayName, allowToCancel, null);
    }

    /**
     * Create a progress ui handle for a long lasting task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Action linkOutput) {
        return createHandle(displayName, null, linkOutput);
    }
    
    /**
     * Create a progress ui handle for a long lasting task.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, true, linkOutput).createProgressHandle();
    }
    
    private static UIInternalHandle ih(ProgressHandle h) {
        return (UIInternalHandle)h.getInternalHandle();
    }

    /**
     * Get the progress bar component for use in custom dialogs, the task won't 
     * show in the progress bar anymore.
     * @return the component to use in custom UI.
     */
    public static JComponent createProgressComponent(ProgressHandle handle) {
        return ih(handle).extractComponent();
    }

    /**
     * Get the task title component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.setDisplayName()</code> method
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createMainLabelComponent(ProgressHandle handle) {
        return ih(handle).extractMainLabel();
    }
    
    /**
     * Get the detail messages component for use in custom dialogs, the task won't 
     * show in the progress bar anymore. The text of the label is changed by calls to <code>ProgressHandle.progress(String)</code> method.
     * @return the component to use in custom UI.
     * @since org.netbeans.api.progress 1.8
     */
    public static JLabel createDetailLabelComponent(ProgressHandle handle) {
        return ih(handle).extractDetailLabel();
    }
    
    /**
     * Create a handle for a long lasting task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName) {
        return createSystemHandle(displayName, null, null);
    }

    /**
     * Create a cancelable handle for a task that is not triggered by explicit user action.
     * Such tasks have lower priority in the UI.
     * @param displayName to be shown in the progress UI
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel) {
        return createSystemHandle(displayName, allowToCancel, null);
    }
    
    /**
     * Create a progress ui handle for a task that is not triggered by explicit user action.
     * @param allowToCancel either null, if the task cannot be cancelled or 
     *          an instance of {@link org.openide.util.Cancellable} that will be called when user 
     *          triggers cancel of the task.
     * @param linkOutput an <code>Action</code> instance that links the running task in the progress bar
     *                   to an output of the task. The action is assumed to open the apropriate component with the task's output.
     * @param displayName to be shown in the progress UI
     * @return an instance of {@link org.netbeans.api.progress.ProgressHandle}, initialized but not started.
     *
     */
    public static ProgressHandle createSystemHandle(String displayName, Cancellable allowToCancel, Action linkOutput) {
        return new UIInternalHandle(displayName, allowToCancel, false, linkOutput).createProgressHandle();
    }    
}
