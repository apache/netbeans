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

package org.netbeans.modules.websvc.wsitconf.spi;

import org.openide.nodes.Node;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;

/**
 * Security Checker
 *
 * @author Martin Grebac
 */
public abstract class SecurityChecker {
               
    /**
     * Returns display name to be presented in UI.
     * @return 
     */
    public abstract String getDisplayName();

    /**
     * Should return true if technology security means represented by this checker are 
     * enabled for service or client represented by passed node and jaxWsModel
     * @param node 
     * @param jaxWsModel 
     * @return 
     */
    public boolean isSecurityEnabled(Node node, JaxWsModel jaxWsModel) {
        return false;
    }

}
