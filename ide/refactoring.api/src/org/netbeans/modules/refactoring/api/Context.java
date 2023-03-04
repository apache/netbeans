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

package org.netbeans.modules.refactoring.api;

import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.Parameters;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * Context contains "environment" in which the refactoring was invoked.
 * For example, Java refactoring might put instance of ClasspathInfo here
 * 
 * <p>The context acts as a {@code Map<Class,Object>} keyed off the concrete
 * implementation class of the "values".</p>
 * 
 * @see AbstractRefactoring
 * @author Jan Becicka
 */
public final class Context extends Lookup {

    private InstanceContent instanceContent;
    private AbstractLookup delegate;

    Context(InstanceContent instanceContent) {
        super();
        delegate = new AbstractLookup(instanceContent);
        this.instanceContent = instanceContent;
    }

    /**
     * Adds value instance into this context.
     * For example, Java impl. puts instance of ClasspathInfo here.
     * If there is an instance already set for this context, old
     * value is replaced by new one.
     * 
     * @param value the instance the add
     */
    public void add(@NonNull Object value) {
        Parameters.notNull("value", value); // NOI18N
        remove(value.getClass());
        instanceContent.add(value);
    }
    
    /**
     * Removes instance from this context.
     * 
     * @param clazz the class to remove the instance of
     * 
     * @since 1.24
     */
    public void remove(@NonNull Class<?> clazz) {
        Parameters.notNull("clazz", clazz); // NOI18N
        Object old = lookup(clazz);
        if (old!=null) {
            instanceContent.remove(old);
        }
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        return delegate.lookup(clazz);
    }

    @Override
    public <T> Result<T> lookup(Template<T> template) {
        return delegate.lookup(template);
    }
}
