/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.ojet.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;

/**
 *
 * @author Petr Pisl
 */
public abstract class DataProvider {

    public abstract Collection<DataItem> getBindingOptions();
    
    public abstract Collection<DataItem> getModuleProperties();

    public abstract Collection<DataItem> getComponents();

    public abstract Collection<DataItem> getComponentOptions(String compName);
    
    public abstract Collection<DataItem> getComponentEvents(String compName);
    
    public abstract Collection<String> getAvailableVersions();
    
    public abstract String getCurrentVersion();
    
    public abstract void setCurrentVersion(String version);
    
    public abstract Collection<JsObject> getGlobalObjects(ModelElementFactory factory);

    public static Collection<DataItem> filterByPrefix(Collection<? extends DataItem> data, String prefix) {
        List<DataItem> result = new ArrayList<>();
        if (prefix == null || prefix.isEmpty()) {
            result.addAll(data);
        } else {
            for (DataItem dataItem : data) {
                if (dataItem.getName().startsWith(prefix)) {
                    result.add(dataItem);
                }
            }
        }
        return result;
    }
}
