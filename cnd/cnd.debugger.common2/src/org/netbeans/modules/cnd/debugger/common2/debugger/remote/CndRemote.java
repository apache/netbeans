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

import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


import org.openide.windows.WindowManager;
import org.openide.util.RequestProcessor;
import org.openide.ErrorManager;

import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;

import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.cnd.debugger.common2.APIAccessor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
// LATER import org.netbeans.modules.nativeexecution.support.Authentication;

/**
 * Bridge utility between our remote DB to the CND remote DB.
 */

public final class CndRemote {
    
    static {
        APIAccessor.register(new APIAccessorImpl());
    }    

    private CndRemote() {
    }

    /**
     * Convert a combo-box index into a HostName
     */
//    public static String hostNameFromIndex(int index) {
//	if (index < 0)
//	    return null;
//        if (DebuggerManager.isStandalone()) {
//            // string in combo-box has more than just hostname in it
//	    CustomizableHostList hostList = DebuggerManager.get().getHostList();
//            Host host = hostList.getRecordAt(index);
//            if (host == null)
//                return null;
//            return host.getHostName();
//        } else {
//	/*
//	 * Server list does NOT support getting/setting by index any more
//	    ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
//	*/
//	    //String serverKey = serverList.getRecords().get[index];
//            // IZ 179270
//	    String[] IdList = getServerListIDs();
//	    if (index < IdList.length)
//		return IdList[index];
//	    else
//		return "localhost"; // NOI18N
//        }
//    }

    /**
     * Convert a combo-box index into a Host
     */
//    public static Host OLD_hostFromIndex(int index) {
//	if (index < 0)
//	    return null;
//        if (DebuggerManager.isStandalone()) {
//	    HostList hostList = DebuggerManager.get().getHostList();
//            return hostList.getRecordAt(index);
//        } else {
//	/*
//	 * Server list does NOT support getting/setting by index any more
//	    ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
//	*/
//	    //String serverKey = serverList.getServerNames()[index];
//
//            // IZ 179270
//            String serverKey = getServerListIDs()[index];
//	    return hostFromName(serverKey);
//        }
//    }

    private final static RequestProcessor validatorRP = 
			new RequestProcessor("validator"); // throughput 1 // NOI18N
    
    /**
     * If the host 'name' is offline, bring it online and get it's
     * compiler-set up-to-date.
     * Once everything is ready continuation is called.
     * See IZ 147560.
     */
    public static void validate(final String name, final Runnable continuation) {
        validate(name, continuation, null);
    }
    
    /*package*/ static boolean syncValidate(final String name) {
	if (name != null && name.equals("localhost")) { // NOI18N
	    return true;
	}
        
        Host host = Host.byName(name);

        final ServerRecord serverRecord = ServerList.get(host.executionEnvironment());

        serverRecord.validate(true);
        // No need to continue if connection is not available
        if (!serverRecord.isOnline()) {
            showErrorDialog(serverRecord);
            return false;
        }
        ExecutionEnvironment exEnv = serverRecord.getExecutionEnvironment();
        CompilerSetManager csm = CompilerSetManager.get(exEnv);
        csm.initialize(true, true, null);
        // initialize host info
        PlatformInfo.getDefault(exEnv);
        return true;
    }

    /**
     * If the host 'name' is offline, bring it online and get it's
     * compiler-set up-to-date.
     * Once everything is ready continuation is called.
     * If not validated (unable to connect), onError is be called.
     */
    public static void validate(final String name, final Runnable continuation, final Runnable onError) {
	if (name != null && name.equals("localhost")) { // NOI18N
	    continuation.run();
	    return;
	}

        Runnable validator = new Runnable() {
            @Override
	    public void run() {
                Host host = Host.byName(name);

                final ServerRecord serverRecord = ServerList.get(host.executionEnvironment());

		serverRecord.validate(true);
                // No need to continue if connection is not available
                if (!serverRecord.isOnline()) {
                    showErrorDialog(serverRecord);
                    if (onError != null) {
                        try {
                            javax.swing.SwingUtilities.invokeAndWait(onError);
                        } catch (Exception x) {
                            ErrorManager.getDefault().notify(x);
                        }
                    }
                    return;
                }
                ExecutionEnvironment exEnv = serverRecord.getExecutionEnvironment();
		CompilerSetManager csm = CompilerSetManager.get(exEnv);
		csm.initialize(true, true, null);
                // initialize host info
                PlatformInfo.getDefault(exEnv);
		try {
		    javax.swing.SwingUtilities.invokeAndWait(continuation);
		} catch (Exception x) {
		    ErrorManager.getDefault().notify(x);
                    showErrorDialog(serverRecord);
		}
	    }

            
	};
	RequestProcessor.Task task = validatorRP.post(validator);
    }
    
