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

/**
 * Support for writing annotation processors which generate XML layer fragments.
 * <p>Whenever an SPI author defines a new way for objects to be registered in
 * the system filesystem, it is encouraged to also define a matching annotation
 * which can create such a registration. If the SPI is associated with a particular
 * Java interface (or abstract class), conventionally this annotation should be named
 * {@code Registration} and be located as a nested type inside the interface.</p>
 * <p>For example, consider an interface {@code FrobnitzFactory} which should be
 * registered for a particular data type such as {@code text/html}. The SPI may
 * declare that a frobnitz factory should be located in the system filesystem
 * in the folder {@code FrobnitzFactories/mime/type/} where {@code mime/type} is
 * the associated data type, and should be declared as an instance file whose instance
 * is assignable to {@code FrobnitzFactory} (basename of file irrelevant).<p>
 * <p>(The SPI could also have just declared a global service interface where each
 * instance specifies its desired MIME type in the return value of a method. This
 * would work but would be undesirable from a performance perspective: the first
 * time the SPI ran, it would need to load <em>every</em> frobnitz factory in the
 * system, which could mean a lot of class loading, even though only one was
 * actually going to be used. Choosing a smart registration style can help avoid this
 * kind of performance mistake, and friendly registration annotations mean that
 * module developers can do the right thing without much effort.)</p>
 * <p>The SPI may also stipulate that for a given data type, it may matter which
 * factory is found "first". SPI code to handle the factories might look like:</p>
 * <pre>
static Frobnitz findFrobnitz(FileObject f) {
    for (FrobnitzFactory ff : Lookups.forPath("FrobnitzFactories/" + f.getMIMEType()).
            lookup(FrobnitzFactory.class).allInstances()) {
        Frobnitz fz = ff.newFrobnitz(f);
        if (fz != null) {
            return fz;
        }
    }
    return null;
}
 * </pre>
 * <p>There should then be an interface with a corresponding annotation:</p>
 * <pre>
public interface FrobnitzFactory {
    Frobnitz newFrobnitz(FileObject file); // may return null
    &#64;interface Registration {
        String mimeType();
        int position() default Integer.MAX_VALUE;
    }
}
 * </pre>
 * <p>Using the annotation is simple. The module author need create just one file
 * containing both the factory and its registration:</p>
 * <pre>
&#64;FrobnitzFactory.Registration(mimeType="text/html", position=300)
public class HtmlFactory implements FrobnitzFactory {
    public Frobnitz newFrobnitz(FileObject file) {...}
}
 * </pre>
 * <p>Now writing the annotation processor to create such a layer registration is easy.
 * Put the processor in some nonpublic package in the SPI module and register it
 * using {@link org.openide.util.lookup.ServiceProvider}.
 * You should extend {@link org.openide.filesystems.annotations.LayerGeneratingProcessor}, which manages the physical
 * writing of the generated layer fragment(s) in cooperation with any other active processors.
 * The {@link org.openide.filesystems.annotations.LayerBuilder} helper class is used to create file entries and attributes.
 * {@link org.openide.filesystems.annotations.LayerGenerationException} can also be thrown if the source code is erroneous.</p>
 * <pre>
&#64;ServiceProvider(service=Processor.class)
&#64;SupportedSourceVersion(SourceVersion.RELEASE_6)
public class FrobnitzFactoryProcessor extends LayerGeneratingProcessor {
    public &#64;Override Set&lt;String> getSupportedAnnotationTypes() {
        return Collections.singleton(Registration.class.getCanonicalName());
    }
    protected boolean handleProcess(Set&lt;? extends TypeElement> annotations,
                                    RoundEnvironment roundEnv)
                      throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(Registration.class)) {
            Registration r = e.getAnnotation(Registration.class);
            if (!r.mimeType().matches("(text|application|image)/([^/]+)")) {
                throw new LayerGenerationException("Bad MIME type: " + r.mimeType(), e);
            }
            layer(e).instanceFile("FrobnitzFactories/" + r.mimeType(), null,
                FrobnitzFactory.class).position(r.position()).write();
        }
        return true;
    }
}
 * </pre>
 * <p>Now when the module is compiled, {@code build/classes/META-INF/generated-layer.xml}
 * should look something like this:</p>
 * <pre>{@code
<filesystem>
    <folder name="FrobnitzFactories">
        <folder name="text">
            <folder name="html">
                <file name="my-module-HtmlFactory.instance">
                    <attr name="position" intvalue="300"/>
                </file>
            </folder>
        </folder>
    </folder>
</filesystem>
 * }</pre>
 * <p>and this layer should be loaded automatically by the module system
 * (in addition to any explicit layer specified in source code).</p>
 * <p>There are two basic ways to test a layer-generating processor:</p>
 * <ol>
 * <li>Create some registrations of the annotation inside the unit test class
 *     (so that they are processed as the tests are compiled).
 *     Make the test check that the corresponding SPI loads the registrations.</li>
 * <li>Run the processor programmatically on some sample registrations,
 *     confirming that it succeeds or aborts under the right conditions.
 *     For this, <code>AnnotationProcessorTestUtils</code> is useful.</li>
 * </ol>
 * <p><a href="http://hg.netbeans.org/main/raw-file/tip/openide.util/test/unit/src/org/netbeans/modules/openide/util/ServiceProviderProcessorTest.java"><code>ServiceProviderProcessorTest</code></a>
 * demonstrates both styles.<p>
 */
package org.openide.filesystems.annotations;
