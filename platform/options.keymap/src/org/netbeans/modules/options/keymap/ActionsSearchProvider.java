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


package org.netbeans.modules.options.keymap;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.TextAction;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


    
/**
 * SearchProvider for all actions. 
 * @author  Jan Becicka, Dafe Simonek
 */
public class ActionsSearchProvider implements SearchProvider {

    private volatile SearchRequest currentRequest;

    /**
     * Iterates through all found KeymapManagers and their sets of actions
     * and fills response object with proper actions that are enabled
     * and can be run meaningfully on current actions context.
     */
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        currentRequest = request;
        final Map<Object, String> duplicateCheck = new HashMap<Object, String>();
        final List<ActionInfo> possibleResults = new ArrayList<ActionInfo>(7);
        // iterate over all found KeymapManagers
        
        for (final KeymapManager m : Lookup.getDefault().lookupAll(KeymapManager.class)) {
            final Object[] ret = new Object[2];
            KeymapModel.waitFinished(new Runnable() {
                public void run() {
                    ret[0] = m.getKeymap(m.getCurrentProfile());
                    ret[1] = m.getActions().entrySet();
                }
            });
            Map<ShortcutAction, Set<String>> curKeymap = (Map<ShortcutAction, Set<String>>)ret[0];
            Set<Entry<String, Set<ShortcutAction>>> entrySet = (Set<Entry<String, Set<ShortcutAction>>>)ret[1];
            for (Entry<String, Set<ShortcutAction>> entry : entrySet) {
                for (ShortcutAction sa : entry.getValue()) {
                    if (currentRequest!=request) {
                        return;
                    }

                    // check action and obtain only meaningful ones
                    ActionInfo actInfo = getActionInfo(sa, curKeymap.get(sa),
                            entry.getKey());
                    if (actInfo == null) {
                        continue;
                    }
                    if (!doEvaluation(sa.getDisplayName(), request, actInfo, response, possibleResults, duplicateCheck)) {
                        return;
                    }
                }
            }
        }
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    // try also actions of activated nodes
                    Node[] actNodes = TopComponent.getRegistry().getActivatedNodes();
                    for (int i = 0; i < actNodes.length; i++) {
                        Action[] acts = actNodes[i].getActions(false);
                        for (int j = 0; j < acts.length; j++) {
                            if (currentRequest!=request) {
                                return;
                            }
                            Action action = checkNodeAction(acts[j]);
                            if (action == null) {
                                continue;
                            }
                            ActionInfo actInfo = new ActionInfo(action, null, null, null);
                            Object name = action.getValue(Action.NAME);
                            if (!(name instanceof String)) {
                                // skip action without proper name
                                continue;
                            }
                            String displayName = ((String) name).replaceFirst("&(?! )", ""); //NOI18N
                            if (!doEvaluation(displayName, request, actInfo, response, possibleResults, duplicateCheck)) {
                                return;
                            }
                        }
                    }
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }


        // add results stored above, actions that contain typed text, but not as prefix
        for (ActionInfo actInfo : possibleResults) {
            if (currentRequest != request) {
                return;
            }
            if (!addAction(actInfo, response, duplicateCheck)) {
                return;
            }
        }
    }
    

    private boolean addAction(ActionInfo actInfo, SearchResponse response, Map<Object, String> duplicateCheck) {
        KeyStroke stroke = null;
        // obtaining shortcut, first try Keymaps
        Set<String> shortcuts = actInfo.getShortcuts();
        if (shortcuts != null && shortcuts.size() > 0) {
            String shortcut = shortcuts.iterator().next();
            stroke = Utilities.stringToKey(shortcut);
        }
        // try accelerator key property if Keymaps returned no shortcut
        Action action = actInfo.getAction();
        if (stroke == null) {
            Object shortcut = action.getValue(Action.ACCELERATOR_KEY);
            if (shortcut instanceof KeyStroke) {
                stroke = (KeyStroke)shortcut;
            }
        }
        
        /* uncomment if needed
         Object desc = ((Action) actAndEvent[0]).getValue(Action.SHORT_DESCRIPTION);
        String sDesc = null;
        if (sDesc instanceof String) {
            sDesc = (String) desc;
        }*/
        
        String displayName = null;
        ShortcutAction sa = actInfo.getShortcutAction();
        if (sa != null) {
            displayName = sa.getDisplayName();
        } else {
            Object name = action.getValue(Action.NAME);
            if (name instanceof String) {
                displayName = ((String) name).replaceFirst("&(?! )", "");  //NOI18N
            }
        }
        if (actInfo.getCategory() != null && !actInfo.getCategory().isEmpty()
                && !actInfo.getCategory().equals(displayName)) {
            displayName += " (" + actInfo.getCategory() + ")";          //NOI18N
        }

        // #140580 - check for duplicate actions
        if (duplicateCheck.put(action, displayName) != null) {
            return true;
        }
         return response.addResult(new ActionResult(action), displayName, null,
                Collections.singletonList(stroke));
    }

    private boolean doEvaluation(String name, SearchRequest request,
            ActionInfo actInfo, SearchResponse response, List<ActionInfo> possibleResults, Map<Object, String> duplicateCheck) {
        int index = name.toLowerCase().indexOf(request.getText().toLowerCase());
        if (index == 0) {
            return addAction(actInfo, response, duplicateCheck);
        } else if (index != -1) {
            // typed text is contained in action name, but not as prefix,
            // store such actions if there are not enough "prefix" actions
            possibleResults.add(actInfo);
        }
        return true;
    }
    
    private ActionInfo getActionInfo(final ShortcutAction sa,
            final Set<String> shortcuts, final String category) {
        final ActionInfo[] result = new ActionInfo[1];
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    Class<?> clazz = sa.getClass();
                    Field f = null;
                    try {
                        f = clazz.getDeclaredField("action");
                        f.setAccessible(true);
                        Action action = (Action) f.get(sa);
                        if (!action.isEnabled()) {
                            return;
                        }
                        result[0] = new ActionInfo(action, sa, shortcuts, category);
                        return;
                    } catch (Throwable thr) {
                        if (thr instanceof ThreadDeath) {
                            throw (ThreadDeath) thr;
                        } // complain
                        Logger.getLogger(getClass().getName()).log(Level.FINE, "Some problem getting action " + sa.getDisplayName(), thr);
                    }
                    return;
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result[0];
    }
    
    
    private static ActionEvent createActionEvent (Action action) {
        Object evSource = null;
        int evId = ActionEvent.ACTION_PERFORMED;

        // text (editor) actions
        if (action instanceof TextAction) {
            EditorCookie ec = Utilities.actionsGlobalContext().lookup(EditorCookie.class);
            if (ec == null) {
                return null;
            }

            JEditorPane[] editorPanes = ec.getOpenedPanes();
            if (editorPanes == null || editorPanes.length <= 0) {
                return null;
            }
            evSource = editorPanes[0];
        }

        if (evSource == null) {
            evSource = TopComponent.getRegistry().getActivated();
        }
        if (evSource == null) {
            evSource = WindowManager.getDefault().getMainWindow();
        }

        
        return new ActionEvent(evSource, evId, null);
    }
    
    private Action checkNodeAction (Action action) {
        if (action == null) {
            return null;
        }
        try {
            if (action.isEnabled()) {
                return action;
            }
        } catch (Throwable thr) {
            if (thr instanceof ThreadDeath) {
                throw (ThreadDeath)thr;
            }
            // just log problems, it is common that some actions may complain
            Logger.getLogger(getClass().getName()).log(Level.FINE,
                    "Problem asking isEnabled on action " + action, thr);
        }
        return null;
    }
    
    private static class ActionResult implements Runnable {
        /** UI logger to notify about invocation of an action */
        private static Logger UILOG = Logger.getLogger("org.netbeans.ui.actions"); // NOI18N
        private Action command;

        public ActionResult(Action command) {
            this.command = command;
        }
        
        public void run() {
            // be careful, some actions throws assertions etc, because they
            // are not written to be invoked directly
            try {
                Action a = command;
                ActionEvent ae = createActionEvent(command);
                Object p = ae.getSource();
                if (p instanceof CloneableEditor) {
                    JEditorPane pane = ((CloneableEditor) p).getEditorPane();
                    Action activeCommand = pane.getActionMap().get(command.getValue(Action.NAME));
                    if (activeCommand != null) {
                        a = activeCommand;
                    }
                }

                a.actionPerformed(ae);
                uiLog(true);
            } catch (Throwable thr) {
                uiLog(false);
                if (thr instanceof ThreadDeath) {
                    throw (ThreadDeath)thr;
                }
                Object name = command.getValue(Action.NAME);
                String displayName = "";
                if (name instanceof String) {
                    displayName = (String)name;
                }
                
                Logger.getLogger(getClass().getName()).log(Level.FINE, 
                        displayName + " action can not be invoked.", thr);
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                        getClass(), "MSG_ActionFailure", displayName));
            }
        }

        private void uiLog(boolean success) {
            LogRecord rec = new LogRecord(Level.FINER, success?"LOG_QUICKSEARCH_ACTION":"LOG_QUICKSEARCH_ACTION_FAILED"); // NOI18N
            rec.setParameters(new Object[] { command.getClass().getName(), command.getValue(Action.NAME) });
            rec.setResourceBundle(NbBundle.getBundle(ActionsSearchProvider.class));
            rec.setResourceBundleName(ActionsSearchProvider.class.getPackage().getName() + ".Bundle"); // NOI18N
            rec.setLoggerName(UILOG.getName());
            UILOG.log(rec);
        }
    }

    private static class ActionInfo {

        private Action action;
        private ShortcutAction shortcutAction = null;
        private Set<String> shortcuts = null;
        private String category = null;

        public ActionInfo(Action action, ShortcutAction shortcutAction,
                Set<String> shortcuts, String category) {
            this.action = action;
            this.shortcutAction = shortcutAction;
            this.shortcuts = shortcuts;
            this.category = category;
        }

        public Action getAction() {
            return action;
        }

        public ShortcutAction getShortcutAction() {
            return shortcutAction;
        }

        public Set<String> getShortcuts() {
            return shortcuts;
        }

        public String getCategory() {
            return category;
        }
    }
}
