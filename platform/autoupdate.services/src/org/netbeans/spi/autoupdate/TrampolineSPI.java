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

package org.netbeans.spi.autoupdate;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.api.autoupdate.*;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;

/** Trampoline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
final class TrampolineSPI extends Trampoline {
    
    @Override
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected UpdateUnitImpl impl (UpdateUnit unit) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected UpdateElement createUpdateElement (UpdateElementImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UpdateElementImpl impl (UpdateElement element) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UpdateItemImpl impl(UpdateItem item) {
        return item.impl;
    }

    @Override
    protected UpdateItem createUpdateItem (UpdateItemImpl impl) {
        return new UpdateItem (impl);
    }

    @Override
    protected OperationContainerImpl impl(OperationContainer container) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected UpdateUnitProvider createUpdateUnitProvider(UpdateProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UpdateUnitProvider createUpdateUnitProvider(UpdateUnitProviderImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UpdateUnitProviderImpl impl(UpdateUnitProvider provider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected OperationInfoImpl impl (OperationInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected OperationInfo createOperationInfo(OperationContainerImpl.OperationInfoImpl impl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected File findCluster (String clusterName, AutoupdateClusterCreator creator) {
        return creator.findCluster (clusterName);
    }

    @Override
    protected File[] registerCluster (String clusterName, File cluster, AutoupdateClusterCreator creator) throws IOException {
        return creator.registerCluster (clusterName, cluster);
    }

    @Override
    public InstallSupportImpl impl(InstallSupport support) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
