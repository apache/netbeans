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


import org.openide.ErrorManager;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

class WatchXMLCodec extends XMLDecoder implements XMLEncoder {

    private WatchBag bag;		// ... to store watches into

    private NativeWatch currentWatch;		// decoded
    private NativeWatch watch;		// encoded

    private static final String TAG_WATCH = "watch";  // NOI18N
    private static final String ATTR_RESTRICTED = "restricted";  // NOI18N

    private static final String TAG_EXPR = "exp";  // NOI18N
    private static final String TAG_QEXPR = "qexp";  // NOI18N
    private static final String TAG_SCOPE = "scope";  // NOI18N

    /**
     * decoder form
     */
    WatchXMLCodec(WatchBag bag) {
	this.bag = bag;
    }

    /**
     * encoder form
     */
    WatchXMLCodec(NativeWatch watch) {
	this.watch = watch;
    }

    NativeWatch currentWatch() {
	return currentWatch;
    } 

    // interface XMLDecoder
    @Override
    protected String tag() {
	return TAG_WATCH;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) {
	if (Log.Watch.xml)
	    System.out.printf("WatchXMLCodec().start(%s)\n", tag()); // NOI18N

	String restrictedString = atts.getValue(ATTR_RESTRICTED);

	try {
	    currentWatch = new NativeWatch(null);
	    currentWatch.setRestricted(Boolean.parseBoolean(restrictedString));
	} catch (Exception x) {
	    ErrorManager.getDefault().annotate(x,
		"Failed to parse watch from XML"); // NOI18N
	    ErrorManager.getDefault().notify(x);
	}
    }

    // interface XMLDecoder
    @Override
    public void end() {
	if (Log.Watch.xml)
	    System.out.printf("WatchXMLCodec().end(%s)\n", tag()); // NOI18N

	if (currentWatch == null) {
	    if (Log.Watch.xml)
		System.out.printf("\tno currentWatch\n"); // NOI18N
	    return;
	} else if (bag != null) {
	    if (Log.Watch.xml)
		System.out.printf("\ttoplevel\n"); // NOI18N
	    bag.restore(currentWatch);
	} else {
	    if (Log.Watch.xml)
		System.out.printf("\tno bag\n"); // NOI18N
	}

	currentWatch = null;
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
	if (Log.Watch.xml)
	    System.out.printf("Watch().startElement(%s)\n", element); // NOI18N
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.Watch.xml)
	    System.out.printf("WatchXMLCodec().endElement(%s)\n", element); // NOI18N
	if (element.equals(TAG_EXPR))
	    currentWatch.setExpression(currentText);
	else if (element.equals(TAG_QEXPR))
	    currentWatch.setQualifiedExpression(currentText);
	else if (element.equals(TAG_SCOPE))
	    currentWatch.setScope(currentText);
    }

    // pseudo-interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	watch.prepareForSaving();

	String restrictedString = Boolean.toString(watch.isRestricted());

	AttrValuePair watchAttrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_RESTRICTED, restrictedString),
	};

	xes.elementOpen(TAG_WATCH, watchAttrs);
	    xes.element(TAG_EXPR, watch.getExpression());
	    xes.element(TAG_QEXPR, watch.getQualifiedExpression());
	    xes.element(TAG_SCOPE, watch.getScope());
	xes.elementClose(TAG_WATCH);
    }
}
