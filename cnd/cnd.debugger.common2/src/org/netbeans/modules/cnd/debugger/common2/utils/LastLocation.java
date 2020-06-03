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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.Point;

import javax.swing.JDialog;

import org.openide.util.Utilities;


/*
 * Remember location of dialog so it comes up in the same place next time.
 *
 * This class is an encapsulation of a bunch fields and code which was
 * (sort-of) used by various debug target and host dialogs.
 * The encapsulation accurately captures the original semantics, even though 
 * they didn't make much sense.
 *
 * Currently IpeUtils.createDialog() uses dialog.setLocationRelativeTo(parent);
 */
public class LastLocation {
    private Point location;	// Acts as a flag for whether location has
				// been remembered yet.
    private int width;
    private int height;

    public LastLocation(int width, int height) {
	this.width = width;
	this.height = height;
    }

    public void rememberFrom(JDialog dialog) {
	location = dialog.getLocation();
	width = dialog.getWidth();
	height = dialog.getHeight();
    }

    public void applyto(JDialog dialog) {
	if (location != null) {
	    dialog.setLocation(location);
	} else {
	    dialog.setBounds(Utilities.findCenterBounds(dialog.getSize()));
	}
    }
}
