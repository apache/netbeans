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

package org.netbeans.modules.java.source.builder;

import java.util.List;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import javax.lang.model.element.*;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import org.netbeans.modules.java.source.base.SourceLevelUtils;

/**
 * Utility methods for working with Element instances.
 */
public class ElementsService {
    private com.sun.tools.javac.code.Types jctypes;
    private Names names;
    private Types types;
    private final boolean allowDefaultMethods;
    
    private static final Context.Key<ElementsService> KEY =
	    new Context.Key<ElementsService>();

    public static ElementsService instance(Context context) {
	ElementsService instance = context.get(KEY);
	if (instance == null)
	    instance = new ElementsService(context);
	return instance;
    }

    protected ElementsService(Context context) {
        context.put(KEY, this);
        jctypes = com.sun.tools.javac.code.Types.instance(context);
        names = Names.instance(context);
        types = JavacTypes.instance(context);
        allowDefaultMethods = SourceLevelUtils.allowDefaultMethods(Source.instance(context));
    }

    /** 
     * The outermost TypeElement which indirectly encloses this element.
     */
    public TypeElement outermostTypeElement(Element element) {
	Element e = element;
	Element prev = null;
	while (e.getKind() != PACKAGE) {
	    prev = e;
	    e = e.getEnclosingElement();
	}
	return prev instanceof TypeElement ? (TypeElement) prev : null;
    }

    /** 
     * The package element which indirectly encloses this element..
     */
    public PackageElement packageElement(Element element) {
	Element e = element;
	while (e.getKind() != PACKAGE) {
	    e = e.getEnclosingElement();
	}
	return (PackageElement)e;
    }
    
    /**
     * Returns true if this element represents a method which overrides a
     * method in one of its superclasses.
     */
    public boolean overridesMethod(ExecutableElement element) {
        MethodSymbol m = (MethodSymbol)element;
        if ((m.flags() & Flags.STATIC) == 0) {
            ClassSymbol owner = (ClassSymbol) m.owner;
            for (Type sup = jctypes.supertype(m.owner.type);
                    sup.hasTag(TypeTag.CLASS);
                    sup = jctypes.supertype(sup)) {
                for (Symbol sym : sup.tsym.members().getSymbolsByName(m.name)) {
                    if (m.overrides(sym, owner, jctypes, true)) 
                        return true;
                }
            }
        }
	return false;
    }
    
    /**
     * Returns true if this element represents a method which 
     * implements a method in an interface the parent class implements.
     */
    public boolean implementsMethod(ExecutableElement element) {
        MethodSymbol m = (MethodSymbol)element;
	TypeSymbol owner = (TypeSymbol) m.owner;
	for (Type type : jctypes.interfaces(m.owner.type)) {
            for (Symbol sym : type.tsym.members().getSymbolsByName(m.name)) {
		if (m.overrides(sym, owner, jctypes, true)) 
		    return true;
	    }
	}
	return false;
    }

    public boolean alreadyDefinedIn(CharSequence name, ExecutableType method, TypeElement enclClass) {
        Type.MethodType meth = ((Type)method).asMethodType();
        ClassSymbol clazz = (ClassSymbol)enclClass;
        Scope scope = clazz.members();
        Name n = names.fromString(name.toString());
        for (Symbol sym : scope.getSymbolsByName(n, Scope.LookupKind.NON_RECURSIVE)) {
            if(sym.type instanceof ExecutableType &&
                    types.isSubsignature(meth, (ExecutableType)sym.type))
                return true;
        }
        return false;
    }
    
    public boolean alreadyDefinedIn(CharSequence name, TypeMirror returnType, List<TypeMirror> paramTypes, TypeElement enclClass) {
        ClassSymbol clazz = (ClassSymbol)enclClass;
        Scope scope = clazz.members();
        Name n = names.fromString(name.toString());
        ListBuffer<Type> buff = new ListBuffer<>();
        for (TypeMirror tm : paramTypes) {
            buff.append((Type)tm);
        }
        for (Symbol sym : scope.getSymbolsByName(n, Scope.LookupKind.NON_RECURSIVE)) {
            if(sym.type instanceof ExecutableType &&
                    jctypes.containsTypeEquivalent(sym.type.asMethodType().getParameterTypes(), buff.toList()) &&
                    jctypes.isSameType(sym.type.asMethodType().getReturnType(), (Type)returnType))
                return true;
        }
        return false;
    }
    
    public boolean isMemberOf(Element e, TypeElement type) {
        return ((Symbol)e).isMemberOf((TypeSymbol)type, jctypes);
    }
    
    public boolean isDeprecated(Element element) {
        Symbol sym = (Symbol)element;
	if ((sym.flags() & Flags.DEPRECATED) != 0 && 
	    (sym.owner.flags() & Flags.DEPRECATED) == 0)
	    return true;
	 
	// Check if this method overrides a deprecated method. 
	TypeSymbol owner = sym.enclClass();
	for (Type sup = jctypes.supertype(owner.type);
                sup.hasTag(TypeTag.CLASS);
                sup = jctypes.supertype(sup)) {
            for (Symbol symbol : sup.tsym.members().getSymbolsByName(sym.name)) {
		if (sym.overrides(symbol, owner, jctypes, true) &&
                        (symbol.flags() & Flags.DEPRECATED) != 0)
		    return true;
	    }
	}
	return false;
    }
    
