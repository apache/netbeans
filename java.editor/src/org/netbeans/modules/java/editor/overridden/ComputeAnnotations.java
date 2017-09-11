/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.overridden;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import java.awt.event.KeyEvent;
import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaParserResultTask;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.*;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class ComputeAnnotations extends JavaParserResultTask<Result> {

    private final AtomicBoolean cancel = new AtomicBoolean();
    
    public ComputeAnnotations() {
        super(Phase.RESOLVED, TaskIndexingMode.ALLOWED_DURING_SCAN);
    }

    @Override
    public void run(Result result, SchedulerEvent event) {
        cancel.set(false);
        
        CompilationInfo info = CompilationInfo.get(result);

        if (info.getChangedTree() != null) {
            //XXX: currently only method bodies are rebuilt.
            return ;
        }
        
        long start = System.currentTimeMillis();
        StyledDocument doc = (StyledDocument) result.getSnapshot().getSource().getDocument(false);

        if (doc == null) {
            return ;
        }
        
        List<IsOverriddenAnnotation> annotations = computeAnnotations(info, doc);

        if (cancel.get()) return ;
        
        AnnotationsHolder holder = AnnotationsHolder.get(info.getFileObject());

        if (holder != null) {
            holder.setNewAnnotations(annotations);
        }

        long end = System.currentTimeMillis();

        Logger.getLogger("TIMER").log(Level.FINE, "Is Overridden Annotations", new Object[] {info.getFileObject(), end - start});
    }

    List<IsOverriddenAnnotation> computeAnnotations(CompilationInfo info, StyledDocument doc) {
        List<IsOverriddenAnnotation> annotations = new LinkedList<IsOverriddenAnnotation>();

        createAnnotations(info, doc, new ComputeOverriding(cancel).process(info), false, annotations);
        createAnnotations(info, doc, new ComputeOverriders(cancel).process(info, null, null, false), true, annotations);
        
        return annotations;
    }

    private void createAnnotations(CompilationInfo info, StyledDocument doc, Map<ElementHandle<? extends Element>, List<ElementDescription>> descriptions, boolean overridden, List<IsOverriddenAnnotation> annotations) {
        String kb = findKeyBinding(overridden ? "goto-implementation" : "goto-super-implementation"); //NOI18N
        if (descriptions != null) {
            for (Entry<ElementHandle<? extends Element>, List<ElementDescription>> e : descriptions.entrySet()) {
                Element ee = e.getKey().resolve(info);
                Tree t = info.getTrees().getTree(ee);

                if (t == null) {
                    //XXX: log
                    continue;
                }

                AnnotationType type;
                String dn;

                if (overridden) {
                    int choice;
                    if (ee.getModifiers().contains(Modifier.ABSTRACT)) {
                        type = AnnotationType.HAS_IMPLEMENTATION;
                        dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_HasImplementations"); //NOI18N
                        choice = 0;
                    } else {
                        type = AnnotationType.IS_OVERRIDDEN;
                        if (ee.getKind().isClass()) {
                            dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_HasSubclasses"); //NOI18N
                            choice = 1;
                        } else {
                            dn = NbBundle.getMessage(ComputeAnnotations.class, "TP_IsOverridden"); //NOI18N
                            choice = 2;
                        }
                    }
                    if (kb != null)
                        dn += NbBundle.getMessage(ComputeAnnotations.class, "LBL_shortcut_promotion", kb, choice); //NOI18N
                } else {
                    StringBuffer tooltip = new StringBuffer();
                    boolean wasOverrides = false;

                    boolean newline = false;

                    for (ElementDescription ed : e.getValue()) {
                        if (newline) {
                            tooltip.append("\n"); //NOI18N
                        }

                        newline = true;

                        if (ed.getModifiers().contains(Modifier.ABSTRACT)) {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Implements", ed.getDisplayName()));
                        } else {
                            tooltip.append(NbBundle.getMessage(ComputeAnnotations.class, "TP_Overrides", ed.getDisplayName()));
                            wasOverrides = true;
                        }
                    }
                    
                    if (wasOverrides) {
                        type = AnnotationType.OVERRIDES;
                    } else {
                        type = AnnotationType.IMPLEMENTS;
                    }

                    dn = tooltip.toString();
                    if (kb != null)
                        dn += NbBundle.getMessage(ComputeAnnotations.class, "LBL_shortcut_promotion", kb, 3); //NOI18N
                }

                int[] elementNameSpan;
                
                switch (t.getKind()) {
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        elementNameSpan = info.getTreeUtilities().findNameSpan((ClassTree) t);
                        break;
                    case METHOD:
                        elementNameSpan = info.getTreeUtilities().findNameSpan((MethodTree) t);
                        break;
                    default:
                        elementNameSpan = new int[] {(int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), t), -1};
                        break;
                }
                
                if (elementNameSpan == null) continue;
                
                Position pos = getPosition(doc, elementNameSpan[0]);

                if (pos == null) {
                    //#179304: possibly the position is outside document bounds (i.e. <0 or >doc.getLenght())
                    continue;
                }
                
                annotations.add(new IsOverriddenAnnotation(doc, pos, type, dn, e.getValue()));
            }
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }
    
    private static Position getPosition(final StyledDocument doc, final int offset) {
        class Impl implements Runnable {
            private Position pos;
            public void run() {
                if (offset < 0 || offset >= doc.getLength())
                    return ;

                try {
                    pos = doc.createPosition(offset - NbDocument.findLineColumn(doc, offset));
                } catch (BadLocationException ex) {
                    //should not happen?
                    Logger.getLogger(ComputeAnnotations.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }

        Impl i = new Impl();

        doc.render(i);

        return i.pos;
    }
    
    private static String findKeyBinding(String actionName) {
        KeyBindingSettings kbs = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(KeyBindingSettings.class); //NOI18N
        for (MultiKeyBinding mkb : kbs.getKeyBindings()) {
            if (actionName.equals(mkb.getActionName())) {
                KeyStroke ks = mkb.getKeyStrokeCount() > 0 ? mkb.getKeyStroke(0) : null;
                return ks != null ? KeyEvent.getKeyModifiersText(ks.getModifiers()) + '+' + KeyEvent.getKeyText(ks.getKeyCode()) : null;
            }
        }
        return null;
    }

    public static final class FactoryImpl extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new ComputeAnnotations());
        }
        
    }
}
