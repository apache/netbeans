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

package org.netbeans.modules.websvc.rest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class RESTClientGenerator implements CodeGenerator {
    private FileObject targetSource;
    private JTextComponent targetComponent;

    RESTClientGenerator(FileObject targetSource, JTextComponent targetComponent) {
        this.targetSource = targetSource;
        this.targetComponent = targetComponent;
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            CompilationController controller = context.lookup(CompilationController.class);

            List<CodeGenerator> ret = new ArrayList<CodeGenerator>();
            if (controller != null) {
                try {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    FileObject targetSource = controller.getFileObject();
                    if (targetSource != null) {
                        JTextComponent targetComponent = context.lookup(JTextComponent.class);
                        ret.add(new RESTClientGenerator(targetSource, targetComponent));
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            return ret;
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RESTClientGenerator.class, "LBL_GenerateRESTClient");
    }

    @Override
    public void invoke() {

        RESTResourcesPanel resourcesPanel = new RESTResourcesPanel();
        DialogDescriptor descriptor = new DialogDescriptor(resourcesPanel,
                NbBundle.getMessage(RESTClientGenerator.class,"TTL_RESTResources")); //NOI18N
        resourcesPanel.setDescriptor(descriptor);
        if (DialogDisplayer.getDefault().notify(descriptor).equals(NotifyDescriptor.OK_OPTION)) {
            Node resourceNode = resourcesPanel.getResourceNode();
            if (resourceNode != null) {
                // Generate Jersey Client
                ClientJavaSourceHelper.generateJerseyClient(resourceNode, targetSource, resourcesPanel.getClassName(), resourcesPanel.getSecurity());
                // logging usage of action
                Object[] params = new Object[2];
                params[0] = LogUtils.WS_STACK_JAXRS;
                params[1] = "GENERATE REST RESOURCE"; // NOI18N
                LogUtils.logWsAction(params);
            }
        }
    }
}
