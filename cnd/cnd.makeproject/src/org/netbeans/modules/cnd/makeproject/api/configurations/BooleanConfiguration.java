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

public class BooleanConfiguration implements Cloneable {

    private boolean def;
    private boolean value;
    private boolean modified;
    private boolean dirty = false;

    public BooleanConfiguration(boolean def) {
        this.def = def;
        reset();
    }

    protected BooleanConfiguration() {
    }

    public void setValue(boolean b) {
        this.value = b;
        setModified(b != getDefault());
    }

    public boolean getValue() {
        return value;
    }

    public void setModified(boolean b) {
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

    public boolean getDefault() {
        return def;
    }

    public void setDefault(boolean b) {
        def = b;
        setModified(value != def);
    }

    public void reset() {
        value = getDefault();
        setModified(false);
    }

    // Clone and Assign
    public void assign(BooleanConfiguration conf) {
        dirty |= conf.getValue() ^ getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
    public BooleanConfiguration clone() {
        BooleanConfiguration clone = new BooleanConfiguration(def);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    @Override
    public String toString() {
        return "{value=" + value + " modified=" + modified + " dirty=" + dirty +  '}'; // NOI18N
    }
}