    private static void showErrorDialog(ServerRecord serverRecord) {
        final String message = MessageFormat.format(Catalog.get("ERR_Cant_Cnnect"), serverRecord.getDisplayName()); // NOI18N
        final String title = Catalog.get("DLG_TITLE_Cant_Connect"); //NOI18N
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(
                        WindowManager.getDefault().getMainWindow(),
                        message, title, JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static String[] getServerListIDs() {
        List<String> l = new ArrayList<String>();
        for (ExecutionEnvironment env : ServerList.getEnvironments()) {
            l.add(ExecutionEnvironmentFactory.toUniqueID(env));
        }
        return l.toArray(new String[l.size()]);
    }

//    private static SecuritySettings getSecuritySettings(ExecutionEnvironment ee) {
//	SecuritySettings securitySettings;
//	securitySettings = new SecuritySettings(ee.getSSHPort(), null);
//	/* LATER
//	Authentication a = Authentication.getFor(ee);
//	switch (a.getType()) {
//	    case PASSWORD:
//		securitySettings = new SecuritySettings(ee.getSSHPort(), null);
//		break;
//	    case SSH_KEY:
//		securitySettings = new SecuritySettings(ee.getSSHPort(), a.getKey());
//		break;
//	    default:
//	    case UNDEFINED:
//		securitySettings = new SecuritySettings(ee.getSSHPort(), null);
//		break;
//	}
//	 */
//	return securitySettings;
//    }

    /**
     * Convert a hostname from either
     * - cnd's host and cset DB's
     * - dbxtools host DB
     * to our host.
     * @return new host
     */
//    public static Host hostFromName(String name) {
//	if (Log.Remote.host)
//	    System.out.printf("hostFromName(%s)\n", name); // NOI18N
//
//	if (name == null)
//	    name = "localhost";		// NOI18N
//
//	if (DebuggerManager.isStandalone()) {
//	    return DebuggerManager.get().getHostList().getHostByName(name);
//	} else {
//	    /*
//	     * not apply anymore
//	     * ServerList is static
//	     ServerList serverList = Lookup.getDefault().lookup(ServerList.class);
//	     */
//            return new Host.CndHost(ExecutionEnvironmentFactory.fromUniqueID(name));
//
////            final ServerRecord serverRecord = ServerList.get(ExecutionEnvironmentFactory.fromUniqueID(name));
////	    String userName = serverRecord.getUserName();
////	    String hostName = serverRecord.getServerName();
////
//////	    if ("127.0.0.1".equals(hostName))			// NOI18N
//////		hostName = "localhost";				// NOI18N
////
////
//////	    String userHostName;
//////	    if ("localhost".equals(hostName))			// NOI18N
//////		userHostName = hostName;
//////	    else
//////		userHostName = userName + "@" + hostName; // NOI18N
////
////	    ExecutionEnvironment ee = serverRecord.getExecutionEnvironment();
////
////            Host host = new Host();
////	    host.setHostName(hostName);
////	    host.setHostLogin(userName);
////	    host.setSecuritySettings(getSecuritySettings(ee));
////
////	    // convert Cnd platform info to Host platform		
////	    // for remote debugging that launched from "Debug Executable"
////	    String pName = platformInfo(ee); // generic platform name
////	    if (pName != null)
////		host.setPlatformName(pName);
////
////	    CompilerSetManager csm =
////		CompilerSetManager.get(ee);
////
////	    CompilerSet cs = csm.getDefaultCompilerSet();
////	    if (cs == null) {
////		String csname = "SunStudio";		// NOI18N
////		cs = csm.getCompilerSet(csname);
////	    }
////
////	    if (Log.Remote.host)
////		System.out.printf("hostFromName() cs %s\n", cs); // NOI18N
////	    if (cs != null) {
////		String base = cs.getDirectory();
////		if (Log.Remote.host)
////		    System.out.printf("hostFromName() base %s\n", base); // NOI18N
////		host.setRemoteStudioLocation(base + "/.."); // NOI18N
////	    } else {
////		// explicitly set to null so we don't end up with the
////		// default value.
////		host.setRemoteStudioLocation(null);
////		// Executor will fall back an a non glue-based provider
////	    }
////            return host;
//	}
//    }

    /**
     * Return the usr@hostname stored in 'conf'.
     */
    public static String userhostFromConfiguration(Configuration conf) {
	if (! (conf instanceof MakeConfiguration))
	    return "localhost"; // NOI18N

	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;

	DevelopmentHostConfiguration hostConfig =
	    makeConfiguration.getDevelopmentHost();

        return ExecutionEnvironmentFactory.toUniqueID(hostConfig.getExecutionEnvironment());
    }

    /**
     * Convert information pointed to by 'conf' in cnd's host and cset DB's
     * to our Host.
     * @return host passed in or new one if the passed-in one is null.
     */
//    public static Host hostFromConfiguration(Host host, Configuration conf) {
//	if (Log.Remote.host)
//	    System.out.printf("hostFromConfiguration(%s, %s)\n", host, conf); // NOI18N
//	if (host == null)
//	    host = new Host();
//
//	if (! (conf instanceof MakeConfiguration))
//	    return host;
//
//	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
//
//	DevelopmentHostConfiguration hostConfig =
//	    makeConfiguration.getDevelopmentHost();
//
//
//	// userHostName is of the form <user>@<hostname>
//	String userName;
//	String hostName;
//	String platformName;
//
//        String userHostName = ExecutionEnvironmentFactory.toUniqueID(hostConfig.getExecutionEnvironment());
//	if ("127.0.0.1".equals(userHostName))	    // NOI18N
//	    userHostName = "localhost";		    // NOI18N
//
//	if ("localhost".equals(userHostName)) {	    // NOI18N
//	    userName = null;
//	    hostName = userHostName;
//	} else {
//	    int atx = userHostName.indexOf('@');
//	    userName = userHostName.substring(0, atx);
//	    hostName = userHostName.substring(atx+1);
//	}
//
//	// covert Cnd platform info to Host platform
//        ExecutionEnvironment exEnv = hostConfig.getExecutionEnvironment();
//	String pName = platformInfo(exEnv); // generic platform name
//	if (pName != null)
//	    host.setPlatformName(pName);
//
//	CompilerSetManager csm = CompilerSetManager.get(exEnv);
//
//	CompilerSet2Configuration csconf = makeConfiguration.getCompilerSet();
//	if (Log.Remote.host)
//	    System.out.printf("hostFromConfiguration() csm %s  csconf %s\n", csm, csconf); // NOI18N
//
//	if (csm != null && csconf.isValid()) {
//	    String csname = csconf.getOption();
//	    CompilerSet cs = csm.getCompilerSet(csname);
//	    if (Log.Remote.host)
//		System.out.printf("hostFromConfiguration() csname %s  cs %s\n", csname, cs); // NOI18N
//	    if (cs != null) {
//		host.setHostName(hostName);
//		if (userName != null)
//		    host.setHostLogin(userName);
//
//		String base = cs.getDirectory();        // usually .../bin
//		if (Log.Remote.host)
//		    System.out.printf("hostFromConfiguration() base %s\n", base); // NOI18N
//		host.setRemoteStudioLocation(base + "/.."); // NOI18N
//		// platform determined automatically
//		host.setSecuritySettings(getSecuritySettings(exEnv));
//	    }
//	}
//
//	return host;
//    }
    
    private static EngineType getDebuggerType(CompilerSet cs) {
        Tool debuggerTool = cs.getTool(PredefinedToolKind.DebuggerTool);
        if (debuggerTool != null) {
            DebuggerDescriptor descriptor = (DebuggerDescriptor) debuggerTool.getDescriptor();
            return EngineTypeManager.getEngineTypeForDebuggerDescriptor(descriptor);
        }
        return null;
    }

    /**
     * Convert information described by host into CND-style and stuff it
     * into 'conf' and the relevant cset.
     * Sort of the reverse of hostFromConfiguration().
     */
    public static void fillConfigurationFromHost(Configuration conf, EngineType engine, Host host) {
	MakeConfiguration makeConfiguration = (MakeConfiguration) conf;
	if (! makeConfiguration.isMakefileConfiguration())
	    return;

	DevelopmentHostConfiguration hostConfig =
	    makeConfiguration.getDevelopmentHost();

        ExecutionEnvironment exEnv = host.executionEnvironment();
        hostConfig.setHost(exEnv);
	CompilerSetManager csm = CompilerSetManager.get(exEnv);

	CompilerSet2Configuration csconf = makeConfiguration.getCompilerSet();
	

	if (csm == null || ! csconf.isValid())
	    return;
        //see CR 7077657 we need to use the toolchain that supports the engine selected
        CompilerSet compilerSet = csconf.getCompilerSet();
        if (engine != getDebuggerType(compilerSet)) {
            for (CompilerSet cs : csm.getCompilerSets()) {
                if (engine == getDebuggerType(cs)) {
                    csconf.setValue(cs.getName());
                    break;
                }
            }
        };

	// point of no return

	/* LATER
	The relevant CND objects don't have setters ...


	cs.setDirectory(base);
	*/
    }
    
    
    private static final class APIAccessorImpl extends APIAccessor {

        @Override
        public boolean syncValidate(String name) {
            return CndRemote.syncValidate(name);
        }

    
    }    
}
