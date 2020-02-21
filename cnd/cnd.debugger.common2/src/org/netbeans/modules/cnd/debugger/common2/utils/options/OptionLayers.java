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

import java.util.ArrayList;
import java.util.Stack;

public class OptionLayers implements OptionSet {

    private Stack<OptionSet> layer = new Stack<OptionSet>();

    public OptionLayers(OptionSet bottommost) {
	assert bottommost != null :
	       "OptionLayers.<init>(): null bottommost";	// NOI18n // NOI18N
	layer.push(bottommost);
    }

    public void push(OptionSet next) {
	assert next != null :
	       "OptionLayers.push(): null next";	// NOI18n // NOI18N
	layer.push(next);
    }

    @Override
    public void save() {
	for (OptionSet os : layer)
	    os.save();
    }

    @Override
    public void open() {
	for (OptionSet os : layer)
	    os.open();
    }

    @Override
    public void markChanges() {
	for (OptionSet os : layer)
	    os.markChanges();
    }

    @Override
    public void doneApplying() {
	for (OptionSet os : layer)
	    os.doneApplying();
    }

    @Override
    public void applyTo(OptionClient client) {
	for (OptionSet os : layer)
	    os.applyTo(client);
    }

    @Override
    public boolean isDirty() {
	for (OptionSet os : layer) {
	    if (os.isDirty()) {
		return true;
	    }
	}
	return false;
    }

    @Override
    public void clearDirty() {
	for (OptionSet os : layer)
	    os.clearDirty();
    }

    @Override
    public OptionValue byType(Option type) {
	OptionValue ov = null;
	for (OptionSet os : layer) {
	    ov = os.byType(type);
	    if (ov != null)
		break;
	}
	return ov;
    }

    @Override
    public OptionValue byName(String name) {
	OptionValue ov = null;
	for (OptionSet os : layer) {
	    ov = os.byName(name);
	    if (ov != null)
		break;
	}
	return ov;
    }

    @Override
    public void deltaWithRespectTo(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void assign(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void assignNonClient(OptionSet that) {
	throw new UnsupportedOperationException();
    }

    @Override
    public OptionSet makeCopy() {
	throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<OptionValue> values() {
	throw new UnsupportedOperationException();
    }

    @Override
    public String tag() {
	throw new UnsupportedOperationException();
    }

    @Override
    public String description() {
	throw new UnsupportedOperationException();
    }
}
