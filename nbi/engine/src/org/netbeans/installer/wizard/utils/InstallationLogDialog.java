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
