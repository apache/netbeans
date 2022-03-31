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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.prefs.Preferences;

import java.io.File;
import java.beans.PropertyChangeEvent;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.loaders.DataObject;
import org.openide.awt.StatusDisplayer;
import org.openide.util.WeakListeners;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.project.Project;

import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.spi.viewmodel.ModelListener;

import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;

import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionLayers;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionClient;
import org.netbeans.modules.cnd.debugger.common2.utils.options.OptionSet;
import org.netbeans.modules.cnd.debugger.common2.utils.InfoPanel;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorDialog;
import org.netbeans.modules.cnd.debugger.common2.utils.ItemSelectorResult;
import org.netbeans.modules.cnd.debugger.common2.utils.Executor;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointBag;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.EngineProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DebuggerOption;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.GlobalOptionSet;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.CustomizableHostList;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Host;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTarget;
import org.netbeans.modules.cnd.debugger.common2.debugger.debugtarget.DebugTargetList;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.ConsoleTopComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.io.PioTopComponent;
import org.netbeans.modules.cnd.debugger.common2.debugger.actions.ProjectSupport;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineTypeManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineType;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerInfo.Factory;
import org.netbeans.modules.cnd.debugger.common2.DbgGuiModule;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.cnd.debugger.common2.DbgActionHandler;
import org.netbeans.modules.cnd.debugger.common2.NativeDebuggerManagerAccessor;
import org.netbeans.modules.cnd.debugger.common2.debugger.options.DbgProfile;
import org.netbeans.modules.cnd.debugger.common2.debugger.remote.Platform;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * debuggercore...DebuggerManager is not meant to be extended or have
 * alternative providers, so we use our own delegating one.
 *
 * It provides the following features:
 * - session startup (debug*() family of methods) 
 * - delegation to debuggercore's DebuggerManager NOT IMPLEMENTED
 * - reference to a TermManager.
 * - Session multiplexing.
 * - refernce to global "current debugger".
 * - quick-and-dirty recent debug target list
 * - the follow-fork dialog
 * - manage error dialogs
 * - manage popup dialogs
 * - manage dialogs
 * - manage the status display
 * - reference to the ActionEnabler
 * - the engine start progress monitor
 * - management of the "$userdir/system/DbxGui" folder.
 * - ownership of global options
 */
public final class NativeDebuggerManager extends DebuggerManagerAdapter {
    
    static {
        NativeDebuggerManagerAccessor.register(new NativeDebuggerManagerAccessorImpl());
    }
    private final static boolean standalone = "on".equals(System.getProperty("spro.dbxtool")); // NOI18N
    private static final boolean pl = "on".equals(System.getProperty("PL_MODE")); // NOI18N;

    private volatile NativeDebugger currentDebugger;
    private InputOutput io;

    // Keep a strong reference to 'changeListener' so it doesn't get GC'ed
    private ChangeListener changeListener;
    
    // request processor for various Native Debugger needs
    private static final RequestProcessor RP = new RequestProcessor("Native Debugger Request Processor", 10); //NOI18N

    private NativeDebuggerManager() {

        delegate().addDebuggerListener(NativeDebuggerManager.this);	// for watchAdded
    // for PROP_CURRENT_SESSION

    // for now we're not saving recent debug targets to disk so
    // hard-code various peoples favorite targets
    }
    
    private static volatile boolean initialized = false;
    
    private final static class LazyInitializer {
        private static final NativeDebuggerManager singleton;
        static {
            initialized = true;
            singleton = new NativeDebuggerManager();
            
            // Initialize DebuggerManager
            singleton.init();

            // restore breakpints if any
            singleton.breakpointBag();
            
            // restore watch bag
            singleton.watchBag();
        }
    }

    public static NativeDebuggerManager get() {
        return LazyInitializer.singleton;
    }
    
    public static void close() {
        if (initialized) {
            get().shutDown();
            get().saveGlobalState();
        }
    }

    public static RequestProcessor getRequestProcessor() {
        return RP;
    }
    
    /**
     * Return whether we're using the Start or the Load/Run model.
     */
    public static boolean isStartModel() {
        String flowModel = System.getProperty("cnd.debuggerflowmodel");
        if ("Start".equals(flowModel)) { // NOI18N
            return true;
        } else if ("LoadRun".equals(flowModel)) { // NOI18N
            return false;
        } else {
            return true;
        }
    }

    /*
     * There are two envs in dbxtool.bash and sunstudio.bash that affect how
     * we decide what's the debug engine is, "cnd.nativedebugger", "debug.engine" :
     *
     * 		  | cnd.nativedebugger 	| debug.engine
     *		  |     'engine'        |    'enabled'
     *   ------------------------------------------------------------
     *   ide	  |  null		| non-null (on)
     *   debugtool|  null               | non-null (on)
     *   gdbtool  |  gdb                | null
     *   dbxtool  |  dbx                | null
     */
    
    public static boolean isChoosableEngine() {
        boolean ret = false;
        String engine_state = System.getProperty("debug.engine");
        if ("on".equalsIgnoreCase(engine_state)) { // NOI18N
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }
    
    /**
     * Return true we're using per-debug-target breakpoints.
     * Default is per-debug-target bpts.
     */
    public static boolean isPerTargetBpts() {
        String perTargetBpts = System.getProperty("dbxgui.pertargetbpts");        
        return isStandalone() && !"false".equals(perTargetBpts);//NOI18N
    }

    public static boolean isAsyncStart() {
        /*
         * Governs whether ACTION_START happens synchronously (i.e on the
         * eventQ) or asynchronously (on an RP).
         *
         * Historically we've run on the eventQ. There is a
         * disadvantage to this in that a start action will fork/exec the engine
	 * and that's a "bad thing" to do on the eventQ. (However, Workshop
         * and sunstudio have done that for many years w/o adverse
         * reprecussions).
         * Another smaller disadvantage is that the progress bar isn't updated
         * nicely.
         *
         * When I first attempted to run start() off the eventQ I ran into
         * problems documented in IZ 50761 (ACTION_START needs to be
         * asynchronous too) so I backed off of that.
         *
         * In the meantime debuggercore beat us to it by making
         * StartAction.doAction() get dispatched on the eventQ. We are
         * working around that by overriding StartAction.postAction() and
         * dispatching start on the eventQ.
         *
         * The purpose of this flag is to allow straightforward switching
         * between the two modes for further experimentation.
         *
         * Note also that the design of glue doesn't accomodate connection
         * establishing of connections off the eventQ so there's a bit of
         * work to do still to properly support async start.
         */

        // return true to make symptoms described in IZ 50761
        // manifest themselves

        return false;
    }

    /* OLD?
    public void shutdownHook() {
    SwingUtilities.invokeLater (new Runnable () {
    public void run () {
    TopComponentGroup group = WindowManager.getDefault ().
    findTopComponentGroup ("debugger"); // NOI18N
    if (group != null) {
    group.close ();
    if (ToolbarPool.getDefault ().getConfiguration ()
    .equals ("Debugging")
    )
    ToolbarPool.getDefault ().setConfiguration
    (ToolbarPool.DEFAULT_CONFIGURATION);
    }
    }
    });
    }
     */
    public Collection<NativeDebugger> nativeDebuggers() {
        ArrayList<NativeDebugger> list = new ArrayList<NativeDebugger>();
        NativeSession[] sessions = getSessions();
        for (int sx = 0; sx < sessions.length; sx++) {
            NativeSession s = sessions[sx];
            DebuggerEngine engine = s.coreSession().getCurrentEngine();
            NativeDebugger debugger = engine.lookupFirst(null, NativeDebugger.class);
            if (debugger != null) {
                list.add(debugger);
            }
        }
        return list;
    }

    /**
     * Spread 'nativeWatch' to all sessions other than 'origin'.
     * If 'origin' is null then it is spread to all sessions.
     */
    public void spreadWatchCreation(NativeDebugger origin,
        NativeWatch nativeWatch) {
        if (Log.Watch.pathway) {
            System.out.println("Spreading watch creation ..."); // NOI18N
        }

	for (NativeSession s : getSessions()) {
            DebuggerEngine engine = s.coreSession().getCurrentEngine();
            NativeDebugger debugger = engine.lookupFirst(null, NativeDebugger.class);
            if (debugger == null) {
                if (Log.Watch.pathway) {
                    System.out.println("\t... null debugger"); // NOI18N
                }
                continue;
            } else if (debugger == origin) {
                if (Log.Watch.pathway) {
                    System.out.println("\t... origin debugger (skipped)"); // NOI18N
                }
                continue;
            } else {
                if (Log.Watch.pathway) {
                    System.out.println("\t... " + debugger); // NOI18N
                }
                debugger.spreadWatchCreation(nativeWatch);
            }
        }
    }

    /**
     * Update all sessions' shadow list of unsaved files
     * If 'debugger' is non-null then only that debuggers list is updated.
     * If 'debugger' is null then all sessions' lists are updated.
     */
    private void notifyUnsavedFiles(NativeDebugger debugger, List<String> fileNames) {
        String file[] = new String[fileNames.size()];
        file = fileNames.toArray(file);
        if (debugger != null) {
            debugger.notifyUnsavedFiles(file);
        } else {
            for (NativeSession s : getSessions()) {
                DebuggerEngine engine = s.coreSession().getCurrentEngine();
                NativeDebugger nd = engine.lookupFirst(null, NativeDebugger.class);
                if (nd != null) {
                    nd.notifyUnsavedFiles(file);
                }
            }
        }
    }

    /**
     * Convert Set to List of String filenames and call notifyUnsavedFiles()
     */
    private void notifyUnsavedFiles(final NativeDebugger debugger, final Set<DataObject> set) {
        RP.submit(new Runnable() {
            @Override
            public void run() {
                List<String> filesNames = new ArrayList<String>();
                for (DataObject dao : set) {
                    FileObject fo = dao.getPrimaryFile();
                    if (fo != null) {
                        File f = FileUtil.toFile(fo);
                        if (f != null) { // can be for memoryFS files
                            try {
                                filesNames.add(f.getCanonicalPath());
                                // DEBUG System.out.println("\t" + f.getCanonicalPath());
                            } catch (Exception ex) {
                                filesNames.add(f.getPath());
                                // DEBUG System.out.println("\t" + f.getPath());
                            }
                        }
                    } else {
                        // DEBUG System.out.println("\tno FO");
                    }
                }
                // DEBUG System.out.println();
                notifyUnsavedFiles(debugger, filesNames);
            }
        });
    }

    /**
     * To be called upon new session startup so debugger engine 
     * starts out with a valid list.
     * @param debugger The debugger which just started.
     */
    public void initialUnsavedFiles(NativeDebugger debugger) {
        DataObject.Registry registry = DataObject.getRegistry();
        notifyUnsavedFiles(debugger, registry.getModifiedSet());
    }

    private void init() {

        /*
         * Track file dirtiness
         */
        DataObject.Registry registry = DataObject.getRegistry();

        changeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent evt) {
		Object source = evt.getSource();
		if (source instanceof Set<?>) {
                    // this is a set of data objects => cast is safe
                    @SuppressWarnings("unchecked")
		    Set<DataObject> set = (Set<DataObject>) source;
		    notifyUnsavedFiles(null, set);
		} else {
		    throw new UnsupportedOperationException(evt.toString());
		}
            }
        };

