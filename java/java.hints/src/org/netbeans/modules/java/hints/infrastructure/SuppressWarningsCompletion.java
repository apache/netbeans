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
package org.netbeans.modules.java.hints.infrastructure;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;

/**
 *
 * @author lahvac
 */
public class SuppressWarningsCompletion implements Processor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        Set<String> keys = new TreeSet<String>();

        for (String k : WELL_KNOWN) {
            keys.add(k);
        }

        for (HintMetadata hm : RulesManager.getInstance().readHints(null, null, null).keySet()) {//XXX: hack
            for (String sw : hm.suppressWarnings) {
                if (sw == null || sw.length() == 0) {
                    break;
                }
                keys.add(sw);
            }
        }
        
        List<Completion> result = new LinkedList<Completion>();

        for (String k : keys) {
            result.add(new CompletionImpl(k));
        }
        
        return result;
    }

    private static final Collection<? extends String> WELL_KNOWN = Arrays.asList("unchecked", "fallthrough", "deprecation");

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.emptySet();
    }

    private static final Set<String> supportedAnnotationTypes = new HashSet<String>(Arrays.asList(SuppressWarnings.class.getName()));

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {}

    private final class CompletionImpl implements Completion {

        private final String value;
        private final String message;

        public CompletionImpl(String value) {
            this(value, null);
        }

        public CompletionImpl(String value, String message) {
            this.value = '\"' + value + '\"';
            this.message = message;
        }

        public String getValue() {
            return value;
        }

        public String getMessage() {
            return message;
        }
    }
}
