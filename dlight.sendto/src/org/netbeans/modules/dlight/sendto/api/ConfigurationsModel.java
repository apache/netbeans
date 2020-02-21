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

package org.netbeans.modules.dlight.sendto.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;

/**
 *
 */
public final class ConfigurationsModel implements Cloneable {

    private static final ConfigurationsModel defaultModel = new ConfigurationsModel();
    private final List<Configuration> model = new ArrayList<Configuration>();
    private final ChangeSupport cs = new ChangeSupport(this);

    private ConfigurationsModel() {
    }

    public static ConfigurationsModel getDefault() {
        return defaultModel;
    }

    // Adds listener weakly. No need to unregister later,
    // but need to keep e refference on the listener during object's life
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(WeakListeners.change(l, this));
    }

    @Override
    public Object clone() {
        ConfigurationsModel clone = new ConfigurationsModel();
        for (Configuration configuration : model) {
            clone.model.add((Configuration) configuration.clone());
        }
        return clone;
    }

    public synchronized void add(Configuration cfg) {
        if (model.contains(cfg)) {
            return;
        }

        model.add(cfg);
        Collections.sort(model);
        
        cs.fireChange();
    }

    public synchronized void remove(Configuration cfg) {
        model.remove(cfg);
        cs.fireChange();
    }

    public synchronized void setDataFrom(ConfigurationsModel src) {
        model.clear();
        model.addAll(src.model);
        cs.fireChange();
    }

    public List<Configuration> getConfigurations() {
        return Collections.unmodifiableList(model);
    }
}