        // Make it a weak listener
        // But don't override the original 'changeListener'
        ChangeListener weakChangeListener =
            WeakListeners.change(changeListener, registry);


        // Track file dirtiness
        registry.addChangeListener(weakChangeListener);

        // send initial update
        notifyUnsavedFiles(null, registry.getModifiedSet());
    }

    public static boolean isStandalone() {
        return standalone;
    }
    
    public static boolean isPL() {
       return pl;
    }

    /**
     * Convenience function returns the DebuggerManager we're delegating to.
     */
    private static org.netbeans.api.debugger.DebuggerManager delegate() {
        return org.netbeans.api.debugger.DebuggerManager.getDebuggerManager();
    }

    public void shutDown() {
        for (NativeDebugger nativeDebugger : nativeDebuggers()) {
            nativeDebugger.shutDown();
        }
    }

    /**
     * Save all global persistent data (in userdir).
     */
    public void saveGlobalState() {
        if (breakpointBag != null && DebuggerOption.SAVE_BREAKPOINTS.isEnabled(globalOptions())) {
            breakpointBag.save();
        }
        if (isStandalone()) {
            DebugTargetList.saveList();
            CustomizableHostList.saveList();
        }
    }
    /*
     * TermManager
     */
//    private static TermManager termManager;
//
//    public TermManager termManager() {
//	if (termManager == null)
//	    termManager = new TermManager();
//	return termManager;
//    } 

    /*
     * ActionEnabler
     */
    private static ActionEnabler actionEnabler;

    static ActionEnabler actionEnabler() {
        if (actionEnabler == null) {
            actionEnabler = new ActionEnabler();
        }
        return actionEnabler;
    }
    //
    // Session support, in particular multiplexing
    //
    private ModelChangeDelegator sessionUpdater = new ModelChangeDelegator();

    public void registerSessionModel(ModelListenerSupport model) {
        sessionUpdater.addListener(model);
    }

    public ModelListener sessionUpdater() {
        return sessionUpdater;
    }

    public NativeSession[] getSessions() {
        List<NativeSession> nativeSessions = new ArrayList<NativeSession>();
        Session[] coreSessions = delegate().getSessions();
        for (int sx = 0; sx < coreSessions.length; sx++) {
            NativeSession ds = NativeSession.map(coreSessions[sx]);
            if (ds != null) {
                nativeSessions.add(ds);
            }
        }
        return nativeSessions.toArray(new NativeSession[nativeSessions.size()]);
    }

    public int sessionCount() {
        return delegate().getSessions().length;
    }

    public void setCurrentSession(Session s) {
        delegate().setCurrentSession(s);
    }

    public void setCurrentDebugger(NativeDebugger d) {
        currentDebugger = d;
    }

    public NativeDebugger currentDebugger() {
        return currentDebugger;
    }

    // OLD: moved logic into clients
//    public DbxDebuggerImpl currentDbxDebugger() {
//        if (currentDebugger instanceof DbxDebuggerImpl) {
//            return (DbxDebuggerImpl) currentDebugger;
//        } else {
//            return null;
//        }
//    }

    // OLD: is not used anywhere
