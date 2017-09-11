/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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

