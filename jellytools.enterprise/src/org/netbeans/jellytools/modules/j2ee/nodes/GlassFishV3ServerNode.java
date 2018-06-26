/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.jellytools.modules.j2ee.nodes;

import java.lang.reflect.Method;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author lukas
 */
public class GlassFishV3ServerNode extends J2eeServerNode {

    public GlassFishV3ServerNode() {
        super(Bundle.getString("org.netbeans.modules.glassfish.common.Bundle",
                "STR_V3_SERVER_NAME"));
    }

    public static GlassFishV3ServerNode invoke() {
        RuntimeTabOperator.invoke();
        return new GlassFishV3ServerNode();
    }

    public static GlassFishV3ServerNode checkServerShown() {
        JTreeOperator tree = new RuntimeTabOperator().getRootNode().tree();
        long oldValue = tree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 1);
        GlassFishV3ServerNode result = null;
        try {
            result = GlassFishV3ServerNode.invoke();
        } catch (TimeoutExpiredException e) {
        } finally {
            tree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldValue);
        }
        return result;
    }

    /**
     * Adds GlassFish V3 using path from com.sun.aas.installRoot property
     */
    public static GlassFishV3ServerNode getGlassFishV3Node(String appServerPath) {
        GlassFishV3ServerNode result = checkServerShown();
        if (result != null) {
            return result;
        }

        if (appServerPath == null) {
            throw new Error("Can't add application server. org.glassfish.v3ee6.installRoot property is not set.");
        }

        String addServerMenuItem = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); // Add Server...
        String addServerInstanceDialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"
        String glassFishV3ListItem = Bundle.getStringTrimmed("org.netbeans.modules.glassfish.common.wizards.Bundle", "V3_EE6_NAME");
        String nextButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
        String finishButtonCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");

        RuntimeTabOperator rto = RuntimeTabOperator.invoke();
        JTreeOperator runtimeTree = rto.tree();

        long oldTimeout = runtimeTree.getTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", 6000);

        TreePath path = runtimeTree.findPath("Servers");
        runtimeTree.selectPath(path);

        try {
            //log("Let's check whether GlassFish V3 is already added");
            runtimeTree.findPath("Servers|GlassFish v3");
        } catch (TimeoutExpiredException tee) {
            //log("There is no GlassFish V3 node so we'll add it");
            new JPopupMenuOperator(runtimeTree.callPopupOnPath(path)).pushMenuNoBlock(addServerMenuItem);
            NbDialogOperator addServerInstanceDialog = new NbDialogOperator(addServerInstanceDialogTitle);
            new JListOperator(addServerInstanceDialog, 1).selectItem(glassFishV3ListItem);
            new JButtonOperator(addServerInstanceDialog, nextButtonCaption).push();
            new JTextFieldOperator(addServerInstanceDialog).enterText(appServerPath);
            new JButtonOperator(addServerInstanceDialog, finishButtonCaption).push();
        }
        runtimeTree.getTimeouts().setTimeout("JTreeOperator.WaitNextNodeTimeout", oldTimeout);
        return GlassFishV3ServerNode.invoke();
    }

    /** performs 'Properties' with this node */
    @Override
    public void properties() {
        waitNotWaiting();
        customizerAction.perform(this);
    }

    /** performs 'Start in Debug Mode' with this node */
    @Override
    public void debug() {
        waitNotWaiting();
        startDebugAction.perform(this);
        waitDebugging();
    }

    /** performs 'Refresh' with this node */
    @Override
    public void refresh() {
        waitNotWaiting();
        refreshAction.perform(this);
        waitNotWaiting();
    }

    /** performs 'Remove' with this node */
    @Override
    public void remove() {
        waitNotWaiting();
        removeInstanceAction.perform(this);
    }

    /** performs 'Restart' with this node */
    @Override
    public void restart() {
        waitNotWaiting();
        restartAction.perform(this);
    }

    /** performs 'Start' with this node */
    @Override
    public void start() {
        waitNotWaiting();
        startAction.perform(this);
        waitRunning();
    }

    /** performs 'Stop' with this node */
    @Override
    public void stop() {
        waitNotWaiting();
        stopAction.perform(this);
        waitStopped();
    }

    /** waits till server finishes current action */
    @Override
    public void waitFinished() {
        waitNotWaiting();
    }

    static Class<?> classGlassfishInstance() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class<?> general = Class.forName("org.netbeans.modules.glassfish.common.GlassfishInstance", false, loader);
            return general;
        } catch (ClassNotFoundException ex) {
            return Class.class;
        }
    }

    //
    // redefined to match int values from org.netbeans.modules.glassfish.common.GlassfishInstance.ServerState
    //
    public static final int STATE_WAITING = 0;//STARTING
    public static final int STATE_WAITING_2 = 4;//STOPING
    public static final int STATE_STOPPED = 5;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_DEBUGGING = 2;
    public static final int STATE_SUSPENDED = 6;
    public static final int STATE_PROFILING = 3;
    public static final int STATE_PROFILER_BLOCKING = 7;
    public static final int STATE_PROFILER_STARTING = 8;

