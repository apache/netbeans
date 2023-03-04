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
package org.netbeans.api.java.source.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

/**A {@link JavaSourceTaskFactorySupport} that registers tasks to all files that are
 * opened in the editor and are visible. This factory also listens on the caret on
 * opened and visible JTextComponents and reschedules the tasks as necessary.
 *
 * The tasks may access current caret position using {@link #getLastPosition} method.
 * 
 * @author Jan Lahoda
 */
public abstract class CaretAwareJavaSourceTaskFactory extends JavaSourceTaskFactory {
    
    private static final int DEFAULT_RESCHEDULE_TIMEOUT = 300;
    private static final RequestProcessor WORKER = new RequestProcessor("CaretAwareJavaSourceTaskFactory worker");
    
    private int timeout;
    private String[] supportedMimeTypes;
    
    /**Construct the CaretAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     */
    public CaretAwareJavaSourceTaskFactory(Phase phase, Priority priority) {
        this(phase, priority, (String[]) null);
    }
    
    /**Construct the CaretAwareJavaSourceTaskFactory with given {@link Phase} and {@link Priority}.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run
     * @since 0.21
     */
    public CaretAwareJavaSourceTaskFactory(Phase phase, Priority priority, String... supportedMimeTypes) {
        super(phase, priority, TaskIndexingMode.DISALLOWED_DURING_SCAN);
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
        this.supportedMimeTypes = supportedMimeTypes != null ? supportedMimeTypes.clone() : null;
    }
    
    /**Construct the CaretAwareJavaSourceTaskFactory.
     *
     * @param phase phase to use for tasks created by {@link #createTask}
     * @param priority priority to use for tasks created by {@link #createTask}
     * @param taskIndexingMode the awareness of indexing. For tasks which can run
     * during indexing use {@link TaskIndexingMode#ALLOWED_DURING_SCAN} for tasks
     * which cannot run during indexing use {@link TaskIndexingMode#DISALLOWED_DURING_SCAN}.
     * @param supportedMimeTypes a list of mime types on which the tasks created by this factory should be run,
     * empty array falls back to default text/x-java.
     * 
     * @since 0.94
     */
    public CaretAwareJavaSourceTaskFactory(
            @NonNull Phase phase,
            @NonNull Priority priority,
            @NonNull TaskIndexingMode taskIndexingMode,
            @NonNull String... supportedMimeTypes) {
        super(phase, priority, taskIndexingMode);
        Parameters.notNull("supportedMimeTypes", supportedMimeTypes);   //NOI18N
        //XXX: weak, or something like this:
        OpenedEditors.getDefault().addChangeListener(new ChangeListenerImpl());
        this.timeout = DEFAULT_RESCHEDULE_TIMEOUT;
        this.supportedMimeTypes = supportedMimeTypes.length > 0 ? supportedMimeTypes.clone() : null;
    }
    
    /**@inheritDoc*/
    public List<FileObject> getFileObjects() {
        List<FileObject> files = OpenedEditors.filterSupportedMIMETypes(OpenedEditors.getDefault().getVisibleEditorsFiles(), supportedMimeTypes);

        return files;
    }

    private Map<JTextComponent, ComponentListener> component2Listener = new WeakHashMap<JTextComponent, ComponentListener>();
    private static Map<FileObject, Integer> file2LastPosition = new WeakHashMap<FileObject, Integer>();
    
    /**Returns current caret position in current {@link JTextComponent} for a given file.
     *
     * @param file file from which the position should be found
     * @return caret position in the current {@link JTextComponent} for a given file.
     */
    public static synchronized int getLastPosition(FileObject file) {
        if (file == null) {
            throw new NullPointerException("Cannot pass null file!");
        }
        
        Integer position = file2LastPosition.get(file);
        
        if (position == null) {
            //no position set yet:
            return 0;
        }
        
        return position;    
    }
    
    static synchronized void setLastPosition(FileObject file, int position) {
       file2LastPosition.put(file, position);
    }
    
    private class ChangeListenerImpl implements ChangeListener {
        
        public void stateChanged(ChangeEvent e) {
            List<JTextComponent> added = new ArrayList<JTextComponent>(OpenedEditors.getDefault().getVisibleEditors());
            List<JTextComponent> removed = new ArrayList<JTextComponent>(component2Listener.keySet());
            
            added.removeAll(component2Listener.keySet());
            removed.removeAll(OpenedEditors.getDefault().getVisibleEditors());
            
            for (JTextComponent c : removed) {
                c.removeCaretListener(component2Listener.remove(c));
            }
            
            for (JTextComponent c : added) {
                ComponentListener l = new ComponentListener(c);
                
                c.addCaretListener(l);
                component2Listener.put(c, l);
                
                //TODO: are we in AWT Thread?:
                Caret caret = c.getCaret();
                if (caret != null) {
                    setLastPosition(OpenedEditors.getFileObject(c), caret.getDot());
                }
            }
            
            fileObjectsChanged();
        }
        
    }
    
    private class ComponentListener implements CaretListener, Runnable {
        
        private final Reference<JTextComponent> component;
        private final RequestProcessor.Task rescheduleTask;
        
        public ComponentListener(JTextComponent component) {
            this.component = new WeakReference<JTextComponent>(component);
            rescheduleTask = WORKER.create(this);
        }
        
        public void run() {
            JTextComponent c = component.get();
            if (c == null) {
                return;
            }
            FileObject file = OpenedEditors.getFileObject(c);

            if (file != null) {
                reschedule(file);
            }
        }
        
        
        public void caretUpdate(CaretEvent e) {
            JTextComponent c = component.get();
            if (c == null) {
                return;
            }
            FileObject file = OpenedEditors.getFileObject(c);
            
            if (file != null) {
                setLastPosition(file, c.getCaretPosition());
                rescheduleTask.schedule(timeout);
            }
        }
        
    }
}
