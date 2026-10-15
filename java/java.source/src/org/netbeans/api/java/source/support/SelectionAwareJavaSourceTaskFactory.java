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
package org.netbeans.api.java.source.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.JavaSourceTaskFactory;
import org.netbeans.modules.parsing.spi.TaskIndexingMode;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;

/**A {@link JavaSourceTaskFactory} that registers tasks to all files that are
 * opened in the editor and are visible. This factory also listens on the selection in
 * opened and visible JTextComponents and reschedules the tasks as necessary.
 *
 * The tasks may access current selection span using {@link #getLastSelection} method.
 *
 * @since 0.15
 * 
 * @author Jan Lahoda
 */
public abstract class SelectionAwareJavaSourceTaskFactory extends JavaSourceTaskFactory {
    
    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("SelectionAwareJavaSourceTaskFactory worker");
    
    private int timeout;
    private String[] supportedMimeTypes;
    
    /**Construct the SelectionAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public SelectionAwareJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String []) null);
    }
    
    /**Construct the SelectionAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run
     * @since 0.22
     */
    public SelectionAwareJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority, TaskIndexingMode.DISALLOWED_DURING_SCAN);
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }
    
    /**Construct the SelectionAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param taskIndexingMode the awareness of indexing. For tasks which can run
     * during indexing use {@link TaskIndexingMode#ALLOWED_DURING_SCAN} for tasks
     * which cannot run during indexing use {@link TaskIndexingMode#DISALLOWED_DURING_SCAN}.
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run,
     * empty array falls back to default text/x-java.
     * @since 0.94
     */
    public SelectionAwareJavaSourceTaskFactory(
            @NonNull final Phase phase,
            @NonNull final Priority priority,
            @NonNull final TaskIndexingMode taskIndexingMode,
            @NonNull String... supportedMimeTypes) {
        super(phase, priority, taskIndexingMode);
        Parameters.notNull("supportedMimeTypes", supportedMimeTypes);   //NOI18N
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
        this.supportedMimeTypes = supportedMimeTypes.length > 0 ? supportedMimeTypes.clone() : null;
    }
    
    /**{@inheritDoc}*/
    public List<FileObject> getFileObjects() {
        List<FileObject> files = OpenedEditors.filterSupportedMIMETypes(OpenedEditors.getDefault().getVisibleEditorsFiles(), supportedMimeTypes);

        return files;
    }

    private static Map<FileObject, Integer> file2SelectionStartPosition = new WeakHashMap<FileObject, Integer>();
    private static Map<FileObject, Integer> file2SelectionEndPosition = new WeakHashMap<FileObject, Integer>();
    
    /**Returns current selection span in current {@link JTextComponent} for a given file.
     *
     * @param file file from which the position should be found
     * @return selection span in the current {@link JTextComponent} for a given file.
     *         <code>null</code> if no selection available so far.
     */
    public static synchronized int[] getLastSelection(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Cannot pass null file!");
        }
        
        Integer startPosition = file2SelectionStartPosition.get(file);
        Integer endPosition = file2SelectionEndPosition.get(file);
        
        if (startPosition == null || endPosition == null) {
            //no position set yet:
            return null;
        }
        
        return new int[] {startPosition, endPosition};
    }
    
    private static synchronized void setLastSelection(FileObject file, int startPosition, int endPosition) {
        file2SelectionStartPosition.put(file, startPosition);
        file2SelectionEndPosition.put(file, endPosition);
    }
    
    /**
     * The listener is called from EditorRegistry under locked OpenedEditors instance in EDT, but it is
     * also called by Prop changes on DataObject in any thread. Selections must be read from components from
     * EDT, otherwise see issue #258581.
     */
    private class ChangeListenerImpl implements ChangeListener, Runnable {
        // @GuardedBy(this)
        private Map<JTextComponent, ComponentListener> component2Listener = new WeakHashMap<JTextComponent, SelectionAwareJavaSourceTaskFactory.ComponentListener>();
        // @GuardedBy(this) just carry-over between stateChanged and EDT
        private Set<JTextComponent>    updateSelection = new HashSet<>();
        
        public void run() {
            Collection<JTextComponent> update;
            
            synchronized (this) {
                update = updateSelection;
                updateSelection = new HashSet<>();
            }
            for (JTextComponent c : update) {
                int selStart, selEnd;
                Caret caret = c.getCaret();
                // possobly the caret is not yet installed ?
                if (caret != null) {
                    selStart = c.getSelectionStart();
                    selEnd = c.getSelectionEnd();
                } else {
                    selStart = selEnd = 0;
                }

                //TODO: are we in AWT Thread?:
                setLastSelection(OpenedEditors.getFileObject(c), selStart, selEnd);
            }
        }
            
        
        // called under locked OpenedEditors. Do not extend the lock. Lock ordering
        // is always the same.
        public synchronized void stateChanged(ChangeEvent e) {
            List<JTextComponent> visible = OpenedEditors.getDefault().getVisibleEditors();
            List<JTextComponent> added = new ArrayList<JTextComponent>(visible);
            List<JTextComponent> removed = new ArrayList<JTextComponent>(component2Listener.keySet());

            added.removeAll(component2Listener.keySet());
            removed.removeAll(visible);

            for (JTextComponent c : removed) {
                c.removeCaretListener(component2Listener.remove(c));
            }

            for (JTextComponent c : added) {
                ComponentListener l = new ComponentListener(c);

                c.addCaretListener(l);
                component2Listener.put(c, l);
            }
            
            if (added.isEmpty()) {
                return;
            }
            updateSelection.addAll(added);
            // invokelater even though we could be in EDT - do not extend OpenedEditors lock.
            SwingUtilities.invokeLater(this);
        }
    }
    
    private class ComponentListener implements CaretListener {
        
        private Reference<JTextComponent> componentRef;
        private final RequestProcessor.Task rescheduleTask;
        
        public ComponentListener(final JTextComponent component) {
            this.componentRef = new WeakReference<JTextComponent>(component);
            rescheduleTask = WORKER.create(new Runnable() {
                public void run() {
                    JTextComponent component = componentRef == null ? null : componentRef.get();
                    if (component == null) {
                        return;
                    }
                    FileObject file = OpenedEditors.getFileObject(component);
                    
                    if (file != null) {
                        reschedule(file);
                    }
                }
            });
        }
        
        public void caretUpdate(CaretEvent e) {
            JTextComponent component = componentRef == null ? null : componentRef.get();
            if (component == null) {
                return;
            }
            FileObject file = OpenedEditors.getFileObject(component);
            
            if (file != null) {
                setLastSelection(OpenedEditors.getFileObject(component), component.getSelectionStart(), component.getSelectionEnd());
                rescheduleTask.schedule(timeout);
            }
        }
        
    }
}
