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
package org.netbeans.modules.csl.editor.overridden;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ImplementationProvider;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.editor.hyperlink.PopupUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class IsOverriddenAnnotationAction extends AbstractAction {

    public IsOverriddenAnnotationAction() {
        putValue(NAME, NbBundle.getMessage(IsOverriddenAnnotationAction.class,
                                          "CTL_IsOverriddenAnnotationAction")); //NOI18N
        putValue("supported-annotation-types", new String[] {
            "org-netbeans-modules-editor-annotations-is_overridden",
            "org-netbeans-modules-editor-annotations-has_implementations",
            "org-netbeans-modules-editor-annotations-implements",
            "org-netbeans-modules-editor-annotations-overrides"
        });
        setEnabled(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!invokeDefaultAction((JTextComponent) e.getSource())) {
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this)
                nextAction++;
            
            nextAction++;
            
            if (actions.length > nextAction) {
                Action a = actions[nextAction];
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }
    
    private FileObject getFile(JTextComponent component) {
        Document doc = component.getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null) {
            return null;
        }
        
        return od.getPrimaryFile();
    }
    
    private IsOverriddenAnnotation findAnnotation(JTextComponent component, AnnotationDesc desc, int offset) {
        FileObject file = getFile(component);
        
        if (file == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the document."); //NOI18N
            }
            return null;
        }
        
        AnnotationsHolder ah = AnnotationsHolder.get(file);

        if (ah == null) {
            Logger.getLogger(IsOverriddenAnnotationAction.class.getName()).log(Level.INFO, "component=" + component + " does not have attached a IsOverriddenAnnotationHandler"); //NOI18N

            return null;
        }

        for(IsOverriddenAnnotation a : ah.getAnnotations()) {
            if (   a.getPosition().getOffset() == offset
                && desc.getShortDescription().equals(a.getShortDescription())) {
                return a;
            }
        }
        
        return null;
    }
    
    private List<IsOverriddenAnnotation> findAnnotations(JTextComponent component, int offset) {
        FileObject file = getFile(component);

        if (file == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the document."); //NOI18N
            }
            return null;
        }

        AnnotationsHolder ah = AnnotationsHolder.get(file);

        if (ah == null) {
            Logger.getLogger(IsOverriddenAnnotationAction.class.getName()).log(Level.INFO, "component=" + component + " does not have attached a IsOverriddenAnnotationHandler"); //NOI18N

            return null;
        }

        List<IsOverriddenAnnotation> annotations = new LinkedList<IsOverriddenAnnotation>();

        for(IsOverriddenAnnotation a : ah.getAnnotations()) {
            if (a.getPosition().getOffset() == offset) {
                annotations.add(a);
            }
        }

        return annotations;
    }

    boolean invokeDefaultAction(final JTextComponent comp) {
        final Document doc = comp.getDocument();
        
        if (doc instanceof BaseDocument) {
            final int currentPosition = comp.getCaretPosition();
            final Annotations annotations = ((BaseDocument) doc).getAnnotations();
            final Map<String, List<OverrideDescription>> caption2Descriptions = new LinkedHashMap<String, List<OverrideDescription>>();
            final Point[] p = new Point[1];
            
            doc.render(new Runnable() {
                public void run() {
                    try {
                        int line = LineDocumentUtils.getLineIndex((BaseDocument) doc, currentPosition);
                        int startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) doc, line);
                        p[0] = comp.modelToView(startOffset).getLocation();
                        AnnotationDesc desc = annotations.getActiveAnnotation(line);

                        if (desc == null) {
                            return ;
                        }
                        
                        Collection<IsOverriddenAnnotation> annots;

                        if (COMBINED_TYPES.contains(desc.getAnnotationType())) {
                            annots = findAnnotations(comp, startOffset);
                        } else {
                            annots = Collections.singletonList(findAnnotation(comp, desc, startOffset));
                        }

                        for (IsOverriddenAnnotation a : annots) {
                            if (a != null) {
                                caption2Descriptions.put(computeCaption(a.getType(), a.getShortDescription()), new ArrayList<OverrideDescription>(a.getDeclarations()));
                            }
                        }
                    }  catch (BadLocationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
            
            if (caption2Descriptions.isEmpty())
                return false;
            
            JumpList.checkAddEntry(comp, currentPosition);

            mouseClicked(caption2Descriptions, comp, p[0]);
            
            return true;
        }
        
        return false;
    }

    private static void mouseClicked(Map<String, List<OverrideDescription>> caption2Descriptions, JTextComponent c, Point p) {
        if (caption2Descriptions.size() == 1 && caption2Descriptions.values().iterator().next().size() == 1) {
            OverrideDescription desc = caption2Descriptions.values().iterator().next().get(0);
            FileObject file = desc.location.getLocation().getFileObject();

            if (file != null) {
                UiUtils.open(file, desc.location.getLocation().getOffset());
            } else {
                Toolkit.getDefaultToolkit().beep();
            }

            return ;
        }
        
        Point position = new Point(p);

        SwingUtilities.convertPointToScreen(position, c);

        StringBuilder caption = new StringBuilder();
        List<OverrideDescription> descriptions = new LinkedList<OverrideDescription>();
        boolean first = true;

        for (Entry<String, List<OverrideDescription>> e : caption2Descriptions.entrySet()) {
            if (!first) {
                caption.append("/");
            }
            first = false;
            caption.append(e.getKey());
            descriptions.addAll(e.getValue());
        }

        PopupUtil.showPopup(new IsOverriddenPopup(caption.toString(), descriptions), caption.toString(), position.x, position.y, true, 0);
    }

    private static String computeCaption(AnnotationType type, String shortDescription) throws MissingResourceException, IllegalStateException {
        String caption;
        switch (type) {
            case IMPLEMENTS:
                caption = NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Implements");
                break;
            case OVERRIDES:
                caption = NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Overrides");
                break;
            case HAS_IMPLEMENTATION:
            case IS_OVERRIDDEN:
                caption = shortDescription;
                break;
            default:
                throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        }
        return caption;
    }

    private static final Set<String> COMBINED_TYPES = new HashSet<String>(Arrays.asList(
            "org-netbeans-modules-editor-annotations-implements-has-implementations-combined",
            "org-netbeans-modules-editor-annotations-implements-is-overridden-combined",
            "org-netbeans-modules-editor-annotations-override-is-overridden-combined"
    ));
}
