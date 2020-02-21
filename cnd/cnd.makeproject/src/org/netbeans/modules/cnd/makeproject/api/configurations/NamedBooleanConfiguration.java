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
public class NamedBooleanConfiguration extends BooleanConfiguration implements Cloneable {

    private final String falseValue;
    private final String trueValue;

    public NamedBooleanConfiguration(boolean def, String falseValue, String trueValue) {
        super(def);
        this.falseValue = falseValue;
        this.trueValue = trueValue;
    }

    public String getOption() {
        if (getValue()) {
            return trueValue;
        } else {
            return falseValue;
        }
    }

    // Clone and Assign
    public void assign(NamedBooleanConfiguration conf) {
        super.assign(conf);
    }

    @Override
    public NamedBooleanConfiguration clone() {
        NamedBooleanConfiguration clone = new NamedBooleanConfiguration(getDefault(), falseValue, trueValue);
        clone.setValue(getValue());
        clone.setModified(getModified());
        return clone;
    }
}
