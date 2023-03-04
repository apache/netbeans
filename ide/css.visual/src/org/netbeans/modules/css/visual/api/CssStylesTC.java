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
package org.netbeans.modules.css.visual.api;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JComponent;
import org.netbeans.modules.css.visual.CssStylesPanel;
import org.netbeans.modules.css.visual.CssStylesTCController;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * CSS Rule Editor {@link TopComponent}.
 *
 * One may manipulate the content of this component by obtaining an instance of
 * {@link RuleEditorController} via {@link #getRuleEditorController() } method.
 *
 * @see RuleEditorController
 *
 * @author mfukala@netbeans.org
 */
@TopComponent.Description(
        preferredID = CssStylesTC.ID,
persistenceType = TopComponent.PERSISTENCE_ALWAYS,
iconBase = "org/netbeans/modules/css/visual/resources/css_rule.png") // NOI18N
@TopComponent.Registration(
        mode = CssStylesTCController.CSS_TC_MODE, // NOI18N
openAtStartup = false)
@ActionID(
        category = "Window", // NOI18N
id = "org.netbeans.modules.css.visual.api.CssStylesTC.OpenAction") // NOI18N
@ActionReference(
        path = "Menu/Window/Web", // NOI18N
position = 200)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_CssStylesAction", // NOI18N
preferredID = CssStylesTC.ID)
@NbBundle.Messages({
    "CTL_CssStylesAction=CSS &Styles", // NOI18N
    "CTL_CssStylesTC.title={0}CSS Styles", // NOI18N
    "HINT_CssStylesTC=This window shows matched style rules of an element and allows to edit them." // NOI18N
})
public final class CssStylesTC extends TopComponent {
    
    /**
     * Help ID of this TopComponent.
     */
    private static final String HELP_ID = "css_visual_CssStylesTC"; //NOI18N
    
    /**
     * TopComponent ID.
     */
    public static final String ID = "CssStylesTC"; // NOI18N
    
    /**
     * Panel shown in this {@code TopComponent}.
     */
    private final CssStylesPanel cssStylesPanel;
    
    public CssStylesTC() {
        setLayout(new BorderLayout());
        cssStylesPanel = new CssStylesPanel();        
        associateLookup(cssStylesPanel.getLookup());
        setContext(null);
        setToolTipText(Bundle.HINT_CssStylesTC());
        setTitle(null);
    }

    private void setContent(JComponent component) {
        if(component.getParent() == null) {
            //not shown
            removeAll();
            add(component, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    /**
     * Sets context file to the CSSStylesTC.
     * Must be called in EDT
     */
    public void setContext(FileObject file) {
        assert EventQueue.isDispatchThread();
        
        setContent(cssStylesPanel);

        cssStylesPanel.setContext(file);
        
        //hack - set context to the create rule action
        EditCSSRulesAction.getDefault().setContext(file);
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        getRuleEditorController().getRuleEditorPanel().componentDeactivated();
    }
    
    /**
     * Returns the default {@link RuleEditorController} associated with this
     * rule editor top component.
     */
    public RuleEditorController getRuleEditorController() {
        return cssStylesPanel.getRuleEditorController();
    }

    /**
     * Sets the title of this view.
     * 
     * @param title new title of this view.
     */
    public void setTitle(String title) {
        String name = (title == null) ? "" : (title + " - "); // NOI18N
        setName(Bundle.CTL_CssStylesTC_title(name));
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID);
    }
    
}