    public boolean isLocal(Element element) {
        Element encl = element.getEnclosingElement();
        if (encl == null ||
            encl.getKind() == ElementKind.PACKAGE ||
            encl.getKind() == ElementKind.MODULE) {
            return false;
        }
        if (encl.getKind().isClass() || encl.getKind().isInterface()) {
            return isLocal(encl);
        }
        return true;
    }
    
    public CharSequence getFullName(Element element) {
        Symbol sym = (Symbol)element;
        return element instanceof Symbol.ClassSymbol ? 
            ((Symbol.ClassSymbol)element).fullname :
            Symbol.TypeSymbol.formFullName(sym.name, sym.owner);
    }
    
    private static boolean hasImplementation(MethodSymbol msym) {
        long f = msym.flags();
        return ((f & Flags.DEFAULT) != 0) || ((f & Flags.ABSTRACT) == 0);
    }

    public Element getImplementationOf(ExecutableElement method, TypeElement origin) {
        MethodSymbol msym = (MethodSymbol)method;
        MethodSymbol implmethod = (msym).implementation((TypeSymbol)origin, jctypes, true);
        if ((msym.flags() & Flags.STATIC) != 0) {
            // return null if outside of hierarchy, or the method itself if origin extends method's class
            if (jctypes.isSubtype(((TypeSymbol)origin).type,  ((TypeSymbol)((MethodSymbol)method).owner).type)) {
                return method;
            } else {
                return null;
            }
        }
        if (implmethod == null || implmethod == method) {
            //look for default implementations
            if (allowDefaultMethods) {
                com.sun.tools.javac.util.List<MethodSymbol> candidates = jctypes.interfaceCandidates(((TypeSymbol) origin).type, (MethodSymbol) method);
                X: for (com.sun.tools.javac.util.List<MethodSymbol> ptr = candidates; ptr.head != null; ptr = ptr.tail) {
                    MethodSymbol prov = ptr.head;
                    if (prov != null && prov.overrides((MethodSymbol) method, (TypeSymbol) origin, jctypes, true) &&
                        hasImplementation(prov)) {
                        // PENDING: even if `prov' overrides the method, there may be a different method, in different interface, that overrides `method'
                        // 'prov' must override all such compatible methods in order to present a valid implementation of `method'.
                        for (com.sun.tools.javac.util.List<MethodSymbol> sibling = candidates; sibling.head != null; sibling = sibling.tail) {
                            MethodSymbol redeclare = sibling.head;

                            // if the default method does not override the alternative candidate from an interface, then the default will be rejected
                            // as specified in JLS #8, par. 8.4.8
                            if (!prov.overrides(redeclare, (TypeSymbol)origin, jctypes, allowDefaultMethods)) {
                                break X;
                            }
                        }
                        implmethod = prov;
                        break;
                    }
                }
            }
        }
        return implmethod;
    }

    public boolean isSynthetic(Element e) {
        return (((Symbol) e).flags() & Flags.SYNTHETIC) != 0 || (((Symbol) e).flags() & Flags.GENERATEDCONSTR) != 0;
    }
    
    public ExecutableElement getOverriddenMethod(ExecutableElement method) {
        MethodSymbol m = (MethodSymbol)method;
        if ((m.flags() & Flags.STATIC) != 0) {
            return null;
        }
	ClassSymbol origin = (ClassSymbol)m.owner;
        MethodSymbol bridgeCandidate = null;
        for (Type t = jctypes.supertype(origin.type); t.hasTag(TypeTag.CLASS); t = jctypes.supertype(t)) {
            TypeSymbol c = t.tsym;
            for (Symbol sym : c.members().getSymbolsByName(m.name)) {
                if (m.overrides(sym, origin, jctypes, false)) {
                    if ((sym.flags() & Flags.BRIDGE) > 0) {
                        if (bridgeCandidate == null) {
                            bridgeCandidate = (MethodSymbol)sym;
                        }
                    } else {
                        return (MethodSymbol)sym;
                    }
                }
            }
        }
        if (allowDefaultMethods) {
            for (com.sun.tools.javac.util.List<MethodSymbol> candidates = jctypes.interfaceCandidates(((TypeSymbol) origin).type, (MethodSymbol) method);
                 candidates != null; candidates = candidates.tail) {
                MethodSymbol prov = candidates.head;
                if (prov != null && prov != method && m.overrides(prov, origin, jctypes, true) &&
                    hasImplementation(prov)) {
                    if ((prov.flags() & Flags.BRIDGE) > 0) {
                        if (bridgeCandidate == null) {
                            bridgeCandidate = (MethodSymbol)prov;
                        }
                    } else {
                        return (MethodSymbol)prov;
                    }
                }
            }
        }
        return bridgeCandidate;
    }
}
