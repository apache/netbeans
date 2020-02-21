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

package org.netbeans.modules.cnd.debugger.gdb2.options;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.xml.sax.Attributes;

import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetXMLCodec;

import org.netbeans.modules.cnd.debugger.common2.debugger.Log;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfileXMLCodec;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Pathmap;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.Signals;

public class GdbProfileXMLCodec extends DbgProfileXMLCodec {

    private final static int thisversion = 1;
    private final OptionSetXMLCodec optionsXMLCodec;

    static final String TAG_ENGINE = "gdb_engine"; // NOI18N

    static final String TAG_HOST = "gdb_host"; // NOI18N

    static final String TAG_OPTIONS = "gdb_options"; // NOI18N
    static final String TAG_OPTION = "gdb_option"; // NOI18N
    static final String ATTR_OPTION_NAME = "gdb_name"; // NOI18N
    static final String ATTR_OPTION_VALUE = "gdb_value"; // NOI18N

    static final String TAG_INTERCEPTLIST = "gdb_interceptlist";// NOI18N
    static final String TAG_INTERCEPT_OPTIONS = "gdbinterceptoptions";// NOI18N
    static final String ATTR_INTERCEPT_ALL = "gdb_all"; // NOI18N
    static final String ATTR_INTERCEPT_UNHANDLED = "gdb_unhandled"; // NOI18N
    static final String ATTR_INTERCEPT_UNEXPECTED = "gdb_unexpected"; // NOI18N
    static final String TAG_INTERCEPT = "gdb_intercept";// NOI18N
    static final String TAG_EXCLUDED = "gdb_excluded";// NOI18N

    static final String TAG_SIGNALS = "gdb_signals";	// NOI18N
    static final String TAG_SIGNAL = "gdb_signal";	// NOI18N
    static final String ATTR_SIGNAL_SIGNAME = "gdb_signame";	// NOI18N
    static final String ATTR_SIGNAL_CAUGHT = "gdb_caught";	// NOI18N
    static final String ATTR_SIGNAL_CAUGHT_BY_DEFAULT = "gdb_caught_by_default"; // NOI18N

    static final String TAG_PATHMAP = "gdb_pathmap";	// NOI18N
    static final String TAG_PATHMAPS = "gdb_pathmaps";	// NOI18N
    static final String ATTR_PATHMAP_FROM = "gdb_from";	// NOI18N
    static final String ATTR_PATHMAP_TO = "gdb_to";	// NOI18N

    static final String TAG_BUILDFIRST = "gdb_buildfirst";	// NOI18N
    static final String ATTR_BUILDFIRST_OVERRIDEN =
				    "gdb_buildfirst_overriden";	// NOI18N
    static final String ATTR_BUILDFIRST_OLD = "gdb_buildfirst_old";	// NOI18N

    public GdbProfileXMLCodec(DbgProfile profile) {
	super(profile);
	optionsXMLCodec = new OptionSetXMLCodec(profile.getOptions());
	registerXMLDecoder(optionsXMLCodec);
    } 

    // PathMap stuff
    private final List<Pathmap.Item> pathmaps = new ArrayList<Pathmap.Item>();

    // Exception stuff
    private final Vector<String> interceptlist = new Vector<String>();
    private final Vector<String> interceptexlist = new Vector<String>();
    private boolean unhandled;
    private boolean unexpected;
    private boolean all;

    // Signal stuff
    private final Vector<Signals.InitialSignalInfo> signals = new Vector<Signals.InitialSignalInfo>();
    
    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
    /* Debug
	System.out.println("  startElement: " + element);
	System.out.println("  startElement atts: " + atts);
    */

