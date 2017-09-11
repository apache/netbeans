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
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;

/**
 *
 * @author rico
 */
public class WsdlDataImpl implements WsdlData{
    private String wsdlUrl;
    private String wsdlFile;
    private WSService wsService;
    private Status status;
    private String id;
    private String name;
    private boolean resolved;
    private List<PropertyChangeListener> propertyListeners = new ArrayList<PropertyChangeListener>();
    public static final String PROP_RESOLVED = "resolved";

    public WsdlDataImpl(String wsdlUrl){
        this.wsdlUrl = wsdlUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getOriginalWsdlUrl() {
        return wsdlUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile){
        this.wsdlFile = wsdlFile;
    }

    public WSService getWsdlService() {
        return wsService;
    }

    public void setWsdlService(WSService wsService){
        this.wsService = wsService;
    }

    public boolean isReady() {
        return status == Status.WSDL_RETRIEVED;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status){
        Status old = this.status;
        this.status = status;

        PropertyChangeEvent evt =
                new PropertyChangeEvent(this, PROP_STATE, old, status); // NOI18N

        for (PropertyChangeListener listener : propertyListeners) {
            listener.propertyChange(evt);
        }
    }

    public void setResolved(boolean resolved){
        Boolean old = this.resolved;
        this.resolved = resolved;
         PropertyChangeEvent evt =
                new PropertyChangeEvent(this, PROP_RESOLVED, old, this.resolved); // NOI18N

        for (PropertyChangeListener listener : propertyListeners) {
            listener.propertyChange(evt);
        }
    }

    public boolean isResolved(){
        return resolved;
    }
    public WsdlServiceProxyDescriptor getJaxWsDescriptor() {
        return null;
    }

    public WsdlServiceProxyDescriptor getJaxRpcDescriptor() {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyListeners.add(l);
        
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyListeners.remove(l);
    }

}
