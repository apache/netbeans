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
package org.netbeans.modules.apisupport.project.copyap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("*")
@SupportedOptions("copy.files")
public class CopyAP extends AbstractProcessor {

    private boolean firstRound = true;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (firstRound) {
            for (String toCopy : processingEnv.getOptions().getOrDefault("copy.files", "").split(",")) {
                if (toCopy.isEmpty()) {
                    continue;
                }
                String[] nameAndFile = toCopy.split("=", 2);
                try (OutputStream target = processingEnv.getFiler()
                                                        .createSourceFile(nameAndFile[0])
                                                        .openOutputStream()) {
                    Files.copy(Paths.get(nameAndFile[1]), target);
                } catch (IOException ex) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, "Error occurred: " + ex.getMessage());
                }
            }
            firstRound = false;
        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
