/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.search.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.editor.search.EditorFindSupport;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.openide.util.WeakListeners;

// suspending the use of EditorActionRegistration due to #167063

//    @EditorActionRegistration(name = BaseKit.toggleHighlightSearchAction,
@EditorActionRegistration(name = BaseKit.toggleHighlightSearchAction, iconResource = "org/netbeans/modules/editor/search/resources/toggle_highlight.png") // NOI18N
public class ToggleHighlightSearchAction extends AbstractEditorAction {
    static final long serialVersionUID = 4603809175771743200L;


    public ToggleHighlightSearchAction() {
        super();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        JToggleButton b = new MyGaGaButton();
        b.setModel(new HighlightButtonModel());
        b.setAction(this);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        if (target != null) {
            Boolean cur = (Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
            if (cur == null || cur.booleanValue() == false) {
                cur = Boolean.TRUE;
            } else {
                cur = Boolean.FALSE;
            }
            EditorFindSupport.getInstance().putFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH, cur);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        JToggleButton b = new MyGaGaButton();
        b.setModel(new HighlightButtonModel());
        b.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        b.setAction(this);
        return b;
    }

    private static final class HighlightButtonModel extends JToggleButton.ToggleButtonModel implements PropertyChangeListener {

        public HighlightButtonModel() {
            EditorFindSupport efs = EditorFindSupport.getInstance();
            efs.addPropertyChangeListener(WeakListeners.propertyChange(this, efs));
            propertyChange(null);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt == null || evt.getPropertyName() == null || evt.getPropertyName().equals(EditorFindSupport.FIND_HIGHLIGHT_SEARCH)) {
                Boolean value = (Boolean) EditorFindSupport.getInstance().getFindProperty(EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
                setSelected(value == null ? false : value.booleanValue());
            }
        }
    } // End of HighlightButtonModel class

    private static final class MyGaGaButton extends JToggleButton implements ChangeListener {

        public MyGaGaButton() {
        }

        @Override
        public void setModel(ButtonModel model) {
            ButtonModel oldModel = getModel();
            if (oldModel != null) {
                oldModel.removeChangeListener(this);
            }
            super.setModel(model);
            ButtonModel newModel = getModel();
            if (newModel != null) {
                newModel.addChangeListener(this);
            }
            stateChanged(null);
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            boolean selected = isSelected();
            super.setContentAreaFilled(selected);
            super.setBorderPainted(selected);
        }

        @Override
        public void setBorderPainted(boolean arg0) {
            if (!isSelected()) {
                super.setBorderPainted(arg0);
            }
        }

        @Override
        public void setContentAreaFilled(boolean arg0) {
            if (!isSelected()) {
                super.setContentAreaFilled(arg0);
            }
        }
    }

} // End of ToggleHighlightSearchAction class
