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

package org.netbeans.modules.cnd.navigation.callgraph;

import java.util.Collection;
import java.util.Collections;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.callgraph.api.Call;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphAction;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphModelFactory;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphUI;
import org.netbeans.modules.cnd.callgraph.api.ui.Catalog;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphActionEDTRunnable;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.nodes.Node;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.callgraph.api.ui.CallGraphModelFactory.class, position = 100)
public class CallGraphModelFactoryImpl extends CallGraphModelFactory {

    @Override
    public CallModel getModel(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return null;
        }
        CsmOffsetableDeclaration declaration = null;
        CsmProject project = null;
        CsmReference ref = CsmReferenceResolver.getDefault().findReference(activatedNodes[0]);
        if (ref == null) {
            return null;
        }
        CsmFile file = ref.getContainingFile();
        if (file == null){
            return null;
        }
        project = file.getProject();
        if (project == null){
            return null;
        }
        CsmObject obj = ref.getReferencedObject();
        if (CsmKindUtilities.isFunction(obj)) {
            declaration = (CsmFunction) obj;
        } else if(CsmKindUtilities.isVariable(obj) && !CsmKindUtilities.isLocalVariable(obj)) {
            declaration = (CsmVariable) obj;
        } else if(CsmKindUtilities.isEnumerator(obj)) {
            declaration = (CsmEnumerator) obj;
        } else {
            obj = ref.getClosestTopLevelObject();
            if (CsmKindUtilities.isFunction(obj)) {
                declaration = (CsmFunction) obj;
            }
        }
        if (declaration != null) {
            return new CallModelImpl(project, declaration);
        }
        return null;
    }

    @Override
    public CallGraphUI getUI(CallModel model) {
        if (model instanceof CallModelImpl) {
            return new CallGraphUI(){
                @Override
                public boolean showGraph() {
                    return CndUtils.getBoolean("cnd.callgraph.showgraph", true); // NOI18N
                }

                @Override
                public Catalog getCatalog() {
                    return null;
                }

                @Override
                public JPanel getContextPanel(Call call) {
                    return null;
                }

                @Override
                public Collection<CallGraphAction> getActions(CallGraphActionEDTRunnable runnable) {
                    return Collections.emptyList();
                }
            };
        }
        return null;
    }

    @Override
    public boolean isCallGraphAvailiable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length == 0) {
            return false;
        }
        CsmFile file = CsmUtilities.getCsmFile(activatedNodes[0], false);
        return file != null && file.isParsed();
    }
}
