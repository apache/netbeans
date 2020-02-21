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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetXMLCodec;
import java.util.Vector;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.xml.sax.Attributes;

public class RtcProfileXMLCodec extends XMLDecoder implements XMLEncoder {

    private final static int thisversion = 1;
    private OptionSetXMLCodec optionsXMLCodec;

    static final String TAG_OPTIONS = "options"; // NOI18N
    static final String TAG_OPTION = "option"; // NOI18N
    static final String ATTR_OPTION_NAME = "name"; // NOI18N
    static final String ATTR_OPTION_VALUE = "value"; // NOI18N

    static final String TAG_LOADOBJS = "loadobjs"; // NOI18N
    static final String TAG_LOADOBJ = "lo"; // NOI18N
    static final String ATTR_LOADOBJ_NAME = "name"; // NOI18N
    static final String ATTR_LOADOBJ_SKIP = "skip"; // NOI18N

    private RtcProfile profile;		// we decode into
    private Vector<Loadobj> loadobjs = new Vector<Loadobj>();


    public RtcProfileXMLCodec() {
    }
    
    public RtcProfileXMLCodec(RtcProfile profile) {
	this.profile = profile;
	optionsXMLCodec = new OptionSetXMLCodec(profile.getOptions());
	registerXMLDecoder(optionsXMLCodec);
    } 

    // interface XMLDecoder
    public String tag() {
	return RtcProfile.ID;
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
	String what = "rtc profile"; // NOI18N
	int maxVersion = 1;
	checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    public void end() {
	profile.clearChanged();
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
    /* DEBUG
	System.out.println("RtcProfileXMLCodec  element: " + element);
	System.out.println("RtcProfileXMLCodec atts: " + atts);
    */
	if (element.equals(TAG_OPTION)) {
	    String optname = atts.getValue(ATTR_OPTION_NAME);
	    String optvalue = atts.getValue(ATTR_OPTION_VALUE);
	    if (optname == null)
		return;
	    optionsXMLCodec.startElement(element, atts);
	}

	if (element.equals(TAG_LOADOBJS)) {
	}

	if (element.equals(TAG_LOADOBJ)) {
	    Loadobj l = new Loadobj();
	    l.lo = atts.getValue(ATTR_LOADOBJ_NAME);
	    l.skip = Boolean.parseBoolean(atts.getValue(ATTR_LOADOBJ_SKIP));
	    loadobjs.add(l);
	}
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
    /* DEBUG
	System.out.println("  endElement: " + element);
	System.out.println("  endElement: " + currentText);
    */
	if (element.equals(TAG_LOADOBJS)) {
	    Loadobj[] vars = new Loadobj[loadobjs.size()];
	    profile.getLoadobjs().setXMLLoadobjs(loadobjs.toArray(vars));
	    profile.getLoadobjs().adjustLoadobjs();
	    loadobjs.clear();
	} else if (element.equals(TAG_LOADOBJ)) {
	}
    }

    // intrface XMLEncoder
    public void encode(XMLEncoderStream xes) {

	xes.elementOpen(tag(), thisversion);

	writeOptionsBlock(xes);
	writeLoadobjsBlock(xes);

	xes.elementClose(tag());
    }

    private void writeOptionsBlock(XMLEncoderStream xes) {
	xes.elementOpen(TAG_OPTIONS);
	optionsXMLCodec.encode(xes);
	xes.elementClose(TAG_OPTIONS);
    }

    private void writeLoadobjsBlock(XMLEncoderStream xes) {
	Loadobj[] los = profile.getLoadobjs().los;
	if (los == null)
	    return;
	
	xes.elementOpen(TAG_LOADOBJS);
	Vector<Loadobj> xml_los = new Vector<Loadobj>();
	int size = los.length;
	for (int i = 0; i < size; i++) {
	    AttrValuePair loadobjAttrs[] = new AttrValuePair[] {
		new AttrValuePair(ATTR_LOADOBJ_NAME,  los[i].lo),
		new AttrValuePair(ATTR_LOADOBJ_SKIP,  Boolean.toString(los[i].skip)),
	    };
	    xes.element(TAG_LOADOBJ, loadobjAttrs);

	    Loadobj l = new Loadobj();
	    l.lo = los[i].lo;
	    l.skip = los[i].skip;
	    xml_los.add(l);
	}

	Loadobj[] vars = new Loadobj[xml_los.size()];
	profile.getLoadobjs().setXMLLoadobjs(xml_los.toArray(vars));
	xes.elementClose(TAG_LOADOBJS);
    }
}
