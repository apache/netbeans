/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.classview.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.util.CharSequences;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class MoreDeclarations extends AbstractAction implements Presenter.Popup {
    private static final String PROP_DECLARATION = "prop_declaration"; // NOI18N
    private Collection<? extends CsmOffsetableDeclaration> arr;
    public MoreDeclarations(Collection<? extends CsmOffsetableDeclaration> arr) {
        this.arr = arr;
    }
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu result = new JMenu();
        List<ItemWrapper> list = new ArrayList<ItemWrapper>();
        for (CsmOffsetableDeclaration decl : arr) {
            list.add(new ItemWrapper(decl));
        }
        Collections.sort(list);
        result.setText(I18n.getMessage("LBL_MoreDeclarations")); //NOI18N
        if (list.size() < 36) {
            for (ItemWrapper i : list) {
                result.add(createItem(i.decl));
            }
        } else {
            int n = (int)Math.ceil(Math.sqrt((double)list.size()));
            Iterator<ItemWrapper> i = list.iterator();
            while(i.hasNext()){
                JMenu current = new JMenu();
                CsmOffsetableDeclaration first = null;
                CsmOffsetableDeclaration last = null;
                for (int j = 0; j < n && i.hasNext(); j++){
                    CsmOffsetableDeclaration decl = i.next().decl;
                    if (j == 0) {
                        first = decl;
                    } else {
                        last = decl;
                    }
                    current.add(createItem(decl));
                }
                if (first != null && last != null){
                    current.setText(first.getContainingFile().getName()+" ... "+last.getContainingFile().getName()); // NOI18N
                    result.add(current);
                } else if (first != null) {
                    result.add(createItem(first));
                }
            }
        }
        return result;
    }

    private JMenuItem createItem(final CsmOffsetableDeclaration decl) {
        JMenuItem item = new JMenuItem();
        CsmFile file = decl.getContainingFile();
        item.setText(file.getName().toString());
        item.putClientProperty(PROP_DECLARATION, decl);
        item.addActionListener(this);
        return item;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        JMenuItem item = (JMenuItem) ae.getSource();
        CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) item.getClientProperty(PROP_DECLARATION);
        GoToDeclarationAction action = new GoToDeclarationAction(decl, true);
        action.actionPerformed(null);
    }
    
    private static class ItemWrapper implements Comparable<ItemWrapper>{
        private CharSequence name;
        private CsmOffsetableDeclaration decl;
        private ItemWrapper(CsmOffsetableDeclaration decl){
            this.decl = decl;
            name = decl.getContainingFile().getName();
        }
        @Override
        public int compareTo(MoreDeclarations.ItemWrapper o) {
            return CharSequences.comparator().compare(name,o.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ItemWrapper other = (ItemWrapper) obj;
            if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
    }
}
