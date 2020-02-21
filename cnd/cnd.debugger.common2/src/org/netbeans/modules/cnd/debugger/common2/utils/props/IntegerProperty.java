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


package org.netbeans.modules.cnd.debugger.common2.utils.props;

public class IntegerProperty extends Property {
    
    private int value = 0;

    public IntegerProperty(PropertyOwnerSupport po,
			   String name, String key, boolean ro,
			   int initialValue) {
	super(po, name, key, ro);
	value = initialValue;
    }

    // interface Property
    @Override
    protected void setFromStringImpl(String s) {
	value = Integer.parseInt(s);
    }

    // interface Property
    @Override
    public final String toString() {
	return Integer.toString(value);
    } 

    // interface Property
    @Override
    protected void setFromObjectImpl(Object o) {
	value = ((Integer) o).intValue();
    }

    // interface Property
    @Override
    public Object getAsObject () {
	return new Integer(value);
    }

    public final void set(int i) {
	value = i;
	setDirty();
    }

    public final int get() {
	return value;
    }
}

