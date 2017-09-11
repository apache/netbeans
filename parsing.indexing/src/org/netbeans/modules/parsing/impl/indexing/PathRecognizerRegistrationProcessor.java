/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
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
