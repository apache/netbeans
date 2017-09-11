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
    public synchronized static int getLastPosition(FileObject file) {
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
    
    synchronized static void setLastPosition(FileObject file, int position) {
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
