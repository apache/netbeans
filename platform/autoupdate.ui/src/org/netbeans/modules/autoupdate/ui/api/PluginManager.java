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

package org.netbeans.modules.autoupdate.ui.api;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.PluginInstaller;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.ui.ModuleInstallerSupport;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizard;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardModel;
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel.OperationType;
import org.openide.NotifyDescriptor;
import org.openide.util.Parameters;

/** Access to UI features of PluginManager that can be useful in other modules
 * as well.
 * @since 1.21
 *
 * @author Jirka Rechtacek
 */
public final class PluginManager {
    private PluginManager() {
    }

    /** Open standard dialog for installing set of modules. Shows it to the user,
     * asks for confirmation, license acceptance, etc. The whole operation requires
     * AWT dispatch thread access (to show the dialog) and blocks
     * (until the user clicks through), so either call from AWT dispatch thread
     * directly, or be sure you hold no locks and block no progress of other
     * threads to avoid deadlocks.
     * <p>
     * Single module installation can be handled easily by
     * {@link #installSingle(java.lang.String, java.lang.String, java.lang.Object[])}.
<pre><code>
{@link OperationContainer}&lt;InstallSupport&gt; container = OperationContainer.createForInstall();
for ({@link UpdateUnit} u : {@link UpdateManager#getUpdateUnits(org.netbeans.api.autoupdate.UpdateManager.TYPE[]) UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE)}) {
    if (u.getCodeName().matches("org.my.favorite.module")) {
        if (u.getAvailableUpdates().isEmpty()) {
            continue;
        }
        container.add(u.getAvailableUpdates().get(0));
    }
}
PluginManager.openInstallWizard(container);
</code></pre>
     *
     * @param container the container with list of modules for install
     * @return true if all the requested modules were successfully installed,
     *    false otherwise.
     * @see #installSingle(java.lang.String, java.lang.String, java.lang.Object[])
     * @see #install(java.util.Set, java.lang.Object[]) 
     */
    public static boolean openInstallWizard(OperationContainer<InstallSupport> container) {
        if (container == null) {
            throw new IllegalArgumentException ("OperationContainer cannot be null."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> all = container.listAll ();
        if (all.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot be empty."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> invalid = container.listInvalid();
        if (! invalid.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot contain invalid elements but " + invalid); // NOI18N
        }
        OperationInfo<InstallSupport> info = all.get (0);
        OperationType doOperation = info.getUpdateUnit ().getInstalled () == null ? OperationType.INSTALL : OperationType.UPDATE;
        return new InstallUnitWizard ().invokeWizard (new InstallUnitWizardModel (doOperation, container), false);
    }

    /** Open standard dialog for installing set of modules. Shows it to the user,
     * asks for confirmation, license acceptance, etc. The whole operation requires
     * AWT dispatch thread access (to show the dialog) and blocks
     * (until the user clicks through), so either call from AWT dispatch thread
     * directly, or be sure you hold no locks and block no progress of other
     * threads to avoid deadlocks.
     *
     * @param container the container with list of modules for install
     * @param runInBackground if <code>true</code> then installation run in the background after license acceptance
     */
    public static void openInstallWizard(OperationContainer<InstallSupport> container, boolean runInBackground) {
        if (container == null) {
            throw new IllegalArgumentException ("OperationContainer cannot be null."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> all = container.listAll ();
        if (all.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot be empty."); // NOI18N
        }
        List<OperationContainer.OperationInfo<InstallSupport>> invalid = container.listInvalid();
        if (! invalid.isEmpty ()) {
            throw new IllegalArgumentException ("OperationContainer cannot contain invalid elements but " + invalid); // NOI18N
        }
        OperationInfo<InstallSupport> info = all.get (0);
        OperationType doOperation = info.getUpdateUnit ().getInstalled () == null ? OperationType.INSTALL : OperationType.UPDATE;
        new InstallUnitWizard ().invokeWizard (new InstallUnitWizardModel (doOperation, container), true, runInBackground);
    }
    
    /** Open standard dialog for installing a module including declared dependencies.
     * Shows it to the user, asks for confirmation, license acceptance, etc.
     * The whole operation requires AWT dispatch thread access (to show the dialog)
     * and blocks (until the user clicks through), so either call from AWT dispatch
     * thread directly, or be sure you hold no locks and block no progress of other
     * threads to avoid deadlocks.
     *
     * @param codenamebase the codenamebase of module to install
     * @param displayName the display name of the module
     * @param alternativeOptions alternative options possibly displayed in error
     *             dialog user may choose if it is not possible to install the plugin;
     *             if chosen the option is return value of this method
     * @return <code>null</code> if the module has been successfully installed
     *             and/or activated, otherwise it returns the options user has
     *             selected in problem dialog, typically {@link NotifyDescriptor#DEFAULT_OPTION}
     *             (on esc), {@link NotifyDescriptor#CANCEL_OPTION} or
     *             any of <code>alternativeOptions</code>.
     * @since 1.35
     * @see #install(java.util.Set, java.lang.Object[]) 
     */
    @CheckForNull
    public static Object installSingle(@NonNull String codenamebase, @NonNull String displayName,
            @NonNull Object... alternativeOptions) {
        Parameters.notNull("cnb", codenamebase);
        Parameters.notNull("displayName", displayName);
        Parameters.notNull("alternativeOptions", alternativeOptions);

        try {
            return new ModuleInstallerSupport(alternativeOptions).installPlugins(displayName, Collections.singleton(codenamebase));
        } catch (OperationException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.WARNING, null, ex);
        }
        return NotifyDescriptor.DEFAULT_OPTION;
    }

    /** Open standard dialog for installing modules including declared dependencies.
     * Shows it to the user, asks for confirmation, license acceptance, etc.
     * The whole operation requires AWT dispatch thread access (to show the dialog)
     * and blocks (until the user clicks through), so either call from AWT dispatch
     * thread directly, or be sure you hold no locks and block no progress of other
     * threads to avoid deadlocks.
     * <p>
     * Although the method is not deprecated, modules that <b>only uses PluginManager.install</b> to
     * install additional plugins should now depend on <code>autoupdate.services</code> directly,
     * and use {@link PluginInstaller#install} to reduce UI dependencies.
     * 
     * @param codenamebases the codenamebases of modules to install; must contain at least
     *             one codenamebase
     * @param alternativeOptions alternative options possibly displayed in error
     *             dialog user may choose if it is not possible to install the plugin;
     *             if chosen the option is return value of this method
     * @return <code>null</code> if all the requested modules have been successfully
     *             installed and/or activated, otherwise it returns the options user has
     *             selected in problem dialog, typically {@link NotifyDescriptor#DEFAULT_OPTION}
     *             (on esc), {@link NotifyDescriptor#CANCEL_OPTION} or
     *             any of <code>alternativeOptions</code>.
     * @throws IllegalArgumentException if the <code>codenamebases</code> is empty
     * @since 1.35
     */
    @CheckForNull
    public static Object install(@NonNull Set<String> codenamebases, @NonNull Object... alternativeOptions) {
        Parameters.notNull("cnb", codenamebases);
        Parameters.notNull("alternativeOptions", alternativeOptions);
        if (codenamebases.isEmpty()) {
            throw new IllegalArgumentException("No plugins to install");
        }

        try {
            return new ModuleInstallerSupport(alternativeOptions).installPlugins(null, codenamebases);
        } catch (OperationException ex) {
            Logger.getLogger(PluginManager.class.getName()).log(Level.WARNING, null, ex);
        }
        return NotifyDescriptor.DEFAULT_OPTION;
    }
}
