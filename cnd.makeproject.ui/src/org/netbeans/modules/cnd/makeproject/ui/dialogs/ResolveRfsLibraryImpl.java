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
package org.netbeans.modules.cnd.makeproject.ui.dialogs;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ResolveRfsLibraryFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
public class ResolveRfsLibraryImpl {
    @org.openide.util.lookup.ServiceProvider(service = ResolveRfsLibraryFactory.class)
    public static final class ResolveRfsLibraryFactoryImpl implements ResolveRfsLibraryFactory {

        @Override
        public void show(final ExecutionEnvironment execEnv) {
            JButton change = new JButton(NbBundle.getMessage(ResolveRfsLibraryImpl.class, "ResolveRfsLibrary.Forbid.text")); // NOI18N
            JButton close = new JButton(NbBundle.getMessage(ResolveRfsLibraryImpl.class, "ResolveRfsLibrary.Close.text")); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor(
                    NbBundle.getMessage(ResolveRfsLibraryImpl.class, "ResolveRfsLibrary.Explanation.text"), // NOI18N
                    NbBundle.getMessage(ResolveRfsLibraryImpl.class, "ResolveRfsLibrary.Title.text"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new JButton[]{change, close},
                    change
            );
            if (DialogDisplayer.getDefault().notify(d) == change) {
                SwingUtilities.invokeLater(() -> {
                    ServerListUI.showServerRecordPropertiesDialog(execEnv);
                });
            }
        }
    }
}
