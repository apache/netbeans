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

package org.openide.filesystems.annotations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import org.openide.util.NbBundle.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convenience class for generating fragments of an XML layer.
 * @see LayerGeneratingProcessor#layer
 * @since org.openide.filesystems 7.15
 */
public final class LayerBuilder {

    private final Document doc;
    private final Element originatingElement;
    private final ProcessingEnvironment processingEnv;
    private final List<File> unwrittenFiles = new LinkedList<File>();

    LayerBuilder(Document document, Element/*or null*/ originatingElement, ProcessingEnvironment/* or null*/ processingEnv) {
        this.doc = document;
        this.originatingElement = originatingElement;
        this.processingEnv = processingEnv;
    }

    /**
     * Adds a file to the layer.
     * You need to {@link File#write} it in order to finalize the effect.
     * @param path the full path to the desired file in resource format, e.g. {@code "Menu/File/exit.instance"}
     * @return a file builder
     */
    public File file(String path) {
        if (!path.matches("[^/]+(/[^/]+)*")) {
            throw new IllegalArgumentException(path);
        }
        File f = new File(path, false);
        unwrittenFiles.add(f);
        return f;
    }

    /**
     * Adds a folder to the layer.
     * You need to {@link File#write} it in order to finalize the effect.
     * <p>Normally just using {@link #file} suffices, since parent folders are
     * created as needed, but you may use this method if you wish to create a folder
     * (possibly with some attributes) without necessarily creating any children.
     * @param path the full path to the desired folder in resource format, e.g. {@code "Menu/File"}
     * @return a file builder
     * @since org.openide.filesystems 7.26
     */
    public File folder(String path) {
        File f = new File(path, true);
        unwrittenFiles.add(f);
        return f;
    }

    void close() {
        for (File f : unwrittenFiles) {
            if (f.getPath().startsWith("dummy/")) {
                // ActionProcessor calls instanceFile purely to check for LayerGenerationException.
                // Better would be to factor out the type-checking into its own set of utility methods.
                continue;
            }
            processingEnv.getMessager().printMessage(Kind.WARNING, "layer file " + f.getPath() + " was never written");
        }
        unwrittenFiles.clear();
    }

    /**
     * Generates an instance file whose {@code InstanceCookie} would load the associated class or method.
     * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @param type a type to which the instance ought to be assignable, or null to skip this check
     * @return an instance file (call {@link File#write} to finalize)
     * @throws IllegalArgumentException if the builder is not associated with exactly one
     *                                  {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @throws LayerGenerationException if the associated element would not be loadable as an instance of the specified type
     */
    public File instanceFile(String path, String name, Class<?> type) throws IllegalArgumentException, LayerGenerationException {
        return instanceFile(path, name, type, null, null);
    }
    /**
     * Generates an instance file whose {@code InstanceCookie} would load the associated class or method.
     * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @param type a type to which the instance ought to be assignable, or null to skip this check
     * @param annotation as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @param annotationMethod as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @return an instance file (call {@link File#write} to finalize)
     * @throws IllegalArgumentException if the builder is not associated with exactly one
     *                                  {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @throws LayerGenerationException if the associated element would not be loadable as an instance of the specified type
     * @since 7.50
     */
    public File instanceFile(String path, String name, Class<?> type, Annotation annotation, String annotationMethod) throws IllegalArgumentException, LayerGenerationException {
        String[] clazzOrMethod = instantiableClassOrMethod(type, annotation, annotationMethod);
        String clazz = clazzOrMethod[0];
        String method = clazzOrMethod[1];
        String basename;
        if (name == null) {
            basename = clazz.replace('.', '-');
            if (method != null) {
                basename += "-" + method;
            }
        } else {
            basename = name;
        }
        LayerBuilder.File f = file(path + "/" + basename + ".instance");
        if (method != null) {
            f.methodvalue("instanceCreate", clazz, method);
        } else if (name != null) {
            f.stringvalue("instanceClass", clazz);
        } // else name alone suffices
        return f;
    }

