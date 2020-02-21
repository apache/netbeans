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

package org.netbeans.modules.cnd.debugger.dbx;

import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
//import com.sun.tools.swdev.toolscommon.base.InstallDir;
//import java.io.File;
//import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
//import org.openide.DialogDisplayer;
//import org.openide.NotifyDescriptor;
//import org.openide.util.NbBundle;

/**
 *
 *
 */
public final class DbxPathProvider {

    public static String getDbxPath(Host host) {
	String dbx = System.getProperty("SPRO_DBX_PATH");	// NOI18N
        if (dbx == null)
	    dbx = System.getenv("SPRO_DBX_PATH");		// NOI18N

	if (dbx != null) {
	    Platform platform;
	    if (host != null)
		platform = host.getPlatform();
	    else
		platform = Platform.local();
	    String variant;
	    if (host != null && host.isLinux64())
		variant = platform.variant64();
	    else
		variant = platform.variant();
	    dbx = dbx.replaceAll("/PLATFORM/", "/" + variant + "/"); // NOI18N
	}

        // this stuff should not be used in the projectless-based dbxtool
        // use spro.home for Tool only, see CR 7014085
//        if (dbx == null && ( NativeDebuggerManager.isStandalone() || NativeDebuggerManager.isPL() ) ) {
//	    String overrideInstallDir = null;
//	    if (host.isRemote())
//		overrideInstallDir = host.getRemoteStudioLocation();
//
//	    if (overrideInstallDir != null) {
//		dbx = overrideInstallDir + "/bin/dbx"; // NOI18N
//	    } else {
//
//		String spro_home = InstallDir.get();
//		if (spro_home == null) {
//		    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
//			    NbBundle.getMessage(DbxPathProvider.class,
//			    "MSG_MISSING_SPRO_HOME"))); // NOI18N
//		} else {
//                    String dbxPath = spro_home + "/bin/dbx"; // NOI18N
//                    File dbxFile = new File(dbxPath);
//
//                    if (dbxFile.exists()) {
//                        dbx = dbxFile.getAbsolutePath();
//                    }
//                }
//	    }
//        }
        return dbx;
    }
}
