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
package org.netbeans.modules.java.navigation.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.java.navigation.base.Filters;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Tomas Zezula
 */
public class SortActions {
    private SortActions() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    @NonNull
    public static Action createSortByNameAction(@NonNull final Filters<?> filters){
        Parameters.notNull("filters", filters); //NOI18N
        return new SortByNameAction(filters);
    }


    @NonNull
    public static Action createSortBySourceAction(@NonNull final Filters<?> filters){
        Parameters.notNull("filters", filters); //NOI18N
        return new SortBySourceAction(filters);
    }


    private abstract static class BaseSortAction extends AbstractAction implements Presenter.Popup {

        public static final String SELECTED = "selected";

        protected final Filters<?> filters;
        private JRadioButtonMenuItem menuItem;

        /** Creates a new instance of SortByNameAction */
        public BaseSortAction (@NonNull final Filters<?> filters) {
            assert filters != null;
            this.filters = filters;
        }

        @Override
        @NonNull
        public final JMenuItem getPopupPresenter() {
            JMenuItem result = obtainMenuItem();
            updateMenuItem();
            return result;
        }

        @NonNull
        protected final JRadioButtonMenuItem obtainMenuItem () {
            if (menuItem == null) {
                menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
                menuItem.setAction(this);
            }
            return menuItem;
        }

        protected abstract void updateMenuItem();
    }


    private static final class SortByNameAction extends BaseSortAction {

        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/navigation/resources/sortAlpha.png";  //NOI18N

        public SortByNameAction (@NonNull final Filters<?> filters) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortByNameAction.class, "LBL_SortByName")); //NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false));
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(false);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(!filters.isNaturalSort());
        }
    }


    private static final class SortBySourceAction extends BaseSortAction {

        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/navigation/resources/sortPosition.png";  //NOI18N

        public SortBySourceAction (@NonNull final Filters<?> filters ) {
            super(filters);
            putValue(Action.NAME, NbBundle.getMessage(SortBySourceAction.class, "LBL_SortBySource")); //NOI18N
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false)); //NOI18N
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            filters.setNaturalSort(true);
            updateMenuItem();
        }

        @Override
        protected void updateMenuItem () {
            JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isNaturalSort());
        }
    }
}
