/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
