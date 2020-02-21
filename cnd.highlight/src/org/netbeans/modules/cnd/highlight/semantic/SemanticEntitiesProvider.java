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
package org.netbeans.modules.cnd.highlight.semantic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.FontColorProvider;
import org.netbeans.modules.cnd.modelutil.FontColorProvider.Entity;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 */
public final class SemanticEntitiesProvider {

    private final List<SemanticEntity> list;

    public List<SemanticEntity> get() {
        return list;
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=100),
        @ServiceProvider(service = SemanticEntity.class, position=100)
    })
    public static final class InactiveCodeProvider extends AbstractSemanticEntity {
        /*package*/static final String INACTIVE_NAME = "inactive"; // NOI18N
        
        public InactiveCodeProvider() {
            super(FontColorProvider.Entity.INACTIVE_CODE);
        }
        @Override
        public String getName() {
            return INACTIVE_NAME;
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                return ModelUtils.getInactiveCodeBlocks(csmFile, doc, interrupter);
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-inactive"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-inactive-AD"); //NOI18N
        }

        @Override
        public boolean isCsmFileBased() {
            return true;
        }    
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=200),
        @ServiceProvider(service = SemanticEntity.class, position=200)
    })
    public static final class MacrosCodeProvider extends AbstractSemanticEntity {
        /*package*/static final String NAME = "macros"; // NOI18N
        
        private Map<String, AttributeSet> sysMacroColors= new HashMap<>();
        private Map<String, AttributeSet> userMacroColors= new HashMap<>();
        
        public MacrosCodeProvider() {
            super(FontColorProvider.Entity.DEFINED_MACRO);
        }
        @Override
        public String getName() {
            return NAME;
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-macros"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-macros-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                return ModelUtils.getMacroBlocks(csmFile, doc, interrupter);
            } else {
                return Collections.emptyList();
            }
        }
        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmMacro macro = (CsmMacro) ((CsmReference) obj).getReferencedObject();
            if (macro == null){
                return getColor(mimePath);
            }
            switch(macro.getKind()){
                case USER_SPECIFIED:
                    return userMacroColors.get(mimePath);
                case COMPILER_PREDEFINED:
                case POSITION_PREDEFINED:
                    return sysMacroColors.get(mimePath);
                case DEFINED:
                    return getColor(mimePath);
                default:
                    throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
            }
        }
        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            sysMacroColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.SYSTEM_MACRO));
            userMacroColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.USER_MACRO));
        }
        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=300),
        @ServiceProvider(service = SemanticEntity.class, position=300)
    })
    public static final class TypedefsCodeProvider extends AbstractSemanticEntity {
        public TypedefsCodeProvider() {
            super(FontColorProvider.Entity.TYPEDEF);
        }
        @Override
        public String getName() {
            return "typedefs"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-typedefs"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-typedefs-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.TypedefReferenceCollector(interrupter);
        }
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=400),
        @ServiceProvider(service = SemanticEntity.class, position=400)
    })
    public static final class FastClassesCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public FastClassesCodeProvider(){
            super(FontColorProvider.Entity.CLASS);
        }
        @Override
        public String getName() {
            return "fast-calasses-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-classes-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-classes-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<>();
            for(CsmReference ref : references) {
                if (interrupter.cancelled()) {
                    break;
                }
                if (CsmKindUtilities.isClass(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }

        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=500),
        @ServiceProvider(service = SemanticEntity.class, position=500)
    })
    public static final class ClassesCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public ClassesCodeProvider() {
            super(FontColorProvider.Entity.CLASS);
        }
        @Override
        public String getName() {
            return "classes-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-classes-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-classes-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.ClassReferenceCollector(interrupter);
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.CLASS_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=600),
        @ServiceProvider(service = SemanticEntity.class, position=600)
    })
    public static final class FastFieldCodeProvider extends AbstractSemanticEntity {
        public FastFieldCodeProvider() {
            super(FontColorProvider.Entity.CLASS_FIELD);
        }
        @Override
        public String getName() {
            return "fast-class-fields"; // NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<>();
            for(CsmReference ref : references) {
                if (interrupter.cancelled()) {
                    break;
                }
                if (CsmKindUtilities.isField(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-class-fields"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-class-fields-AD"); //NOI18N
        }

        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=700),
        @ServiceProvider(service = SemanticEntity.class, position=700)
    })
    public static final class FieldCodeProvider extends AbstractSemanticEntity {
        public FieldCodeProvider(){
            super(FontColorProvider.Entity.CLASS_FIELD);
        }
        @Override
        public String getName() {
            return "class-fields"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-class-fields"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-class-fields-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.FieldReferenceCollector(interrupter);
        }
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=800),
        @ServiceProvider(service = SemanticEntity.class, position=800)
    })
    public static final class FastFunctionsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public FastFunctionsCodeProvider(){
            super(FontColorProvider.Entity.FUNCTION);
        }
        @Override
        public String getName() {
            return "fast-functions-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-functions-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-functions-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<>();
            for(CsmReference ref : references) {
                if (interrupter.cancelled()) {
                    break;
                }
                if (CsmKindUtilities.isFunction(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }

        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=900),
        @ServiceProvider(service = SemanticEntity.class, position=900)
    })
    public static final class FunctionsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public FunctionsCodeProvider() {
            super(FontColorProvider.Entity.FUNCTION);
        }
        @Override
        public String getName() {
            return "functions-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-functions-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-functions-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.FunctionReferenceCollector(interrupter);
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.FUNCTION_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1000),
        @ServiceProvider(service = SemanticEntity.class, position=1000)
    })
    public static final class FastEnumsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public FastEnumsCodeProvider(){
            super(FontColorProvider.Entity.ENUM);
        }
        @Override
        public String getName() {
            return "fast-enums-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-enums-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-enums-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<>();
            for(CsmReference ref : references) {
                if (interrupter.cancelled()) {
                    break;
                }
                if (CsmKindUtilities.isEnum(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }

        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.ENUM_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1100),
        @ServiceProvider(service = SemanticEntity.class, position=1100)
    })
    public static final class EnumsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public EnumsCodeProvider() {
            super(FontColorProvider.Entity.ENUM);
        }
        @Override
        public String getName() {
            return "enums-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-enums-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-enums-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.EnumReferenceCollector(interrupter);
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.ENUM_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1200),
        @ServiceProvider(service = SemanticEntity.class, position=1200)
    })
    public static final class FastEnumeratorsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public FastEnumeratorsCodeProvider(){
            super(FontColorProvider.Entity.ENUMERATOR);
        }
        @Override
        public String getName() {
            return "fast-enumerators-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-enumerators-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-fast-enumerators-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            Collection<CsmReference> references = CsmReferenceResolver.getDefault().getReferences(csmFile);
            List<CsmOffsetable> res = new ArrayList<>();
            for(CsmReference ref : references) {
                if (interrupter.cancelled()) {
                    break;
                }
                if (CsmKindUtilities.isEnumerator(ref.getReferencedObject())){
                    res.add(ref);
                }
            }
            return res;
        }

        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.ENUMERATOR_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return true;
        }
    }
    
    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1300),
        @ServiceProvider(service = SemanticEntity.class, position=1300)
    })
    public static final class EnumerarorsCodeProvider extends AbstractSemanticEntity {
        private Map<String, AttributeSet> funUsageColors = new HashMap<>();
        
        public EnumerarorsCodeProvider() {
            super(FontColorProvider.Entity.ENUMERATOR);
        }
        @Override
        public String getName() {
            return "enumerators-names"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-enumerators-names"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-enumerators-names-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.EnumeratorReferenceCollector(interrupter);
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimePath) {
            CsmReference ref = (CsmReference) obj;
            // check if we are in the function declaration
            if (ref == null || CsmReferenceResolver.getDefault().isKindOf(ref, CsmReferenceKind.FUNCTION_DECLARATION_KINDS)) {
                return getColor(mimePath);
            } else {
                return funUsageColors.get(mimePath);
            }
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            funUsageColors.put(provider.getMimeType(), getFontColor(provider, FontColorProvider.Entity.ENUMERATOR_USAGE));
        }
        
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    @ServiceProviders({
        @ServiceProvider(path=NamedOption.HIGHLIGTING_CATEGORY, service=NamedOption.class, position=1400),
        @ServiceProvider(service = SemanticEntity.class, position=1400)
    })
    public static final class UnusedVariablesCodeProvider extends AbstractSemanticEntity {
        private final ConcurrentHashMap<String, AttributeSet> unusedToolTipColors= new ConcurrentHashMap<>();
        private final AttributeSet UNUSED_TOOLTIP = AttributesUtilities.createImmutable(
                    EditorStyleConstants.Tooltip,
                    NbBundle.getMessage(SemanticEntitiesProvider.class, "UNUSED_VARIABLE_TOOLTIP")); // NOI18N

        public UnusedVariablesCodeProvider() {
            super(FontColorProvider.Entity.UNUSED_VARIABLES);
        }
        @Override
        public String getName() {
            return "unused-variables"; // NOI18N
        }
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-unused-variables"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(SemanticEntitiesProvider.class, "Show-unused-variables-AD"); //NOI18N
        }
        @Override
        public List<? extends CsmOffsetable> getBlocks(CsmFile csmFile, Document doc, Interrupter interrupter) {
            if (doc.getProperty(CsmMacroExpansion.MACRO_EXPANSION_VIEW_DOCUMENT) == null) {
                return ModelUtils.collect(csmFile, doc, getCollector(doc, interrupter), interrupter);
            } else {
                // TODO: does not work for macro expanded document
                return Collections.emptyList();
            }
        }
        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return new ModelUtils.UnusedVariableCollector(interrupter);
        }

        @Override
        protected AttributeSet getColor(String mimeType) {
            return unusedToolTipColors.get(mimeType);
        }

        @Override
        public void updateFontColors(FontColorProvider provider) {
            super.updateFontColors(provider);
            unusedToolTipColors.put(provider.getMimeType(), AttributesUtilities.createComposite(UNUSED_TOOLTIP, super.getColor(provider.getMimeType())));
        }
        @Override
        public boolean isCsmFileBased() {
            return false;
        }
    }

    private SemanticEntitiesProvider() {
        if (HighlighterBase.MINIMAL) { // for QEs who want to save performance on UI tests
            list = new ArrayList<>();
            list.add(Lookup.getDefault().lookup(SemanticEntity.class));
        } else {
            list = new ArrayList<SemanticEntity>(Lookup.getDefault().lookupAll(SemanticEntity.class));
        } 
    }
    
    private static abstract class AbstractSemanticEntity extends SemanticEntity {

        private final ConcurrentHashMap<String, AttributeSet> color = new ConcurrentHashMap<>();
        private final FontColorProvider.Entity entity;
        private static final AttributeSet cleanUp = AttributesUtilities.createImmutable(
                StyleConstants.Underline, null,
                StyleConstants.StrikeThrough, null,
                StyleConstants.Background, null,
                EditorStyleConstants.WaveUnderlineColor, null,
                EditorStyleConstants.Tooltip, null);

        public AbstractSemanticEntity() {
            this.entity = null;
        }

        public AbstractSemanticEntity(Entity entity) {
            this.entity = entity;
        }

        protected AttributeSet getColor(String mimeType) {
            return color.get(mimeType);
        }
        
        @Override
        public void updateFontColors(FontColorProvider provider) {
            assert entity != null;
            color.put(provider.getMimeType(), getFontColor(provider, entity));
        }

        protected static AttributeSet getFontColor(FontColorProvider provider, FontColorProvider.Entity entity) {
            AttributeSet attributes = AttributesUtilities.createComposite(provider.getColor(entity), cleanUp);
            return attributes;
        }

        @Override
        public AttributeSet getAttributes(CsmOffsetable obj, String mimeType) {
            return color.get(mimeType);
        }

        @Override
        public ReferenceCollector getCollector(Document doc, Interrupter interrupter) {
            return null;
        }

        @Override
        public NamedOption.OptionKind getKind() {
            return NamedOption.OptionKind.Boolean;
        }

        @Override
        public Object getDefaultValue() {
            return true;
        }
        
    }

    // Singleton
    private static class Instantiator {
        static SemanticEntitiesProvider instance = new SemanticEntitiesProvider();
        private Instantiator() {
        }
    }

    public static SemanticEntitiesProvider instance() {
        return Instantiator.instance;
    }
}

