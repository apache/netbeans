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

package org.netbeans.modules.cnd.classview.model;

import java.awt.Image;
import java.util.Collection;
import javax.swing.Action;
import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.classview.actions.GoToDeclarationAction;
import org.netbeans.modules.cnd.classview.actions.MoreDeclarations;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.nodes.Children;

/**
 */
public class NamespaceNode extends NPNode {
    private CharSequence id;
    private CsmProject project;
    private static Image namespaceImage = null;
    private CharSequence name;
    private CharSequence qname;
    
    public NamespaceNode(CsmNamespace ns, Children.Array key) {
        super(key);
        init(ns);
    }

    private void init(CsmNamespace ns){
        id = ns.getQualifiedName();
        project = ns.getProject();
        name = ns.getQualifiedName();
        qname = CVUtil.getNamespaceDisplayName(ns).toString();
        if( namespaceImage == null ) {
            namespaceImage = CsmImageLoader.getImage(ns);
        }
    }

    @Override
    public Image getIcon(int param) {
        return namespaceImage;
    }
    
    @Override
    public CsmNamespace getNamespace() {
        return project.findNamespace(id);
    }
    
    @Override
    public String getName() {
        return name.toString();
    }

    @Override
    public String getDisplayName() {
        return qname.toString();
    }

    @Override
    public String getShortDescription() {
        return name.toString();
    }
    
    @Override
    public String getHtmlDisplayName() {
        String retValue = qname.toString();
        // make unnamed namespace bold and italic
        if (retValue.startsWith(" ") || retValue.startsWith("unnamed ")) { // NOI18N
            retValue = "<i>" + retValue; // NOI18N
        }
        return retValue;
    }

    @Override
    public Action getPreferredAction() {
        return createOpenAction();
    }
    
    private Action createOpenAction() {
        CsmNamespace ns = getNamespace();
        if (ns != null){
            Collection<? extends CsmOffsetableDeclaration> arr = ns.getDefinitions();
            if (arr.size() > 0) {
                return new GoToDeclarationAction(arr.iterator().next());
            }
        }
        return null;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        Action action = createOpenAction();
        if (action != null){
            CsmNamespace ns = getNamespace();
            Collection<? extends CsmOffsetableDeclaration> arr = ns.getDefinitions();
            if (arr.size() > 1){
                Action more = new MoreDeclarations(arr);
                return new Action[] { action, more };
            }
            return new Action[] { action };
        }
        return new Action[0];
    }
}
