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
package org.netbeans.api.autoupdate;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.autoupdate.PluginInstallerImplementation;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

/**
 * Allows to programmatically install plugins. It serves as a frontend API to UI/CLI
 * whichever is installed - other modules need to provide the actual installation provider to handle the requests. 
 * No default implementation for the installation provider is present by default unless additional modules
 * like {@code autoupdate.ui} is installed. If no providers are registered, the installation fails as if
 * the user had rejected the operation using "Cancel" button.
 * 
 * @since 1.76
 * @author sdedic
 */
public final class PluginInstaller {
    private static final Logger LOG = Logger.getLogger(PluginInstaller.class.getName());
    
    private static PluginInstaller INSTANCE;
    
    public static PluginInstaller getDefault() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        // no sync needed, instance is stateless.
        return INSTANCE = new PluginInstaller();
    }
    
    /**
     * Installs a single module. When reporting progress or messages, uses the supplied `displayName`; use {@code null}
     * to use a generic description possibly based on the module name/codename. The method converts the {@link UserCancelException}
     * thrown by the actual implementation into {@link NotifyDescriptor#CANCEL_OPTION} for compatibility with older code.
     * Otherwise, see {@link #install(java.lang.String, java.lang.String, org.openide.util.Lookup, java.lang.Object...)}.
     * 
     * @param codenamebase codename base to install
     * @param displayName title/heading for messages
     * @param context additional context passed to the process
     * @param alternativeOptions possible failure resolution choices
     * @return null on success, {@link NotifyDescriptor#CANCEL_OPTION} if the operation is rejected (i.e. user cancel) or one of
     * "alternativeOptions".
     */
    public Object install(@NonNull String codenamebase, @NonNull String displayName,
            Lookup context, @NonNull Object... alternativeOptions) {
        try {
            return install(Collections.singleton(codenamebase), displayName, context, alternativeOptions);
        } catch (UserCancelException ex) {
            LOG.log(Level.FINE, "User cancelled", ex);
            return NotifyDescriptor.CANCEL_OPTION;
        } catch (OperationException ex) {
            LOG.log(Level.WARNING, "Exception during install of " + codenamebase, ex);
            return NotifyDescriptor.CANCEL_OPTION;
        }
    }
    
    /**
     * Installs a single module. Uses some default title for possible progress indication during module search 
     * and installation. Does not support alternative options. The method converts the {@link UserCancelException}
     * thrown by the actual implementation into {@link NotifyDescriptor#CANCEL_OPTION} for compatibility with older code.
     * Otherwise, see {@link #install(java.lang.String, java.lang.String, org.openide.util.Lookup, java.lang.Object...)}.
     * 
     * @param codenamebase codename base of the module to install
     * @return null on success, {@link NotifyDescriptor#CANCEL_OPTION} if the operation is rejected (i.e. user cancel).
     */
    public Object install(@NonNull String codenamebase) {
        return install(codenamebase, null, null);
    }
    
    /**
     * Attempts to install one or more plugins, specified by their codebases. During module + dependency search and download
     * some interim progress messages can be displayed - the `displayName` will be used as a title, heading or otherwise provide
     * informative context for those messages. If the display name is not provided, some generic title can be used (i.e. searching,
     * installing).
     * <p>
     * If fetching the plugin catalog data fails on I/O, the user can get a choice to 
     * retry the process or cancel the operation. `alternativeOptions` are presented as other alternative solutions. If the user
     * chooses to cancel, operation ends with {@link NotifyDescriptor#CANCEL_OPTION}. The user can choose to retry the operation.
     * The user can also select one of the `alternativeOptions`, which will terminate the operation with the chosen option as a result.
     * During the download, the operation can be cancelled resulting in {@link NotifyDescriptor#CANCEL}.
     * <p>
     * If the operation completes successfully, the method returns {@code null}.
     * <p>
     * In the case that no installation provider is installed, the implementation throws {@link UserCancelException} as if the operation
     * was rejected by the user.
     * 
     * @param codenamebases codename bases of modules to install. The modules will be installed in no particular order.
     * @param displayName 
     * @param alternativeOptions
     * @return {@code null} on success. {@link NotifyDescriptor#CANCEL_OPTION} on cancel, or one of `alternativeOptions` on failure
     * according to user's choice.
     * @throws OperationException if the download or installation / enable operation fails.
     * @throws UserCancelException if the operation is rejected.
     */
    public Object install(@NonNull Set<String> codenamebases, String displayName, Lookup context, Object... alternativeOptions) throws OperationException, UserCancelException {
        PluginInstallerImplementation impl = Lookup.getDefault().lookup(PluginInstallerImplementation.class);
        if (impl == null) {
            throw new UserCancelException();
        }
        Object o = impl.install(codenamebases, displayName, context == null ? Lookup.EMPTY : context, alternativeOptions);
        if (o == NotifyDescriptor.CLOSED_OPTION) {
            return NotifyDescriptor.CANCEL_OPTION;
        } else {
            // even OK_OPTION would display in case of some failure.
            return o;
        }
    }
}
