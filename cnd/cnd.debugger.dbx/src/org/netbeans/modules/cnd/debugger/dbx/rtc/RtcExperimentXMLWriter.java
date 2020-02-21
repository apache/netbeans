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

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;


import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;


import org.netbeans.modules.cnd.api.xml.*;

class RtcExperimentXMLWriter extends XMLDocWriter {
    
    private final String directory;
    private final String filename;
    private final RtcModel model;
    private final RtcExperimentXMLCodec codec;

    RtcExperimentXMLWriter(String directory, String filename, RtcModel model) {
	this.directory = directory;
	this.filename = filename;
	this.model = model;
	codec = new RtcExperimentXMLCodec(model);
    }

    public void write() throws IOException {

	final String fullPathName = directory + File.separator + filename;
	if (Log.Rtc.debug) {
	    System.out.printf("RtcExperimentXMLWriter.write() ... \n"); // NOI18N
	    System.out.printf("\tto %s\n", fullPathName); // NOI18N
	}
	final FileObject rootFo = FileUtil.toFileObject(new File("/")); // NOI18N
	final FileSystem fs = rootFo.getFileSystem();

	fs.runAtomicAction(new FileSystem.AtomicAction() {

	    public void run() throws IOException {
		File fullPath = new File(fullPathName);
		fullPath = FileUtil.normalizeFile(fullPath);
		final FileObject fo = FileUtil.createData(fullPath);
		final FileLock lock = fo.lock();
		try {
		    OutputStream os = fo.getOutputStream(lock);
		    // will call back to encode
		    write(os);
		} finally {
		    lock.releaseLock();
		}
	    }
	} );
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	codec.encode(xes);
    }
}
