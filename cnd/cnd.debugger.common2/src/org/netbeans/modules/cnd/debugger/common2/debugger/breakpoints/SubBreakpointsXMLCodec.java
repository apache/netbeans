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
import org.openide.ErrorManager;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;
import org.netbeans.spi.debugger.ui.BreakpointType;


class SubBreakpointsXMLCodec extends XMLDecoder implements XMLEncoder {

    private BreakpointXMLCodec parentCodec;
    private HashMap<String, BreakpointType> types;

    private NativeBreakpoint parent;

    static private final String TAG_SUBBPTS = "subbpts";// NOI18N


    /**
     * decoder form
     */
    public SubBreakpointsXMLCodec(BreakpointXMLCodec parentCodec,
				  HashMap<String, BreakpointType> types) {
	this.parentCodec = parentCodec;
	this.types = types;
    }

    /**
     * encoder form
     */
    public SubBreakpointsXMLCodec(NativeBreakpoint parent) {
	this.parent = parent;
    }

    // interface XMLDecoder
    @Override
    public String tag() {
	return TAG_SUBBPTS;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	// DEBUG System.out.printf("SubBreakpointsXMLCodec().start(%s)\n", tag());

	String what = "subbpt list"; // NOI18N
	int maxVersion = 1;
	checkVersion(atts, what, maxVersion);

	registerXMLDecoder(new BreakpointXMLCodec(null,
				                  parentCodec.currentBreakpoint(),
						  types));
    }

    // interface XMLDecoder
    @Override
    public void end() {
	// DEBUG System.out.printf("SubBreakpointsXMLCodec().end(%s)\n", tag());
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
	if (parent.nChildren() == 0)
	    return;
	xes.elementOpen(TAG_SUBBPTS, version());
	try {
	    for (NativeBreakpoint b : parent.getChildren()) {
		BreakpointXMLCodec encoder = new BreakpointXMLCodec(b);
		encoder.encode(xes);
	    }
	} catch (Exception x) {
	    ErrorManager.getDefault().annotate(x,
		"Failed to encode sub-bpt into XML"); // NOI18N
	    ErrorManager.getDefault().notify(x);
	}
	xes.elementClose(TAG_SUBBPTS);
    }
}
