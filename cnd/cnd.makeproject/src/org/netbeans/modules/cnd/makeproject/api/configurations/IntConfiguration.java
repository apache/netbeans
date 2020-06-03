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

public class IntConfiguration implements Cloneable {

    private IntConfiguration master;
    private byte def;
    private String[] names;
    private String[] options;
    private byte value;
    private byte previousValue;
    private boolean modified;
    private boolean dirty = false;

    public IntConfiguration(IntConfiguration master, int def, String[] names, String[] options) {
        this.master = master;
        this.def = (byte) def;
        this.names = names;
        this.options = options;
        reset();
    }
    
    /** Needed for CompilerSetConfiguration to maintain compatibility */
    protected IntConfiguration() {
    }

    public void setMaster(IntConfiguration master) {
        this.master = master;
    }

    public void setValue(int value) {
        this.previousValue = this.value;
        this.value = (byte) value;
        if (master != null) {
            setModified(true);
        } else {
            setModified(value != getDefault());
        }
    }

    public void setValue(String s) {
        if (s != null) {
            for (int i = 0; i < names.length; i++) {
                if (s.equals(names[i])) {
                    setValue(i);
                    break;
                }
            }
        }
    }

    public int getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return value;
        }
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

    public int getDefault() {
        return def;
    }

    public void setDefault(int def) {
        this.def = (byte) def;
        setModified(value != getDefault());
    }

    public void reset() {
        previousValue = value;
        value = (byte) getDefault();
        setModified(false);
    }

    public String getName() {
        if (getValue() < names.length) {
            return names[getValue()];
        } else {
            return "???"; // FIXUP // NOI18N
        }
    }

    public String[] getNames() {
        return names;
    }

    public String getOption() {
        return options[getValue()] + " "; // NOI18N
    }

    // Clone and Assign
    public void assign(IntConfiguration conf) {
        dirty = getValue() != conf.getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    @org.netbeans.api.annotations.common.SuppressWarnings("CN") // each subclass implemented Clonable must override this method
    public IntConfiguration clone() {
        IntConfiguration clone = new IntConfiguration(master, def, names, options);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }

    public byte getPreviousValue() {
        return previousValue;
    }

    @Override
    public String toString() {
        return "(" + getValue() + ")" + getName(); // NOI18N
    }

}
