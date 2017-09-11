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

package org.netbeans.modules.editor.hints;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;

import org.netbeans.modules.editor.lib2.highlighting.HighlightingManager;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author JanLahoda
 */
public class NextErrorAction extends AbstractAction implements PropertyChangeListener {
    
    public NextErrorAction() {
        putValue(NAME, NbBundle.getMessage(NextErrorAction.class, "LBL_Next_Error"));
        EditorRegistry.addPropertyChangeListener(WeakListeners.propertyChange(this, EditorRegistry.class));
    }

    public void actionPerformed(ActionEvent e) {
        final JTextComponent comp = EditorRegistry.focusedComponent();
        
        if (comp == null) {
            return ;
        }
        
        comp.getDocument().render(new Runnable() {
            public void run() {
                List<ErrorDescription> errors = null;
                int errorOffset = -1;
                int unusedOffset = -1;
                int offsetToTest = comp.getCaretPosition() + 1;
                
                if (offsetToTest < comp.getDocument().getLength()) {
                    errors = findNextError(comp, offsetToTest);
                    errorOffset = errors.isEmpty() ? -1 : errors.iterator().next().getRange().getBegin().getOffset();
                    unusedOffset = findNextUnused(comp, offsetToTest);
                }

                if (errorOffset == (-1) && unusedOffset == (-1)) {
                    errors = findNextError(comp, 0);
                    errorOffset = errors.isEmpty() ? -1 : errors.iterator().next().getRange().getBegin().getOffset();
                    unusedOffset = findNextUnused(comp, 0);
                }

                if (errorOffset == (-1) && unusedOffset == (-1)) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    if (errorOffset != (-1) && (errorOffset < unusedOffset || unusedOffset == (-1))) {
                        comp.getCaret().setDot(errorOffset);

                        Utilities.setStatusText(comp, buildText(errors), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                    } else {
                        comp.getCaret().setDot(unusedOffset);

                        Utilities.setStatusText(comp,NbBundle.getMessage(NextErrorAction.class, "LBL_UnusedElement"),
                                StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                    }
                }
            }
        });
    }
    
    private List<ErrorDescription> findNextError(JTextComponent comp, int offset) {
        Document doc = Utilities.getDocument(comp);
        Object stream = doc.getProperty(Document.StreamDescriptionProperty);
        
        if (!(stream instanceof DataObject)) {
            return Collections.emptyList();
        }
        
        AnnotationHolder holder = AnnotationHolder.getInstance(((DataObject) stream).getPrimaryFile());
        
        List<ErrorDescription> errors = holder.getErrorsGE(offset);
        
        return errors;
    }
    
    private int findNextUnused(JTextComponent comp, int offset) {
        try {
            BaseDocument doc = Utilities.getDocument(comp);
            int lineStart = Utilities.getRowStart(doc, offset);
            // "unused-browseable" in java.editor/.../ColoringManager and csl.api/.../ColoringManager
            HighlightsSequence s = HighlightingManager.getInstance(comp).getBottomHighlights().getHighlights(lineStart, Integer.MAX_VALUE);
            int lastUnusedEndOffset = -1;

            while (s.moveNext()) {
                AttributeSet attrs = s.getAttributes();
                if (attrs != null && attrs.containsAttribute("unused-browseable", Boolean.TRUE)) {
                    
                    if (lastUnusedEndOffset != s.getStartOffset() && s.getStartOffset() >= offset) {
                        return s.getStartOffset();
                    }
                    lastUnusedEndOffset = s.getEndOffset();
                }
            }

            return -1;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return -1;
        }
    }

    private String buildText(List<ErrorDescription> errors) {
        List<ErrorDescription> trueErrors = new LinkedList<ErrorDescription>();
        List<ErrorDescription> others = new LinkedList<ErrorDescription>();

        for (ErrorDescription ed : errors) {
            if (ed == null) {
                continue;
            }

            if (ed.getSeverity() == Severity.ERROR) {
                trueErrors.add(ed);
            } else {
                others.add(ed);
            }
        }

        //build up the description of the annotation:
        StringBuffer description = new StringBuffer();

        concatDescription(trueErrors, description);

        if (!trueErrors.isEmpty() && !others.isEmpty()) {
            description.append(" ");
        }

        concatDescription(others, description);

        return description.toString().replace('\n', ' ');
    }

    private static void concatDescription(List<ErrorDescription> errors, StringBuffer description) {
        boolean first = true;
        
        for (ErrorDescription e : errors) {
            if (!first) {
                description.append(" ");
            }
            description.append(e.getDescription());
            first = false;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(EditorRegistry.focusedComponent() != null);
    }
}
