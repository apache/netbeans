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


package org.openide.loaders;

import java.io.IOException;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.nodes.CookieSet;

/**
 * API trampoline to access package private methods in org.openide.loaders
 * package.
 * 
 * @since 6.3
 * @author S. Aubrecht
 */
final class DataObjectAccessorImpl extends DataObjectAccessor {
    
    public DataObject copyRename( DataObject dob, DataFolder f, String name, String ext ) throws IOException {
        return dob.copyRename( f, name, ext );
    }
    
    public CookieSet getCookieSet(MultiDataObject dob) {
        return dob.getCookieSet();
    }

    @Override
    public boolean isInstancesThread() {
        return FolderInstance.PROCESSOR.isRequestProcessorThread() || FolderList.isFolderRecognizerThread();
    }

    @Override
    public void precreateInstances(FolderInstance fi) {
        fi.precreateInstances();
    }

    @Override
    public String getOrigName() {
        return DataObject.CreateAction.getOrigName();
    }
}
