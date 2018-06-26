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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.jsf.editor;
import org.netbeans.modules.web.jsf.api.editor.JSFEditorUtilities;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.jsf.JSFConfigDataObject;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Description;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.dialogs.AddDialog;
import org.netbeans.modules.web.jsf.dialogs.AddManagedBeanDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationCaseDialog;
import org.netbeans.modules.web.jsf.dialogs.AddNavigationRuleDialog;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelUtilities;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;


/**
 *
 * @author Petr Pisl
 */
public final class JsfPopupAction extends SystemAction implements Presenter.Popup {
    
    private ArrayList actions = null;
    protected final static int MANAGED_BEAN_TYPE = 1;
    protected final static int NAVIGATION_RULE_TYPE = 2;
    
    public String getName() {
        return NbBundle.getMessage(JsfPopupAction.class, "org-netbeans-modules-web-jsf-editor-JSFPopupAction.instance"); // NOI18N
    }
    
    public void actionPerformed(ActionEvent ev) {
        // do nothing - should never be called
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public javax.swing.JMenuItem getPopupPresenter() {
        return new SubMenu(getName());
    }
    
    public class SubMenu extends JMenu {
        
        public SubMenu(String s){
            super(s);
        }
        
        /** Gets popup menu. Overrides superclass. Adds lazy menu items creation. */
        public JPopupMenu getPopupMenu() {
            JPopupMenu pm = super.getPopupMenu();
            pm.removeAll();
            pm.add(new AddNavigationRuleAction());
            pm.add(new AddNavigationCaseAction());
            pm.add(new JSeparator());
            pm.add(new AddManagedBeanAction());
            pm.pack();
            return pm;
        }
    }
    
    public static class AddManagedBeanAction extends BaseAction{
        public AddManagedBeanAction(){
            super(NbBundle.getBundle(JsfPopupAction.class).getString("add-managed-bean-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddManagedBeanDialog dialogPanel = new AddManagedBeanDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JsfPopupAction.class,"TTL_AddManagedBean"), //NOI18N
                    new HelpCtx("org.netbeans.modules.web.jsf.dialogs.AddManagedBeanDialog")); //NOI18N
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try             {
                    final FacesConfig facesConfig = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true).getRootComponent();
                    final ManagedBean bean = facesConfig.getModel().getFactory().createManagedBean();
                    
                    bean.setManagedBeanName(dialogPanel.getManagedBeanName());
                    bean.setManagedBeanClass(dialogPanel.getBeanClass());
                    bean.setManagedBeanScope(dialogPanel.getScope());
                    if (dialogPanel.getManagedBeanDescription() != null &&
                            dialogPanel.getManagedBeanDescription().trim().length() > 0){
                        Description description = facesConfig.getModel().getFactory().createDescription();
                        description.setValue(dialogPanel.getManagedBeanDescription());
                        bean.addDescription(description);
                    }
                    JSFConfigModelUtilities.doInTransaction(facesConfig.getModel(), new Runnable() {
                        @Override
                        public void run() {
                            facesConfig.addManagedBean(bean);
                        }
                    });
                    JSFConfigModelUtilities.saveChanges(facesConfig.getModel());
                    target.setCaretPosition(bean.findPosition());
                } catch (IllegalStateException ex) {
                    java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                            ex.getMessage(),
                            ex);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                            ex.getMessage(),
                            ex);
                }
            }
        }
    }
    
    public static class AddNavigationRuleAction extends BaseAction{
        public AddNavigationRuleAction(){
            super(NbBundle.getBundle(JsfPopupAction.class).getString("add-navigation-rule-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationRuleDialog dialogPanel = new AddNavigationRuleDialog(data);
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JsfPopupAction.class,"TTL_AddNavigationRule"), //NOI18N
                    new HelpCtx("org.netbeans.modules.web.jsf.dialogs.AddNavigationRuleDialog")); //NOI18N
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true);
                    final FacesConfig facesConfig = model.getRootComponent();
                    final NavigationRule rule = facesConfig.getModel().getFactory().createNavigationRule();
                    String descriptionText = dialogPanel.getDescription();
                    if (descriptionText != null && descriptionText.trim().length() > 0){
                        Description description = facesConfig.getModel().getFactory().createDescription();
                        description.setValue(descriptionText);
                        rule.addDescription(description);
                    }
                    if (dialogPanel.getFromView() != null && dialogPanel.getFromView().trim().length() > 0){
                        rule.setFromViewId(dialogPanel.getFromView());
                    }
                    JSFConfigModelUtilities.doInTransaction(facesConfig.getModel(), new Runnable() {
                        @Override
                        public void run() {
                            facesConfig.addNavigationRule(rule);
                        }
                    });
                    JSFConfigModelUtilities.saveChanges(facesConfig.getModel());
                    target.setCaretPosition(rule.findPosition());
                } catch (IllegalStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (java.io.IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
    
    public static class AddNavigationCaseAction extends BaseAction{
        public AddNavigationCaseAction(){
            super(NbBundle.getBundle(JsfPopupAction.class).getString("add-navigation-case-action")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            BaseDocument doc = (BaseDocument)target.getDocument();
            JSFConfigDataObject data = (JSFConfigDataObject)NbEditorUtilities.getDataObject(doc);
            AddNavigationCaseDialog dialogPanel = new AddNavigationCaseDialog(data,
                    JSFEditorUtilities.getNavigationRule((BaseDocument)doc, target.getCaretPosition()));
            AddDialog dialog = new AddDialog(dialogPanel,
                    NbBundle.getMessage(JsfPopupAction.class,"TTL_AddNavigationCase"),    //NOI18N
                    new HelpCtx("org.netbeans.modules.web.jsf.dialogs.AddNavigationCaseDialog"));  //NOI18N
            dialog.disableAdd(); // disable Add button
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            if (dialog.getValue().equals(dialog.ADD_OPTION)) {
                try {
                    final FacesConfig facesConfig = ConfigurationUtils.getConfigModel(data.getPrimaryFile(),true).getRootComponent();
                    boolean newRule = false;
                    String fromView = dialogPanel.getRule();
                    if (fromView.length() == 0){
                        fromView = null;
                    }
                    final NavigationRule[] rule = new NavigationRule[1];
                    rule[0] = JSFConfigUtilities.findNavigationRule(data, fromView);
                    if (rule[0] == null){
                        rule[0] = facesConfig.getModel().getFactory().createNavigationRule();
                        rule[0].setFromViewId(fromView);
                        JSFConfigModelUtilities.doInTransaction(facesConfig.getModel(), new Runnable() {
                            @Override
                            public void run() {
                                facesConfig.addNavigationRule(rule[0]);
                            }
                        });
                        newRule = true;
                    }
                    final NavigationCase nCase = facesConfig.getModel().getFactory().createNavigationCase();
                    if(dialogPanel.getFromAction() != null && !dialogPanel.getFromAction().equals(""))      //NOI18N
                        nCase.setFromAction(dialogPanel.getFromAction());
                    if(dialogPanel.getFromOutcome() != null && !dialogPanel.getFromOutcome().equals(""))    //NOI18N
                        nCase.setFromOutcome(dialogPanel.getFromOutcome());
                    nCase.setRedirected(dialogPanel.isRedirect());
                    nCase.setToViewId(dialogPanel.getToView());
                    if(dialogPanel.getDescription() != null && !dialogPanel.getDescription().equals("")) {   //NOI18N
                        Description description = nCase.getModel().getFactory().createDescription();
                        description.setValue(dialogPanel.getDescription());
                        nCase.addDescription(description);
                    }
                    JSFConfigModelUtilities.doInTransaction(facesConfig.getModel(), new Runnable() {
                        @Override
                        public void run() {
                            rule[0].addNavigationCase(nCase);
                        }
                    });
                    JSFConfigModelUtilities.saveChanges(facesConfig.getModel());

                    if (newRule) {
                        target.setCaretPosition(rule[0].findPosition());    //NOI18N
                    } else {
                        target.setCaretPosition(nCase.findPosition());
                    }
                } catch (IllegalStateException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (java.io.IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
