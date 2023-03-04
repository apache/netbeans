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
package org.netbeans.modules.notifications.center;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.notifications.filter.FilterEditor;
import org.netbeans.modules.notifications.filter.FilterRepository;
import org.netbeans.modules.notifications.filter.NotificationFilter;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 * @author jpeska
 */
public class FiltersMenuButton extends MenuToggleButton {

    private final NotificationCenterManager notificationManager;

    /**
     * Creates a new instance of FiltersMenuButton
     */
    public FiltersMenuButton(NotificationFilter currentFilter) {
        super(ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/filter.png", false), ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/filter_rollover.png", false), 4);  //NOI18N
        notificationManager = NotificationCenterManager.getInstance();

        updateState(currentFilter, false);

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (!isSelected()) {
                    updateState(NotificationFilter.EMPTY, false);
                } else {
                    updateState(openFilterEditor(), false);
                }
            }
        });
    }

    @Override
    protected JPopupMenu getPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        fillMenu(popup, null);

        return popup;
    }

    void fillMenu(JPopupMenu popup, JMenu menu) {
        assert null != popup || null != menu;
        FilterRepository filterRep = FilterRepository.getInstance();
        NotificationFilter activeFilter = notificationManager.getActiveFilter();

        JRadioButtonMenuItem item = new JRadioButtonMenuItem(new CancelFilterAction());
        item.setSelected(NotificationFilter.EMPTY.equals(activeFilter));
        if (null == popup) {
            menu.add(item);
        } else {
            popup.add(item);
        }

        if (null == popup) {
            menu.addSeparator();
        } else {
            popup.addSeparator();
        }

        List<NotificationFilter> allFilters = filterRep.getAllFilters();
        for (NotificationFilter tf : allFilters) {
            item = new JRadioButtonMenuItem(new SetFilterAction(tf));
            item.setSelected(activeFilter.equals(tf));
            if (null == popup) {
                menu.add(item);
            } else {
                popup.add(item);
            }
        }
        if (allFilters.size() > 0) {
            if (null == popup) {
                menu.addSeparator();
            } else {
                popup.addSeparator();
            }
        }

        if (null == popup) {
            menu.add(new ManageFiltersAction());
        } else {
            popup.add(new ManageFiltersAction());
        }
    }

    private class CancelFilterAction extends AbstractAction {

        public CancelFilterAction() {
            super(NbBundle.getMessage(FiltersMenuButton.class, "LBL_CancelFilter")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateState(NotificationFilter.EMPTY, true);
        }
    }

    private class SetFilterAction extends AbstractAction {

        private final NotificationFilter filter;

        public SetFilterAction(NotificationFilter filter) {
            super(filter.getName());
            this.filter = filter;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            updateState(filter, true);
        }
    }

    private class ManageFiltersAction extends AbstractAction {

        public ManageFiltersAction() {
            super(NbBundle.getMessage(FiltersMenuButton.class, "LBL_EditFilters")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            updateState(openFilterEditor(), false);
        }
    }

    private void updateState(NotificationFilter filter, boolean saveFilters) {
        if (null == filter || NotificationFilter.EMPTY.equals(filter)) {
            setSelected(false);
            setToolTipText(NbBundle.getMessage(FiltersMenuButton.class, "HINT_SelectFilter")); //NOI18N
        } else {
            setSelected(true);
            setToolTipText(filter.getName());
        }
        FilterRepository.getInstance().setActive(filter);
        notificationManager.updateTable(true);
        if (saveFilters) {
            try {
                FilterRepository.getInstance().save();
            } catch (IOException ioE) {
                NotificationCenterManager.getLogger().log(Level.INFO, ioE.getMessage(), ioE);
            }
        }
    }

    private static NotificationFilter openFilterEditor() {
        FilterRepository filterRep = FilterRepository.getInstance();
        FilterRepository clone = (FilterRepository) filterRep.clone();
        FilterEditor fe = new FilterEditor(clone);
        if (fe.showWindow()) {
            filterRep.assign(clone);
            try {
                filterRep.save();
            } catch (IOException ioE) {
                NotificationCenterManager.getLogger().log(Level.INFO, ioE.getMessage(), ioE);
            }
        }
        return filterRep.getActive();
    }
}
