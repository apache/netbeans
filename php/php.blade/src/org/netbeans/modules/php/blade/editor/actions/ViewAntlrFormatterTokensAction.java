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
package org.netbeans.modules.php.blade.editor.actions;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.AbstractAction;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.php.blade.syntax.antlr4.formatter.BladeAntlrFormatterLexer;
import static org.netbeans.modules.php.blade.syntax.antlr4.formatter.BladeAntlrFormatterLexer.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 *
 * @author bhaidu
 */
@ActionID(id = "org.netbeans.modules.php.blade.editor.actions.ViewAntlrFormatterTokens", category = "DebugAntlrActions")
@ActionRegistration(displayName = "AntlrFormatter Tokens")
public class ViewAntlrFormatterTokensAction extends AbstractAction implements ActionListener {

    Node node;
    private transient JEditorPane viewer;

    public ViewAntlrFormatterTokensAction(Node node) {
        this.node = node;
        putValue(NAME, "AntlrFormatter Tokens");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            return;
        }
        AntlrLexerPreviewComponent comp = new AntlrLexerPreviewComponent(fo);
        comp.open();
        comp.setVisible(true);
    }

    public final class AntlrLexerPreviewComponent extends TopComponent {

        private final FileObject fileObject;

        public AntlrLexerPreviewComponent(FileObject fo) {
            this.fileObject = fo;
            initComponents();
        }

        @Override
        public int getPersistenceType() {
            return TopComponent.PERSISTENCE_NEVER;
        }

        protected void initComponents() {
            setName("Antlr formatter token preview - " + fileObject.getName());
            setLayout(new BorderLayout());
            viewer = new JEditorPane();
            viewer.setContentType("text/plain");
            viewer.setEditable(false);
            //viewer.addHyperlinkListener(this::linkHandler);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(viewer), BorderLayout.CENTER);
            add(panel);
            Rectangle vis = viewer.getVisibleRect();
            try {
                CharStream cs = CharStreams.fromString(String.valueOf(fileObject.asText()));
                BladeAntlrFormatterLexer lexer = new BladeAntlrFormatterLexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                tokens.fill();
                Document doc = viewer.getDocument();

                // Would be better to create some diff and update the changed elemets
                doc.remove(0, doc.getLength());
                StringBuilder result = new StringBuilder();

                int lastLine = 0;
                for (Token token : tokens.getTokens()) {
                    if (lastLine != token.getLine()) {
                        lastLine = token.getLine();
                        if (lastLine > 1) {
                            result.append("\n");
                        }
                        result.append("L");
                        result.append(lastLine);
                        result.append(": ");
                    }
                    switch (token.getType()) {
                        case PARAM_COMMA:
                            result.append(" ~,");
                            break;
                        case D_ARG_LPAREN:
                            result.append(" ~(");
                            break;
                        case D_ARG_RPAREN:
                            result.append(" ~)");
                            break;
                        case SG_QUOTE:
                            result.append(" '");
                            break;
                        case WS:
                            result.append(" (");
                            result.append(token.getText());
                            result.append(" )");
                            break;
                        case NL:
                            result.append(" (n)");
                            break;
                        case EQ:
                            result.append("(EQ)");
                            break;
                        case STRING:
                            result.append("string");
                            break;
                        case IDENTIFIER:
                            result.append("~");
                            result.append(token.getText());
                            break;
                        case COMPONENT_TAG:
                            result.append("COMP_TAG ");
                            result.append(token.getText());
                            break;
                        case HTML_CLOSE_TAG:
                            result.append("CLOSE_TAG ");
                            result.append(token.getText());
                            break;
                        case HTML_START_BLOCK_TAG:
                            result.append("START_TAG ");
                            result.append(token.getText());
                            break;
                        case GT_SYMBOL:
                            result.append(" (>)");
                            break;
                        case D_PHP:
                            result.append(" (@php)");
                            break;
                        default:
                            result.append(token.getType());
                            if (token.getText().startsWith("@")) {
                                result.append(" (DIRECTIVE)");
                            }
                    }
                    if (token.getType() > -1) {
                        result.append(" | ");
                    }
                }

                EditorKit kit = viewer.getEditorKit();
                Reader reader = new StringReader(result.toString());
                //doc.
                kit.read(reader, doc, 0);
                viewer.scrollRectToVisible(vis);
            } catch (IOException | BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
