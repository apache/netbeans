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
package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class InstantRenameAction extends BaseAction {
    
    private RenameImplementation renameImplementation;
    
    /** Creates a new instance of InstantRenameAction */
    public InstantRenameAction() {
        super ("in-place-refactoring", ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }
    
    public void actionPerformed (ActionEvent evt, final JTextComponent editor) {
        if (renameImplementation != null) return;
        int offset = editor.getCaretPosition ();
        ParserManagerImpl parserManager = ParserManagerImpl.getImpl (editor.getDocument ());
        if (parserManager.getState () == State.PARSING) {
            return;
        }
        try {
            ASTNode node = parserManager.getAST ();
            DatabaseContext root = DatabaseManager.getRoot (node);
            if (root == null) return;
            DatabaseItem databaseItem = root.getDatabaseItem (offset);
            if (databaseItem == null)
                databaseItem = root.getDatabaseItem (offset - 1);
            if (databaseItem == null) {
                return;
            }
            renameImplementation = new RenameImplementation (databaseItem, editor, node);
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
        }
    }
    
    private class RenameImplementation implements KeyListener, DocumentListener {
        
        private List<Element>               elements;
        private List<Highlight>             highlights;
        private JTextComponent              editor;
        private NbEditorDocument            document;
        private String                      text;
        
        RenameImplementation (
            DatabaseItem        databaseItem,
            JTextComponent      editor,
            ASTNode             node
        ) throws BadLocationException {
            this.editor = editor;
            document = (NbEditorDocument) editor.getDocument ();
            elements = getUssages (databaseItem, node);
            MarkOccurrencesSupport.removeHighlights (editor);
            if (!elements.isEmpty ()) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        highlights = new ArrayList<Highlight> ();
                        Highlighting highlighting = Highlighting.getHighlighting (document);
                        Iterator<Element> it = elements.iterator ();
                        while (it.hasNext ()) {
                            Element element = it.next ();
                            ASTItem item = element.getItem ();
                            highlights.add (highlighting.highlight (item.getOffset (), item.getEndOffset (), getHighlightAS ()));
                        }
                    }
                });
            }
            document.addDocumentListener (this);
            editor.addKeyListener (this);
        }

        // KeyListener .............................................................

        public void keyTyped (KeyEvent e) {
        }

        public void keyPressed (KeyEvent e) {
            if ((e.getKeyCode () == KeyEvent.VK_ESCAPE && e.getModifiers () == 0) || 
                (e.getKeyCode () == KeyEvent.VK_ENTER  && e.getModifiers() == 0)
            ) {
                if (highlights == null) return;
                final List<Highlight> oldHighlights = highlights;
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        Iterator<Highlight> it = oldHighlights.iterator ();
                        while (it.hasNext ())
                            it.next ().remove ();
                    }
                });
                editor.removeKeyListener (this);
                document.removeDocumentListener (this);
                highlights = null;
                elements = null;
                editor = null;
                document = null;
                renameImplementation = null;
                 e.consume ();
            }    
        }

        public void keyReleased (KeyEvent e) {
        }


        // DocumentListener ........................................................

        public void insertUpdate(DocumentEvent e) {
            update ();
        }

        public void removeUpdate(DocumentEvent e) {
            update ();
        }

        public void changedUpdate(DocumentEvent e) {
            update ();
        }

        private List<Element> getUssages (DatabaseItem item, ASTNode root) throws BadLocationException {
            List<Element> result = new ArrayList<Element> ();
            DatabaseDefinition definition = null;
            if (item instanceof DatabaseDefinition) {
                definition = (DatabaseDefinition) item;
            } else {
                definition = ((DatabaseUsage) item).getDefinition ();
                ASTItem i = root.findPath (item.getOffset ()).getLeaf ();
                result.add (new Element (
                    i, 
                    document.createPosition (i.getOffset ()),
                    document.createPosition (i.getEndOffset ()),
                    document
                ));
            }
            ASTItem i = root.findPath (definition.getOffset ()).getLeaf ();
            Element element = new Element (
                i, 
                document.createPosition (i.getOffset ()),
                document.createPosition (i.getEndOffset ()),
                document
            );
            result.add (element);
            text = element.getText ();
            Iterator<DatabaseUsage> it = definition.getUsages ().iterator ();
            while (it.hasNext ()) {
                DatabaseUsage databaseUsage = it.next ();
                i = root.findPath (databaseUsage.getOffset ()).getLeaf ();
                if (i == result.get (0).getItem ()) continue;
                result.add (new Element (
                    i, 
                    document.createPosition (i.getOffset ()),
                    document.createPosition (i.getEndOffset ()),
                    document
                ));
            }
            return result;
        }

        private RequestProcessor requestProcessor;
        private RequestProcessor.Task task;

        private void update () {
            int offset = editor.getCaretPosition ();
            if (!elements.get (0).contains (offset)) return;
            try {
                String newText = elements.get (0).getText ();
                if (text.equals (newText)) return;
                text = newText;
            } catch (BadLocationException ex) {
                ex.printStackTrace ();
            }
            if (requestProcessor == null)
                requestProcessor = new RequestProcessor ("InstantRename");
            if (task != null) task.cancel ();
            task = requestProcessor.post (new Runnable () {
                public void run () {
                    document.removeDocumentListener (RenameImplementation.this);
                    document.runAtomicAsUser (new Runnable () {
                        public void run () {
                            Iterator<Element> it = elements.iterator ();
                            try {
                                String text = it.next ().getText ();
                                while (it.hasNext ())
                                    it.next ().setText (text);
                            } catch (BadLocationException ex) {
                                ex.printStackTrace ();
                            }
                        }
                    });
                    document.addDocumentListener (RenameImplementation.this);
                }
            });
        }

    }
    
    protected Class getShortDescriptionBundleClass () {
        return InstantRenameAction.class;
    }
    
    private static final AttributeSet defaultSyncedTextBlocksHighlight = AttributesUtilities.createImmutable(StyleConstants.Background, new Color(138, 191, 236));
    
    private static AttributeSet getHighlightAS () {
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet as = fcs.getFontColors("synchronized-text-blocks"); //NOI18N
        return as == null ? defaultSyncedTextBlocksHighlight : as;
    }
    
    
    // innerclasses ............................................................
    
    private static class Element {
        private Position start, end;
        private ASTItem item;
        private Document doc;
        
        Element (ASTItem item, Position start, Position end, Document doc) {
            this.item = item;
            this.start = start;
            this.end = end;
            this.doc = doc;
        }
        
        boolean contains (int offset) {
            return start.getOffset () <= offset && offset <= end.getOffset ();
        }
        
        void setText (final String text) {
            try {
                if (text.equals (getText ())) return;
                doc.insertString (end.getOffset (), text, null);
                doc.remove (start.getOffset (), end.getOffset () - start.getOffset () - text.length ());
            } catch (BadLocationException ex) {
                ex.printStackTrace ();
            }
        }
        
        ASTItem getItem () {
            return item;
        }
        
        String getText () throws BadLocationException {
            return doc.getText (start.getOffset (), end.getOffset () - start.getOffset ());
        }
    }
}
