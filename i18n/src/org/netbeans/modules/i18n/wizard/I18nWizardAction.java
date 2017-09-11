/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.i18n.wizard;

import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;

/**
 * Action which runs i18n wizard.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nWizardAction extends NodeAction {

    public I18nWizardAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }
    
    /** Generated serial version UID. */
    static final long serialVersionUID = 6965968608028644524L;

    /** Weak reference to dialog. */
    private static WeakReference<Dialog> dialogWRef = new WeakReference<Dialog>(null);

    
    /** 
     * We create non-modal but not rentrant dialog. Wait until
     * previous one is closed.
     */
    protected boolean enable(Node[] activatedNodes) {

        if (Util.wizardEnabled(activatedNodes) == false) {
            return false;
        }
        
        Dialog previous = dialogWRef.get();
        if (previous == null) return true;
        return previous.isVisible() == false;
    }
    
    /** 
     * Popup non modal wizard.
     */
    protected void performAction(Node[] activatedNodes) {
        Dialog dialog = dialogWRef.get();
        
        if(dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }

	/* find out the current project from activated nodes */
	Project project = org.netbeans.modules.i18n.Util.getProjectFor(activatedNodes);
	if (project == null) return;
	  
        WizardDescriptor wizardDesc = I18nWizardDescriptor.createI18nWizardDescriptor(
            getWizardIterator(),
            new I18nWizardDescriptor.Settings(Util.createWizardSourceMap(activatedNodes), project)
        );

        initWizard(wizardDesc);
        
        dialog = DialogDisplayer.getDefault().createDialog(wizardDesc);
        dialogWRef = new WeakReference<Dialog>(dialog);
        dialog.setVisible(true);
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator<I18nWizardDescriptor.Settings> getWizardIterator() {
        List<WizardDescriptor.Panel<I18nWizardDescriptor.Settings>> panels
                = new ArrayList<WizardDescriptor.Panel<I18nWizardDescriptor.Settings>>(4);
        
        panels.add(new SourceWizardPanel.Panel());
        panels.add(new ResourceWizardPanel.Panel());
        panels.add(new AdditionalWizardPanel.Panel());
        panels.add(new HardStringWizardPanel.Panel());
        
        return new WizardDescriptor.ArrayIterator<I18nWizardDescriptor.Settings>(
            panels.toArray(new WizardDescriptor.Panel[panels.size()])
        );
    }

    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);

        List<String> contents = new ArrayList<String>(4);
        contents.add(Util.getString("TXT_SelectSourcesHelp"));          //NOI18N
        contents.add(Util.getString("TXT_SelectResourceHelp"));         //NOI18N
        contents.add(Util.getString("TXT_AdditionalHelp"));             //NOI18N
        contents.add(Util.getString("TXT_FoundStringsHelp"));           //NOI18N
        
        wizardDesc.putProperty(
            WizardDescriptor.PROP_CONTENT_DATA,
            contents.toArray(new String[contents.size()])
        ); 
        
        wizardDesc.setTitle(Util.getString("LBL_WizardTitle"));         //NOI18N
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})"));              // NOI18N

        wizardDesc.setModal(false);
    }

    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return Util.getString("LBL_WizardActionName");                  //NOI18N
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
    }

    @Override
    protected boolean asynchronous() {
      return false;
    }
    
}
