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

package org.netbeans.modules.debugger.jpda.visual.options;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.debugger.Properties;

/**
 *
 * @author Martin Entlicher
 */
public class Options {
    
    private static final String PROPERTIES_VISUAL = "debugger.options.JPDA.visual"; // NOI18N
    public static final String PROPERTY_TCC = "TrackComponentChanges";  // NOI18N
    public static final String PROPERTY_UPLOAD_AGENT = "UploadAgent";  // NOI18N
    
    private static Reference<Properties> propertiesRef = new WeakReference<Properties>(null);
    
    private Options() {}
    
    private static boolean isUploadAgentDefault() {
        return true;
    }
    
    private static boolean isTrackComponentChangesDefault() {
        return false;
    }
    
    public static synchronized Properties getProperties() {
        Properties properties = propertiesRef.get();
        if (properties == null) {
            properties = Properties.getDefault().getProperties(PROPERTIES_VISUAL);
            propertiesRef = new WeakReference<>(properties);
        }
        return properties;
    }
    
    public static boolean isUploadAgent() {
        return getProperties().getBoolean(PROPERTY_UPLOAD_AGENT, isUploadAgentDefault());
    }
    
    public static boolean isTrackComponentChanges() {
        return getProperties().getBoolean(PROPERTY_TCC, isTrackComponentChangesDefault());
    }
    
    public static void setUploadAgent(boolean ua) {
        getProperties().setBoolean(PROPERTY_UPLOAD_AGENT, ua);
    }
    
    public static void setTrackComponentChanges(boolean tcc) {
        getProperties().setBoolean(PROPERTY_TCC, tcc);
    }
    
}
