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

import java.util.*;


public abstract class OptionSetSupport implements OptionSet {

    private Hashtable<String, OptionValue> nameMap =
	new Hashtable<String, OptionValue>(50);

    private ArrayList<OptionValue> values = new ArrayList<OptionValue>();

    @Override
    public ArrayList<OptionValue> values() { return values; }

    protected OptionSetSupport() {
    }

    protected void setup(Option[] options) {
	for (int ox = 0; ox < options.length; ox++) {
	    Option opt = options[ox];

	    OptionValue ov = new OptionValue(this, opt, opt.getDefaultValue());
	    values.add(ov);
	    nameMap.put(opt.getName(), ov);
	}
    } 

    /**
     * Assign option values form that into this.
     * @param copying If true it means we are copying/cloning and the dirty bit
     * should be copied.
     * @param nonClient If true only copy non-client options.
     */

    private void assignHelp(OptionSet that,
			    boolean copying, boolean nonClient) {

	if (this.getClass() != that.getClass())
	    return;

	ArrayList<OptionValue> these = this.values();
	ArrayList<OptionValue> those = that.values();

	if (these.size() != those.size())
	    return;

	for (int ox = 0; ox < these.size(); ox++) {
	    OptionValue thisOpt = these.get(ox);
	    OptionValue thatOpt = those.get(ox);
	    if (thisOpt.type() != thatOpt.type())
		continue;
	    if (nonClient && thisOpt.type().isClientOption())
		continue;
	    if (copying) {
		thisOpt.setInitialValue(thatOpt.get());
		thisOpt.setDirty(thatOpt.isDirty());
	    } else {
		// won't dirty unless values differ
		thisOpt.set(thatOpt.get());
	    }
	}
    }

    /**
     * Assign option values form that into this.
     * New values are marked dirty if they change due to assignment.
     */

    @Override
    public void assign(OptionSet that) {
	assignHelp(that, false, false);
    }


    /**
     * Assign non-client option values from 'that' into 'this'.
     */

    @Override
    public void assignNonClient(OptionSet that) {
	assignHelp(that, false, true);
    }


    /**
     * Copy option values form that into this. For use by copy constructors.
     * Dirty bits of values are copied.
     */

    protected void copy(OptionSet that) {
	assignHelp(that, true, false);
    }



    /**
     * Given the name of the option, find its instance
     */

    @Override
    public OptionValue byName(String name) {
	return nameMap.get(name);
    }

    @Override
    public OptionValue byType(Option type) {
	for (OptionValue o : values) {
	    if (o.type() == type)
		return o;
	}
	return null;
    }

    @Override
    public boolean isDirty() {
	for (OptionValue o : values) {
	    if (o.isDirty()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void clearDirty() {
	for (OptionValue o : values) {
	    o.setDirty(false);
	}
    }

    @Override
    public void applyTo(OptionClient client) {
	if (client == null)
	    return;

	for (OptionValue o : values)
	    o.applyTo(client);
    }

    /*
     * Mark all values as having been applied.
     */
    @Override
    public void doneApplying() {
	for (OptionValue o : values)
	    o.setDirty(false);
    }

    /**
     * Mark all options which are not the same as the defaults
     * as "dirty" (such that an apply will set them).
     */
    @Override
    public void markChanges() {
	for (OptionValue o : values)
	    o.setDirty(true);
    }


    /**
     * Mark options in 'this' set as dirty if they differ from the
     * corresponding options in 'that'.
     */

    @Override
    public void deltaWithRespectTo(OptionSet that) {

	if (this.getClass() != that.getClass())
	    return;

	ArrayList<OptionValue> these = this.values();
	ArrayList<OptionValue> those = that.values();

	if (these.size() != those.size())
	    return;

	for (int ox = 0; ox < these.size(); ox++) {
	    OptionValue thisOpt = these.get(ox);
	    OptionValue thatOpt = those.get(ox);
	    if (thisOpt.type() != thatOpt.type())
		continue;
	    thisOpt.deltaWithRespectTo(thatOpt);
	}
    }

    // XML persistence support

    /** Has the options set changed such that we need to save */
    protected boolean needSave = false;

    void needSave() {
	needSave = true;
    } 

    @Override
    abstract public void save();
    @Override
    abstract public void open();
}
