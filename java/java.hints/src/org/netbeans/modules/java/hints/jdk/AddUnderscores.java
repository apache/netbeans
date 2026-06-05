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
package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.hints.jdk.AddUnderscores.CustomizerProviderImpl;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.AddUnderscores", description = "#DESC_org.netbeans.modules.java.hints.jdk.AddUnderscores", id=AddUnderscores.ID, category="rules15", enabled=false, severity=Severity.HINT, customizerProvider=CustomizerProviderImpl.class,
        minSourceVersion = "7")
public class AddUnderscores {
    public static final String ID = "org.netbeans.modules.java.hints.jdk.AddUnderscores";

    @TriggerTreeKind({Kind.INT_LITERAL, Kind.LONG_LITERAL})
    public static ErrorDescription hint(HintContext ctx) {
        TreePath tp = ctx.getPath();
        int end = (int) ctx.getInfo().getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
        int start = (int) ctx.getInfo().getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
        TokenSequence<?> ts = ctx.getInfo().getTokenHierarchy().tokenSequence();
        ts.move(end);
        if (!ts.movePrevious()) return null;
        String literal = ts.token().text().toString();
        StringBuilder tokenPrefix = new StringBuilder();
        
        while (ts.offset() > start) {
            if (!ts.movePrevious()) {
                break;
            }
            if (ts.offset() == start) {
                tokenPrefix.append(ts.token().text().toString());
                break;
            }
        }
        if (literal.contains("_") && !isReplaceLiteralsWithUnderscores(ctx.getPreferences())) {
            return null;
        }
        RadixInfo info = radixInfo(literal);
        if (info.radix == 8) return null;//octals ignored for now
        String normalized = info.constant.replace("_", "");
        int separateCount = getSizeForRadix(ctx.getPreferences(), info.radix);
        StringBuilder split = new StringBuilder();
        int count = separateCount + 1;

        for (int i = normalized.length(); i > 0; i--) {
            if (--count == 0) {
                split.append("_");
                count = separateCount;
            }
            split.append(normalized.charAt(i - 1));
        }

        split.reverse();

        String result = info.prefix + split.toString() + info.suffix;

        if (result.equals(literal)) return null;

        String displayName = NbBundle.getMessage(AddUnderscores.class, "ERR_" + ID);
        Fix f = new FixImpl(ctx.getInfo(), tp, tokenPrefix.toString() + result).toEditorFix();

        return ErrorDescriptionFactory.forTree(ctx, tp, displayName, f);
    }

    public static final String KEY_SIZE_BINARY = "size-binary";
    public static final String KEY_SIZE_DECIMAL = "size-decimal";
    public static final String KEY_SIZE_HEXADECIMAL = "size-hexadecimal";
    public static final String KEY_ALSO_WITH_UNDERSCORES = "also-with-underscores";
    
    static int getSizeForRadix(Preferences prefs, int radix) {
        String key;
        int def;

        switch (radix) {
            case 2: key = KEY_SIZE_BINARY; def = 4; break;
            case 10: key = KEY_SIZE_DECIMAL; def = 3; break;
            case 16: key = KEY_SIZE_HEXADECIMAL; def = 4; break;
            default: throw new IllegalStateException("radix=" + radix);
        }

        return prefs.getInt(key, def);
    }

    static boolean isReplaceLiteralsWithUnderscores(Preferences prefs) {
        return prefs.getBoolean(KEY_ALSO_WITH_UNDERSCORES, false);
    }

    static void setSizeForRadix(Preferences prefs, int radix, int size) {
        String key;

        switch (radix) {
            case 2: key = KEY_SIZE_BINARY; break;
            case 10: key = KEY_SIZE_DECIMAL; break;
            case 16: key = KEY_SIZE_HEXADECIMAL; break;
            default: throw new IllegalStateException("radix=" + radix);
        }

        prefs.putInt(key, size);
    }

    static void setReplaceLiteralsWithUnderscores(Preferences prefs, boolean value) {
        prefs.putBoolean(KEY_ALSO_WITH_UNDERSCORES, value);
    }


    public static RadixInfo radixInfo(String literal) {
        String suffix = "";

        if (literal.endsWith("l") || literal.endsWith("L")) {
            suffix = literal.substring(literal.length() - 1);
            literal = literal.substring(0, literal.length() - 1);
        }

        int currentRadix = 10;
        String prefix = "";

        if (literal.startsWith("0x") || literal.startsWith("0X")) {
            currentRadix = 16;
            prefix = literal.substring(0, 2);
            literal = literal.substring(2);
        } else if (literal.startsWith("0b") || literal.startsWith("0B")) {
            currentRadix = 2;
            prefix = literal.substring(0, 2);
            literal = literal.substring(2);
        } else if (literal.startsWith("0") && literal.length() > 1) {
            currentRadix = 8;
            prefix = literal.substring(0, 1);
            literal = literal.substring(1);
        }

        return new RadixInfo(prefix, literal, suffix, currentRadix);
    }

    public static final class RadixInfo {
        public final String prefix;
        public final String constant;
        public final String suffix;
        public final int radix;

        public RadixInfo(String prefix, String constant, String suffix, int radix) {
            this.prefix = prefix;
            this.constant = constant;
            this.suffix = suffix;
            this.radix = radix;
        }

    }
    
    private static final class FixImpl extends JavaFix {

        private final String target;

        public FixImpl(CompilationInfo info, TreePath tp, String target) {
            super(info, tp);
            this.target = target;
        }


        @Override
        protected String getText() {
            return NbBundle.getMessage(AddUnderscores.class, "FIX_" + ID, target);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            wc.rewrite(tp.getLeaf(), wc.getTreeMaker().Identifier(target));
        }

    }

    public static final class CustomizerProviderImpl implements CustomizerProvider {

        @Override public JComponent getCustomizer(Preferences prefs) {
            JComponent customizer = new AddUnderscoresPanel(prefs);
            prefs.putInt(KEY_SIZE_BINARY, getSizeForRadix(prefs, 2));
            prefs.putInt(KEY_SIZE_DECIMAL, getSizeForRadix(prefs, 10));
            prefs.putInt(KEY_SIZE_HEXADECIMAL, getSizeForRadix(prefs, 16));
            prefs.putBoolean(KEY_ALSO_WITH_UNDERSCORES, isReplaceLiteralsWithUnderscores(prefs));
            return customizer;
        }

    }
}
