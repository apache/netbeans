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
package org.netbeans.libs.graalsdk.impl;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import org.graalvm.polyglot.Context;

/**
 *
 * @author sdedic
 */
final class LangContext implements ScriptContext {
    final String   langId;
    final GraalContext  global;
    Bindings langBindings;

    public LangContext(String langId, GraalContext global) {
        this.langId = langId;
        this.global = global;
    }

    @Override
    public void setBindings(Bindings arg0, int arg1) {
        throw new UnsupportedOperationException("Not supported."); 
    }

    @Override
    public Bindings getBindings(int scope) {
        if (scope == ENGINE_SCOPE) {
            synchronized (this) {
                if (langBindings != null) {
                    return langBindings;
                }
            }
            if (langBindings == null) {
                Context c = global.ctx();
                synchronized (this) {
                    if (langBindings == null) {
                        langBindings = new SimpleBindings(c.getBindings(langId).as(Map.class));
                    }
                }
            }
            return langBindings;
        }
        return global.getBindings(scope);
    }

    @Override
    public void setAttribute(String key, Object value, int scope) {
        if (scope == GLOBAL_SCOPE) {
            global.setAttribute(key, value, scope);
            return;
        }
        getBindings(scope).put(key, value);
    }

    @Override
    public Object getAttribute(String key, int scope) {
        if (scope == GLOBAL_SCOPE) {
            return global.getAttribute(key, scope);
        }
        return getBindings(scope).get(key);
    }

    @Override
    public Object removeAttribute(String key, int scope) {
        if (scope == GLOBAL_SCOPE) {
            return global.removeAttribute(key, scope);
        }
        return getBindings(scope).remove(key);
    }

    @Override
    public Object getAttribute(String key) {
        Bindings eb = getBindings(ENGINE_SCOPE);
        if (eb.containsKey(key)) {
            return eb.get(key);
        } else {
            return global.getAttribute(key);
        }
    }

    @Override
    public int getAttributesScope(String key) {
        Bindings eb = getBindings(ENGINE_SCOPE);
        if (eb.containsKey(key)) {
            return GLOBAL_SCOPE;
        } else {
            return global.getAttributesScope(key);
        }
    }

    @Override
    public Writer getWriter() {
        return global.getWriter();
    }

    @Override
    public Writer getErrorWriter() {
        return global.getErrorWriter();
    }

    @Override
    public void setWriter(Writer writer) {
        global.setWriter(writer);
    }

    @Override
    public void setErrorWriter(Writer writer) {
        global.setErrorWriter(writer);
    }

    @Override
    public Reader getReader() {
        return global.getReader();
    }

    @Override
    public void setReader(Reader reader) {
        global.setReader(reader);
    }

    @Override
    public List<Integer> getScopes() {
        return Arrays.asList(ENGINE_SCOPE, GLOBAL_SCOPE);
    }
}
