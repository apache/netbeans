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

