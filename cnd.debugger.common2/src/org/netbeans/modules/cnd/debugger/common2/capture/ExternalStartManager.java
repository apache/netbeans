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

package org.netbeans.modules.cnd.debugger.common2.capture;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collection;

import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Manage binding between Host's and ExternalStart
 */

public final class ExternalStartManager {
    private static Job pendingJob;
    private static boolean debuggerStarted;
    private static boolean waitingForDebuggerAck;

    // We need to queue up session start request in order to prevent 
    // the flooding of the user and debuggercore machinery

    // queue of session start jobs
    private static final LinkedList<Job> queue = new LinkedList<Job>();

    private final static HashMap<String, ExternalStart> hostXstartMap =
			new HashMap<String, ExternalStart>();

    /**
     * Start the default local ss_attach listener.
     */
    public static void startLocal() {
        // fix for IZ 181830 - do not start on windows & mac
        if (Utilities.isWindows() || Utilities.isMac()) {
            return;
        }
        
        try {
        Host host = Host.getLocal();
	ExternalStart xstart = ExternalStartManager.getXstart(host);
	if (xstart == null) {
	    xstart = ExternalStartManager.createExternalStart(host);
	    if (xstart != null) {
		ExternalStartManager.addXstart(host, xstart);
		xstart.start();
	    }
        }
        } catch (Throwable t) {
            // Unsupported platform?
            System.err.println("Problems initializing dbx MI: "+t.getLocalizedMessage());
        }
    }

    public static ExternalStart getXstart(Host host) {
	if (NativeDebuggerManager.isStandalone()) {
	    return hostXstartMap.get(host.getHostName());
	} else {
	    if (host.getHostName().equals(Host.localhost)) {
		return hostXstartMap.get(host.getHostName());
            } else {
		return hostXstartMap.get(host.getHostLogin() + '@' +
			host.getHostName());
            }
	}
    }

    public static ExternalStart getXstart(String hostName) {
        // IZ 179270,
        // has port #, ignore for the time being
        int i = hostName.indexOf(':');
        if (i > 0)
            hostName = hostName.substring(0, i); // strip port #
	return hostXstartMap.get(hostName);
    }

    /* package */ static void addXstart(Host host, ExternalStart x) {
	if (NativeDebuggerManager.isStandalone()) {
	    hostXstartMap.put(host.getHostName(), x);
	} else {
	    if (host.getHostName().equals(Host.localhost)) {
		hostXstartMap.put(host.getHostName(), x);
            } else {
		hostXstartMap.put(host.getHostLogin() + '@' +
			host.getHostName(), x);
            }
	}
    }

    /* package */ static boolean isSupported() {
	ExternalStartProvider xstartProvider = Lookup.getDefault().lookup(ExternalStartProvider.class);
	return xstartProvider != null && xstartProvider.isSupported();
    }

    /* package */ static ExternalStart createExternalStart(Host host) {
	ExternalStartProvider xstartProvider = Lookup.getDefault().lookup(ExternalStartProvider.class);
	if (xstartProvider != null)
	    return xstartProvider.createExternalStart(host);
	else
	    return null;
    }

    public static void stopAll() {
	Collection<ExternalStart> xstart_set = ExternalStartManager.hostXstartMap.values();
	for (ExternalStart xstart : xstart_set) {
	    xstart.stop();
	}
    }


    // IPI
    public interface Job {
	CaptureInfo captureInfo();
	void failed(String msg);
	void proceed();
    }

    // IPI
    public static void enqueue(Job job) {
	queue.add(job);
	runJob();
    }

    /* package */ static void fail(String label) {
        // pendingJob will be null if we get called more than once
        // during messy shutdown/cancellation scenarios
        if (pendingJob != null) {
            pendingJob.failed(Catalog.get(label));
            pendingJob = null;
        }
        runJob();
    }

