/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2011 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008-2011 Sun Microsystems, Inc.
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



