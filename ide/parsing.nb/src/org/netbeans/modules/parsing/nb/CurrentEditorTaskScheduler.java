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

package org.netbeans.modules.parsing.nb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.parsing.api.Source;

import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Jancura
 */
public abstract class CurrentEditorTaskScheduler extends Scheduler {
    
    private JTextComponent  currentEditor;
    
    public CurrentEditorTaskScheduler () {
        setEditor (EditorRegistry.focusedComponent ());
        EditorRegistry.addPropertyChangeListener (new AListener ());
    }
    
    protected abstract void setEditor (JTextComponent editor);
    
    private class AListener implements PropertyChangeListener {
    
        public void propertyChange (PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null ||
                propName.equals (EditorRegistry.FOCUSED_DOCUMENT_PROPERTY) ||
                propName.equals (EditorRegistry.FOCUS_GAINED_PROPERTY)
            ) {
                JTextComponent editor = EditorRegistry.focusedComponent ();
                if (editor == currentEditor || (editor != null && editor.getClientProperty("AsTextField") != null)) {    //NOI18N
                    return;
                }
                if (currentEditor != null) {
                    currentEditor.removePropertyChangeListener(this);
                }
                currentEditor = editor;
                if (currentEditor != null) {
                    Document document = currentEditor.getDocument ();
                    FileObject fileObject = DataObjectEnvFactory.getFileObject (document);
                    if (fileObject == null) {
//                        System.out.println("no file object for " + document);
                        return;
                    }
                    currentEditor.addPropertyChangeListener(this);
                }
                setEditor (currentEditor);
            } else if (propName.equals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY)) {
                if (currentEditor != null) {
                    currentEditor.removePropertyChangeListener(this);
                }
                currentEditor = null;
                setEditor(null);
            } else if (propName.equals("document") && currentEditor != null) {   //NOI18N
                final Document document = currentEditor.getDocument();
                final FileObject fileObject = DataObjectEnvFactory.getFileObject(document);
                if (fileObject != null && fileObject.isValid()) {
                    final Source src = Source.create(document);
                    schedule(src, new SchedulerEvent(this){});
                }
            }
        }
    }
}