    /**
     * Generates an instance file that is <em>not initialized</em> with an instance.
     * Useful for {@link LayerGeneratingProcessor}s which define layer fragments
     * which indirectly instantiate Java objects from the annotated code via a generic factory method.
     * Invoke the factory using {@link File#methodvalue} on {@code instanceCreate}
     * and configure it with a {@link File#instanceAttribute} appropriate to the factory.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @return an instance file (call {@link File#write} to finalize)
     * @throws IllegalArgumentException if the builder is not associated with exactly one
     *                                  {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @throws LayerGenerationException if the associated element would not be loadable as an instance 
     * @since org.openide.filesystems 7.27
     */
    public File instanceFile(String path, String name) throws IllegalArgumentException, LayerGenerationException {
        return instanceFile(path, name, null, null);
    }
    /**
     * Generates an instance file that is <em>not initialized</em> with an instance.
     * Useful for {@link LayerGeneratingProcessor}s which define layer fragments
     * which indirectly instantiate Java objects from the annotated code via a generic factory method.
     * Invoke the factory using {@link File#methodvalue} on {@code instanceCreate}
     * and configure it with a {@link File#instanceAttribute} appropriate to the factory.
     * <p>While you can pick a specific instance file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param path path to folder of instance file, e.g. {@code "Menu/File"}
     * @param name instance file basename, e.g. {@code "my-menu-Item"}, or null to pick a name according to the element
     * @param annotation as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @param annotationMethod as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @return an instance file (call {@link File#write} to finalize)
     * @throws IllegalArgumentException if the builder is not associated with exactly one
     *                                  {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
     * @throws LayerGenerationException if the associated element would not be loadable as an instance
     * @since org.openide.filesystems 7.50
     */
    public File instanceFile(String path, String name, Annotation annotation, String annotationMethod) throws IllegalArgumentException, LayerGenerationException {
        String[] clazzOrMethod = instantiableClassOrMethod(null, annotation, annotationMethod);
        String clazz = clazzOrMethod[0];
        String method = clazzOrMethod[1];
        String basename;
        if (name == null) {
            basename = clazz.replace('.', '-');
            if (method != null) {
                basename += "-" + method;
            }
        } else {
            basename = name;
        }
        return file(path + "/" + basename + ".instance");
    }

    private String[] instantiableClassOrMethod(Class<?> type, Annotation annotation, String annotationMethod) throws IllegalArgumentException, LayerGenerationException {
        if (originatingElement == null) {
            throw new IllegalArgumentException("Only applicable to builders with exactly one associated element");
        }
        TypeMirror typeMirror = type != null ?
            processingEnv.getTypeUtils().getDeclaredType(
                processingEnv.getElementUtils().getTypeElement(type.getName().replace('$', '.'))) :
            null;
        switch (originatingElement.getKind()) {
            case CLASS: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) originatingElement).toString();
                if (originatingElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    throw new LayerGenerationException(clazz + " must not be abstract", originatingElement, processingEnv, annotation, annotationMethod);
                }
                {
                    boolean hasDefaultCtor = false;
                    for (ExecutableElement constructor : ElementFilter.constructorsIn(originatingElement.getEnclosedElements())) {
                        if (constructor.getParameters().isEmpty()) {
                            hasDefaultCtor = true;
                            break;
                        }
                    }
                    if (!hasDefaultCtor) {
                        throw new LayerGenerationException(clazz + " must have a no-argument constructor", originatingElement, processingEnv, annotation, annotationMethod);
                    }
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(originatingElement.asType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + " is not assignable to " + typeMirror, originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (!originatingElement.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new LayerGenerationException(clazz + " is not public", originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (((TypeElement) originatingElement).getNestingKind().isNested() && !originatingElement.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + " is nested but not static", originatingElement, processingEnv, annotation, annotationMethod);
                }
                return new String[] {clazz, null};
            }
            case METHOD: {
                String clazz = processingEnv.getElementUtils().getBinaryName((TypeElement) originatingElement.getEnclosingElement()).toString();
                String method = originatingElement.getSimpleName().toString();
                if (!originatingElement.getModifiers().contains(Modifier.STATIC)) {
                    throw new LayerGenerationException(clazz + "." + method + " must be static", originatingElement, processingEnv, annotation, annotationMethod);
                }
                List<? extends VariableElement> params = ((ExecutableElement) originatingElement).getParameters();
                TypeMirror utilMapType = processingEnv.getTypeUtils().getDeclaredType(
                        processingEnv.getElementUtils().getTypeElement("java.util.Map"));
                boolean mapParam = (params.size() == 1 && processingEnv.getTypeUtils().isAssignable(
                        params.get(0).asType(), utilMapType));
                if (!params.isEmpty() && !mapParam) {
                    throw new LayerGenerationException(clazz + "." + method + " must not take arguments", originatingElement, processingEnv, annotation, annotationMethod);
                }
                if (typeMirror != null && !processingEnv.getTypeUtils().isAssignable(((ExecutableElement) originatingElement).getReturnType(), typeMirror)) {
                    throw new LayerGenerationException(clazz + "." + method + " is not assignable to " + typeMirror, originatingElement, processingEnv, annotation, annotationMethod);
                }
                return new String[] {clazz, method};
            }
            default:
                throw new LayerGenerationException("Annotated element is not loadable as an instance", originatingElement, processingEnv, annotation, annotationMethod);
        }
    }

