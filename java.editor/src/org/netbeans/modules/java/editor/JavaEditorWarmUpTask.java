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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.editor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.java.JavaKit;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * "Warm-up" task for editor. Executed after IDE startup, it should
 * pre-initialize some suitable parts of the module to improve first time usage
 * experience - which might suffer from long response time due to class loading
 * and various initialization.
 * See {@link org.netbeans.core.AfterStartWarmUp} for details about how the task is run.
 *
 * @author  Tomas Pavek, Martin Roskanin
 */
public class JavaEditorWarmUpTask implements Runnable {
    
    @ServiceProvider(service=Runnable.class,path="WarmUp")
    public static class Provider implements Runnable {

        private AtomicBoolean b = new AtomicBoolean();
        
        @Override
        public void run() {
            if (!b.compareAndSet(false, true))
                return;
            new JavaEditorWarmUpTask().run();
        }
    }
    
    /**
     * Number of lines that an artificial document
     * for view hierarchy code optimization will have.
     * <br/>
     * The number is hotspot's threshold for method compilation 1500 divided by 10 + 1
     * since 1500 would be rather high line count. Anyway main effect of warmup
     * the class pre-loading should apply regardless of this value.
     */
    private static final int ARTIFICIAL_DOCUMENT_LINE_COUNT = 151;

    /**
     * Number of times a long document is assigned to the editor pane
     * which causes the view hierarchy for it to be (re)built.
     */
    private static final int VIEW_HIERARCHY_CREATION_COUNT = 1;
    
    /**
     * Width of buffered image area.
     */
    private static final int IMAGE_WIDTH = 600;
    
    /**
     * Height of buffered image area.
     */
    private static final int IMAGE_HEIGHT = 400;
    
    /**
     * Number of paints to be simulated.
     */
    private static final int PAINT_COUNT = 1;

    // -J-Dorg.netbeans.modules.java.editor.JavaEditorWarmUpTask.level=FINE
    private static final Logger LOG = Logger.getLogger(JavaEditorWarmUpTask.class.getName());
    
    private static final int STATUS_INIT = 0;
    private static final int STATUS_CREATE_PANE = 1;
    private static final int STATUS_CREATE_DOCUMENTS = 2;
    private static final int STATUS_SWITCH_DOCUMENTS = 3;
    private static final int STATUS_TRAVERSE_VIEWS = 4;
    private static final int STATUS_RENDER_FRAME = 5;
    private static final int STATUS_FINISHED = 6;
    
    private static final RequestProcessor RP = new RequestProcessor(JavaEditorWarmUpTask.class.getName(), 1, false, false);
    
    private int status = STATUS_INIT;

    private JEditorPane pane;
    private JFrame frame;
    private Document emptyDoc;
    private Document longDoc;
    private Document lexerDoc;
    private Graphics bGraphics;
    
    private BaseKit javaKit;

    private long startTime;
    
