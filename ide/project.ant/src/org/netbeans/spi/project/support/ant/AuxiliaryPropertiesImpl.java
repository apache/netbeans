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

package org.netbeans.spi.project.support.ant;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.openide.util.Mutex;

/**
 *
 * @author Jan Lahoda
 */
class AuxiliaryPropertiesImpl implements AuxiliaryProperties {

    private final AntProjectHelper helper;
    private static final String propertyPrefix = "auxiliary.";

    AuxiliaryPropertiesImpl(AntProjectHelper helper) {
        this.helper = helper;
    }
    
    @Override public String get(String key, boolean shared) {
        String location = shared ? AntProjectHelper.PROJECT_PROPERTIES_PATH : AntProjectHelper.PRIVATE_PROPERTIES_PATH;
        EditableProperties props = helper.getProperties(location);
        
        return props.get(propertyPrefix + key);
    }

    @Override public void put(final String key, final String value, final boolean shared) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            @Override public Void run() {
                String location = shared ? AntProjectHelper.PROJECT_PROPERTIES_PATH : AntProjectHelper.PRIVATE_PROPERTIES_PATH;
                EditableProperties props = helper.getProperties(location);

                if (value != null) {
                    props.put(propertyPrefix + key, value);
                } else {
                    props.remove(propertyPrefix + key);
                }

                helper.putProperties(location, props);
                
                return null;
            }
        });
    }

    @Override public Iterable<String> listKeys(boolean shared) {
        List<String> result = new LinkedList<String>();
        String location = shared ? AntProjectHelper.PROJECT_PROPERTIES_PATH : AntProjectHelper.PRIVATE_PROPERTIES_PATH;
        EditableProperties props = helper.getProperties(location);
        
        for (String k : props.keySet()) {
            if (k.startsWith(propertyPrefix)) {
                result.add(k.substring(propertyPrefix.length()));
            }
        }
        
        return result;
    }

}
