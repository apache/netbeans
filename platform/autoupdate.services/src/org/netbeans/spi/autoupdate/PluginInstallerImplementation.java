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
package org.netbeans.spi.autoupdate;

import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.autoupdate.OperationException;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.UserCancelException;

/**
 * Handles out plugin installation requests. An implementation should be provided for CLI, UI or remote UI as suitabke
 * for the distribution that handles possible user interaction. As downloading the plugins often require license acceptance
 * no default implementation is provided.
 * 
 * @since 1.76
 * @author sdedic
 */
public interface PluginInstallerImplementation {
    /**
     * Attempts to install one or more plugins. Before the actual installation, some network I/O may occur
     * to e.g. download catalog of plugins or download the plugin itself. The `displayName` serves as a context
     * for possible progress messages or interactive UI. When the network I/O fails, the implementation
     * may prompt the user and retry the operation or offer other remedies (and implement them).
     * The `alternativeOptions` are other choices that should be presented to the user, and will be handled
     * by the caller. Values accepted in `alternativeOptions are the same as with {@link NotifyDescriptor#setOptions}.
     * <p>
     * After the network operations complete, the implementation should install the plugin(s). Any report from this
     * phase should be reported by throwing a {@link OperationException} that describes the failure.
     * <p>
     * On successful completion, returns {@code null}. In the case of a network error must return {@link NotifyDescriptor#CANCEL_OPTION}
     * if the user cancelled the operation, one of the `additionalOption` values if the user selected an alternative choice.
     * Retry (if offered) must be handled by the implementation.
     * <p>
     * If the user cancels the operation, the {@link UserCancelException} should be thrown to avoid dependency on Dialogs API; modules that use Dialogs API may
     * return {@link NotifyDescriptor#CANCEL_OPTION}.
     * 
     * @param codenamebases codenames of plugins to install
     * @param displayName context for possible progress or error messages
     * @param alternativeOptions alternative network failure resolution choices
     * @return {@code null} if successful, {@link NotifyDescriptor#CANCEL_OPTION} or one of `additionalOptions'.
     * @throws OperationException on a failed operation.
     * @throws UserCancelException if the user cancels the operation.
     */
    public Object install(@NonNull Set<String> codenamebases, String displayName, Lookup context, @NonNull Object... alternativeOptions)
            throws OperationException, UserCancelException;
}
