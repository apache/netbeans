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

import java.util.Vector;

/**
 * Holds a set of OptionUI's visible in a dialog or panel.
 */

public class UISet {
    private Vector<OptionUI> uis = new Vector<OptionUI>();

    public void add(OptionUI newUis[]) {
	for (int x = 0; x < newUis.length; x++)
	    uis.add(newUis[x]);
    }

    public void add(OptionUI ui) {

	if (ui instanceof SubcategoryOptionUI) {
	    SubcategoryOptionUI sui = (SubcategoryOptionUI) ui;
	    for (int suix = 0; suix < sui.optionUIs.length; suix++)
		uis.add(sui.optionUIs[suix]);

	} else {
	    uis.add(ui);
	}
    }

    /**
     * Transfer the values stored in the UI's to the OptionValue's.
     *
     * (Called when OK is pressed in the global options dialog?)
     */

    public void applyChanges() {
	for (OptionUI ui : uis) {
	    ui.applyChanges();
	}
    }

    /**
     * Bind the UI's to a new "model".
     * Have Each UI point to the corresponding OptionValue in 'options'.
     */

    public void bind(OptionSet options) {
	for (OptionUI ui : uis) {
	    ui.bind(options);
	}
    }
}

