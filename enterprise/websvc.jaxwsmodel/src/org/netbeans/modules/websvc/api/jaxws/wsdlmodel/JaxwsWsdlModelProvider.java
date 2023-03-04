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
package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import java.io.File;
import java.net.URL;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider.class)
public class JaxwsWsdlModelProvider implements WsdlModelProvider {

    private String packageName;
    private Throwable creationException;

    public WsdlModel getWsdlModel(URL url, String packageName, URL catalog, 
            boolean forceReload) 
    {
        this.packageName = packageName;
        WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(url);
        modeler.setCatalog(catalog);
        WsdlModel model = modeler.getAndWaitForWsdlModel(forceReload);
        if (model != null && (this.packageName == null || this.packageName.trim().length() == 0)) {
            if(model.getServices().size() > 0)
                this.packageName = model.getServices().get(0).getJavaName();
            else
                this.packageName = "defaultPackage";
        }

        Throwable ce = modeler.getCreationException();
        if (ce != null) {
            creationException = ce;
        } else {
            creationException = null;
        }

        return model;
    }

    public boolean canAccept(URL url) {
        if (isRPCEncoded(url)) {
            return false;
        }
        return true;
    }

    public Throwable getCreationException() {
        return creationException;
    }

    public String getEffectivePackageName() {
        return packageName;
    }

    private boolean isRPCEncoded(URL url) {
        try {
            FileObject wsdlFO = FileUtil.toFileObject(new File(url.toURI()));
            WSDLModel model = WSDLModelFactory.getDefault().getModel(Utilities.createModelSource(wsdlFO, false));
            return isRPCEncoded(model);
        } catch (Exception ex) {
            Logger.getLogger(JaxwsWsdlModelProvider.class.getName()).log(Level.INFO, "", ex);
        }
        return false;
    }

    public static boolean isRPCEncoded(WSDLModel wsdlModel) {

        Definitions definitions = wsdlModel.getDefinitions();
        Collection<Binding> bindings = definitions.getBindings();
        for (Binding binding : bindings) {
            List<SOAPBinding> soapBindings = binding.getExtensibilityElements(SOAPBinding.class);
            for (SOAPBinding soapBinding : soapBindings) {
                if (soapBinding.getStyle() == Style.RPC) {
                    Collection<BindingOperation> bindingOperations = binding.getBindingOperations();
                    for (BindingOperation bindingOperation : bindingOperations) {
                        BindingInput bindingInput = bindingOperation.getBindingInput();
                        if (bindingInput != null) {
                            List<SOAPBody> soapBodies = bindingInput.getExtensibilityElements(SOAPBody.class);
                            if (soapBodies != null && soapBodies.size() > 0) {
                                SOAPBody soapBody = soapBodies.get(0);
                                if (soapBody.getUse() == Use.ENCODED) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

        }
        return false;
    }
}
