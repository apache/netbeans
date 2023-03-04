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

package org.netbeans.modules.debugger.jpda.backend.truffle;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.graalvm.polyglot.Engine;

/**
 * Provides the MIME types of languages supported by a GraalVM instance.
 * 
 * @author martin
 */
public class GetMIMETypes {
    
    public static void main(String[] args) throws Exception {
        /*
        Method loadLanguageClass = Engine.class.getDeclaredMethod("loadLanguageClass", String.class);
        loadLanguageClass.setAccessible(true);
        Class polyglotEngineClass = (Class) loadLanguageClass.invoke(null, "com.oracle.truffle.api.vm.PolyglotEngine");
        Object builder = polyglotEngineClass.getDeclaredMethod("newBuilder").invoke(null);
        Object polyglotEngine = builder.getClass().getMethod("build").invoke(builder);
        Map languages = (Map) polyglotEngine.getClass().getMethod("getLanguages").invoke(polyglotEngine);
        languages.values().stream().
                flatMap((l) -> {
                    Stream<String> stream;
                    try {
                        stream = ((Set<String>) l.getClass().getMethod("getMimeTypes").invoke(l)).stream();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return stream;}).distinct().
                forEach((mt) -> System.out.println(mt));
        */
        Engine.create().getLanguages().values().stream().
                flatMap((l) -> l.getMimeTypes().stream()).distinct().
                forEach((mt) -> System.out.println(mt));
        /*
        Set<String> MIMETypes = new HashSet<>();
        Collection<? extends PolyglotEngine.Language> languages = PolyglotEngine.newBuilder().build().getLanguages().values();
        for (PolyglotEngine.Language language : languages) {
            MIMETypes.addAll(language.getMimeTypes());
        }
        for (String mt : MIMETypes) {
            System.out.println(mt);
        }*/
    }
}
