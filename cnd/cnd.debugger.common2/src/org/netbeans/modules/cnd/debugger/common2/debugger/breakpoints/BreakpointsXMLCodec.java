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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.util.HashMap;
import java.util.List;

import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.api.debugger.DebuggerManager;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

class BreakpointsXMLCodec extends XMLDecoder implements XMLEncoder {

    private BreakpointBag bag;	// ... to store bpts into

    static private final String TAG_BREAKPOINTS = "breakpoints";// NOI18N

    // map a type name to a NativeBreakpointType
    final private HashMap<String, BreakpointType> types =
	new HashMap<String, BreakpointType>();

    public BreakpointsXMLCodec(BreakpointBag bag) {
	this.bag = bag;
	initializeTypeLookup();
	registerXMLDecoder(new BreakpointXMLCodec(bag, null, types));
    }

    /**
     * Initialize the type name to a NativeBreakpointType map.
     */
    private void initializeTypeLookup() {
        final List<? extends BreakpointType> breakpointTypes =
                DebuggerManager.getDebuggerManager().lookup(null, BreakpointType.class);
	if (breakpointTypes != null) {
            synchronized (breakpointTypes) {
                for (BreakpointType bt : breakpointTypes) {
                    String category = bt.getCategoryDisplayName();
                    if (!NativeBreakpointType.isOurs(category))
                        continue;
                    // can not check with instance of, because manager uses lazy class objects
                    // not our real registered NativeBreakpointTypes
    //		if (! (bt instanceof NativeBreakpointType))
    //		    continue;
                    types.put(((NativeBreakpointType)bt).id(), bt);

                    // for compatibility with old localized style
                    types.put(bt.getTypeDisplayName(), bt);
                }
            }
	}
    } 

    // interface XMLDecoder
    @Override
    public String tag() {
	return TAG_BREAKPOINTS;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	String what = "breakpoint list"; // NOI18N
	int maxVersion = 1;
	checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
    }

    private static int version() {
	return 1;
    } 

    // interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(TAG_BREAKPOINTS, version());
	    NativeBreakpoint[] breakpoints = bag.getBreakpoints();
	    for (int bx = 0; bx < breakpoints.length; bx++) {
		NativeBreakpoint b = breakpoints[bx];
		BreakpointXMLCodec encoder = new BreakpointXMLCodec(b);
		encoder.encode(xes);
	    }
	xes.elementClose(TAG_BREAKPOINTS);
    }
}
