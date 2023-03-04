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
package org.netbeans.modules.jshell.navigation;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.Rng;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
public class OpenAction extends AbstractAction {
    private final Openable performer;

    @NbBundle.Messages("ACTION_Open=Go to source")
    public OpenAction(Openable performer) {
        super(Bundle.ACTION_Open());
        this.performer = performer;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        performer.open();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }
    
    public static Openable createOpener(JShellEnvironment env, SnippetHandle h) {
        return new Performer(env, h);
    }
    
    
    
    static class Performer implements Openable {
        private final JShellEnvironment   env;
        private final SnippetHandle       theHandle;

        public Performer(JShellEnvironment env, SnippetHandle theHandle) {
            this.env = env;
            this.theHandle = theHandle;
        }

        @NbBundle.Messages({
            "ERR_CannotLocateConsole=Shell console is not accessible",
            "ERR_ShellConsoleClosed=Shell console has been closed",
            "ERR_NoSourceForSnippet=The snippet has no source"
        })
        @Override
        public void open() {
            FileObject f = env.getConsoleFile();
            if (!f.isValid()) {
                StatusDisplayer.getDefault().setStatusText(Bundle.ERR_CannotLocateConsole());
                return;
            }
            
            if (theHandle.getSection() == null) {
                StatusDisplayer.getDefault().setStatusText(Bundle.ERR_NoSourceForSnippet());
                return;
            }
            
            try {
                DataObject d = DataObject.find(f);
                EditorCookie cake = d.getLookup().lookup(EditorCookie.class);
                cake.openDocument();
                if (!SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(() -> performOpen(cake.getOpenedPanes()));
                } else {
                    performOpen(cake.getOpenedPanes());
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        private void performOpen(JEditorPane[] panes) {
            if (panes == null || panes.length == 0) {
                StatusDisplayer.getDefault().setStatusText(Bundle.ERR_ShellConsoleClosed());
                return;
            }
            JEditorPane p = panes[0];
            Rng[] fragments = theHandle.getFragments();
            
            int to = fragments[0].start;
            p.requestFocus();
            int pos = Math.min(p.getDocument().getLength() - 1, to);
            p.setCaretPosition(pos);
            try {
                Rectangle r = p.modelToView(pos);
                p.scrollRectToVisible(r);
            } catch (BadLocationException ex) {
            }
        }
    }
}
