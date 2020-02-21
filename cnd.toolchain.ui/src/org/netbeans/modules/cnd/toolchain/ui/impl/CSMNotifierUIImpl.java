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
package org.netbeans.modules.cnd.toolchain.ui.impl;

import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.spi.toolchain.CSMNotifier;
import org.netbeans.modules.cnd.toolchain.ui.compilerset.FixCodeAssistancePanel;
import org.netbeans.modules.cnd.toolchain.ui.compilerset.NoCompilersPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = CSMNotifier.class, position = 100)
public class CSMNotifierUIImpl extends CSMNotifier {

    @Override
    public void notifyNoCompilerSet(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DialogDescriptor dialogDescriptor = new DialogDescriptor(
                        new NoCompilersPanel(),
                        message,
                        true,
                        new Object[]{DialogDescriptor.OK_OPTION},
                        DialogDescriptor.OK_OPTION,
                        DialogDescriptor.BOTTOM_ALIGN,
                        null,
                        null);
                DialogDisplayer.getDefault().notify(dialogDescriptor);
            }
        });
    }

    @Override
    public void showNotification(Map<Tool, List<List<String>>> needReset, CompilerSetManager csm) {
        FixCodeAssistancePanel.showNotification(needReset, csm);
    }

}
