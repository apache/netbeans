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

import org.netbeans.modules.cnd.api.picklist.DefaultPicklistModel;

public class ComboStringConfiguration implements Cloneable {

    private ComboStringConfiguration master;
    private String def;
    private String value;
    private boolean modified;
    private boolean dirty = false;
    private DefaultPicklistModel picklist;

    public ComboStringConfiguration(ComboStringConfiguration master, String def, DefaultPicklistModel picklist) {
        this.master = master;
        this.def = def;

        this.picklist = picklist;
        reset();
    }

    /** Needed for CompilerSetConfiguration to maintain compatibility */
    protected ComboStringConfiguration() {
    }

    public void setMaster(ComboStringConfiguration master) {
        this.master = master;
    }

    public void setValue(String value) {
        value = value.trim();
        this.value = value;
        if (master != null) {
            setModified(true);
        } else {
            setModified(!value.equals(getDefault()));
        }
    }

    public String getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
    }

    public final void setModified(boolean b) {
        this.modified = b;
    }

    public boolean getModified() {
        return modified;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    public String getDefault() {
        return def;
    }

    public void setDefault(String def) {
        this.def = def;
        setModified(!value.equals(getDefault()));
    }

    public final void reset() {
        value = getDefault();
        setModified(false);
    }

    public DefaultPicklistModel getPicklist() {
        return picklist;
    }

    // Clone and Assign
    public void assign(ComboStringConfiguration conf) {
        dirty = !getValue().equals(conf.getValue());
        picklist = (DefaultPicklistModel)conf.getPicklist().clonePicklist();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public ComboStringConfiguration clone() {
        ComboStringConfiguration clone = new ComboStringConfiguration(master, def, (DefaultPicklistModel)picklist.clonePicklist());
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    @Override
    public String toString() {
        return "(" + getValue() + ")"; // NOI18N
    }

}
