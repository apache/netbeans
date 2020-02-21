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
package org.netbeans.modules.cnd.makeproject.ui.configurations;

import org.openide.nodes.PropertySupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;

public class StringNodeProp extends PropertySupport<String> {

    private final StringConfiguration stringConfiguration;
    private String def = null;
    private boolean canWrite = true;

    public StringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
    }

    public StringNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
        this.def = def;
    }

    public StringNodeProp(StringConfiguration stringConfiguration, String def, boolean canWrite, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, canWrite);
        this.stringConfiguration = stringConfiguration;
        this.def = def;
    }

    @Override
    public String getHtmlDisplayName() {
        if (stringConfiguration.getModified()) {
            return "<b>" + getDisplayName(); // NOI18N
        }
        else {
            return null;
        }
    }

    @Override
    public String getValue() {
        return stringConfiguration.getValueDef(def);
    }

    @Override
    public void setValue(String v) {
        stringConfiguration.setValue(v);
    }

    @Override
    public void restoreDefaultValue() {
        stringConfiguration.reset();
    }

    @Override
    public boolean supportsDefaultValue() {
        return true;
    }

    @Override
    public boolean isDefaultValue() {
        return !stringConfiguration.getModified();
    }
    
    public void setDefaultValue(String def) {
        this.def = def;
        stringConfiguration.setDefaultValue(def);
    }

    @Override
    public boolean canWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean v) {
        canWrite = v;
    }
}
