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

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.JComponent;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.modules.java.hints.jdk.ConvertToDiamondBulkHint.CustomizerProviderImpl;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.MatcherUtilities;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_Javac_canUseDiamond", description = "#DESC_Javac_canUseDiamond", id=ConvertToDiamondBulkHint.ID, category="rules15",enabled=true, customizerProvider=CustomizerProviderImpl.class, suppressWarnings="Convert2Diamond",
        minSourceVersion = "7")
public class ConvertToDiamondBulkHint {

    public static final String ID = "Javac_canUseDiamond";
    public static final Set<String> CODES = new HashSet<String>(Arrays.asList("compiler.warn.diamond.redundant.args"));

    //XXX: hack:
    public static boolean isHintEnabled() {
//        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {
//            if (ID.equals(hm.id)) {
//                return HintsSettings.isEnabled(hm);
//            }
//        }

        return true;
    }

    private static final Map<String, Collection<String>> key2Pattern = new LinkedHashMap<String, Collection<String>>();

    static {
        key2Pattern.put("initializer", Arrays.asList("$mods$ $type $name = $init;"));
        key2Pattern.put("assignment", Arrays.asList("$var = $init"));
        key2Pattern.put("return", Arrays.asList("return $init;"));
        key2Pattern.put("argument", Arrays.asList("$site.<$T$>$name($p$, $init, $s$)", "$name($p$, $init, $s$)", "new $type<$T$>($p$, $init, $s$)", "new $type($p$, $init, $s$)"));
        key2Pattern.put("other", Arrays.asList(new String[] {null}));
    }
    
    @TriggerPatterns({
        @TriggerPattern("new $clazz<$tparams$>($params$)")
    })
    public static ErrorDescription compute(HintContext ctx) {
        // hint disabled for var type variable initialization.
        TreePath parentPath = ctx.getPath().getParentPath();
        boolean isVarInit = MatcherUtilities.matches(ctx, parentPath, "$mods$ $type $name = $init;");   //NOI18N
        if (isVarInit) {
            if (ctx.getInfo().getTreeUtilities().isVarType(parentPath)) {
                return null;
            }
        }

        if (ctx.getMultiVariables().get("$tparams$").isEmpty()) return null;
        
        TreePath clazz = ctx.getVariables().get("$clazz");
        long start = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(clazz.getCompilationUnit(), clazz.getLeaf());

        ctx.getVariables().put("$init", ctx.getPath());
        
        OUTER: for (Diagnostic<?> d : ctx.getInfo().getDiagnostics()) {
            if (start != d.getStartPosition()) continue;
            if (!CODES.contains(d.getCode())) continue;

            FOUND: for (Entry<String, Collection<String>> e : key2Pattern.entrySet()) {
                for (String p : e.getValue()) {
                    if (p == null || MatcherUtilities.matches(ctx, ctx.getPath().getParentPath(), p)) {
                        boolean enabled = isEnabled(ctx, e.getKey());

                        if (!enabled) {
                            continue OUTER;
                        } else {
                            break FOUND;
                        } 
                    }
                }
            }
            
            // check that the resolved symbol has no overloads, which would
            // take parametrized supertypes of the arguments
            if (checkAmbiguousOverload(ctx.getInfo(), ctx.getPath())) {
                return null;
            }

            return ErrorDescriptionFactory.forTree(ctx, clazz.getParentPath(), d.getMessage(null), new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix());
        }

        return null;
    }
    
    /**
     * Checks whether an ambiguous overload exists. Javacs prior to JDK9 use imperfect
     * inference for type parameters, which causes more overloads to match the invocation,
     * therefore causing an error during compilation. If the diamond hint was applied
     * in such a case, the result would not be error-highlighted in editor, but would
     * fail to compile using JDK &lt; 9.
     * <p/>
     * The check is very rough, so it should not be used to generate errors as older
     * javacs do.
     * <p/>
     * See defect #248162
     */
    private static boolean checkAmbiguousOverload(CompilationInfo info, TreePath newPath) {
        if (info.getSourceVersion().compareTo(SourceVersion.RELEASE_8) > 0) {
            return false;
        }
        Element el = info.getTrees().getElement(newPath);
        if (el == null || el.getKind() != ElementKind.CONSTRUCTOR) {
            return false;
        }
        ExecutableElement ctor = (ExecutableElement)el;
        DeclaredType resolvedType = (DeclaredType)info.getTrees().getTypeMirror(newPath);
        ExecutableType ctorType = (ExecutableType)info.getTypes().asMemberOf(resolvedType, el);
        for (ExecutableElement ee : ElementFilter.constructorsIn(el.getEnclosingElement().getEnclosedElements())) {
            if (ee == el) {
                continue;
            }
            if (ee.getParameters().size() != ctor.getParameters().size()) {
                continue;
            }
            TypeMirror t = info.getTypes().asMemberOf(resolvedType, ee);
            if (!Utilities.isValidType(t) || t.getKind() != TypeKind.EXECUTABLE) {
                continue;
            }
            ExecutableType et = (ExecutableType)t;
            for (int i = 0; i < ee.getParameters().size(); i++) {
                TypeMirror earg = et.getParameterTypes().get(i);
                TypeMirror carg = ctorType.getParameterTypes().get(i);
                if (!earg.getKind().isPrimitive() && !carg.getKind().isPrimitive()) {
                    TypeMirror erasedC = info.getTypes().erasure(carg);
                    TypeMirror erasedE = info.getTypes().erasure(earg);
                    if (info.getTypes().isAssignable(erasedC, earg) && 
                        !info.getTypes().isSameType(erasedC, erasedE)) {
                        // invalid hint here!
                        return true;
                    }
                }
            }
        }
        return false;
    }

    static final String KEY = "enabledVariants";
    static final String ALL = "argument,assignment,initializer,other,return";
    
    static String getConfiguration(Preferences p) {
        return p.get(KEY, ALL);
    }

    static void putConfiguration(Preferences p, String configuration) {
        p.put(KEY, configuration);
    }

    private static boolean isEnabled(HintContext ctx, String key) {
        return isEnabled(ctx.getPreferences(), key);
    }

    static boolean isEnabled(Preferences p, String key) {
        return ("," + getConfiguration(p) + ",").contains("," + key + ",");
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath path) {
            super(info, path);
        }

        public String getText() {
            return NbBundle.getMessage(ConvertToDiamondBulkHint.class, "FIX_ConvertToDiamond");
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            if (tp.getLeaf().getKind() != Tree.Kind.NEW_CLASS) {
                //XXX: warning
                return ;
            }

            NewClassTree nct = (NewClassTree) tp.getLeaf();

            if (nct.getIdentifier().getKind() != Tree.Kind.PARAMETERIZED_TYPE) {
                //XXX: warning
                return ;
            }

            TreeMaker make = copy.getTreeMaker();
            ParameterizedTypeTree ptt = (ParameterizedTypeTree) nct.getIdentifier();
            ParameterizedTypeTree nue = make.ParameterizedType(ptt.getType(), Collections.<Tree>emptyList());

            copy.rewrite(ptt, nue);
        }

    }

    public static final class CustomizerProviderImpl implements CustomizerProvider {

        @Override public JComponent getCustomizer(Preferences prefs) {
            return new ConvertToDiamondBulkHintPanel(prefs);
        }

    }
}
