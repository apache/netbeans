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

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.AbstractRecordList;
import org.openide.ErrorManager;

public class DebugTargetList extends AbstractRecordList<DebugTarget> {

    private static DebugTargetList instance = null;

    /*package*/ static int debuglistMaxSize = 100; // FIXUP should be 500?

    /*package*/ static final String moduleFolderName = "DbxGui"; // NOI18N
    /*package*/ static final String folderName = "DebugTargets"; // NOI18N
    /*package*/ static final String filename = "debugtargets";           // NOI18N
    private static final UserdirFile userdirFile =
        new UserdirFile(moduleFolderName, folderName, filename);

    public DebugTargetList(int max) {
	super(max);
    }

    /**
     * Cloning constructor.
     */
    protected DebugTargetList(DebugTargetList that) {
	super(debuglistMaxSize, that);
    }

    public static DebugTargetList getInstance() {
	if (instance == null) {
	    try {
		instance = new DebugTargetList(debuglistMaxSize);
		instance.restore(userdirFile);
	    }
	    catch (Exception e) {
		System.out.println("DebugTargetList - getInstance - e " + e); //FIXUP //NOI18N
		System.out.println("Cannot restore debuglist ..."); //FIXUP //NOI18N
	    }
	}
	return instance;
    }

    public static void saveList() {
	if (instance == null) {
	    // No-one requested it so no need to save
	    return;
	}
	instance.save(userdirFile);
    }

    public void save(UserdirFile userdirFile) {
        DebugTargetsXMLWriter xw =
	    new DebugTargetsXMLWriter(userdirFile, this);
        try {
            xw.write();
//            clearDirty();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

    public void restore(UserdirFile userdirFile) {
        DebugTargetsXMLReader xr = new DebugTargetsXMLReader(userdirFile, this);
        try {
            xr.read();
        } catch (Exception x) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, x);
        }
    }

    // implement AbstractRecordList
    @Override
    public DebugTargetList cloneList() {
	return new DebugTargetList(this);
    }
}
