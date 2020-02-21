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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordList;

import org.netbeans.modules.cnd.debugger.common2.debugger.Log;

public class DebugTargetsXMLCodec extends XMLDecoder implements XMLEncoder {

    private final RecordList<DebugTarget> model;

    static private final String TAG_DEBUGTARGETS = "debugtargets";// NOI18N
    private static final int VERSION = 2;

    public DebugTargetsXMLCodec(RecordList<DebugTarget> model) {
	this.model = model;
	registerXMLDecoder(new DebugTargetXMLCodec(model));
    }

    // interface XMLDecoder
    @Override
    public String tag() {
	return TAG_DEBUGTARGETS;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	String what = "debugtarget list"; // NOI18N
	checkVersion(atts, what, VERSION);
    }

    // interface XMLDecoder
    @Override
    public void end() {
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
	if (Log.XML.debug)
	    System.out.println("DebugTargetsXMLCodec: " + element); // NOI18N
	if (element.equals(TAG_DEBUGTARGETS)) {
            //skip;
	}
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
    }

    private static int version() {
	return VERSION;
    } 

    /* 
     * Encode
     * interface XMLEncoder
     */
    @Override
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(TAG_DEBUGTARGETS, version());
	    int index = 1;
	    for (DebugTarget dt : model) {
                DebugTargetXMLCodec encoder = new DebugTargetXMLCodec(dt, index++);
                encoder.encode(xes);
	    }
	xes.elementClose(TAG_DEBUGTARGETS);
    }
}
