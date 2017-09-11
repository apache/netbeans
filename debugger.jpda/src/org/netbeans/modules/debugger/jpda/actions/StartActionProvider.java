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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.EventRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.AbstractDICookie;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ListeningDICookie;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;

import org.openide.util.Cancellable;
import org.openide.util.Exceptions;


/**
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"start"})
public class StartActionProvider extends ActionsProvider implements Cancellable {
//    private static transient String []        stopMethodNames = 
//        {"main", "start", "init", "<init>"}; // NOI18N

    private static final Logger logger = Logger.getLogger(StartActionProvider.class.getName());
    private static int jdiTrace;
    static { 
        if (System.getProperty ("netbeans.debugger.jditrace") != null) {
            try {
                jdiTrace = Integer.parseInt (
                    System.getProperty ("netbeans.debugger.jditrace")
                );
            } catch (NumberFormatException ex) {
                jdiTrace = VirtualMachine.TRACE_NONE;
            }
        } else
            jdiTrace = VirtualMachine.TRACE_NONE;
    }

    private final JPDADebuggerImpl debuggerImpl;
    private final ContextProvider lookupProvider;
    private Thread startingThread;
    private final Object startingThreadLock = new Object();
    
    
    public StartActionProvider (ContextProvider lookupProvider) {
        debuggerImpl = (JPDADebuggerImpl) lookupProvider.lookupFirst
            (null, JPDADebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_START);
    }
    
    @Override
    public void doAction (Object action) {
        logger.fine("S StartActionProvider.doAction ()");
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        if ( debugger != null && 
             debugger.getVirtualMachine () != null
        ) return;
                
        logger.fine("S StartActionProvider." +
                    "doAction () setStarting");
        debuggerImpl.setStarting ();
        final AbstractDICookie cookie = lookupProvider.lookupFirst(null, AbstractDICookie.class);
        doStartDebugger(cookie);
        logger.fine("S StartActionProvider." +
                    "doAction () end");
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        logger.fine("S StartActionProvider.postAction ()");
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
        if ( debugger != null && 
             debugger.getVirtualMachine () != null
        ) {
            actionPerformedNotifier.run();
            return;
        }
        
        
        final AbstractDICookie cookie = lookupProvider.lookupFirst(null, AbstractDICookie.class);
        
        logger.fine("S StartActionProvider." +
                    "postAction () setStarting");

        debuggerImpl.setStarting ();  // JS
        
        logger.fine("S StartActionProvider." +
                    "postAction () setStarting end");
        
        debuggerImpl.getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                //debuggerImpl.setStartingThread(Thread.currentThread());
                synchronized (startingThreadLock) {
                    startingThread = Thread.currentThread();
                }
                try {
                    doStartDebugger(cookie);
                //} catch (InterruptedException iex) {
                    // We've interrupted ourselves
                } finally {
                    synchronized (startingThreadLock) {
                        startingThread = null;
                        startingThreadLock.notifyAll();
                    }
                    //debuggerImpl.unsetStartingThread();
                    actionPerformedNotifier.run();
                }
                
            }
        });
        
    }
    
    private void doStartDebugger(AbstractDICookie cookie) {
        logger.fine("S StartActionProvider." +
                    "doStartDebugger");
        Throwable throwable = null;
        try {
            debuggerImpl.setAttaching(cookie);
            VirtualMachine virtualMachine = cookie.getVirtualMachine ();
            debuggerImpl.setAttaching(null);
            VirtualMachineWrapper.setDebugTraceMode (virtualMachine, jdiTrace);

            final Object startLock = new Object();
            Operator o = createOperator (virtualMachine, startLock);
            synchronized (startLock) {
                logger.fine("S StartActionProvider.doAction () - " +
                            "starting operator thread");
                o.start ();
                if (cookie instanceof ListeningDICookie) 
                    startLock.wait(1500);
            }
       
            debuggerImpl.setRunning (
                virtualMachine,
                o
            );
          
            // PATCH #46295 JSP breakpoint isn't reached during 
            // second debugging
//            if (cookie instanceof AttachingDICookie) {
//                synchronized (debuggerImpl.LOCK) {
//                    virtualMachine.resume ();
//                }
//            }
            // PATCHEND Hanz

            logger.fine("S StartActionProvider." +
                        "doStartDebugger end: success");
        } catch (InterruptedException iex) {
            throwable = iex;
        } catch (IOException ioex) {
            throwable = ioex;
        } catch (Exception ex) {
            throwable = ex;
            // Notify! Otherwise bugs in the code can not be located!!!
            Exceptions.printStackTrace(ex);
        } catch (ThreadDeath td) {
            throw td;
        } catch (Error err) {
            throwable = err;
            Exceptions.printStackTrace(err);
        }
        if (throwable != null) {
            logger.log(Level.FINE,"S StartActionProvider.doAction ().thread end: threw {0}",
                       throwable);
            debuggerImpl.setException (throwable);
            // kill the session that did not start properly
            final Session session = lookupProvider.lookupFirst(null, Session.class);
            debuggerImpl.getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    // Kill it in a separate thread so that the startup sequence can be finished.
                    session.kill();
                }
            });
        }
    }

    @Override
    public boolean isEnabled (Object action) {
        return true;
    }

    @Override
    public void addActionsProviderListener (ActionsProviderListener l) {}
    @Override
    public void removeActionsProviderListener (ActionsProviderListener l) {}
    
    private Operator createOperator (
        VirtualMachine virtualMachine,
        final Object startLock
    ) {
        return new Operator (
            virtualMachine,
            debuggerImpl,
            new Executor () {
                @Override
                public boolean exec(Event event) {
                    synchronized(startLock) {
                        startLock.notify();
                    }
                    return false;
                }

                @Override
                public void removed(EventRequest eventRequest) {}
                
            },
            new Runnable () {
                @Override
                public void run () {
                    debuggerImpl.finish();
                }
            },
            debuggerImpl.accessLock
        );
    }

    @Override
    public boolean cancel() {
        synchronized (startingThreadLock) {
            logger.log(Level.FINE, "StartActionProvider.cancel(): startingThread = {0}",
                       startingThread);
            for (int i = 0; i < 10; i++) { // Repeat several times, it can be called too early
                if (startingThread != null) {
                    logger.log(Level.FINE, "Interrupting {0}", startingThread);
                    startingThread.interrupt();
                    boolean cancelInterrupted = false;
                    try {
                        startingThreadLock.wait(500);
                    } catch (InterruptedException iex) {
                        cancelInterrupted = true;
                    }
                    AbstractDICookie cookie = lookupProvider.lookupFirst(null, AbstractDICookie.class);
                    logger.log(Level.FINE, "Listening cookie = {0}, is listening = {1}", new Object[]{cookie, cookie instanceof ListeningDICookie});
                    if (cookie instanceof ListeningDICookie) {
                        ListeningDICookie lc = (ListeningDICookie) cookie;
                        try {
                            lc.getListeningConnector().stopListening(lc.getArgs());
                        } catch (IOException ex) {
                        } catch (IllegalConnectorArgumentsException ex) {
                        }
                    }
                    if (cancelInterrupted) {
                        return false;
                    }
                } else {
                    return true;
                }
            }
            return startingThread == null;
        }
    }
}
