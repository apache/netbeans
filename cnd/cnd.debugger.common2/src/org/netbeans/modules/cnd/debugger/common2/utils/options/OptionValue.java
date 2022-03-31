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

package org.netbeans.modules.cnd.debugger.common2.utils.options;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

/**
 * Holds the textual value of an option and a dirty bit.
 */

public class OptionValue {

    private final OptionSetSupport owner;// Set to which we belong
    private final Option type;		// ... back pointer
    private final String defaultValue;	// initial value

    private String currValue;		// current value

    private boolean dirty = false;	// true when option has been changed
					// but not applied

    OptionValue(OptionSetSupport owner, Option type, String initialValue) {
	this.owner = owner;
	this.type = type;
	this.defaultValue = initialValue;

	currValue = initialValue;
    }

    String getDefaultValue() {
	return defaultValue;
    }

    public String get() {
	return currValue;
    } 

    public OptionSet owner() {
	return owner;
    }

    public Option type() {
	return type;
    } 

    /**
     * @return true if the value was truly set
     */

    private boolean setHelp(String newValue) {
	if (type.isTrim() && newValue != null)
	    newValue = newValue.trim();
	if (currValue == newValue)
	    return false;
	if (currValue != null && currValue.equals(newValue))
	    return false;
	currValue = newValue;
	return true;
    }

    void setInitialValue(String newValue) {
	setHelp(newValue);
    }

    public void set(String newValue) {
	if (setHelp(newValue)) {
	    setDirty(true);
	    owner.needSave();
	}
    }


    /**
     * Mark 'this' option as dirty if it's value differs from 'that'.
     */

    void deltaWithRespectTo(OptionValue that) {
	if (IpeUtils.sameString(this.currValue, that.currValue)) {
	    setDirty(false);
	} else {
	    setDirty(true);
	}
    }

    void applyTo(OptionClient client) {
	if (dirty) {
	    if (type().isClientOption())
		client.apply(this);
	    // OLD dirty = false;
	}
    }

    public void setEnabled(boolean enabled) {
	assert type.isYesNoOption();

	if (enabled)
	    set("on");		// NOI18N
	else
	    set("off");		// NOI18N
    }

    public boolean isEnabled() {
	assert type.isYesNoOption();
	if (get().equals("on"))	// NOI18N
	    return true;
	else
	    return false;
    }

    void setDirty(boolean dirty) {
	this.dirty = dirty;
    } 

    public boolean isDirty() {
	return dirty == true;
    }

    @Override
    public String toString() {
        return currValue + (dirty ? " (dirty)" : ""); //NOI18N
    }
}
