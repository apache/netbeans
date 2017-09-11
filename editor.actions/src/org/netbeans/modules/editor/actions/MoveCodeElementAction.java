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
package org.netbeans.modules.editor.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorActionNames;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.EditorUtilities;
import org.netbeans.editor.BaseKit;
import org.netbeans.spi.editor.AbstractEditorAction;

/**
 * Move entire code elements (statements and class members) up or down.
 *
 * @author Dusan Balek
 */
@EditorActionRegistrations({
    @EditorActionRegistration(name = EditorActionNames.moveCodeElementUp,
                              menuPath = "Source",
                              menuPosition = 840,
                              menuText = "#" + EditorActionNames.moveCodeElementUp + "_menu_text"),
    @EditorActionRegistration(name = EditorActionNames.moveCodeElementDown,
                              menuPath = "Source",
                              menuPosition = 860,
                              menuText = "#" + EditorActionNames.moveCodeElementDown + "_menu_text")
})
public class MoveCodeElementAction extends AbstractEditorAction {

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent component) {
        if (component != null) {
            String actionName = EditorActionNames.moveCodeElementUp.equals(actionName())
                    ? BaseKit.moveSelectionElseLineUpAction
                    : BaseKit.moveSelectionElseLineDownAction;
            Action action = EditorUtilities.getAction(component.getUI().getEditorKit(component), actionName);
            if (action != null) {
                action.actionPerformed(evt);
                return;
            }
        }
        Toolkit.getDefaultToolkit().beep();
    }
}
