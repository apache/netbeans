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
