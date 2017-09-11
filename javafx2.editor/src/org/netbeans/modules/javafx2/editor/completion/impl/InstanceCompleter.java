/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.Collection;
import java.util.Iterator;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.javafx2.editor.completion.beans.FxBean;
import org.netbeans.modules.javafx2.editor.completion.model.FxInstance;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;

/**
 * Abstract class, which completes attributes, or member elements for
 * object instances.
 * 
 * @author sdedic
 */
abstract class InstanceCompleter implements Completer, Completer.Factory {
    protected final FxInstance          instance;
    protected final boolean             attribute;
    protected final CompletionContext   ctx;
    protected final TypeElement         instanceType;
    
    private FxBean beanInfo;
    
    protected InstanceCompleter() {
        instance = null;
        attribute = false;
        ctx = null;
        instanceType = null;
    }

    protected InstanceCompleter(FxInstance instance, boolean attribute, CompletionContext context) {
        this.instance = instance;
        this.attribute = attribute;
        this.ctx = context;
        if (instance.getJavaType() != null) {
            instanceType = instance.getJavaType().resolve(ctx.getCompilationInfo());
        } else {
            instanceType = null;
        }
    }
    
    protected abstract InstanceCompleter createCompleter(FxInstance instance, boolean attribute, CompletionContext ctx);
    
    protected FxBean getBeanInfo() {
        if (beanInfo == null) {
            beanInfo = ctx.getBeanInfo(instance);
        }
        return beanInfo;
    }
    
    @Override
    public Completer createCompleter(final CompletionContext ctx) {
        FxNode parent = ctx.getElementParent();
        FxInstance enclosingInstance = null;
        
        if (parent != null && parent.getKind() == FxNode.Kind.Instance) {
            enclosingInstance = (FxInstance)parent;
        }
        
        if (enclosingInstance == null) {
            return null;
        }
        
        switch (ctx.getType()) {
            case CHILD_ELEMENT:
                // if the enclosing element was a non-implicit PropertySetter, the
                // instance was null, so here we have only property elements in instances.
            case PROPERTY_ELEMENT:
            case ROOT:
            case BEAN:
                return createCompleter(enclosingInstance, false, ctx);
                
            case PROPERTY:
                return createCompleter(enclosingInstance, true, ctx);
                
            default:
                return null;
        }
    }

    protected final Collection<String> filterNames(Collection<String> names) {
        String pref = ctx.getPrefix();
        if (pref.startsWith("<") && !attribute) {
            pref = pref.substring(1);
        }
        if (!pref.isEmpty()) {
            boolean camel = CompletionUtils.isCamelCasePrefix(pref);
            String lowPref = pref.toLowerCase();
            for (Iterator<String> it = names.iterator(); it.hasNext(); ) {
                String n = it.next();
                if (camel) {
                    if (!CompletionUtils.startsWithCamelCase(n, pref)) {
                        it.remove();
                    }
                } else if (!n.toLowerCase().startsWith(lowPref)) {
                    it.remove();
                }
            }
            return names;
        } else {
            return names;
        }
    }

}
