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

package org.openide.awt;

import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static javax.swing.Action.ACTION_COMMAND_KEY;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;

final class InjectorExactlyOne extends ContextAction.Performer<Object> {
    public InjectorExactlyOne(Map fo) {
        super(fo);
    }

    @Override
    protected Object createDelegate(Provider everything, List<?> data) {
        if (data.size() != 1) {
            return null;
        }
        String clazz = (String) delegate.get("injectable"); // NOI18N
        String type = (String) delegate.get("type"); // NOI18N
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
        if (l == null) {
            l = Thread.currentThread().getContextClassLoader();
        }
        if (l == null) {
            l = Actions.class.getClassLoader();
        }
        try {
            Class<?> typeC = Class.forName(type, true, l);
            Class<?> clazzC = Class.forName(clazz, true, l);
            Constructor c = clazzC.getConstructor(typeC);
            ActionListener action = (ActionListener) c.newInstance(data.get(0));
            return action;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object o = delegate.get("key"); // NOI18N
        if (o == null) {
            o = delegate.get(ACTION_COMMAND_KEY);
        }
        Object d= instDelegate == null ? null : instDelegate.get();
        sb.append("PerformerONE{id = ").append(Objects.toString(o))
                .append(", del = ").append(Objects.toString(d))
                .append(", injectable = ").append(delegate.get("injectable"))
                .append(", type = ").append(delegate.get("type"))
                .append("}");
        return sb.toString();
    }
}
