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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;

class BreakpointXMLReader extends XMLDocReader implements UserdirFile.Reader {

    private UserdirFile userdirFile;
    private BreakpointBag bb;

    BreakpointXMLReader(UserdirFile userdirFile, BreakpointBag bb) {

	this.userdirFile = userdirFile;
	this.bb = bb;

	registerXMLDecoder(new BreakpointsXMLCodec(bb));
    } 

    public void read() throws IOException {
	// will call back to readFrom()
	userdirFile.read(this);
    }


    // interface UserdirFile.Reader
    @Override
    public void readFrom(InputStream is) throws IOException {
	read(is, "breakpoints"); // NOI18N
    }

    // interface XMLDecoder
    @Override
    protected String tag() {
	return null;
	
    } 
    // interface XMLDecoder
    @Override
    public void start(Attributes atts) {
    } 

    // interface XMLDecoder
    @Override
    public void end() {
    } 

    // interface XMLDecoder
    @Override
    public void startElement(String name, Attributes atts) {
    }

    // interface XMLDecoder
    @Override
    public void endElement(String name, String currentText) {
    }
}
