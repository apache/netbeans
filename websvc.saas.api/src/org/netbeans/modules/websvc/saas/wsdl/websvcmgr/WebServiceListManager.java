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
package org.netbeans.modules.websvc.saas.wsdl.websvcmgr;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.Saas;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;

/**
 *
 * @author rico
 */
public class WebServiceListManager implements PropertyChangeListener {

    private List<WsdlDataImpl> wsdlServices;
    private static WebServiceListManager mgr;
    private boolean initialized;

    public static WebServiceListManager getInstance() {
        if (mgr == null) {
            mgr = new WebServiceListManager();
        }
        return mgr;
    }

    private WebServiceListManager() {
    }

    public void init() {
        if (!initialized) {
            initialized = true;
            wsdlServices = new ArrayList<WsdlDataImpl>();
            SaasServicesModel saasServicesModel = SaasServicesModel.getInstance();
            saasServicesModel.addPropertyChangeListener(this);
            SaasGroup root = saasServicesModel.getRootGroup();
            List<SaasGroup> groups = root.getChildrenGroups();
            for (SaasGroup group : groups) {
                List<Saas> saasServices = group.getServices();
                for (Saas saasService : saasServices) {
                    if (saasService instanceof WsdlSaas) {
                        WsdlSaas wsdlSaas = (WsdlSaas) saasService;

                        wsdlServices.add(new WsdlDataImpl(wsdlSaas.getDelegate().getUrl()));
                    }
                }
            }
        }
    }

    public boolean wsdlDataExistsFor(String wsdlUrl) {
        init();
        for (WsdlDataImpl wsdlData : wsdlServices) {
            if (wsdlData.getOriginalWsdlUrl().equals(wsdlUrl)) {
                return true;
            }
        }
        return false;
    }

    public WsdlDataImpl findWsdlData(String wsdlUrl) {
        init();
        for (WsdlDataImpl wsdlData : wsdlServices) {
            if (wsdlData.getOriginalWsdlUrl().equals(wsdlUrl)) {
                return wsdlData;
            }
        }
        return null;
    }

    public WsdlDataImpl findReadyWsdlData(String wsdlUrl) {
        WsdlDataImpl wsdlData = findWsdlData(wsdlUrl);
        if(wsdlData != null && wsdlData.isReady()){
            return wsdlData;
        }
        return null;
    }

    public void addWsdlData(WsdlDataImpl wsdlData) {
        wsdlServices.add(wsdlData);
    }

    public void removeWsdlData(WsdlDataImpl wsdlData) {
        if (wsdlDataExistsFor(wsdlData.getOriginalWsdlUrl())) {
            wsdlServices.remove(wsdlData);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }
}
