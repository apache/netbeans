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

package org.netbeans.modules.cnd.navigation.classhierarchy;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.navigation.services.HierarchyModel;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Hierarchy Tree node.
 */
public final class HierarchyNode extends AbstractCsmNode{
    private HierarchyModel.Node object;
    private CsmProject project;
    private HierarchyModel model;
    private CharSequence uin;

    public HierarchyNode(HierarchyModel.Node element, HierarchyModel model, HierarchyChildren parent) {
        this(element, new HierarchyChildren(element, model, parent), model, false);
    }

    public HierarchyNode(HierarchyModel.Node element, Children children, HierarchyModel model, boolean recursion) {
        super(children);
        if (recursion) {
            setName(element.getDisplayName()+" "+getString("CTL_Recuesion")); // NOI18N
        } else {
            setName(element.getDisplayName().toString());
        }
        object = element;
        this.model = model;
        uin = object.getDeclaration().getUniqueName();
        project = object.getDeclaration().getContainingFile().getProject();
    }

    @Override
    public Image getIcon(int param) {
        ImageIcon icon = CsmImageLoader.getIcon(object.getDeclaration());
        if (object.isSpecialization()) {
            Image badge = ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/specialization-badge.png");
            return ImageUtilities.mergeImages(icon.getImage(), badge, 16, 0);            
        }
        return icon.getImage();
    }

    @Override
    public CsmOffsetableDeclaration getCsmObject() {
        if (!object.getDeclaration().isValid()) {
            CsmDeclaration d = project.findDeclaration(uin);
            if (d instanceof CsmClass) {
                object = new HierarchyModel.Node((CsmClass) d, object.isSpecialization());
            }
        }
        return object.getDeclaration();
    }

    @Override
    public Action getPreferredAction() {
        CsmOffsetableDeclaration obj = getCsmObject();
        if (obj.isValid()) {
            if (CsmKindUtilities.isOffsetable(obj)){
                return new GoToClassAction((CsmOffsetable)obj, model.getCloseWindowAction());
            }
        }
        return new EmptyAction();
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (!(action instanceof EmptyAction)){
            List<Action> list = new ArrayList<Action>();
            list.add(action);
            list.add(null);
            for (Action a : model.getDefaultActions()){
                list.add(a);
            }
            return list.toArray(new Action[list.size()]);
        }
        return model.getDefaultActions();
    }

    private String getString(String key) {
        return NbBundle.getMessage(HierarchyNode.class, key);
    }

    static class EmptyAction implements Action{
        @Override
        public Object getValue(String key) {
            return null;
        }
        @Override
        public void putValue(String key, Object value) {
        }
        @Override
        public void setEnabled(boolean b) {
        }
        @Override
        public boolean isEnabled() {
            return true;
        }
        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
}
