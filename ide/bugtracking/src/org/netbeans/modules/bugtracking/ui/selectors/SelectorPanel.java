/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.ui.selectors;

import java.util.Arrays;
import java.util.MissingResourceException;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public class SelectorPanel {

    private final RepositorySelectorBuilder builder = new RepositorySelectorBuilder();
    private final String comboLabelText
            = NbBundle.getMessage(SelectorPanel.class,
                                  "SelectorPanel.connectorLabel.text"); //NOI18N

    boolean create() {
        String title = createCreateDescriptor();
        DialogDescriptor dd = builder.createDialogDescriptor(title);
        return DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
    }

    boolean edit(RepositoryImpl repository, String errorMessage) {
        DialogDescriptor dd = createEditDescriptor(repository, errorMessage);
        return DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
    }

    private String createCreateDescriptor() throws MissingResourceException {
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_CreateTitle"); //NOI18N
        builder.setLabelText(comboLabelText);
        builder.setBugtrackingConnectorDisplayFormat("{0}"); //NOI18N
        return title;
    }

    private DialogDescriptor createEditDescriptor(RepositoryImpl repository, String errorMessage) throws MissingResourceException {
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_EditTitle"); //NOI18N
        builder.setLabelVisible(false);
        builder.setComboBoxVisible(false);
        builder.setPreselectedRepository(repository);
        builder.setInitialErrorMessage(errorMessage);
        DialogDescriptor dd = builder.createDialogDescriptor(title);
        return dd;
    }

    RepositoryImpl getRepository() {
        return builder.getSelectedRepository();
    }
    
    void setConnectors(DelegatingConnector[] connectors) {
        Arrays.sort(connectors, new ConnectorComparator());
        builder.setBugtrackingConnectors(connectors);
    }    
}
