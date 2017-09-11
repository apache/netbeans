/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
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
