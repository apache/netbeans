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

package org.netbeans.modules.cnd.debugger.common2.debugger;


import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;


public class WatchesXMLCodec extends XMLDecoder implements XMLEncoder {

    private WatchBag bag;	// ... to store watches into

    static private final String TAG_WATCHES = "watches";// NOI18N

    public WatchesXMLCodec(WatchBag bag) {
	this.bag = bag;
	registerXMLDecoder(new WatchXMLCodec(bag));
    }

    // interface XMLDecoder
    @Override
    public String tag() {
	return TAG_WATCHES;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	String what = "watch list"; // NOI18N
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
	xes.elementOpen(TAG_WATCHES, version());
	    for (NativeWatch w : bag.getWatches()) {
		NativeDebugger debugger = NativeDebuggerManager.get().currentNativeDebugger();
		WatchVariable dw = w.findByDebugger(debugger);
		if (dw != null ) {
                    WatchXMLCodec encoder = new WatchXMLCodec(w);
                    encoder.encode(xes);
		}
	    }
	xes.elementClose(TAG_WATCHES);
    }
}
