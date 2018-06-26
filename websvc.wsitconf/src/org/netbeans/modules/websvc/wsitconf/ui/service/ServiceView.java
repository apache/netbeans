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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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
package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.util.ArrayList;
import java.util.HashSet;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingFaultNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.OperationNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.OperationContainerServiceNode;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingContainerServiceNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingInputNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingOutputNode;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.openide.filesystems.FileObject;

/**
 * @author Martin Grebac
 */
public class ServiceView extends SectionView {

    ServiceView(InnerPanelFactory factory, WSDLModel model, Node node, Service s) {
        this(factory,  model, node,  null, s, null, null, false);
    }

    ServiceView(InnerPanelFactory factory, WSDLModel model, Node node, FileObject implClass, Service s, JaxWsService jaxService, Collection<Binding> bs, boolean serviceOnly) {
        super(factory);

        if ((implClass == null) && (node == null)) {
            return;
        }
        
        if (implClass == null) {
            if (!(WSITModelSupport.isServiceFromWsdl(node))) {
                implClass = node.getLookup().lookup(FileObject.class);
            }
        }

        Collection<Binding> bindings = bs;
        if (bindings == null) {
            bindings = new HashSet<Binding>();
            Set<FileObject> traversedModels = new HashSet<FileObject>();
            WSITModelSupport.fillImportedBindings(model, bindings, traversedModels);
        }
        
        //add binding section
        Children rootChildren = new Children.Array();
                      
        // if there's only one binding, make the dialog simpler
        Node root = new AbstractNode(rootChildren);
        setRoot(root);

        if (bindings.size() > 1) {
            Node[] bindingNodes = new Node[bindings.size()];
            int i = 0;
            for (Binding binding : bindings) {

                if (implClass != null) {
                    Util.refreshOperations(binding, implClass);
                }
                
                // main node container for a specific binding
                Children bindingChildren = new Children.Array();
                Node bindingNodeContainer = new BindingContainerServiceNode(bindingChildren);
                SectionContainer bindingCont = new SectionContainer(this, 
                        bindingNodeContainer, binding.getName() + " Binding");      //NOI18N
                addSection(bindingCont, false);

                ArrayList<Node> nodes = initOperationView(bindingCont, binding, serviceOnly);
                bindingChildren.add(nodes.toArray(new Node[nodes.size()]));

                bindingNodes[i++] = bindingNodeContainer;
            }
            rootChildren.add(bindingNodes);
        } else if (bindings.size() == 1) {
            Binding b = bindings.iterator().next();

            if (implClass != null) {
                Util.refreshOperations(b, implClass);
            }
            
            ArrayList<Node> nodes = initOperationView(null, b, serviceOnly);
            rootChildren.add(nodes.toArray(new Node[nodes.size()]));
        }
        
    }
        
    private ArrayList<Node> initOperationView(SectionContainer bindingCont, Binding binding, boolean serviceOnly) {
        ArrayList<Node> nodes = new ArrayList<Node>();

        Node bindingNode = new BindingNode(binding);
        nodes.add(bindingNode);
        SectionPanel bindingPanel = new SectionPanel(this, bindingNode, binding, true);
        if (bindingCont != null) {
            bindingCont.addSection(bindingPanel, false);
        } else {
            addSection(bindingPanel, false);
        }
        
        if (!serviceOnly) {
            Collection<BindingOperation> operations = binding.getBindingOperations();
            for (BindingOperation op : operations) {
                Children opChildren = new Children.Array();
                Node opNodeContainer = new OperationContainerServiceNode(opChildren);
                SectionContainer opCont = new SectionContainer(this, opNodeContainer, op.getName() + " Operation"); //NOI18N
                if (bindingCont != null) {
                    bindingCont.addSection(opCont, false);
                } else {
                    addSection(opCont, false);
                }

                ArrayList<Node> subNodes = new ArrayList<Node>();

                Node opNode = new OperationNode(op);
                subNodes.add(opNode);
                SectionPanel opPanel = new SectionPanel(this, opNode, op, false);
                opCont.addSection(opPanel, false);

                BindingInput bi = op.getBindingInput();
                if (bi != null) {
                    Node biNode = new BindingInputNode(bi);
                    subNodes.add(biNode);
                    SectionPanel biPanel = new SectionPanel(this, biNode, bi, false);
                    opCont.addSection(biPanel, false);
                }
                BindingOutput bo = op.getBindingOutput();
                if (bo != null) {
                    Node boNode = new BindingOutputNode(bo);
                    subNodes.add(boNode);
                    SectionPanel boPanel = new SectionPanel(this, boNode, bo, false);
                    opCont.addSection(boPanel, false);
                }
                Collection<BindingFault> bfs = op.getBindingFaults();
                for (BindingFault bf : bfs) {
                    Node bfNode = new BindingFaultNode(bf);
                    subNodes.add(bfNode);
                    SectionPanel bfPanel = new SectionPanel(this, bfNode, bf, false);
                    opCont.addSection(bfPanel, false);
                }
                opChildren.add(subNodes.toArray(new Node[subNodes.size()]));
                nodes.add(opNodeContainer);
            }
        }
        //bindingPanel.open();
        return nodes;
    }
}
