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
