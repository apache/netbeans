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