    /**
     * Convenience method to create a shadow file (like a symbolic link).
     * <p>While you can pick a specific shadow file name, if possible you should pass null for {@code name}
     * as using the generated name will help avoid accidental name collisions between annotations.
     * @param target the complete path to the original file (use {@link File#getPath} if you just made it)
     * @param folder the folder path in which to create the shadow, e.g. {@code "Menu/File"}
     * @param name the basename of the shadow file sans extension, e.g. {@code "my-Action"}, or null to pick a default
     * @return a shadow file (call {@link File#write} to finalize)
     */
    public File shadowFile(String target, String folder, String name) {
        if (name == null) {
            name = target.replaceFirst("^.+/", "").replaceFirst("\\.[^./]+$", "");
        }
        return file(folder + "/" + name + ".shadow").stringvalue("originalFile", target);
    }

    /**
     * Validates a resource named in an annotation.
     * <p>Note that the binary compilation classpath for an Ant-based NetBeans module does
     * not include non-public packages.
     * (As of the 7.1 harness it does include non-classfile resources from public packages of module dependencies.)
     * The processorpath does contain all of these but it is not consulted.
     * The classpath for a Maven-based module does contain all resources from dependencies.
     * @param resource an absolute resource path with no leading slash (perhaps the output of {@link #absolutizeResource})
     * @param originatingElement the annotated element; used both for error reporting, and (optionally) for its package
     * @param annotation as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @param annotationMethod as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
     * @param searchClasspath true to search in the binary classpath and not just source path (see caveat about JDK 6)
     * @return the content of the resource, for further validation
     * @throws LayerGenerationException if no such resource can be found
     * @since 7.51
     */
    public FileObject validateResource(String resource, Element originatingElement, Annotation annotation, String annotationMethod, boolean searchClasspath) throws LayerGenerationException {
        if (resource.startsWith("/")) {
            throw new LayerGenerationException("do not use leading slashes on resource paths", originatingElement, processingEnv, annotation, annotationMethod);
        }
        if (searchClasspath) {
            for (Location loc : new Location[] {StandardLocation.SOURCE_PATH, /* #181355 */StandardLocation.CLASS_OUTPUT, StandardLocation.CLASS_PATH, StandardLocation.PLATFORM_CLASS_PATH}) {
                try {
                    FileObject f = processingEnv.getFiler().getResource(loc, "", resource);
                    if (loc.isOutputLocation()) {
                        f.openInputStream().close();
                    }
                    return f;
                } catch (IOException ex) {
                    continue;
                }
            }
            throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
        } else {
            try {
                try {
                    FileObject f = processingEnv.getFiler().getResource(StandardLocation.SOURCE_PATH, "", resource);
                    f.openInputStream().close();
                    return f;
                } catch (FileNotFoundException x) {
                    try {
                        FileObject f = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", resource);
                        f.openInputStream().close();
                        return f;
                    } catch (IOException x2) {
                        throw x;
                    }
                }
            } catch (IOException x) {
                throw new LayerGenerationException("Cannot find resource " + resource, originatingElement, processingEnv, annotation, annotationMethod);
            }
        }
    }

