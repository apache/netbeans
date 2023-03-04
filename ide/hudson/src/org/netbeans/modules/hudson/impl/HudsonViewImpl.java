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

package org.netbeans.modules.hudson.impl;

import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import static org.netbeans.modules.hudson.constants.HudsonViewConstants.*;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;

/**
 * Implementation of the HudsonView
 *
 * @author Michal Mocnak
 */
public class HudsonViewImpl implements HudsonView, OpenableInBrowser {
    
    private HudsonInstance instance;
    private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    public HudsonViewImpl(HudsonInstance instance, String name, String url) {
        properties.putProperty(VIEW_NAME, name);
        properties.putProperty(VIEW_URL, url);
        
        this.instance = instance;
    }
    
    public String getName() {
        return properties.getProperty(VIEW_NAME, String.class);
    }
    
    public String getUrl() {
        return properties.getProperty(VIEW_URL, String.class);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HudsonViewImpl))
            return false;
        
        HudsonViewImpl v = (HudsonViewImpl) o;
        
        return getName().equals(v.getName()) && getUrl().equals(v.getUrl());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public HudsonInstance getInstance() {
        return instance;
    }

}
