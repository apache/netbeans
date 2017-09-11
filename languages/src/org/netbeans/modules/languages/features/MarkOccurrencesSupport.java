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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseItem;
import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.modules.languages.features.AnnotationManager.LanguagesAnnotation;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class MarkOccurrencesSupport implements CaretListener {

    private static Map<JTextComponent,WeakReference<MarkOccurrencesSupport>> 
                                        editorToMOS = new WeakHashMap<JTextComponent,WeakReference<MarkOccurrencesSupport>> ();
    private JTextComponent              editor;
    private RequestProcessor.Task       parsingTask;
    private List<Highlight>             highlights;
    private List<LanguagesAnnotation>   annotations;
    private final RequestProcessor PROC = new RequestProcessor(MarkOccurrencesSupport.class.getName(), 1);
    
    
    public MarkOccurrencesSupport (JTextComponent editor) {
        this.editor = editor;
        editorToMOS.put (editor, new WeakReference<MarkOccurrencesSupport> (this));
    }

    public void caretUpdate (final CaretEvent e) {
        if (parsingTask != null) {
            parsingTask.cancel ();
        }
        parsingTask = PROC.post (new Runnable () {
            public void run () {
                refresh (e.getDot ());
            }
        }, 1000);
    }
    
    private void refresh (int offset) {
        ParserManagerImpl parserManager = ParserManagerImpl.getImpl (editor.getDocument ());
        if (parserManager.getState () == State.PARSING) {
            return;
        }
        ASTNode node = parserManager.getAST ();
        DatabaseContext root = DatabaseManager.getRoot (node);
        if (root == null) {
            // I keep getting NPEs on the next line while editing RHTML
            // files - please check
            return;
        }
        DatabaseItem item = root.getDatabaseItem (offset);
        if (item == null)
            item = root.getDatabaseItem (offset - 1);
        if (item == null) return;
        removeHighlights ();
        addHighlights (getUsages (item, node));
    }
    
    private void addHighlights (final List<ASTItem> ussages) {
        if (ussages.isEmpty ()) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
                    Highlighting highlighting = Highlighting.getHighlighting (doc);
                    annotations = new ArrayList<LanguagesAnnotation> ();
                    highlights = new ArrayList<Highlight> ();
                    Iterator<ASTItem> it = ussages.iterator ();
                    HashSet<Integer> lines = new HashSet<Integer>();
                    while (it.hasNext ()) {
                        ASTItem i = it.next ();
                        Highlight h = highlighting.highlight (i.getOffset (), i.getEndOffset (), getHighlightAS ());
                        if (h == null) {
                            continue;
                        }
                        highlights.add (h);
                        int lineNumber = Utilities.getLineOffset(doc, i.getOffset());
                        if (!lines.contains(lineNumber)) {
                            LanguagesAnnotation la = new LanguagesAnnotation (
                                "Usage",
                                "..."
                            );
                            doc.addAnnotation (
                                doc.createPosition (i.getOffset ()),
                                i.getLength (),
                                la
                            );
                            lines.add(lineNumber);
                            annotations.add (la);
                        }
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                }
            }
        });
    }
    
    static List<ASTItem> getUsages (DatabaseItem item, ASTNode root) {
        List<ASTItem> result = new ArrayList<ASTItem> ();
        DatabaseDefinition definition = null;
        if (item instanceof DatabaseDefinition)
            definition = (DatabaseDefinition) item;
        else
            definition = ((DatabaseUsage) item).getDefinition ();
        if (definition.getSourceFileUrl() == null) 
            // It's a local definition
            result.add (root.findPath (definition.getOffset ()).getLeaf ());
        Iterator<DatabaseUsage> it = definition.getUsages ().iterator ();
        while (it.hasNext ()) {
            DatabaseUsage databaseUsage =  it.next();
            result.add (root.findPath (databaseUsage.getOffset ()).getLeaf ());
        }
        return result;
    }
    
    static void removeHighlights (JTextComponent editor) {
        WeakReference<MarkOccurrencesSupport> wr = editorToMOS.get (editor);
        if (wr == null) return;
        MarkOccurrencesSupport mos = wr.get ();
        if (mos == null) return;
        mos.removeHighlights ();
    }

    private void removeHighlights (
    ) {
        if (highlights == null) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                if (highlights == null) return;
                NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
                Iterator<Highlight> it = highlights.iterator ();
                while (it.hasNext ())
                    it.next ().remove ();
                Iterator<LanguagesAnnotation> it2 = annotations.iterator ();
                while (it2.hasNext ())
                    doc.removeAnnotation (it2.next ());
                highlights = null;
                annotations = null;
            }
        });
    }
            
    private static AttributeSet highlightAS = null;
    
    private static AttributeSet getHighlightAS () {
        if (highlightAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Background, new Color (236, 235, 163));
            highlightAS = as;
        }
        return highlightAS;
    }
}
