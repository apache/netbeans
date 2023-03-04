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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.modules.autoupdate.services.UpdateUnitImpl;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.spi.autoupdate.UpdateItem;

/** Trampline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
final class TrampolineAPI extends Trampoline {
    
    @Override
    protected UpdateUnit createUpdateUnit (UpdateUnitImpl impl) {
        UpdateUnit unit = new UpdateUnit (impl);
        impl.setUpdateUnit (unit);
        return unit;
    }
    
    @Override
    protected UpdateUnitImpl impl(UpdateUnit unit) {
        return unit.impl;
    }
    
    @Override
    protected UpdateElement createUpdateElement(UpdateElementImpl impl) {
        UpdateElement element = new UpdateElement (impl);
        impl.setUpdateElement (element);
        return element;
    }

    @Override
    protected UpdateElementImpl impl (UpdateElement element) {
        return element.impl;
    }

    @Override
    public UpdateItemImpl impl(UpdateItem item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected UpdateItem createUpdateItem (UpdateItemImpl impl) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    @Override
    protected OperationContainerImpl impl(OperationContainer container) {
        return container.impl;
    }

    @Override
    protected UpdateUnitProvider createUpdateUnitProvider (UpdateUnitProviderImpl impl) {
        return new UpdateUnitProvider (impl);
    }

    @Override
    public UpdateUnitProviderImpl impl (UpdateUnitProvider provider) {
        return provider.impl;
    }
    
    @Override
    protected OperationInfoImpl impl (OperationInfo info) {
        return info.impl;
    }

    @SuppressWarnings ("unchecked")
    @Override
    protected OperationContainer.OperationInfo createOperationInfo (OperationInfoImpl impl) {
        return new OperationContainer.OperationInfo (impl);
    }

    @Override
    protected File findCluster (String clusterName, AutoupdateClusterCreator creator) {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    @Override
    protected File[] registerCluster (String clusterName, File cluster, AutoupdateClusterCreator creator) throws IOException {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    @Override
    public InstallSupportImpl impl(InstallSupport support) {
        return support.impl;
    }

}
