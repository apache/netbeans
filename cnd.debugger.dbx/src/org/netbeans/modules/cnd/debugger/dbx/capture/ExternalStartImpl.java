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

package org.netbeans.modules.cnd.debugger.dbx.capture;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import com.sun.tools.swdev.glue.*;
import com.sun.tools.swdev.glue.xstart.*;

import org.netbeans.modules.cnd.debugger.common2.utils.Executor;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport.Model;

import org.netbeans.modules.cnd.debugger.common2.capture.AbstractExternalStart;
import org.netbeans.modules.cnd.debugger.common2.capture.CaptureInfo;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStart;
import org.netbeans.modules.cnd.debugger.common2.capture.ExternalStartManager;
import org.netbeans.modules.cnd.debugger.common2.capture.Log;
import org.netbeans.modules.cnd.debugger.dbx.RemoteSupport;


/**
 * This is an implementation of rfe 4921666
 * 
 * assumption: all state transitions, except start/stop, done on the AWT EQ.
 */

public final class ExternalStartImpl extends AbstractExternalStart implements ExternalStart {

    private final Host host;

    private static ExternalStartService svc = null;	// glue service

    private Executor bridgeExecutor;


    /* package */ ExternalStartImpl(Host host) {
	this.host = host;
    }

    private int maxRequests = -1;

    private int getMaxRequests() {
        if (maxRequests == -1) {
            maxRequests = 10;
            String s;
	    s = System.getProperty("spro.ss_attach.maxrequests"); // NOI18N
            if (s != null) {
                try {
                    maxRequests = Integer.parseInt(s);
                } catch (java.lang.NumberFormatException e) {
                }
            }
        }
        return maxRequests;
    }

    @Override
    public boolean attached(int pid) {
	if (svc == null)
	    return false;
	return ExternalStartManager.attached(pid);
    }

    /**
     * Start the glue service, locally or remotely
     */
    public boolean start() {
	boolean success = false;

	// always start a new one because apparently you cannot
	// deregister and re-register a service under glue.
	Notifier notifier = NotifierThread.notifier();
	if (svc == null) {
	    svc = new MyExternalStartService(notifier);
	    svc.limit_hard(getMaxRequests());
	    success = svc.svc_register(null);
	    if (success) {
		NotifierThread.runNotifier();
	    }
	}


	if (host.isRemote()) {
	    // Start remote bridge
	    success = initXstart(host);
	}  else {
	    success = (svc != null);
	}

	setRunning(success);
	return success;
    }

    /**
     * Stop the glue service.
     */

    public boolean stop() {
	if (host.isRemote()) {
	    if (bridgeExecutor == null )
		return false;
	    else
		try {
		    bridgeExecutor.terminate();
		} catch (Exception x) {
		}
	} else {
	    if (svc == null )
		return false;
	    else {
		svc.stop();
		svc = null;
	    }
	}
	setRunning(false);
	return false;
    }

    /* start xstart_bridge service remotely */
    private synchronized boolean initXstart(Host host) {
	bridgeExecutor = Executor.getDefault("ss_attach", host, 0); // NOI18N

	// construct command for starting Xbridge server
        String bridgeName = host.getRemoteStudioLocation() + "/lib/dbx/xstart_bridge_svc" ; // NOI18N
	HostInfo hostInfo;
	if (host.isRemote()) {
	    SecurityStyle securityStyle;
	    securityStyle = RemoteSupport.securityStyle(host.getSecuritySettings());

	    svc.setSecurityStyle(securityStyle);
	    hostInfo = new HostInfo(host.getHostName(),
				    host.getHostLogin(),
				    securityStyle,
				    host.getRemoteStudioLocation());
	} else {
	    hostInfo = null;
	}
	NetAddr net_addr = svc.inet_address(false, hostInfo);
	
	if (Log.Start.capture_xstart) {
	    if (!net_addr.parse(net_addr.toString())) {
		System.out.println("Couldn't parse inet_address"); // NOI18N
	    }
	    System.out.println("net_addr: " + // NOI18N
				"host '" + net_addr.host() + // NOI18N
				"' port " + net_addr.port()); // NOI18N
	}

	if (net_addr != null) {
	    String [] args = new String[2];
	    args[0] = bridgeName;
	    args[1] = net_addr.toString();

	    bridgeExecutor.startShellCmd(args);
	    bridgeExecutor.reap();
	}

        return true;

    }

    private static class MyExternalStartService extends ExternalStartService {
        MyExternalStartService(Notifier notifier) {
            super(notifier);
        }

        public Servant Servant_new() {
            return new MyExternalStartServant(this);
        }

        @Override
        protected void hard_limit_exceeded() {
            // ExternalStartImpl.stop();
            stop();

            String message = Catalog.get("LBL_TooMany"); // NOI18N
            NotifyDescriptor d = new NotifyDescriptor.Message(message,
                NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    private static class JobImpl implements ExternalStartManager.Job {
        private final CaptureInfo captureInfo;
        private final ExternalStartServant servant;

        JobImpl(CaptureInfo captureInfo, ExternalStartServant servant) {
            this.captureInfo = captureInfo;
            this.servant = servant;
        }

	public CaptureInfo captureInfo() {
	    return captureInfo;
	}

	public void failed(String msg) {
            servant.failed(msg);
	}

	public void proceed() {
            servant.proceed();
	}
    }


    private static class MyExternalStartServant extends
				ExternalStartServant {

        MyExternalStartServant(Service svc) {
            super(svc);
        }

        public void startSession(String executable,
                                    int argc,
                                    String argv[],
                                    String workingDirectory,
                                    int pid,
                                    int model,
                                    String hostName) {

            // convert glue model to ProjectSupport model
            Model projectModel = Model.DONTCARE;
            switch (model) {
                case ExternalStartModel.DONTCARE:
                    projectModel = Model.DONTCARE;
                    break;
                case ExternalStartModel.IS32:
                    projectModel = Model.IS32;
                    break;
                case ExternalStartModel.IS64:
                    projectModel = Model.IS64;
                    break;
            }

            CaptureInfo ci;

            if (hostName != null &&
		!hostName.equals(Host.localhost) &&
		!NativeDebuggerManager.isStandalone()) {

		hostName = System.getProperty("user.name") + "@" +
						hostName;
	    }

            ci = new CaptureInfo(executable, argc, argv,
		workingDirectory, pid, projectModel, hostName);
            ExternalStartManager.Job j = new JobImpl(ci, this);
            ExternalStartManager.enqueue(j);
        }
    }
}
