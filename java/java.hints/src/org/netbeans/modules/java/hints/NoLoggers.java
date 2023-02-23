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

package org.netbeans.modules.java.hints;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JComponent;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.NoLoggers.NoLoggersCustomizer;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.NoLoggers", description = "#DESC_org.netbeans.modules.java.hints.NoLoggers", category="logging", suppressWarnings={"ClassWithoutLogger"}, enabled=false, customizerProvider = NoLoggersCustomizer.class) //NOI18N
public final class NoLoggers {

    public NoLoggers() {
    }

    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static Iterable<ErrorDescription> checkNoLoggers(HintContext ctx) {
        Element cls = ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (cls == null || cls.getKind() != ElementKind.CLASS || cls.getModifiers().contains(Modifier.ABSTRACT) ||
            (cls.getEnclosingElement() != null && cls.getEnclosingElement().getKind() != ElementKind.PACKAGE)
        ) {
            return null;
        }

        TypeElement loggerTypeElement = ctx.getInfo().getElements().getTypeElement("java.util.logging.Logger"); // NOI18N
        if (loggerTypeElement == null) {
            return null;
        }
        TypeMirror loggerTypeElementAsType = loggerTypeElement.asType();
        if (loggerTypeElementAsType == null || loggerTypeElementAsType.getKind() != TypeKind.DECLARED) {
            return null;
        }

        List<TypeMirror> customLoggersList = new ArrayList<>();
        if (isCustomEnabled(ctx.getPreferences())) {
            List<String> customLoggerClasses = getCustomLoggers(ctx.getPreferences());
            if (customLoggerClasses != null) {
                for (String className : customLoggerClasses) {
                    TypeElement customTypeElement = ctx.getInfo().getElements().getTypeElement(className);
                    if (customTypeElement == null) {
                        continue;
                    }
                    TypeMirror customTypeMirror = customTypeElement.asType();
                    if (customTypeMirror == null || customTypeMirror.getKind() != TypeKind.DECLARED) {
                        continue;
                    }
                    customLoggersList.add(customTypeMirror);
                }
            }
        }

        List<VariableElement> loggerFields = new LinkedList<VariableElement>();
        List<VariableElement> fields = ElementFilter.fieldsIn(cls.getEnclosedElements());
        for(VariableElement f : fields) {
            if (f.getKind() != ElementKind.FIELD) {
                continue;
            }

            if (f.asType().equals(loggerTypeElementAsType)) {
                loggerFields.add(f);
            } else if (customLoggersList.contains(f.asType())) {
                loggerFields.add(f);
            }
        }

        if (loggerFields.size() == 0) {
            return Collections.singleton(ErrorDescriptionFactory.forName(
                    ctx,
                    ctx.getPath(),
                    NbBundle.getMessage(NoLoggers.class, "MSG_NoLoggers_checkNoLoggers", cls), //NOI18N
                    new NoLoggersFix(NbBundle.getMessage(NoLoggers.class, "MSG_NoLoggers_checkNoLoggers_Fix", cls), TreePathHandle.create(cls, ctx.getInfo())).toEditorFix() //NOI18N
            ));
        } else {
            return null;
        }
    }
    
    private static boolean isCustomEnabled(Preferences p) {
        return p.getBoolean(LoggerHintsCustomizer.CUSTOM_LOGGERS_ENABLED, false);
    }

    private static List<String> getCustomLoggers(Preferences p) {
        String loggers = p.get(LoggerHintsCustomizer.CUSTOM_LOGGERS, null);
        if (loggers == null) {
            return null;
        }
        List<String> loggersList = new ArrayList<>();
        String[] tmpArray = loggers.split(",");
        loggersList.addAll(Arrays.asList(tmpArray));
        return loggersList;
    }

    public static final class NoLoggersCustomizer implements CustomizerProvider {

        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return new LoggerHintsCustomizer(prefs);
        }
    }

    private static final class NoLoggersFix extends JavaFix {

        private final String description;

        public NoLoggersFix(String description, TreePathHandle loggerFieldHandle) {
            super(loggerFieldHandle);
            this.description = description;
        }

        public String getText() {
            return description;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            TreeMaker m = wc.getTreeMaker();
            ClassTree classTree = (ClassTree) tp.getLeaf();
            Element cls = wc.getTrees().getElement(tp);
            
            if (cls == null) {
                // TODO: log/inform user
                return;
            }

            // find free field name
            String loggerFieldName = null;
            List<VariableElement> fields = ElementFilter.fieldsIn(cls.getEnclosedElements());
            if (!contains(fields, "LOG")) { //NOI18N
                loggerFieldName = "LOG"; //NOI18N
            } else {
                if (!contains(fields, "LOGGER")) { //NOI18N
                    loggerFieldName = "LOGGER"; //NOI18N
                } else {
                    for(int i = 1; i < Integer.MAX_VALUE; i++) {
                        String n = "LOG" + i; //NOI18N
                        if (!contains(fields, n)) {
                            loggerFieldName = n;
                            break;
                        }
                    }
                }
            }

            if (loggerFieldName == null) {
                return;
            }

            // modifiers
            Set<Modifier> mods = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            ModifiersTree mt = m.Modifiers(mods);

            // logger type
            TypeElement loggerTypeElement = wc.getElements().getTypeElement("java.util.logging.Logger"); // NOI18N
            if (loggerTypeElement == null) {
                // TODO: report to the user
                return;
            }
            ExpressionTree loggerClassQualIdent = m.QualIdent(loggerTypeElement);

            // initializer
            MemberSelectTree getLogger = m.MemberSelect(loggerClassQualIdent, "getLogger"); //NOI18N
            ExpressionTree initializer = m.MethodInvocation(
                Collections.<ExpressionTree>emptyList(),
                getLogger,
                Collections.singletonList(m.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    m.MemberSelect(m.QualIdent(cls), "class.getName"), //NOI18N
                    Collections.<ExpressionTree>emptyList())
            ));

            // new logger field
            VariableTree nueLogger = m.Variable(mt, loggerFieldName, loggerClassQualIdent, initializer); //NOI18N
            ClassTree nueClassTree = m.addClassMember(classTree, nueLogger);
            wc.rewrite(classTree, nueClassTree);
        }

        private static boolean contains(Collection<VariableElement> fields, String name) {
            for(VariableElement f : fields) {
                if (f.getSimpleName().contentEquals(name)) {
                    return true;
                }
            }
            return false;
        }
    } // End of FixImpl class
    
}
