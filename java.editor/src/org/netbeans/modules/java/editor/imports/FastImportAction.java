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

                    final List<TypeElement> priviledged = ElementFilter.typesIn(result.a.get(ident));

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
