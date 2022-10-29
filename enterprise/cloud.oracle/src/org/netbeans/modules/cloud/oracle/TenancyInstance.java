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
package org.netbeans.modules.cloud.oracle;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import javax.swing.JComponent;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Horvath
 */
public class TenancyInstance implements ServerInstanceImplementation {

    private final OCIItem tenancy;

    public TenancyInstance(OCIItem tenancy) {
        this.tenancy = tenancy;
    }
    
    @Override
    public String getDisplayName() {
        return tenancy.getName();
    }

    @Override
    public String getServerDisplayName() {
        return tenancy.getKey().getValue();
    }

    @Override
    public Node getFullNode() {
        return getBasicNode();
    }

    @Override
    public Node getBasicNode() {
        return new TenancyNode(tenancy);
    }

    @Override
    public JComponent getCustomizer() {
        return null;
    }

    @Override
    public void remove() {
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
    
}
