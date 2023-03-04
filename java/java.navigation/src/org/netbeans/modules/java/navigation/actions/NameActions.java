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
import org.openide.util.actions.Presenter;

/**
 *
 * @author Tomas Zezula
 */
public class NameActions {

    private NameActions() {
        throw new IllegalStateException();
    }


    public static Action createFullyQualifiedNameAction(@NonNull final Filters filters) {
        assert filters != null;
        return new FullyQualifiedNameAction(filters);
    }


    private static final class FullyQualifiedNameAction extends AbstractAction implements Presenter.Popup {

        @StaticResource
        private static final String ICON = "org/netbeans/modules/java/navigation/resources/fqn.png";  //NOI18N

        private final Filters filters;
        private JRadioButtonMenuItem menuItem;

        @NbBundle.Messages({
            "LBL_FullyQualifiedName=Fully Qualified Names"
        })
        public FullyQualifiedNameAction (@NonNull final Filters filters) {
            assert filters != null;
            this.filters = filters;
            putValue(Action.NAME, Bundle.LBL_FullyQualifiedName());
            putValue(Action.SMALL_ICON, ImageUtilities.loadImageIcon(ICON, false)); //NOI18N
        }

        @Override
        public void actionPerformed (ActionEvent e) {
            filters.setFqn(!filters.isFqn());
            updateMenuItem();
        }

        @Override
        @NonNull
        public final JMenuItem getPopupPresenter() {
            JMenuItem result = obtainMenuItem();
            updateMenuItem();
            return result;
        }
        
        private void updateMenuItem () {
            final JRadioButtonMenuItem mi = obtainMenuItem();
            mi.setSelected(filters.isFqn());
        }

        @NonNull
        private JRadioButtonMenuItem obtainMenuItem () {
            if (menuItem == null) {
                menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
                menuItem.setAction(this);
            }
            return menuItem;
        }
    }
}
