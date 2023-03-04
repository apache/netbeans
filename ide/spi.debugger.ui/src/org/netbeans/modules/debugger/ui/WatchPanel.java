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
package org.netbeans.modules.debugger.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * A GUI panel for customizing a Watch.
 *
 * @author Maros Sandor
 */
public class WatchPanel {

    private JPanel panel;
    private JTextComponent editorPane;
    private String expression;

    public WatchPanel(String expression) {
        this.expression = expression;
    }

    public JComponent getPanel() {
        if (panel != null) return panel;

        panel = new JPanel();
        ResourceBundle bundle = NbBundle.getBundle(WatchPanel.class);

        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JLabel textLabel = new JLabel();
        Mnemonics.setLocalizedText(textLabel, bundle.getString ("CTL_Watch_Name")); // NOI18N
        textLabel.setBorder (new EmptyBorder (0, 0, 5, 0));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add (BorderLayout.NORTH, textLabel);

        final FileObject file = getRecentFile();
        int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
        int column = getRecentColumn();
        String mimeType = file != null ? file.getMIMEType() : "text/plain"; // NOI18N
        boolean doBind = true;
        if (!mimeType.startsWith("text/") && !mimeType.startsWith("application/")) { // NOI18N
            // If the current file happens to be of unknown or not text MIME type, use the ordinary text one.
            mimeType = "text/plain"; // NOI18N
            doBind = false; // Do not do binding to an unknown file content.
        }

        //Add JEditorPane and context
        JComponent [] editorComponents;
        try {
            editorComponents = Utilities.createSingleLineEditor(mimeType);
        } catch (IllegalArgumentException iaex) {
            // bad MIME type
            editorComponents = Utilities.createSingleLineEditor("text/plain");
            doBind = false; // Do not do binding to an unknown file content.
        }
        JScrollPane sp = (JScrollPane) editorComponents[0];
        editorPane = (JTextComponent) editorComponents[1];
        int h = sp.getPreferredSize().height;
        int w = Math.min(70*editorPane.getFontMetrics(editorPane.getFont()).charWidth('a'),
                         org.openide.windows.WindowManager.getDefault().getMainWindow().getSize().width);
        sp.setPreferredSize(new Dimension(w, h));
        if (doBind) {
            final Point lineCol = adjustLineAndColumn(file, line, column);
            if (file != null && lineCol.x >= 0) {
                DialogBinding.bindComponentToFile(file, lineCol.x - 1, lineCol.y, 0, editorPane);
            }
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    final Point adjustedLineCol = adjustLineAndColumn(file, lineCol.x, lineCol.y);
                    if (!adjustedLineCol.equals(lineCol)) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                DialogBinding.bindComponentToFile(file, adjustedLineCol.x - 1, adjustedLineCol.y, 0, editorPane);
                            }
                        });
                    }
                }
            });
        }
        panel.add (BorderLayout.CENTER, sp);
        editorPane.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        String t = Utils.getIdentifier ();
        if (t != null) {
            editorPane.setText (t);
        } else {
            editorPane.setText (expression);
        }
        editorPane.selectAll ();

        textLabel.setLabelFor (editorPane);
        editorPane.requestFocus ();
        return panel;
    }

    public static FileObject getRecentFile() {
        FileObject fo = EditorContextDispatcher.getDefault().getMostRecentFile();
        if (fo == null) {
            Line mostRecentLine = EditorContextDispatcher.getDefault().getMostRecentLine();
            if (mostRecentLine != null) {
                fo = mostRecentLine.getLookup().lookup(FileObject.class);
            }
        }
        return fo;
    }
    
    public static int getRecentColumn() {
        JEditorPane mostRecentEditor = EditorContextDispatcher.getDefault().getMostRecentEditor();
        if (mostRecentEditor != null) {
            Caret caret = mostRecentEditor.getCaret();
            if (caret != null) {
                int offset = caret.getDot();
                try {
                    int rs = javax.swing.text.Utilities.getRowStart(mostRecentEditor, offset);
                    return offset - rs;
                } catch (BadLocationException blex) {}
            }
        }
        return 0;
    }

    public String getExpression() {
        return editorPane.getText().trim();
    }

    public static Point adjustLineAndColumn(FileObject fo, int theLine, int theColumn) {
        if (theLine == -1) {
            theLine = 1;
        }
        if (fo == null) {
            return new Point(theLine, theColumn);
        }
        if (!"java".equalsIgnoreCase(fo.getExt())) {
            // we do not understand other languages
            return new Point(theLine, theColumn);
        }
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(fo.getInputStream()));
        } catch (FileNotFoundException ex) {
            return new Point(theLine, theColumn);
        }
        try {
            int line = findClassLine(br);
            Point lc = findMethodLineColumn(line, theColumn, br);
            if (theLine < lc.x) {
                return lc;
            }
        } catch (IOException ioex) {
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
        return new Point(theLine, theColumn);
    }

    private static int findClassLine(BufferedReader br) throws IOException {
        int l = 1;
        String line;
        boolean comment = false;
        boolean classDecl = false;
        for (; (line = br.readLine()) != null; l++) {
            if (classDecl) {
                if (line.indexOf('{') >= 0) {
                    return l + 1;
                } else {
                    continue;
                }
            }
            boolean slash = false;
            boolean asterix = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (comment) {
                    if (asterix && c == '/') {
                        comment = false;
                        asterix = false;
                        continue;
                    }
                    asterix = c == '*';
                    continue;
                }
                if (slash && c == '*') {
                    comment = true;
                    slash = false;
                    continue;
                }
                if (c == '/') {
                    if (slash) {
                        // comment, ignore the rest of the line
                        break;
                    }
                    slash = true;
                }
                if (c == 'c' && line.length() >= (i+"class".length()) && "lass".equals(line.substring(i+1, i+5))) {
                    // class declaration
                    classDecl = true;
                    if (line.indexOf('{', i+5) > 0) {
                        return l + 1;
                    }
                }
            }
        }
        return 1; // Did not find anything interesting
    }

    private static Point findMethodLineColumn(int l, int col, BufferedReader br) throws IOException {
        int origLine = l;
        String line;
        boolean isParenthesis = false;
        boolean isThrows = false;
        for (; (line = br.readLine()) != null; l++) {
            int i = 0;
            if (!isParenthesis && (i = line.indexOf(')')) >= 0 || isParenthesis) {
                isParenthesis = true;
                if (!isThrows) {
                    for (i++; i < line.length() && Character.isWhitespace(line.charAt(i)); i++) ;
                    if ((i+"throws".length()) < line.length() && "throws".equals(line.substring(i, i+"throws".length()))) {
                        isThrows = true;
                    }
                }
                if (isThrows) {
                    i = line.indexOf("{", i);
                    if (i < 0) i = line.length();
                }
                if (i < line.length()) {
                    if (line.charAt(i) == '{') {
                        return new Point(l, i+1);
                    } else {
                        isParenthesis = false;
                    }
                }
            }
        }
        return new Point(origLine, col);
    }

}