//    private static enum ServerState {
//        STARTING,0
//        RUNNING,1
//        RUNNING_JVM_DEBUG,2
//        RUNNING_JVM_PROFILER,3
//        STOPPING,4
//        STOPPED,5
//        STOPPED_JVM_BP,6
//        STOPPED_JVM_PROFILER7
//    }
    /** Waits till server is running in debug mode. */
    private void waitDebugging() {
        waitServerState(STATE_DEBUGGING);
    }

    /** Waits till server is running. */
    private void waitRunning() {
        waitServerState(STATE_RUNNING);
    }

    /** Waits till server is stopped. */
    private void waitStopped() {
        waitServerState(STATE_STOPPED);
    }

    @Override
    public int getServerState() {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        return getServerState(ideNode);
    }

    //// PRIVATE METHODS ////
    private static int getServerState(org.openide.nodes.Node n) {
        try {
            Object server = n.getLookup().lookup(classGlassfishInstance());
            Method m = classGlassfishInstance().getMethod("getServerState");
            return ((Enum) m.invoke(server)).ordinal();
        } catch (Exception ex) {
            return STATE_STOPPED;
        }
    }

    private void waitServerState(int state) {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        final int targetState = state;
        waitFor(new Waitable() {

            public Object actionProduced(Object obj) {
                if (getServerState(ideNode) == targetState) {
                    return "Server state: " + getStateName() + " reached.";
                }
                return null;
            }

            public String getDescription() {
                return "Wait for server state: " + getStateName();
            }

            private String getStateName() {
                switch (targetState) {
                    case STATE_DEBUGGING:
                        return "DEBUGGING";
                    case STATE_RUNNING:
                        return "RUNNING";
                    case STATE_STOPPED:
                        return "STOPPED";
                    case STATE_SUSPENDED:
                        return "SUSPENDED";
                    case STATE_WAITING:
                        return "WAITING (Starting)";
                    case STATE_WAITING_2:
                        return "WAITING_2 (Stopping)";
                    default:
                        return "UNKNOWN STATE";
                }
            }
        });
    }

    private void waitNotWaiting() {
        final org.openide.nodes.Node ideNode = (org.openide.nodes.Node) getOpenideNode();
        waitFor(new Waitable() {

            public Object actionProduced(Object obj) {
                if (getServerState(ideNode) != STATE_WAITING) {
                    return "Server leaves WAITING state.";
                }
                return null;
            }

            public String getDescription() {
                return "Wait till server leaves state WAITING.";
            }
        });
    }

    private static Object waitFor(Waitable action) {
        Waiter waiter = new Waiter(action);
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", 120000);
        try {
            return waiter.waitAction(null);
        } catch (InterruptedException ex) {
            throw new JemmyException(action.getDescription() + " has been "
                    + "interrupted.", ex);
        }
    }
}