	if (element.equals(TAG_PATHMAPS)) {
	}
	if (element.equals(TAG_PATHMAP)) {
	    Pathmap.Item pathmap = new Pathmap.Item(atts.getValue(ATTR_PATHMAP_FROM),
						    atts.getValue(ATTR_PATHMAP_TO),
						    false);
	    pathmaps.add(pathmap);
	}
	if (element.equals(TAG_BUILDFIRST)) {
	    String isBuildFirstOverridenStr =
		atts.getValue(ATTR_BUILDFIRST_OVERRIDEN);
	    profile.setBuildFirstOverriden
		(Boolean.parseBoolean(isBuildFirstOverridenStr));

	    String isSavedBuildFirstStr =
		atts.getValue(ATTR_BUILDFIRST_OLD);
	    profile.setSavedBuildFirst
		(Boolean.parseBoolean(isSavedBuildFirstStr));
	}
	if (element.equals(TAG_INTERCEPTLIST)) {
	}
	if (element.equals(TAG_INTERCEPT_OPTIONS)) {
	    unhandled = atts.getValue(ATTR_INTERCEPT_UNHANDLED).equals("true"); // NOI18N
	    unexpected = atts.getValue(ATTR_INTERCEPT_UNEXPECTED).equals("true"); // NOI18N
	    all = atts.getValue(ATTR_INTERCEPT_ALL).equals("true"); // NOI18N
	}
	if (element.equals(TAG_INTERCEPT)) {
	}
	if (element.equals(TAG_EXCLUDED)) {
	}
	if (element.equals(TAG_SIGNALS)) {
	}
	if (element.equals(TAG_SIGNAL)) {
	    Signals.InitialSignalInfo signal = new Signals.InitialSignalInfo(
		0,                                  // no
		atts.getValue(ATTR_SIGNAL_SIGNAME),		    // name
		null,		    // description
		atts.getValue(ATTR_SIGNAL_CAUGHT_BY_DEFAULT).equals("true"), // NOI18N
		atts.getValue(ATTR_SIGNAL_CAUGHT).equals("true")); // NOI18N
	    signals.add(signal);
	}
	if (element.equals(TAG_OPTION)) {
	    String optname = atts.getValue(ATTR_OPTION_NAME);
	    String optvalue = atts.getValue(ATTR_OPTION_VALUE);
	    if (optname == null)
		return;

	    if (optname.equals(TAG_HOST)) {
		profile.setHost(optvalue);
	    } else {
		optionsXMLCodec.startElement(element, atts);
	    }

	    /* LATER
	    else if (optname.equals(OPTION_BREAKDURINGSTEP)) {
		profile.setAllowBreakDuringStep(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_FINDSOURCE)) {
		profile.setShowFirstSourceFunc(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_STEPGRANULARITY)) {
		profile.setStepGranularity(optvalue);
	    }
	    else if (optname.equals(OPTION_STACKDESTRUCTORS)) {
		profile.setExecuteDestructors(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_FSUBSCRIPTS)) {
		profile.setCheckFortranSubscripts(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_LANGUAGE)) {
		profile.setLanguage(optvalue);
	    }
	    else if (optname.equals(OPTION_FORKFOLLOW)) {
		profile.setForkfollow(optvalue);
	    }
	    else if (optname.equals(OPTION_INHERITBREAKPOINTS)) {
		profile.setInheritBreakpoints(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_QUICKMODE)) {
		profile.setQuickMode(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_TRADEOFFSPEEDFORRESOURCES)) {
		profile.setTradeOffSpeedForResources(optvalue.equals(TRUE_VALUE));
	    }
	    else if (optname.equals(OPTION_EXCLATTACH)) {
		profile.setExclusiveAttach(optvalue.equals(TRUE_VALUE));
	    }
	    */
	}
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.XML.debug) {
	    System.out.println("  endElement: " + element); // NOI18N
	    System.out.println("  endElement: " + currentText); // NOI18N
	}

	if (element.equals(TAG_PATHMAPS)) { // number of entries of pathmap
            Pathmap.Item [] vars = new Pathmap.Item[pathmaps.size()];
	    profile.pathmap().setPathmap(pathmaps.toArray(vars));
	    pathmaps.clear();
	} else if (element.equals(TAG_PATHMAP)) {
	} else if (element.equals(TAG_INTERCEPTLIST)) {
	    String [] ilist = new String[interceptlist.size()];
	    String [] xlist = new String[interceptexlist.size()];
	    profile.exceptions().setInterceptList(
			interceptlist.toArray(ilist), 
			interceptexlist.toArray(xlist), 
			all, unhandled, unexpected);
	    interceptlist.clear();
	    interceptexlist.clear();
	} else if (element.equals(TAG_INTERCEPT_OPTIONS)) {
	} else if (element.equals(TAG_INTERCEPT)) {
	    interceptlist.add(currentText);
	} else if (element.equals(TAG_EXCLUDED)) {
	    interceptexlist.add(currentText);
	} else if (element.equals(TAG_SIGNAL)) { 
	} else if (element.equals(TAG_SIGNALS)) { 
            Signals.InitialSignalInfo [] vars = new Signals.InitialSignalInfo[signals.size()];
	    //profile.signals().setSignals(((Signals.InitialSignalInfo[])signals.toArray(vars)));

	    profile.signals().setXMLSignal(signals.toArray(vars));
	    signals.clear();
	}
    }

    // intrface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {

	xes.elementOpen(tag(), thisversion);

	// xes.element(TAG_ENGINE, profile.getEngineString());

	writePathmapsBlock(xes);
	writeExceptionsBlock(xes);
	writeSignalsBlock(xes);
	writeOptionsBlock(xes);
	writeBuildfirstBlock(xes);

	xes.elementClose(tag());
    }

    private void writeOptionsBlock(XMLEncoderStream xes) {
	xes.elementOpen(TAG_OPTIONS);
	optionsXMLCodec.encode(xes);
	xes.elementClose(TAG_OPTIONS);
    }

    private void writeBuildfirstBlock(XMLEncoderStream xes) {
	AttrValuePair buildfirstAttrs[] = new AttrValuePair[] {
	    new AttrValuePair(ATTR_BUILDFIRST_OVERRIDEN,
			      Boolean.toString(profile.isBuildFirstOverriden())),
	    new AttrValuePair(ATTR_BUILDFIRST_OLD, 
			      Boolean.toString(profile.isSavedBuildFirst())),
	};
	xes.element(TAG_BUILDFIRST, buildfirstAttrs);
    }

    private void writePathmapsBlock(XMLEncoderStream xes) {
	Pathmap.Item[] pm = profile.pathmap().getPathmap();
	if (pm == null)
	    return;

	xes.elementOpen(TAG_PATHMAPS);
	int size = pm.length;
	for (int i = 0; i < size; i++) {
	    AttrValuePair pathmapAttrs[] = new AttrValuePair[] {
		new AttrValuePair(ATTR_PATHMAP_FROM, pm[i].from()),
		new AttrValuePair(ATTR_PATHMAP_TO, pm[i].to()),
	    };
	    xes.element(TAG_PATHMAP, pathmapAttrs);
	}
	xes.elementClose(TAG_PATHMAPS);
    }

    private void writeExceptionsBlock(XMLEncoderStream xes) {
	xes.elementOpen(TAG_INTERCEPTLIST);

	    AttrValuePair exceptionAttrs[] = new AttrValuePair[] {
		new AttrValuePair(ATTR_INTERCEPT_ALL, 
				Boolean.toString(profile.exceptions().isAll())),
		new AttrValuePair(ATTR_INTERCEPT_UNHANDLED, 
				Boolean.toString(profile.exceptions().isInterceptUnhandled())),
		new AttrValuePair(ATTR_INTERCEPT_UNEXPECTED, 
				Boolean.toString(profile.exceptions().isInterceptUnexpected())),
	    };
	    xes.element(TAG_INTERCEPT_OPTIONS, exceptionAttrs);

	String [] elist = profile.exceptions().getInterceptList();
	if (elist != null) {
	    int size = elist.length;
	    for (int i = 0; i < size; i++) {
		xes.element(TAG_INTERCEPT, elist[i]);
	    }
	}

	String [] xlist = profile.exceptions().getInterceptExceptList();
	if (xlist != null) {
	    int size = xlist.length;
	    for (int i = 0; i < size; i++) {
		xes.element(TAG_EXCLUDED, xlist[i]);
	    }
	}

	xes.elementClose(TAG_INTERCEPTLIST);
    }

    private void writeSignalsBlock(XMLEncoderStream xes) {

	int size = profile.signals().count();

	if (size == 0)
	    return;

	xes.elementOpen(TAG_SIGNALS);
	for (int i = 0; i < size; i++) {
	  Signals.InitialSignalInfo sig = profile.signals().getSignal(i);
	  /* OLD
	  if ((sig.isCaught() && !sig.isCaughtByDefault()) ||
		(!sig.isCaught() && sig.isCaughtByDefault())) {
	  */
	  if (sig.isCaught() != sig.isCaughtByDefault()) {
	    AttrValuePair signalAttrs[] = new AttrValuePair[] {
	        new AttrValuePair(ATTR_SIGNAL_SIGNAME, 
			sig.name()),
	        new AttrValuePair(ATTR_SIGNAL_CAUGHT, 
			Boolean.toString(sig.isCaught())),
	        new AttrValuePair(ATTR_SIGNAL_CAUGHT_BY_DEFAULT, 
			Boolean.toString(sig.isCaughtByDefault())),
	    };
	    xes.element(TAG_SIGNAL, signalAttrs);
	  }
	}

	xes.elementClose(TAG_SIGNALS);
    }
}
