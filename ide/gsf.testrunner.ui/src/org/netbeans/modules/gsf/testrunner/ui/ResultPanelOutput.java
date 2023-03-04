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

package org.netbeans.modules.gsf.testrunner.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.accessibility.AccessibleContext;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.gsf.testrunner.ui.output.OutputDocument;
import org.netbeans.modules.gsf.testrunner.ui.output.OutputEditorKit;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Marian Petras
 */
final class ResultPanelOutput extends JScrollPane
                              implements ActionListener {
    
    private static final boolean LOG = false;
    
    static final Color selectedFg;
    static final Color unselectedFg;
    static final Color selectedErr;
    static final Color unselectedErr;
    
    private static final int UPDATE_DELAY = 300;         //milliseconds
    
    static {
        
        /*
         * The property names and colour value constants were copied from class
         * org.netbeans.core.output2.WrappedTextView.
         */
        
        Color color;
        
        color = UIManager.getColor("nb.output.foreground.selected");    //NOI18N
        if (color == null) {
            color = UIManager.getColor("textText");                     //NOI18N
            if (color == null) {
                color = Color.BLACK;
            }
        }
        selectedFg = color;
        
        color = UIManager.getColor("nb.output.foreground");             //NOI18N
        if (color == null) {
            color = selectedFg;
        }
        unselectedFg = color;

        color = UIManager.getColor("nb.output.err.foreground.selected");//NOI18N
        if (color == null) {
            color = new Color(164, 0, 0);
        }
        selectedErr = color;
        
        color = UIManager.getColor("nb.output.err.foreground");         //NOI18N
        if (color == null) {
            color = selectedErr;
        }
        unselectedErr = color;
    }
    
    /** */
    private final JEditorPane textPane;
    /** */
    private final Document doc;
    /** */
    private final ResultDisplayHandler displayHandler;
    
    private Timer timer = null;
    
    /*
     * accessed from multiple threads but accessed only from blocks
     * synchronized with the ResultDisplayHandler's output queue lock
     */
    private volatile boolean timerRunning = false;
    
    /**
     * Creates a new instance of ResultPanelOutput
     */
    ResultPanelOutput(ResultDisplayHandler displayHandler) {
        super();
        if (LOG) {
            System.out.println("ResultPanelOutput.<init>");
        }
        
        textPane = new JEditorPane();
        textPane.setFont(new Font("monospaced", Font.PLAIN, getFont().getSize()));
        textPane.setEditorKit(new OutputEditorKit());
        textPane.setEditable(false);
        textPane.getCaret().setVisible(true);
        textPane.getCaret().setBlinkRate(0);
        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setViewportView(textPane);

        /*
         * On GTK L&F, background of the text pane is gray, even though it is
         * white on a JTextArea. The following is a hack to fix it:
         */
        Color background = UIManager.getColor("TextPane.background");   //NOI18N
        if (background != null) {
            textPane.setBackground(background);
        }

        doc = textPane.getDocument();

        AccessibleContext ac = textPane.getAccessibleContext();
        ac.setAccessibleName(NbBundle.getMessage(getClass(),
                                                "ACSN_OutputTextPane"));//NOI18N
        ac.setAccessibleDescription(NbBundle.getMessage(getClass(),
                                                "ACSD_OutputTextPane"));//NOI18N
        
        this.displayHandler = displayHandler;
    }
    
    /**
     */
    @Override
    public void addNotify() {
        super.addNotify();
        
        final Object[] pendingOutput;
        
        if (LOG) {
            System.out.println("ResultPanelOutput.addNotify()");
        }
        
        /*
         * We must make the following block synchronized using the output queue
         * lock to prevent a scenario that some new output would be delivered to
         * the display handler and the output listener would not be set yet.
         */
        synchronized (displayHandler.getOutputQueueLock()) {
            pendingOutput = displayHandler.consumeOutput();
            if (pendingOutput.length == 0) {
//                displayHandler.setOutputListener(this);
            }
        }
        
        if (pendingOutput.length != 0) {
            displayOutput(pendingOutput);
            startTimer();
        }
    }
    
    /**
     */
    void outputAvailable() {

        /* Called from the AntLogger's thread */

        if (LOG) {
            System.out.println("ResultOutputPanel.outputAvailable() - called by the AntLogger");
        }
        //synchronized (displayHandler.getOutputQueueLock()):
        final Object[] pendingOutput = displayHandler.consumeOutput();
        assert pendingOutput.length != 0;
        new OutputDisplayer(pendingOutput).run();
        if (!timerRunning) {
            startTimer();
        }
    }

    final class OutputDisplayer implements Runnable {
        private final Object[] output;
        OutputDisplayer(Object[] output) {
            this.output = output;
        }
        @Override
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeLater(this);
                return;
            }
            displayOutput(output);
        }
    }
    
    /**
     * This method is called by a Swing timer (in the dispatch thread).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        
        /* Called by the Swing timer (in the EventDispatch thread) */
        
        assert EventQueue.isDispatchThread();
        
        if (LOG) {
            System.out.println("ResultOutputPanel.actionPerformed(...) - called by the timer");
        }
        final Object[] pendingOutput = displayHandler.consumeOutput();
        if (pendingOutput.length != 0) {
            displayOutput(pendingOutput);
        } else {
            synchronized (displayHandler.getOutputQueueLock()) {
                stopTimer();
            }
        }
    }
    
    /**
     */
    private void startTimer() {
        if (LOG) {
            System.out.println("ResultPanelOutput.startTimer()");
        }
        if (timer == null) {
            timer = new Timer(UPDATE_DELAY, this);
        }
        timerRunning = true;
        timer.start();
    }
    
    /**
     */
    private void stopTimer() {
        if (LOG) {
            System.out.println("ResultPanelOutput.stopTimer()");
        }
        if (timer != null) {
            timer.stop();
            timerRunning = false;
        }
    }

    /**
     */
    void displayOutput(final Object[] output) {
        assert EventQueue.isDispatchThread();

        if (LOG) {
            System.out.println("ResultPanelOutput.displayOutput(...):");
            for (int i = 0; output[i] != null; i++) {
                System.out.println("    " + output[i]);
            }
        }
        Object o;
        int index = 0;
        while ((o = output[index++]) != null) {
            boolean errOutput = false;
            if (o == Boolean.TRUE) {
                o = output[index++];
                errOutput = true;
            }
            displayOutputLine(o.toString(), errOutput);
        }
    }
    
    /**
     */
    private void displayOutputLine(final String text, final boolean error) {
        // split to lines, otherwise everything will
        // be printed on one line even if the text 
        // contains line breaks
        String[] lines = text.split("\n"); //NO18N
        for (int i = 0; i < lines.length; i++) {
            if (i == lines.length - 1) {
                // add a trailing new line to the last line
                lines[i] = lines[i] + "\n"; //NOI18N
            }
            try {
                doc.insertString(doc.getLength(),
                        lines[i],
                        error ? OutputDocument.attrs : null);

            } catch (BadLocationException ex) {
                ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
            }
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="displayReport(Report)">
    /* *
     * /
    private void displayReport(final Report report) {
        if (report == null) {
            clear();
            return;
        }
        
        try {
            doc.insertString(
                    0,
                    NbBundle.getMessage(getClass(), "MSG_StdOutput"),   //NOI18N
                    headingStyle);
            doc.insertString(
                    doc.getLength(),
                    "\n",                                               //NOI18N
                    headingStyle);
            if ((report.outputStd != null) && (report.outputStd.length != 0)) {
                displayText(report.outputStd);
            }
            doc.insertString(
                    doc.getLength(),
                    "\n\n",                                             //NOI18N
                    outputStyle);
            doc.insertString(
                    doc.getLength(),
                    NbBundle.getMessage(getClass(), "MSG_ErrOutput"),   //NOI18N
                    headingStyle);
            if ((report.outputErr != null) && (report.outputErr.length != 0)) {
                doc.insertString(
                        doc.getLength(),
                        "\n",                                           //NOI18N
                        headingStyle);
                displayText(report.outputErr);
            }
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
    }
    */
    //</editor-fold>
    
    /**
     */
    private void clear() {
        assert EventQueue.isDispatchThread();
        
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException ex) {
            ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="displayText(String[])">
    /* *
     * /
    private void displayText(final String[] lines) throws BadLocationException {
        final int limit = lines.length - 1;
        for (int i = 0; i < limit; i++) {
            doc.insertString(doc.getLength(),
                             lines[i] + '\n',
                             outputStyle);
        }
        doc.insertString(doc.getLength(),
                         lines[limit],
                         outputStyle);
    }
    */
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="display(DisplayContents)">
    /* *
     * /
    private void display(ResultDisplayHandler.DisplayContents display) {
        assert EventQueue.isDispatchThread();
        
        Report report = display.getReport();
        String msg = display.getMessage();
        if (report != null) {
            displayReport(report);
        } else {
            clear();
        }
    }
    //</editor-fold>

    /* *
     * /
    //<editor-fold defaultstate="collapsed" desc="updateDisplay()">
    private void updateDisplay() {
        ResultDisplayHandler.DisplayContents display
                                                = displayHandler.getDisplay();
        if (display != null) {
            display(display);
        }
    }
    */
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="stateChanged(ChangeEvent)">
    /* *
     * /
    public void stateChanged(ChangeEvent e) {
        updateDisplay();
    }
     */
    //</editor-fold>
    
    /**
     */
    @Override
    public boolean requestFocusInWindow() {
        return textPane.requestFocusInWindow();
    }

}
