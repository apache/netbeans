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
