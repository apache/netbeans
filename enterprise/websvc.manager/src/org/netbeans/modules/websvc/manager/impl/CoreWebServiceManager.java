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
package org.netbeans.modules.websvc.manager.impl;

import javax.swing.Action;
import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import org.openide.nodes.Node;

/**
 * Basic consumer of web service manager.
 * 
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt.class)
public class CoreWebServiceManager implements WebServiceManagerExt {

    public CoreWebServiceManager() {
    }

    public boolean wsServiceAddedExt(WebServiceDescriptor wsMetadataDesc) {
        // place-holder, nothing to do;
        return true;
    }

    public boolean wsServiceRemovedExt(WebServiceDescriptor wsMetadataDesc) {
        // place-holder, nothing to do;
        return true;
    }

    public static final Action[] EMPTY_ACTIONS = new Action[0];
    public Action[] getWebServicesRootActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getGroupActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getWebServiceActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getPortActions(Node node) {
        return EMPTY_ACTIONS;
    }

    public Action[] getMethodActions(Node node) {
        return EMPTY_ACTIONS;
    }

}
