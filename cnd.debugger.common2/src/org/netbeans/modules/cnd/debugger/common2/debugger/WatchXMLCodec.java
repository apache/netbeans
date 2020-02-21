/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
