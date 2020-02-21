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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.navigation.services.IncludedModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
public class IncludedChildren extends Children.Keys<CsmFile> {
    private static final Comparator<CsmFile> COMARATOR = new MyComparator();
    
    private final CsmFile object;
    private final IncludedChildren parent;
    private final IncludedModel model;
    private boolean isInited = false;
    
    public IncludedChildren(CsmFile object, IncludedModel model, IncludedChildren parent) {
        this.object = object;
        this.parent = parent;
        this.model = model;
    }
    
    public void dispose(){
        if (isInited) {
            isInited = false;
            setKeys(new CsmFile[0]);
        }
    }
    
    private synchronized void resetKeys(){
        if (object.isValid()) {
            Set<CsmFile> set = model.getModel().get(object);
            if (set != null && set.size() > 0) {
                List<CsmFile> list = new ArrayList<CsmFile>(set);
                Collections.sort(list, COMARATOR);
                setKeys(list);
                return;
            }
        }
        setKeys(new CsmFile[0]);
    }
    
    @Override
    protected Node[] createNodes(CsmFile file) {
        Node node;
        Set<CsmFile> set = model.getModel().get(file);
        if (set == null || set.isEmpty()) {
            node = new IncludeNode(file, Children.LEAF, model, false);
        } else {
            if (checkRecursion(file)) {
                node = new IncludeNode(file, Children.LEAF, model, true);
            } else {
                node = new IncludeNode(file, model, this);
            }
        }
        return new Node[]{node};
    }
    
    private boolean checkRecursion(CsmFile file){
        if (file.equals(object)) {
            return true;
        }
        IncludedChildren arr = parent;
        while (arr != null){
            if (file.equals(arr.object)){
                return true;
            }
            arr = arr.parent;
        }
        return false;
    }
    
    @Override
    protected void addNotify() {
        isInited = true;
        resetKeys();
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        dispose();
    }
    
    private static class MyComparator implements Comparator<CsmFile> {
        @Override
        public int compare(CsmFile o1, CsmFile o2) {
            String n1 = o1.getName().toString();
            String n2 = o2.getName().toString();
            return n1.compareTo(n2);
        }
    }
}
