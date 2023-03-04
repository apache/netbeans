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

package org.netbeans.modules.editor.breadcrumbs.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Jan Lahoda, David Strupl, Jan Jancura
 */
@ServiceProvider(service=Scheduler.class)
public final class BreadCrumbsScheduler extends Scheduler {

    public BreadCrumbsScheduler () {
        EditorRegistry.addPropertyChangeListener (new EditorListener ());
        setEditor (EditorRegistry.focusedComponent ());
    }

    private void refresh () {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (currentEditor == null || getSource() == null) return;
                schedule (getSource(), new CursorMovedSchedulerEvent (this, currentEditor.getCaret ().getDot (), currentEditor.getCaret ().getMark ()) {});
            }
        });
    }

    private class AListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
    }

    private final AtomicBoolean listensOnSettings = new AtomicBoolean();
    private JTextComponent  currentEditor;
    private CaretListener   caretListener;
    private Document        currentDocument;


    protected void setEditor (JTextComponent editor) {
        if (currentEditor != null) {
            currentEditor.removeCaretListener (caretListener);
        }
        currentEditor = editor;
        if (editor != null) {
            if (listensOnSettings.compareAndSet(false,true)) {
                BreadcrumbsController.addBreadCrumbsEnabledListener(new AListener ());
            }
            if (caretListener == null) {
                caretListener = new ACaretListener ();
            }
            editor.addCaretListener (caretListener);
            Document document = editor.getDocument ();
            if (currentDocument == document) return;
            currentDocument = document;
            final Source source = Source.create (currentDocument);
            schedule (source, new CursorMovedSchedulerEvent (this, editor.getCaret ().getDot (), editor.getCaret ().getMark ()) {});
        } else {
            currentDocument = null;
            schedule(null, null);
        }
    }

    @Override
    public String toString () {
        return "BreadCrumbsScheduler";
    }

    @Override
    protected SchedulerEvent createSchedulerEvent (SourceModificationEvent event) {
        final JTextComponent ce = currentEditor;
        final Caret caret = ce != null ? ce.getCaret() : null;
        final Source s = getSource();
        if (event.getModifiedSource() == s && caret != null) {
            return new CursorMovedSchedulerEvent(this, caret.getDot(), caret.getMark()) { };
        }
        return null;
    }


    // innerclasses ............................................................

    private class ACaretListener implements CaretListener {

        public void caretUpdate (CaretEvent e) {
            schedule (new CursorMovedSchedulerEvent (this, e.getDot (), e.getMark ()) {});
        }
    }

    private class EditorListener implements PropertyChangeListener {

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
                    FileObject fileObject = NbEditorUtilities.getFileObject (document);
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
}
