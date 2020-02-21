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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;

public class StringConfiguration implements Cloneable {

    private StringConfiguration master;
    private String def;
    private String value;
    private boolean modified;

    public StringConfiguration(StringConfiguration master, String def) {
        this.master = master;
        this.def = def;
        reset();
    }

    protected StringConfiguration() {
    }

    public void setMaster(StringConfiguration master) {
        this.master = master;
    }

    public void setValue(String b) {
        if (b == null) {
            b = ""; // NOI18N
        }
        this.value = b;
        if (master != null) {
            setModified(true);
        } else {
            setModified(!b.equals(getDefault()));
        }
    }

    public String getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public String getValueDef(String def) {
        if (master != null && !getModified() && !master.getModified() && def != null) {
            return def;
        }
        if (master != null && !getModified()) {
            return master.getValue();
        } else if (!getModified() && def != null) {
            return def;
        } else {
            return value;
        }
    }

    public String getValue(String delim) {
        StringBuilder ret = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(getValue());
        while (tokenizer.hasMoreTokens()) {
            ret.append(tokenizer.nextToken());
            if (tokenizer.hasMoreTokens()) {
                ret.append(delim);
            }
        }
        return ret.toString();
    }

    public void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public String getDefault() {
        return def;
    }

    public void reset() {
        value = getDefault();
        setModified(false);
    }

    public void setDefaultValue(String def) {
        this.def = def;
    }

    // Clone and Assign
    public void assign(StringConfiguration conf) {
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public StringConfiguration clone() {
        StringConfiguration clone = new StringConfiguration(master, def);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    @Override
    public String toString() {
        return "{value=" + value + " modified=" + modified + '}'; // NOI18N
    }
}
