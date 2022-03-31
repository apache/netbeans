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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordList;

import org.netbeans.modules.cnd.debugger.common2.debugger.Log;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationAuxObject;

class DebugTargetXMLCodec extends XMLDecoder implements XMLEncoder {

    private RecordList<DebugTarget> model;

    private DebugTarget 		currentDT;	// decoded
    private Collection<XMLDecoder> currentDTAuxDecoders;
    private int 			index;		// encoded

    private static final String TAG_DEBUGTARGET = "debugtarget";  // NOI18N
    private static final String ATTR_ID = "id";  // NOI18N

    private static final String PROFILE_ID = "runprofile"; // NOI18N
    private static final String TARGET_SETTINGS_ID = "settings"; // NOI18N

    private final static String NAME_ATTR = "name"; // NOI18N
    private final static String VALUE_ATTR = "value"; // NOI18N
    private final static String ENVIRONMENT_ELEMENT = "environment"; // NOI18N
    private final static String CORE_ELEMENT = "core"; // NOI18N
    private final static String EXEC_ELEMENT = "exec"; // NOI18N
    private final static String ARGS_ELEMENT = "args"; // NOI18N
    private final static String HOST_ELEMENT = "host"; // NOI18N
    private final static String ENGINE_ELEMENT = "engine"; // NOI18N
    private final static String RUNDIR_ELEMENT = "rundir"; // NOI18N
    private final static String VARIABLE_ELEMENT = "variable"; // env variables NOI18N

/* LATER
    private final static String BUILD_FIRST_ELEMENT = "buildfirst"; // NOI18N
    private final static String CONSOLE_TYPE_ELEMENT = "console-type"; // NOI18N
    private final static String TERMINAL_TYPE_ELEMENT = "terminal-type"; // NOI18N
    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N
    private static final String ATTR_RESTRICTED = "restricted";  // NOI18N

    private static final String TAG_EXPR = "exp";  // NOI18N
    private static final String TAG_QEXPR = "qexp";  // NOI18N
    private static final String TAG_SCOPE = "scope";  // NOI18N
*/

    /**
     * decoder form
     */
    DebugTargetXMLCodec(RecordList<DebugTarget> model) {
	this.model = model;
    }

    /**
     * encoder form
     */
    DebugTargetXMLCodec(DebugTarget debugtarget, int index) {
	this.index = index;
	this.currentDT = debugtarget;
    }

    // interface XMLDecoder
    @Override
    protected String tag() {
	return TAG_DEBUGTARGET;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	String id = atts.getValue(ATTR_ID); 
	if (Log.XML.debug)
	    System.out.println(" DebugTargetXMLCodec start: id " + id); // NOI18N

	currentDT = new DebugTarget(id);
	model.appendRecord(currentDT);
        // register aux objects' codecs
        currentDTAuxDecoders = new ArrayList<XMLDecoder>();
        for (ConfigurationAuxObject configurationAuxObject : currentDT.getAuxProfiles()) {
            XMLDecoder decoder = configurationAuxObject.getXMLDecoder();
            registerXMLDecoder(decoder);
            currentDTAuxDecoders.add(decoder);
        }
    }

    // interface XMLDecoder
    @Override
    public void end() {
        // unregister aux objects' codecs
        for (XMLDecoder decoder : currentDTAuxDecoders) {
            deregisterXMLDecoder(decoder);
        }
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
	if (Log.XML.debug) {
	    System.out.println(" DebugTargetXMLCodec startElement: element " + element); // NOI18N
	    System.out.println(" DebugTargetXMLCodec startElement: atts " + atts); // NOI18N
	}
        if (element.equals(PROFILE_ID)) {
            // old versions had such container, now we use TARGET_SETTINGS_ID
            // and read them in endElement without additional decoder
        } else if (element.equals(VARIABLE_ELEMENT)) {
	    currentDT.getRunProfile().getEnvironment().putenv(atts.getValue(0), atts.getValue(1));
        }
    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.XML.debug) {
	    System.out.println(" DebugTargetXMLCodec endElement: element " + element); // NOI18N
	    System.out.println(" DebugTargetXMLCodec endElement: currentText " + currentText); // NOI18N
	}

