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

package org.netbeans.modules.project.ui.actions;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor.Task;

/** An action sensitive to selected node. Used for 1-off actions
 */
public final class FileAction extends LookupSensitiveAction implements ContextAwareAction {
    private String command;
    private FileActionPerformer performer;
    private final String namePattern;

    private final Task refreshTask = createRefreshTask(); // #250349
    private Lookup refreshContext; // Context for which refresh is scheduled.

    private static final Logger LOG = Logger.getLogger(FileAction.class.getName());
    
    public FileAction(String command, String namePattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, icon, lookup );
    }
    
    public FileAction( FileActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, icon, lookup );
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    private FileAction(String command, FileActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        super(icon, lookup, new Class<?>[] {Project.class, DataObject.class});
        
        assert (command != null || performer != null) && !(command != null && performer != null); // exactly one of the arguments must be provided
        
        this.command = command;
        if ( command != null ) {
            ShortcutManager.INSTANCE.registerAction(command, this);
        }
        this.performer = performer;
        this.namePattern = namePattern;

        String presenterName = ActionsUtil.formatName( namePattern, 0, "" );
        setDisplayName( presenterName );
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    public final @Override void putValue( String key, Object value ) {
        super.putValue( key, value );
        
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ShortcutManager.INSTANCE.registerShortcut(command, value);
        }   
    }
    
    @Override
    protected void refresh(final Lookup context, final boolean immediate) {
        if (immediate) {
            refreshImpl(context);
        } else {
            synchronized (this) {
                refreshContext = context;
            }
            refreshTask.schedule(10);
        }
    }

    /**
     * Implementation of the refresh operation. It can be called synchronously
     * (directly from {@link #refresh(Lookup, boolean)}) or asynchronously (from
     * the {@link #refreshTask}).
     *
     * @param context The current context.
     */
    private void refreshImpl(final Lookup context) {
        final boolean enable;
        final String presenterName;
        if (command != null) {

            Project[] projects = ActionsUtil.getProjectsFromLookup(context, command);
            // XXX #64991: handle >1 project (tricky since must pass subset of selection to each)
            if (projects.length != 1) {
                if (projects.length == 0 && globalProvider(context) != null) {
                    enable = true;
                    Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
                    presenterName = ActionsUtil.formatName(namePattern, files.size(),
                            files.isEmpty() ? "" : files.iterator().next().getPrimaryFile().getNameExt()); // NOI18N

                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.log(Level.FINER, "Enabling [{0}, {1}] for {2}. (no projects in lookup)", new Object[]{presenterName, enable, files.isEmpty() ? "no files" : files.iterator().next().getPrimaryFile()}); // NOI18N
                    }
                } else {
                    enable = false; // Zero or more than one projects found or command not supported
                    presenterName = ActionsUtil.formatName(namePattern, 0, "");

                    if (LOG.isLoggable(Level.FINER)) {
                        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
                        LOG.log(Level.FINER, "Enabling [{0}, {1}] for {2}. (projects > 1 in lookup)", new Object[]{presenterName, enable, files.isEmpty() ? "no files" : files.iterator().next().getPrimaryFile()}); // NOI18N
                    }
                }
            } else {
                FileObject[] files = ActionsUtil.getFilesFromLookup(context, projects[0]);
                enable = true;
                presenterName = ActionsUtil.formatName(namePattern, files.length, files.length > 0 ? files[0].getNameExt() : ""); // NOI18N

                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Enabling [{0}, {1}] for {2}. (one project in lookup)", new Object[]{presenterName, enable, files.length == 0 ? "no files" : files[0]}); // NOI18N
                }
            }
        } else if (performer != null) {

            Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
            if (dobjs.size() == 1) {
                FileObject f = dobjs.iterator().next().getPrimaryFile();

                enable = performer.enable(f);
                presenterName = ActionsUtil.formatName(namePattern, 1, f.getNameExt());

                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Enabling [{0}, {1}] for {2}.", new Object[]{presenterName, enable, f}); // NOI18N
                }

            } else {
                enable = false;
                presenterName = ActionsUtil.formatName(namePattern, 0, ""); // NOI18N

                if (LOG.isLoggable(Level.FINER)) {
                    LOG.log(Level.FINER, "Enabling [{0}, {1}]. (no dataobjects)", new Object[]{presenterName, enable}); // NOI18N
                }
            }
        } else {
            return;
        }
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override
            public void run() {
                putValue("menuText", presenterName);
                putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
                setEnabled(enable);
            }
        });
    }

    /**
     * Create task for refreshing of the action. The refresh will be invoked for
     * the last known context (passed to {@link #refresh(Lookup, boolean)}).
     *
     * @return The task.
     */
    private Task createRefreshTask() {
        return RP.create(new Runnable() {

            @Override
            public void run() {
                Lookup l;
                synchronized (FileAction.this) {
                    l = refreshContext;
                }
                if (l != null) {
                    refreshImpl(l);
                }
                synchronized (FileAction.this) {
                    //check the last context is the one that was just used
                    if (refreshContext == l) {
                        refreshContext = null;
                    }
                }
            }
        });
    }

    @Override
    protected void actionPerformed( final Lookup context ) {
        Runnable r = new Runnable() {
            //the tricky part here is that context can change in the time between AWT and RP execution.
            //unfortunately the ActionProviders from project need the lookup to see if the command is supported.
            // that sort of renders the ActionUtils.mineData() method useless here. Unless we are able to create a mock lookup with only projects and files.
            @Override
            public void run() {
        
        if (command != null) {
            final Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
            Runnable r2 = new Runnable() {

                @Override
                public void run() {
                    if ( projects.length == 1 ) {            
                        ActionProvider ap = projects[0].getLookup().lookup(ActionProvider.class);
                        ap.invokeAction( command, context );
                        return;
                    }

                    ActionProvider provider = globalProvider(context);
                    if (provider != null) {
                        provider.invokeAction(command, context);
                    }
                }
            };
            //ActionProvider is supposed to run in awt
            if (SwingUtilities.isEventDispatchThread()) {
                r2.run();
            } else {
                SwingUtilities.invokeLater(r2);
            }            
        } else if (performer != null) {
            Collection<? extends DataObject> dobjs = context.lookupAll(DataObject.class);
            if (dobjs.size() == 1) {
                performer.perform(dobjs.iterator().next().getPrimaryFile());
            }
        }
                   }
        };
        //no clear way of waiting for RP finishing the task, a lot of tests rely on sync execution.
        if (Boolean.getBoolean("sync.project.execution")) {
            r.run();
        } else {
            RP.post(r);
        }

    }

    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new FileAction( command, performer, namePattern, (Icon)getValue( SMALL_ICON ), actionContext );
    }

    private ActionProvider globalProvider(Lookup context) {
        for (ActionProvider ap : Lookup.getDefault().lookupAll(ActionProvider.class)) {
            if (Arrays.asList(ap.getSupportedActions()).contains(command) && ap.isActionEnabled(command, context)) {
                return ap;
            }
        }
        return null;
    }

}
