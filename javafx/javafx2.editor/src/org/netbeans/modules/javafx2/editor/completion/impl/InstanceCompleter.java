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
