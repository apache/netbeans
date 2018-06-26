/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
