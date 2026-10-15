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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.ArithmeticUtilities;
import org.netbeans.modules.java.hints.errors.Utilities;
import static org.netbeans.modules.java.hints.errors.Utilities.isValidType;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;

import static org.netbeans.modules.java.hints.suggestions.Bundle.*;
import org.netbeans.spi.java.hints.CustomizerProvider;

/**
 * Checks naming for constants and other fields.
 * @author sdedic
 */
public class ConstantNameHint {
    static final String PREF_MIN_LENGTH = "minLength"; // NOI18N
    static final String PREF_MAX_LENGTH = "maxLength"; // NOI18N
    static final String PREF_CHECK_ONLY_IMMUTABLES = "onlyCheckImmutables"; // NOI18N
    static final String PREF_IMMUTABLE_CLASSES = "immutableClasses"; // NOI18N
    static final String PREF_CONSTANT_NAME_PATTERN = "namePattern"; // NOI18N
    
    private static final String ID = "org.netbeans.modules.java.hints.suggestions.ConstantNameHint"; // NOI18N
    
    // constant name: starts with capital, followed by capital-digit strings, possibly terminated by _. Must end with
    // capital/digit, not _.
    static final String DEFAULT_CONSTANT_NAME_PATTERN = "[A-Z]([A-Z\\d_]*[A-Z\\d])?";
    static final int DEFAULT_MIN_LENGTH = 0;
    static final int DEFAULT_MAX_LENGTH = 35;
    static final boolean DEFAULT_CHECK_ONLY_IMMUTABLES = true;
    
    static final Set<String> IGNORE_CONSTANT_NAMES = new HashSet<>();
    
    static {
        // java serialization constants
        IGNORE_CONSTANT_NAMES.add("serialVersionUID"); // NOI18N
        IGNORE_CONSTANT_NAMES.add("serialPersistentFields"); // NOI18N
    }
    
    @NbBundle.Messages({
        "# {0} - minimum constant name length",
        "ERR_ConstantNameMinLength=Constant name should be at least {0} characters long",
        "# {0} - maximum constant name length",
        "ERR_ConstantNameMaxLength=Constant name should be shorter than {0} characters",
        "# {0} - constant name",
        "ERR_BadConstantName=Constant name does not follow naming conventions: {0}"
    })
    @Hint(
            id = ID,
            category = "naming",
            displayName = "#DN_ConstantNameHint",
            description = "#DESC_ConstantNameHint",
            customizerProvider=CPImpl.class
    )
    @TriggerTreeKind(Tree.Kind.VARIABLE)
    public static ErrorDescription checkConstantName(HintContext ctx) {
        TreePath p = ctx.getPath();
        if (p.getParentPath()== null) {
            return null;
        }
        Tree parent = p.getParentPath().getLeaf();
        if (parent.getKind() != Tree.Kind.CLASS &&
            parent.getKind() != Tree.Kind.INTERFACE &&
            parent.getKind() != Tree.Kind.ENUM
            ) {
            return null;
        }
        // only work on static final fields
        VariableTree vt = (VariableTree) p.getLeaf();
        Set<Modifier> mods = vt.getModifiers().getFlags();
        if (parent.getKind() != Tree.Kind.INTERFACE) {
            if (parent.getKind() == Tree.Kind.ENUM) {
                return null;
            }
            if (!mods.contains(Modifier.FINAL) || !mods.contains(Modifier.STATIC)) {
                return null;
            }
        }
        // ignore specification-defined fields, althgough their names may not satisfy the check
        if (IGNORE_CONSTANT_NAMES.contains(vt.getName().toString())) {
            return null;
        }
        Preferences prefs = ctx.getPreferences();
        
        boolean onlyImmClasses = prefs.getBoolean(PREF_CHECK_ONLY_IMMUTABLES, DEFAULT_CHECK_ONLY_IMMUTABLES);
        if (onlyImmClasses && !isImmutableValue(ctx.getInfo(), p, ctx.getPreferences())) {
            return null;
        }
        // check naming:
        String constantName = vt.getName().toString();
        String patString = prefs.get(PREF_CONSTANT_NAME_PATTERN, DEFAULT_CONSTANT_NAME_PATTERN);
        if ("".equals(patString)) {
            return null;
        }
        try {
            Pattern namePattern = Pattern.compile(patString);
            
            if (!namePattern.matcher(constantName).matches()) {
                return ErrorDescriptionFactory.forName(ctx, vt,
                        ERR_BadConstantName(constantName)
                );
            }
        } catch (PatternSyntaxException ex) {
            // just ignore
        }
        int min = prefs.getInt(PREF_MIN_LENGTH, 0);
        if (min > 0) {
            if (constantName.length() < min) {
                return ErrorDescriptionFactory.forName(ctx, vt, ERR_ConstantNameMinLength(min));
            }
        }
        int max = prefs.getInt(PREF_MAX_LENGTH, 0);
        if (max > 0) {
            if (constantName.length() > max) {
                return ErrorDescriptionFactory.forName(ctx, vt, ERR_ConstantNameMaxLength(max));
            }
        }
        return null;
    }
    
    
    
