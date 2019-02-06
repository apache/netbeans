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

import java.io.Reader;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

final class GraalEngine implements ScriptEngine, Invocable {

    private final GraalEngineFactory factory;

    GraalEngine(GraalEngineFactory f) {
        this.factory = f;
    }

    private String id() {
        return factory.id;
    }

    @Override
    public GraalContext getContext() {
        return factory.ctx;
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

    private Value evalImpl(ScriptContext arg1, String src) throws ScriptException {

        try {
            return ((GraalContext) arg1).ctx().eval(id(), src);
        } catch (PolyglotException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader arg0, ScriptContext arg1) throws ScriptException {
        Source src = Source.newBuilder(id(), arg0, null).buildLiteral();
        Value result = ((GraalContext)arg1).ctx().eval(src);
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
    }

    @Override
    public Object get(String arg0) {
        return null;
    }

    @Override
    public Bindings getBindings(int scope) {
        return getContext().getBindings(scope);
    }

    @Override
    public void setBindings(Bindings arg0, int arg1) {
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
        if (!(thiz instanceof Value)) {
            throw new IllegalArgumentException();
        }
        final Value thisValue = (Value) thiz;
        Value fn = thisValue.getMember(name);
        if (!fn.canExecute()) {
            throw new NoSuchMethodException(name);
        }
        Value result = fn.execute(args);
        return unbox(result);
    }

    @Override
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException {
        final Value fn = evalImpl(getContext(), name);
        final Value result = fn.execute(args);
        return unbox(result);
    }

    @Override
    public <T> T getInterface(Class<T> clasz) {
        return getInterface(getContext().ctx().getPolyglotBindings(), clasz);
    }

    @Override
    public <T> T getInterface(Object thiz, Class<T> clasz) {
        if (thiz instanceof Value) {
            return ((Value) thiz).as(clasz);
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
        if (result.isNumber()) {
            return result.as(Number.class);
        }
        if (result.isString()) {
            return result.as(String.class);
        }
        return result;
    }
}
