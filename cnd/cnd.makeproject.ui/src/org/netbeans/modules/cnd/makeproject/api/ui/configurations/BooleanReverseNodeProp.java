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

package org.netbeans.modules.cnd.makeproject.api.ui.configurations;

import org.openide.nodes.PropertySupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;

/**
 * node property to treat 'true' as 'false' and vice versa
 */
public class BooleanReverseNodeProp extends PropertySupport<Boolean> {
    private final BooleanConfiguration booleanConfiguration;

    public BooleanReverseNodeProp(BooleanConfiguration booleanConfiguration, boolean canWrite, String name1, String name2, String name3) {
        super(name1, Boolean.class, name2, name3, true, canWrite);
        this.booleanConfiguration = booleanConfiguration;
    }

    @Override
    public String getHtmlDisplayName() {
        if (!booleanConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        } else {
            return null;
        }
    }

    @Override
    public Boolean getValue() {
        return !booleanConfiguration.getValue();
    }

    @Override
    public void setValue(Boolean v) {
        booleanConfiguration.setValue(!v);
    }

    @Override
    public void restoreDefaultValue() {
        booleanConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return booleanConfiguration.getModified();
    }
}
