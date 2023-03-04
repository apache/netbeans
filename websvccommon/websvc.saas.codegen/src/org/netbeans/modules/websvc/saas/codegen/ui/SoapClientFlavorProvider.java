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
package org.netbeans.modules.websvc.saas.codegen.ui;

import java.net.URL;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.model.WsdlSaasMethod;
import org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Ayub Khan
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.ConsumerFlavorProvider.class)
public class SoapClientFlavorProvider implements ConsumerFlavorProvider {

    public SoapClientFlavorProvider() {
    }

    public Transferable addDataFlavors(Transferable transferable) {
        try {
            if (transferable.isDataFlavorSupported(ConsumerFlavorProvider.WSDL_METHOD_FLAVOR)) {
                Object data = transferable.getTransferData(ConsumerFlavorProvider.WSDL_METHOD_FLAVOR);
                if (data instanceof WsdlSaasMethod) {
                    WsdlSaasMethod method = (WsdlSaasMethod) data;
                    ExTransferable t = ExTransferable.create(transferable);
                    SoapClientEditorDrop editorDrop = new SoapClientEditorDrop(method);
                    ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
                    t.put(s);
                    return t;
                }
            } else if (transferable.isDataFlavorSupported(ConsumerFlavorProvider.WSDL_SERVICE_FLAVOR)) {
                Object data = transferable.getTransferData(ConsumerFlavorProvider.WSDL_SERVICE_FLAVOR);
                if (data instanceof WsdlSaas) {
                    WsdlSaas saas = (WsdlSaas) data;
                    URL url = getWsdlLocationURL(saas);
                    if (url == null) {
                        return transferable;
                    }
                    
                    ExTransferable t = ExTransferable.create(transferable);
                    SoapServiceClientEditorDrop editorDrop = new SoapServiceClientEditorDrop(saas);
                    ServiceActiveEditorDropTransferable s = new ServiceActiveEditorDropTransferable(editorDrop);
                    t.put(s);
                    return t;
                    
                    //TODO take care of web service node from the project's explorer tree
                    //WebServiceReference ref = new WebServiceReference(getWsdlLocationURL(saas), saas.getWsdlModel().getName(), "");
                    //ExTransferable t = ExTransferable.create(transferable);
                    //t.put(new WebServiceTransferable(ref));
                    //return t;
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        return transferable;
    }

    private URL getWsdlLocationURL(WsdlSaas saas){
        URL url = null;
        java.lang.String wsdlURL = saas.getWsdlData().getWsdlFile();
        if (wsdlURL == null) {
            return null;
        }
        try {
            url = new URL(wsdlURL);
        } catch (MalformedURLException ex) {
            //attempt to recover
            File f = new File(wsdlURL);
            try{
                url = BaseUtilities.normalizeURI(f.getCanonicalFile().toURI()).toURL();
            } catch (IOException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
        return url;
    }
    
    private static class ActiveEditorDropTransferable extends ExTransferable.Single {

        private SoapClientEditorDrop drop;

        ActiveEditorDropTransferable(SoapClientEditorDrop drop) {
            super(SoapClientEditorDrop.FLAVOR);

            this.drop = drop;
        }

        public Object getData() {
            return drop;
        }
    }
    
    private static class ServiceActiveEditorDropTransferable extends ExTransferable.Single {

        private SoapServiceClientEditorDrop drop;

        ServiceActiveEditorDropTransferable(SoapServiceClientEditorDrop drop) {
            super(SoapServiceClientEditorDrop.FLAVOR);

            this.drop = drop;
        }

        public Object getData() {
            return drop;
        }
    }
}
