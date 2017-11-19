/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.hints;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.ImplementationProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 *@author Jan Lahoda
 */
public class FixAction extends AbstractAction {
    
    public FixAction() {
        putValue(NAME, NbBundle.getMessage(FixAction.class, "NM_FixAction"));
        putValue("supported-annotation-types", new String[] {
            "org-netbeans-spi-editor-hints-parser_annotation_err_fixable",
            "org-netbeans-spi-editor-hints-parser_annotation_warn_fixable",
            "org-netbeans-spi-editor-hints-parser_annotation_verifier_fixable",
            "org-netbeans-spi-editor-hints-parser_annotation_hint_fixable"
        });
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!HintsUI.getDefault().invokeDefaultAction(true)) {
            Object source = e.getSource();
            
            if (!(source instanceof JTextComponent)) {
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(FixAction.class, "ERR_NoFixableError"));
                return ; //probably right click, Fixable Error -> Fix Action
            }
            
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) source);
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this)
                nextAction++;
            
            nextAction++;
            
            if (actions.length > nextAction) {
                Action a = actions[nextAction]; //TODO - create GUI chooser
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        TopComponent activetc = TopComponent.getRegistry().getActivated();
        if (activetc instanceof CloneableEditorSupport.Pane) {
            return true;
        }
        return false;
    }
}

