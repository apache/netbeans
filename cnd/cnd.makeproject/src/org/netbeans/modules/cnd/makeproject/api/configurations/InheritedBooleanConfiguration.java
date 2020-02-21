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

/**
 *
 */
public class InheritedBooleanConfiguration extends BooleanConfiguration implements Cloneable {

    private InheritedBooleanConfiguration master;

    public InheritedBooleanConfiguration(InheritedBooleanConfiguration master, boolean def) {
        super(def);
        this.master = master;
    }

    protected InheritedBooleanConfiguration() {
    }

    protected BooleanConfiguration getMaster() {
        return master;
    }

    public void setMaster(InheritedBooleanConfiguration master) {
        this.master = master;
    }

    @Override
    public void setValue(boolean b) {
        super.setValue(b);
        if (master != null) {
            setModified(true);
        }
    }

    @Override
    public boolean getValue() {
        if (master != null && !getModified()) {
            return master.getValue();
        } else {
            return super.getValue();
        }
    }

    // Clone and Assign
    public void assign(InheritedBooleanConfiguration conf) {
        super.assign(conf);
    }

    @Override
    public InheritedBooleanConfiguration clone() {
        InheritedBooleanConfiguration clone = new InheritedBooleanConfiguration(master, super.getDefault());
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }
}
