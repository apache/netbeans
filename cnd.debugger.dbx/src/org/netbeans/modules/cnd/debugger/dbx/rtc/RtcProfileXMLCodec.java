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
