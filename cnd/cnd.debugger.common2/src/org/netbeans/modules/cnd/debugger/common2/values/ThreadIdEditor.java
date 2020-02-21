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

package org.netbeans.modules.cnd.debugger.common2.values;

/*
 * PropertyEditor for thread ids.
 */

public class ThreadIdEditor extends AsyncEditor {

    @Override
    public void setAsText(String newText) {
	// Called when an edit is commited through user action

	// SHOULD deal with Java tid's LATER

	AId tid = new AId(newText, false, false);
	if (tid.errorMessage != null)
	    badValue(tid.errorMessage);

	notePending(newText);

	// Propagate the new value to the node property setter which
	// will forward the property to the engine.
	setValue(tid.toString());
    }
}

