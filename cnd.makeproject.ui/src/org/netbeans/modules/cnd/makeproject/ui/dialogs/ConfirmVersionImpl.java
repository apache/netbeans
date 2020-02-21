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

import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ConfirmVersionFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 */
public class ConfirmVersionImpl implements ConfirmSupport.ConfirmVersion {

    @org.openide.util.lookup.ServiceProvider(service = ConfirmVersionFactory.class)
    public static final class ConfirmVersionFactoryImpl implements ConfirmVersionFactory {

        @Override
        public ConfirmSupport.ConfirmVersion create(final String dialogTitle, final String message, String autoConfirmMessage, final Runnable onConfirm) {
            Runnable warning = () -> {
                NotifyDescriptor nd = new NotifyDescriptor(message,
                        dialogTitle, NotifyDescriptor.YES_NO_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        null, NotifyDescriptor.YES_OPTION);
                Object ret = DialogDisplayer.getDefault().notify(nd);
                if (ret == NotifyDescriptor.YES_OPTION) {
                    onConfirm.run();
                }
            };
            SwingUtilities.invokeLater(warning);
            return null;
        }

        @Override
        public ConfirmSupport.ConfirmVersion createAndWait(String dialogTitle, String message, String autoConfirmMessage) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(message, dialogTitle, NotifyDescriptor.YES_NO_OPTION); // NOI18N
            if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                return null;
            }
            return new ConfirmSupport.ConfirmVersion() {
            };
        }
    }
}