//    public GdbDebuggerImpl currentGdbDebugger() {
//        if (currentDebugger instanceof GdbDebuggerImpl) {
//            return (GdbDebuggerImpl) currentDebugger;
//        } else {
//            return null;
//        }
//    }

    public NativeDebugger currentNativeDebugger() {
        return currentDebugger;
    }

    private void switchOutput(final NativeDebugger od, final NativeDebugger nd) {

        // Issue 46475 documents how we get this called redundantly at times.
        final boolean redundant = (od == nd);
        // Well ... We go one of these off the AWT-EQ when a jpda
        // debugger was started. I don't like this. What happens if
        // a user clicks switch sessions quickly that we get called again
        // between the activate and the deactivate?

//	if (od != null)
//	    od.savedVisibility(od.termset().getVisibility());

        if (od != null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    od.deactivate(redundant);
                }
            });
        }

        if (nd != null) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    nd.activate(redundant);
                }
            });
        }

        currentDebugger = nd;
    }

    // interface DebuggerManagerAdapter
    @Override
    public void propertyChange(PropertyChangeEvent e) {

        if (org.netbeans.api.debugger.DebuggerManager.PROP_CURRENT_SESSION.equals(e.getPropertyName())) {

            if (Log.Start.debug) {
                System.out.printf("DebuggerManager.PROP_CURRENT_SESSION\n"); // NOI18N
            }

            //System.err.println("DDM.propertyChange: PROP_CURRENT_SESSION");
            // Our sessions have only one debugger engine
            // If we get a non-native session, we'll get _some_ engine
            // and if it's not a native engine then lookup() will return a null
            // and that will make switchOutput do the correct thing as well.

            NativeDebugger odebugger = null;
            Session osession = (Session) e.getOldValue();
            if (osession != null) {
                DebuggerEngine oengine = osession.getCurrentEngine();
                odebugger = oengine.lookupFirst(null, NativeDebugger.class);
            }

            NativeDebugger ndebugger = null;
            Session nsession = (Session) e.getNewValue();
            if (nsession != null) {
                DebuggerEngine nengine = nsession.getCurrentEngine();
                ndebugger = nengine.lookupFirst(null, NativeDebugger.class);
            }

            switchOutput(odebugger, ndebugger);
        }
    }
    private final Map<Watch, NativeWatch> watchMap = new HashMap<Watch, NativeWatch>();
    
    /**
     * Add a NativeWatch to the watch map.
     */
    public void watchMap(NativeWatch nativeWatch) {
        assert nativeWatch.watch() != null;
        watchMap.put(nativeWatch.watch(), nativeWatch);
    }

    /**
     * Note that a Watch was added.
     * Called out from debuggercore.
     */

    // interface DebuggerManagerAdapter
    @Override
    public void watchAdded(Watch watch) {
        if (Log.Watch.pathway) {
            System.out.printf("DebuggerManager.watchAdded('%s')\n", // NOI18N
                watch.getExpression());
        }
        if (watch.getExpression() == null) {
            // Watch created through the SPONTANEOUS pathway
            if (Log.Watch.pathway) {
                System.out.printf("\tnull expression\n"); // NOI18N
            }

        } else {
            if (Log.Watch.pathway) {
                System.out.printf("\tproceeding\n"); // NOI18N
            }
            NativeWatch nativeWatch = new NativeWatch(watch);
            watchMap(nativeWatch);
            watchBag().restore(nativeWatch);
            
            // IZ 181906 was fixed, so this should not happen any more
            //if (!watchBag().isRestoring()) {
                // if we're restoring the watches will be applied on prog_load.
                spreadWatchCreation(null, nativeWatch);
            //}

        }
    }

    /**
     * Note that a Watch was removed.
     * Called out from debuggercore.
     */

    // interface DebuggerManagerAdapter
    @Override
    public void watchRemoved(Watch watch) {
        if (Log.Watch.pathway) {
            System.out.printf("DebuggerManager.watchRemoved('%s')\n", // NOI18N
                watch.getExpression());
        }
        NativeWatch removed = watchMap.remove(watch);
        assert removed == null || removed.watch() == watch;
        watchBag().remove(removed);
    }
    /**
     * Panel which asks whether we start a new session, reuse one 
     * or cancel the start.
     *
     * was: org.netbeans.modules.debugger.multisession.EnterpriseDebugger.
     *	showStartSessionDialog()
     */
    private static enum Start {NEW, CANCEL, REUSE};

    private Start startDebuggerDialog(String programInfo, String hostname) {

        if (DebuggerOption.SESSION_REUSE.isEnabled(globalOptions())) {
            return Start.REUSE;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 8));

        String title = Catalog.get("TITLE_StartNewSession");// NOI18N
        String msg = Catalog.format("FMT_StartNewSession", // NOI18N
            programInfo, hostname);

        JTextArea textArea = new JTextArea(msg);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setEnabled(false);
        textArea.setOpaque(false);
        textArea.setFont(UIManager.getFont("Label.font"));	// NOI18N
        textArea.setDisabledTextColor(
            UIManager.getColor("Label.foreground")); // NOI18N
        Catalog.setAccessibleName(panel, "ACSD_StartNewSession"); // NOI18N
        panel.getAccessibleContext().setAccessibleDescription(textArea.getText());
        panel.add(textArea, BorderLayout.CENTER);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setText(Catalog.get("CTL_session_reuse")); // NOI18N
        checkBox.setMnemonic(Catalog.get("CTL_session_reuse_Mnemonic").charAt(0)); // NOI18N
        checkBox.getAccessibleContext().setAccessibleDescription(Catalog.get("ACSD_session_reuse")); // NOI18N
        checkBox.setSelected(DebuggerOption.SESSION_REUSE.isEnabled(globalOptions()));

        panel.add(checkBox, BorderLayout.SOUTH);

        panel.setPreferredSize(new java.awt.Dimension(100, 130));



        NotifyDescriptor.Confirmation descriptor =
            new NotifyDescriptor.Confirmation(panel, title);

        JButton b_reuse =
            new JButton(Catalog.get("CTL_FinishAndStart"));	// NOI18N
        b_reuse.setMnemonic(Catalog.get("CTL_FinishAndStart_Mnemonic").charAt(0)); // NOI18N
        b_reuse.getAccessibleContext().setAccessibleDescription(Catalog.get("ACSD_FinishAndStart")); // NOI18N
        JButton b_new =
            new JButton(Catalog.get("CTL_StartNewSession"));// NOI18N
        b_new.setMnemonic(Catalog.get("CTL_StartNewSession_Mnemonic").charAt(0)); // NOI18N
        b_new.getAccessibleContext().setAccessibleDescription(Catalog.get("ACSD_StartNewSession")); // NOI18N

        descriptor.setOptions(new Object[]{
                b_reuse, b_new, NotifyDescriptor.CANCEL_OPTION
            });

        // show dialog
        Object pressedButton = DialogDisplayer.getDefault().notify(descriptor);

        DebuggerOption.SESSION_REUSE.setEnabled(new OptionLayers(globalOptions),
            checkBox.isSelected());

        if (pressedButton == b_new) {
            return Start.NEW;
        } else if (pressedButton == b_reuse) {
            return Start.REUSE;
        } else {
            return Start.CANCEL;
        }
    }

    //
    // Utilities to extract stuff from Configuration's
    //
    private static CompilerSet compilerSet(Configuration makeConfiguration) {
        if ((makeConfiguration instanceof MakeConfiguration) && ((MakeConfiguration)makeConfiguration).getCompilerSet() != null) {
            return ((MakeConfiguration)makeConfiguration).getCompilerSet().getCompilerSet();
        } else {
            return null;
        }
    }


    //
    // Main debug actions and their support routines.
    //

    /**
     * Return the debugger type for the given configuration.
     * If -J-Dcnd.nativedebugger=dbx|gdb was used on the commandline then
     * it is used.
     * Otherwise the general Debugger properties "engine" property is 
     * inspected. If it is a specific debugger that one is used.
     * If it is set to "inherit" then the CompilerCollection property is used.
     */
    public static EngineType debuggerType(Configuration configuration) {
        EngineType ret = EngineTypeManager.getOverrideEngineType();
        if (ret != null) {
            return ret;
        }
        EngineProfile engineProfile = (EngineProfile) configuration.getAuxObject(EngineProfile.PROFILE_ID);
        if (engineProfile == null || !NativeDebuggerManager.isChoosableEngine()) {
            ret = EngineTypeManager.getInherited();
        } else {
            ret = engineProfile.getEngineType();
        }
        if (ret.isInherited()) {
            final CompilerSet compilerSet = compilerSet(configuration);
            if (compilerSet != null) {
                Tool debugger = compilerSet.getTool(PredefinedToolKind.DebuggerTool);
                if (debugger != null) {
                    DebuggerDescriptor descriptor = (DebuggerDescriptor) debugger.getDescriptor();
                    EngineType typeForTool = EngineTypeManager.getEngineTypeForDebuggerDescriptor(descriptor);
                    if (typeForTool != null) {
                        ret = typeForTool;
                    }
                }
            }
        }
        if (ret.isInherited()) {
            ret = EngineTypeManager.getFallbackEnineType();
        }
        return ret;
    }


    /*
     * Factory for NativeDebuggerInfo
     */
    private NativeDebuggerInfo makeNativeDebuggerInfo(EngineType debuggerType) {

        NativeDebuggerInfo info = null;
        Collection<? extends Factory> factories = Lookup.getDefault().lookupAll(NativeDebuggerInfo.Factory.class);
        for (Factory factory : factories) {
            info = factory.create(debuggerType);
            if (info != null) {
                break;
            }
        }
        assert info != null : "unknown debugger type " + debuggerType;
//        if (debuggerType.equals("dbx")) {
//            info = DbxDebuggerInfo.create(debuggerType);
//        } else if (debuggerType.equals("gdb")) {
//            info = GdbDebuggerInfo.create(debuggerType);
//        } else {
//            assert false : "known debugger type";
//        }

        return info;
    }

    /**
     * Find out from the user whether they want to reuse an existng session
     * or start a new one.
     * Deals with the engine not being compatible with desired type
     * (gdb vs dbx).
     * 
     * On Linux, figure out if dbx and app match. (32 vs 64)
     */
    private Start getExistingDebugger(NativeDebuggerInfo ndi) {
        Start start = Start.CANCEL;

        if (currentDebugger != null) {
            // have existing session, ask user what to do
            String currentHostName = currentDebugger.session().getSessionHost();
            String desiredHostName = ndi.getHostName();


   //         EngineType currentEngine = currentDebugger.session().getSessionEngine();
            EngineType currentEngine = currentDebugger.getNDI().debuggerType();
            EngineType desiredEngine = ndi.debuggerType();

            if (Log.Startup.debug) {
                System.out.printf("getExistingDebugger():\n"); // NOI18N
                System.out.printf("\tcurrentHostName %s\n", currentHostName); // NOI18N
                System.out.printf("\tdesiredHostName %s\n", desiredHostName); // NOI18N
            }

            if ((currentEngine != null) && !currentEngine.equals(desiredEngine)) {
                start = Start.NEW;
            } else if (ndi.isCaptured()) {
                start = Start.NEW;
            } else if (IpeUtils.sameString(currentHostName, desiredHostName)) {
		Host currentHost = currentDebugger.getHost();
                // 6821014
                if (!currentDebugger.state().isLoaded) {
		    // empty , resue this engine session
		    if (!currentHost.isLinux()) {
			start = Start.REUSE;
		    } else { // isLinux
		    /* LATER
			if ((currentHost.isLinux64() && is64bitDebuggee(ndi, currentHost)) ||
			    (!currentHost.isLinux64() && !is64bitDebuggee(ndi, currentHost)))
			    start = Start.REUSE;
			else
			*/
			    start = Start.NEW;
		    }
                } else {
		    Host desiredHost = getHostList().getHostByName(desiredHostName);
		    if (desiredHost != null && desiredHost.isLinux()) {
			start = Start.NEW;
		    } else {
			String programInfo = currentDebugger.session().toString();
			start = startDebuggerDialog(programInfo, desiredHostName);
		    }
                }
            } else {
                // current session is not on the same host start a new one
                start = Start.NEW;
            }

        } else {
            // go ahead and start a new one
            start = Start.NEW;
        }


        if (start == Start.CANCEL) {
            return start;

        } else if (start == Start.NEW) {
            return start;

        } else {
            // SHOULD check that the profile (or ndi) isn't specifying
            // a new host
            return Start.REUSE;
        }
    }

    /**
     * Common debugger startup entry point.
     * Used for load/attach/core & ...
     * Starts a new session w/o asking the user about session reuse.
     */
    private void startDebugger(final Start start, NativeDebuggerInfo ndi) {
        if (start == Start.CANCEL) {
            return;

        } else if (start == Start.NEW) {
            if (currentDebugger != null) {
                NativeDebuggerManager.openComponent("sessionsView", true); // NOI18N
            }
            debugNoAsk(ndi);

        } else {
            currentDebugger.reuse(ndi);
        }
    }

    /**
     * Common debugger startup entry point.
     * Starts a new session w/o asking the user about session reuse.
     */
    public void debugNoAsk(NativeDebuggerInfo ndi) {
        // "convert" our NativeDebuggerInfo to a core DebuggerInfo
        // I"m not really sure what exactly happens here, just aping jpda.
        final DebuggerInfo di =
            DebuggerInfo.create(ndi.getID(), new Object[]{ndi});

        // We eventually end up in DbxStartActionProvider.post/doAction()
        // which calls DbxDebuggerImpl.start()
        // See "./README.startup"

        if (isAsyncStart()) {
            getRequestProcessor().post(new Runnable() {

                @Override
                public void run() {
                    delegate().startDebugging(di);
                }
            });
        } else {
            delegate().startDebugging(di);
        }
    }

    /**
     * Create a new project and debug it.
     */
    public void debugProject(DebugTarget dt, String projectFolder, String processName) {
        Project project = null;

        if (projectFolder != null && processName != null) {
            project = ProjectSupport.matchProject(projectFolder, processName);
        }

        if (project == null) {
            try {
                project = dt.createProject();

            } catch (Exception e) {
                project = null;
            }
        }

        if (project != null) {
            ProjectActionEvent projectActionEvent = new ProjectActionEvent(
		    project, ProjectActionEvent.PredefinedType.DEBUG_STEPINTO, dt.getExecutable(),
		    ConfigurationSupport.getProjectActiveConfiguration(project), null, false);
	    ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[] {projectActionEvent});
        } else {
            System.out.println("    project is null        "); // NOI18N
        }
    }
    /**
     * Start debugging target.
     */
    private static DebugTargetList debugtargetlist = null;

    public void debugTarget(DebugTarget debugtarget, boolean runFirst, boolean use32bitEngine, InputOutput io) {
        Configuration conf = debugtarget.getConfig();
        String execPath = debugtarget.getExecutable();
        NativeDebuggerInfo ndi = makeNativeDebuggerInfo(debugtarget.getEngine());
        ndi.setDebugTarget(debugtarget);

        if (execPath == null || execPath.trim().isEmpty()) { // NOI18N
            if (debugtarget.getHostName().equals("localhost")) { // NOI18N
                conf.getProfile().setRunDir(System.getenv("PWD")); // NOI18N
            }
        }

        if (execPath != null) {
            ((MakeConfiguration) conf).getMakefileConfiguration().getOutput().setValue(execPath);
        }
        

        ndi.setTarget(execPath);
        ndi.setHostName(debugtarget.getHostName());
        ndi.setConfiguration(conf);
        ndi.set32bitEngine(use32bitEngine);
        ndi.setInputOutput(io);

        if (runFirst) {
            ndi.setAction(RUN);
        } else {
            if (isStandalone() || isPL() || !ndi.isAutoStart() || 
                    !DebuggerOption.RUN_AUTOSTART.isEnabled(globalOptions())) {
                ndi.setAction(LOAD);
            } else {
                ndi.setAction(this.getAction());
            }
        }
        
        String symbolFile = DebuggerOption.SYMBOL_FILE.getCurrValue(ndi.getDbgProfile().getOptions());
        symbolFile = ((MakeConfiguration) conf).expandMacros(symbolFile);
        ndi.setSymbolFile(symbolFile);

        if (isStandalone()) {
            startDebugger(getExistingDebugger(ndi), ndi);
        } else {
            startDebugger(Start.NEW, ndi);
        }
    }
    
    public void debugTarget(DebugTarget debugtarget, boolean runFirst, boolean use32bitEngine) {
        debugTarget(debugtarget, runFirst, use32bitEngine, null);
    }

    private static final Preferences prefs =
        NbPreferences.forModule(NativeDebuggerManager.class);
    private static final String PREFIX = "Doption."; // NOI18N
    private static final String PREF_DONOTSHOWAGAIN =
        PREFIX + "doNotShowAgain";      // NOI18N

    private static boolean isDoNotShowAgain() {
        return prefs.getBoolean(PREF_DONOTSHOWAGAIN, false);
    }

    private static void setDoNotShowAgain(boolean doNotShowAgain) {
        prefs.putBoolean(PREF_DONOTSHOWAGAIN, doNotShowAgain);
    }

    private static void refDbxtool(String msg) {
        InfoPanel panel = new InfoPanel(msg);
        NotifyDescriptor dlg = new NotifyDescriptor.Confirmation(
            panel,
            Catalog.get("INFORMATION"), // NOI18N
            NotifyDescriptor.DEFAULT_OPTION);
        Object answer = DialogDisplayer.getDefault().notify(dlg);
        setDoNotShowAgain(panel.dontShowAgain());

        if (answer == NotifyDescriptor.CLOSED_OPTION) {
            return;
        }
    }
    private CustomizableHostList hostList = null;

    public CustomizableHostList getHostList() {
        if (!isStandalone()) {
            return null;
        }

        if (hostList == null) {
            hostList = CustomizableHostList.getInstance();
        }

        return hostList;
    }

    public DebugTarget getDTByKey(String key) {
        if (debugtargetlist == null) {
            return null;
        }

        DebugTarget dt = null;
        int dtIndex = debugtargetlist.recordByKey(key);
        if (dtIndex != -1) {
            dt = debugtargetlist.getRecordAt(dtIndex);
        }

        return dt;
    }

    public DebugTargetList getDebugTargetList() {
        return debugtargetlist;
    }

    public void addRecentDebugTarget(String progname, boolean from_unload) {
        if (!isStandalone() || progname == null || progname.length() == 0) {
            return;
        }

        // SHOULD pass on the platform we're originating from so it can
        // properly be used as a DebugTarget key.

        DebugTarget dt = currentDebugger.getNDI().getDebugTarget();
        if (dt == null) {
            // follow fork - attach child
            return;
        }

        String corefile = currentDebugger.session().getCorefile();

        if (corefile != null) { // we want to remember executable path instead of corefile path
            dt.setExecutable(progname);
        } else if (!dt.getExecutable().equals(progname) && !from_unload) {
            // load progname from dbx console
            dt.setExecutable(progname);
        }

        // not for attach <pid>
        // 6827590
        // if (!currentDebugger.state().isAttach) {
        // find an existing dt from debugtarget list first
        int foundAt = debugtargetlist.recordByKey(dt.getKey());
        if (foundAt != -1) {
            // found it
            debugtargetlist.replaceRecordAt(dt, foundAt);
            debugtargetlist.moveToFront(foundAt);


        // watch out for second time of Debug Most Recent
        // pcs will be overrided by first time of Debug Most Recent
        // dt = debugtargetlist.getRecordAt(foundAt);
        // currentDebugger.getNDI().setDebugTarget(dt);

        } else {
            // not found, fall through and add into debugtarget list
            debugtargetlist.addRecord(dt);
        }
    //}
    }

    /**
     * Start debugging by loading program.
     */
    public NativeDebuggerInfo debug(String executable, String symbolFile, Configuration configuration, String host,
            InputOutput io, DbgActionHandler dah, RunProfile profile) {
        NativeDebuggerInfo ndi = makeNativeDebuggerInfo(debuggerType(configuration));
        ndi.setTarget(executable);
        ndi.setHostName(host);
        ndi.setConfiguration(configuration);
        ndi.setProfile(profile);
        ndi.setInputOutput(io);
        ndi.setDah(dah);
        if (isStandalone() || !ndi.isAutoStart() || !DebuggerOption.RUN_AUTOSTART.isEnabled(globalOptions())) {
            ndi.setAction(LOAD);
        } else {
            ndi.setAction(this.getAction());
        }
        
        DbgProfile dbgProfile = ndi.getDbgProfile();
        // override executable if needed
        String debugExecutable = dbgProfile.getExecutable();
        if (debugExecutable != null && !debugExecutable.isEmpty()) {
            ndi.setTarget(debugExecutable);
        }
        if (symbolFile == null || symbolFile.isEmpty()) {
            symbolFile = DebuggerOption.SYMBOL_FILE.getCurrValue(dbgProfile.getOptions());
            symbolFile = ((MakeConfiguration) configuration).expandMacros(symbolFile);
            if (!CndPathUtilities.isPathAbsolute(symbolFile)) {
                symbolFile = ((MakeConfiguration) configuration).getBaseDir() + "/" + symbolFile; // NOI18N
                symbolFile = CndPathUtilities.normalizeSlashes(symbolFile);
                symbolFile = CndPathUtilities.normalizeUnixPath(symbolFile);
            }
        }
        ndi.setSymbolFile(symbolFile);

        startDebugger(Start.NEW, ndi);
        return ndi;
    }
    
    /**
     * Start debugging by attaching to pid.
     * @param isAutoStart <code>true</code> if start after attaching, otherwise use <code>false</code>
     * @param dt 
     */
    public void attach(boolean isAutoStart, DebugTarget dt) {
        attach(dt, null, isAutoStart);
    }

    /**
     * Start debugging by attaching to pid.
     * @param dt
     */
    public void attach(DebugTarget dt) {
        attach(dt, null, DebuggerOption.RUN_AUTOSTART.isEnabled(NativeDebuggerManager.get().globalOptions()));
    }
    
    //make it private as DbgActionHandler is not public therefore method should be package or private 
    //NativeDebuggerManagerAccessor is inroduced to call this method
    private void attach(DebugTarget dt, DbgActionHandler dah) {
        attach(dt, dah, DebuggerOption.RUN_AUTOSTART.isEnabled(NativeDebuggerManager.get().globalOptions()));
    }
    
    private void attach(DebugTarget dt, DbgActionHandler dah, boolean isAutoStart) {
        final Configuration conf = dt.getConfig();

        NativeDebuggerInfo ndi = makeNativeDebuggerInfo(dt.getEngine());
        ndi.setDebugTarget(dt);
        ndi.setPid(dt.getPid());
        ndi.setDah(dah);
        ndi.setAutoStart(isAutoStart);

        //ndi.setTarget(dt.getExecutable());
	Host host = Host.byName(dt.getHostName());
        Executor executor = Executor.getDefault(Catalog.get("File"), host, 0); // NOI18N
	EngineDescriptor engine = ndi.getEngineDescriptor();
        
        if (dt.getProjectMode() == DebugTarget.ProjectMode.NO_PROJECT) {
            conf.getProfile().setRunDirectory(executor.readDirLink(dt.getPid()));
        }

	// CR 6997426, cause gdb problem IZ 193248
	// ndi.setTarget("-"); // NOI18N
        ndi.setConfiguration(conf);
        ndi.setHostName(dt.getHostName());
        ndi.setAction(ATTACH);
        ndi.setCaptureInfo(dt.getCaptureInfo());
        
        if (dt.getProjectMode() == DebugTarget.ProjectMode.OLD_PROJECT) {
            String symbolFile = DebuggerOption.SYMBOL_FILE.getCurrValue(ndi.getDbgProfile().getOptions());
            symbolFile = ((MakeConfiguration) conf).expandMacros(symbolFile);
            if (!symbolFile.isEmpty() && !CndPathUtilities.isPathAbsolute(symbolFile)) {
                symbolFile = ((MakeConfiguration) conf).getBaseDir() + "/" + symbolFile; // NOI18N
                symbolFile = CndPathUtilities.normalizeSlashes(symbolFile);
                symbolFile = CndPathUtilities.normalizeUnixPath(symbolFile);
                // The below is a partial fix of the issue #252827 - Remote Attach doesn't work
                // The changesets 6ec8ff163ad7 and a459d48725b5 don't seem correct,
                // but their motivation need to be thoroughly investigated for the new fix not to break old ones.
                // In any case, if the path is not absolute, then project will return LOCAL path,
                // so we need to map it to remote one
                MakeConfiguration makeConf = (MakeConfiguration) conf;
                ExecutionEnvironment execEnv = makeConf.getDevelopmentHost().getExecutionEnvironment();
                if (execEnv.isRemote()) {
                    RemoteSyncFactory syncFactory = makeConf.getRemoteSyncFactory();
                    if (syncFactory != null) {
                        PathMap pathMap = syncFactory.getPathMap(execEnv);
                        if (pathMap != null) {
                            String remoteSymbolFile = pathMap.getRemotePath(symbolFile, false);
                            if (remoteSymbolFile != null) {
                                symbolFile = remoteSymbolFile;
                            }
                        }
                    }
                }
                // end of the partial fix of the issue #252827
            }
            ndi.setTarget(symbolFile);
        } else {
            // CR 6997426, cause gdb problem IZ 193248
            if (engine.hasCapability(EngineCapability.DERIVE_EXECUTABLE)) {
                ndi.setTarget("-"); //NOI18N
            } else {
                String execPath = ndi.getTarget();
                switch (host.getPlatform()) {
                    case MacOSX_x86:
                        execPath = executor.readlsof(dt.getPid());
                        break;
                    case Windows_x86:
                        // omit arguments (IZ 230518)
                        //execPath = execPath.split(" ")[0]; // NOI18N
                        break;
                    default:
                        execPath = executor.readlink(dt.getPid());
                        break;
                }
                ndi.setTarget(execPath);
            }
        }
        if (isStandalone()) {
            startDebugger(getExistingDebugger(ndi), ndi);
        } else {
            startDebugger(Start.NEW, ndi);
        }        
    }

    public void debugCore(DebugTarget dt) {

        Configuration conf = dt.getConfig();


        NativeDebuggerInfo ndi = makeNativeDebuggerInfo(dt.getEngine());
        ndi.setDebugTarget(dt);

        ndi.setTarget(dt.getExecutable());
        ndi.setHostName(dt.getHostName());
        ndi.setCorefile(dt.getCorefile());
        ndi.setConfiguration(conf);
        ndi.setAction(CORE);
        
        if (isStandalone()) {
            startDebugger(getExistingDebugger(ndi), ndi);
        } else {
            startDebugger(Start.NEW, ndi);
        }
    }

    /*
     * Stuff to manage debug target
     */
    public void restoreDT() {
        if (isStandalone() && debugtargetlist == null) {
            // work around for race condition of restoring debug list from disk
            // when using option -D
            try {
                java.lang.Thread.sleep(200);
            } catch (Exception e) {
            }

            debugtargetlist = DebugTargetList.getInstance();
        }
    }
    /*
     * Stuff to manege global watches
     */
    private WatchBag watchBag;

    public WatchBag watchBag() {
        if (watchBag == null) {
            watchBag = new WatchBag();
            // on creation read all existing watches from debuggercore Watches
            // see IZ 203606
            Watch[] existingWatches = getWatches();
            for (Watch watch : existingWatches) {
                watchAdded(watch);
            }
        }
        return watchBag;
    }

    public void postDeleteAllWatches() {
        for (NativeDebugger debugger : nativeDebuggers()) {
            debugger.postDeleteAllWatches();
        }
    }
    private ModelListenerSupport watchModelListener =
        new ModelListenerSupport("watch"); // NOI18N

    public ModelListenerSupport watchModelListener() {
        return watchModelListener;
    }
    private final ModelChangeDelegator watchUpdater = new ModelChangeDelegator();

    public ModelChangeDelegator watchUpdater() {
        return watchUpdater;
    }
    
    private final Map<NativeDebugger, PinWatchUISupport.ValueProvider.ValueChangeListener> pinnedWatchesUpdaters = new ConcurrentHashMap();
    
    /*package*/ void registerPinnedWatchesUpdater(NativeDebugger debugger, PinWatchUISupport.ValueProvider.ValueChangeListener listener) {
        pinnedWatchesUpdaters.put(debugger, listener);
    }
    
    public void firePinnedWatchChange(NativeDebugger debugger, Watch watch) {
        PinWatchUISupport.ValueProvider.ValueChangeListener listener = pinnedWatchesUpdaters.get(debugger);
        if (listener != null) {
            listener.valueChanged(watch);
        }
    }
    
    /*
     * Stuff to manage global breakpoints
     */
    private BreakpointBag breakpointBag;

    public BreakpointBag breakpointBag() {
        if (breakpointBag == null) {
            breakpointBag = new BreakpointBag();
            if (DebuggerOption.SAVE_BREAKPOINTS.isEnabled(globalOptions())) {
                breakpointBag.restore();
            }
        }
        return breakpointBag;
    }
    private ModelListenerSupport breakpointModelListener =
        new ModelListenerSupport("breakpoint"); // NOI18N

    public ModelListenerSupport breakpointModelListener() {
        return breakpointModelListener;
    }
    private ModelChangeDelegator breakpointUpdater = new ModelChangeDelegator();

    public ModelChangeDelegator breakpointUpdater() {
        return breakpointUpdater;
    }


    /*
     * Delegations to debuggercores DebuggerManager
     */
    public Watch createWatch(String expression) {
        return delegate().createWatch(expression);
	// We'll continue in watchAdded()
    }

    public Watch[] getWatches() {
        return delegate().getWatches();
    }

    public void removeBreakpoint(NativeBreakpoint b) {
        delegate().removeBreakpoint(b);
    }

    public void addBreakpoint(NativeBreakpoint b) {
        delegate().addBreakpoint(b);
    }

    /* 6306083 */
    public void handleExec64() {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.add(new JLabel(Catalog.get("EXEC64MSG")), // NOI18N
            java.awt.BorderLayout.NORTH);
        JCheckBox jc = new JCheckBox(Catalog.get("OPTION_EXEC64")); // NOI18N
        jc.getAccessibleContext().setAccessibleDescription(
            Catalog.get("ACSD_OPTION_EXEC64")); // NOI18N
        panel.add(jc, java.awt.BorderLayout.SOUTH);
        jc.setSelected(!DebuggerOption.OPTION_EXEC32.isEnabled(currentDebugger.optionLayers()));

        DialogDescriptor dlg = new DialogDescriptor(
            panel, // NOI18N
            Catalog.get("DebuggerError"), // NOI18N
            true,
            new Object[]{
                jc,
                DialogDescriptor.OK_OPTION
            },
            jc,
            DialogDescriptor.BOTTOM_ALIGN,
            null,
            null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dlg.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        dlg.setClosingOptions(new Object[]{DialogDescriptor.OK_OPTION});
        dialog.setVisible(true);

        if (jc.isSelected()) {
            DebuggerOption.OPTION_EXEC32.setCurrValue(currentDebugger.optionLayers(), "off"); // NOI18N
            DebugTarget dt = currentDebugger.getNDI().getDebugTarget();
            NativeDebuggerInfo ndi = currentDebugger.getNDI();
            ndi.setConfiguration((Configuration) dt.getConfig());
            currentDebugger.postKill();
            startDebugger(Start.NEW, ndi);
        }
    }

    /** popup to let user set dbx option -xexec32 */
    /* 6306083 */
    public void handleExec32() {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout());
        panel.add(new JLabel(Catalog.get("EXEC32MSG")), // NOI18N
            java.awt.BorderLayout.NORTH);
        JCheckBox jc = new JCheckBox(Catalog.get("OPTION_EXEC32")); // NOI18N
        jc.getAccessibleContext().setAccessibleDescription(
            Catalog.get("ACSD_OPTION_EXEC32")); // NOI18N
        panel.add(jc, java.awt.BorderLayout.SOUTH);
        //jc.setSelected(DebuggerOption.OPTION_EXEC32.isEnabled(globalOptions()));
        jc.setSelected(DebuggerOption.OPTION_EXEC32.isEnabled(currentDebugger.optionLayers()));

        DialogDescriptor dlg = new DialogDescriptor(
            panel, // NOI18N
            Catalog.get("DebuggerError"), // NOI18N
            true,
            new Object[]{
                jc,
                DialogDescriptor.OK_OPTION
            },
            jc,
            DialogDescriptor.BOTTOM_ALIGN,
            null,
            null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dlg.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
        dlg.setClosingOptions(new Object[]{DialogDescriptor.OK_OPTION});
        dialog.setVisible(true);
        if (jc.isSelected()) {
            DebuggerOption.OPTION_EXEC32.setCurrValue(currentDebugger.optionLayers(), "on"); // NOI18N
            DebugTarget dt = currentDebugger.getNDI().getDebugTarget();
            NativeDebuggerInfo ndi = currentDebugger.getNDI();
            ndi.setConfiguration((Configuration) dt.getConfig());
            currentDebugger.postKill();
            startDebugger(Start.NEW, ndi);
        }
    }

    /**
     * The source file has been modified more recently than the executable
     * @return	true if a fix was requested
     */
    private boolean error_sourceModified(NativeDebugger origin, String msg) {
        origin.setSrcOODMessage(msg);
        return false;

    /* LATER
    JButton ignoreButton = new JButton();
    ignoreButton.setText(Catalog.get("SourceOODIgnore")); // NOI18N

    JButton fixButton = new JButton();
    fixButton.setText(Catalog.get("SourceOODFix")); // NOI18N

    Catalog.setAccessibleDescription(ignoreButton,
    "ACSD_SourceOODIgnore");// NOI18N
    Catalog.setAccessibleDescription(fixButton,
    "ACSD_SourceOODFix");	// NOI18N

    DialogDescriptor dlg = new DialogDescriptor(
    msg,
    Catalog.get("SourceOODTitle"), // NOI18N
    true,
    new JButton [] {
    ignoreButton,
    fixButton
    },
    ignoreButton,
    DialogDescriptor.DEFAULT_ALIGN,
    null,
    null);
    dlg.setMessageType(NotifyDescriptor.WARNING_MESSAGE);
    dlg.setClosingOptions(null);

    final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
    dialog.setVisible(true);

    Object pressedButton = dlg.getValue();
    if (pressedButton == fixButton) {
    return true;
    } else {
    return false;
    }
     */
    }

    /**
     * The user has attempted to load in a bad corefile. Handle it.
     */
    private void error_BadCore(String explanation, boolean canForce) {
        // Let user force load or select other

        JButton forceButton = new JButton();
        forceButton.setText(Catalog.get("CoreLoadAnyway")); // NOI18N
        Catalog.setAccessibleDescription(forceButton, "ACSD_CoreLoadAnyway");	// NOI18N
        forceButton.setMnemonic(Catalog.getMnemonic("MNEM_CoreLoadAnyway"));	// NOI18N

        Object[] options;
        Object def;
        if (canForce) {
            def = forceButton;
            options = new Object[]{
                    forceButton,
                    NotifyDescriptor.CANCEL_OPTION
                };
        } else {
            def = NotifyDescriptor.OK_OPTION;
            options = new Object[]{
                    NotifyDescriptor.OK_OPTION
                };
        }
        DialogDescriptor dlg = new DialogDescriptor(
            explanation,
            Catalog.get("CorefileError"), // NOI18N
            true,
            options,
            def,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        dlg.setMessageType(NotifyDescriptor.ERROR_MESSAGE);

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);

        Object pressedButton = dlg.getValue();
        if (pressedButton == forceButton) {
            // Reload anyway
	    /* LATER
            askToAttachCorefile(0, session, null, true);
             */
            // That method knows that if the corefile name is null, it
            // should use its previous argument
        } else {
            // Select different corefile
            //String exename = config.getProgram().getExecutableName(); // NOI18N
            //String corefilename = session.getCorefile();
            //((DebugCoreNodeAction)SystemAction.get(DebugCoreNodeAction.class)).performAction(exename, corefilename);
        }
    }

    /*
     * A resumption failed
     */
    private void error_runFailed(NativeDebugger originatingDebugger) {
        originatingDebugger.runFailed();
    }

    /**
     * Put up a dialog to display an error message originating from the engine.
     * There is a fair bit of smarts and heuristics in here.
     */
    public void error(int rt, final Error error, NativeDebugger originatingDebugger) {

        // XXX WORKAROUND for dbx problem - see bugid (bugid here)
        if (error.isRedundantPathmap()) {
            return;
        }

        /* LATER
        if (error.isCancelled()) {
        return;
        }
         */


        // LATER debugger.error(msg.toString());

        if (RoutingToken.BREAKPOINTS.isSameSubsystem(rt) &&
            error.maxSeverity() == Error.Severity.ERROR) {

            // Creating the breakpoint failed in dbx, Create a "broken"
            // breakpoint.

            if (originatingDebugger.bm().noteBreakpointError(rt, error)) {
                return;
            }

        // we're not restoring so treat this as a regular user failure
        // and continue with popping up an error dialog


        } else if (RoutingToken.WATCHES.isSameSubsystem(rt) &&
            error.maxSeverity() == Error.Severity.ERROR) {

            // Ibid as for bpts above.
            if (originatingDebugger.watchError(rt, error)) {
                return;
            }
        }


        // Act on specific error messages from dbx. These are symbolic
        // names for special error conditions such that I don't have to
        // parse (possibly localized) error messages meant for the user.

        boolean handled = false;
        if (error.errorToken() != null) {
            if (error.isBadcore()) {
                error_BadCore(Catalog.get("CoreNameMismatch"), // NOI18N
                    true);
                handled = true;

            } else if (error.isBadcoreOod()) {
                error_BadCore(Catalog.get("CoreOld"), true); // NOI18N
                handled = true;

            } else if (error.isBadcoreNoprog()) {
                error_BadCore(Catalog.get("CoreNameExtract"), // NOI18N
                    false);
                handled = true;

            } else if (error.isXExec32()) {
                // 6825221
                //not needed handleExec32();
                handled = false;

            } else if (error.isXExec64()) {
                // 6825221
                // not needed handleExec64();
                handled = false;

            } else if (error.isOodSrc()) {
                // Dbx only
                Host host = originatingDebugger.getHost();
                if (!isFixing() && host != null && !host.isLinux()) {
                    if (error_sourceModified(originatingDebugger, error.text())) {
                        setFixStatus(true);
                        currentDebugger().fix();
                    }
                }
                handled = true;

            } else if (error.isRunFailed()) {
                error_runFailed(originatingDebugger);
                handled = true;

            } else {
                assert false :
                    "Error cookie \"" + // NOI18N
                    error.errorToken() +
                    "\" not handled in Dbx.error"; // NOI18N
            }
        }


        if (!handled && (error.maxSeverity() == Error.Severity.ERROR)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    IpeUtils.postError(error.text());
                }
            });
        }

        refocusDialog();
    }

    /**
     * Code common to error() warning().
     */
    private static JComponent errorWarning(String msg) {
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea();
        Catalog.setAccessibleName(textArea,
            "ACSN_WarningArea");	// NOI18N
        Catalog.setAccessibleDescription(textArea,
            "ACSD_WarningArea");	// NOI18N
        textArea.setLineWrap(false);
        textArea.setEditable(false);

        textArea.setFont(UIManager.getFont("Label.font"));	// NOI18N
        textArea.setDisabledTextColor(UIManager.getColor("Label.foreground")); // NOI18N

        textArea.setColumns(60);
        textArea.setRows(7);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);
        Catalog.setAccessibleDescription(scrollPane, "ACSD_Warning");// NOI18N

        textArea.setText(msg);
        return scrollPane;
    }

    /**
     * Put up a dialog to display an error message originating in the gui as 
     * opposed to an engine.
     */
    public static void error(String msg) {
        NotifyDescriptor.Message descriptor =
            new NotifyDescriptor.Message(errorWarning(msg),
            NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     * Put up a dialog to display a warning message originating in the gui as
     * opposed to an engine.
     */
    public static void warning(String msg) {
        NotifyDescriptor.Message descriptor =
            new NotifyDescriptor.Message(errorWarning(msg),
            NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    /**
     * Cannot yet set breakpoints unless a program is loaded.
     */
    public static void errorLoadBeforeBpt() {
        String msg = Catalog.get("ERR_no_set_til_session");	// NOI18N
        NotifyDescriptor n =
            new NotifyDescriptor.Message(msg,
            NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(n);
    }

    /**
     * Put up a dialog to display a popup query originating from the engine.
     */
    public ItemSelectorResult popup(int rt, String cookie,
        NativeDebugger originatingDebugger,
        String title,
        int nitems, String item[],
        boolean cancelable,
        boolean multiple_selection) {

	return popupHelp(title, nitems, item, cancelable, multiple_selection);
    }

    public ItemSelectorResult popupHelp(String title,
        int nitems, String item[],
        boolean cancelable,
        boolean multiple_selection) {

        ItemSelectorDialog isd = new ItemSelectorDialog();
        isd.showWindow(title, nitems, item, cancelable, multiple_selection);
        return isd.getResult();
    }
    /*
     * Fix & Cont mgmt
     */
    private boolean isfixing = false;

    public void setFixStatus(boolean f) {
        isfixing = f;
    }

    public boolean isFixing() {
        return isfixing;
    }
    /*
     * Dialog mgmt
     */
    private DialogManager currentDialog;

    public void registerDialog(DialogManager dialog) {
        if (currentDialog == dialog) {
            return;
        }
        // TMP assert currentDialog == null;
        if (currentDialog != null) {
            DbgGuiModule.logger.log(Level.WARNING,
                "DebuggerManager.registerDialog(): " + // NOI18N
                "registering a dialog when previous one wasn't deregistered"); // NOI18N
        }
        currentDialog = dialog;
    }

    public void deRegisterDialog(DialogManager dialog) {
        if (currentDialog == null) {
            return;
        }
        assert currentDialog == dialog;
        currentDialog = null;
    }

    public void bringDownDialog() {
        if (currentDialog != null) {
            currentDialog.bringDown();
            deRegisterDialog(currentDialog);
        }
    }

    /*
     * After the error message is dismissed adjust any current dialogs focus
     */
    private void refocusDialog() {
        // SHOULD pass a clue from the error message regarding which
        // field/property had error in it so we can bring focus to that field.
        if (currentDialog != null) {
            currentDialog.refocus();
        }
    }

    /*
     * Put the given text into the status area
     */
    public void setStatusText(String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }

    /*
     * Format a message and put the resulting text into the status area
     */
    public String formatStatusText(String fmtKey, Object... args) {
        String msg = Catalog.format(fmtKey, args);
        setStatusText(msg);
        return msg;
    }
    private ProgressMonitor progressMonitor = null;

    /**
     * Show a progress monitor. Safe to call from any thread.
     *
     * @param message
     * The message to be shown. Only has an effect the first time the
     * progressMonitor is shown
     * @param note
     * The note shown below the message, or null to leave it blank
     * @param
     * progress The amount of progress we've made [0,100]. Set to -1 to leave it
     * unchanged.
     */
    public void updateProgress(final String message,
        final String note, final int progress) {

        if (SwingUtilities.isEventDispatchThread()) {
            updateProgressSameThread(message, note, progress);
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateProgressSameThread(message, note, progress);
                }
            });
        }
    }

    /**
     * Do the actual updateProgress work.
     * Assumes we're in a safe (read: AWT) thread.
     */
    private void updateProgressSameThread(final String message,
        final String note,
        final int progress) {

        StatusDisplayer.getDefault().setStatusText(message != null ? message : note);
        if (progressMonitor == null) {
            progressMonitor = new ProgressMonitor(
                null, /// XXX is this valid?
                (message != null) ? message : note,
                ((note != null) ? note : ""),
                //null,
                0, 100);
            progressMonitor.setMillisToPopup(0);
            progressMonitor.setMillisToDecideToPopup(0);
            if (progress != -1) {
                progressMonitor.setProgress(progress);
            } else {
                // Workaround for Swing bug
            }
        } else {
            if (progress != -1) {
                progressMonitor.setProgress(progress);
            }
            if (note != null) {
                progressMonitor.setNote(note);
            }
        }
    }

    /**
     * Cancel the progress indicator (if it's showing for this process).
     * Safe to call from any thread.
     */
    public void cancelProgress() {
        if (SwingUtilities.isEventDispatchThread()) {
            cancelProgressSameThread();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    cancelProgressSameThread();
                }
            });
        }
    }

    /**
     * Do the actual cancelProgress work. Assumes we're called from a
     * safe (read: AWT) thread.
     */
    private void cancelProgressSameThread() {
        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
        if (progressMonitor != null) {
            progressMonitor.close();
            progressMonitor = null;
        }
    }

    boolean isProgressCancelled() {
        if (progressMonitor == null) {
            return false;
        } else {
            return progressMonitor.isCanceled();
        }
    }

    /**
     * class to communicate follow fork dialog results
     */
    public static final class FollowForkInfo {
        public boolean parent;
        public boolean child;
        public boolean stopAfterFork;
    }

    private static boolean last_stopAfterFork = false;

    private static class FollowForkDialog {

        private NativeDebugger origin;
        private Dialog dialog;
        private DialogDescriptor dlg;
        private JButton parentButton;
        private JButton childButton;
        private JButton bothButton;
        private JButton stopButton;
        private JCheckBox jc;

        public FollowForkDialog(NativeDebugger origin, String name) {
            this.origin = origin;

            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());

            String msg = Catalog.get("ProcForkQuestion"); // NOI18N
            JTextArea textArea = new JTextArea(msg);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            textArea.setEnabled(false);
            textArea.setOpaque(false);
            textArea.setFont(UIManager.getFont("Label.font"));// NOI18N
            textArea.setDisabledTextColor(UIManager.getColor("Label.foreground")); // NOI18N
            panel.add(textArea, BorderLayout.CENTER);


            jc = new JCheckBox(Catalog.get("ProcForkStopAfter"));// NOI18N
            jc.setSelected(last_stopAfterFork);
            Catalog.setAccessibleDescription(jc,
                "ACSD_FollowStopAfter");			// NOI18N
            jc.setMnemonic(Catalog.getMnemonic("MNEM_FollowStopAfter"));	// NOI18N
            // LATER
            panel.add(jc, BorderLayout.SOUTH);

            parentButton = new JButton();
            parentButton.setText(Catalog.get("ProcForkParent"));// NOI18N
            Catalog.setAccessibleDescription(parentButton,
                "ACSD_FollowParent");				// NOI18N
            parentButton.setMnemonic(Catalog.getMnemonic("MNEM_FollowParent"));		// NOI18N

            childButton = new JButton();
            childButton.setText(Catalog.get("ProcForkChild"));	// NOI18N
            Catalog.setAccessibleDescription(childButton,
                "ACSD_FollowChild");				// NOI18N
            childButton.setMnemonic(Catalog.getMnemonic("MNEM_FollowChild"));		// NOI18N

            bothButton = new JButton();
            bothButton.setText(Catalog.get("ProcForkBoth"));	// NOI18N
            Catalog.setAccessibleDescription(bothButton,
                "ACSD_FollowBoth");				// NOI18N
            bothButton.setMnemonic(Catalog.getMnemonic("MNEM_FollowBoth"));		// NOI18N

            stopButton = new JButton();
            stopButton.setText(Catalog.get("ProcForkStop"));	// NOI18N
            Catalog.setAccessibleDescription(stopButton,
                "ACSD_FollowStop");				// NOI18N
            stopButton.setMnemonic(Catalog.getMnemonic("MNEM_FollowStop"));		// NOI18N

            Host host = origin.getHost();
            if (host != null && host.isLinux()) // PTrace do not support attaching child without detaching parent
            {
                bothButton.setEnabled(false);
            }

            String title = Catalog.format("TTL_ProcForkHeader", name); // NOI18N

            dlg = new DialogDescriptor(panel,
                title,
                true,
                new JButton[]{
                    parentButton,
                    childButton,
                    bothButton,
                    stopButton
                },
                parentButton,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

            dlg.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);

            /* LATER
            This doesn't work yet. See IZ 50960

            ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            Object pressedButton = event.getSource();
            System.out.println("FollowForkDialog action: "
            + event);
            }
            };
            dlg.setButtonListener(listener);
             */

            dialog = DialogDisplayer.getDefault().createDialog(dlg);
            Catalog.setAccessibleDescription(dialog,
                "ACSD_Followfork");	// NOI18N
        }

        public void show() {
            dialog.setVisible(true);

            FollowForkInfo ffi = getResult();
            origin.forkThisWay(ffi);
        }

        public FollowForkInfo getResult() {
            FollowForkInfo ffi = new FollowForkInfo();

            Object pressedButton = dlg.getValue();

            if (pressedButton == parentButton) {
                ffi.parent = true;
            } else if (pressedButton == childButton) {
                ffi.child = true;
            } else if (pressedButton == bothButton) {
                ffi.parent = true;
                ffi.child = true;
            } else if (pressedButton == stopButton) {
                // don't do anything
            } else {
                // The dialog window was closed/cancelled
                // don't do anything
            }

            if (jc.isSelected()) {
                ffi.stopAfterFork = true;
            }
            last_stopAfterFork = ffi.stopAfterFork;

            return ffi;
        }
    }

    public void aboutToFork(NativeDebugger debugger, String name) {
        FollowForkDialog d = new FollowForkDialog(debugger, name);
        d.show();
    }


    //
    // Option collections
    //
    private OptionSet globalOptions = null;

    public OptionSet globalOptions() {
        if (globalOptions == null) {
            globalOptions = new GlobalOptionSet();
        }

        // Read from disk if necessary so we always get up-to-date version
        globalOptions.open();

        return globalOptions;
    }

    public void applyGlobalOptions() {
        OptionSet options = globalOptions();
        for (NativeDebugger debugger : nativeDebuggers()) {
            OptionClient client = debugger.getOptionClient();
            if (client != null) {
                options.applyTo(client);
            }
        }
        options.doneApplying();
    }
    // 
    // the mode in which we start debugger in
    //
    private int action = 0;
    @SuppressWarnings("PointlessBitwiseExpression")
    public static final int RUN = (1 << 0);
    public static final int STEP = (1 << 1);
    public static final int ATTACH = (1 << 2);
    public static final int CORE = (1 << 3);
    public static final int LOAD = (1 << 4);
    public static final int CONNECT = (1 << 5);

    public void setAction(int i) {
        action |= i;
    }

    public void removeAction(int i) {
        action &= ~i;
    }

    public int getAction() {
        // could be "run" or "step" or "" after load program
        // would be refered in Dbx.prog_loaded
        return action;
    }

    //
    // Window opening
    //

    /**
     * Open Debugger console tab
     */
    public void enableConsoleWindow() {

        // Perhaps SHOULD disable the action to begin with?
        if (currentNativeDebugger() == null) {
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        enableConsoleWindow();
                    }
                });
		return;
	} else {
	    ConsoleTopComponent.findInstance().open();
	    if (DebuggerOption.FRONT_DBGWIN.isEnabled(globalOptions())) {
		ConsoleTopComponent.findInstance().requestActive();
	    }
	}
    }

    /** Open Process I/O tab */
    public void enablePioWindow() {

        // Perhaps SHOULD disable the action to begin with?
        if (currentNativeDebugger() == null) {
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
	    SwingUtilities.invokeLater(new Runnable() {
                @Override
		public void run() {
		    enablePioWindow();
		}
	    });
	    return;
	} else {
	    PioTopComponent.findInstance().open();
	    PioTopComponent.findInstance().requestActive();
	}
    }

    public void setIO(InputOutput io) {
        this.io = io;
    }

    public InputOutput getIO() {
        return io;
    }

    public static TopComponent openComponent(String viewName, boolean activate) {
        // SHOULD be EDT-safe like enableRtcWindow below?
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        view.open();
        if (activate) {
            view.requestActive();
        }
        return view;
    }
    public static boolean isComponentOpened(String viewName) {
        TopComponent view = WindowManager.getDefault().findTopComponent(viewName);
        if (view == null) {
            throw new IllegalArgumentException(viewName);
        }
        return view.isOpened();
    }
    
    public interface DebuggerStateListener {
        void notifyAttached(NativeDebugger debugger, long pid);
    }
    
    private final HashSet<DebuggerStateListener> debuggerStateListeners = new HashSet<DebuggerStateListener>();
    
    public void addDebuggerStateListener(DebuggerStateListener listener) {
        synchronized (debuggerStateListeners) {
            if (!debuggerStateListeners.contains(listener)) {
                debuggerStateListeners.add(listener);
            }
        }
    }
    
    public void removeDebuggerStateListener(DebuggerStateListener listener) {
        synchronized (debuggerStateListeners) {
            if (debuggerStateListeners.contains(listener)) {
                debuggerStateListeners.remove(listener);
            }
        }
    }
    
    public void notifyAttached(NativeDebugger debugger, long pid) {
        List<DebuggerStateListener> listenersCopy;
        synchronized(debuggerStateListeners) {
            listenersCopy = new ArrayList<DebuggerStateListener>(debuggerStateListeners);
        }
        for (DebuggerStateListener stateListener : listenersCopy) {
            stateListener.notifyAttached(debugger, pid);
        }
    }
    
    private static class NativeDebuggerManagerAccessorImpl extends NativeDebuggerManagerAccessor {

        @Override
        public void attach(DebugTarget dt, DbgActionHandler dah) {
            NativeDebuggerManager.get().attach(dt, dah);
        }
        
    }
    
}
