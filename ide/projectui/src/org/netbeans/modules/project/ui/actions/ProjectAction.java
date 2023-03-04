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

import java.awt.Toolkit;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class ProjectAction extends LookupSensitiveAction implements ContextAwareAction {
    
    private String command;
    private ProjectActionPerformer performer;
    private final String namePattern;
    private final String popupPattern;
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public ProjectAction(String command, String namePattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, namePattern, icon, lookup );
    }

    public ProjectAction(String command, String namePattern, String popupPattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, popupPattern, icon, lookup );
    }
    
    public ProjectAction( ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, namePattern, icon, lookup );
    }

    private ProjectAction( ProjectActionPerformer performer, String namePattern, String popupPattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, popupPattern, icon, lookup );
    }
    
    @SuppressWarnings("LeakingThisInConstructor")
    private ProjectAction(String command, ProjectActionPerformer performer, String namePattern, String popupPattern, Icon icon, Lookup lookup) {
        super(icon, lookup, new Class<?>[] {Project.class, DataObject.class});
        this.command = command;
        if ( command != null ) {
            ShortcutManager.INSTANCE.registerAction(command, this);
        }
        this.performer = performer;
        this.namePattern = namePattern;
        this.popupPattern = popupPattern;
        String presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
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
    protected void actionPerformed( final Lookup context ) {
        Runnable r = new Runnable() {
            //the tricky part here is that context can change in the time between AWT and RP execution.
            //unfortunately the ActionProviders from project need the lookup to see if the command is supported.
            // that sort of renders the ActionUtils.mineData() method useless here. Unless we are able to create a mock lookup with only projects and files.
            @Override
            public void run() {
                final Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
                Runnable r2 = new Runnable() {
                    @Override
                    public void run() {
                        if (command != null && projects.length > 0) {
                            runSequentially(new LinkedList<Project>(Arrays.asList(projects)), ProjectAction.this, command);
                        } else if (performer != null && projects.length == 1) {
                            performer.perform(projects[0]);
                        }
                    }
                };
                //ActionProvider is supposed to run in awt
                if (SwingUtilities.isEventDispatchThread()) {
                    r2.run();
                } else {
                    SwingUtilities.invokeLater(r2);
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
    
    static void runSequentially(final Queue<Project> queue, final LookupSensitiveAction a, final String command) {
        Project p = queue.remove();
        final ActionProvider ap = p.getLookup().lookup(ActionProvider.class);
        if (ap == null) {
            return;
        }
        if (!Arrays.asList(ap.getSupportedActions()).contains(command)) {
            // #47160: was a supported command (e.g. on a freeform project) but was then removed.
            Utilities.disabledActionBeep();
            a.resultChanged(null);
            return;
        }
        LogRecord r = new LogRecord(Level.FINE, "PROJECT_ACTION"); // NOI18N
        r.setResourceBundle(NbBundle.getBundle(ProjectAction.class));
        r.setParameters(new Object[] {
            a.getClass().getName(),
            p.getClass().getName(),
            a.getValue(NAME)
        });
        r.setLoggerName(UILOG.getName());
        UILOG.log(r);
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override public void run() {
                final AtomicBoolean started = new AtomicBoolean();
                ap.invokeAction(command, Lookups.singleton(new ActionProgress() {
                    @Override protected void started() {
                        started.set(true);
                    }
                    @Override public void finished(boolean success) {
                        if (success && !queue.isEmpty()) { // OK, next...
                            runSequentially(queue, a, command);
                        } else { // stopping now; restore natural action enablement state
                            a.resultChanged(null);
                        }
                    }
                }));
                if (started.get()) {
                    a.setEnabled(false);
                } else if (!queue.isEmpty()) {
                    // Did not run action for some reason; try others?
                    runSequentially(queue, a, command);
                }
            }
        });
    }
    
    @Override
    protected void refresh(final Lookup context, final boolean immediate) {
        Runnable r = new Runnable() {
            @Override public void run() {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        final boolean enable;
        if ( command != null ) {
            enable = projects.length > 0;
        } else if ( performer != null && projects.length == 1 ) {
            enable = performer.enable(projects[0]);
        } else {
            enable = false;
        }
        
        final String presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        final String popupName;
        if (popupPattern != null) {
            popupName = ActionsUtil.formatProjectSensitiveName(popupPattern, projects);
        } else {
            popupName = null;
        }
        Mutex.EVENT.writeAccess(new Runnable() {
            @Override public void run() {
        setEnabled(enable);
        putValue("menuText", presenterName); // NOI18N
        putValue("popupText", popupName); // NOI18N
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
            }
        });
            }
        };
        if (immediate) {
            r.run();
        } else {
            RP.post(r);
        }
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    public @Override Action createContextAwareInstance(Lookup actionContext) {
        Action a = new ProjectAction(command, performer, namePattern, popupPattern, (Icon) getValue(SMALL_ICON), actionContext);
        a.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
        return a;
    }

}
