/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
