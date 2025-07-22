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

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vita Stejskal
 */
@ServiceProvider(service=Processor.class)
@SupportedAnnotationTypes("org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration") //NOI18N
public class PathRecognizerRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for(Element e : roundEnv.getElementsAnnotatedWith(PathRecognizerRegistration.class)) {
            TypeElement cls = (TypeElement) e;
            PathRecognizerRegistration prr = cls.getAnnotation(PathRecognizerRegistration.class);

            String sourcePathIds = processIds(prr.sourcePathIds());
            String libraryPathIds = processIds(prr.libraryPathIds());
            String binaryLibraryPathIds = processIds(prr.binaryLibraryPathIds());
            String mimeTypes = processMts(prr.mimeTypes());

            if (mimeTypes != null && (sourcePathIds != null || libraryPathIds != null || binaryLibraryPathIds != null)) {
                final LayerBuilder lb = layer(cls);
                File f = instanceFile(lb,
                        "Services/Hidden/PathRecognizers", //NOI18N
                        makeFilesystemName(cls.getQualifiedName().toString()),
                        DefaultPathRecognizer.class,
                        "createInstance", //NOI18N
                        PathRecognizer.class);

                if (sourcePathIds != null) {
                    f.stringvalue("sourcePathIds", sourcePathIds); //NOI18N
                }
                if (libraryPathIds != null) {
                    f.stringvalue("libraryPathIds", libraryPathIds); //NOI18N
                }
                if (binaryLibraryPathIds != null) {
                    f.stringvalue("binaryLibraryPathIds", binaryLibraryPathIds); //NOI18N
                }
                if (mimeTypes != null) {
                    f.stringvalue("mimeTypes", mimeTypes); //NOI18N
                }

                f.write();
            }
        }
        return true;
    }

    private static String processIds(String [] ids) {
        if (ids.length == 0) {
            return null;
        } else if (ids.length == 1 && ids[0].equals("ANY")) {
            return "ANY"; //NOI18N
        } else {
            StringBuilder sb = new StringBuilder();
            for(String s : ids) {
                if (s == null) {
                    continue;
                }

                s = s.trim();
                if (s.length() == 0 || s.equals("ANY")) {
                    continue;
                }

                if (sb.length() > 0) {
                    sb.append(','); //NOI18N
                }
                sb.append(s);
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
    }

    private static String processMts(String [] mts) {
        if (mts == null || mts.length == 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            for(String s : mts) {
                if (s == null) {
                    continue;
                }

                s = s.trim();
                if (s.length() == 0) {
                    continue;
                }

                if (sb.length() > 0) {
                    sb.append(','); //NOI18N
                }
                sb.append(s);
            }
            return sb.length() > 0 ? sb.toString() : null;
        }
    }

    private static File instanceFile(LayerBuilder b, String folder, String name, Class implClass, String factoryMethod, Class... instanceOf) {
        String basename;
        if (name == null) {
            basename = implClass.getName().replace('.', '-'); //NOI18N
            if (factoryMethod != null) {
                basename += "-" + factoryMethod; //NOI18N
            }
        } else {
            basename = name;
        }

        File f = b.file(folder + "/" + basename + ".instance"); //NOI18N
        if (implClass != null) {
            if (factoryMethod != null) {
                f.methodvalue("instanceCreate", implClass.getName(), factoryMethod); //NOI18N
            } else {
                f.stringvalue("instanceClass", implClass.getName()); //NOI18N
            }
        }

        for(Class c : instanceOf) {
            f.stringvalue("instanceOf", c.getName()); //NOI18N
        }

        return f;
    }

    private static String makeFilesystemName(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for(int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append("-"); //NOI18N
            }
        }
        return sb.toString();
    }
}
