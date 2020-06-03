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
package org.netbeans.modules.odcs.cnd.actions;

import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.odcs.cnd.json.VMDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 */
public class PropertiesAction extends AbstractAction {

    private static final ImageIcon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/odcs/cnd/resources/gear.png", true); // NOI18N
    private static final Logger LOG = Logger.getLogger(PropertiesAction.class.getName());

    private final VMDescriptor desc;

    @NbBundle.Messages({
        "remotevm.properties.action.text=Properties"
    })
    public PropertiesAction(VMDescriptor desc) {
        super(Bundle.remotevm_properties_action_text(), ICON);
        this.desc = desc;
    }

    @NbBundle.Messages({
        "# {0} - vm name",
        "remotevm.properties.title=Properties for {0}"
    })
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            BeanNode<VMDescriptor> beanNode = new BeanNode<>(desc);
            PropertySheet propertySheet = new PropertySheet();
            propertySheet.setNodes(new Node[]{beanNode});

            DialogDescriptor dd = new DialogDescriptor(propertySheet, Bundle.remotevm_properties_title(desc.getHostname()));
            DialogDisplayer.getDefault().notify(dd);
        } catch (IntrospectionException ex) {
            LOG.log(Level.INFO, "Can't show properties", ex);
        }
    }
}
