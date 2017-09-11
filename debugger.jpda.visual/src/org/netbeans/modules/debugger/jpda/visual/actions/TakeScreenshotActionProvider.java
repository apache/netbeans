/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.actions;

import com.sun.jdi.ObjectReference;
import java.beans.PropertyChangeEvent;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.RemoteAWTScreenshot;
import org.netbeans.modules.debugger.jpda.visual.RemoteFXScreenshot;
import org.netbeans.modules.debugger.jpda.visual.RetrievalException;
import org.netbeans.modules.debugger.jpda.visual.RemoteServices;
import org.netbeans.modules.debugger.jpda.visual.breakpoints.ComponentBreakpoint;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 * Grabs screenshot of remote application.
 * 
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"takeScreenshot"})
public class TakeScreenshotActionProvider extends ActionsProviderSupport {
    
    private JPDADebugger debugger;
    private BPListener bpListener;

    public TakeScreenshotActionProvider (ContextProvider contextProvider) {
        debugger = contextProvider.lookupFirst(null, JPDADebugger.class);
        addEngineListener();
        bpListener = new BPListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, bpListener, DebuggerManager.getDebuggerManager()));
    }
    
    @Override
    public Set getActions() {
        return Collections.singleton (ScreenshotUIManager.ACTION_TAKE_SCREENSHOT);
    }
    
    @Override
    public void doAction(Object action) {
        String msg = null;
        setEnabled(ScreenshotUIManager.ACTION_TAKE_SCREENSHOT, false);
        try {
            ProgressHandle ph = createProgress();
            
            RemoteScreenshot[] screenshots = null;
            boolean taken = false;
            try {
                screenshots = RemoteAWTScreenshot.takeCurrent(debugger);
                for (int i = 0; i < screenshots.length; i++) {
                    final RemoteScreenshot screenshot = screenshots[i];
                    screenshot.getScreenshotUIManager().open();
                    bpListener.addScreenshot(screenshot);
                    /*
                    SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    ScreenshotComponent sc = new ScreenshotComponent(screenshot);
                    sc.open();
                    sc.requestActive();
                    }
                    });*/
                }
                if (screenshots != null && screenshots.length != 0) {
                    GestureSubmitter.logSnapshotTaken("Swing", debugger);
                    taken = true;
                }

                screenshots = RemoteFXScreenshot.takeCurrent(debugger);
                for (int i = 0; i < screenshots.length; i++) {
                    final RemoteScreenshot screenshot = screenshots[i];
                    screenshot.getScreenshotUIManager().open();
                    bpListener.addScreenshot(screenshot);
                }
                
                if (screenshots != null && screenshots.length != 0) {
                    GestureSubmitter.logSnapshotTaken("JavaFX", debugger);
                    taken = true;
                }
            } finally {
                ph.finish();
            }
            if (!taken) {
                msg = NbBundle.getMessage(TakeScreenshotActionProvider.class, "MSG_NoScreenshots");
            }
        } catch (RetrievalException ex) {
            msg = ex.getLocalizedMessage();
            if (ex.getCause() != null) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            setEnabled(ScreenshotUIManager.ACTION_TAKE_SCREENSHOT, true);
        }
        if (msg != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private ProgressHandle createProgress() {
        ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(TakeScreenshotActionProvider.class, "MSG_TakingApplicationScreenshot"));
        ph.setInitialDelay(500);
        ph.start();
        return ph;
    }

    private DebuggerManagerAdapter enableListener = null;
    
    private void addEngineListener() {
        if (enableListener != null) {
            return ;
        }
        
        enableListener = 
            new DebuggerManagerAdapter() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    //firePropertyChange("enabled", null, null);
                    setEnabled(ScreenshotUIManager.ACTION_TAKE_SCREENSHOT, RemoteServices.hasServiceAccess(debugger));
                }
            };
        
        //DebuggerManager.getDebuggerManager().addDebuggerListener(
        //        DebuggerManager.PROP_CURRENT_ENGINE, enableListener);
        RemoteServices.addServiceListener(enableListener);
    }
    
    private class BPListener implements DebuggerManagerListener {
        
        private final Set<RemoteScreenshot> screenshots = new WeakSet<RemoteScreenshot>();
        
        void addScreenshot(RemoteScreenshot screenshot) {
            synchronized (screenshots) {
                screenshots.add(screenshot);
            }
            Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
            for (Breakpoint b : breakpoints) {
                if (b instanceof ComponentBreakpoint) {
                    markBreakpoint(screenshot, (ComponentBreakpoint) b);
                }
            }
        }
        
        private void markBreakpoint(RemoteScreenshot screenshot, ComponentBreakpoint b) {
            ComponentBreakpoint.ComponentDescription cd = ((ComponentBreakpoint) b).getComponent();
            if (cd != null) {
                ObjectReference oc = cd.getComponent(debugger);
                if (oc != null) {
                    ComponentInfo ci = findComponentInfo(screenshot.getComponentInfo(), oc);
                    if (ci != null) {
                        screenshot.getScreenshotUIManager().markBreakpoint(ci);
                    }
                }
            }
        }
        
        private void unmarkBreakpoint(RemoteScreenshot screenshot, ComponentBreakpoint b) {
            ComponentBreakpoint.ComponentDescription cd = ((ComponentBreakpoint) b).getComponent();
            if (cd != null) {
                ObjectReference oc = cd.getComponent(debugger);
                if (oc != null) {
                    ComponentInfo ci = findComponentInfo(screenshot.getComponentInfo(), oc);
                    if (ci != null) {
                        screenshot.getScreenshotUIManager().unmarkBreakpoint(ci);
                    }
                }
            }
        }
        
        private ComponentInfo findComponentInfo(ComponentInfo ci, ObjectReference oc) {
            if (ci instanceof JavaComponentInfo) {
                ObjectReference or = ((JavaComponentInfo) ci).getComponent();
                if (oc.equals(or)) {
                    return ci;
                }
            }
            for (ComponentInfo sci : ci.getSubComponents()) {
                ComponentInfo fci = findComponentInfo(sci, oc);
                if (fci != null) {
                    return fci;
                }
            }
            return null;
        }

        @Override
        public Breakpoint[] initBreakpoints() {
            return new Breakpoint[] {};
        }

        @Override
        public void breakpointAdded(Breakpoint breakpoint) {
            if (breakpoint instanceof ComponentBreakpoint) {
                RemoteScreenshot[] scrs;
                synchronized (screenshots) {
                    scrs = screenshots.toArray(new RemoteScreenshot[] {});
                }
                for (RemoteScreenshot screenshot : scrs) {
                    markBreakpoint(screenshot, (ComponentBreakpoint) breakpoint);
                }
            }
        }

        @Override
        public void breakpointRemoved(Breakpoint breakpoint) {
            if (breakpoint instanceof ComponentBreakpoint) {
                RemoteScreenshot[] scrs;
                synchronized (screenshots) {
                    scrs = screenshots.toArray(new RemoteScreenshot[] {});
                }
                for (RemoteScreenshot screenshot : scrs) {
                    unmarkBreakpoint(screenshot, (ComponentBreakpoint) breakpoint);
                }
            }
        }

        @Override
        public void initWatches() {}

        @Override
        public void watchAdded(Watch watch) {}

        @Override
        public void watchRemoved(Watch watch) {}

        @Override
        public void sessionAdded(Session session) {}

        @Override
        public void sessionRemoved(Session session) {}

        @Override
        public void engineAdded(DebuggerEngine engine) {}

        @Override
        public void engineRemoved(DebuggerEngine engine) {}

        @Override
        public void propertyChange(PropertyChangeEvent evt) {}
        
    }

}
