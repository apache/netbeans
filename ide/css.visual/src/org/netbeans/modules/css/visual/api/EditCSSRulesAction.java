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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.css.visual.CreateRulePanel;
import org.netbeans.modules.css.visual.HtmlSourceElementHandle;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @since 3.13
 * @author marekfukala
 */
@NbBundle.Messages({
    "label.create.rule=Edit CSS Rules"
})
public class EditCSSRulesAction extends AbstractAction {

    private static final String HELP_ID = "css_visual_CreateRulePanel"; //NOI18N
    
    private FileObject context;
    private FileObject targetLocation;
    private HtmlSourceElementHandle handle;
    
    private static EditCSSRulesAction instance;

    public static EditCSSRulesAction getDefault() {
        if(instance == null) {
            instance = new EditCSSRulesAction();
        }
        return instance;
    }
    
    public EditCSSRulesAction() {
        super(Bundle.label_create_rule());
        putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/css/visual/resources/newRule.png", false)); //NOI18N
        setEnabled(false);
    }
    
    public String getToolTip() {
        return Bundle.label_create_rule();
    }
    
    public void setContext(FileObject context) {
        this.context = context;
        setEnabled(context != null);
    }
    
    public void setHtmlSourceElementHandle(OpenTag openTag, Snapshot snapshot, FileObject file) {
        this.handle = new HtmlSourceElementHandle(openTag, snapshot, file);
    }
    
    public void setTargetLocation(FileObject stylesheet) {
        this.targetLocation = stylesheet;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final CreateRulePanel panel = new CreateRulePanel(context, handle);
        
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.label_create_rule(),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(e.getSource().equals(DialogDescriptor.OK_OPTION)) {
                            RequestProcessor.getDefault().post(new Runnable() {
                                @Override
                                public void run() {
                                    panel.applyChanges();
                                }
                                
                            });
                        }
                    }
                });
        
        descriptor.setHelpCtx(new HelpCtx(HELP_ID));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        
    }

}
