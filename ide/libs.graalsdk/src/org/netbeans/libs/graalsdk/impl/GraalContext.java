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
package org.netbeans.libs.graalsdk.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.openide.util.io.ReaderInputStream;

final class GraalContext implements ScriptContext {
    private final static String ALLOW_ALL_ACCESS = "allowAllAccess"; // NOI18N
    private Context ctx;
    private final WriterOutputStream writer = new WriterOutputStream(new OutputStreamWriter(System.out));
    private final WriterOutputStream errorWriter = new WriterOutputStream(new OutputStreamWriter(System.err));
    private Reader reader;
    private final Bindings globals;
    private SimpleBindings bindings;
    private boolean allowAllAccess;

    // BEGIN: org.netbeans.libs.graalsdk.impl.GraalContext#SANDBOX
    private static final HostAccess SANDBOX = HostAccess.newBuilder().
            allowPublicAccess(true).
            allowArrayAccess(true).
            allowListAccess(true).
            allowAllImplementations(true).
            denyAccess(Class.class).
            denyAccess(Method.class).
            denyAccess(Field.class).
            denyAccess(Proxy.class).
            denyAccess(Object.class, false).
            build();
    // END: org.netbeans.libs.graalsdk.impl.GraalContext#SANDBOX

    GraalContext(Bindings globals) {
        this.globals = globals;
    }

    synchronized final Context ctx() {
        if (ctx == null) {
            final Context.Builder b = Context.newBuilder();
            b.out(writer);
            b.err(errorWriter);
            if (reader != null) {
                try {
                    b.in(new ReaderInputStream(reader, "UTF-8"));
                } catch (IOException ex) {
                    throw raise(RuntimeException.class, ex);
                }
            }
            b.allowPolyglotAccess(PolyglotAccess.ALL);
            if (Boolean.TRUE.equals(getAttribute(ALLOW_ALL_ACCESS, ScriptContext.GLOBAL_SCOPE))) {
                b.allowHostAccess(HostAccess.ALL);
                b.allowAllAccess(true);
            } else {
                b.allowHostAccess(SANDBOX);
            }
            ctx = b.build();
        }
        return ctx;
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Bindings getBindings(int scope) {
        assertGlobalScope(scope);
        if (bindings == null) {
            Map<String,Object> map = ctx().getPolyglotBindings().as(Map.class);
            bindings = new SimpleBindings(map);
        }
        return bindings;
    }

    private void assertGlobalScope(int scope) throws IllegalArgumentException {
        if (scope != GLOBAL_SCOPE) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        assertGlobalScope(scope);
        if (ALLOW_ALL_ACCESS.equals(name)) {
            if (this.ctx == null) {
                this.allowAllAccess = Boolean.TRUE.equals(value);
                return;
            }
            throw new IllegalStateException();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public Object getAttribute(String name, int scope) {
        assertGlobalScope(scope);
        if (ALLOW_ALL_ACCESS.equals(name)) {
            if (this.allowAllAccess) {
                return true;
            }
        }
        return globals == null ? null : globals.get(name);
    }

    @Override
    public Object removeAttribute(String name, int scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public int getAttributesScope(String name) {
        return -1;
    }

    @Override
    public Writer getWriter() {
        return writer.getWriter();
    }

    @Override
    public Writer getErrorWriter() {
        return errorWriter.getWriter();
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer.setWriter(writer);
    }

    @Override
    public void setErrorWriter(Writer writer) {
        this.errorWriter.setWriter(writer);
    }

    @Override
    public Reader getReader() {
        return this.reader;
    }

    @Override
    public void setReader(Reader reader) {
        if (ctx != null) {
            throw new IllegalStateException("Too late. Context has already been created!");
        }
        this.reader = reader;
    }

    @Override
    public List<Integer> getScopes() {
        return Collections.nCopies(1, GLOBAL_SCOPE);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Exception> T raise(Class<T> aClass, Throwable ex) throws T {
        throw (T) ex;
    }
}
