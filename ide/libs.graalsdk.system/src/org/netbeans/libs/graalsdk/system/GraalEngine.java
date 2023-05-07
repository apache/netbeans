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
package org.netbeans.libs.graalsdk.system;

import java.io.Reader;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

final class GraalEngine implements ScriptEngine, Invocable {

    private final GraalEngineFactory factory;
    private final ScriptContext langContext;

    GraalEngine(GraalEngineFactory f) {
        this.factory = f;
        this.langContext = new LangContext(factory.id, f.ctx);
    }

    private String id() {
        return factory.id;
    }

    @Override
    public ScriptContext getContext() {
        return langContext;
    }

    @Override
    public GraalEngineFactory getFactory() {
        return factory;
    }

    @Override
    public Object eval(String src, ScriptContext arg1) throws ScriptException {
        Value result = evalImpl(arg1, src);
        return unbox(result);
    }
    
    private static interface ScriptAction {
        public Value run();
    }
    
    private Value handleException(ScriptAction r) throws ScriptException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Engine.class.getClassLoader());
            return r.run();
        } catch (PolyglotException e) {
            if (e.isHostException()) {
                e.initCause(e.asHostException());
                throw e;
            }
            // avoid exposing polyglot stack frames - might be confusing.
            throw new ScriptException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private Value evalImpl(ScriptContext arg1, String src) throws ScriptException {
        return handleException(() -> ((GraalContext) arg1).ctx().eval(id(), src));
    }
    
    @Override
    public Object eval(Reader arg0, ScriptContext arg1) throws ScriptException {
        Source src = Source.newBuilder(id(), arg0, null).buildLiteral();
        Value result = handleException(() -> ((GraalContext)arg1).ctx().eval(src));
        return unbox(result);
    }

    @Override
    public Object eval(String arg0) throws ScriptException {
        return eval(arg0, factory.ctx);
    }

    @Override
    public Object eval(Reader arg0) throws ScriptException {
        return eval(arg0, factory.ctx);
    }

    @Override
    public Object eval(String arg0, Bindings arg1) throws ScriptException {
        throw new ScriptException("Cannot use alternative bindings!");
    }

    @Override
    public Object eval(Reader arg0, Bindings arg1) throws ScriptException {
        throw new ScriptException("Cannot use alternative bindings!");
    }

    @Override
    public void put(String arg0, Object arg1) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(arg0, arg1);
    }

    @Override
    public Object get(String arg0) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(arg0);
    }

    @Override
    public Bindings getBindings(int scope) {
        return GraalContext.executeWithClassLoader(() ->getContext().getBindings(scope),
                Engine.class.getClassLoader());
    }

    @Override
    public void setBindings(Bindings arg0, int arg1) {
        // allow setting the same bindins as already active in the factory;
        // this is done by ScriptEngineManager before it returns the engine.
        if (arg1 == ScriptContext.GLOBAL_SCOPE) {
            if (factory.ctx.getGlobals() == arg0) {
                return;
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Bindings createBindings() {
        return null;
    }

    @Override
    public void setContext(ScriptContext arg0) {
        throw new IllegalStateException();
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException {
        final Value thisValue = factory.ctx.ctx().asValue(thiz);
        if (!thisValue.canInvokeMember(name)) {
            if (!thisValue.hasMember(name)) {
                throw new NoSuchMethodException(name);
            } else {
                throw new NoSuchMethodException(name + " is not a function");
            }
        }
        return unbox(handleException(() -> thisValue.invokeMember(name, args)));
    }
    
    private GraalContext graalContext() {
        return factory.ctx;
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        final Value fn = evalImpl(graalContext(), name);
        return unbox(handleException(() -> fn.execute(args)));
    }

    @Override
    public <T> T getInterface(Class<T> clasz) {
        return getInterface(graalContext().ctx().getPolyglotBindings(), clasz);
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        try {
            if (thiz instanceof Value) {
                return ((Value) thiz).as(clasz);
            }
            Value v = factory.ctx.ctx().asValue(thiz);
            T ret = v.as(clasz);
            if (ret != null) {
                return ret;
            }
        } catch (ClassCastException ex) {
            // the interface is not supported on the value object; ignore.
        }
        if (clasz.isInstance(thiz)) {
            return clasz.cast(thiz);
        }
        return null;
    }

    private Object unbox(Value result) {
        if (result.isNull()) {
            return null;
        }
        return result.as(Object.class);
    }
}
