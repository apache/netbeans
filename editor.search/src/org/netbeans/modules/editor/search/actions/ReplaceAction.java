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
package org.netbeans.modules.editor.search.actions;

import java.awt.event.ActionEvent;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.EditorUI;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.editor.search.ReplaceBar;
import org.netbeans.modules.editor.search.SearchBar;
import org.netbeans.modules.editor.search.SearchNbEditorKit;
import org.netbeans.spi.editor.AbstractEditorAction;

public class ReplaceAction extends AbstractEditorAction {

    static final long serialVersionUID = -1;

    @EditorActionRegistration(name = SearchNbEditorKit.REPLACE_ACTION,
            menuText="#" + SearchNbEditorKit.REPLACE_ACTION + "_menu_text") // NOI18N
    public static Action create(Map<String,?> attrs) {
        return new ReplaceAction(attrs);
    }

    public ReplaceAction(Map<String, ?> attrs) {
        super(attrs);
        putValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY, org.openide.actions.ReplaceAction.class.getName());
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            if ((target instanceof JEditorPane) && ((JEditorPane) target).getEditorKit() instanceof SearchNbEditorKit) {
                target = SearchBar.getInstance().getActualTextComponent();
            }
            JPanel jp = null;
            Object clientProperty = target.getClientProperty(SearchNbEditorKit.PROP_SEARCH_CONTAINER);
            if (clientProperty instanceof JPanel) {
                jp = (JPanel) clientProperty;
            } else {
                EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(target);
                if (eui != null) {
                    JComponent comp = eui.hasExtComponent() ? eui.getExtComponent() : null;
                    if (comp != null) {
                        jp = SearchNbEditorKit.findComponent(comp, SearchNbEditorKit.SearchJPanel.class, 5);
                    }
                }
            }
            if (jp != null && target.isEditable()) {
                SearchBar searchBar = SearchBar.getInstance(target);
                jp.add(searchBar);
                jp.add(ReplaceBar.getInstance(searchBar));
                ReplaceBar.getInstance(searchBar).gainFocus(false);
                SearchNbEditorKit.makeSearchAndReplaceBarPersistent();
            }

        }
    }
}
