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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NamespaceIndexFilter;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.EnumElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.TraitElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassInstanceCreationInfo;
import org.netbeans.modules.php.editor.model.nodes.EnumDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.TraitDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;

/**
 *
 * @author Radek Matous
 */
abstract class TypeScopeImpl extends ScopeImpl implements TypeScope {

    private Map<String, List<? extends InterfaceScope>> ifaces = new HashMap<>();
    private Collection<QualifiedName> fqIfaces = new ArrayList<>();
    private Set<? super TypeScope> superRecursionDetection = new HashSet<>();
    private Set<? super TypeScope> subRecursionDetection = new HashSet<>();

    TypeScopeImpl(Scope inScope, ClassDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), nodeInfo.getOriginalNode().getBody(), isDeprecated);
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        for (Expression identifier : interfaces) {
            String ifaceName = CodeUtils.extractQualifiedName(identifier);
            if (ifaceName != null) {
                ifaces.put(ifaceName, null);
                fqIfaces.add(VariousUtils.getFullyQualifiedName(QualifiedName.create(ifaceName), nodeInfo.getOriginalNode().getStartOffset(), inScope));
            }
        }
    }

    TypeScopeImpl(Scope inScope, ClassInstanceCreationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), nodeInfo.getOriginalNode().getBody(), isDeprecated);
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        setInterfaces(interfaces, nodeInfo.getOriginalNode().getStartOffset(), inScope);
    }

    TypeScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody(), isDeprecated);
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        setInterfaces(interfaces, nodeInfo.getOriginalNode().getStartOffset(), inScope);
    }

    TypeScopeImpl(Scope inScope, TraitDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody(), isDeprecated);
    }

    TypeScopeImpl(Scope inScope, EnumDeclarationInfo nodeInfo, boolean isDeprecated) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody(), isDeprecated);
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        setInterfaces(interfaces, nodeInfo.getOriginalNode().getStartOffset(), inScope);
    }

    private void setInterfaces(List<? extends Expression> interfaces, int offset, Scope inScope) {
        for (Expression identifier : interfaces) {
            String ifaceName = CodeUtils.extractQualifiedName(identifier);
            if (ifaceName != null) {
                ifaces.put(ifaceName, null);
                fqIfaces.add(VariousUtils.getFullyQualifiedName(QualifiedName.create(ifaceName), offset, inScope));
            }
        }
    }

    protected TypeScopeImpl(Scope inScope, ClassElement element) {
        super(inScope, element, PhpElementKind.CLASS);
        fqIfaces = element.getFQSuperInterfaceNames();
        for (QualifiedName qualifiedName : element.getSuperInterfaces()) {
            ifaces.put(qualifiedName.toString(), null);
        }
    }

    protected TypeScopeImpl(Scope inScope, InterfaceElement element) {
        super(inScope, element, PhpElementKind.IFACE);
        fqIfaces = element.getFQSuperInterfaceNames();
        for (QualifiedName qualifiedName : element.getSuperInterfaces()) {
            ifaces.put(qualifiedName.toString(), null);
        }
    }

    protected TypeScopeImpl(Scope inScope, TraitElement element) {
        super(inScope, element, PhpElementKind.TRAIT);
    }

    protected TypeScopeImpl(Scope inScope, EnumElement element) {
        super(inScope, element, PhpElementKind.ENUM);
    }

    @Override
    public Collection<QualifiedName> getFQSuperInterfaceNames() {
        return Collections.unmodifiableCollection(this.fqIfaces);
    }

    @Override
    public List<? extends String> getSuperInterfaceNames() {
        if (indexedElement instanceof TypeElement) {
            List<String> retval = new ArrayList<>();
            final Set<QualifiedName> superInterfaces = ((TypeElement) indexedElement).getSuperInterfaces();
            for (QualifiedName qualifiedName : superInterfaces) {
                retval.add(qualifiedName.toString());
            }
            return retval;
        }
        return new ArrayList<>(ifaces.keySet());
    }

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "WMI_WRONG_MAP_ITERATOR", justification = "Way too complicated code")
    @Override
    public List<? extends InterfaceScope> getSuperInterfaceScopes() {
        Set<InterfaceScope> retval = new LinkedHashSet<>();
        Set<String> keySet = (indexedElement instanceof TypeElement) ? new HashSet<>(getSuperInterfaceNames()) : ifaces.keySet();
        if (!fqIfaces.isEmpty()) {
            for (QualifiedName qualifiedName : fqIfaces) {
                retval.addAll(IndexScopeImpl.getInterfaces(qualifiedName, this));
            }
        }
        if (retval.isEmpty() && !keySet.isEmpty()) {
            for (String ifaceName : keySet) {
                List<? extends InterfaceScope> iface = ifaces.get(ifaceName);
                if (iface == null) {
                    if (indexedElement == null) {
                        Scope inScope = getInScope();
                        if (inScope instanceof ClassScope || inScope instanceof TraitScope || inScope instanceof EnumScope) {
                            // in case of anonymous class
                            while(!(inScope instanceof NamespaceScope)) {
                                inScope = inScope.getInScope();
                                if (inScope == null) {
                                    break;
                                }
                            }
                        }
                        if (inScope instanceof NamespaceScope) {
                            NamespaceScope top = (NamespaceScope) inScope;
                            NamespaceScopeImpl ps = (NamespaceScopeImpl) top;
                            iface = ModelUtils.filter(ps.getDeclaredInterfaces(), ifaceName);
                            retval.addAll(iface);
                            ifaces.put(ifaceName, iface);
                            /*for (InterfaceScopeImpl interfaceScope : iface) {
                                retval.addAll(interfaceScope.getInterfaces());
                            }*/
                            if (retval.isEmpty() && top instanceof NamespaceScopeImpl) {
                                IndexScope indexScope = ModelUtils.getIndexScope(ps);
                                if (indexScope != null) {
                                    Collection<? extends InterfaceScope> cIfaces = IndexScopeImpl.getInterfaces(QualifiedName.create(ifaceName), this);
                                    ifaces.put(ifaceName, (List<? extends InterfaceScopeImpl>) cIfaces);
                                    for (InterfaceScope interfaceScope : cIfaces) {
                                        retval.add((InterfaceScopeImpl) interfaceScope);
                                    }
                                } else {
                                    //TODO: create it from idx
                                    throw new UnsupportedOperationException();
                                    /*assert iface != null;
                                    ifaces.put(key, iface);*/
                                }
                            }
                        }
                    } else {
                        iface = Collections.emptyList();
                    }
                } else {
                    retval.addAll(iface);
                }
                assert iface != null;
            //duplicatesChecker.addAll(iface);
            }
        }
        return new ArrayList<>(retval);
    }

    @Override
    public Collection<? extends MethodScope> getDeclaredMethods() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = ModelUtils.getIndexScope(this);
            return indexScopeImpl.findMethods(this);
        }
        return filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.METHOD);
            }
        });
    }

    public Collection<? extends MethodScope> findDeclaredMethods(final String queryName, final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            QualifiedName qn = getNamespaceName().append(getName());
            NamespaceIndexFilter filter = new NamespaceIndexFilter(qn.toString());
            List<? extends MethodScope> methods = indexScopeImpl.findMethods(this, queryName, modifiers);
            return filter.filterModelElements(methods, true);
        }

        return filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.METHOD)
                        && ModelElementImpl.nameKindMatch(element.getName(), QuerySupport.Kind.EXACT, queryName)
                        && (modifiers.length == 0 || (element.getPhpModifiers().toFlags() & PhpModifiers.fromBitMask(modifiers).toFlags()) != 0);
            }
        });
    }


    @Override
    public final Collection<? extends ClassConstantElement> getDeclaredConstants() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findClassConstants(this); //NOI18N
        }
        return filter(getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.TYPE_CONSTANT);
            }
        });
    }

    @Override
    public String getNormalizedName() {
        StringBuilder sb = new StringBuilder();
        Collection<QualifiedName> fQSuperInterfaceNames = getFQSuperInterfaceNames();
        if (fQSuperInterfaceNames.isEmpty()) {
            List<? extends String> ifaceNames = getSuperInterfaceNames();
            for (String ifName : ifaceNames) {
                sb.append(ifName);
            }
        } else {
            for (QualifiedName qualifiedName : fQSuperInterfaceNames) {
                sb.append(qualifiedName.toString());
            }
        }
        return sb.toString() + super.getNormalizedName();
    }

    @Override
    public Set<QualifiedName> getSuperInterfaces() {
        Set<QualifiedName> retval = new HashSet<>();
        List<? extends String> superInterfaceNames = getSuperInterfaceNames();
        for (String name : superInterfaceNames) {
            retval.add(QualifiedName.create(name));
        }
        return retval;
    }

    @Override
    public final boolean isClass() {
        return this.getPhpElementKind().equals(PhpElementKind.CLASS);
    }

    @Override
    public final boolean isInterface() {
        return this.getPhpElementKind().equals(PhpElementKind.IFACE);
    }

    @Override
    public final boolean isTrait() {
        return this.getPhpElementKind().equals(PhpElementKind.TRAIT);
    }

    @Override
    public final boolean isTraited() {
        return this.getPhpElementKind().equals(PhpElementKind.TRAIT) || this.getPhpElementKind().equals(PhpElementKind.CLASS);
    }

    @Override
    public final boolean isEnum() {
        return this.getPhpElementKind() == PhpElementKind.ENUM;
    }

    @Override
    public boolean isSuperTypeOf(final TypeScope subType) {
        boolean result = false;
        if (superRecursionDetection.add(subType)) {
            for (InterfaceScope interfaceScope : subType.getSuperInterfaceScopes()) {
                if (interfaceScope.equals(this)) {
                    result = true;
                } else {
                    result = isSuperTypeOf(interfaceScope);
                }
                if (result) {
                    break;
                }
            }
            if (!result && !subType.isInterface()) {
                result = subType.isSubTypeOf(this);
            }
        }
        return result;
    }

    @Override
    public boolean isSubTypeOf(final TypeScope superType) {
        boolean result = false;
        if (subRecursionDetection.add(superType)) {
            if (superType.isInterface()) {
                for (InterfaceScope interfaceScope : getSuperInterfaceScopes()) {
                    if (interfaceScope.equals(superType)) {
                        result = true;
                    } else {
                        result = interfaceScope.isSubTypeOf(superType);
                    }
                    if (result) {
                        break;
                    }
                }
            }
        }
        return result;
    }

}
