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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;
import org.openide.util.WeakSet;

/**
 *
 * @author Jan Lahoda
 */
class OpenedEditors implements PropertyChangeListener {

    private Set<JTextComponent> visibleEditors = new WeakSet<JTextComponent>();
    private Map<JTextComponent, DataObject> visibleEditors2Files = new WeakHashMap<JTextComponent, DataObject>();
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    private static OpenedEditors DEFAULT;

    private OpenedEditors() {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                stateChanged();
            }
        });
    }

    public static synchronized OpenedEditors getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OpenedEditors();
        }

        return DEFAULT;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> listenersCopy = null;

        synchronized (this) {
            listenersCopy = new ArrayList(listeners);
        }

        for (ChangeListener l : listenersCopy) {
            l.stateChanged(e);
        }
    }

    public synchronized List<JTextComponent> getVisibleEditors() {
        List<JTextComponent> result = new LinkedList<JTextComponent>();
        
        for (JTextComponent c : visibleEditors) {
            if (visibleEditors2Files.get(c) != null) {
                result.add(c);
            }
        }
        
        return Collections.unmodifiableList(result);
    }

    public synchronized Collection<FileObject> getVisibleEditorsFiles() {
        Set<FileObject> result = new LinkedHashSet<FileObject>();
        
        for (DataObject file : visibleEditors2Files.values()) {
            if (file != null) {
                result.add(file.getPrimaryFile());
            }
        }

        return Collections.unmodifiableCollection(result);
    }

    public synchronized void stateChanged() {
        for (JTextComponent c : visibleEditors) {
            c.removePropertyChangeListener(this);
            
            DataObject file = visibleEditors2Files.remove(c);
            
            if (file != null) {
                file.removePropertyChangeListener(this);
            }
        }

        visibleEditors.clear();

        JTextComponent editor = EditorRegistry.lastFocusedComponent();

        DataObject file = editor != null ? getDataObject(editor) : null;
        
        if (editor instanceof JEditorPane && file != null && JavaSource.forFileObject(file.getPrimaryFile()) != null) {
            visibleEditors.add(editor);
        }

        for (JTextComponent c : visibleEditors) {
            c.addPropertyChangeListener(this);
            DataObject cFile = getDataObject(c);
            cFile.addPropertyChangeListener(this);
            visibleEditors2Files.put(c, cFile);
        }

        fireChangeEvent();
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof DataObject && DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
            fireChangeEvent();
            return ;
        }
        
        if (evt.getSource() instanceof JTextComponent && visibleEditors.contains(evt.getSource())) {
            JTextComponent c = (JTextComponent) evt.getSource();
            DataObject originalFile = visibleEditors2Files.get(c);
            DataObject nueFile = getDataObject(c);

            if (originalFile != nueFile) {
                visibleEditors2Files.put(c, nueFile);
                fireChangeEvent();
            }
            
            return ;
        }
    }

    private static DataObject getDataObject(JTextComponent pane) {
        Object source = pane.getDocument().getProperty(Document.StreamDescriptionProperty);
        
        if (!(source instanceof DataObject)) {
            return null;
        }
        
        return (DataObject) source;
    }

    static FileObject getFileObject(JTextComponent pane) {
        DataObject od = getDataObject(pane);
        
        return od != null ? od.getPrimaryFile() : null;
    }
    
    /**Checks if the given file is supported. See {@link #filterSupportedMIMETypes}
     * for more details.
     *
     * @param file to check
     * @param type the type to check for the {@link SupportedMimeTypes} annotation
     * @return true if and only if the given file is supported (see {@link #filterSupportedMIMETypes})
     * @throws NullPointerException if <code>file == null</code> or <code>type == null</code>
     */
    public static boolean isSupported(FileObject file, String... mimeTypes) throws NullPointerException {
        Parameters.notNull("files", file);
        
        return !filterSupportedMIMETypes(Collections.singletonList(file), mimeTypes).isEmpty();
    }
    
    /**Filter unsupported files from the <code>files</code> parameter. A supported file
     * <code>f</code> is defined as follows:
     * <ul>
     *     <li><code>JavaSource.forFileObject(f) != null</code></li>
     *     <li>If the <code>type</code> is annotated with the {@link SupportedMimeTypes} annotation,
     *         the file is supported if <code>type.getAnnotation(SupportedMimeTypes.class).value()</code>
     *         contains <code>FileUtil.getMIMEType(f)</code>.
     *     </li>
     *     <li>If the <code>type</code> is not annotated with the {@link SupportedMimeTypes} annotation,
     *         the file is supported if <code>FileUtil.getMIMEType(f) == "text/x-java"</code>.
     * </ul>
     *
     * @param files the list of files to filter
     * @param type the type to check for the {@link SupportedMimeTypes} annotation
     * @return list of files that are supported (see above).
     * @throws NullPointerException if <code>files == null</code> or <code>type == null</code>
     */
    public static List<FileObject> filterSupportedMIMETypes(Collection<FileObject> files, String... mimeTypes) throws NullPointerException {
        Parameters.notNull("files", files);
        
        boolean            allowJavaExtension = false;
        
        if (mimeTypes == null) {
            mimeTypes = new String[] {"text/x-java"};
            allowJavaExtension = true;
        }
        
        List<String>       mimeTypesList = Arrays.asList(mimeTypes);
        boolean            allowAll  = mimeTypesList.contains("*");
        List<FileObject>   result    = new LinkedList<FileObject>();
        
        Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "mimeTypesList={0}", mimeTypesList);
        
        for (FileObject f : files) {
            Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "analyzing={0}", f);
            
            if (JavaSource.forFileObject(f) == null)
                continue;
            
            if (allowAll) {
                result.add(f);
                continue;
            }
            
            if (allowJavaExtension && "java".equals(f.getExt())) {
                result.add(f);
                continue;
            }
            
            String fileMimeType = FileUtil.getMIMEType(f);
            
            Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "fileMimeType={0}", fileMimeType);

            if (fileMimeType == null) {
                continue;
            }
            
            if (mimeTypesList.contains(fileMimeType)) {
                result.add(f);
                continue;
            }
            
            String shorterMimeType = fileMimeType;
            
            while (true) {
                int slash = shorterMimeType.indexOf('/');
                
                if (slash == (-1))
                    break;
                
                int plus  = shorterMimeType.indexOf('+', slash);
                
                if (plus == (-1))
                    break;
                
                shorterMimeType = shorterMimeType.substring(0, slash + 1) + shorterMimeType.substring(plus + 1);
                
                if (mimeTypesList.contains(shorterMimeType)) {
                    result.add(f);
                    break;
                }
            }
        }
        
        Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINE, "filter({0}, {1})={2}", new Object[] {files, mimeTypesList, result});
        
        return result;
    }
    
    static {
        JavaSourceSupportAccessor.ACCESSOR = new JavaSourceSupportAccessor() {
            public Collection<FileObject> getVisibleEditorsFiles() {
                return OpenedEditors.getDefault().getVisibleEditorsFiles();
            }
        };
    }
}
