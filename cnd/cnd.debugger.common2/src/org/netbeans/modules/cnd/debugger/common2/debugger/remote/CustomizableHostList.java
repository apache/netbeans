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

import org.netbeans.modules.cnd.debugger.common2.utils.UserdirFile;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.AbstractRecordList;
import org.openide.ErrorManager;

public class CustomizableHostList extends AbstractRecordList<CustomizableHost> {

    private static CustomizableHostList instance = null;

    private static int listMaxSize = 100; // FIXUP should be 500?

    private static final String moduleFolderName = "DbxGui"; // NOI18N
    private static final String folderName = "RemoteSettings";	// NOI18N
    private static final String filename = "remotesettings";	// NOI18N
    private static final UserdirFile userdirFile =
        new UserdirFile(moduleFolderName, folderName, filename);

    public CustomizableHostList(int max) {
	super(max);
    }

    /**
     * Cloning constructor.
     */
    protected CustomizableHostList(CustomizableHostList that) {
	super(listMaxSize, that);
    }

    public static CustomizableHostList getInstance() {
	if (instance == null) {
	    try {
		instance = new CustomizableHostList(listMaxSize);
		instance.restore(userdirFile);
		instance.addLocalhost();
	    }
	    catch (Exception e) {
		System.out.println("HostList - getInstance - e " + e); //FIXUP //NOI18N
		System.out.println("Cannot restore host list ..."); //FIXUP //NOI18N
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
        HostListXMLWriter xw = 
	    new HostListXMLWriter(userdirFile, this);
        try {
            xw.write();
//            clearDirty();
        } catch (Exception e) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
        }
    }

    public void restore(UserdirFile userdirFile) {
        HostListXMLReader xr = new HostListXMLReader(userdirFile, this);
        try {
            xr.read();
        } catch (Exception x) {
            ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, x);
        }
    }

    // override AbstractRecordList
    @Override
    public CustomizableHostList cloneList() {
	return new CustomizableHostList(this);
    }

    // override AbstractRecordList
    @Override
    public String newKey() {
	return "<newhost>";		// NOI18N
    }

    /**
     * Add a default "localhost" entry if there doesn't exist one.
     */

    private void addLocalhost() {
	int i = getHostIndexByName(Host.localhost);
	if (i == 0) {
	    return;
	} 
        if (i == -1) {
	    addRecord(new CustomizableHost());
	} else {
	    // Move it to the front (someone edited the XML?)
	    addRecord(getRecordAt(i), true);
	}
    }

    public CustomizableHost getHostByDispName(String hostname) {
	for (CustomizableHost host : this) {
	    if (host.displayName().equals(hostname))
		return host;
	}
	return null;
    }

    public CustomizableHost getHostByName(String hostname) {
	for (CustomizableHost host : this) {
	    if (host.getHostName().equals(hostname))
		return host;
	}
	return null;
    }

    public int getHostIndexByName(String hostname) {
	int i = 0;
	for (Host host : this) {
	    if (host.getHostName().equals(hostname))
		return i;
	    i++;
	}
	return -1;
    }

}
