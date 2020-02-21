/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
