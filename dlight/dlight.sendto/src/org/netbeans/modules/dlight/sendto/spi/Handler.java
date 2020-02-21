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
package org.netbeans.modules.dlight.sendto.spi;

import org.netbeans.modules.dlight.sendto.api.Configuration;
import org.netbeans.modules.dlight.sendto.api.ConfigurationPanel;
import org.netbeans.modules.dlight.sendto.action.FutureAction;
import org.openide.util.Lookup;

/**
 *
 */
public abstract class Handler<T extends ConfigurationPanel> {

    private final String id;
    private T panel;

    public Handler(String id) {
        this.id = id;
    }

    @Override
    public final String toString() {
        return getDescription();
    }

    public abstract String getDescription();

    public final T getConfigurationPanel() {
        if (panel == null) {
            panel = createConfigurationPanel();
        }

        return panel;
    }

    protected abstract T createConfigurationPanel();

    /**
     * Returns immutable (means that Lookup's content may be changed at the 
     * invocation time. So future action should be fully constructed and be 
     * insensitive to Lookup changes) action that can be invoked later. 
     * <br>
     * It is guaranteed that:
     * <br>
     * <ul>
     * <li>this method is invoked from the EDT;
     * <li>returned Action is invoked NOT from the EDT.
     * <br>
     * So this method should be fast. It is up to Handler implementor to decide
     * either cache result or not ...
     * 
     * @param actionContext
     * @param cfg
     * @return Future action or NULL if not applicable
     */
    public abstract FutureAction createActionFor(final Lookup actionContext, final Configuration cfg);

    public final String getID() {
        return id;
    }

    public abstract void applyChanges(Configuration cfg);
}
