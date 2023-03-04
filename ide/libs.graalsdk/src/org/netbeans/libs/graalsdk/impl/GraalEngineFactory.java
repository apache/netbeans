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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import org.graalvm.polyglot.Language;

final class GraalEngineFactory implements ScriptEngineFactory {

    final String id;
    final Language language;
    final GraalContext ctx;
    private GraalEngine eng;

    GraalEngineFactory(GraalContext ctx, String id, Language language) {
        this.id = id;
        this.language = language;
        this.ctx = ctx;
    }

    @Override
    public String getEngineName() {
        return "GraalVM:" + language.getId();
    }

    @Override
    public String getEngineVersion() {
        return language.getVersion();
    }

    @Override
    public List<String> getExtensions() {
        return Collections.singletonList(id);
    }

    @Override
    public List<String> getMimeTypes() {
        return new ArrayList<>(language.getMimeTypes());
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(language.getName(), getEngineName());
    }

    @Override
    public String getLanguageName() {
        return language.getName();
    }

    @Override
    public String getLanguageVersion() {
        return language.getVersion();
    }

    @Override
    public Object getParameter(String arg0) {
        return null;
    }

    @Override
    public String getMethodCallSyntax(String arg0, String arg1, String... arg2) {
        return null;
    }

    @Override
    public String getOutputStatement(String arg0) {
        return null;
    }

    @Override
    public String getProgram(String... arg0) {
        return null;
    }

    @Override
    public ScriptEngine getScriptEngine() {
        // return the same instance to indicate the engines actually
        // retain all the context through Polyglot Context object.
        synchronized (this) {
            if (eng == null) {
                eng = new GraalEngine(this);
            }
            return eng;
        }
    }

    @Override
    public String toString() {
        return "GraalEngineFactory[" + id + "]";
    }
}
