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

package org.netbeans.modules.cnd.discovery.wizard.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

/**
 *
 */
public class MacrosListModel implements ListModel<String> {
    private final NodeConfigurationImpl configuration;
    private final Map<String,String> map;
    private final List<String> names;
    public MacrosListModel(NodeConfigurationImpl configuration, boolean resulting){
        this.configuration = configuration;
        map = configuration.getUserMacros(resulting);
        names = new ArrayList<>(map.keySet());
        Collections.<String>sort(names);
    }

    @Override
    public int getSize() {
        return names.size();
    }

    @Override
    public String getElementAt(int index) {
        String key = names.get(index);
        String value = map.get(key);
        if (value != null) {
            return key+"="+value; // NOI18N
        }
        return key;
    }

    public NodeConfigurationImpl getNodeConfiguration(){
        return configuration;
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }
}