    private static final Set<String> IMMUTABLE_JDK_CLASSES = new HashSet<>();
    
    static {
        IMMUTABLE_JDK_CLASSES.add("java.lang.String");
        IMMUTABLE_JDK_CLASSES.add("java.awt.Color");
        IMMUTABLE_JDK_CLASSES.add("java.awt.Font");
        IMMUTABLE_JDK_CLASSES.add("java.math.BigDecimal");
        IMMUTABLE_JDK_CLASSES.add("java.math.BigInteger");
        IMMUTABLE_JDK_CLASSES.add("java.io.File");
        IMMUTABLE_JDK_CLASSES.add("java.nio.Path");
        IMMUTABLE_JDK_CLASSES.add("java.io.URI");
        IMMUTABLE_JDK_CLASSES.add("java.util.regex.Pattern");
    }
    
    private static boolean isImmutableType(CompilationInfo info, TypeMirror m, Preferences p) {
        if (m == null) {
            return false;
        }
        if (m.getKind().isPrimitive() || !isValidType(m)) {
            return true;
        }
        if (Utilities.isPrimitiveWrapperType(m)) {
            return true;
        }
        if (m.getKind() != TypeKind.DECLARED) {
            return false;
        }
        Element e = ((DeclaredType)m).asElement();
        if (e == null) {
            return false;
        }
        if (e.getKind() == ElementKind.ENUM) {
            return true;
        }
        if (e.getKind() != ElementKind.CLASS) {
            return false;
        }
        String qn = ((TypeElement)e).getQualifiedName().toString();
        
        if (IMMUTABLE_JDK_CLASSES.contains(qn)) {
            return true;
        }
        List<String> classes = getImmutableTypes(p);
        return classes.contains(qn);
    }
    
    private static boolean isImmutableValue(CompilationInfo info, TreePath val, Preferences p) {
        TypeMirror m = info.getTrees().getTypeMirror(val);
        if (Utilities.isValidType(m) && m.getKind() == TypeKind.ARRAY) {
            return checkZeroSizeArray(info, val);
        } 
        return isImmutableType(info, m, p);
    }
    
    private static boolean checkZeroSizeArray(CompilationInfo info, TreePath val) {
        if (val.getLeaf().getKind() != Tree.Kind.VARIABLE) {
            return false;
        }
        VariableTree vt = (VariableTree)val.getLeaf();
        ExpressionTree xpr = vt.getInitializer();
        if (xpr == null) {
            return false;
        }
        if (xpr.getKind() == Tree.Kind.NEW_ARRAY) {
            NewArrayTree nat = (NewArrayTree)xpr;
            List<? extends ExpressionTree> dims = nat.getDimensions();
            if (dims != null && !dims.isEmpty()) {
                Object size = ArithmeticUtilities.compute(info, 
                        new TreePath(
                            new TreePath(val, xpr),
                            dims.get(dims.size() -1)), 
                        true);
                return ArithmeticUtilities.isRealValue(size) && Integer.valueOf(0).equals(size);
            } else {
                return nat.getInitializers() != null && nat.getInitializers().isEmpty();
            }
        }
        return false;
    }
    
    public static List<String> getImmutableTypes(Preferences p) {
        String val = p.get(PREF_IMMUTABLE_CLASSES, "");
        StringTokenizer tukac = new StringTokenizer(", ", val);
        List<String> res = new ArrayList<>(3);
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if (token.isEmpty()) {
                continue;
            }
            res.add(token);
        }
        return res;
    }
    
    public static void setImmutableTypes(Preferences p, List<String> types) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (String s : types) {
            if (first) {
                sb.append(", ");
            }
            first = false;
            sb.append(s);
        }
        p.put(PREF_IMMUTABLE_CLASSES, sb.toString());
    }
    
    public static class CPImpl implements CustomizerProvider {
        @Override
        public JComponent getCustomizer(Preferences prefs) {
            return new ConstantNameOptions(prefs);
        }
    }
}
