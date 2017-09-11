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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
 * <pre>
&lt;filesystem>
    &lt;folder name="FrobnitzFactories">
        &lt;folder name="text">
            &lt;folder name="html">
                &lt;file name="my-module-HtmlFactory.instance">
                    &lt;attr name="position" intvalue="300"/>
                &lt;/file>
            &lt;/folder>
        &lt;/folder>
    &lt;/folder>
&lt;/filesystem>
 * </pre>
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
