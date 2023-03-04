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

package org.netbeans.modules.notifications.checklist;

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
