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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSetXMLCodec;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordList;
import org.netbeans.modules.cnd.debugger.common2.debugger.Log;

class HostListXMLCodec extends XMLDecoder implements XMLEncoder {

    private RecordList<CustomizableHost> model;
    private OptionSetXMLCodec optionsXMLCodec;
    private CustomizableHost currentHost;	// decoded

    private static final String TAG_REMOTEHOST = "host";	// NOI18N
    private static final String TAG_REMOTEHOSTS = "hosts";	// NOI18N
    private static final String ATTR_ID = "id";			// NOI18N

    // OLD private static final String HOSTSETTING = "hostsetting";	// NOI18N

    /**
     * decoder form
     */
    HostListXMLCodec(RecordList<CustomizableHost> model) {
	this.model = model;
    }

    private static int version() {
        return 1;
    }

    // interface XMLDecoder
    @Override
    protected String tag() {
	return TAG_REMOTEHOSTS;
    } 

    // interface XMLDecoder
    @Override
    public void start(Attributes atts) throws VersionException {
	;
    }

    // interface XMLDecoder
    @Override
    public void end() {
	;
    }

    // interface XMLDecoder
    @Override
    public void startElement(String element, Attributes atts) {
	String id = atts.getValue(ATTR_ID); 
	if (Log.XML.debug) {
	    System.out.println(" HostListXMLCodec startElement: element " + element); // NOI18N
	    System.out.println(" HostListXMLCodec startElement: id" + id); // NOI18N
	}

	if (element.equals(TAG_REMOTEHOST)) {
	    currentHost = new CustomizableHost();
	    optionsXMLCodec =  new OptionSetXMLCodec(currentHost.getOptions());
	    model.appendRecord(currentHost);
	}
	optionsXMLCodec.startElement(element, atts);

    }

    // interface XMLDecoder
    @Override
    public void endElement(String element, String currentText) {
	if (Log.XML.debug) {
	    System.out.println(" HostListXMLCodec endElement: element " + element); // NOI18N
	    System.out.println(" HostListXMLCodec endElement: currentText " + currentText); // NOI18N
	}
	optionsXMLCodec.endElement(element, currentText);
    }

    // pseudo-interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
        xes.elementOpen(TAG_REMOTEHOSTS, version());
            // deal with remote host list
	    for (CustomizableHost host : model) {
		String hostName = host.getHostName();
		if (!hostName.equals("localhost")) { // NOI18N
		    AttrValuePair id_attr[] = new AttrValuePair[] {
			new AttrValuePair(ATTR_ID, hostName)};
	  
		    xes.elementOpen(TAG_REMOTEHOST, id_attr);        

			// deal with remote settings for each host
			optionsXMLCodec = new OptionSetXMLCodec(host.getOptions());
			optionsXMLCodec.encode(xes);

		    xes.elementClose(TAG_REMOTEHOST);
		}
            }
        xes.elementClose(TAG_REMOTEHOSTS);
    }
}
