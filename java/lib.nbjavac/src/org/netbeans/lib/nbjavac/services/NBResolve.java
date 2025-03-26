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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 *
 * @author lahvac
 */
public class NBResolve extends Resolve {
    public static NBResolve instance(Context context) {
        Resolve instance = context.get(resolveKey);
        if (instance == null)
            instance = new NBResolve(context);
        return (NBResolve) instance;
    }

    public static void preRegister(Context context) {
        context.put(resolveKey, new Context.Factory<Resolve>() {
            @Override public Resolve make(Context c) {
                return new NBResolve(c);
            }
        });
    }

    private final Symtab syms;
    boolean inStringTemplate;

    protected NBResolve(Context ctx) {
        super(ctx);
        syms = Symtab.instance(ctx);
    }

    private boolean accessibleOverride;
    
    public void disableAccessibilityChecks() {
        accessibleOverride = true;
    }
    
    public void restoreAccessbilityChecks() {
        accessibleOverride = false;
    }
    
    @Override
    public boolean isAccessible(Env<AttrContext> env, Type site, Symbol sym, boolean checkInner) {
        if (accessibleOverride) return true;
        return super.isAccessible(env, site, sym, checkInner);
    }

    @Override
    public boolean isAccessible(Env<AttrContext> env, TypeSymbol c, boolean checkInner) {
        if (accessibleOverride) return true;
        return super.isAccessible(env, c, checkInner);
    }

    public static boolean isStatic(Env<AttrContext> env) {
        return Resolve.isStatic(env);
    }

    @Override
    public Symbol.MethodSymbol resolveInternalMethod(JCDiagnostic.DiagnosticPosition pos, Env<AttrContext> env, Type site, Name name, List<Type> argtypes, List<Type> typeargtypes) {
        try {
            return super.resolveInternalMethod(pos, env, site, name, argtypes, typeargtypes);
        } catch (FatalError ex) {
            if (inStringTemplate) {
                return new Symbol.MethodSymbol(0, name, new MethodType(argtypes, syms.errType, List.nil(), syms.methodClass), syms.noSymbol);
            }
            throw ex;
        }
    }

}
