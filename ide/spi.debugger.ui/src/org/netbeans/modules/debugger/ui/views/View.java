/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.ui.views;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class CallStackView extends TopComponent {
// ====
public class View extends TopComponent implements org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    public static final String BREAKPOINTS_VIEW_NAME = "BreakpointsView";
    public static final String CALLSTACK_VIEW_NAME = "CallStackView";
    public static final String LOCALS_VIEW_NAME = "LocalsView";
    public static final String SESSIONS_VIEW_NAME = "SessionsView";
    public static final String THREADS_VIEW_NAME = "ThreadsView";
    public static final String WATCHES_VIEW_NAME = "WatchesView";
    public static final String SOURCES_VIEW_NAME = "SourcesView";
    public static final String RESULTS_VIEW_NAME = "ResultsView";
    
    private transient JComponent contentComponent;
    private transient ViewModelListener viewModelListener;
    protected String name; // Store just the name persistently, we'll create the component from that
    protected transient String helpID;
    protected transient String propertiesHelpID;
    private transient String displayNameResource;
    private transient String toolTipResource;
    private transient boolean isComponentShowing;
    
    protected View (String icon, String name, String helpID, String propertiesHelpID,
                  String displayNameResource, String toolTipResource) {
        setIcon (ImageUtilities.loadImage (icon));
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
        this.name = name;
        this.helpID = helpID;
        this.propertiesHelpID = propertiesHelpID;
        this.displayNameResource = displayNameResource;
        this.toolTipResource = toolTipResource;
    }

    @Override
    protected String preferredID() {
        return this.getClass().getPackage().getName() + "." + name;
    }

    @Override
    protected void componentShowing () {
        isComponentShowing = true;
        super.componentShowing ();
        if (viewModelListener != null) {
            viewModelListener.setUp();
            return ;
        }
        JComponent buttonsPane;
        if (contentComponent == null) {
            setLayout (new BorderLayout ());
            contentComponent = new javax.swing.JPanel(new BorderLayout ());
            
            //tree = Models.createView (Models.EMPTY_MODEL);
            if (toolTipResource != null) {
                contentComponent.setName (NbBundle.getMessage (View.class, toolTipResource));
            }
            add (contentComponent, BorderLayout.CENTER);  //NOI18N
            JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
            toolBar.setFloatable(false);
            toolBar.setRollover(true);
            toolBar.setBorderPainted(true);
            if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
                toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            toolBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1,
                    javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));
            add(toolBar, BorderLayout.WEST);
            buttonsPane = toolBar;
        } else {
            buttonsPane = (JComponent) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.WEST);
        }
        // <RAVE> CR 6207738 - fix debugger help IDs
        // Use the modified constructor that stores the propertiesHelpID
        // for nodes in this view
        // viewModelListener = new ViewModelListener (
        //     "ThreadsView",
        //     tree
        // );
        // ====
        viewModelListener = new ViewModelListener (
            name,
            contentComponent,
            buttonsPane,
            propertiesHelpID,
            getIcon()
        );
        // </RAVE>
    }
    
    @Override
    protected void componentHidden () {
        isComponentShowing = false;
        super.componentHidden ();
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct help ID
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(helpID);
    }
    // </RAVE>
    
    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (contentComponent == null) {
            return false;
        }
        if (contentComponent.getComponentCount() > 0) {
            return contentComponent.getComponent(0).requestFocusInWindow ();
        } else {
            return contentComponent.requestFocusInWindow ();
        }
    }

    @Override
    public void requestActive() {
        super.requestActive();
        if (contentComponent != null) {
            if (contentComponent.getComponentCount() > 0) {
                contentComponent.getComponent(0).requestFocusInWindow ();
            } else {
                contentComponent.requestFocusInWindow ();
            }
        }
    }
    
    @Override
    public String getName () {
        return NbBundle.getMessage (View.class, displayNameResource);
    }
    
    @Override
    public String getToolTipText () {
        return NbBundle.getMessage (View.class, toolTipResource);// NOI18N
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper(name);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (isComponentShowing && viewModelListener != null) {
            viewModelListener.setUp();
        }
    }


    /**
     * The serializing class.
     */
    private static final class ResolvableHelper implements Externalizable {
        
        private String name;
        
        private static final long serialVersionUID = 1L;
        
        public ResolvableHelper(String name) {
            this.name = name;
        }
        
        public ResolvableHelper() {
            // Just for the purpose of deserialization
        }
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(name);
        }
        
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            name = (String) in.readObject();
        }
        
        public Object readResolve() {
            return View.getView(name);
        }
    }
    
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getBreakpointsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint.gif",
            BREAKPOINTS_VIEW_NAME,
            "NetbeansDebuggerBreakpointNode",
            null,
            "CTL_Breakpoints_view",
            "CTL_Breakpoints_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getCallStackView() {
        return new View(
            "org/netbeans/modules/debugger/resources/allInOneView/CallStack.gif",
            CALLSTACK_VIEW_NAME,
            "NetbeansDebuggerCallStackNode",
            null,
            "CTL_Call_stack_view",
            "CTL_Call_stack_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getLocalsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/localsView/local_variable_16.png",
            LOCALS_VIEW_NAME,
            "NetbeansDebuggerVariableNode",
            null,
            "CTL_Variables_view",
            "CTL_Locals_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getSessionsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/sessionsView/session_16.png",
            SESSIONS_VIEW_NAME,
            "NetbeansDebuggerSessionNode",
            "NetbeansDebuggerSessionsPropertiesSheet",
            "CTL_Sessions_view",
            "CTL_Sessions_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getThreadsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/threadsView/RunningThread.gif",
            THREADS_VIEW_NAME,
            "NetbeansDebuggerThreadNode",
            "NetbeansDebuggerThreadsPropertiesSheet",
            "CTL_Threads_view",
            "CTL_Threads_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getWatchesView() {
        return new View(
            "org/netbeans/modules/debugger/resources/watchesView/watch_16.png",
            WATCHES_VIEW_NAME,
            "NetbeansDebuggerWatchNode",
            null,
            "CTL_Watches_view",
            "CTL_Watches_view_tooltip"
        );
    }
    
    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getSourcesView() {
        return new View(
            "org/netbeans/modules/debugger/resources/sourcesView/sources_16.png",
            SOURCES_VIEW_NAME,
            "NetbeansDebuggerSourcesNode",
            null,
            "CTL_Sources_view",
            "CTL_Sources_view_tooltip"
        );
    }

    /** Creates the view. Call from the module layer only!
     * @deprecated Do not call.
     */
    @Deprecated
    public static synchronized TopComponent getResultsView() {
        return new View(
            "org/netbeans/modules/debugger/resources/sourcesView/sources_16.png",
            RESULTS_VIEW_NAME,
            "NetbeansDebuggerResultNode", // NOI18N
            null,
            "CTL_Result_view",
            "CTL_Result_view_tooltip"
        );
    }

    private static TopComponent getView(String viewName) {
        if (viewName.equals(BREAKPOINTS_VIEW_NAME)) {
            return getBreakpointsView();
        }
        if (viewName.equals(CALLSTACK_VIEW_NAME)) {
            return getCallStackView();
        }
        if (viewName.equals(LOCALS_VIEW_NAME)) {
            return getLocalsView();
        }
        if (viewName.equals(SESSIONS_VIEW_NAME)) {
            return getSessionsView();
        }
        if (viewName.equals(THREADS_VIEW_NAME)) {
            return getThreadsView();
        }
        if (viewName.equals(WATCHES_VIEW_NAME)) {
            return getWatchesView();
        }
        if (viewName.equals(SOURCES_VIEW_NAME)) {
            return getSourcesView();
        }
        if (viewName.equals(RESULTS_VIEW_NAME)) {
            return getResultsView();
        }
        throw new IllegalArgumentException(viewName);
    }
    
}
