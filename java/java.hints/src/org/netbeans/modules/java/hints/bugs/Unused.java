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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.base.semantic.UnusedDetector;
import org.netbeans.modules.java.editor.base.semantic.UnusedDetector.UnusedDescription;
import org.netbeans.modules.java.hints.Feature;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.bugs.Unused", description = "#DESC_org.netbeans.modules.java.hints.bugs.Unused", category="bugs", options=Hint.Options.QUERY, suppressWarnings="unused")
@Messages({
    "DN_org.netbeans.modules.java.hints.bugs.Unused=Unused Element",
    "DESC_org.netbeans.modules.java.hints.bugs.Unused=Detects and reports unused variables, methods and classes",
    "LBL_UnusedPackagePrivate=Also detect unused package private elements",
    "TP_UnusedPackagePrivate=Will also detect package private elements that are unused"
})
public class Unused {

    private static final SourceVersion UNNAMED_VARIABLES = SourceVersion.RELEASE_22;
    private static final Set<ElementKind> SUPPORT_UNNAMED = Set.of(ElementKind.BINDING_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.RESOURCE_VARIABLE);

    private static final boolean DETECT_UNUSED_PACKAGE_PRIVATE_DEFAULT = true;

    @BooleanOption(displayName="#LBL_UnusedPackagePrivate", tooltip="#TP_UnusedPackagePrivate", defaultValue=DETECT_UNUSED_PACKAGE_PRIVATE_DEFAULT)
    public static final String DETECT_UNUSED_PACKAGE_PRIVATE = "detect.unused.package.private";

    @TriggerTreeKind({
        //class-like kinds:
        Kind.ANNOTATION_TYPE, Kind.CLASS, Kind.ENUM, Kind.INTERFACE, Kind.RECORD,
        Kind.VARIABLE,
        Kind.METHOD
    })
    public static List<ErrorDescription> unused(HintContext ctx) {
        List<UnusedDescription> unused = UnusedDetector.findUnused(ctx.getInfo(), () -> ctx.isCanceled());
        if (unused.isEmpty()) {
            return null;
        }
        boolean detectUnusedPackagePrivate = ctx.getPreferences().getBoolean(DETECT_UNUSED_PACKAGE_PRIVATE, DETECT_UNUSED_PACKAGE_PRIVATE_DEFAULT);
        for (UnusedDescription ud : unused) {
            if (ctx.isCanceled()) {
                break;
            }
            if (ud.unusedElementPath().getLeaf() != ctx.getPath().getLeaf()) {
                continue;
            }
            if (!detectUnusedPackagePrivate && ud.packagePrivate()) {
                continue;
            }
            ErrorDescription err = convertUnused(ctx, ud);
            if (err != null) {
                return List.of(err);
            }
            break;
        }
        return null;
    }

    @Messages({
        "# {0} - variable name",
        "ERR_NeitherReadOrWritten=Variable {0} is neither read or written to",
        "# {0} - variable name",
        "ERR_NotWritten=Variable {0} is never written to",
        "# {0} - variable name",
        "ERR_NotRead=Variable {0} is never read",
        "# {0} - element name",
        "ERR_NotUsed={0} is never used",
        "ERR_NotUsedConstructor=Constructor is never used",
        "# {0} - element name",
        "FIX_RemoveUsedElement=Remove unused \"{0}\"",
        "FIX_RemoveUsedConstructor=Remove unused constructor",
    })
    private static ErrorDescription convertUnused(HintContext ctx, UnusedDescription ud) {
        //TODO: switch expression candidate!
        String name = ud.unusedElement().getSimpleName().toString();
        String message;
        Fix fix = null;
        switch (ud.reason()) {
            case NOT_WRITTEN_READ: message = Bundle.ERR_NeitherReadOrWritten(name);
                fix = JavaFixUtilities.removeFromParent(ctx, Bundle.FIX_RemoveUsedElement(name), ud.unusedElementPath());
                break;
            case NOT_WRITTEN: message = Bundle.ERR_NotWritten(name);
                break;
            case NOT_READ: message = Bundle.ERR_NotRead(name);
                fix = JavaFixUtilities.safelyRemoveFromParent(ctx, Bundle.FIX_RemoveUsedElement(name), ud.unusedElementPath());
                break;
            case NOT_USED:
                if (ud.unusedElement().getKind() == ElementKind.CONSTRUCTOR) {
                    message = Bundle.ERR_NotUsedConstructor();
                    fix = JavaFixUtilities.removeFromParent(ctx, Bundle.FIX_RemoveUsedConstructor(), ud.unusedElementPath());
                } else {
                    message = Bundle.ERR_NotUsed(name);
                    fix = JavaFixUtilities.removeFromParent(ctx, Bundle.FIX_RemoveUsedElement(name), ud.unusedElementPath());
                }
                break;
            default:
                throw new IllegalStateException("Unknown unused type: " + ud.reason());
        }

        List<Fix> fixes = new ArrayList<>();

        if (fix != null) {
            fixes.add(fix);
        }

        if (Feature.UNNAMED_VARIABLES.isEnabled(ctx.getInfo()) &&
            SUPPORT_UNNAMED.contains(ud.unusedElement().getKind())) {
            fixes.add(new RenameToUnderscore(ctx.getInfo(), ctx.getPath()).toEditorFix());
        }

        return ErrorDescriptionFactory.forName(ctx, ud.unusedElementPath(), message, fixes.toArray(Fix[]::new));
    }

    private static final class RenameToUnderscore extends JavaFix {

        public RenameToUnderscore(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        @Messages("FIX_RenameToUnderscore=Rename the variable to '_'")
        protected String getText() {
            return Bundle.FIX_RenameToUnderscore();
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            wc.rewrite(ctx.getPath().getLeaf(), make.setLabel(ctx.getPath().getLeaf(), "_"));
        }

    }
}