    public @Override void run() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        
        switch (status) {
            case STATUS_INIT:
                startTime = System.currentTimeMillis();
        
                // Init of JavaKit and JavaOptions
                javaKit = BaseKit.getKit(JavaKit.class);
        
                //creating actions instances
                javaKit.getActions();

                try {
                    ((Callable) javaKit).call();
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }

                // initialize empty doc
                emptyDoc = javaKit.createDefaultDocument();
                
                lexerDoc = javaKit.createDefaultDocument();
                try {
                    lexerDoc.insertString(0, "'c'\"s\"/**d*/", null);
                    lexerDoc.render(new Runnable() {
                        @Override
                        public void run() {
                            TokenHierarchy<?> th = TokenHierarchy.get(lexerDoc);
                            TokenSequence<JavaTokenId> ts = th.tokenSequence(JavaTokenId.language());
                            while (ts != null && ts.moveNext()) {
                                ts.embedded(); // Pre-init embedded tokens
                            }
                        }
                    });
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                // Start of a code block that tries to force hotspot to compile
                // the view hierarchy and related classes for faster performance
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Kit instances initialized: {0}", (System.currentTimeMillis() - startTime)); //NOI18N
                }
                startTime = System.currentTimeMillis();

                
                if (EditorRegistry.lastFocusedComponent() == null) { // no components opened yet
                    status = STATUS_CREATE_PANE;
                    SwingUtilities.invokeLater(this); // must run in AWT
                }// otherwise stop because editor pane(s) already opened (optimized)
                break;
                
            case STATUS_CREATE_PANE: // now create editor component and assign a kit to it
                assert SwingUtilities.isEventDispatchThread(); // This part must run in AWT

                pane = new JEditorPane();
                pane.setEditorKit(javaKit);

                // Obtain extended component (with editor's toolbar and scrollpane)
                EditorUI editorUI = Utilities.getEditorUI(pane);
                if (editorUI != null) {
                    // Make sure extended component necessary classes get loaded
                    editorUI.getExtComponent();
                }

                status = STATUS_CREATE_DOCUMENTS;
                RP.post(this);
                break;
                
            case STATUS_CREATE_DOCUMENTS:

                // Have two documents - one empty and another one filled with many lines
                longDoc = pane.getDocument();

                try {
                    // Fill the document with data.
                    // Number of lines is more important here than number of columns in a line
                    // Do one big insert instead of many small inserts
                    StringBuilder sb = new StringBuilder();
                    for (int i = ARTIFICIAL_DOCUMENT_LINE_COUNT; i > 0; i--) {
                        sb.append("int ident = 1; // comment\n"); // NOI18N
                    }
                    longDoc.insertString(0, sb.toString(), null);

                    status = STATUS_SWITCH_DOCUMENTS;
                    SwingUtilities.invokeLater(this);

                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }
                break;

            case STATUS_SWITCH_DOCUMENTS:
                // Switch between empty doc and long several times
                // to force view hierarchy creation
                for (int i = 0; i < VIEW_HIERARCHY_CREATION_COUNT; i++) {
                    pane.setDocument(emptyDoc);

                    // Set long doc - causes view hierarchy to be rebuilt
                    pane.setDocument(longDoc);
                }
                
                status = STATUS_TRAVERSE_VIEWS;
                SwingUtilities.invokeLater(this);
                break;
                
            case STATUS_TRAVERSE_VIEWS:
                Dimension paneSize = pane.getPreferredSize();
                Document doc = pane.getDocument();
                int docLen = doc.getLength();
                try {
                    // Do model<=>view calculations several times
                    int opCount = 10;
                    for (int i = 0; i < opCount; i++) {
                        pane.modelToView(docLen * i / opCount);
                        pane.viewToModel(new Point(paneSize.height * i / opCount, paneSize.width * i / opCount));
                    }
                } catch (BadLocationException e) {
                    Exceptions.printStackTrace(e);
                }

                status = STATUS_RENDER_FRAME;
                SwingUtilities.invokeLater(this);
                break;

            case STATUS_RENDER_FRAME:
                JEditorPane p = pane;
                frame = new JFrame();
                EditorUI ui = Utilities.getEditorUI(p);
                JComponent mainComp = null;
                if (ui != null) {
                    mainComp = ui.getExtComponent();
                }
                if (mainComp == null) {
                    mainComp = new javax.swing.JScrollPane(p);
                }
                frame.getContentPane().add(mainComp);
                frame.pack();
                frame.paint(bGraphics);
                frame.getContentPane().removeAll();
                frame.dispose();
                if (p != null) {
                    p.setEditorKit(null);
                }

                // Candidates Annotations.getLineAnnotations()

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "View hierarchy initialized: {0}", (System.currentTimeMillis() - startTime)); //NOI18N
                }
                startTime = System.currentTimeMillis();
                status = STATUS_FINISHED;
                RP.post(this);
                break;

            case STATUS_FINISHED:
                this.pane = null;
                this.frame = null;
                this.emptyDoc = null;
                this.longDoc = null;
                this.bGraphics = null;
                this.javaKit = null;
                this.startTime = 0;
                this.status = STATUS_INIT;
                break;

            default:
                throw new IllegalStateException();
        }
    }
    
}
