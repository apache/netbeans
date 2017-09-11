/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.wizard.utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.swing.NbiDialog;
import org.netbeans.installer.utils.helper.swing.NbiLabel;
import org.netbeans.installer.utils.helper.swing.NbiPanel;
import org.netbeans.installer.utils.helper.swing.NbiScrollPane;
import org.netbeans.installer.utils.helper.swing.NbiTextPane;

public class InstallationLogDialog extends NbiDialog {
    private NbiTextPane   logPane;
    private NbiPanel      logPanel;
    private NbiScrollPane logScrollPane;
    
    private NbiLabel errorLabel;
    
    private File logFile;
    private static final String MSG_LOADING_LOGFILE_KEY =
            "ILD.loading.logfile";//NOI18N
    private static final String ERROR_READING_LOGFILE_KEY =
            "ILD.error.reading.log";//NOI18N
    private static final String ERROR_LOG_CONTENTS =
            "ILD.error.log.contents";//NOI18N
    public InstallationLogDialog() {
        super();
        
        initComponents();
        initialize();
    }
    
    private void initialize() {
        logFile = LogManager.getLogFile();
        
        setTitle(logFile.getAbsolutePath());
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        
        logPane = new NbiTextPane();
        logPane.setFont(new Font("Monospaced", 
                logPane.getFont().getStyle(), logPane.getFont().getSize()));
        
        logPanel = new NbiPanel();
        logPanel.setLayout(new BorderLayout());
        logPanel.add(logPane, BorderLayout.CENTER);
        
        logScrollPane = new NbiScrollPane(logPanel);
        logScrollPane.setViewportBorder(new EmptyBorder(new Insets(5, 5, 5, 5)));
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        errorLabel = new NbiLabel();
        
        add(logScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, 
                new Insets(11, 11, 11, 11), 0, 0));
        add(errorLabel,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, 
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(11, 11, 11, 11), 0, 0));
    }
    
    public void loadLogFile() {
        try {
            logScrollPane.setVisible(false);
            errorLabel.setVisible(true);
            
            errorLabel.setText(ResourceUtils.getString(
                    InstallationLogDialog.class,
                    MSG_LOADING_LOGFILE_KEY));
            logPane.setText(FileUtils.readFile(logFile));
            logPane.setCaretPosition(0);
            
            logScrollPane.setVisible(true);
            errorLabel.setVisible(false);
        }  catch (IOException e) {
            ErrorManager.notify(ErrorLevel.WARNING,
                    ResourceUtils.getString(InstallationLogDialog.class,
                    ERROR_READING_LOGFILE_KEY,
                    logFile),
                    e);
            
            errorLabel.setText(ResourceUtils.getString(
                    InstallationLogDialog.class, ERROR_LOG_CONTENTS));
            
            logScrollPane.setVisible(false);
            errorLabel.setVisible(true);
        }
    }
}
