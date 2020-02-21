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

public class StringProperty extends Property {
    
    protected String value = null;

    public static class Tracking extends StringProperty {
	public Tracking(PropertyOwnerSupport po, String name, String key, boolean ro, String initialValue) {
	    super(po, name, key, ro, initialValue);
	    System.out.println("PROP " + name + " initial value = " + initialValue); // NOI18N
	}

        @Override
	protected void setFromStringImpl(String s) {
	    super.setFromStringImpl(s);
	    System.out.println("PROP " + name() + " new value = " + value); // NOI18N
	}

        @Override
	protected void setFromObjectImpl(Object o) {
	    super.setFromObjectImpl(o);
	    System.out.println("PROP " + name() + " new value = " + o); // NOI18N
	}

        @Override
	public final void set(String s) {
	    super.set(s);
	    System.out.println("PROP " + name() + " new value = " + s); // NOI18N
	}
    }

    public StringProperty(PropertyOwnerSupport po, String name, String key, boolean ro,
			  String initialValue) {
	super(po, name, key, ro);
	value = initialValue;
    }

    // interface Property
    @Override
    protected void setFromStringImpl(String s) {
	value = s;
    }

    // interface Property
    @Override
    public final String toString() {
	return value;
    } 

    // interface Property
    @Override
    protected void setFromObjectImpl(Object o) {
	if (o != null)
	    value = ((String) o).valueOf(o);
        else
	    value = null;
    }

    // interface Property
    @Override
    public final Object getAsObject () {
	return value;
    }

    public void set(String s) {
	value = s;
	setDirty();
    }

    public final String get() {
	return value;
    }
}

