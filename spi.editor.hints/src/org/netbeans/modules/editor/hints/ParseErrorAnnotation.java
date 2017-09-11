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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.text.Annotation;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jan Lahoda
 */
public class ParseErrorAnnotation extends Annotation implements PropertyChangeListener {

    private final Severity severity;
    private final String customType;
    private final FixData fixes;
    private final String description;
    private final String shortDescription;
    private final Position lineStart;
    private final AnnotationHolder holder;
    
    /** Creates a new instance of ParseErrorAnnotation */
    public ParseErrorAnnotation(Severity severity, FixData fixes, String description, Position lineStart, AnnotationHolder holder) {
        this.severity = severity;
        this.customType = null;
        this.fixes = fixes;
        this.description = description;
        this.shortDescription = description + NbBundle.getMessage(ParseErrorAnnotation.class, "LBL_shortcut_promotion"); //NOI18N
        this.lineStart = lineStart;
        this.holder = holder;
        
        if (!fixes.isComputed()) {
            fixes.addPropertyChangeListener(WeakListeners.propertyChange(this, fixes));
        }
    }
    
    public ParseErrorAnnotation(Severity severity, String customType, FixData fixes, String description, Position lineStart, AnnotationHolder holder) {
        this.severity = severity;
        this.customType = customType;
        this.fixes = fixes;
        this.description = description;
        this.shortDescription = description + NbBundle.getMessage(ParseErrorAnnotation.class, "LBL_shortcut_promotion"); //NOI18N
        this.lineStart = lineStart;
        this.holder = holder;
        
        if (!fixes.isComputed()) {
            fixes.addPropertyChangeListener(WeakListeners.propertyChange(this, fixes));
        }
    }

    public String getAnnotationType() {
        boolean hasFixes = fixes.isComputed() && !fixes.getFixes().isEmpty();
        
        if (customType == null) {
            switch (severity) {
                case ERROR:
                    if (hasFixes)
                        return "org-netbeans-spi-editor-hints-parser_annotation_err_fixable";
                    else
                        return "org-netbeans-spi-editor-hints-parser_annotation_err";

                case WARNING:
                    if (hasFixes)
                        return "org-netbeans-spi-editor-hints-parser_annotation_warn_fixable";
                    else
                        return "org-netbeans-spi-editor-hints-parser_annotation_warn";
                case VERIFIER:
                    if (hasFixes)
                        return "org-netbeans-spi-editor-hints-parser_annotation_verifier_fixable";
                    else
                        return "org-netbeans-spi-editor-hints-parser_annotation_verifier";
                case HINT:
                    if (hasFixes)
                        return "org-netbeans-spi-editor-hints-parser_annotation_hint_fixable";
                    else
                        return "org-netbeans-spi-editor-hints-parser_annotation_hint";
                default:
                    throw new IllegalArgumentException(String.valueOf(severity));
            }
        } else {
            return customType;
        }
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (fixes.isComputed()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    firePropertyChange(PROP_ANNOTATION_TYPE, null, getAnnotationType());
                }
            });
        }
    }
    
    public FixData getFixes() {
        return fixes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLineNumber() {
        return holder.lineNumber(lineStart);
    }
    
    Severity getSeverity() {
        return severity;
    }
    
    String getCustomType() {
        return customType;
    }

    private StyledDocument attachedTo;

    synchronized void attachAnnotation(StyledDocument doc, Position lineStart) {
        if (attachedTo == null) {
            attachedTo = doc;
            NbDocument.addAnnotation((StyledDocument) doc, lineStart, -1, this);
        } else {
            Level toLog = Level.FINE;
            assert (toLog = Level.INFO) != null;
            AnnotationHolder.LOG.log(toLog, "Attempt to attach already attached annotation", new Exception());
        }
    }

    synchronized void detachAnnotation(StyledDocument doc) {
        if (attachedTo != null) {
            assert attachedTo == doc : doc.toString() + " is not " + attachedTo.toString();
            NbDocument.removeAnnotation(attachedTo, this);
            attachedTo = null;
        } else {
            Level toLog = Level.FINE;
            assert (toLog = Level.INFO) != null;
            AnnotationHolder.LOG.log(toLog, "Attempt to detach not attached annotation", new Exception());
        }
    }
}
