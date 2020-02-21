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

import java.io.IOException;


import org.openide.util.actions.SystemAction;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.DataObjectExistsException;


public class RtcDataLoader extends UniFileLoader {

    private static final String className =
	"org.netbeans.modules.cnd.debugger.dbx.rtc.RtcDataObject"; // NOI18N

    private SystemAction[] actions;

    public RtcDataLoader() {
	super(className);

	if (Log.Rtc.debug)
	    System.out.printf("RtcDataLoader.<init>()\n"); // NOI18N
    }

    // implement UniFileLoader
    protected MultiDataObject createMultiObject(FileObject primaryFile)
	throws DataObjectExistsException, IOException {

	if (Log.Rtc.debug) {
	    System.out.printf("RtcDataLoader.createMultiObject(%s)\n", // NOI18N
		FileUtil.getFileDisplayName(primaryFile));
	}
	return new RtcDataObject(primaryFile, this);
    }

    // override SharedClassObject
    protected void initialize() {
	super.initialize();
	if (Log.Rtc.debug)
	    System.out.printf("RtcDataLoader.initialize()\n"); // NOI18N

	// LATER ... what _is_ a dataloaders display name?
	// setDisplayName("--- An RTC Experiment ---");

	ExtensionList extensions = new ExtensionList();
	extensions.addExtension(RtcDataObject.EXTENSION);
	setExtensions(extensions);
    }

    // override DataLoader
    protected String actionsContext() {
	// The actions applicable to this DO are defined in our
	// mf-layer file under:
	return "Loaders/text/x-sun-rtc/Actions";	// NOI81N // NOI18N
    }
}
