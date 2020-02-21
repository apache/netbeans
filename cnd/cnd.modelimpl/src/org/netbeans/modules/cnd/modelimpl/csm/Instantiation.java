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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl.InstantiationParametersInfo;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.CndCollectionUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Pair;

/**
 * Instantiations.
 *
 */
public abstract class Instantiation<T extends CsmOffsetableDeclaration> extends OffsetableIdentifiableBase<CsmInstantiation> implements CsmOffsetableDeclaration, CsmInstantiation, CsmIdentifiable {
    
    // How much times it is possible io instantiate something using the same template
    private static final int MAX_RECURSIVE_INSTANTIATIONS = 10;     
    
    private static final int MAX_INHERITANCE_DEPTH = 20;    
    
    private static final Logger LOG = Logger.getLogger(Instantiation.class.getSimpleName());

    protected final T declaration;
    protected final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping;
    protected int hashCode = 0;

    private Instantiation(T declaration, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        super(declaration.getContainingFile(), declaration.getStartOffset(), declaration.getEndOffset());
        this.declaration = declaration;
        this.mapping = mapping;
    }

//    @Override
//    public int getStartOffset() {
//        return declaration.getStartOffset();
//    }
//    
//    @Override
//    public int getEndOffset() {
//        return declaration.getEndOffset();
//    }

    // FIX for 146522, we compare toString value until better solution is found
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CsmObject)) {
            return false;
        }
        CsmObject csmobj = (CsmObject) obj;
        if (!CsmKindUtilities.isInstantiation(csmobj)) {
            return false;
        }
        CsmInstantiation inst = (CsmInstantiation) csmobj;
        if (inst instanceof Instantiation) {
            if (this.hashCode != ((Instantiation)inst).hashCode && 
                    (this.hashCode != 0 && ((Instantiation)inst).hashCode != 0)) {
                return false;
            }
        }
        if (!CndCollectionUtils.equals(this.getMapping(), inst.getMapping())) {
            return false;
        }
        return this.getTemplateDeclaration().equals(inst.getTemplateDeclaration());

