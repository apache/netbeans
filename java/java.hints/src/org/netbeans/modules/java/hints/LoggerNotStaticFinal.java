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

import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import java.util.EnumSet;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.LoggerNotStaticFinal", description = "#DESC_org.netbeans.modules.java.hints.LoggerNotStaticFinal", category="logging", suppressWarnings={"NonConstantLogger"}) //NOI18N
public class LoggerNotStaticFinal {

    @TriggerPatterns({
        @TriggerPattern(value="$mods$ java.util.logging.Logger $LOG;"), //NOI18N
        @TriggerPattern(value="$mods$ java.util.logging.Logger $LOG = $init;"), //NOI18N
        @TriggerPattern(value="$mods$ java.lang.System.Logger $LOG;"), //NOI18N
        @TriggerPattern(value="$mods$ java.lang.System.Logger $LOG = $init;") //NOI18N
    })
    public static ErrorDescription checkLoggerDeclaration(HintContext ctx) {
        Element e = ctx.getInfo().getTrees().getElement(ctx.getPath());
        if (e != null && e.getEnclosingElement().getKind() == ElementKind.CLASS &&
            (!e.getModifiers().contains(Modifier.STATIC) || !e.getModifiers().contains(Modifier.FINAL)) &&
            ctx.getInfo().getElementUtilities().outermostTypeElement(e) == e.getEnclosingElement()
        ) {
            return ErrorDescriptionFactory.forName(
                    ctx,
                    ctx.getPath(),
                    NbBundle.getMessage(LoggerNotStaticFinal.class, "MSG_LoggerNotStaticFinal_checkLoggerDeclaration", e), //NOI18N
                    new LoggerNotStaticFinalFix(NbBundle.getMessage(LoggerNotStaticFinal.class, "MSG_LoggerNotStaticFinal_checkLoggerDeclaration_fix", e), TreePathHandle.create(e, ctx.getInfo())).toEditorFix() //NOI18N
            );
        } else {
            return null;
        }
    }

    private static final class LoggerNotStaticFinalFix extends JavaFix {

        private final String text;

        public LoggerNotStaticFinalFix(String text, TreePathHandle loggerFieldHandle) {
            super(loggerFieldHandle);
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            VariableTree vt = (VariableTree) tp.getLeaf();
            ModifiersTree mt = vt.getModifiers();
            Set<Modifier> modifiers = EnumSet.of(Modifier.FINAL, Modifier.STATIC);
            modifiers.addAll(mt.getFlags());

            ModifiersTree newMod = wc.getTreeMaker().Modifiers(modifiers, mt.getAnnotations());

            wc.rewrite(mt, newMod);
        }

    } // End of FixImpl class
}
