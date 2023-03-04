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
package org.netbeans.modules.websvc.saas.wsdl.websvcmgr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author rico
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlDataManager.class)
public class WsdlDataManagerImpl implements WsdlDataManager, PropertyChangeListener {
    private int precedence;

    public WsdlDataManagerImpl() {
        precedence = 1;
    }

    public WsdlData getWsdlData(String wsdlUrl, String serviceName, boolean synchronuous) {
        return WebServiceListManager.getInstance().findReadyWsdlData(wsdlUrl);
    }

    public WsdlData addWsdlData(String wsdlUrl, String packageName) {
        final WsdlDataImpl wsData = new WsdlDataImpl(wsdlUrl);
        wsData.setStatus(WsdlData.Status.WSDL_UNRETRIEVED);
        Runnable addWsRunnable = new Runnable() {

            public void run() {
                try {
                    WebServiceManager.getInstance().addWebService(wsData, true);
                } catch (IOException ex) {
                    handleException(ex);
                }
            }
        };
        RequestProcessor.getDefault().post(addWsRunnable);
        return wsData;
    }

    public void removeWsdlData(String wsdlUrl, String serviceName) {
        WsdlDataImpl wsData = WebServiceListManager.getInstance().findWsdlData(wsdlUrl);
        if (wsData != null) {
            WebServiceManager.getInstance().removeWebService(wsData);
        }

    }

    public void refresh(WsdlData wsdlData) {
        if (wsdlData instanceof WsdlDataImpl) {
            final WsdlDataImpl data = (WsdlDataImpl) wsdlData;
            Runnable addWsRunnable = new Runnable() {

                public void run() {
                    try {
                        WebServiceManager.getInstance().refreshWebService(data);
                    } catch (IOException ex) {
                        handleException(ex);
                    }
                }
            };
            RequestProcessor.getDefault().post(addWsRunnable);
        }
    }

    private void handleException(final Exception exception) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (exception instanceof FileNotFoundException) {
                    String errorMessage = NbBundle.getMessage(WsdlDataManagerImpl.class, "INVALID_URL");
                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                } else {
                    String cause = (exception != null) ? exception.getLocalizedMessage() : null;
                    String excString = (exception != null) ? exception.getClass().getName() + " - " + cause : null;

                    String errorMessage = NbBundle.getMessage(WsdlDataManagerImpl.class, "WS_ADD_ERROR", excString);

                    NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                    DialogDisplayer.getDefault().notify(d);
                }
            }
        });
    }

    public void propertyChange(PropertyChangeEvent evt) {
       
    }

    public WsdlData findWsdlData(String wsdlUrl, String serviceName) {
        return WebServiceListManager.getInstance().findWsdlData(wsdlUrl);
    }

    public void setPrecedence(int precedence) {
        this.precedence = precedence;
    }

    public int getPrecedence() {
        return precedence;
    }
}

