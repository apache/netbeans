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