    /**
     * Allows a processor to accept relative resource paths.
     * For example, to produce the output value {@code net/nowhere/lib/icon.png}
     * given an element in the package {@code net.nowhere.app}, the following inputs are permitted:
     * <ul>
     * <li>{@code ../lib/icon.png}
     * <li>{@code /net/nowhere/lib/icon.png}
     * </ul>
     * @param originatingElement the annotated element, used for its package
     * @param resource a possibly relative resource path
     * @return an absolute resource path (with no leading slash)
     * @throws LayerGenerationException in case the resource path is malformed
     * @since 7.51
     */
    public static String absolutizeResource(Element originatingElement, String resource) throws LayerGenerationException {
        if (resource.startsWith("/")) {
            return resource.substring(1);
        } else {
            try {
                return new URI(null, findPackage(originatingElement).replace('.', '/') + "/", null).resolve(new URI(null, resource, null)).getPath();
            } catch (URISyntaxException x) {
                throw new LayerGenerationException(x.toString(), originatingElement);
            }
        }
    }
    private static String findPackage(Element e) {
        switch (e.getKind()) {
        case PACKAGE:
            return ((PackageElement) e).getQualifiedName().toString();
        default:
            return findPackage(e.getEnclosingElement());
        }
    }


    /**
     * Builder for creating a single file entry.
     */
    public final class File {

        private final String path;
        private final boolean folder;
        private final Map<String,String[]> attrs = new LinkedHashMap<String,String[]>();
        private String contents;
        private String url;

        File(String path, boolean folder) {
            this.path = path;
            this.folder = folder;
        }

        /**
         * Gets the path this file is to be created under.
         * @return the configured path, as in {@link #file}
         */
        public String getPath() {
            return path;
        }

        /**
         * Configures the file to have inline text contents.
         * @param contents text to use as the body of the file
         * @return this builder
         */
        public File contents(String contents) {
            if (this.contents != null || url != null || contents == null || folder) {
                throw new IllegalArgumentException();
            }
            this.contents = contents;
            return this;
        }

        /**
         * Configures the file to have external contents.
         * @param url a URL to the body of the file, e.g. {@code "nbresloc:/org/my/module/resources/definition.xml"}
         *            or more commonly an absolute resource path such as {@code "/org/my/module/resources/definition.xml"}
         * @return this builder
         */
        public File url(String url) {
            if (contents != null || this.url != null || url == null || folder) {
                throw new IllegalArgumentException();
            }
            this.url = url;
            return this;
        }

        /**
         * Adds a string-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File stringvalue(String attr, String value) {
            attrs.put(attr, new String[] {"stringvalue", value});
            return this;
        }

        /**
         * Adds a byte-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File bytevalue(String attr, byte value) {
            attrs.put(attr, new String[] {"bytevalue", Byte.toString(value)});
            return this;
        }

        /**
         * Adds a short-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File shortvalue(String attr, short value) {
            attrs.put(attr, new String[] {"shortvalue", Short.toString(value)});
            return this;
        }

        /**
         * Adds an int-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File intvalue(String attr, int value) {
            attrs.put(attr, new String[] {"intvalue", Integer.toString(value)});
            return this;
        }

        /**
         * Adds a long-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File longvalue(String attr, long value) {
            attrs.put(attr, new String[] {"longvalue", Long.toString(value)});
            return this;
        }

        /**
         * Adds a float-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File floatvalue(String attr, float value) {
            attrs.put(attr, new String[] {"floatvalue", Float.toString(value)});
            return this;
        }

        /**
         * Adds a double-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File doublevalue(String attr, double value) {
            attrs.put(attr, new String[] {"doublevalue", Double.toString(value)});
            return this;
        }

        /**
         * Adds a boolean-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File boolvalue(String attr, boolean value) {
            attrs.put(attr, new String[] {"boolvalue", Boolean.toString(value)});
            return this;
        }

        /**
         * Adds a character-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value
         * @return this builder
         */
        public File charvalue(String attr, char value) {
            attrs.put(attr, new String[] {"charvalue", Character.toString(value)});
            return this;
        }

