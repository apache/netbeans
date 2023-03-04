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
import javax.swing.tree.TreeNode;
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
        Enumeration<TreeNode> enumeration = root.preorderEnumeration();
        
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
