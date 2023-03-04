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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl.OperationInfoImpl;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.spi.autoupdate.UpdateItem;

/** Trampoline to access internals of API and SPI.
 *
 * @author Jiri Rechtacek
 */
public abstract class Trampoline<Support> extends Object {
    static {
        try {
            java.lang.Class.forName(
                UpdateUnit.class.getName(),
                true,
                Trampoline.class.getClassLoader()
            );
            java.lang.Class.forName(
                UpdateItem.class.getName(),
                true,
                Trampoline.class.getClassLoader()
            );
        } catch (ClassNotFoundException ex) {
            Logger.getLogger ("org.netbeans.modules.autoupdate.services.Trampoline").log (Level.SEVERE, ex.getMessage (), ex);
        }
    }

    public static Trampoline API;
    public static Trampoline SPI;

    // api.UpdateUnit
    protected abstract UpdateUnit createUpdateUnit (UpdateUnitImpl impl);
    protected abstract UpdateUnitImpl impl (UpdateUnit unit);
    
    // api.UpdateElement
    protected abstract UpdateElement createUpdateElement (UpdateElementImpl impl);
    protected abstract UpdateElementImpl impl (UpdateElement element);
    
    // api.OperationContainer
    protected abstract OperationContainerImpl impl (OperationContainer container);
    protected abstract OperationInfoImpl impl (OperationInfo info);
    protected abstract OperationInfo<Support> createOperationInfo (OperationInfoImpl impl);
    
    // api.UpdateUnitProvider
    protected abstract UpdateUnitProvider createUpdateUnitProvider (UpdateUnitProviderImpl impl);
    public abstract UpdateUnitProviderImpl impl (UpdateUnitProvider provider);
    
    // api.InstallSupport
    public abstract InstallSupportImpl impl(InstallSupport support);

    // spi.UpdateItem
    public abstract UpdateItemImpl impl (UpdateItem item);
    protected abstract UpdateItem createUpdateItem (UpdateItemImpl impl);
    
    // spi.AutoupdateClusterCreator
    protected abstract File findCluster (String clusterName, AutoupdateClusterCreator creator);
    protected abstract File[] registerCluster (String clusterName, File cluster, AutoupdateClusterCreator creator) throws IOException;
}
