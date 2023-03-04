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

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author nam
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager.class)
public class WsdlDataManagerImpl implements WsdlDataManager {
    /** Request processor for this class. */
    private static final RequestProcessor RP = new RequestProcessor(WsdlDataManagerImpl.class);

    private int precedence;

    public WsdlDataManagerImpl() {
        precedence = 0;
    }

    @Override
    public WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous) {
        return WebServiceListModel.getInstance().getWebServiceData(wsdlUrl, serviceName, synchronuous);
    }

    @Override
    public WsdlData addWsdlData(String wsdlUrl, String packageName) {
        final WebServiceData wsData = new WebServiceData(wsdlUrl, WebServiceListModel.DEFAULT_GROUP);
        wsData.setPackageName(packageName);
        wsData.setResolved(false);

        // Run the add W/S asynchronously
        Runnable addWsRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    WebServiceManager.getInstance().addWebService(wsData, true);
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        };
        RP.post(addWsRunnable);
        return wsData;
    }

    @Override
    public void removeWsdlData(String wsdlUrl, String serviceName) {
        WebServiceData wsData = WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
        if (wsData != null) {
            WebServiceManager.getInstance().removeWebService(wsData);
        }
    }

    @Override
    public WsdlData findWsdlData(String wsdlUrl, String serviceName) {
        return WebServiceListModel.getInstance().findWebServiceData(wsdlUrl, serviceName, true);
    }

    @Override
    public void refresh(WsdlData wsdlData) {
        if (wsdlData instanceof WebServiceData) {
            final WebServiceData data = (WebServiceData) wsdlData;
            Runnable addWsRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        WebServiceManager.getInstance().refreshWebService(data);
                    } catch (IOException ex) {
                        handleException(ex);
                    }
                }
            };
            RP.post(addWsRunnable);
        }
    }

    private void handleException(final Exception exception) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (exception instanceof FileNotFoundException) {
                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "INVALID_URL");
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                } else {
                    String cause = (exception != null) ? exception.getLocalizedMessage() : null;
                    String excString = (exception != null) ? exception.getClass().getName() + " - " + cause : null;

                    String errorMessage = NbBundle.getMessage(WebServiceListModel.class, "WS_ADD_ERROR") + "\n\n" + excString; // NOI18N

                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        });
    }

    @Override
    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }
}
