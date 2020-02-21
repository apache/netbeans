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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import org.xml.sax.Attributes;

import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.debugger.Log;

public class EngineProfileXMLCodec extends XMLDecoder implements XMLEncoder {

    private final static int thisversion = 1;

    private EngineProfile profile;

    static final String TAG_ENGINE = "engine"; // NOI18N

    public EngineProfileXMLCodec(EngineProfile profile) {
	this.profile = profile;
    } 

    // interface XMLDecoder
    @Override
    public String tag() {
	return profile.getId();
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
        String what = "Engine profile"; // NOI18N
        int maxVersion = 1;
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    @Override
    public void end() {
        profile.clearChanged();
    }


    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
	if (Log.XML.debug) {
	    System.out.println("  startElement: " + element); // NOI18N
	    System.out.println("  startElement atts: " + atts); // NOI18N
	}
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.XML.debug) {
	    System.out.println("  endElement: " + element); // NOI18N
	    System.out.println("  endElement: " + currentText); // NOI18N
	}

	if (element.equals(TAG_ENGINE)) {
	    profile.setEngineByID(currentText);
	}
    }

    // intrface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(tag(), thisversion);
	xes.element(TAG_ENGINE, profile.getEngineType().getDebuggerID());
	xes.elementClose(tag());
    }
}
