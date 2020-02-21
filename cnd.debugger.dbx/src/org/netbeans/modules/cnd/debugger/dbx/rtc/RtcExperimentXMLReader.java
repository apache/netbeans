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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import org.xml.sax.Attributes;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.cnd.api.xml.*;

class RtcExperimentXMLReader extends XMLDocReader {
    
    private FileObject fileObject;
    private String fullPathName;
    private RtcModel model;

    RtcExperimentXMLReader(FileObject fileObject, RtcModel model) {
	this.fileObject = fileObject;
	this.model = model;

	registerXMLDecoder(new RtcExperimentXMLCodec(model));

	fullPathName = fileObject.getPath();
    }

    RtcExperimentXMLReader(String fullPathName, RtcModel model) {
	this.fullPathName = fullPathName;
	this.model = model;
	registerXMLDecoder(new RtcExperimentXMLCodec(model));
    }

    public void read() throws IOException {

	if (Log.Rtc.debug) {
	    System.out.printf("RtcExperimentXMLReader.read() ... \n"); // NOI18N
	    System.out.printf("\tfrom %s\n", fullPathName); // NOI18N
	}

	if (fileObject == null) {
	    final FileObject rootFo = FileUtil.toFileObject(new File("/")); // NOI18N
	    fileObject = FileUtil.createData(rootFo, fullPathName);
	}

	InputStream is = fileObject.getInputStream();
	read(is, "rtc experiment"); // NOI18N
    }

    // interface XMLDecoder
    protected String tag() {
	return null;
    }

    // interface XMLDecoder
    public void start(Attributes atts) {
    }

    // interface XMLDecoder
    public void end() {
    }

    // interface XMLDecoder
    public void startElement(String name, Attributes atts) {
    }

    // interface XMLDecoder
    public void endElement(String name, String currentText) {
    }
}
