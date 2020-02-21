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

package org.netbeans.modules.cnd.debugger.dbx.options;

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

public class DbxProfileXMLCodec extends DbgProfileXMLCodec {

    private final static int thisversion = 1;
    private final OptionSetXMLCodec optionsXMLCodec;

    static final String TAG_ENGINE = "engine"; // NOI18N

    static final String TAG_HOST = "host"; // NOI18N

    static final String TAG_OPTIONS = "options"; // NOI18N
    static final String TAG_OPTION = "option"; // NOI18N
    static final String ATTR_OPTION_NAME = "name"; // NOI18N
    static final String ATTR_OPTION_VALUE = "value"; // NOI18N

    static final String TAG_INTERCEPTLIST = "interceptlist";// NOI18N
    static final String TAG_INTERCEPT_OPTIONS = "interceptoptions";// NOI18N
    static final String ATTR_INTERCEPT_ALL = "all"; // NOI18N
    static final String ATTR_INTERCEPT_UNHANDLED = "unhandled"; // NOI18N
    static final String ATTR_INTERCEPT_UNEXPECTED = "unexpected"; // NOI18N
    static final String TAG_INTERCEPT = "intercept";// NOI18N
    static final String TAG_EXCLUDED = "excluded";// NOI18N

    static final String TAG_SIGNALS = "signals";	// NOI18N
    static final String TAG_SIGNAL = "signal";	// NOI18N
    static final String ATTR_SIGNAL_SIGNAME = "signame";	// NOI18N
    static final String ATTR_SIGNAL_CAUGHT = "caught";	// NOI18N
    static final String ATTR_SIGNAL_CAUGHT_BY_DEFAULT = "caught_by_default"; // NOI18N

    static final String TAG_PATHMAP = "pathmap";	// NOI18N
    static final String TAG_PATHMAPS = "pathmaps";	// NOI18N
    static final String ATTR_PATHMAP_FROM = "from";	// NOI18N
    static final String ATTR_PATHMAP_TO = "to";	// NOI18N

    static final String TAG_BUILDFIRST = "buildfirst";	// NOI18N
    static final String ATTR_BUILDFIRST_OVERRIDEN =
				    "buildfirst_overriden";	// NOI18N
    static final String ATTR_BUILDFIRST_OLD = "buildfirst_old";	// NOI18N

    public DbxProfileXMLCodec(DbgProfile profile) {
	super(profile);
	optionsXMLCodec = new OptionSetXMLCodec(profile.getOptions());
	registerXMLDecoder(optionsXMLCodec);
    } 

    // PathMap stuff
    private final Vector<Pathmap.Item> pathmaps = new Vector<Pathmap.Item>();

    // Exception stuff
    private final Vector<String> interceptlist = new Vector<String>();
    private final Vector<String> interceptexlist = new Vector<String>();
    private boolean unhandled;
    private boolean unexpected;
    private boolean all;

    // Signal stuff
    private final Vector<Signals.InitialSignalInfo> signals = new Vector<Signals.InitialSignalInfo>();
    
    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
	if (Log.XML.debug) {
	    System.out.println("  startElement: " + element); // NOI18N
	    System.out.println("  startElement atts: " + atts); // NOI18N
	}

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
		atts.getValue(ATTR_SIGNAL_SIGNAME),			// name
		null,			// description
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

        // commenting out to pass unit test
//	if (size == 0)
//	    return;

	xes.elementOpen(TAG_SIGNALS);
	for (int i = 0; i < size; i++) {
	  Signals.InitialSignalInfo sig = profile.signals().getSignal(i);
	  /* OLD
	  if ((sig.isCaught() && !sig.isCaughtByDefault()) ||
		(!sig.isCaught() && sig.isCaughtByDefault()))
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