        if (element.equals(CORE_ELEMENT)) {
	    currentDT.setCorefile(currentText);
	    // ??? currentDT.setExecutable("");
        } else if (element.equals(EXEC_ELEMENT)) {
	    currentDT.setExecutable(currentText);
	} else if (element.equals(ARGS_ELEMENT)) {
	    currentDT.getRunProfile().setArgs(currentText);
        } else if (element.equals(RUNDIR_ELEMENT)) {
	    currentDT.getRunProfile().setRunDir(currentText);
        } else if (element.equals(HOST_ELEMENT)) {
	    currentDT.setHostName(currentText);
        } else if (element.equals(ENGINE_ELEMENT)) {
	    currentDT.setEngineByID(currentText);
        } else if (element.equals(ENVIRONMENT_ELEMENT)) {
	    //SHOULD read encoded Env;
        } else {
            int idx;
            try {
                idx = Integer.parseInt(currentText);
            } catch (NumberFormatException ex) {
                idx = 0;
	    }
	    /* LATER
            if (element.equals(CONSOLE_TYPE_ELEMENT)) {
                profile.getConsoleType().setValue(idx);
            } else if (element.equals(TERMINAL_TYPE_ELEMENT)) {
                profile.getTerminalType().setValue(idx);
            }
	    */
        }
    }

    /*
     * Encode
     *
     */
    private static void encode(XMLEncoderStream xes, String[] pair) {
        xes.element(VARIABLE_ELEMENT,
                    new AttrValuePair[] {
                        new AttrValuePair(NAME_ATTR, "" + pair[0]), // NOI18N
                        new AttrValuePair(VALUE_ATTR, "" + pair[1]) // NOI18N
                    });
    }

    private static void encode(XMLEncoderStream xes, Env env) {
        String[][] environment = env.getenvAsPairs();
        xes.elementOpen(ENVIRONMENT_ELEMENT);
        for (int i = 0; i < environment.length; i++) {
            encode(xes, environment[i]);
        }
        xes.elementClose(ENVIRONMENT_ELEMENT);
    }


    // pseudo-interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
            AttrValuePair id_attr[] = new AttrValuePair[] {
                new AttrValuePair(ATTR_ID, index + " " + currentDT.displayName())}; // NOI18N
            xes.elementOpen(TAG_DEBUGTARGET, id_attr);
            
                //runprofile, include engine
                encodeTargetSettings(xes);

                for (ConfigurationAuxObject configurationAuxObject : currentDT.getAuxProfiles()) {
                    XMLEncoder auxProfileEncoder = configurationAuxObject.getXMLEncoder();
                    auxProfileEncoder.encode(xes);
                }
            xes.elementClose(TAG_DEBUGTARGET);
    }

    private void encodeTargetSettings(XMLEncoderStream xes) {
        String engine = currentDT.getEngine().getDebuggerID();
        xes.element(ENGINE_ELEMENT, engine);

        xes.elementOpen(TARGET_SETTINGS_ID);
            if (currentDT.getCorefile() != null) {
                xes.element(CORE_ELEMENT, currentDT.getCorefile());
            }

            xes.element(EXEC_ELEMENT, currentDT.getExecutable());
            xes.element(ARGS_ELEMENT, currentDT.getRunProfile().getArgsFlat());

            String host = currentDT.getHostName();
            if (host == null) {
                host = "";
            }
            xes.element(HOST_ELEMENT, host);

            String fullpath = currentDT.getRunProfile().getRunDir();
            xes.element(RUNDIR_ELEMENT, fullpath);
            encode(xes, currentDT.getRunProfile().getEnvironment());
	xes.elementClose(TARGET_SETTINGS_ID);

    }
}
