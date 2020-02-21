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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.impl.services.MemberResolverImpl;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 * Class for types B and C in the compound type A::B::C
 */
public final class NestedType extends TypeImpl {
    private final CsmType parentType;

    private NestedType(CsmType parent, CsmFile file, boolean packExpansion, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, int startOffset, int endOffset) {
        super(file, packExpansion, pointerDepth, reference, arrayDepth, _const, _volatile, startOffset, endOffset);
        this.parentType = parent;
    }

    public static NestedType create(CsmType parent, CsmFile file, boolean packExpansion, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, int startOffset, int endOffset) {
        return new NestedType(parent, file, packExpansion, pointerDepth, reference, arrayDepth, _const, _volatile, startOffset, endOffset);
    }
    
    private NestedType(CsmType parent, CsmType type) {
        super(type);
        this.parentType = parent;
    }

    public static NestedType create(CsmType parent, CsmType type) {
        return new NestedType(parent, type);
    }

    // package-local - for facory only
    NestedType(NestedType type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        super(type, pointerDepth, reference, arrayDepth, _const, _volatile);
        this.parentType = type.parentType;
    }

    // package-local - for facory only
    NestedType(NestedType type, List<CsmSpecializationParameter> instantiationParams) {
        super(type, instantiationParams);
        this.parentType = type.parentType;
    }

    @Override
    public CsmClassifier getClassifier() {
        return getClassifier(null, false);
    }
    
    @Override
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
        CsmClassifier classifier = _getClassifier();
        if (CsmBaseUtilities.isValid(classifier)) {
            // skip
        } else {
            _setClassifier(null);
            boolean validParentClassifierExamined = false; // flag that parent classifier was resolved and examined
            if (parentType != null) {
                CsmClassifier parentClassifier;
                if(parentType instanceof TypeImpl) {
                    if (instantiations == null) {
                        instantiations = new ArrayList<>();
                    }
                    parentClassifier = ((TypeImpl)parentType).getClassifier(instantiations, false);
                } else {
                    parentClassifier = parentType.getClassifier();                        
                }
                if (CsmBaseUtilities.isValid(parentClassifier)) {
                    validParentClassifierExamined = true;
                    MemberResolverImpl memberResolver = new MemberResolverImpl();
                    classifier = getNestedClassifier(memberResolver, parentClassifier, getOwnText());
                    if (classifier == null) {
                        List<CharSequence> fqn = getFullQName();
                        classifier = ProjectBase.getDummyForUnresolved(fqn.toArray(new CharSequence[fqn.size()]), this);
                    }
                }
            }
            if (!CsmBaseUtilities.isValid(classifier) && !validParentClassifierExamined) {
                // try to resolve qualified name, not through the parent classifier
                List<CharSequence> fqn = getFullQName();
                classifier = renderClassifier(fqn.toArray(new CharSequence[fqn.size()]));
            }
            _setClassifier(classifier);
        }
        if (isInstantiation() && CsmKindUtilities.isTemplate(classifier) && !((CsmTemplate)classifier).getTemplateParameters().isEmpty()) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj= null;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) classifier, this, specialize);
                        if(CsmKindUtilities.isInstantiation(obj)) {
                            if (instantiations == null) {
                                instantiations = new ArrayList<>();
                            }
                            instantiations.add((CsmInstantiation)obj);
                        }
                    }
                } finally {
                    ResolverFactory.releaseResolver(resolver);
                }
            } else {
                obj = ip.instantiate((CsmTemplate) classifier, this);
            }
            if (CsmKindUtilities.isClassifier(obj)) {
                obj = specialize((CsmClassifier) obj, instantiations);
                classifier = (CsmClassifier) obj;
            }
        }
        return classifier;
    }
    
    private List<CharSequence> getFullQName() {
        List<CharSequence> res = new ArrayList<>();
        if (parentType instanceof NestedType) {
            res.addAll(((NestedType)parentType).getFullQName());
        } else if (parentType instanceof TypeImpl) {
            res.add(((TypeImpl)parentType).getOwnText());
        } else if (parentType instanceof TemplateParameterTypeImpl) {
            res.add(((TemplateParameterTypeImpl)parentType).getOwnText());
        }
        res.add(getOwnText());
        return res;
    }

    /*package local*/ CsmType getParent() {
        return parentType;
    }
    
    /*
     * Classifier text should contain specialization of the parent classifier
     */
    @Override
    public CharSequence getClassifierText() {
        if (parentType != null) {
            return CharSequenceUtils.concatenate(parentType.getClassifierText(), getInstantiationText(parentType), "::", super.getClassifierText()); // NOI18N
        } else {
            return CharSequenceUtils.concatenate("::", super.getClassifierText()); // NOI18N
        }
    }

    @Override
    public boolean isInstantiation() {
        return (parentType != null && parentType.isInstantiation()) || super.isInstantiation();
    }

    @Override
    public boolean isTemplateBased() {
        return isTemplateBased(new HashSet<CsmType>());
    }

    @Override
    public boolean isTemplateBased(Set<CsmType> visited) {
        if (parentType instanceof SafeTemplateBasedProvider) {
            if (visited.contains(this)) {
                return false;
            }
            // Fixed IZ#155112 : False positive error highlighting errors on inner types of templates
            // Check for isTemplateBased parent type and then this
            HashSet<CsmType> t = new HashSet<>(visited);
            t.add(this);
            boolean result = ((SafeTemplateBasedProvider)parentType).isTemplateBased(t);
            if (!result) {
                result = super.isTemplateBased(visited);
            }
            visited.add(this);
            return result;
        } else if (parentType != null && parentType.isTemplateBased()) {
            return true;
        } else {
            return super.isTemplateBased(visited);
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CsmType otherParent = ((NestedType)obj).parentType;
        if (parentType == null) {
            if(otherParent == null) {
                return super.equals(obj);
            } else {
                return false;
            }
        } else {
            if(otherParent == null) {
                return false;
            } else {
                return super.equals(obj) && parentType.equals(otherParent);
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        if(parentType != null) {
            hash = 47 * hash + parentType.hashCode();
        }
        return hash;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeType(parentType, output);
    }

    public NestedType(RepositoryDataInput input) throws IOException {
        super(input);
        parentType = PersistentUtils.readType(input);
    }

    /*package*/ static CsmClassifier getNestedClassifier(MemberResolverImpl memberResolver, CsmClassifier parentClassifier, CharSequence ownText) {
        CsmClassifier classifier = null;
        Iterator<CsmClassifier> iter = memberResolver.getNestedClassifiers(parentClassifier, ownText);
        while (iter.hasNext()) {
            classifier = iter.next();
            // stop on the first not class forward classifier
            if (!CsmKindUtilities.isClassForwardDeclaration(classifier)) {
                break;
            }
        }
        return classifier;
    }
}
