/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.python.qshell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.lib.terminalemulator.ActiveTerm;
import org.netbeans.lib.terminalemulator.StreamTerm;
import org.netbeans.modules.python.qshell.richexecution.Program;
import org.netbeans.modules.python.qshell.richexecution.PtyProcess;

/**
 *
 * @author Maros Sandor
 */
public class QShellTerminalPanel extends JComponent {

    private final ActiveTerm term;

    private PtyProcess termProcess;
    private Program lastProgram;

    private boolean closing;
    private boolean closed;


    public QShellTerminalPanel() {
        // this.term = new StreamTerm();
        this.term = new ActiveTerm();
        this.term.setCursorVisible(true);

        term.setHorizontallyScrollable(false);
        term.setEmulation("ansi");
        term.setBackground(Color.white);
        term.setHistorySize(4000);

//        termOptions.addPropertyChangeListener(termOptionsPCL);
//        applyTermOptions(true);

        term.getScreen().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point p = SwingUtilities.convertPoint((Component) e.getSource(),
                                                          e.getPoint(),
                                                          term.getScreen());
//                    postPopupMenu(p);
                }
            }
        } );

        // Tell term about keystrokes we use for menu accelerators so
        // it passes them through.
        /* LATER
         * A-V brings up the main View menu.
        term.getKeyStrokeSet().add(copyAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(pasteAction.getValue(Action.ACCELERATOR_KEY));
        term.getKeyStrokeSet().add(closeAction.getValue(Action.ACCELERATOR_KEY));
        */

        setLayout(new BorderLayout());
        add(term, BorderLayout.CENTER);

//        termTopComponent.add(this);
//        termTopComponent.topComponent().open();
//        termTopComponent.topComponent().requestActive();
//        if (name != null)
//            setTitle(name);
    }

    /**
     * Return the underlying Term.
     * @return the underlying StreamTerm.
     */
    public StreamTerm term() {
        return term;
    }


    public void startProgram(Program program, final boolean restartable) {

        if (termProcess != null)
            throw new IllegalStateException("Process already running");

//        setTitle(program.name());

        if (restartable) {
            lastProgram = program;
//            setActions(new Action[] {rerunAction, stopAction});
        } else {
            lastProgram = null;
//            setActions(new Action[0]);
        }

        TermExecutor executor = new TermExecutor();
        termProcess = executor.start(program, term());

        if (restartable) {
//            stopAction.setEnabled(true);
//            rerunAction.setEnabled(false);
        }

        Thread reaperThread = new Thread() {
            @Override
            public void run() {
                termProcess.waitFor();
                if (restartable && !closing) {
//                    stopAction.setEnabled(false);
//                    rerunAction.setEnabled(true);
                } else {
                    closing = true;
                    closeWork();
                }
                // This doesn't yield the desired result because we need to
                // wait for all the output to be processed:
                // LATER tprintf("Exited with %d\n\r", termProcess.exitValue());
                termProcess = null;
            }
        };
        reaperThread.start();
    }

    private void closeWork() {
        assert closing;
        if (closed)
            return;
//        terminalContainer.reaped(this);
//        termOptions.removePropertyChangeListener(termOptionsPCL);
        closed = true;
    }




}
