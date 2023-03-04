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

package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ErrorFixesFakeHint extends AbstractHint {

    private FixKind kind;

    private ErrorFixesFakeHint(FixKind kind) {
        super(true, false, null);
        this.kind = kind;
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(ErrorFixesFakeHint.class, "DESC_ErrorFixesFakeHint" + kind.name());
    }

    public Set<Kind> getTreeKinds() {
        return EnumSet.noneOf(Kind.class);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo, TreePath treePath) {
        return null;//should not be called
    }

    public String getId() {
        return ErrorFixesFakeHint.class.getName() + kind.name();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ErrorFixesFakeHint.class, "DN_ErrorFixesFakeHint" + kind.name());
    }
    
    public void cancel() {}

    @Override
    public JComponent getCustomizer(Preferences node) {
        JComponent customizer;
        switch (kind) {
            case CREATE_LOCAL_VARIABLE:
                customizer = new LocalVariableFixCustomizer(node);
                setCreateLocalVariableInPlace(node, isCreateLocalVariableInPlace(node));
                break;
            case SURROUND_WITH_TRY_CATCH:
                customizer = new SurroundWithTryCatchLog(node);
                setRethrow(node, isRethrow(node));
                setRethrowAsRuntimeException(node, isRethrowAsRuntimeException(node));
                setPrintStackTrace(node, isPrintStackTrace(node));
                setUseExceptions(node, isUseExceptions(node));
                setUseLogger(node, isUseLogger(node));
                break;
            case CREATE_FINAL_FIELD_CTOR:
                customizer = new FinalFieldsFromCtorCustomiser(node);
                setCreateFinalFieldsForCtor(node, isCreateFinalFieldsForCtor(node));
                break;
            case IMPORT_CLASS:
                customizer = new ImportClassCustomizer(node);
                setOrganizeAfterImportClass(node, isOrganizeAfterImportClass(node));
                break;
            default:
                customizer = super.getCustomizer(node);
                break;
        }
        return customizer;
    }

    public static enum FixKind {
        SURROUND_WITH_TRY_CATCH,
        CREATE_LOCAL_VARIABLE,
        CREATE_FINAL_FIELD_CTOR,
        IMPORT_CLASS;
    }
    
    private static Map<FixKind, ErrorFixesFakeHint> kind2Hint = new  EnumMap<FixKind, ErrorFixesFakeHint>(FixKind.class);
    
    private static synchronized ErrorFixesFakeHint getHint(FixKind kind) {
        ErrorFixesFakeHint h = kind2Hint.get(kind);
        
        if (h == null) {
            kind2Hint.put(kind, h = new ErrorFixesFakeHint(kind));
        }
        
        return h;
    }
    
    public static boolean enabled(FixKind kind) {
        return getHint(kind).isEnabled();
    }
    
    public static boolean enabled(FileObject forFile, FixKind fixKind) {
        Preferences prefs = getPreferences(forFile, fixKind);
        if (prefs == null) {
            return enabled(fixKind);
        }
        String s = prefs.get("enabled", null);
        return s == null  ? enabled(fixKind) : Boolean.parseBoolean(s);
    }
    
    public static Preferences getPreferences(FileObject forFile, FixKind fixKind) {
        return HintsSettings.getSettingsFor(forFile).getHintPreferences(HintMetadata.Builder.create(getHint(fixKind).getId()).build());
    }

    public static boolean isCreateFinalFieldsForCtor(Preferences p) {
        return p.getBoolean(FINAL_FIELDS_FROM_CTOR, true);
    }

    public static void setCreateFinalFieldsForCtor(Preferences p, boolean v) {
        p.putBoolean(FINAL_FIELDS_FROM_CTOR, v);
    }

    public static boolean isCreateLocalVariableInPlace(Preferences p) {
        return p.getBoolean(LOCAL_VARIABLES_INPLACE, true);
    }
    
    public static void setCreateLocalVariableInPlace(Preferences p, boolean v) {
        p.putBoolean(LOCAL_VARIABLES_INPLACE, v);
    }
    
    public static boolean isUseExceptions(Preferences p) {
        return p.getBoolean(SURROUND_USE_EXCEPTIONS, true);
    }
    
    public static void setUseExceptions(Preferences p, boolean v) {
        p.putBoolean(SURROUND_USE_EXCEPTIONS, v);
    }

    public static boolean isPrintStackTrace(Preferences p) {
        return p.getBoolean(SURROUND_PRINT_STACK_TRACE, true);
    }

    public static void setPrintStackTrace(Preferences p, boolean v) {
        p.putBoolean(SURROUND_PRINT_STACK_TRACE, v);
    }

    public static boolean isRethrowAsRuntimeException(Preferences p) {
        return p.getBoolean(SURROUND_RETHROW_AS_RUNTIME, false);
    }

    public static void setRethrowAsRuntimeException(Preferences p, boolean v) {
        p.putBoolean(SURROUND_RETHROW_AS_RUNTIME, v);
    }

    public static boolean isRethrow(Preferences p) {
        return p.getBoolean(SURROUND_RETHROW, false);
    }

    public static void setRethrow(Preferences p, boolean v) {
        p.putBoolean(SURROUND_RETHROW, v);
    }
    
    public static boolean isUseLogger(Preferences p) {
        return p.getBoolean(SURROUND_USE_JAVA_LOGGER, true);
    }
    
    public static void setUseLogger(Preferences p, boolean v) {
        p.putBoolean(SURROUND_USE_JAVA_LOGGER, v);
    }
    
    public static final String LOCAL_VARIABLES_INPLACE = "create-local-variables-in-place"; // NOI18N
    public static final String SURROUND_USE_EXCEPTIONS = "surround-try-catch-org-openide-util-Exceptions"; // NOI18N
    public static final String SURROUND_PRINT_STACK_TRACE = "surround-try-catch-printStackTrace"; // NOI18N
    public static final String SURROUND_RETHROW_AS_RUNTIME = "surround-try-catch-rethrow-runtime"; // NOI18N
    public static final String SURROUND_RETHROW = "surround-try-catch-rethrow"; // NOI18N
    public static final String SURROUND_USE_JAVA_LOGGER = "surround-try-catch-java-util-logging-Logger"; // NOI18N
    public static final String FINAL_FIELDS_FROM_CTOR = "create-final-fields-from-ctor"; // NOI18N
    
    public static final String ORGANIZE_AFTER_IMPORT_CLASS = "organize-import-class"; // NOI18N

    public static void setOrganizeAfterImportClass(Preferences p, boolean v) {
        p.putBoolean(ORGANIZE_AFTER_IMPORT_CLASS, v);
    }

    public static boolean isOrganizeAfterImportClass(Preferences p) {
        return p.getBoolean(ORGANIZE_AFTER_IMPORT_CLASS, false);
    }

    public static ErrorFixesFakeHint create(FileObject file) {
        if (file.getName().endsWith("surround")) {
            return getHint(FixKind.SURROUND_WITH_TRY_CATCH);
        }
        if (file.getName().endsWith("local")) {
            return getHint(FixKind.CREATE_LOCAL_VARIABLE);
        }
        if (file.getName().endsWith("finalfield")) {
            return getHint(FixKind.CREATE_FINAL_FIELD_CTOR);
        }
        if (file.getName().endsWith("importClass")) {
            return getHint(FixKind.IMPORT_CLASS);
        }
        
        throw new IllegalArgumentException();
    }
}
