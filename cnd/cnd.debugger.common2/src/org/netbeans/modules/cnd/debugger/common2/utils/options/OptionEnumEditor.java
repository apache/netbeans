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

import java.lang.IllegalArgumentException;

import java.beans.PropertyEditorSupport;

import org.openide.ErrorManager;

/**
 * Editor for enums, but also plain text or enums+text.
 */

class OptionEnumEditor extends PropertyEditorSupport {
    OptionPropertySupport ops;
    public OptionEnumEditor(OptionPropertySupport ops) {
	this.ops = ops;
    }

    @Override
    public void setAsText(String text) {

	// text = text.trim();	Not all text should be blindly trimmed!

	Validity v = ops.getValidity(text);
	if (!v.isValid()) {
	    IllegalArgumentException e = new IllegalArgumentException(v.why());
	    // THe following will make it appear as a nice error dialog.
	    ErrorManager.getDefault().annotate(e,
					       ErrorManager.USER,
					       v.why(),
					       v.why(),
					       null,
					       null);
	    throw e;
	}

	setValue(text); // from PropertyEditorSupport
    }

    @Override
    public String getAsText() {
	return (String) getValue();
        //return ops.getValue();
    }

    @Override
    public String[] getTags() {
	/* example of what to return.
	String[] tags = new String[4];
	tags[0] = "8";
	tags[1] = "16";
	tags[2] = "32";
	tags[3] = "automatic";
	return tags;
	*/
	return ops.getValues();
    }
}
