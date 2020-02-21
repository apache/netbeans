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

import java.io.IOException;
import java.io.OutputStream;
import org.openide.filesystems.FileStateInvalidException;

import org.netbeans.modules.cnd.api.xml.*;

import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.RecordList;

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;

class HostListXMLWriter extends XMLDocWriter implements UserdirFile.Writer {

    private UserdirFile userdirFile;
    private HostListXMLCodec encoder;

    HostListXMLWriter(UserdirFile userdirFile, RecordList<CustomizableHost> model) {
	this.userdirFile = userdirFile;
	encoder = new HostListXMLCodec(model);
    } 

    public void write() throws IOException, FileStateInvalidException {
	// will call back to writeTo()
	userdirFile.write(this);
    }

    // interface UserdirFile.Writer
    @Override
    public void writeTo(OutputStream os)
	throws IOException, FileStateInvalidException {

	// will call back to encode
	write(os);
    }

    // interface XMLEncoder
    @Override
    public void encode(XMLEncoderStream xes) {
	encoder.encode(xes);
    }
}