        /**
         * Adds a URL-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value, e.g. {@code "/my/module/resource.html"}
         *              or {@code "nbresloc:/my/module/resource.html"}; relative values permitted
         *              but not likely useful as base URL would be e.g. {@code "jar:...!/META-INF/"}
         * @return this builder
         * @throws LayerGenerationException in case an opaque URI is passed as {@code value}
         */
        public File urlvalue(String attr, URI value) throws LayerGenerationException {
            if (value.isOpaque()) {
                throw new LayerGenerationException("Cannot use an opaque URI: " + value, originatingElement);
            }
            attrs.put(attr, new String[] {"urlvalue", value.toString()});
            return this;
        }

        /**
         * Adds a URL-valued attribute.
         * @param attr the attribute name
         * @param value the attribute value, e.g. {@code "/my/module/resource.html"}
         *              or {@code "nbresloc:/my/module/resource.html"}; relative values permitted
         *              but not likely useful as base URL would be e.g. {@code "jar:...!/META-INF/"}
         * @return this builder
         * @throws LayerGenerationException in case {@code value} cannot be parsed as a URI or is opaque
         */
        public File urlvalue(String attr, String value) throws LayerGenerationException {
            try {
                return urlvalue(attr, URI.create(value));
            } catch (IllegalArgumentException x) {
                throw new LayerGenerationException(x.getLocalizedMessage(), originatingElement);
            }
        }

        /**
         * Adds an attribute loaded from a Java method.
         * @param attr the attribute name
         * @param clazz the fully-qualified binary name of the factory class
         * @param method the name of a static method
         * @return this builder
         */
        public File methodvalue(String attr, String clazz, String method) {
            attrs.put(attr, new String[] {"methodvalue", clazz + "." + method});
            return this;
        }

        /**
         * Adds an attribute loaded from a Java constructor.
         * @param attr the attribute name
         * @param clazz the fully-qualified binary name of a class with a no-argument constructor
         * @return this builder
         */
        public File newvalue(String attr, String clazz) {
            attrs.put(attr, new String[] {"newvalue", clazz});
            return this;
        }

        /**
         * Adds an attribute to load the associated class or method.
         * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
         * @param attr the attribute name
         * @param type a type to which the instance ought to be assignable, or null to skip this check
         * @return this builder
         * @throws IllegalArgumentException if the associated element is not a {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
         * @throws LayerGenerationException if the associated element would not be loadable as an instance of the specified type
         */
        public File instanceAttribute(String attr, Class<?> type) throws IllegalArgumentException, LayerGenerationException {
            return instanceAttribute(attr, type, null, null);
        }
        /**
         * Adds an attribute to load the associated class or method.
         * Useful for {@link LayerGeneratingProcessor}s which define layer fragments which instantiate Java objects from the annotated code.
         * @param attr the attribute name
         * @param type a type to which the instance ought to be assignable, or null to skip this check
         * @param annotation as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
         * @param annotationMethod as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
         * @return this builder
         * @throws IllegalArgumentException if the associated element is not a {@linkplain TypeElement class} or {@linkplain ExecutableElement method}
         * @throws LayerGenerationException if the associated element would not be loadable as an instance of the specified type
         * @since 7.50
         */
        public File instanceAttribute(String attr, Class<?> type, Annotation annotation, String annotationMethod) throws IllegalArgumentException, LayerGenerationException {
            String[] clazzOrMethod = instantiableClassOrMethod(type, annotation, annotationMethod);
            if (clazzOrMethod[1] == null) {
                newvalue(attr, clazzOrMethod[0]);
            } else {
                methodvalue(attr, clazzOrMethod[0], clazzOrMethod[1]);
            }
            return this;
        }

