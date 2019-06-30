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
package org.netbeans.modules.fish.payara.micro.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.netbeans.api.scripting.Scripting;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class TemplateUtil {

    private static final String ENCODING_PROPERTY_NAME = "encoding"; //NOI18N

    private static URL getResourceURL(String resource) {
        ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        return loader.getResource(resource.startsWith("/") ? resource.substring(1) : resource);
    }

    public static InputStream loadResource(String resource) {
        InputStream inputStream = null;
        try {
            inputStream = getResourceURL(resource).openStream();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return inputStream;
    }

    public static String expandTemplate(Reader reader, Map<String, Object> values) {
        StringWriter writer = new StringWriter();
        ScriptEngine eng = getScriptEngine();
        Bindings bind = eng.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
        if (values != null) {
            bind.putAll(values);
        }
        bind.put(ENCODING_PROPERTY_NAME, Charset.defaultCharset().name());
        eng.getContext().setWriter(writer);
        try {
            eng.eval(reader);
        } catch (ScriptException ex) {
            Exceptions.printStackTrace(ex);
        }

        return writer.toString();
    }
    
    
    private static ScriptEngine getScriptEngine() {
        return Scripting.createManager().getEngineByName("freemarker"); // NOI18N
    }
}
