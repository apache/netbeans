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

package org.netbeans.modules.lexer.demo;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.swing.JFrame;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import javax.swing.text.TextAction;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenUpdater;

/**
 * Example of using the lexer framework.
 * <BR><CODE>createLexer()</CODE> can be overriden if necessary.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class EditorPaneDemo extends DemoTokenUpdater {
    
    private boolean elementChangeDump;

    public EditorPaneDemo(Language language, boolean maintainLookbacks,
    String initialContent) {

        super(new PlainDocument(), language, maintainLookbacks);
        
        JFrame frame = new JFrame();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                System.exit(0);
            }
        });

        frame.setTitle("Test of " + splitClassName(language.getClass().getName())[1]
            + " - Use Ctrl+L to dump tokens");

        JEditorPane jep = new JEditorPane();
        
        Document doc = getDocument();
        jep.setDocument(doc);
        // Insert initial content string
        try {
            if (initialContent != null) {
                doc.insertString(0, initialContent, null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
            return;
        }
        
        // Initially debug token changes
        setDebugTokenChanges(true);

        frame.getContentPane().add(jep);
        
        DumpAction da = new DumpAction();
        jep.registerKeyboardAction(da,
            KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), 0);
        
        DebugTokenChangesAction dtca = new DebugTokenChangesAction();
        jep.registerKeyboardAction(dtca,
            KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK), 0);
        
        
        System.err.println("NOTE: Press Ctrl+L to dump the document's token list.\n");
        System.err.println("      Press Ctrl+T to toggle debugging of token changes.\n");
        
        // Debug initially
        dump();

        frame.setSize(400, 300);
        frame.setVisible(true);
        
    }
    
    private static String[] splitClassName(String classFullName) {
        int lastDotIndex = classFullName.lastIndexOf('.');
        return new String[] {
            (lastDotIndex >= 0) ? classFullName.substring(0, lastDotIndex) : "", // pkg name
            classFullName.substring(lastDotIndex + 1) // class name
        };
    }
    
    private void dump() {
        System.err.println(allTokensToString());
    }
    
    private class DumpAction extends TextAction {
 
        DumpAction() {
            super("dump");
        }
        
        public void actionPerformed(ActionEvent evt) {
            dump();
        }
        
    }
    
    private class DebugTokenChangesAction extends TextAction {
        
        DebugTokenChangesAction() {
            super("debugTokenChanges");
        }
        
        public void actionPerformed(ActionEvent evt) {
            boolean debugTokenChanges = !getDebugTokenChanges();
            setDebugTokenChanges(debugTokenChanges);
            System.out.println("Debugging of token changes turned "
                + (debugTokenChanges ? "on" : "off")
            );
        }
        
    }
    
    /** Tests language by opening it in a editor.
     */
    public static void main (String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: java " + EditorPaneDemo.class.getName ()
                    + " <language-class-name> [file-to-load]");
                System.exit (1);
            }

            Class langCls = Class.forName (args[0]);

            java.lang.reflect.Method m = langCls.getDeclaredMethod("get", new Class[0]);
            Language language = (Language)m.invoke (null, new Object[0]);

            String content = null;

            if (args.length > 1) {
                String contentFileName = args[1];
                File contentFile = new File(contentFileName);
                if (contentFile.exists()) {
                    Reader reader = new FileReader(contentFile);
                    char[] contentChars = new char[(int)contentFile.length() + 1];
                    int totalReadCount = 0;
                    while (true) {
                        int readCount = reader.read(contentChars, totalReadCount,
                            contentChars.length - totalReadCount);
                        if (readCount == -1) { // no more chars
                            break;
                        }
                        totalReadCount += readCount;
                    }

                    content = new String(contentChars, 0, totalReadCount);

                } else { // content file does not exist
                    System.err.println("Input file NOT FOUND:\n"
                        + contentFile);
                }
            }
            
            new EditorPaneDemo(language, false, content);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

