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
package org.netbeans.modules.maven.jaxws.nodes;

/** WSDL children (Service elements)
 *
 * @author mkuchtiak
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.DialogDisplayer;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

public class JaxWsClientChildren extends Children.Keys<WsdlService> {

    //JaxWsService client;
    WsdlModel wsdlModel;
    private JAXWSLightSupport jaxWsSupport;

    public JaxWsClientChildren(JAXWSLightSupport jaxWsSupport, JaxWsService client) {
        this.jaxWsSupport = jaxWsSupport;
        //this.client = client;
    }

    @Override
    protected void addNotify() {
        final WsdlModeler wsdlModeler = ((JaxWsClientNode) getNode()).getWsdlModeler();
        if (wsdlModeler != null) {
            wsdlModeler.setCatalog(jaxWsSupport.getCatalog());
            wsdlModel = wsdlModeler.getWsdlModel();
            if (wsdlModel == null) {
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                    @Override
                    public void modelCreated(WsdlModel model) {
                        wsdlModel = model;
                        ((JaxWsClientNode) getNode()).changeIcon();
                        if (model == null) {
                            DialogDisplayer.getDefault().notify(
                                    new WsImportFailedMessage(false, wsdlModeler.getCreationException()));
                        }
                        updateKeys();
                    }
                });
            } else {
                updateKeys();
            }
        }
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<WsdlService>emptySet());
    }

    void updateKeys() {
        List<WsdlService> keys = null;
        if (wsdlModel != null) {
            keys = wsdlModel.getServices();
        }
        setKeys(keys == null ? new ArrayList<WsdlService>() : keys);
    }

    @Override
    protected Node[] createNodes(WsdlService key) {
        return new Node[]{new ServiceNode(key)};
    }


    void setWsdlModel(WsdlModel wsdlModel) {
        this.wsdlModel = wsdlModel;
    }
}
