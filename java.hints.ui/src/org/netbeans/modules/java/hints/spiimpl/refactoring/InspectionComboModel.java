/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.spiimpl.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.spiimpl.options.HintsPanelLogic.HintCategory;

/**
 *
 * @author Jan Becicka
 */
public final class InspectionComboModel extends AbstractListModel implements ComboBoxModel {
    private Object selected;
    private final ArrayList<Object> hintsList;

    public InspectionComboModel(Collection<? extends HintMetadata> hints) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) constructTM(hints, false).getRoot();
        Enumeration enumeration = root.preorderEnumeration();
        
        hintsList = new ArrayList<Object>();
        while (enumeration.hasMoreElements()) {
            Object userObject = ((DefaultMutableTreeNode) enumeration.nextElement()).getUserObject();
            if (userObject!=null)
                hintsList.add(userObject);
        }
        if (getSize() > 0) {
            selected = getElementAt(Math.min(getSize(), 1));
        }
    }
    
    @Override
    public int getSize() {
        return hintsList.size();
    }
    

    @Override
    public Object getElementAt(int i) {
        return hintsList.get(i);
    }

    @Override
    public void setSelectedItem(Object o) {
        selected = o;
        fireContentsChanged(this, -1, -1);
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }
 
    /**
     * TODO: copy/paste from HintsPanel 
     */
    private DefaultTreeModel constructTM(Collection<? extends HintMetadata> metadata, boolean allHints) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        Map<HintCategory, Collection<HintMetadata>> cat2Hints = new TreeMap<HintCategory, Collection<HintMetadata>>(new Comparator<HintCategory>() {
            @Override
            public int compare(HintCategory o1, HintCategory o2) {
                return o1.displayName.compareToIgnoreCase(o2.displayName);
            }
        });
        Map<String, HintCategory> cat2CatDesc =  new HashMap<String, HintCategory>();

        for (HintMetadata m : metadata) {
            if (m.options.contains(Options.NON_GUI)) continue;
            if (m.options.contains(Options.QUERY)) continue;

            HintCategory cat = cat2CatDesc.get(m.category);

            if (cat == null) {
                cat2CatDesc.put(m.category, cat = new HintCategory(m.category));
            }
            
            Collection<HintMetadata> catNode = cat2Hints.get(cat);

            if (catNode == null) {
                cat2Hints.put(cat, catNode = new TreeSet<HintMetadata>(new Comparator<HintMetadata>() {
                    public int compare(HintMetadata o1, HintMetadata o2) {
                        return o1.displayName.compareToIgnoreCase(o2.displayName);
                    }
                }));
            }

            catNode.add(m);
        }

        for (Entry<HintCategory, Collection<HintMetadata>> e : cat2Hints.entrySet()) {
            DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(e.getKey());

            for (HintMetadata hm : e.getValue()) {
                DefaultMutableTreeNode hmNode = new DefaultMutableTreeNode(hm);

                catNode.add(hmNode);
                //hint2Path.put(hm, new TreePath(new Object[] {root, catNode, hmNode}));
            }

            root.add(catNode);
        }

        //if (allHints) 
        //root.add(extraNode);
        
        return new DefaultTreeModel(root);
    }    
}
