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
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ForbidBuildAnalyzerFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 */
public class ForbidBuildAnalyzerImpl {
    @org.openide.util.lookup.ServiceProvider(service = ForbidBuildAnalyzerFactory.class)
    public static final class ForbidBuildAnalyzerFactoryImpl implements ForbidBuildAnalyzerFactory {
        @Override
        public void show(final Project project) {
            JButton forbid = new JButton(NbBundle.getMessage(ForbidBuildAnalyzerImpl.class, "ResolveBuildLibrary.Forbid.text")); // NOI18N
            JButton close = new JButton(NbBundle.getMessage(ForbidBuildAnalyzerImpl.class, "ResolveBuildLibrary.Close.text")); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor(
                    NbBundle.getMessage(ForbidBuildAnalyzerImpl.class, "ResolveBuildLibrary.Explanation.text"), // NOI18N
                    NbBundle.getMessage(ForbidBuildAnalyzerImpl.class, "ResolveBuildLibrary.Title.text"), // NOI18N
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new JButton[] {forbid, close},
                    forbid
            );
            if (DialogDisplayer.getDefault().notify(d) == forbid) {
                //Preferences makeProjectPref = NbPreferences.root().node("/org/netbeans/modules/cnd/makeproject"); //NOI18N
                //if (makeProjectPref != null) {
                //    makeProjectPref.putBoolean("useBuildTrace", false); //NOI18N
                //}
                SwingUtilities.invokeLater(() -> {
                    if (project != null) {
                        MakeCustomizerProvider cp = project.getLookup().lookup( MakeCustomizerProvider.class );
                        if (cp != null) {
                            ((MakeCustomizerProvider)cp).showCustomizer("CodeAssistance"); // NOI18N
                        }
                    }
                });
            }
        }
    }

}
