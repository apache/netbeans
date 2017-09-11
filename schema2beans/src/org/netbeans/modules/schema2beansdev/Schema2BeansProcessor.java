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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.schema2beansdev;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import org.apache.tools.ant.util.FileUtils;
import org.netbeans.modules.schema2beans.Schema2Beans;
import org.netbeans.modules.schema2beans.Schema2Beans.Multiple;
import org.netbeans.modules.schema2beansdev.GenBeans.Config;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Schema2BeansProcessor extends AbstractProcessor {

    public @Override Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
                Schema2Beans.class.getCanonicalName(),
                Multiple.class.getCanonicalName()));
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.errorRaised() || roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Schema2Beans.class)) {
            handle(e, e.getAnnotation(Schema2Beans.class));
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Multiple.class)) {
            for (Schema2Beans s2b : e.getAnnotation(Multiple.class).value()) {
                handle(e, s2b);
            }
        }
        return true;
    }

    private void handle(final Element e, Schema2Beans s2b) {
        try {
            Config config = new Config();
            config.setAuto(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            config.setMessageOut(ps);
            final String pkg = ((PackageElement) e).getQualifiedName().toString();
            config.setPackagePath(pkg);
            config.setOutputStreamProvider(new GenBeans.OutputStreamProvider() {
                public OutputStream getStream(String dir, String name, String extension) throws IOException {
                    if (!dir.replace('\\', '/').endsWith(pkg.replace('.', '/'))) {
                        throw new IOException("Unexpected dir: " + dir);
                    }
                    if (!extension.equals("java")) {
                        throw new IOException("Unexpected extension: " + extension);
                    }
                    String nameAndExt = name + "." + extension;
                    try {
                        return processingEnv.getFiler().createSourceFile(pkg + "." + name, e).openOutputStream();
                    } catch (FilerException x) {
                        processingEnv.getMessager().printMessage(Kind.WARNING, "ignoring attempt to regenerate " + nameAndExt/*, e*/);
                        return new ByteArrayOutputStream(); // XXX could check that the same contents are written
                    }
                }
                public boolean isOlderThan(String dir, String name, String extension, long time) throws IOException {
                    return true;
                }
            });
            FileObject schema = findResource(s2b.schema(), pkg);
            processingEnv.getMessager().printMessage(Kind.NOTE, "generating beans in " + pkg/*, e*/);
            config.setFileIn(schema.openInputStream());
            String inputUri = fileObjectToUri(schema).toString();
            processingEnv.getMessager().printMessage(Kind.NOTE, "parsing: " + inputUri);
            config.setInputURI(inputUri);
            config.setJava5(s2b.java5());
            switch (s2b.schemaType()) {
            case DTD:
                config.setSchemaType(Config.DTD);
                break;
            case XML_SCHEMA:
                config.setSchemaType(Config.XML_SCHEMA);
                break;
            default:
                assert false;
            }
            if (s2b.mddFile().length() > 0) {
                try {
                    FileObject mdd = findResource(s2b.mddFile(), pkg);
                    config.setMddIn(mdd.openInputStream());
                } catch (FileNotFoundException ex) {
                    // try to create one
                    FileObject mdd = createResource(s2b.mddFile(), pkg);
                    File f = new File(fileObjectToUri(mdd));
                    f.getParentFile().mkdirs();
                    processingEnv.getMessager().printMessage(Kind.NOTE, "going to create new MDD file at " + f.getAbsolutePath());
                    config.setMddFile(f);
                }
            }
            switch (s2b.outputType()) {
            case TRADITIONAL_BASEBEAN:
                config.setOutputType(Config.OUTPUT_TRADITIONAL_BASEBEAN);
                break;
            case JAVABEANS:
                config.setOutputType(Config.OUTPUT_JAVABEANS);
                break;
            default:
                assert false;
            }
            config.setGenerateValidate(s2b.validate());
            config.setRemoveUnreferencedNodes(s2b.removeUnreferencedNodes());
            config.setAttributesAsProperties(s2b.attrProp());
            config.setGenerateHasChanged(s2b.generateHasChanged());
            if (s2b.commonInterface().length() > 0) {
                config.setGenerateCommonInterface(s2b.commonInterface());
            }
            config.setUseInterfaces(s2b.useInterfaces());
            config.setExtendBaseBean(s2b.extendBaseBean());
            config.setFinder(s2b.finder()); // XXX refactor BeanBuilder.processFinder to check syntax
            if (s2b.docRoot().length() > 0) {
                config.setDocRoot(s2b.docRoot());
            }
            config.setGenerateInterfaces(s2b.generateInterfaces());
            config.setStandalone(s2b.standalone());
            try {
                GenBeans.doIt(config);
            } finally {
                ps.flush();
                for (String line : baos.toString().split("\n")) {
                    if (line.length() > 0) {
                        processingEnv.getMessager().printMessage(Kind.NOTE, line/*, e*/);
                    }
                }
            }
        } catch (Exception x) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to process", e);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            x.printStackTrace(ps);
            ps.close();
            processingEnv.getMessager().printMessage(Kind.ERROR, baos.toString());
        }
    }

    private FileObject findResource(String path, String pkg) throws URISyntaxException, IOException {
        // XXX LayerBuilder has standard versions of this logic now
        String abspath;
        if (path.startsWith("/")) {
            abspath = path.substring(1);
        } else {
            abspath = new URI(null, pkg.replace('.', '/') + "/", null).resolve(new URI(null, path, null)).getPath();
        }
        FileObject f = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", abspath);
        try { // #181355
            f.openInputStream().close();
        } catch (FileNotFoundException x) {
            f = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", abspath);
        }
        return f;
    }

    private FileObject createResource(String path, String pkg) throws URISyntaxException, IOException {
        // XXX LayerBuilder has standard versions of this logic now
        String abspath;
        if (path.startsWith("/")) {
            abspath = path.substring(1);
        } else {
            abspath = new URI(null, pkg.replace('.', '/') + "/", null).resolve(new URI(null, path, null)).getPath();
        }
        return processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", abspath);
    }

    /** Workaround for JRE #6419926 */
    private URI fileObjectToUri(FileObject f) throws URISyntaxException {
        URI u = f.toUri();
        if (u.getScheme() == null) {
            u = new URI("file", u.getPath(), u.getFragment());
        }
        // XXX note that File.toURI is broken for UNC paths: JDK #6916645
        return u;
    }

}
