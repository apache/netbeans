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
package org.netbeans.modules.sendopts;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Completion;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@ServiceProvider(service=Processor.class)
public final class OptionAnnotationProcessor implements Processor {
    private Processor delegate;
    private String msg;

    @Override
    public Set<String> getSupportedOptions() {
        if (delegate() != null) {
            return delegate().getSupportedOptions();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        if (delegate() != null) {
            return delegate().getSupportedAnnotationTypes();
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (delegate() != null) {
            return delegate().getSupportedSourceVersion();
        } else {
            return SourceVersion.latest();
        }
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        if (delegate() != null) {
            delegate().init(processingEnv);
        } else {
            processingEnv.getMessager().printMessage(
                Diagnostic.Kind.NOTE, 
                "Please add org.openide.filesystems module on classpath to generate declarative registration for @Arg" // NO18N
            );
            if (msg != null) {
                processingEnv.getMessager().printMessage(
                    Diagnostic.Kind.NOTE, msg
                );
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (delegate() != null) {
            return delegate().process(annotations, roundEnv);
        } else {
            return true;
        }
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        if (delegate() != null) {
            return delegate().getCompletions(element, annotation, member, userText);
        } else {
            return Collections.emptySet();
        }
    }
    private Processor delegate() {
        if (delegate == null) {
            try {
                delegate = new OptionAnnotationProcessorImpl();
            } catch (LinkageError ex) {
                msg = ex.getMessage();
            }
        }
        return delegate;
    }
}
