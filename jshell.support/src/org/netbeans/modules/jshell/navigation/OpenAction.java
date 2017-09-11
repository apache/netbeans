/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