//        if (CsmKindUtilities.isInstantiation(csmobj)) {
//            return getFullName().equals(((Instantiation)csmobj).getFullName());
//        } else if (CsmKindUtilities.isTemplate(csmobj) ||
//                   CsmKindUtilities.isTemplateInstantiation(csmobj)) {
//            return this.getUniqueName().equals(((CsmDeclaration)csmobj).getUniqueName());
//        }
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int hash = 3;
            hash = 31 * hash + (this.declaration != null ? this.declaration.hashCode() : 0);
            hash = 31 * hash + (this.mapping != null ? CndCollectionUtils.hashCode(this.mapping) : 0);
            hashCode = hash;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(), 0);
    }
    
    private String toString(StringBuilder out, int indent) {
        indent(out, indent).append("INSTANTIATION OF "); // NOI18N
        String instName = this.getClass().getSimpleName()+"@"+System.identityHashCode(this); // NOI18N
        out.append(instName).append(":\n");// NOI18N
        if (declaration instanceof Instantiation) {
            ((Instantiation)declaration).toString(out, indent + 2);
        } else {
            indent(out, indent + 2);
            out.append(declaration);
        }
        out.append("\n");// NOI18N
        if (!mapping.isEmpty()) {
            indent(out, indent).append("WITH MAPPING:\n");// NOI18N
            for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entrySet()) {
                indent(out, indent).append("[").append(entry.getKey()).append("]=>{"); // NOI18N
                out.append(entry.getValue()).append("}\n"); // NOI18N
            }
        }
        indent(out, indent).append("END OF ").append(instName);// NOI18N
        return out.toString();
    }
        
    protected static StringBuilder indent(StringBuilder b, int level) {
        for (int i = 0; i < level; i++) {
            b.append(' '); // NOI18N
        }
        return b;
    }
    
    private CsmClassForwardDeclaration findCsmClassForwardDeclaration(CsmScope scope, CsmClass cls) {
        if (scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                CsmFile file = (CsmFile) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    if (((CsmClassForwardDeclaration) decl).getCsmClass().equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(file, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) scope;
                CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION);
                Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    if (((CsmClassForwardDeclaration) decl).getCsmClass().equals(cls)) {
                        return (CsmClassForwardDeclaration) decl;
                    }
                }
                filter = CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                declarations = CsmSelect.getDeclarations(nsd, filter);
                for (Iterator<CsmOffsetableDeclaration> it = declarations; it.hasNext();) {
                    CsmOffsetableDeclaration decl = it.next();
                    CsmClassForwardDeclaration fdecl = findCsmClassForwardDeclaration((CsmNamespaceDefinition) decl, cls);
                    if (fdecl != null) {
                        return fdecl;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public T getTemplateDeclaration() {
        return declaration;
    }

    @Override
    public Map<CsmTemplateParameter, CsmSpecializationParameter> getMapping() {
        return mapping;
    }

    @Override
    public boolean isValid() {
        return CsmBaseUtilities.isValid(declaration);
    }

    public static CsmObject create(CsmTemplate template, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
//        System.err.println("Instantiation.create for " + template + " with mapping " + mapping);
        if (canSkipInstantiation(template, mapping)) {
            return template;
        }
        if (template instanceof CsmClass) {
            Class newClass = new Class((CsmClass)template, mapping);
            if(UIDProviderIml.isPersistable(newClass.getUID())) {
                CsmFile file = newClass.getContainingFile();
                if(file instanceof FileImpl) {
                    ((FileImpl)file).addInstantiation(newClass);
                }
            }
            return newClass;
        } else if (CsmKindUtilities.isFunctionPointerClassifier(template)) {
            // Function pointer
            return new FunctionPointerClassifier((CsmFunctionPointerClassifier) template, mapping);
        } else if (template instanceof CsmConstructor) {
            return new Constructor((CsmConstructor)template, mapping);
        } else if (template instanceof CsmMethod) {
            return new Method((CsmMethod)template, mapping);            
        } else if (template instanceof CsmFunction) {
            return new Function((CsmFunction)template, mapping);
        } else if (template instanceof CsmField) {
            return new Field((CsmField) template, mapping);
        } else if (template instanceof CsmVariable) {
            return new Variable((CsmVariable) template, mapping);
        } else if (template instanceof CsmTypeAlias) {
            CsmTypeAlias alias = (CsmTypeAlias) template;
            return CsmKindUtilities.isClassMember(alias) ? new MemberTypeAlias(alias, mapping) : new TypeAlias(alias, mapping);
        } else {
            if (CndUtils.isDebugMode()) {
                CndUtils.assertTrueInConsole(false, "Unknown class " + template.getClass() + " for template instantiation:" + template); // NOI18N
            }
        }
        return template;
    }    
    
    /**
     * Method ensures that instantiating template with the given mapping makes sense.
     * Most likely, it shouldn't be used in future when we'll instantiate 
     * objects with the right mappings (now there could be duplicates).
     * 
     * @param template
     * @param mapping
     * @return true if template shouldn't be instantiated, false otherwise
     */
    private static boolean canSkipInstantiation(CsmObject template, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {        
        if (mapping == null || mapping.isEmpty()) {
            return true;
        }
        
        CsmObject current = (template instanceof Inheritance) ? ((Inheritance) template).getAncestorType() : template;
        
        int depth = 0;
        
        while (CsmKindUtilities.isInstantiation(current) || current instanceof Type) {
            ++depth;
            
            Map<CsmTemplateParameter, CsmSpecializationParameter> origMapping = null;
            
            if (CsmKindUtilities.isInstantiation(current)) {
                origMapping = ((CsmInstantiation) current).getMapping();
                current = ((CsmInstantiation) current).getTemplateDeclaration();
            } else if (current instanceof Type) {
                origMapping = ((Type) current).getInstantiation().getMapping();
                current = ((Type) current).originalType;
            } else {
                break; // paranoia
            }
            
            // If instances are the same, then it is erroneous instantiation
            if (origMapping == mapping) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "REFUSE TO INSTANTITATE:\n{0}\n", new Object[] {template}); //NOI18N
                }
                return true;
            }
        }
        
        // Check if there is a recursion here
        if (depth > MAX_RECURSIVE_INSTANTIATIONS) {
            if (isRecursiveInstantiation(template, mapping, MAX_RECURSIVE_INSTANTIATIONS)) {
                return true;
            }
        }
                
        return false;
    }
    
    /**
     * Method ensures that instantiating of a template doesn't cause a recursion.
     * Repeating pattern in instantiations in most cases means recursion.
     * 
     * @param template to instantiate
     * @param mapping with which instantiation should happen. Can be null.
     * @return true if template shouldn't be instantiated, false otherwise
     */
    private static boolean isRecursiveInstantiation(CsmObject template, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping, final int recursionLimit) {                
        CsmObject current = (template instanceof Inheritance) ? ((Inheritance) template).getAncestorType() : template;
        
        Map<TemplateMapKey, Integer> repeatings = new HashMap<>();
        
        if (mapping != null) {
            repeatings.put(new TemplateMapKey(mapping), 1);
        }
        
        while (CsmKindUtilities.isInstantiation(current) || current instanceof Type) {
            Map<CsmTemplateParameter, CsmSpecializationParameter> origMapping = null;
            
            if (CsmKindUtilities.isInstantiation(current)) {
                origMapping = ((CsmInstantiation) current).getMapping();
                current = ((CsmInstantiation) current).getTemplateDeclaration();
            } else if (current instanceof Type) {
                origMapping = ((Type) current).getInstantiation().getMapping();
                current = ((Type) current).originalType;
            } else {
                return false; // paranoia
            }
            
            TemplateMapKey mapKey = new TemplateMapKey(origMapping);
            Integer counter = repeatings.get(mapKey);
            if (counter != null) {
                if (counter < recursionLimit) {
                    counter += 1;
                } else {
                    return true;
                }
            } else {
                counter = 1;
            }
            repeatings.put(mapKey, counter);
        }
                
        return false;
    }    
    
    // TODO: consider if we should check CsmSpecializationParameters as well
    private static final class TemplateMapKey {
                
        private final Map<CsmTemplateParameter, CsmSpecializationParameter> map;

        public TemplateMapKey(Map<CsmTemplateParameter, CsmSpecializationParameter> map) {
            this.map = map;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            for (CsmTemplateParameter templateParam : map.keySet()) {
                hash = 79 * hash + Objects.hashCode(templateParam);
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TemplateMapKey other = (TemplateMapKey) obj;
            if (map.size() != other.map.size()) {
                return false;
            }
            for (CsmTemplateParameter templateParam : map.keySet()) {
                if (!other.map.containsKey(templateParam)) {
                    return false;
                }
            }
            return true;
        }
        
    }
    
    @Override
    public CsmFile getContainingFile() {
        return getTemplateDeclaration().getContainingFile();
    }

    @Override
    public CharSequence getText() {
        return getTemplateDeclaration().getText();
    }

    @Override
    public Kind getKind() {
        return getTemplateDeclaration().getKind();
    }

    @Override
    public CharSequence getUniqueName() {
        return getTemplateDeclaration().getUniqueName();
    }

    @Override
    public CharSequence getQualifiedName() {
        return getTemplateDeclaration().getQualifiedName();
    }

    @Override
    public CharSequence getName() {
        return getTemplateDeclaration().getName();
    }

    @Override
    public CsmScope getScope() {
        return getTemplateDeclaration().getScope();
    }

    @Override
    protected CsmUID<?> createUID() {
        return createInstantiationUID(this);
    }

    public static <T extends CsmInstantiation> CsmUID<?> createInstantiationUID(CsmInstantiation inst) {
        if(CsmKindUtilities.isClass(inst) && inst.getTemplateDeclaration() instanceof ClassImpl) {
            final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = inst.getMapping();        
            boolean persistable = !mapping.isEmpty();
            if (persistable) {
                for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> param : mapping.entrySet()) {
                    CsmSpecializationParameter specParam = param.getValue();
                    if(CsmKindUtilities.isTypeBasedSpecalizationParameter(specParam)) {
                        if (!PersistentUtils.isPersistable(((CsmTypeBasedSpecializationParameter)specParam).getType())) {
                            persistable = false;
                            break;
                        } else if (!isScopePersistable(specParam.getScope())) {
                            persistable = false;
                            break;
                        }
                    } else {
                        persistable = false;
                        break;
                    }
                }
            }
            if (persistable) {
                return UIDUtilities.createInstantiationUID(inst);
            }
        }
        return new InstantiationSelfUID((Instantiation)inst);
    }
    
    private static boolean isScopePersistable(CsmScope scope) {
        if (scope != null) {
            if (scope instanceof CsmIdentifiable) {
                return UIDProviderIml.isPersistable(((CsmIdentifiable) scope).getUID());
            }
            return false;
        }
        return true; // null could be persisted
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert (declaration instanceof ClassImpl);
        
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(UIDCsmConverter.declarationToUID(declaration), output);

        List<CsmUID<CsmTemplateParameter>> keys = new ArrayList<>();
        List<CsmSpecializationParameter> vals = new ArrayList<>();
        for (CsmTemplateParameter key : mapping.keySet()) {
            keys.add(UIDCsmConverter.declarationToUID(key));
            vals.add(mapping.get(key));
        }
        factory.writeUIDCollection(keys, output, true);
        PersistentUtils.writeSpecializationParameters(vals, output);
    }

    public Instantiation(RepositoryDataInput input) throws IOException {
        super(input);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        
        CsmUID<T> declUID = factory.readUID(input);
        declaration = declUID.getObject();
        
        List<CsmUID<CsmTemplateParameter>> keys = new ArrayList<>();
        List<CsmSpecializationParameter> vals = new ArrayList<>();
        
        factory.readUIDCollection(keys, input);
        PersistentUtils.readSpecializationParameters(vals, input);
        
        mapping = new HashMap<>();
        for (int i = 0; i < keys.size() && i < vals.size(); i++) {
            mapping.put(keys.get(i).getObject(), vals.get(i));
        }
    }            
    
    //////////////////////////////
    ////////////// STATIC MEMBERS
    public static class Class extends Instantiation<CsmClass> implements CsmClass, CsmMember, CsmTemplate,
                                    SelectImpl.FilterableMembers {
        private volatile List<CsmInheritance> inheritances;
        public Class(CsmClass clazz, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(clazz, mapping);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public Collection<CsmTypedef> getEnclosingTypedefs() {
            return declaration.getEnclosingTypedefs();
        }

        @Override
        public Collection<CsmVariable> getEnclosingVariables() {
            return declaration.getEnclosingVariables();
        }

        @Override
        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isSpecialization() {
            return ((CsmTemplate) declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTemplate) declaration).isExplicitSpecialization();
        }
        
        private boolean isRecursion(CsmTemplate type, int i){
            if (i == 0) {
                return true;
            }
            if (type instanceof Class) {
                Class t = (Class) type;
                return isRecursion((CsmTemplate)t.declaration, i-1);
            }
            return false;
        }

        private CsmMember createMember(CsmMember member) {
            if (member instanceof CsmField) {
                return new Field((CsmField)member, this);
            } else if (member instanceof CsmConstructor) {
                return new Constructor((CsmConstructor)member, this);
            } else if (member instanceof CsmMethod) {
                return new Method((CsmMethod)member, this);
            } else if (member instanceof CsmTypeAlias) {
                return new MemberTypeAlias((CsmTypeAlias)member, this);
            } else if (member instanceof CsmTypedef) {
                return new Typedef((CsmTypedef)member, this);
            } else if (member instanceof CsmClass) {
                Class newClass = new Class((CsmClass)member, getMapping());
                if(UIDProviderIml.isPersistable(newClass.getUID())) {
                    CsmFile file = newClass.getContainingFile();
                    if(file instanceof FileImpl) {
                        ((FileImpl)file).addInstantiation(newClass);
                    }
                }
                return newClass;
            } else if (member instanceof CsmClassForwardDeclaration) {
                return new ClassForward((CsmClassForwardDeclaration)member, getMapping());
            } else if (member instanceof CsmEnumForwardDeclaration) {
                return new EnumForward((CsmEnumForwardDeclaration)member, getMapping());
            } else if (member instanceof CsmEnum) {
                // no need to instantiate enums?
                return member;
            } else if (member instanceof CsmUsingDeclaration) {
                return new UsingDeclaration((CsmUsingDeclaration) member, this);
            }
            assert false : "Unknown class for member instantiation:" + member + " of class:" + member.getClass(); // NOI18N
            return member;
        }

        @Override
        public Collection<CsmMember> getMembers() {
            Collection<CsmMember> res = new ArrayList<>();
            for (CsmMember member : declaration.getMembers()) {
                res.add(createMember(member));
            }
            return res;
        }

        @Override
        public Iterator<CsmMember> getMembers(CsmFilter filter) {
            Collection<CsmMember> res = new ArrayList<>();
            Iterator<CsmMember> it = CsmSelect.getClassMembers(declaration, filter);
            while(it.hasNext()){
                res.add(createMember(it.next()));
            }
            return res.iterator();
        }

        @Override
        public int getLeftBracketOffset() {
            return declaration.getLeftBracketOffset();
        }

        @Override
        public Collection<CsmFriend> getFriends() {
            return declaration.getFriends();
        }

        @Override
        public Collection<CsmInheritance> getBaseClasses() {
            if (inheritances == null) {
                List<CsmInheritance> res = new ArrayList<>(1);
                for (CsmInheritance inh : declaration.getBaseClasses()) {
                    if (canSkipInstantiation(inh, mapping)) {
                        res.add(inh);
                    } else {
                        res.add(new Inheritance(inh, this));
                    }
                }
                inheritances = res.isEmpty() ? Collections.<CsmInheritance>emptyList() : res;
            }
            return inheritances;
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTemplate)declaration).getDisplayName();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate)declaration).getTemplateParameters();
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
        }

        public Class(RepositoryDataInput input) throws IOException {
            super(input);
        }        
        
    }

    private static class Inheritance implements CsmInheritance {
        private final CsmInheritance inheritance;
        private final CsmType type;
        private CsmClassifier resolvedClassifier;

        public Inheritance(CsmInheritance inheritance, Instantiation instantiation) {
            this.inheritance = inheritance;
            this.type = createType(inheritance.getAncestorType(), instantiation);
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Inheritance for\n{0}\n=>INHERITANCE TYPE=>\n{1}", new Object[] {this, type});
            }
        }

        @Override
        public CsmType getAncestorType() {
            return type;
        }

        @Override
        public CharSequence getText() {
            return inheritance.getText();
        }

        @Override
        public Position getStartPosition() {
            return inheritance.getStartPosition();
        }

        @Override
        public int getStartOffset() {
            return inheritance.getStartOffset();
        }

        @Override
        public Position getEndPosition() {
            return inheritance.getEndPosition();
        }

        @Override
        public int getEndOffset() {
            return inheritance.getEndOffset();
        }

        @Override
        public CsmFile getContainingFile() {
            return inheritance.getContainingFile();
        }

        @Override
        public boolean isVirtual() {
            return inheritance.isVirtual();
        }

        @Override
        public CsmVisibility getVisibility() {
            return inheritance.getVisibility();
        }

        @Override
        public CsmClassifier getClassifier() {
            if (resolvedClassifier == null) {
                CsmType t= getAncestorType();
                resolvedClassifier = t.getClassifier();                        
            }
            return resolvedClassifier;
        }

        @Override
        public CsmScope getScope() {
            return inheritance.getScope();
        }
        
        @Override
        public String toString() {
            return toString(new StringBuilder(), 0);
        }
        
        private String toString(StringBuilder out, int indent) {
            String instName = this.getClass().getSimpleName()+"@"+System.identityHashCode(this);//NOI18N
            indent(out, indent)
                    .append("INSTANTIATION OF INHERITANCE ") // NOI18N  
                    .append(instName)
                    .append("WITH TYPE:\n"); // NOI18N
            ////////////////////////////////////////////////////////////////////
            // Commented out because the only valuable thing here is type
//            out.append(instName).append(":\n");// NOI18N
//            if (inheritance instanceof Inheritance) {
//                ((Inheritance) inheritance).toString(out, indent + 2);
//            } else {
//                indent(out, indent + 2);
//                out.append(inheritance);
//            }
            ////////////////////////////////////////////////////////////////////
            ((Type)type).toString(out, indent + 2);
            out.append("\n");// NOI18N
            return out.toString();
        }
    }

    private static class Function extends Instantiation<CsmFunction> implements CsmFunction, CsmTemplate {
        private final CsmType retType;

        public Function(CsmFunction function, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(function, mapping);
            this.retType = createType(function.getReturnType(), Function.this);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isInline() {
            return declaration.isInline();
        }

        @Override
        public boolean isOperator() {
            return declaration.isOperator();
        }

        @Override
        public OperatorKind getOperatorKind() {
            return declaration.getOperatorKind();
        }

        @Override
        public CharSequence getSignature() {
            return declaration.getSignature();
        }

        @Override
        public CsmType getReturnType() {
            return retType;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            ArrayList<CsmParameter> res = new ArrayList<>();
            Collection<CsmParameter> parameters = declaration.getParameterList().getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            res.trimToSize();
            return FunctionParameterListImpl.create(declaration.getParameterList(), res);
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<>();
            Collection<CsmParameter> parameters = declaration.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CsmFunction getDeclaration() {
            return declaration.getDeclaration();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public boolean isSpecialization() {
            return ((CsmTemplate)declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTemplate)declaration).isExplicitSpecialization();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate)declaration).getTemplateParameters();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTemplate)declaration).getDisplayName();
        }
    }
    
    private static class FunctionPointerClassifier extends Instantiation<CsmFunctionPointerClassifier> implements CsmFunctionPointerClassifier, CsmTemplate {
        
        private final CsmType retType;
        
        public FunctionPointerClassifier(CsmFunctionPointerClassifier function, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(function, mapping);
            this.retType = createType(function.getReturnType(), FunctionPointerClassifier.this);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public CharSequence getSignature() {
            return declaration.getSignature();
        }

        @Override
        public CsmType getReturnType() {
            return retType;
        }
        @Override
        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<>();
            Collection<CsmParameter> parameters = declaration.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }
        
        @Override
        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }        

        @Override
        public boolean isSpecialization() {
            return ((CsmTemplate)declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTemplate)declaration).isExplicitSpecialization();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate)declaration).getTemplateParameters();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTemplate)declaration).getDisplayName();
        }
    }
    
    private static class Variable extends Instantiation<CsmVariable> implements CsmVariable {
        
        private final CsmType type;
        

        public Variable(CsmVariable var, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(var, mapping);
            this.type = createType(var.getType(), Variable.this);
        }

        @Override
        public boolean isExtern() {
            return declaration.isExtern();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmExpression getInitialValue() {
            return declaration.getInitialValue();
        }

        @Override
        public CharSequence getDisplayText() {
            return declaration.getDisplayText();
        }

        @Override
        public CsmVariableDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }
    }

    private static class Field extends Instantiation<CsmField> implements CsmField {
        private final CsmType type;
        
        public Field(CsmField field, CsmInstantiation instantiation) {
            this(field, instantiation.getMapping());
        }

        public Field(CsmField field, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(field, mapping);
            this.type = createType(field.getType(), Field.this);
        }

        @Override
        public boolean isExtern() {
            return declaration.isExtern();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmExpression getInitialValue() {
            return declaration.getInitialValue();
        }

        @Override
        public CharSequence getDisplayText() {
            return declaration.getDisplayText();
        }

        @Override
        public CsmVariableDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isStatic() {
            return declaration.isStatic();
        }

        @Override
        public CsmVisibility getVisibility() {
            return declaration.getVisibility();
        }

        @Override
        public CsmClass getContainingClass() {
            return declaration.getContainingClass();
        }
    }

    private static class Typedef extends Instantiation<CsmTypedef> implements CsmTypedef, CsmMember {
        private final CsmType type;
        
        public Typedef(CsmTypedef typedef, CsmInstantiation instantiation) {
            super(typedef, instantiation.getMapping());
            this.type = createType(typedef.getType(), instantiation);
        }

        @Override
        public boolean isTypeUnnamed() {
            return declaration.isTypeUnnamed();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }
    }
    
    private static class UsingDeclaration extends Instantiation<CsmUsingDeclaration> implements CsmUsingDeclaration, CsmMember {

        public UsingDeclaration(CsmUsingDeclaration usingDeclaration, CsmInstantiation instantiation) {
            super(usingDeclaration, instantiation.getMapping());
        }

        @Override
        public CsmDeclaration getReferencedDeclaration() {
            CsmDeclaration referenced = getTemplateDeclaration().getReferencedDeclaration();
            // TODO: check if isTemplateScope(referenced.getScope())?
            if (referenced instanceof CsmTemplate) {
                return (CsmDeclaration) Instantiation.create((CsmTemplate) referenced, getMapping());
            }
            return referenced;
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }        
    }
            
    private static class TypeAlias extends Instantiation<CsmTypeAlias> implements CsmTypeAlias {
        
        private final CsmType type;

        public TypeAlias(CsmTypeAlias typeAlias, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(typeAlias, mapping);
            this.type = createType(typeAlias.getType(), TypeAlias.this);
        }
        
        public TypeAlias(CsmTypeAlias typeAlias, CsmInstantiation instantiation) {
            super(typeAlias, instantiation.getMapping());
            this.type = createType(typeAlias.getType(), instantiation);
        }

        @Override
        public boolean isTypeUnnamed() {
            return declaration.isTypeUnnamed();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public boolean isTemplate() {
            return ((CsmTypeAlias)declaration).isTemplate();
        }

        @Override
        public boolean isSpecialization() {
            return ((CsmTypeAlias)declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTypeAlias)declaration).isExplicitSpecialization();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTypeAlias)declaration).getTemplateParameters();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTypeAlias)declaration).getDisplayName();
        }
    }    
    
    private static class MemberTypeAlias extends TypeAlias implements CsmMember {
        
        public MemberTypeAlias(CsmTypeAlias typeAlias, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(typeAlias, mapping);
            assert CsmKindUtilities.isClassMember(typeAlias) : "Attempt to instantiate member typealias from " + typeAlias; //NOI18N
        }
        
        public MemberTypeAlias(CsmTypeAlias typeAlias, CsmInstantiation instantiation) {
            super(typeAlias, instantiation.getMapping());
            assert CsmKindUtilities.isClassMember(typeAlias) : "Attempt to instantiate member typealias from " + typeAlias; //NOI18N;
        }        
        
        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }        
    }
      
    private static class ClassForward extends Instantiation<CsmClassForwardDeclaration> implements CsmClassForwardDeclaration, CsmMember {
        private CsmClass csmClass = null;

        public ClassForward(CsmClassForwardDeclaration forward, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(forward, mapping);
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember)declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember)declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember)declaration).isStatic();
        }

        @Override
        public CsmClass getCsmClass() {
            if (csmClass == null) {
                CsmClass declClassifier = declaration.getCsmClass();
                if (CsmKindUtilities.isTemplate(declClassifier)) {
                    csmClass = (CsmClass)Instantiation.create((CsmTemplate)declClassifier, getMapping());
                } else {
                    csmClass = declClassifier;
                }
            }
            return csmClass;
        }
    }

    private static class EnumForward extends Instantiation<CsmEnumForwardDeclaration> implements CsmEnumForwardDeclaration, CsmMember {

        private CsmEnum csmEnum = null;

        public EnumForward(CsmEnumForwardDeclaration forward, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(forward, mapping);
        }

        @Override
        public CsmClass getContainingClass() {
            return ((CsmMember) declaration).getContainingClass();
        }

        @Override
        public CsmVisibility getVisibility() {
            return ((CsmMember) declaration).getVisibility();
        }

        @Override
        public boolean isStatic() {
            return ((CsmMember) declaration).isStatic();
        }

        @Override
        public CsmEnum getCsmEnum() {
            if (csmEnum == null) {
                CsmEnum declClassifier = declaration.getCsmEnum();
                if (CsmKindUtilities.isTemplate(declClassifier)) {
                    csmEnum = (CsmEnum) Instantiation.create((CsmTemplate) declClassifier, getMapping());
                } else {
                    csmEnum = declClassifier;
                }
            }
            return csmEnum;
        }
    }

    private static class Method extends Instantiation<CsmMethod> implements CsmMethod, CsmFunctionDefinition, CsmTemplate {
        private CsmType retType;
        private CsmFunctionDefinition definition = null;
        private CsmClass containingClass = null;
        
        public Method(CsmMethod method, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(method, mapping);
        }

        public Method(CsmMethod method, CsmInstantiation instantiation) {
            super(method, instantiation.getMapping());
        }

        @Override
        public DefinitionKind getDefinitionKind() {
            if (CsmKindUtilities.isFunctionDefinition(declaration)) {
                return ((CsmFunctionDefinition)declaration).getDefinitionKind();
            }
            return null;
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return declaration.getScopeElements();
        }

        @Override
        public boolean isStatic() {
            return declaration.isStatic();
        }

        @Override
        public CsmVisibility getVisibility() {
            return declaration.getVisibility();
        }

        @Override
        public CsmClass getContainingClass() {
            if(containingClass == null) {
                containingClass = _getContainingClass();
            }
            return containingClass;
        }
        
        public CsmClass _getContainingClass() {
            CsmClass containingClass = declaration.getContainingClass();
            if(CsmKindUtilities.isTemplate(containingClass)) {
                CsmInstantiationProvider p = CsmInstantiationProvider.getDefault();
                if (p instanceof InstantiationProviderImpl) {
                    CsmObject inst = ((InstantiationProviderImpl) p).instantiate((CsmTemplate)containingClass, this);
                    if (inst instanceof CsmClass) {
                        return (CsmClass) inst;
                    }
                }
            }
            return containingClass;
        }

        @Override
        public boolean isSpecialization() {
            return ((CsmTemplate) declaration).isSpecialization();
        }

        @Override
        public boolean isExplicitSpecialization() {
            return ((CsmTemplate) declaration).isExplicitSpecialization();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return ((CsmTemplate) declaration).getTemplateParameters();
        }

        @Override
        public CharSequence getDisplayName() {
            return ((CsmTemplate) declaration).getDisplayName();
        }

        @Override
        public boolean isTemplate() {
            return ((CsmTemplate)declaration).isTemplate();
        }

        @Override
        public boolean isInline() {
            return declaration.isInline();
        }

        @Override
        public CharSequence getSignature() {
            return declaration.getSignature();
        }

        @Override
        public CsmType getReturnType() {
            if (retType == null) {
                retType = createType(declaration.getReturnType(), this);
            }
            return retType;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            Collection<CsmParameter> res = new ArrayList<>();
            Collection<CsmParameter> parameters = ((CsmFunction) declaration).getParameterList().getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return FunctionParameterListImpl.create(((CsmFunction) declaration).getParameterList(), res);
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            Collection<CsmParameter> res = new ArrayList<>();
            Collection<CsmParameter> parameters = declaration.getParameters();
            for (CsmParameter param : parameters) {
                res.add(new Parameter(param, this));
            }
            return res;
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            if(definition == null) {
                definition = _getDefinition();
            }
            return definition;
        }

        public CsmFunctionDefinition _getDefinition() {
            CsmClass cls = getContainingClass();
            if (CsmKindUtilities.isSpecialization(cls) && declaration instanceof FunctionImpl) {
                FunctionImpl decl = (FunctionImpl) declaration;
                return decl.getDefinition(cls);
            }
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isVirtual() {
            return declaration.isVirtual();
        }

        @Override
        public boolean isOverride() {
            return declaration.isOverride();
        }

        @Override
        public boolean isFinal() {
            return declaration.isFinal();
        }

        @Override
        public boolean isExplicit() {
            return declaration.isExplicit();
        }

        @Override
        public boolean isConst() {
            return declaration.isConst();
        }

        @Override
        public boolean isVolatile() {
            return declaration.isVolatile();
        }

        @Override
        public boolean isLValue() {
            return declaration.isLValue();
        }

        @Override
        public boolean isRValue() {
            return declaration.isRValue();
        }

        @Override
        public boolean isAbstract() {
            return declaration.isAbstract();
        }

        @Override
        public boolean isOperator() {
            return declaration.isOperator();
        }

        @Override
        public OperatorKind getOperatorKind() {
            return declaration.getOperatorKind();
        }

        @Override
        public CsmCompoundStatement getBody() {
            if (CsmKindUtilities.isFunctionDefinition(declaration)) {
                return ((CsmFunctionDefinition)declaration).getBody();
            }
            return null;
        }

        @Override
        public CsmFunction getDeclaration() {
            if (CsmKindUtilities.isFunctionDefinition(declaration)) {
                return ((CsmFunctionDefinition)declaration).getDeclaration();
            }
            return this;
        }
    }

    private static class Constructor extends Method implements CsmConstructor {

        public Constructor(CsmMethod method, Map<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            super(method, mapping);
        }
        
        public Constructor(CsmConstructor method, CsmInstantiation instantiation) {
            super((CsmMethod)method, instantiation);
        }

        @Override
        public Collection<CsmExpression> getInitializerList() {
            return ((CsmConstructor) declaration).getInitializerList();
        }
    }
    
    private static class Parameter extends Instantiation<CsmParameter> implements CsmParameter {
        private final CsmType type;

        public Parameter(CsmParameter parameter, CsmInstantiation instantiation) {
            super(parameter, instantiation.getMapping());
            this.type = parameter.isVarArgs() ? TypeFactory.getVarArgType() : createType(parameter.getType(), instantiation);
        }

        @Override
        public boolean isExtern() {
            return declaration.isExtern();
        }

        @Override
        public CsmType getType() {
            return type;
        }

        @Override
        public CsmExpression getInitialValue() {
            return declaration.getInitialValue();
        }

        @Override
        public CharSequence getDisplayText() {
            return declaration.getDisplayText();
        }

        @Override
        public CsmVariableDefinition getDefinition() {
            return declaration.getDefinition();
        }

        @Override
        public CharSequence getDeclarationText() {
            return declaration.getDeclarationText();
        }

        @Override
        public boolean isVarArgs() {
            return declaration.isVarArgs();
        }
    }   
    
    public static CsmType createType(CsmType type, CsmInstantiation instantiation) {
        return createType(type, instantiation, new TemplateParameterResolver());
    }    
    
    public static CsmType createType(CsmType type, List<CsmInstantiation> instantiations) {
        CsmType result = type;
        for (CsmInstantiation instantiation : instantiations) {
            result = createType(result, instantiation);
        }
        return result;
    }             
    
    private static CsmType createType(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
        if (type == null) {
            throw new NullPointerException("no type for " + instantiation); // NOI18N
        }
        if (canSkipInstantiation(type, instantiation.getMapping())) {
            return type;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Instantiation.createType {0}; inst:{1}\n", new Object[]{type.getText(), instantiation.getTemplateDeclaration().getName()});
        }
//        System.err.println("Instantiation.createType for " + type + " with instantiation " + instantiation);
        if (CsmKindUtilities.isTemplateParameterType(type)) {
            CsmType instantiatedType = templateParamResolver.clone().resolveTemplateParameterType(type, instantiation);
            if (instantiatedType == null) {
                return new TemplateParameterType(type, instantiation, templateParamResolver);
            } else if (CsmKindUtilities.isTemplateParameterType(instantiatedType)) {
                if (instantiatedType != type) {
                    return new TemplateParameterType(type, instantiation, templateParamResolver);
                } else {
                    return type;
                }
            } else if (instantiatedType instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                return new NestedTemplateParameterType(type, instantiation, templateParamResolver);
            } else if (CsmKindUtilities.isFunctionPointerType(instantiatedType)) {
                return new FunPtrTemplateParameterType(type, instantiation, templateParamResolver);
            }
        }
        if (type instanceof NestedTemplateParameterType) {
            return new NestedTemplateParameterType(type, instantiation, templateParamResolver);
        }
        if (type instanceof FunPtrTemplateParameterType) {
            return new FunPtrTemplateParameterType(type, instantiation, templateParamResolver);
        }        
        if (isNestedType(type)) {
            return new NestedType(type, instantiation, templateParamResolver);
        }
        if (type instanceof DeclTypeImpl || type instanceof Decltype) {
            return new Decltype(type, instantiation, templateParamResolver);
        }
        if (CsmKindUtilities.isFunctionPointerType(type)) {
            return new TypeFunPtr((CsmFunctionPointerType) type, instantiation, templateParamResolver);
        }
        return new Type(type, instantiation, templateParamResolver);       
    }       
    
    public static boolean isRecursiveInstantiation(CsmObject instantiation) {
        // TODO: think about constant "MAX_RECURSIVE_INSTANTIATIONS - 1"
        return isRecursiveInstantiation(instantiation, null, MAX_RECURSIVE_INSTANTIATIONS - 1);
    }
    
    public static CsmType unfoldInstantiatedType(CsmType type) {
        return unfoldInstantiatedType(type, Integer.MAX_VALUE);
    }    
    
    public static CsmType unfoldInstantiatedType(CsmType type, int iterations) {
        CsmType result = type;
        while (result instanceof Type && iterations > 0) {
            result = ((Type) result).instantiatedType;
            --iterations;
        }
        return result;
    }    
    
    public static CsmType unfoldOriginalType(CsmType type) {
        return unfoldOriginalType(type, Integer.MAX_VALUE);
    } 
    
    public static CsmType unfoldOriginalType(CsmType type, int iterations) {
        CsmType result = type;
        while (result instanceof Type && iterations > 0) {
            result = ((Type) result).originalType;
            --iterations;
        }
        return result;
    } 
    
    public static boolean isInstantiatedType(CsmType type) {
        return type instanceof Type;
    }
    
    public static int getInstantiationDepth(CsmType type)  {
        int depth = 0;
        while (type instanceof Type) {
            ++depth;
            type = ((Type) type).originalType;
        }
        return depth;
    }
    
    public static CsmInstantiation getInstantiatedTypeInstantiation(CsmType type) {
        if (isInstantiatedType(type)) {
            return ((Type) type).getInstantiation();
        }
        return null;
    }

    public static List<CsmInstantiation> getInstantiatedTypeInstantiations(CsmType type) {
        if (isInstantiatedType(type)) {
            List<CsmInstantiation> insts = new ArrayList<>();
            while (type instanceof Type) {
                insts.add(((Type) type).getInstantiation());
                type = ((Type) type).originalType;
            }        
            return insts;
        }
        return null;
    }    
    
    public static boolean isNestedType(CsmType type) {
        return type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType ||
               type instanceof NestedType; 
    }    
    
    public static boolean isTemplateBasedInstantiation(CsmInstantiation inst) {
        return isTemplateBasedInstantiation(inst, new IdentityHashMap<CsmInstantiation, Boolean>(2));
    }
    
    private static boolean isTemplateBasedInstantiation(CsmInstantiation inst, Map<CsmInstantiation, Boolean> visited) {
        if (!visited.containsKey(inst)) {
            visited.put(inst, Boolean.TRUE);
            for (CsmSpecializationParameter param : inst.getMapping().values()) {
                if (isTemplateBasedParameter(param, visited)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static boolean isTemplateBasedParameter(CsmSpecializationParameter param, Map<CsmInstantiation, Boolean> visited) {
        if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
            CsmType type = ((CsmTypeBasedSpecializationParameter) param).getType();
            if (CsmKindUtilities.isTemplateParameterType(type)) {
                return true;
            } else if (type instanceof Type) {
                return isTemplateBasedInstantiation(((Type) type).getInstantiation(), visited);
            }
        } else if (CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
            for (CsmSpecializationParameter inner : ((CsmVariadicSpecializationParameter) param).getArgs()) {
                if (isTemplateBasedParameter(inner, visited)) {
                    return true;
                }
            }
        }
        return false;
    }
   
    private static class TemplateParameterType extends Type implements CsmTemplateParameterType {
        public TemplateParameterType(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super(type, instantiation, templateParamResolver);
        }

        @Override
        public CsmTemplateParameter getParameter() {
            return ((CsmTemplateParameterType)instantiatedType).getParameter();
        }

        @Override
        public CsmType getTemplateType() {
            return ((CsmTemplateParameterType)instantiatedType).getTemplateType();
        }
    }
    
    // Type that represents template paramter type resolved into nested type
    private static class NestedTemplateParameterType extends Type {
        
        public NestedTemplateParameterType(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super(type, instantiation, templateParamResolver);
            
            if (CsmKindUtilities.isTemplateParameterType(type)) {
                if (instantiationHappened() && isNestedType(instantiatedType)) { // paranoia
                    CsmInstantiation alteredInstantiation = templateParamResolver.alterInstantiation(instantiation);
                    if (alteredInstantiation != null) {
                        instantiatedType = createType(instantiatedType, alteredInstantiation, templateParamResolver);
                    }
                }                
            } else if (type instanceof NestedTemplateParameterType) {
                NestedTemplateParameterType prev = (NestedTemplateParameterType) type;
                if (!instantiationHappened() && isNestedType(prev.instantiatedType)) { // paranoia
                    CsmInstantiation alteredInstantiation = templateParamResolver.alterInstantiation(instantiation);
                    if (alteredInstantiation != null) {
                        instantiatedType = createType(prev.instantiatedType, alteredInstantiation, templateParamResolver);
                    }
                }
            }
        }        
    }
    
    // Type that represents template paramter type resolved into function pointer type
    private static class FunPtrTemplateParameterType extends Type implements CsmFunctionPointerType {
        
        public FunPtrTemplateParameterType(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super(type, instantiation, templateParamResolver);
            
            if (CsmKindUtilities.isTemplateParameterType(type)) {
                if (instantiationHappened() && CsmKindUtilities.isFunctionPointerType(instantiatedType)) {
                    CsmInstantiation alteredInstantiation = templateParamResolver.alterInstantiation(instantiation);
                    if (alteredInstantiation != null) {
                        instantiatedType = createType(instantiatedType, alteredInstantiation, templateParamResolver);
                    }
                }                
            } else if (type instanceof FunPtrTemplateParameterType) {
                FunPtrTemplateParameterType prev = (FunPtrTemplateParameterType) type;
                if (!instantiationHappened() && CsmKindUtilities.isFunctionPointerType(prev.instantiatedType)) {
                    CsmInstantiation alteredInstantiation = templateParamResolver.alterInstantiation(instantiation);
                    if (alteredInstantiation != null) {
                        instantiatedType = createType(prev.instantiatedType, alteredInstantiation, templateParamResolver);
                    }
                }
            }
        }        

        @Override
        public Collection<CsmParameter> getParameters() {
            return ((CsmFunctionPointerType) instantiatedType).getParameters();
        }

        @Override
        public CsmType getReturnType() {
            return ((CsmFunctionPointerType) instantiatedType).getReturnType();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmFunctionPointerType) instantiatedType).getScopeElements();
        }

        @Override
        public CsmScope getScope() {
            return ((CsmFunctionPointerType) instantiatedType).getScope();
        }
    }    

    private static class Type implements CsmType, Resolver.SafeTemplateBasedProvider {
        protected final CsmType originalType;
        protected final CsmInstantiation instantiation;
        protected final boolean inst;
        protected CsmType instantiatedType;
        protected CsmTemplateParameter parameter;
        protected CachedResolved cachedResolved;

        private Type(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            this.instantiation = instantiation;
            CsmType origType = type;
            CsmType newType = type;
            parameter = null;

            if (CsmKindUtilities.isTemplateParameterType(type)) {
                CsmTemplateParameterType paramType = (CsmTemplateParameterType)type;
                parameter = paramType.getParameter();
                TemplateParameterResolver paramsResolver = null;
                if (CsmKindUtilities.isTemplate(parameter) && paramType.getTemplateType().hasInstantiationParams()) {
                    paramsResolver = templateParamResolver.clone();
                }
                newType = templateParamResolver.resolveTemplateParameterType(type, instantiation);
                if (newType != null) {
                    int pointerDepth = (newType != origType ? newType.getPointerDepth() + origType.getPointerDepth() : origType.getPointerDepth());
                    int arrayDepth = (newType != origType ? newType.getArrayDepth() + origType.getArrayDepth() : origType.getArrayDepth());
                    
                    newType = TypeFactory.createType(
                            newType, 
                            pointerDepth, 
                            TypeFactory.getReferenceValue(origType), 
                            arrayDepth,
                            origType.isConst(),
                            origType.isVolatile()
                    );             
                    
                    CsmTemplateParameter p = paramType.getParameter();
                    if (CsmKindUtilities.isTemplate(p)) {
                        CsmType paramTemplateType = paramType.getTemplateType();
                        if (paramTemplateType != null) {
                            List<CsmSpecializationParameter> paramInstParams = paramTemplateType.getInstantiationParams();
                            if (!paramInstParams.isEmpty()) {
                                List<CsmSpecializationParameter> newInstParams = new ArrayList<>(newType.getInstantiationParams());
                                boolean updateInstParams = false;
                                for (CsmSpecializationParameter param : paramInstParams) {
                                    if (!newInstParams.contains(param)) {
                                        fillNewInstantiationParams(paramsResolver, param, newInstParams);
                                        updateInstParams = true;
                                    }
                                }
                                if(updateInstParams) {
                                    newType = TypeFactory.createType(newType, newInstParams);
                                }
                            }
                        }
                    }
                    origType = paramType.getTemplateType();
                } else {
                    newType = type;
                }
            }

            if(!isRecursion(Type.this, MAX_INHERITANCE_DEPTH)) {
                this.originalType = origType;
                this.instantiatedType = newType;
            } else {
                CndUtils.assertTrueInConsole(false, "Infinite recursion in file " + Type.this.getContainingFile() + " type " + Type.this.toString()); //NOI18N
                this.originalType = origType;
                this.instantiatedType = origType;
            }
            
            inst = instantiationHappened() ? newType.isInstantiation() : origType.isInstantiation();
        }
        
        private void fillNewInstantiationParams(TemplateParameterResolver paramsResolver, CsmSpecializationParameter param, List<CsmSpecializationParameter> newInstParams) {
            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                CsmTypeBasedSpecializationParameter typeInstParam = (CsmTypeBasedSpecializationParameter) param;
                boolean paramAdded = false;
                if (paramsResolver != null && CsmKindUtilities.isTemplateParameterType(typeInstParam.getType())) {
                    paramsResolver = paramsResolver.clone();
                    CsmSpecializationParameter specParam = paramsResolver.resolveTemplateParameter(
                            ((CsmTemplateParameterType) typeInstParam.getType()).getParameter(), 
                            new MapHierarchy(instantiation.getMapping())
                    );
                    if (CsmKindUtilities.isVariadicSpecalizationParameter(specParam)) {
                        CsmVariadicSpecializationParameter variadic = (CsmVariadicSpecializationParameter) specParam;
                        for (CsmSpecializationParameter fromVariadic : variadic.getArgs()) {
                            newInstParams.add(fromVariadic);
//                            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(fromVariadic)) {
//                                CsmTypeBasedSpecializationParameter typeVariadicParam = (CsmTypeBasedSpecializationParameter) fromVariadic;
//                                CsmType newVariadicParamType = createType(typeVariadicParam.getType(), instantiation, paramsResolver.clone());
//                                newInstParams.add(CsmInstantiationProvider.getDefault().createTypeBasedSpecializationParameter(newVariadicParamType, param.getScope()));
//                            }
                        }
                        paramAdded = true;
                    }
                }
                if (!paramAdded) {
                    //newInstParams.add(param);
                    CsmType newParamType = createType(typeInstParam.getType(), instantiation, paramsResolver);
                    newInstParams.add(CsmInstantiationProvider.getDefault().createTypeBasedSpecializationParameter(newParamType, param.getScope()));
                }
            } else {
                newInstParams.add(param);
            }
        }

        public final boolean instantiationHappened() {
            return originalType != instantiatedType;
        }

        @Override
        public CharSequence getClassifierText() {
            if (isInstantiatedType(instantiatedType)) {
                return instantiatedType.getClassifierText();
            }
            return CharSequenceUtils.concatenate(instantiatedType.getClassifierText(), TypeImpl.getInstantiationText(this));
        }

        private CharSequence getInstantiatedText() {
            return getTextImpl(true);
        }

        @Override
        public CharSequence getText() {
            return getTextImpl(false);
        }
        
        private CharSequence getTextImpl(boolean instantiate) {
            if (originalType instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                return ((org.netbeans.modules.cnd.modelimpl.csm.NestedType)originalType).getOwnText();
            } 
            if (originalType instanceof NestedType) {
                return ((NestedType)originalType).getOwnText();
            }            
            if (originalType instanceof TypeImpl) {
                // try to instantiate original classifier
                CsmClassifier classifier = null;
                if (instantiate) {
                    classifier = getClassifier();
                    if (classifier != null) {
                        classifier = CsmBaseUtilities.getOriginalClassifier(classifier, getContainingFile());
                    }
                }
                CharSequence clsText;
                if (classifier == null || CsmKindUtilities.isInstantiation(classifier)) {
                    clsText = getClassifierText();
                } else {
                    clsText = classifier.getName();
                }
                return ((TypeImpl)originalType).decorateText( clsText, this, false, null);
            }
            return originalType.getText();
        }

        public CharSequence getOwnText() {
            if (originalType instanceof TypeImpl) {
                return ((TypeImpl) originalType).getOwnText();
            }
            return originalType.getText();
        }

        @Override
        public Position getStartPosition() {
            return instantiatedType.getStartPosition();
        }

        @Override
        public int getStartOffset() {
            return instantiatedType.getStartOffset();
        }

        @Override
        public Position getEndPosition() {
            return instantiatedType.getEndPosition();
        }

        @Override
        public int getEndOffset() {
            return instantiatedType.getEndOffset();
        }

        @Override
        public CsmFile getContainingFile() {
            return instantiatedType.getContainingFile();
        }

        @Override
        public boolean isInstantiation() {
            return instantiatedType.isInstantiation();
        }

        @Override
        public boolean isPackExpansion() {
            return instantiatedType.isPackExpansion();
        }

        @Override
        public boolean hasInstantiationParams() {
            return instantiatedType.hasInstantiationParams();
        }

        private boolean isRecursion(CsmType type, int i){
            if (i == 0) {
                return true;
            }
            if (type instanceof Instantiation.NestedType) {
                Instantiation.NestedType t = (NestedType) type;
                if (t.parentType != null) {
                    return isRecursion(t.parentType, i-1);
                } else {
                    return isRecursion(t.instantiatedType, i-1);
                }
            } else if (type instanceof Type) {
                return isRecursion(((Type)type).instantiatedType, i-1);
            } else if (type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                org.netbeans.modules.cnd.modelimpl.csm.NestedType t = (org.netbeans.modules.cnd.modelimpl.csm.NestedType) type;
                if (t.getParent() != null) {
                    return isRecursion(t.getParent(), i-1);
                } else {
                    return false;
                }
            } else if (type instanceof TypeImpl){
                return false;
            } else if (type instanceof TemplateParameterTypeImpl){
                return isRecursion(((TemplateParameterTypeImpl)type).getTemplateType(), i-1);
            }
            return false;
        }


        @Override
        public boolean isTemplateBased() {
            return isTemplateBased(new HashSet<CsmType>());
        }

        @Override
        public boolean isTemplateBased(Set<CsmType> visited) {
            if (instantiatedType == null) {
                return true;
            }
            if (visited.contains(this)) {
                return false;
            }
            visited.add(this);
            if (instantiatedType instanceof SafeTemplateBasedProvider) {
                return ((SafeTemplateBasedProvider)instantiatedType).isTemplateBased(visited);
            }
            return instantiatedType.isTemplateBased();
        }

        @Override
        public boolean isReference() {
            if(instantiationHappened()) {
                return originalType.isReference() || instantiatedType.isReference();
            } else {
                return originalType.isReference();
            }
        }

        @Override
        public boolean isRValueReference() {
            if (instantiationHappened()) {
                return originalType.isRValueReference() || instantiatedType.isRValueReference();
            } else {
                return originalType.isRValueReference();
            }
        }
        
        @Override
        public boolean isPointer() {
            if(instantiationHappened()) {
                return originalType.isPointer() || instantiatedType.isPointer();
            } else {
                return originalType.isPointer();
            }
        }

        @Override
        public boolean isConst() {
            if(instantiationHappened()) {
                return originalType.isConst() || instantiatedType.isConst();
            } else {
                return originalType.isConst();
            }
        }
        
        @Override
        public boolean isVolatile() {
            if(instantiationHappened()) {
                return originalType.isVolatile() || instantiatedType.isVolatile();
            } else {
                return originalType.isVolatile();
            }
        }

        @Override
        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return instantiatedType.isBuiltInBased(resolveTypeChain);
        }

        @Override
        public List<CsmSpecializationParameter> getInstantiationParams() {
            if (!originalType.isInstantiation()) {
                return Collections.emptyList();
            }
            List<CsmSpecializationParameter> res = new ArrayList<>();
            for (CsmSpecializationParameter instParam : originalType.getInstantiationParams()) {
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam) &&
                        CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instParam).getType())) {
                    CsmTemplateParameterType paramType = (CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instParam).getType();
                    CsmSpecializationParameter newTp = instantiation.getMapping().get(paramType.getParameter());
                    if (newTp != null && newTp != instParam) {
                        res.add(newTp);
                    } else {
                        res.add(instParam);
                    }
                } else {
                    res.add(instParam);
                }
            }
            return res;
        }

        @Override
        public int getPointerDepth() {
            if (instantiationHappened()) {
                return instantiatedType.getPointerDepth();
            } else {
                return originalType.getPointerDepth();
            }
        }

        @Override
        public CsmClassifier getClassifier() {
            return  getClassifier(new ArrayList<CsmInstantiation>(), false);
        }
        
        public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
            instantiations.add(instantiation);
            CsmClassifier resolved = getCachedResolved(instantiations);
            if (resolved == null) {
                if (!instantiationHappened()) {
                    CsmClassifier classifier;
                    if(originalType instanceof TypeImpl) {
                        classifier = ((TypeImpl)originalType).getClassifier(instantiations, false);
                    } else if(originalType instanceof Type) {
                        classifier = ((Type)originalType).getClassifier(instantiations, false);                               
                    } else {
                        classifier = originalType.getClassifier();                        
                    }
                    resolved = classifier;
                } else {
                    if(instantiatedType instanceof TypeImpl) {
                        resolved = ((TypeImpl)instantiatedType).getClassifier(instantiations, false);
                    } else if(instantiatedType instanceof Type) {
                        resolved = ((Type)instantiatedType).getClassifier(instantiations, false);                               
                    } else {
                        resolved = instantiatedType.getClassifier();                        
                    }
                } 
                
                if (inst && CsmKindUtilities.isTemplate(resolved)) {
                    CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                    CsmObject obj = null;
                    if(ip instanceof InstantiationProviderImpl) {
                        Resolver resolver = ResolverFactory.createResolver(this);
                        try {
                            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                if(!isTemplateParameterTypeBased() || !instantiation.getMapping().keySet().contains(getResolvedTemplateParameter())) {
                                    obj = ((InstantiationProviderImpl)ip).instantiate((CsmTemplate) resolved, instantiation, specialize);
                                } else {
//                                    final Map<CsmTemplateParameter, CsmSpecializationParameter> mapping1 = new HashMap<CsmTemplateParameter, CsmSpecializationParameter>(instantiation.getMapping());
//                                    mapping1.remove(getResolvedTemplateParameter());
//                                    obj = ((InstantiationProviderImpl)ip).instantiate((CsmTemplate) resolved, mapping1);
                                }
                            } else {
                                return null;
                            }
                        } finally {
                            ResolverFactory.releaseResolver(resolver);
                        }
                    } else {
                        obj = ip.instantiate((CsmTemplate) resolved, instantiation);
                    }
                    if (CsmKindUtilities.isClassifier(obj)) {
                        resolved = (CsmClassifier) obj;
                        return cacheResolved(instantiations, resolved);
                    }
                }                
                
                if (instantiation != null && !canSkipInstantiation(resolved, instantiation.getMapping())) {
                    if (CsmKindUtilities.isTypedef(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember tdMember = (CsmMember)resolved;
                        if (isTemplateScope(tdMember.getContainingClass())) {
                            resolved = new Typedef((CsmTypedef)resolved, instantiation);
                            return cacheResolved(instantiations, resolved);
                        }
                    }
                    if (CsmKindUtilities.isTypeAlias(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember taMember = (CsmMember)resolved;
                        if (isTemplateScope(taMember.getContainingClass())) {
                            resolved = new MemberTypeAlias((CsmTypeAlias)resolved, instantiation);
                            return cacheResolved(instantiations, resolved);
                        }
                    }
                    if (CsmKindUtilities.isClass(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember tdMember = (CsmMember)resolved;
                        if (isTemplateScope(tdMember.getContainingClass())) {
                            resolved = new Class((CsmClass)resolved, instantiation.getMapping());
                            return cacheResolved(instantiations, resolved);
                        }
                    }
                }
            }
            return cacheResolved(instantiations, resolved);
        }        

        @Override
        public CharSequence getCanonicalText() {
            return originalType.getCanonicalText();
        }

        @Override
        public int getArrayDepth() {
            if (instantiationHappened()) {
                return originalType.getArrayDepth() + instantiatedType.getArrayDepth();
            } else {
                return originalType.getArrayDepth();
            }
        }

        public CsmInstantiation getInstantiation() {
            return instantiation;
        }
        
        public boolean isTemplateParameterTypeBased() {
            CsmType baseType = originalType;
            while(baseType instanceof Type) {
                if(((Type)baseType).instantiationHappened()) {
                    return true;
                }
                baseType = ((Type)baseType).originalType;
            }
            return false;
        }        
        
        public CsmTemplateParameter getResolvedTemplateParameter() {
            CsmType baseType = originalType;
            while(baseType instanceof Type) {
                if(((Type)baseType).parameter != null) {
                    return ((Type)baseType).parameter;
                }
                baseType = ((Type)baseType).originalType;
            }
            return null;
        }
        
        protected CsmClassifier getCachedResolved(List<CsmInstantiation> instantiations) {
            if (cachedResolved != null) {
                if (!cachedResolved.fromTemplateContext) {
                    return cachedResolved.classifier;
                }
                // cache was done within template context
                if (!instantiations.isEmpty()) {
                    if (isTemplateBasedInstantiation(instantiations.get(0))) {
                        return cachedResolved.classifier;
                    }
                }
            }
            return null;
        }
        
        protected CsmClassifier cacheResolved(List<CsmInstantiation> instantiations, CsmClassifier resolved) {
            ////////////////////////////////////////////////////////////////////
            // If there would be problems with performance, enable
            // caching without conditions
            ///////////////////////////////////////////////////////////////////
            
            // FIXME: disabling cache in http://hg.netbeans.org/cnd-main/rev/bc1384664e04 was too agressive
            // new timing bacome:
            // FileMaps Cache: HITS=414,034, Used 174,633ms, SavedTime=828,068ms, Cached 1 Values (NULLs=0) ([88]Code Model Client Request :Go to declaration)
            // OrigClassifier Cache: HITS=120,470, Used 174,656ms, SavedTime=120,470ms, Cached 85 Values (NULLs=0) ([88]Code Model Client Request :Go to declaration)
            // SELECT Cache: HITS=363,349, Used 174,656ms, SavedTime=363,349ms, Cached 194 Values (NULLs=0) ([88]Code Model Client Request :Go to declaration)
            // Resolver3 Cache: HITS=414,289, Used 174,638ms, SavedTime=510ms, Cached 7 Values (NULLs=4) ([88]Code Model Client Request :Go to declaration)
            //FROM previous:
            //FileMaps Cache: HITS=106, Used 655ms, SavedTime=388ms, Cached 6 Values (NULLs=0) ([58]Code Model Client Request :Go to declaration)
            //OrigClassifier Cache: HITS=11, Used 665ms, SavedTime=11ms, Cached 4 Values (NULLs=0) ([58]Code Model Client Request :Go to declaration)
            //SELECT Cache: HITS=74, Used 671ms, SavedTime=74ms, Cached 11 Values (NULLs=0) ([58]Code Model Client Request :Go to declaration)
            //Resolver3 Cache: HITS=107, Used 671ms, SavedTime=1,605ms, Cached 32 Values (NULLs=22) ([58]Code Model Client Request :Go to declaration)
            //
            //if (true) cachedResolved = resolved;
            if (resolved != null && getCachedResolved(instantiations) == null) {
                if (!instantiations.isEmpty()) {
                    // We should cache resolved classifier only if this type is on top (knows full context).
                    // If type which do not know full context would cache resolved classifier,
                    // it might be reused later (because of cache) with other instantiations 
                    // above the context it knows, but would return already cached value.
                    boolean fromTemplateContext = isTemplateBasedInstantiation(instantiations.get(0));
                    if (instantiations.get(0) == instantiation && !fromTemplateContext) {
                        cachedResolved = new CachedResolved(resolved, false);
                    } else if (fromTemplateContext) {
                        cachedResolved = new CachedResolved(resolved, true);
                    }
                }
            }
            return resolved;
        }
        
        @Override
        public String toString() {
            return toString(new StringBuilder(), 0);
        }
        
        private String toString(StringBuilder out, int indent) {
            indent(out, indent).append("INSTANTIATION OF "); // NOI18N
            String instName = this.getClass().getSimpleName()+"@"+System.identityHashCode(this);//NOI18N
            out.append(instName).append(":\n");// NOI18N
            if (originalType instanceof Type) {
                ((Type) originalType).toString(out, indent + 2);
            } else {
                indent(out, indent + 2);
                out.append(originalType);
            }
            out.append("\n");// NOI18N
            if (!instantiation.getMapping().isEmpty()) {
                indent(out, indent).append("WITH MAPPING:\n"); // NOI18N
                for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : instantiation.getMapping().entrySet()) {
                    indent(out, indent).append("[").append(entry.getKey()).append("]=>{"); // NOI18N
                    if (CsmKindUtilities.isTypeBasedSpecalizationParameter(entry.getValue())) {
                        CsmType mappedType = ((CsmTypeBasedSpecializationParameter) entry.getValue()).getType();
                        if (isInstantiatedType(mappedType)) {
                            CsmType unfoldedType = unfoldOriginalType(mappedType);
                            out.append("INSTANTIATED ").append(unfoldedType).append("}\n"); // NOI18N
                        } else {
                            out.append(mappedType).append("}\n"); // NOI18N
                        }
                    } else {
                        out.append(entry.getValue()).append("}\n"); // NOI18N
                    }
                }
            }
            if (instantiationHappened()) {
                indent(out, indent).append(" BECOME ").append(instantiatedType); // NOI18N
            }
            indent(out, indent).append("END OF ").append(instName);// NOI18N
            return out.toString();
        }
        
        private boolean isTemplateScope(CsmClass cls) {
            while (!CsmKindUtilities.isTemplate(cls)) {
                if (!CsmKindUtilities.isClassMember(cls)) {
                    return false;
                }
                cls = ((CsmMember) cls).getContainingClass();
            }
            return true;
        }

        protected static class CachedResolved {
            
            public final CsmClassifier classifier;
            
            public final boolean fromTemplateContext;

            public CachedResolved(CsmClassifier classifier, boolean fromTemplateContext) {
                this.classifier = classifier;
                this.fromTemplateContext = fromTemplateContext;
            }
        }
    }
    
    private static class TypeFunPtr extends Type implements CsmFunctionPointerType {

        public TypeFunPtr(CsmFunctionPointerType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super((CsmType)type, instantiation, templateParamResolver);
        }

        @Override
        public CsmType getReturnType() {
            return createType(((CsmFunctionPointerType) originalType).getReturnType(), instantiation);
        }

        @Override
        public CsmScope getScope() {
            return ((CsmFunctionPointerType) originalType).getScope();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return ((CsmFunctionPointerType) originalType).getScopeElements();
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            return ((CsmFunctionPointerType) originalType).getParameters();
        }
    }
    
    private static class Decltype extends Type {

        public Decltype(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super(type, instantiation, templateParamResolver);
        }

        @Override
        public boolean isPointer() {
            return isPointer(new ArrayList<CsmInstantiation>());
        }
        
        public boolean isPointer(List<CsmInstantiation> instantiations) {
            instantiations.add(instantiation);
            return isPointerImpl(originalType, instantiations);
        }

        @Override
        public boolean isReference() {
            return isReference(new ArrayList<CsmInstantiation>());
        }

        public boolean isReference(List<CsmInstantiation> instantiations) {
            instantiations.add(instantiation);
            return isReferenceImpl(originalType, instantiations);
        }

        @Override
        public boolean isConst() {
            return isConst(new ArrayList<CsmInstantiation>());
        }
        
        public boolean isConst(List<CsmInstantiation> instantiations) {
            instantiations.add(instantiation);
            return isConstImpl(originalType, instantiations);
        }        

        @Override
        public boolean isRValueReference() {
            return isRValueReference(new ArrayList<CsmInstantiation>());
        }

        public boolean isRValueReference(List<CsmInstantiation> instantiations) {
            instantiations.add(instantiation);
            return isRValueReferenceImpl(originalType, instantiations);
        }        
        
        private static boolean isPointerImpl(CsmType type, List<CsmInstantiation> instantiations) {
            if (type instanceof Decltype) {
                return ((Decltype) type).isPointer(instantiations);
            } else if (type instanceof DeclTypeImpl) {
                return ((DeclTypeImpl) type).isPointer(instantiations);
            } else {
                return type.isPointer();
            }
        }
        
        private static boolean isReferenceImpl(CsmType type, List<CsmInstantiation> instantiations) {
            if (type instanceof Decltype) {
                return ((Decltype) type).isReference(instantiations);
            } else if (type instanceof DeclTypeImpl) {
                return ((DeclTypeImpl) type).isReference(instantiations);
            } else {
                return type.isReference();
            }
        }
        
        private static boolean isConstImpl(CsmType type, List<CsmInstantiation> instantiations) {
            if (type instanceof Decltype) {
                return ((Decltype) type).isConst(instantiations);
            } else if (type instanceof DeclTypeImpl) {
                return ((DeclTypeImpl) type).isConst(instantiations);
            } else {
                return type.isConst();
            }
        }   
        
        private static boolean isRValueReferenceImpl(CsmType type, List<CsmInstantiation> instantiations) {
            if (type instanceof Decltype) {
                return ((Decltype) type).isRValueReference(instantiations);
            } else if (type instanceof DeclTypeImpl) {
                return ((DeclTypeImpl) type).isRValueReference(instantiations);
            } else {
                return type.isRValueReference();
            }
        }           
    }    

    private static class NestedType extends Type {

        private final CsmType parentType;

        private NestedType(CsmType type, CsmInstantiation instantiation, TemplateParameterResolver templateParamResolver) {
            super(type, instantiation, templateParamResolver);

            if (type instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                org.netbeans.modules.cnd.modelimpl.csm.NestedType t = (org.netbeans.modules.cnd.modelimpl.csm.NestedType) type;
                CsmType parent = t.getParent();
                if (parent != null) {
                    parentType = createType(parent, instantiation, templateParamResolver);
                } else {
                    parentType = null;
                }
            } else if (type instanceof NestedType) {
                NestedType t = (NestedType) type;
                CsmType parent = t.parentType;
                if (parent != null) {
                    parentType = createType(parent, instantiation, templateParamResolver);
                } else {
                    parentType = null;
                }
            } else {
                parentType = null;
            }
        }

        @Override
        public CsmClassifier getClassifier() {
            return getClassifier(new ArrayList<CsmInstantiation>(), false);
        }
        
        @Override
        public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
            instantiations.add(instantiation);
            CsmClassifier resolved = getCachedResolved(instantiations);
            if (resolved == null) {
                if(!instantiationHappened()) {
                    if (parentType != null) {
                        CsmClassifier parentClassifier;
                        if(parentType instanceof TypeImpl) {
                            parentClassifier = ((TypeImpl)parentType).getClassifier(instantiations, false);
//                        } else if(parentType instanceof Type) {
//                            parentClassifier = ((Type)parentType).getClassifier(instantiations, false);
                        } else {
                            parentClassifier = parentType.getClassifier();                        
                        }
                        if (CsmBaseUtilities.isValid(parentClassifier)) {
                            MemberResolverImpl memberResolver = new MemberResolverImpl();
                            if (instantiatedType instanceof org.netbeans.modules.cnd.modelimpl.csm.NestedType) {
                                resolved = getNestedClassifier(memberResolver, parentClassifier, ((org.netbeans.modules.cnd.modelimpl.csm.NestedType) instantiatedType).getOwnText());
                            } else if (instantiatedType instanceof NestedType) {
                                resolved = getNestedClassifier(memberResolver, parentClassifier, ((NestedType) instantiatedType).getOwnText());
                            }
                        }
                    } 
                    if (isInstantiation() && CsmKindUtilities.isTemplate(resolved) && !((CsmTemplate) resolved).getTemplateParameters().isEmpty()) {
                        CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                        CsmObject obj = null;
                        if (ip instanceof InstantiationProviderImpl) {
                            Resolver resolver = ResolverFactory.createResolver(this);
                            try {
                                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                    obj = instantiateResolved((InstantiationProviderImpl) ip, resolved, specialize);
                                } else {
                                    return null;
                                }
                            } finally {
                                ResolverFactory.releaseResolver(resolver);
                            }
                        } else {
                            obj = ip.instantiate((CsmTemplate) resolved, this);
                        }
                        if (CsmKindUtilities.isClassifier(obj)) {
                            resolved = (CsmClassifier) obj;
                        }
                    }
                } 
                if (resolved == null) {
                    if(instantiatedType instanceof TypeImpl) {
                        resolved = ((TypeImpl)instantiatedType).getClassifier(instantiations, false);
                    } else if(instantiatedType instanceof Type) {
                        resolved = ((Type)instantiatedType).getClassifier(instantiations, false);                               
                    } else {
                        resolved = instantiatedType.getClassifier();                        
                    }
                    if (isInstantiation() && CsmKindUtilities.isTemplate(resolved) && !((CsmTemplate) resolved).getTemplateParameters().isEmpty()) {
                        CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                        CsmObject obj = null;
                        if (ip instanceof InstantiationProviderImpl) {
                            Resolver resolver = ResolverFactory.createResolver(this);
                            try {
                                if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                                    obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) resolved, instantiation, specialize);
                                } else {
                                    return null;
                                }
                            } finally {
                                ResolverFactory.releaseResolver(resolver);
                            }
                        } else {
                            obj = ip.instantiate((CsmTemplate) resolved, this);
                        }
                        if (CsmKindUtilities.isClassifier(obj)) {
                            resolved = (CsmClassifier) obj;
                        }
                    }
                }
                if (instantiation != null && !canSkipInstantiation(resolved, instantiation.getMapping())) {
                    if (CsmKindUtilities.isTypedef(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember tdMember = (CsmMember) resolved;
                        if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                            resolved = new Typedef((CsmTypedef) resolved, instantiation);
                            return cacheResolved(instantiations, resolved);
                        }
                    }
                    if (CsmKindUtilities.isTypeAlias(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember taMember = (CsmMember)resolved;
                        if (CsmKindUtilities.isTemplate(taMember.getContainingClass())) {
                            resolved = new MemberTypeAlias((CsmTypeAlias)resolved, instantiation);
                            return cacheResolved(instantiations, resolved);
                        }
                    }          
                    if (CsmKindUtilities.isClass(resolved) && CsmKindUtilities.isClassMember(resolved)) {
                        CsmMember tdMember = (CsmMember)resolved;
                        if (CsmKindUtilities.isTemplate(tdMember.getContainingClass())) {
                            resolved = new Class((CsmClass)resolved, instantiation.getMapping());
                            return cacheResolved(instantiations, resolved);
                        }
                    }                
                }
            }
            return cacheResolved(instantiations, resolved);
        }

        @Override
        public boolean isInstantiation() {
            return (parentType != null && parentType.isInstantiation()) || super.isInstantiation();
        }
                
        /**
         * Tries to merge lexical context and parent context into one and
         * instantiate resolved with that context.
         * 
         * Example: 
         * <code>
         *   ...
         *   Alloc::something<T>
         *   ...
         * </code>
         * 
         * "Alloc" can be template parameter, in that case context from resolved 
         * template parameter "Alloc" (parent context) can not contain full context 
         * and therefore template parameter "T".
         * 
         * @param ip - instantiation provider
         * @param resolved - resolved nested name
         * @param specialize
         * @return instantiated resolved classifier in appropriate context
         */
        private CsmObject instantiateResolved(InstantiationProviderImpl ip, CsmClassifier resolved, boolean specialize) {
            CsmObject obj = resolved;
            List<CsmInstantiation> parentInstantiations = new ArrayList<>();
            while (CsmKindUtilities.isInstantiation(obj)) {
                parentInstantiations.add((CsmInstantiation) obj);
                obj = (CsmClassifier) ((CsmInstantiation) obj).getTemplateDeclaration();
            }                  
            List<CsmInstantiation> contextInstantiations = new ArrayList<>();
            CsmType contextType = this;
            while (contextType instanceof Type) {
                contextInstantiations.add(((Type) contextType).getInstantiation());
                contextType = ((Type) contextType).originalType;
            }
            if (!parentInstantiations.isEmpty()) {
                CsmInstantiation firstInst = parentInstantiations.get(0);
                int matchIndex = -1;
                for (int ctxIndex = 0; ctxIndex < contextInstantiations.size(); ++ctxIndex) {
                    if (firstInst.getMapping() == contextInstantiations.get(ctxIndex).getMapping()) {
                        matchIndex = ctxIndex;
                        break;
                    }
                }
                if (matchIndex >= 0) {
                    int matchNumber = 0;
                    while (matchNumber < parentInstantiations.size() && (matchNumber + matchIndex) < contextInstantiations.size() &&
                           parentInstantiations.get(matchNumber).getMapping() == contextInstantiations.get(matchNumber + matchIndex).getMapping()) {
                        ++matchNumber;
                    }
                    int insertIndex = matchIndex + matchNumber;
                    for (int i = matchNumber; i < parentInstantiations.size(); i++) {
                        contextInstantiations.add(insertIndex++, parentInstantiations.get(i));
                    }
                }
            }
            Collections.reverse(contextInstantiations);
            obj = ip.instantiate((CsmTemplate) obj, contextType, false);
            for (CsmInstantiation contextInst : contextInstantiations) {
                obj = ip.instantiate((CsmTemplate) obj, contextInst, specialize);                                        
            }  
            return obj;
        }
    }

    private static CsmClassifier getNestedClassifier(MemberResolverImpl memberResolver, CsmClassifier parentClassifier, CharSequence ownText) {
        return org.netbeans.modules.cnd.modelimpl.csm.NestedType.getNestedClassifier(memberResolver, parentClassifier, ownText);
    }    

    public final static class InstantiationSelfUID implements CsmUID<CsmInstantiation>, SelfPersistent {
        private final Instantiation ref;
        private InstantiationSelfUID(Instantiation ref) {
            this.ref = ref;
        }

        @Override
        public Instantiation getObject() {
            return this.ref;
        }
        ////////////////////////////////////////////////////////////////////////////
        // impl for Persistent

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            // write nothing
        }

        public InstantiationSelfUID(RepositoryDataInput input) throws IOException {
            this.ref = null;
        }
    }
    
    public static CharSequence getOriginalText(CsmType type) {
        if (type instanceof Type) {
            return getOriginalText(((Type) type).originalType);
        }
        return type.getText();
    }
    
    public static CharSequence getInstantiatedText(CsmType type) {
        if (type instanceof Type) {
            return ((Type)type).getInstantiatedText();
        } else if (false && type.isInstantiation() && type.getClassifier() != null) {
            StringBuilder sb = new StringBuilder(type.getClassifier().getQualifiedName());
            sb.append(Instantiation.getInstantiationCanonicalText(type.getInstantiationParams()));
            return sb;
        } else {
            return type.getText();
        }
    }
    
    public static interface CsmSpecializationParamTextProvider {
        
        CharSequence getSpecParamText(CsmSpecializationParameter param, 
                                      CsmType paramType,
                                      List<CsmInstantiation> context);
    }
    
    public static class DefaultSpecParamTextProvider implements CsmSpecializationParamTextProvider {
        
        @Override
        public CharSequence getSpecParamText(CsmSpecializationParameter param, CsmType paramType, List<CsmInstantiation> context) {
            if(CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                return TypeImpl.getCanonicalText(paramType);
            }
            if(CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                return param.getText();
            }
            if(CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                return param.getText();
            }
            return ""; // NOI18N
        }
    }
    
    public static CharSequence getInstantiationCanonicalText(List<CsmSpecializationParameter> params) {
        return getInstantiationCanonicalText(new SimpleInstantiationParamsInfo(params), new DefaultSpecParamTextProvider());
    }

    public static CharSequence getInstantiationCanonicalText(InstantiationParametersInfo paramsInfo, CsmSpecializationParamTextProvider specParamTextProvider) {
        List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> params = paramsInfo.getExpandedParams();
        List<CsmType> types = paramsInfo.getParamsTypes();
        
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        assert params.size() == types.size() : "Arrays of params and their types must have equal sizes!"; //NOI18N
        
        Iterator<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> paramsIter = params.iterator();
        Iterator<CsmType> typesIter = types.iterator();
        
        StringBuilder sb = new StringBuilder();
        sb.append('<');
        boolean first = true;
        while (paramsIter.hasNext() && typesIter.hasNext()) {
            Pair<CsmSpecializationParameter, List<CsmInstantiation>> pair = paramsIter.next();
            CsmSpecializationParameter param = pair.first();
            List<CsmInstantiation> context = pair.second();
            CsmType paramType = typesIter.next();
            
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            
            sb.append(specParamTextProvider.getSpecParamText(param, paramType, context));
        }
        TemplateUtils.addGREATERTHAN(sb);
        return sb;
    }
    
    public static CsmSpecializationParameter resolveTemplateParameter(CsmTemplateParameter templateParameter, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
        return new TemplateParameterResolver().resolveTemplateParameter(templateParameter, mapping);
    }
    
    public static class TemplateParameterResolver implements Cloneable {
        
        private static final int RESOLVED_LIMIT = 32;
        
        // Technically it is enough to store one last parameter, 
        // but to be extra sure that there is no infinite recursion here,
        // we store all of them and also set restriction on number of resolved 
        // parameters per instantiation of one type
        private final Set<CsmTemplateParameter> lastResolvedParameters = new HashSet<>();

        public TemplateParameterResolver() {
            this(Collections.<CsmTemplateParameter>emptySet());
        }

        public TemplateParameterResolver(Set<CsmTemplateParameter> lastResolvedParameters) {
            this.lastResolvedParameters.addAll(lastResolvedParameters);
        }
        
        public CsmType resolveTemplateParameterType(CsmType type, CsmInstantiation instantiation) {
            if (CsmKindUtilities.isTemplateParameterType(type)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Instantiation.resolveTemplateParameter {0}; mapping={1}\n", new Object[]{type.getText(), instantiation.getTemplateDeclaration().getName()});
                }
                MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping = new MapHierarchy<>(instantiation.getMapping());
                CsmTemplateParameter param = ((CsmTemplateParameterType) type).getParameter();
                if (param != null) {
                    CsmType resolvedType = resolveTemplateParameterType(param, mapping);
                    if (resolvedType != null) {
                        return resolvedType;
                    }
                } else {
                    LOG.log(Level.INFO, "no param for " + type + " and \n" + instantiation, new IllegalStateException()); // NOI18N;
                }
            }
            return type;
        }    

        public CsmType resolveTemplateParameterType(CsmTemplateParameter templateParameter, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            CsmSpecializationParameter resolvedParam = resolveTemplateParameter(templateParameter, mapping);
            if (resolvedParam != null && resolvedParam instanceof CsmTypeBasedSpecializationParameter) {
                return ((CsmTypeBasedSpecializationParameter) resolvedParam).getType();
            }
            return null;  
        }    

        public CsmSpecializationParameter resolveTemplateParameter(CsmTemplateParameter templateParameter, MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapping) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Instantiation.resolveTemplateParameter {0}; mapping={1}\n", new Object[]{templateParameter.getName(), mapping.size()});
            }
            if (lastResolvedParameters.size() > RESOLVED_LIMIT) {
                return null; // probably infinite recursion
            }
            CsmSpecializationParameter instantiatedParam = mapping.get(templateParameter);
            int iteration = MAX_INHERITANCE_DEPTH;
            while (CsmKindUtilities.isTypeBasedSpecalizationParameter(instantiatedParam) &&
                    CsmKindUtilities.isTemplateParameterType(((CsmTypeBasedSpecializationParameter) instantiatedParam).getType()) && iteration != 0) {
                CsmTemplateParameter nextTemplateParameter = ((CsmTemplateParameterType) ((CsmTypeBasedSpecializationParameter) instantiatedParam).getType()).getParameter();
                CsmSpecializationParameter nextInstantiatedParam = mapping.get(templateParameter);
                if (nextInstantiatedParam != null) {
                    instantiatedParam = nextInstantiatedParam;
                    templateParameter = nextTemplateParameter;
                } else {
                    break;
                }
                iteration--;
            }
            if (instantiatedParam != null) {
                for (CsmTemplateParameter alreadyResolvedParam : lastResolvedParameters) {
                    if (alreadyResolvedParam.getScope() == templateParameter.getScope()) {
                        if (alreadyResolvedParam.getStartOffset() <= templateParameter.getStartOffset()) {
                            // This prevent us from infinite instantiation of type
                            //
                            // Example: resolving 'next::type' never finishes because 
                            // we instantiate type T with instantiation of AAA 
                            // where T = T::next
                            //
                            // template <typename T>
                            // struct AAA {
                            //   typedef AAA<T::next> next;
                            //   typedef T type;
                            // }
                            //
                            // TODO: it is better to use not lastResolvedParameters, 
                            // but just iterator instead of mapping to make sure that we
                            // never go backward.                            
                            return null;
                        }
                    }
                }
                lastResolvedParameters.add(templateParameter);
                return instantiatedParam;                
            }
            return null;  
        }       
        
        // TODO: here is the only place where instantiations are created with incomplete mappings! 
        // Think if it can affect something!
        //
        // Method creates new instantiation with altered mapping. New mapping do not contain
        // already resolved template parameters and parameters after them.
        public CsmInstantiation alterInstantiation(CsmInstantiation instantiation)  {
            if (!lastResolvedParameters.isEmpty() && CsmKindUtilities.isTemplate(instantiation.getTemplateDeclaration())) {
                CsmTemplateParameter firstResolved = null;
                CsmTemplateParameter firstParam = null;
                Map<CsmTemplateParameter, CsmSpecializationParameter> mapping = instantiation.getMapping();
                for (CsmTemplateParameter templateParam : mapping.keySet()) {
                    if (lastResolvedParameters.contains(templateParam)) {
                        if (firstResolved == null) {
                            firstResolved = templateParam;
                        } else if (templateParam.getStartOffset() <= firstResolved.getStartOffset()) {
                            firstResolved = templateParam;
                        }
                    }
                    if (firstParam == null) {
                        firstParam = templateParam;
                    } else if (templateParam.getStartOffset() <= firstParam.getStartOffset()) {
                        firstParam = templateParam;
                    }
                }
                if (firstResolved == null) {
                    // must be assert firstResolved != null instead of this
                    return instantiation;
                } else if (firstResolved != firstParam) {
                    Map<CsmTemplateParameter, CsmSpecializationParameter> newMapping = new HashMap<>();
                    for (Map.Entry<CsmTemplateParameter, CsmSpecializationParameter> entry : mapping.entrySet()) {
                        if (entry.getKey().getStartOffset() < firstResolved.getStartOffset()) {
                            newMapping.put(entry.getKey(), entry.getValue());
                        }
                    }
                    CsmObject instantiated = create((CsmTemplate) instantiation.getTemplateDeclaration(), newMapping);
                    if (CsmKindUtilities.isInstantiation(instantiated)) {
                        return (CsmInstantiation) instantiated;
                    }
                } else {
                    return null;
                }
            }
            return instantiation;
        }

        @Override
        protected TemplateParameterResolver clone() {
            return new TemplateParameterResolver(lastResolvedParameters);
        }
    }        
    
    private static class SimpleInstantiationParamsInfo implements InstantiationParametersInfo {
        
        private final List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> parameters;
        
        private final List<CsmType> types;

        public SimpleInstantiationParamsInfo(List<CsmSpecializationParameter> parameters) {
            this.parameters = new ArrayList<>(parameters.size());
            this.types = new ArrayList<>(parameters.size());
            for (CsmSpecializationParameter param : parameters) {
                this.parameters.add(Pair.of(param, Collections.<CsmInstantiation>emptyList()));
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                    this.types.add(((CsmTypeBasedSpecializationParameter) param).getType());
                } else {
                    this.types.add(null);
                }               
            }
        }

        @Override
        public List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getOriginalParams() {
            return parameters;
        }

        @Override
        public List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> getExpandedParams() {
            return parameters;
        }
        
        @Override
        public List<CsmType> getParamsTypes() {
            return types;
        }        

        @Override
        public boolean isVariadic() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public List<CsmSpecializationParameter> getInstParams() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }

        @Override
        public List<String> getParamsTexts() {
            throw new UnsupportedOperationException("Not supported."); // NOI18N
        }
    }
}
