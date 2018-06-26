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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
