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

import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.MultipleLoggers", description = "#DESC_org.netbeans.modules.java.hints.MultipleLoggers", category="logging", suppressWarnings={"ClassWithMultipleLoggers"}, options=Options.QUERY) //NOI18N
public final class MultipleLoggers {

    public MultipleLoggers() {
    }

    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static Iterable<ErrorDescription> checkMultipleLoggers(HintContext ctx) {
        Element cls = ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (cls == null || cls.getKind() != ElementKind.CLASS || cls.getModifiers().contains(Modifier.ABSTRACT) ||
            (cls.getEnclosingElement() != null && cls.getEnclosingElement().getKind() != ElementKind.PACKAGE)
        ) {
            return null;
        }

        // Check for JUL in namespace.
        TypeElement loggerTypeElement = ctx.getInfo().getElements().getTypeElement("java.util.logging.Logger"); // NOI18N
        TypeMirror loggerTypeElementAsType = null;
        if (loggerTypeElement != null) {
            loggerTypeElementAsType = loggerTypeElement.asType();
            if (loggerTypeElementAsType != null && loggerTypeElementAsType.getKind() != TypeKind.DECLARED) {
                loggerTypeElementAsType = null;
            }
        }

        // Check for System.Logger in namespace.
        TypeElement sysLoggerTypeElement = ctx.getInfo().getElements().getTypeElement("java.lang.System.Logger"); // NOI18N
        TypeMirror sysLoggerTypeElementAsType = null;
        if (sysLoggerTypeElement != null) {
            sysLoggerTypeElementAsType = sysLoggerTypeElement.asType();
            if (sysLoggerTypeElementAsType != null && sysLoggerTypeElementAsType.getKind() != TypeKind.DECLARED) {
                sysLoggerTypeElementAsType = null;
            }
        }

        // Get out if no known loggers in namespace.
        if (loggerTypeElementAsType == null && sysLoggerTypeElementAsType == null)
            return null;

        List<VariableElement> loggerFields = new LinkedList<>();
        List<VariableElement> fields = ElementFilter.fieldsIn(cls.getEnclosedElements());
        for(VariableElement f : fields) {
            if (f.getKind() != ElementKind.FIELD) {
                continue;
            }

            if (f.asType().equals(loggerTypeElementAsType)
                    || f.asType().equals(sysLoggerTypeElementAsType)) {
                loggerFields.add(f);
            }
        }

        if (loggerFields.size() > 1) {
            StringBuilder loggers = new StringBuilder();
            for(VariableElement f : loggerFields) {
                Tree path = ctx.getInfo().getTrees().getTree(f);
                if (path instanceof VariableTree) {
                    int [] span = ctx.getInfo().getTreeUtilities().findNameSpan((VariableTree)path);
                    if (span != null) {
                        if (loggers.length() > 0) {
                            loggers.append(", "); //NOI18N
                        }
                        loggers.append(f.getSimpleName().toString());
                    }
                }
            }

            List<ErrorDescription> errors = new LinkedList<ErrorDescription>();
            for(VariableElement f : loggerFields) {
                Tree path = ctx.getInfo().getTrees().getTree(f);
                ErrorDescription ed = ErrorDescriptionFactory.forName(ctx, path,
                    NbBundle.getMessage(MultipleLoggers.class, "MSG_MultipleLoggers_checkMultipleLoggers", loggers, cls)); //NOI18N
                errors.add(ed);
            }
            return errors;
        } else {
            return null;
        }
    }

}
