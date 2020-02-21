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

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;


/**
 * Explicit properties.
 *
 * The standard bean model has implicit properties that are 
 * based on naming conventions. There a property is typically implemented as
 * a private field and get/set/is accessors. Furthermore the set method
 * can fire property change notification.
 *
 * This class is a base class for explicit properties. For example:
 *	public IntegerProperty id =
 *		new IntegerProperty(this, "id", null, false, 0);
 *
 * 'owner' allows for registration of this property with an owner which, in
 * turn, provides iteration through properties and their lookup by name or key.
 *
 * 'name' is the name of the property and it's main purpose is for XML
 * persistence. It's looked up using equals().
 *
 * 'key' is used for binding the property to presentation layers like 
 * property sheets and tables and so on where the names might be
 * provided for us and/or follow this convention:
 *	public static final String PROP_BREAKPOINT_ID = "PROP_BREAKPOINT_ID"
 * such that quick, == based, lookup is preferred. Also because of the
 * final variable indirection the actual string may change and in such
 * cases any use of it in persistence will be troublesome.
 *
 * 'readonly' has to do with presentation to the user. The property is
 * programmatically always settable. readonly help remember wheter the user
 * can set it (through a property sheet etc).
 *
 * TBD:
 * One of the main motivations for using this scheme was to have a dirty bit
 * which is yet to be added. The dirty bit is an alternative approach to 
 * property change notifications and is easier to manage.
 *
 * A property can be gotten or set generically as a String or an Object.
 */

public abstract class Property {

    private final PropertyOwnerSupport owner;
    private final String name;
    private final String key;
    private final boolean readOnly;
    private /* LATER final */ boolean differentiating;

    private boolean dirty;

    protected Property(PropertyOwnerSupport owner,
		       String name, String key, boolean readOnly) {
	this.owner = owner;
	this.name = name;
	this.key = key;
	this.readOnly = readOnly;
	this.differentiating = true;

	owner.register(this);
    }

    public void setDifferentiating(boolean differentiating) {
	this.differentiating = differentiating;
    }

    public boolean isDifferentiating() {
	return differentiating;
    }

    public boolean isReadOnly() {
	return readOnly;
    }

    public String name() {
	return name;
    }

    public String key() {
	return key;
    }

    public boolean matches(Property that) {
	if (!IpeUtils.sameString(this.name, that.name))
	    return false;

	// if names are equal so should these
	assert this.readOnly == that.readOnly;
	assert IpeUtils.sameString(this.key, that.key);

	return IpeUtils.sameString(this.toString(), that.toString());
    }


    public abstract Object getAsObject();
    @Override
    public abstract String toString();

    public void setFromObject(Object o) {
	setFromObjectImpl(o);
	setDirty();
    }

    public void setFromObjectInitial(Object o) {
	setFromObjectImpl(o);
    }

    public void setFromString(String s) {
	setFromStringImpl(s);
	setDirty();
    }

    protected void setDirty() {
	owner.setDirty();
	this.dirty = true;
    }

    public boolean isDirty() {
	return dirty;
    }

    protected abstract void setFromObjectImpl(Object o);
    protected abstract void setFromStringImpl(String s);
}

