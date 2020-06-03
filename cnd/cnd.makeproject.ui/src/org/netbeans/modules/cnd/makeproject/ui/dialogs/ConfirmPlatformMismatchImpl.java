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
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ConfirmPlatformMismatch;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport.ConfirmPlatformMismatchFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 */
public class ConfirmPlatformMismatchImpl implements ConfirmPlatformMismatch {

    @org.openide.util.lookup.ServiceProvider(service = ConfirmPlatformMismatchFactory.class)
    public static final class ConfirmPlatformMismatchFactoryImpl implements ConfirmPlatformMismatchFactory {

        @Override
        public ConfirmPlatformMismatch create(final String dialogTitle, final String message) {
            SwingUtilities.invokeLater(() -> {
                Object[] options = new Object[]{NotifyDescriptor.OK_OPTION};
                DialogDescriptor nd = new DialogDescriptor(new ConfigurationWarningPanel(message), dialogTitle, true, options, NotifyDescriptor.OK_OPTION, 0, null, null);
                DialogDisplayer.getDefault().notify(nd);
            });
            return null;
        }

        @Override
        public ConfirmPlatformMismatch createAndWait(String message, String autoConfirmMessage) {
            if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, NotifyDescriptor.WARNING_MESSAGE)) != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            return new ConfirmPlatformMismatch() {
            };
        }
    }

}
