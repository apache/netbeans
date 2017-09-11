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

package org.netbeans.modules.tasklist.ui.checklist;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;

/**
 * List with checkboxes.
 */
public class CheckList extends JList {

    private static final long serialVersionUID = 1;

    private final CheckListModel model;

    /**
     * Constructs a <code>CheckList</code> that displays the elements in the
     * specified, non-<code>null</code> model. 
     * All <code>CheckList</code> constructors delegate to this one.
     *
     * @param dataModel   the data model for this list
     * @exception IllegalArgumentException   if <code>dataModel</code>
     *						is <code>null</code>
     */    
    public CheckList(CheckListModel dataModel) {
        super(dataModel);
        this.model = dataModel;
        setCellRenderer(new DefaultCheckListCellRenderer());
        Action action = new CheckAction();
        getActionMap().put("check", action); //NOI18N
        registerKeyboardAction(action, KeyStroke.getKeyStroke(' '), 
            JComponent.WHEN_FOCUSED);
        addMouseListener(
            new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JList list = (JList) e.getComponent();
                    
                    int index = list.locationToIndex(e.getPoint());
                    if (index < 0)
                        return;

                    if (e.getX() > 15)
                        return;

                    model.setChecked(index, !model.isChecked(index));
                    
                    e.consume();
                    repaint();
                }
            }
        );
    }

    /**
     * Constructs a <code>JList</code> that displays the elements in
     * the specified array.  This constructor just delegates to the
     * <code>ListModel</code> constructor.
     * 
     * @param state state of the checkboxes
     * @param  listData  the array of Objects to be loaded into the data model
     */
    public CheckList(boolean[] state, Object[] listData, String[] descriptions) {
        this(new DefaultCheckListModel(state, listData, descriptions));
    }

    @Override public String getToolTipText(MouseEvent event) {
        return model.getDescription(locationToIndex(event.getPoint()));
    }

    /* Seems more annoying than helpful:
    @Override public Point getToolTipLocation(MouseEvent event) {
        int index = locationToIndex(event.getPoint());
        Rectangle bounds = getCellBounds(index, index);
        return new Point(bounds.width, bounds.y);
    }
    */
    
    /**
     * Check/uncheck currently selected item
     */
    public static class CheckAction extends AbstractAction {

        private static final long serialVersionUID = 1;

        public void actionPerformed(ActionEvent e) {
	    JList list = (JList) e.getSource();
            int index = list.getSelectedIndex();
            if (index < 0)
                return;
            CheckListModel model = (CheckListModel) list.getModel();
            model.setChecked(index, !model.isChecked(index));
        }
    }
    
    /**
     * Sets new model
     *
     * @param m new model != null
     */
    public void setModel(CheckListModel m) {
        super.setModel(m);
    }
}
