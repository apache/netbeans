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

package org.netbeans.modules.subversion.ui.wizards.urlpatternstep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.ui.browser.Browser;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.ui.browser.RepositoryPaths;
import org.netbeans.modules.subversion.ui.search.SvnSearch;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Stupka
 */
public class URLPatternStep extends AbstractStep implements DocumentListener, ActionListener, FocusListener, ItemListener {
    
    private URLPatternPanel urlPatternPanel;
    private RepositoryPaths repositoryPaths;

    public HelpCtx getHelp() {    
        return new HelpCtx(URLPatternStep.class);
    }    

    protected JComponent createComponent() {
        if (urlPatternPanel == null) {
            urlPatternPanel = new URLPatternPanel();            
            urlPatternPanel.repositoryPathTextField.getDocument().addDocumentListener(this);
            urlPatternPanel.depthComboBox.addItemListener(this);
            urlPatternPanel.depthComboBox.addFocusListener(this);
            urlPatternPanel.anyURLCheckBox.addActionListener(this);
            urlPatternPanel.useFolderRadioButton.addActionListener(this);
            urlPatternPanel.useSubfolderRadioButton.addActionListener(this);
        }                                        
        return urlPatternPanel;              
    }

    public void setup(RepositoryFile repositoryFile) {
        if(repositoryPaths == null) {                    
            repositoryPaths = 
                new RepositoryPaths(
                        repositoryFile, 
                        urlPatternPanel.repositoryPathTextField, 
                        urlPatternPanel.browseRepositoryButton,
                        null, 
                        null
                );        
            String browserPurposeMessage = org.openide.util.NbBundle.getMessage(URLPatternStep.class, "LBL_BrowserMessage");
            int browserMode = Browser.BROWSER_SHOW_FILES | Browser.BROWSER_FOLDERS_SELECTION_ONLY;
            repositoryPaths.setupBehavior(browserPurposeMessage, browserMode, Browser.BROWSER_HELP_ID_URL_PATTERN, SvnSearch.SEACRH_HELP_ID_URL_PATTERN);
        } else {
            repositoryPaths.setRepositoryFile(repositoryFile);
        }                
        urlPatternPanel.repositoryPathTextField.setText(repositoryFile.getPath());        
        validateUserInput();          
    }    
     
    protected void validateBeforeNext() {
        // do nothing
    }

    private void validateUserInput() {         
        if(urlPatternPanel.repositoryPathTextField.getText().trim().equals("")) {        
            setInvalid(new AbstractStep.WizardMessage(NbBundle.getMessage(URLPatternStep.class, "MSG_MissingFolder"), true));
            return;                        
        }        
        refreshPreview();        
        valid();
    }
    
    private void setInvalid(AbstractStep.WizardMessage msg) {
        invalid(msg);
        urlPatternPanel.previewLabel.setText(" ");
    }
    
    private void refreshPreview() {              
        urlPatternPanel.previewLabel.setText(getPattern(true));
    }   
    
    private String getGroupiefiedPath(int depth, boolean html) {
        String[] segments = urlPatternPanel.repositoryPathTextField.getText().split("/");
        StringBuffer ret = new StringBuffer();
        for(int i = 0; i < segments.length; i++) {
            if(i == depth) {
                ret.append("/(");
                if(html)ret.append("<b>");
                ret.append(segments[i]);
                if(html)ret.append("</b>");
                ret.append(")");
            } else {
                ret.append("/");
                ret.append(segments[i]);
            }                        
        }
        if(depth >= segments.length) {
            for(int i = segments.length; i <= depth; i++) {
                if(i == depth) {                    
                    ret.append("/");                    
                    if(html)ret.append("<b>");
                    ret.append("(.+?)");                    
                    if(html)ret.append("</b>");
                } else {
                    ret.append("/.*");
                }                        
            }            
        }
        ret.append("/.*");
        return ret.toString();
    }
    
    public void insertUpdate(DocumentEvent e) {        
        validateUserInput();
    }

    public void removeUpdate(DocumentEvent e) {
        validateUserInput();        
    }

    public void changedUpdate(DocumentEvent e) {        
        validateUserInput();      
    }

    public void actionPerformed(ActionEvent e) {
        validateUserInput();          
    }

    public void focusGained(FocusEvent evt) {
        if(evt.getSource() == urlPatternPanel.depthComboBox && 
           ! urlPatternPanel.useSubfolderRadioButton.isSelected()) 
        {
            urlPatternPanel.useSubfolderRadioButton.setSelected(true);
        }        
    }

    public void focusLost(FocusEvent evt) {
        
    }

    public void itemStateChanged(ItemEvent arg0) {
        validateUserInput();          
    }

    public String getPattern() {
        return getPattern(false);
    }
    
    private String getPattern(boolean html) {
        StringBuffer preview = new StringBuffer();                
        if(html) preview.append("<html>");        
        if(urlPatternPanel.anyURLCheckBox.isSelected()) {
            preview.append(".*");
        } else {
            preview.append(repositoryPaths.getRepositoryUrl().toString());
        }
        if(urlPatternPanel.useFolderRadioButton.isSelected()) {
            preview.append("/");
            if(html) preview.append("<b>");
            preview.append(urlPatternPanel.repositoryPathTextField.getText());            
            if(html) preview.append("</b>");
            preview.append("/.*");
        } else {
            String depthString = (String) urlPatternPanel.depthComboBox.getSelectedItem();
            if(depthString.equals("")) {
                preview.append(getGroupiefiedPath(0, html));
            } else {
                preview.append(getGroupiefiedPath(Integer.parseInt(depthString), html));
            }
        }        
        
        if(html) preview.append("</html>");
        return preview.toString();
    }
    
    public String getRepositoryFolder() {
        return urlPatternPanel.repositoryPathTextField.getText();
    }
    
    public boolean useName() {
        return urlPatternPanel.useFolderRadioButton.isSelected();
    }
        
}

