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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