        /**
         * Adds an attribute loaded from a resource bundle.
         * @param attr the attribute name
         * @param bundle the full name of the bundle, e.g. {@code "org.my.module.Bundle"}
         * @param key the key to look up inside the bundle
         * @return this builder
         */
        public File bundlevalue(String attr, String bundle, String key) {
            attrs.put(attr, new String[] {"bundlevalue", bundle + "#" + key});
            return this;
        }

        /**
         * Adds an attribute for a possibly localized string.
         * @param attr the attribute name
         * @param label either a general string to store as is, or a resource bundle reference
         *              such as {@code "my.module.Bundle#some_key"},
         *              or just {@code "#some_key"} to load from a {@code "Bundle"}
         *              in the same package as the element associated with this builder (if exactly one)
         * @return this builder
         * @throws LayerGenerationException if a bundle key is requested but it cannot be found in sources
         */
        public File bundlevalue(String attr, String label) throws LayerGenerationException {
            return bundlevalue(attr, label, null, null);
        }
        /**
         * Adds an attribute for a possibly localized string.
         * @param attr the attribute name
         * @param label either a general string to store as is, or a resource bundle reference
         *              such as {@code "my.module.Bundle#some_key"},
         *              or just {@code "#some_key"} to load from a {@code "Bundle"}
         *              in the same package as the element associated with this builder (if exactly one)
         * @param annotation as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
         * @param annotationMethod as in {@link LayerGenerationException#LayerGenerationException(String,Element,ProcessingEnvironment,Annotation,String)}
         * @return this builder
         * @throws LayerGenerationException if a bundle key is requested but it cannot be found in sources
         * @since 7.50
         */
        public File bundlevalue(String attr, String label, Annotation annotation, String annotationMethod) throws LayerGenerationException {
            String javaIdentifier = "(?:\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*)";
            Matcher m = Pattern.compile("((?:" + javaIdentifier + "\\.)+[^\\s.#]+)?#(\\S*)").matcher(label);
            if (m.matches()) {
                String bundle = m.group(1);
                String key = m.group(2);
                if (bundle == null) {
                    Element referenceElement = originatingElement;
                    while (referenceElement != null && referenceElement.getKind() != ElementKind.PACKAGE) {
                        referenceElement = referenceElement.getEnclosingElement();
                    }
                    if (referenceElement == null) {
                        throw new LayerGenerationException("No reference element to determine package in '" + label + "'", originatingElement);
                    }
                    bundle = ((PackageElement) referenceElement).getQualifiedName() + ".Bundle";
                }
                verifyBundleKey(bundle, key, m.group(1) == null, annotation, annotationMethod);
                bundlevalue(attr, bundle, key);
            } else {
                stringvalue(attr, label);
            }
            return this;
        }
        private void verifyBundleKey(String bundle, String key, boolean samePackage, Annotation annotation, String annotationMethod) throws LayerGenerationException {
            if (processingEnv == null) {
                return;
            }
            if (samePackage) {
                for (Element e = originatingElement; e != null; e = e.getEnclosingElement()) {
                    Messages m = e.getAnnotation(Messages.class);
                    if (m != null) {
                        for (String kv : m.value()) {
                            if (kv.startsWith(key + "=")) {
                                return;
                            }
                        }
                    }
                }
            }
            try {
                InputStream is = validateResource(bundle.replace('.', '/') + ".properties", originatingElement, null, null, false).openInputStream();
                try {
                    Properties p = new Properties();
                    p.load(is);
                    if (p.getProperty(key) == null) {
                        throw new LayerGenerationException("No key '" + key + "' found in " + bundle, originatingElement, processingEnv, annotation, annotationMethod);
                    }
                } finally {
                    is.close();
                }
            } catch (IOException x) {
                throw new LayerGenerationException("Could not open " + bundle + ": " + x, originatingElement, processingEnv, annotation, annotationMethod);
            }
        }

        /**
         * Adds an attribute which deserializes a Java value.
         * @param attr the attribute name
         * @param data the serial data as created by {@link ObjectOutputStream}
         * @return this builder
         */
        public File serialvalue(String attr, byte[] data) {
            StringBuilder buf = new StringBuilder(data.length * 2);
            for (byte b : data) {
                if (b >= 0 && b < 16) {
                    buf.append('0');
                }
                buf.append(Integer.toHexString(b < 0 ? b + 256 : b));
            }
            attrs.put(attr, new String[] {"serialvalue", buf.toString().toUpperCase(Locale.ENGLISH)});
            return this;
        }