    /* package */ static void debuggerStarted() {
        // ensure that the test of 'debuggerStarted' at the end of
        // runJob() hasn't happenned yet because of a race condition
        // cause by asynch startup of debugger

        assert waitingForDebuggerAck;
        debuggerStarted = true;
    }

    // IPI
    public static boolean attached(int pid) {

        if (pendingJob == null)
            return false;

        assert pendingJob.captureInfo().pid == pid;

        pendingJob.proceed();

        pendingJob = null;
        runJob();

        return true;
    }


    /**
     * Pick a job off the queue and start a session based on it, 
     * unless...already in the middle of starting a session. In which 
     * case we'll get called again from attached().
     */
    private static void runJob() {
        if (queue.isEmpty())
            return;

        if (pendingJob != null)
            return;

        pendingJob = queue.removeFirst();

        /*
         * Ask the user if they really want to attach to this
         */

        CapturePanel panel = new CapturePanel(pendingJob.captureInfo());
        NotifyDescriptor dlg = new NotifyDescriptor.Confirmation(
            panel,
            Catalog.get("LBL_dialogName"),      // NOI18N
            NotifyDescriptor.YES_NO_OPTION);
        Object answer = DialogDisplayer.getDefault().notify(dlg);
        // ---------- we block here -------------

        if (answer == NotifyDescriptor.NO_OPTION ||
            answer == NotifyDescriptor.CLOSED_OPTION) {
            // user doesn't want to attach, so let go
            fail("LBL_UserDeniedRequest");      // NOI18N
            return;
        }

        Object project = panel.getSelectedProject();
        boolean noProject = panel.getNoProject();

        ProjectSupport.ProjectSeed seed;

        seed = new ProjectSupport.ProjectSeed(
            panel.getSelectedProject(), EngineTypeManager.getFallbackEnineType(), panel.getNoProject(),
            pendingJob.captureInfo().executable,
            pendingJob.captureInfo().model,
            /*corefile*/null,
            pendingJob.captureInfo().pid,
            pendingJob.captureInfo().workingDirectory,
            pendingJob.captureInfo().quotedArgvString(),
            /*envs*/ null,
             pendingJob.captureInfo().hostName);

        ProjectSupport.getProject(seed);

        // Do it
        // Is attach sycnhronous?
        try {
            debuggerStarted = false;
            waitingForDebuggerAck = true;
            DebugTarget dt = new DebugTarget(seed.conf());
            dt.setExecutable(seed.executableNoSentinel());
            dt.setHostName(seed.getHostName());
            dt.setPid(seed.pid());
            dt.setCaptureInfo(pendingJob.captureInfo());
            NativeDebuggerManager.get().attach(dt);
        } catch (Exception e) {
            ErrorManager.getDefault().notify(e);
            fail("LBL_IDEError");       // NOI18N
            return;
        }
        // If a debugger is started (i.e. user didn't cancel )
        // DbxDebuggerImpl.start() leaves a crumb by calling
        // this.debuggerStarted()
        // This only works because DbxDebuggerImpl.start() 
	// is run synchronusly.
        // Also it's still too early ... we get debuggerStarted() 
	// called before we attempt to run dbx etc.

        if (!debuggerStarted) {
            fail("LBL_CancelledByUser");        // NOI18N
        }

        waitingForDebuggerAck = false;
    }

    @ServiceProvider(service=ExternalStartProvider.class, position=10000)
    static public class NoopExternalStartProvider implements ExternalStartProvider {

        @Override
	public ExternalStart createExternalStart(Host host) {
	    return new ExternalStart() {

                @Override
		public boolean attached(int pid) {
		    return false;
		}

                @Override
		public void debuggerStarted() {
		}

                @Override
		public void fail() {
		}

                @Override
		public boolean start() {
		    return false;
		}

                @Override
		public boolean stop() {
		    return false;
		}

                @Override
		public boolean isRunning() {
		    return false;
		}
	    };
	}

        @Override
	public boolean isSupported() {
	    return false;
	}
    }
}
