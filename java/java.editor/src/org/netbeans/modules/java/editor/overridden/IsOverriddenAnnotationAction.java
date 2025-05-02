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
package org.netbeans.modules.java.editor.overridden;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Collection;
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
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class IsOverriddenAnnotationAction extends AbstractAction {

    public static final Logger LOG = Logger.getLogger(IsOverriddenAnnotationAction.class.getName());

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
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!invokeDefaultAction((JTextComponent) e.getSource())) {
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this) {
                nextAction++;
            }
            
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
            LOG.log(Level.WARNING, "component={0} does not have a file specified in the document.", component); //NOI18N
            return null;
        }
        
        AnnotationsHolder ah = AnnotationsHolder.get(file);

        if (ah == null) {
            LOG.log(Level.INFO, "component={0} does not have attached a IsOverriddenAnnotationHandler", component); //NOI18N
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
            LOG.log(Level.WARNING, "component={0} does not have a file specified in the document.", component); //NOI18N
            return null;
        }

        AnnotationsHolder ah = AnnotationsHolder.get(file);

        if (ah == null) {
            LOG.log(Level.INFO, "component={0} does not have attached a IsOverriddenAnnotationHandler", component); //NOI18N
            return null;
        }

        List<IsOverriddenAnnotation> annotations = new LinkedList<>();

        for(IsOverriddenAnnotation a : ah.getAnnotations()) {
            if (a.getPosition().getOffset() == offset) {
                annotations.add(a);
            }
        }

        return annotations;
    }

    boolean invokeDefaultAction(final JTextComponent comp) {
        final Document doc = comp.getDocument();
        
        if (doc instanceof BaseDocument baseDocument) {
            final int currentPosition = comp.getCaretPosition();
            final Annotations annotations = baseDocument.getAnnotations();
            final Map<String, List<ElementDescription>> caption2Descriptions = new LinkedHashMap<>();
            final Point[] p = new Point[1];
            
            doc.render(() -> {
                try {
                    int line = LineDocumentUtils.getLineIndex(baseDocument, currentPosition);
                    int startOffset = LineDocumentUtils.getLineStartFromIndex(baseDocument, line);
                    p[0] = comp.modelToView(startOffset).getLocation();
                    AnnotationDesc desc = annotations.getActiveAnnotation(line);
                    
                    if (desc == null) {
                        return ;
                    }
                    
                    Collection<IsOverriddenAnnotation> annots;
                    
                    if (COMBINED_TYPES.contains(desc.getAnnotationType())) {
                        annots = findAnnotations(comp, startOffset);
                    } else {
                        annots = List.of(findAnnotation(comp, desc, startOffset));
                    }
                    
                    for (IsOverriddenAnnotation a : annots) {
                        if (a != null) {
                            caption2Descriptions.put(computeCaption(a.getType(), a.getShortDescription()), a.getDeclarations());
                        }
                    }
                } catch (BadLocationException ex) {
                    LOG.log(Level.WARNING, "bad location", ex); //NOI18N
                }
            });
            
            if (caption2Descriptions.isEmpty())
                return false;
            
            JumpList.addEntry(comp, currentPosition);

            mouseClicked(caption2Descriptions, comp, p[0]);
            
            return true;
        }
        
        return false;
    }

    static void mouseClicked(Map<String, List<ElementDescription>> caption2Descriptions, JTextComponent c, Point p) {
        if (caption2Descriptions.size() == 1 && caption2Descriptions.values().iterator().next().size() == 1) {
            ElementDescription desc = caption2Descriptions.values().iterator().next().get(0);
            desc.open();
            return ;
        }
        
        Point position = new Point(p);

        SwingUtilities.convertPointToScreen(position, c);

        StringBuilder caption = new StringBuilder();
        List<ElementDescription> descriptions = new LinkedList<>();
        boolean first = true;

        for (Entry<String, List<ElementDescription>> e : caption2Descriptions.entrySet()) {
            if (!first) {
                caption.append("/");
            }
            first = false;
            caption.append(e.getKey());
            descriptions.addAll(e.getValue());
        }

        PopupUtil.showPopup(new IsOverriddenPopup(caption.toString(), descriptions), caption.toString(), position.x, position.y, true, 0);
    }

    static String computeCaption(AnnotationType type, String shortDescription) throws MissingResourceException, IllegalStateException {
        return switch (type) {
            case IMPLEMENTS -> NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Implements");
            case OVERRIDES -> NbBundle.getMessage(IsOverriddenAnnotation.class, "CAP_Overrides");
            case HAS_IMPLEMENTATION, IS_OVERRIDDEN -> shortDescription;
            default -> throw new IllegalStateException("Currently not implemented: " + type); //NOI18N
        };
    }

    private static final Set<String> COMBINED_TYPES = Set.of(
            "org-netbeans-modules-editor-annotations-implements-has-implementations-combined",
            "org-netbeans-modules-editor-annotations-implements-is-overridden-combined",
            "org-netbeans-modules-editor-annotations-override-is-overridden-combined"
    );
}
