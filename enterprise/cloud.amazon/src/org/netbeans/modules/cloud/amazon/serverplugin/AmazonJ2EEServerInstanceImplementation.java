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
package org.netbeans.modules.cloud.amazon.serverplugin;

import javax.swing.JComponent;
import org.netbeans.modules.cloud.amazon.ui.AmazonJ2EEInstanceNode;
import org.netbeans.modules.cloud.amazon.ui.serverplugin.AmazonJ2EEServerWizardComponent;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;

/**
 *
 */
public class AmazonJ2EEServerInstanceImplementation implements ServerInstanceImplementation {

    private AmazonJ2EEInstance aij;

    public AmazonJ2EEServerInstanceImplementation(AmazonJ2EEInstance aij) {
        this.aij = aij;
    }
    
    @Override
    public String getDisplayName() {
        return getBasicNode().getDisplayName(); //aij.getApplicationName() + " - " + aij.getEnvironmentName();
    }

    @Override
    public String getServerDisplayName() {
        return "Tomcat";
    }

    @Override
    public Node getFullNode() {
        return getBasicNode();
    }

    @Override
    public Node getBasicNode() {
        return new AmazonJ2EEInstanceNode(aij);
    }

    @Override
    public JComponent getCustomizer() {
        AmazonJ2EEServerWizardComponent panel = new AmazonJ2EEServerWizardComponent(null, null, aij);
        return panel;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return aij.getInstance().getProperty(key);
    }

}
