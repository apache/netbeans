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

import org.openide.ErrorManager;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

public class OptionSetXMLCodec extends XMLDecoder implements XMLEncoder {

    static private final String TAG_OPTION = "option"; // NOI18N

    static private final String ATTR_OPTION_NAME = "name"; // NOI18N
    static private final String ATTR_OPTION_VALUE = "value"; // NOI18N


    private OptionSet optionSet;

    public OptionSetXMLCodec(OptionSet optionSet) {
	this.optionSet = optionSet;
    }

    // interface XMLDecoder
    @Override
    public String tag() {
	return optionSet.tag();
    }

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	String what = optionSet.description();
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
	if (TAG_OPTION.equals(element)) {
	    String name = atts.getValue(ATTR_OPTION_NAME);
	    OptionValue o = optionSet.byName(name);
	    if (o != null) {
		String value = atts.getValue(ATTR_OPTION_VALUE);
		o.setInitialValue(value);
	    } else {
		ErrorManager.getDefault().log("Warning: unknown option " + name); // NOI18N
	    }
	} 
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
    }

    // interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	xes.elementOpen(tag());
	    for (int ox = 0; ox < optionSet.values().size(); ox++) {
		OptionValue o = optionSet.values().get(ox);
		writeOption(xes, o);
	    }
	xes.elementClose(tag());
    }

    private void writeOption(XMLEncoderStream xes, OptionValue o) {
	// was: OptionValue.perhapsToXML()
	if (!o.type().persist(o))
	    return;
	String xmlval = o.get();
	String defaultval = o.getDefaultValue();
        String option_name = o.type().getName();
        if (!option_name.equals("stack_max_size") && !xmlval.equals(defaultval)) { // NOI18N
	    AttrValuePair optionAttrs[] = new AttrValuePair[] {
		new AttrValuePair(ATTR_OPTION_NAME, option_name),
		new AttrValuePair(ATTR_OPTION_VALUE, xmlval)
	    };
	    xes.element(TAG_OPTION, optionAttrs);
	}
    }
}
