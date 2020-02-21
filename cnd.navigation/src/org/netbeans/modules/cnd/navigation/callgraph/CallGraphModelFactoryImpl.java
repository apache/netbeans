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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
