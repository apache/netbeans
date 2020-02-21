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

import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandlerFactory;
import org.netbeans.modules.cnd.makeproject.spi.DebuggerChooserProvider;
import org.openide.util.Lookup;

public class DebuggerChooserConfiguration implements Cloneable {

    private static DebuggerChooserProvider storage;

    private int value;
    private boolean modified;
    private boolean dirty = false;

    public DebuggerChooserConfiguration(Lookup lookup) {
        if (storage == null) {
            storage = DebuggerChooserProvider.getInstance();
        }
        reset();
    }

    private DebuggerChooserConfiguration(DebuggerChooserConfiguration conf) {
        value = conf.value;
        setModified(false);
    }

    public void setValue(int value) {
        this.value = value;
        setModified(true);
    }

    public void setValue(String s) {
        if (s != null) {
            for (int i = 0; i < storage.getNames().length; i++) {
                if (s.equals(storage.getName(i))) {
                    setValue(i);
                    break;
                }
            }
        }
    }

    public int getValue() {
        return value;
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

    public int getDefault() {
        return storage.getDefault();
    }

    public final void reset() {
        value = getDefault();
        setModified(false);
    }

    public String getName() {
        if (getValue() < storage.getNames().length) {
            return storage.getName(getValue());
        } else {
            return "???"; // FIXUP // NOI18N
        }
    }

    public ProjectActionHandlerFactory getNode() {
        if (getValue() < storage.getNodesSize()) {
            return storage.getNode(getValue());
        } else {
            return null;
        }
    }

    public String[] getNames() {
        return storage.getNames();
    }

    // Clone and Assign
    public void assign(DebuggerChooserConfiguration conf) {
        dirty = getValue() != conf.getValue();
        setValue(conf.getValue());
        setModified(conf.getModified());
    }

    @Override
    public DebuggerChooserConfiguration clone() {
        return new DebuggerChooserConfiguration(this);
    }
}
