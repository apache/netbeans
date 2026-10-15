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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.openide.awt.AcceleratorBinding;
import org.openide.awt.Actions;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Keymap.class)
public final class NbKeymap implements Keymap, Comparator<KeyStroke> {

    private static final RequestProcessor RP = new RequestProcessor(NbKeymap.class);
    
    /**
     * Extension, which indicates that the given binding should be removed by keymap
     * profile. The marker is ignored in the 'Shortcuts' base directory.
     */
    public static final String BINDING_REMOVED = "removed";  // NO18N
    
    /**
     * Extension of the DataShadow files; private in loaders API
     */
    public static final String SHADOW_EXT = "shadow"; // NOI18N
    
    //for unit testing only
    private RequestProcessor.Task refreshTask;

    private static final Action BROKEN = new AbstractAction("<broken>") { // NOI18N
        public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().beep();
        }
    };

    /** Represents a binding of a keystroke. */
    private static class Binding {
        /** file defining an action; null if nested is not null */
        final FileObject actionDefinition;
        /** lazily instantiated actual action, in case actionDefinition is not null */
        private Action action;
        /** nested bindings; null if actionDefinition is not null */
        final Map<KeyStroke,Binding> nested;
        Binding(FileObject def) {
            actionDefinition = def;
            nested = null;
        }
        Binding() {
            actionDefinition = null;
            nested = new HashMap<KeyStroke,Binding>();
        }
        synchronized Action loadAction() {
            assert actionDefinition != null;
            if (action == null) {
                try {
                    DataObject d = DataObject.find(actionDefinition);
                    InstanceCookie ic = d.getLookup().lookup(InstanceCookie.class);
                    if (ic == null) {
                        return null;
                    }
                    action = (Action) ic.instanceCreate();
                } catch (/*ClassNotFoundException,IOException,ClassCastException*/Exception x) {
                    LOG.log(Level.INFO, "could not load action for " + actionDefinition.getPath(), x);
                }
            }
            if (action == null) {
                action = BROKEN;
            }
            return action;
        }
    }

    private Map<KeyStroke,Binding> bindings;
    private Map<String,KeyStroke> id2Stroke;
    private final Map<Action,String> action2Id = new WeakHashMap<Action,String>();
    private FileChangeListener keymapListener;
    private FileChangeListener bindingsListener = new FileChangeAdapter() {
        public @Override void fileDataCreated(FileEvent fe) {
            refreshBindings();
        }
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {
            refreshBindings();
        }
        public @Override void fileChanged(FileEvent fe) {
            refreshBindings();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            refreshBindings();
        }
        public @Override void fileDeleted(FileEvent fe) {
            refreshBindings();
        }
    };

    private void refreshBindings() {
        refreshTask = RP.post(new Runnable() {
            @Override
            public void run() {
                doRefreshBindings();
            }
        });
    }

    private synchronized void doRefreshBindings() {
        bindings = null;
        bindings();
    }

    //for unit testing only
    boolean waitFinished() throws InterruptedException {
        return refreshTask != null ? refreshTask.waitFinished(9999) : false;
    }

    private synchronized Map<KeyStroke,Binding> bindings() {
        if (bindings == null) {
            bindings = new HashMap<KeyStroke,Binding>();
            boolean refresh = id2Stroke != null;
            id2Stroke = new TreeMap<String,KeyStroke>();
            List<FileObject> dirs = new ArrayList<FileObject>(2);
            dirs.add(FileUtil.getConfigFile("Shortcuts")); // NOI18N
            FileObject keymaps = FileUtil.getConfigFile("Keymaps"); // NOI18N
            if (keymaps != null) {
                String curr = (String) keymaps.getAttribute("currentKeymap"); // NOI18N
                if (curr == null) {
                    curr = "NetBeans"; // NOI18N
                }
                dirs.add(keymaps.getFileObject(curr));
                if (keymapListener == null) {
                    keymapListener = new FileChangeAdapter() {
                        public @Override void fileAttributeChanged(FileAttributeEvent fe) {
                            refreshBindings();
                        }
                    };
                    keymaps.addFileChangeListener(keymapListener);
                }
            }
            Map<String,FileObject> id2Dir = new HashMap<String,FileObject>(); // #170677
            boolean processingProfile = false;

            // #217497: as translation String > KeyStroke[] is not unique, we must process .removed based on String 
            // externalized keystroke. Note that LHM will retain iteration order the same as was originally processing
            // order of the folders, so replacing same KeyStroke[] with different externalization will still work.
            Map<String, FileObject> activeShortcuts = new LinkedHashMap<String, FileObject>();
            for (FileObject dir : dirs) {
                if (dir != null) {
                    for (FileObject def : dir.getChildren()) {
                       if (def.isData()) {
                           boolean removed = processingProfile && BINDING_REMOVED.equals(def.getExt());
                           String fn = def.getName().toUpperCase();
                           
                           if (removed) {
                               activeShortcuts.remove(fn);
                           } else {
                               activeShortcuts.put(fn, def);
                           }
                       } 
                    }
                    dir.removeFileChangeListener(bindingsListener);
                    dir.addFileChangeListener(bindingsListener);
                }
                // the 1st iteration is Shortcuts/ the next are profiles
                processingProfile = true;
            }
            
            outer: for (FileObject def : activeShortcuts.values()) {
                FileObject dir = def.getParent();
                if (def.isData()) {
                    KeyStroke[] strokes = Utilities.stringToKeys(def.getName());
                    if (strokes == null || strokes.length == 0) {
                        LOG.log(Level.WARNING, "could not load parse name of " + def.getPath());
                        continue;
                    }
                    Map<KeyStroke,Binding> binder = bindings;
                    for (int i = 0; i < strokes.length - 1; i++) {
                        Binding sub = binder.get(strokes[i]);
                        if (sub != null && sub.nested == null) {
                            LOG.log(Level.WARNING, "conflict between " + sub.actionDefinition.getPath() + " and " + def.getPath());
                            sub = null;
                        }
                        if (sub == null) {
                            binder.put(strokes[i], sub = new Binding());
                        }
                        binder = sub.nested;
                    }

                    // XXX warn about conflicts here too:
                    binder.put(strokes[strokes.length - 1], new Binding(def));
                    if (strokes.length == 1) {
                        String id = idForFile(def);
                        KeyStroke former = id2Dir.put(id, dir) == dir ? id2Stroke.get(id) : null;
                        if (former == null || compare(former, strokes[0]) > 0) {
                            id2Stroke.put(id, strokes[0]);
                        }
                    }
                }
            }
            if (refresh) {
                // Update accelerators of existing actions after switching keymap.
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        synchronized( action2Id ) {
                            for (Map.Entry<Action, String> entry : action2Id.entrySet()) {
                                entry.getKey().putValue(Action.ACCELERATOR_KEY, id2Stroke.get(entry.getValue()));
                            }
                        }
                    }
                });
            }
            if (LOG.isLoggable(Level.FINE)) {
                for (Map.Entry<String,KeyStroke> entry : id2Stroke.entrySet()) {
                    LOG.fine(entry.getValue() + " => " + entry.getKey());
                }
            }
        }
        return bindings;
    }
    
    private static final List<KeyStroke> context = new ArrayList<KeyStroke>(); // accessed reflectively from org.netbeans.editor.MultiKeymap
    
    private static void resetContext() {
        context.clear();
        StatusDisplayer.getDefault().setStatusText("");
    }

    public static KeyStroke[] getContext() { // called from ShortcutAndMenuKeyEventProcessor
        return context.toArray(new KeyStroke[0]);
    }

    private static void shiftContext(KeyStroke stroke) {
        context.add(stroke);

        StringBuilder text = new StringBuilder();
        for (KeyStroke ks: context) {
            text.append(Actions.keyStrokeToString(ks)).append(' ');
        }
        StatusDisplayer.getDefault().setStatusText(text.toString());        
    }
           
    private static final Logger LOG = Logger.getLogger(NbKeymap.class.getName());
    
    public NbKeymap() {
        context.clear(); // may be useful in unit testing
    }

    public Action getDefaultAction() {
        return null;
    }

    public void setDefaultAction(Action a) {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        return "Default"; // NOI18N
    }

    public Action getAction(final KeyStroke key) {
        switch (key.getKeyCode()) {
        case KeyEvent.VK_SHIFT:
        case KeyEvent.VK_CONTROL:
        case KeyEvent.VK_ALT:
        case KeyEvent.VK_ALT_GRAPH:
        case KeyEvent.VK_META:
        case KeyEvent.VK_UNDEFINED:
        case KeyEvent.CHAR_UNDEFINED:
                // Not actually a bindable key press.
                return null;
        }
        if (key.isOnKeyRelease()) {
            // Again, not really our business here.
            return null;
        }
        LOG.log(Level.FINE, "getAction {0}", key);
        Map<KeyStroke,Binding> binder = bindings();
        for (KeyStroke ctx : context) {
            Binding sub = binder.get(ctx);
            if (sub == null) {
                resetContext();
                return BROKEN; // no entry found after known prefix
            }
            binder = sub.nested;
            if (binder == null) {
                resetContext();
                return BROKEN; // anomalous, expected to find submap here
            }
        }
        Binding b = binder.get(key);
        if (b == null) {
            resetContext();
            return null; // normal, not found
        }
        if (b.nested == null) {
            resetContext();
            return b.loadAction(); // found real action
        } else {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    shiftContext(key); // entering submap
                }
            };
        }
    }

    public KeyStroke[] getBoundKeyStrokes() {
        assert false;
        return null;
    }

    public Action[] getBoundActions() {
        assert false;
        return null;
    }

    public KeyStroke[] getKeyStrokesForAction(Action a) {
        return new KeyStroke[0];
    }

    KeyStroke keyStrokeForAction(Action a, FileObject definingFile) {
        String id = idForFile(definingFile);
        bindings();
        synchronized( action2Id ) {
            action2Id.put(a, id);
            KeyStroke k = id2Stroke.get(id);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "found keystroke {0} for {1} with ID {2}", new Object[] {k, id(a), id});
            }
            return k;
        }
    }

    @ServiceProvider(service=AcceleratorBinding.class)
    public static final class AcceleratorBindingImpl extends AcceleratorBinding {
        protected @Override KeyStroke keyStrokeForAction(Action action, FileObject definingFile) {
            Keymap km = Lookup.getDefault().lookup(Keymap.class);
            if (km instanceof NbKeymap) {
                return ((NbKeymap) km).keyStrokeForAction(action, definingFile);
            } else {
                LOG.log(Level.WARNING, "unexpected keymap: {0}", km);
                return null;
            }
        }
    }

    /**
     * Traverses shadow files to origin.
     * Returns impl class name if that is obvious (common for SystemAction's);
     * else just returns file path (usual for more modern registrations).
     */
    private static String idForFile(FileObject f) {
        if (f.hasExt(SHADOW_EXT)) {
            String path = (String) f.getAttribute("originalFile");
            if (path != null && f.getSize() == 0) {
                f = FileUtil.getConfigFile(path);
                if (f == null) {
                    return path; // #169887: some race condition with layer init?
                }
            } else {
                try {
                    DataObject d = DataObject.find(f);
                    if (d instanceof DataShadow) {
                        f = ((DataShadow) d).getOriginal().getPrimaryFile();
                    }
                } catch (DataObjectNotFoundException x) {
                    LOG.log(Level.FINE, f.getPath(), x);
                }
            }
        }
        // Cannot actually load instanceCreate methodvalue=... attribute; just want to see if it is there.
        if (f.hasExt("instance") && !Collections.list(f.getAttributes()).contains("instanceCreate")) {
            String clazz = (String) f.getAttribute("instanceClass");
            if (clazz != null) {
                return clazz;
            } else {
                return f.getName().replace('-', '.');
            }
        }
        return f.getPath();
    }

    public synchronized boolean isLocallyDefined(KeyStroke key) {
        assert false;
        return false;
    }

    public int compare(KeyStroke k1, KeyStroke k2) {
        //#47024 and 32733 - "Find" should not be shown as an accelerator,
        //nor should "Backspace" for Delete.  Solution:  The shorter text wins.
        return KeyEvent.getKeyText(k1.getKeyCode()).length() - 
            KeyEvent.getKeyText(k2.getKeyCode()).length();
    }
    
    
    public void addActionForKeyStroke(KeyStroke key, Action a) {
        assert false;
    }

    public void removeKeyStrokeBinding(KeyStroke key) {
        assert false;
    }

    public void removeBindings() {
        assert false;
    }

    public Keymap getResolveParent() {
        return null;
    }

    public void setResolveParent(Keymap parent) {
        throw new UnsupportedOperationException();
    }

    private static Object id(Action a) {
        if (a instanceof SystemAction) {
            return a.getClass();
        }
        return a;
    }
    
}
