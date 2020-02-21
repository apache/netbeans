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

package org.netbeans.modules.cnd.navigation.includeview;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.netbeans.modules.cnd.navigation.services.IncludedModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class IncludeNode extends AbstractCsmNode {
    private CsmFile object;
    private IncludedModel model;
    private boolean isRoot = false;
    
    /** Creates a new instance of IncludeNode */
    public IncludeNode(CsmFile element, IncludedModel model, IncludedChildren parent) {
        this(element, new IncludedChildren(element,model,parent), model, false);
        isRoot = parent == null;
    }
    
    public IncludeNode(CsmFile element, Children children, IncludedModel model, boolean recursion) {
        super(children);
        if (recursion) {
            setName(element.getName()+" "+getString("CTL_Recuesion")); // NOI18N
        } else {
            setName(element.getName().toString());
        }
        this.model = model;
        object = element;
    }

    public Lookup getNodeLookup() {
        FileObject fo = object.getFileObject();
        DataObject dobj = null;
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
            }
        }
        if (fo != null && dobj != null) {
            return Lookups.fixed(fo, dobj);
        } else {
            if (fo != null) {
                return Lookups.fixed(fo);
            } else if (dobj != null) {
                return Lookups.fixed(dobj);
            }
        }
        return null;
    }

    
    @Override
    public CsmObject getCsmObject() {
        return object;
    }
    
    @Override
    public Image getIcon(int param) {
        Image image;
        image = CsmImageLoader.getIcon(object).getImage();
        return mergeBadge(image);
    }

    private Image mergeBadge(Image original) {
        if (model.isDownDirection()) {
            return ImageUtilities.mergeImages(original, downBadge, 0, 0);
        } else {
            if (isRoot){
                return original;
            }
            Image res = ImageUtilities.mergeImages(emptyBadge, original, 4, 0);
            return ImageUtilities.mergeImages(res, upBadge, 0, 0);
        }
    }
    
    public int compareTo(Object o) {
        if( o instanceof IncludeNode ) {
            return getDisplayName().compareTo(((IncludeNode) o).getDisplayName());
        }
        return 0;
    }
    
    @Override
    public Action getPreferredAction() {
        if (object.isValid()) {
            Node parent = getParentNode();
            if (parent instanceof IncludeNode){
                CsmFile find = ((IncludeNode)parent).object;
                for (final CsmInclude inc : object.getIncludes()){
                    if (find.equals(inc.getIncludeFile())) {
                        if (CsmKindUtilities.isOffsetable(inc)){
                            return new GoToFileAction(inc, model.getCloseWindowAction());
                        }
                        break;
                    } else if (object.equals(inc.getIncludeFile())){
                        if (CsmKindUtilities.isOffsetable(inc)){
                            return new GoToFileAction(inc, model.getCloseWindowAction());
                        }
                    }
                }
            }
            return new GoToFileAction(object, model.getCloseWindowAction());
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        Action action = getPreferredAction();
        if (action != null){
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
        return NbBundle.getMessage(IncludeNode.class, key);
    }

    @Override
    public String getShortDescription() {
        if (object.isValid()) {
            return object.getAbsolutePath().toString();
        }
        return super.getShortDescription();
    }

    private static Image downBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/navigation/includeview/resources/down_20.png" ); // NOI18N
    private static Image upBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/navigation/includeview/resources/up_8.png" ); // NOI18N
    private static Image emptyBadge = ImageUtilities.loadImage( "org/netbeans/modules/cnd/navigation/includeview/resources/empty_20.png" ); // NOI18N
}
