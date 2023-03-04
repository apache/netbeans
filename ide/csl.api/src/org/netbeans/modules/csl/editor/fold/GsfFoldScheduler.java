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

package org.netbeans.modules.csl.editor.fold;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**Copied and adjusted CurrentDocumentScheduler.
 *
 * @author Jan Jancura, Jan Lahoda
 */
@ServiceProvider(service=Scheduler.class)
public class GsfFoldScheduler extends Scheduler {
    
    private JTextComponent  currentEditor;
    private Document        currentDocument;

    public GsfFoldScheduler() {
        setEditor (EditorRegistry.focusedComponent ());
        EditorRegistry.addPropertyChangeListener (new AListener ());
    }

    protected void setEditor (JTextComponent editor) {
        if (editor != null) {
            Document document = editor.getDocument ();
            if (currentDocument == document) return;
            currentDocument = document;
            final Source source = Source.create (currentDocument);
            schedule (source, new SchedulerEvent (this) {});
        }
        else {
            currentDocument = null;
            schedule(null, null);
        }
    }


    private class AListener implements PropertyChangeListener {

        public void propertyChange (PropertyChangeEvent evt) {
            if (evt.getPropertyName () == null ||
                evt.getPropertyName ().equals (EditorRegistry.FOCUSED_DOCUMENT_PROPERTY) ||
                evt.getPropertyName ().equals (EditorRegistry.FOCUS_GAINED_PROPERTY)
            ) {
                JTextComponent editor = EditorRegistry.focusedComponent ();
                if (editor == currentEditor) return;
                currentEditor = editor;
                if (currentEditor != null) {
                    Document document = currentEditor.getDocument ();
                    FileObject fileObject = NbEditorUtilities.getFileObject(document);
                    if (fileObject == null) {
//                        System.out.println("no file object for " + document);
                        return;
                    }
                }
                setEditor (currentEditor);
            }
            else if (evt.getPropertyName().equals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY)) {
                currentEditor = null;
                setEditor(null);
            }
        }
    }

    
    @Override
    public String toString () {
        return this.getClass().getSimpleName();
    }

    @Override
    protected SchedulerEvent createSchedulerEvent (SourceModificationEvent event) {
        if (event.getModifiedSource () == getSource())
            return new SchedulerEvent (this) {};
        return null;
    }

    public static void reschedule() {
        for (Scheduler s : Lookup.getDefault().lookupAll(Scheduler.class)) {
            if (s instanceof GsfFoldScheduler) {
                GsfFoldScheduler gsfScheduler = (GsfFoldScheduler) s;
                gsfScheduler.schedule(new SchedulerEvent(gsfScheduler) {});
            }
        }
    }
}



