/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.util.Context;

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

    protected NBResolve(Context ctx) {
        super(ctx);
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
    
}
