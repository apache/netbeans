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
            if (hasFixes) {
                //dynamically register this annotation type as fixable, so Fix action (click on the icon) will work for it
                FixAction.addFixableAnnotationType(customType);
            }
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
