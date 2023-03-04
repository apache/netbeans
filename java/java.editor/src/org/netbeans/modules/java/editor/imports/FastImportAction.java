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
package org.netbeans.modules.java.editor.imports;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.editor.imports.ComputeImports.Pair;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class FastImportAction extends BaseAction {
    
    public static final String NAME = "fast-import"; // NOI18N
    
    /** Creates a new instance of FastImportAction */
    public FastImportAction() {
        super(NAME);
    }

    public void actionPerformed(final ActionEvent evt, final JTextComponent target) {
        try {
            final Rectangle carretRectangle = target.modelToView(target.getCaretPosition());
            final Font font = target.getFont();
            final Point where = new Point( carretRectangle.x, carretRectangle.y + carretRectangle.height );
            SwingUtilities.convertPointToScreen( where, target);

            final int position = target.getCaretPosition();
            final String ident = Utilities.getIdentifier(Utilities.getDocument(target), position);
            FileObject file = getFile(target.getDocument());
            
            if (ident == null || file == null) {
                Toolkit.getDefaultToolkit().beep();
                return ;
            }
            
            final JavaSource js = JavaSource.forFileObject(file);
            
            if (js == null) {
                Toolkit.getDefaultToolkit().beep();
                return ;
            }

            final AtomicBoolean cancel = new AtomicBoolean();
            final Task<CompilationController> task = new Task<CompilationController>() {

                public void run(final CompilationController parameter) throws IOException {
                    parameter.toPhase(Phase.RESOLVED);
                    if (cancel.get()) {
                        return;
                    }
                    final JavaSource javaSource = parameter.getJavaSource();
                    Pair<Map<String, List<Element>>, Map<String, List<Element>>> result = new ComputeImports(parameter).computeCandidates(Collections.singleton(ident));

                    List<Element> candidates = result.a.get(ident);
                    // If the identifier is already imported, in scope, or does
                    // not correspond to an importable element, then there will
                    // not be any candidates.
                    if (candidates == null) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    final List<TypeElement> priviledged = ElementFilter.typesIn(candidates);

                    if (priviledged == null) {
                        //not found?
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }

                    final List<TypeElement> denied = new ArrayList<TypeElement>(ElementFilter.typesIn(result.b.get(ident)));

                    denied.removeAll(priviledged);

                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            ImportClassPanel panel = new ImportClassPanel(priviledged, denied, font, javaSource, position, target);
                            PopupUtil.showPopup(panel, "", where.x, where.y, true, carretRectangle.height);
                        }
                    });
                }
            };

            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                public void run() {
                    try {
                        js.runUserActionTask(task, true);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }, NbBundle.getMessage(FastImportAction.class, "LBL_Fast_Import"), cancel, false); // NOI18N
            
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private FileObject getFile(Document doc) {
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null)
            return null;
        
        return od.getPrimaryFile();
    }
}