        /**
         * Sets a position attribute.
         * This is a convenience method so you can define in your annotation:
         * <code>int position() default Integer.MAX_VALUE;</code>
         * and later call:
         * <code>fileBuilder.position(annotation.position())</code>
         * @param position a numeric position for this file, or {@link Integer#MAX_VALUE} to not define any position
         * @return this builder
         */
        public File position(int position) {
            if (position != Integer.MAX_VALUE) {
                intvalue("position", position);
            }
            return this;
        }

        /**
         * Writes the file or folder to the layer.
         * Any intervening parent folders are created automatically.
         * If the file already exists, the old copy is replaced (not true in case of a folder).
         * @return the originating layer builder, in case you want to add another file
         */
        public LayerBuilder write() {
            unwrittenFiles.remove(this);
            org.w3c.dom.Element e = doc.getDocumentElement();
            String[] pieces = path.split("/");
            for (String piece : Arrays.asList(pieces).subList(0, pieces.length - 1)) {
                org.w3c.dom.Element kid = find(e, piece, "file|folder");
                if (kid != null) {
                    if (!kid.getNodeName().equals("folder")) {
                        throw new IllegalArgumentException(path);
                    }
                    e = kid;
                } else {
                    e = (org.w3c.dom.Element) e.appendChild(doc.createElement("folder"));
                    e.setAttribute("name", piece);
                }
            }
            String piece = pieces[pieces.length - 1];
            org.w3c.dom.Element file = find(e, piece, "file|folder");
            if (file == null) {
                file = (org.w3c.dom.Element) e.appendChild(doc.createElement(folder ? "folder" : "file"));
                file.setAttribute("name", piece);
            }
            if (originatingElement != null) {
                // Embed comment in generated-layer.xml for easy navigation back to the annotation.
                String name;
                switch (originatingElement.getKind()) {
                case CONSTRUCTOR:
                case ENUM_CONSTANT:
                case FIELD:
                case INSTANCE_INIT:
                case METHOD:
                case STATIC_INIT:
                    name = originatingElement.getEnclosingElement() + "." + originatingElement;
                    break;
                default:
                    name = originatingElement.toString();
                }
                boolean addComment = true;
                NodeList oldComments = file.getChildNodes();
                for (int i = 0; i < oldComments.getLength(); i++) {
                    Node node = oldComments.item(i);
                    if (node.getNodeType() == Node.COMMENT_NODE && node.getNodeValue().equals(name)) {
                        addComment = false;
                        break;
                    }
                }
                if (addComment) {
                    file.appendChild(doc.createComment(name));
                }
            }
            for (Map.Entry<String,String[]> entry : attrs.entrySet()) {
                org.w3c.dom.Element former = find(file, entry.getKey(), "attr");
                if (former != null) {
                    file.removeChild(former);
                }
                org.w3c.dom.Element attr = (org.w3c.dom.Element) file.appendChild(doc.createElement("attr"));
                attr.setAttribute("name", entry.getKey());
                attr.setAttribute(entry.getValue()[0], entry.getValue()[1]);
            }
            if (url != null) {
                file.setAttribute("url", url);
            } else if (contents != null) {
                NodeList oldContents = file.getChildNodes();
                for (int i = 0; i < oldContents.getLength();) {
                    Node node = oldContents.item(i);
                    if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                        file.removeChild(node);
                    } else {
                        i++;
                    }
                }
                file.appendChild(doc.createCDATASection(contents));
            }
            return LayerBuilder.this;
        }

        private org.w3c.dom.Element find(org.w3c.dom.Element parent, String name, String kindRx) {
            NodeList nl = parent.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                if (item.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                org.w3c.dom.Element e = (org.w3c.dom.Element) item;
                if (e.getAttribute("name").equals(name) && e.getNodeName().matches(kindRx)) {
                    return e;
                }
            }
            return null;
        }

    }

}
