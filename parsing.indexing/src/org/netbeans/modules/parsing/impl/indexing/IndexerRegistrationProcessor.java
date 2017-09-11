/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.impl.indexing;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.openide.filesystems.annotations.LayerBuilder.File;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 * {@link LayerGeneratingProcessor} to generate {@link BinaryIndexer} registrations.
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 * @author Tomas Zezula
 */
@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class IndexerRegistrationProcessor extends LayerGeneratingProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(ConstrainedBinaryIndexer.Registration.class.getCanonicalName());
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        for (Element e : roundEnv.getElementsAnnotatedWith(ConstrainedBinaryIndexer.Registration.class)) {
            assert e.getKind().isClass();
            final ConstrainedBinaryIndexer.Registration reg = e.getAnnotation(ConstrainedBinaryIndexer.Registration.class);
            final Elements elements = processingEnv.getElementUtils();
            final Types types = processingEnv.getTypeUtils();
            final TypeElement binIndexerType = elements.getTypeElement(ConstrainedBinaryIndexer.class.getName());
            if (types.isSubtype(((TypeElement)e).asType(), binIndexerType.asType())) {
                final String indexerName = reg.indexerName();
                if (indexerName == null) {
                    throw new LayerGenerationException("Indexer name has to be given.", e); //NOI18N
                }
                final File f = layer(e).instanceFile("Editors", null, null);    //NOI18N
                f.stringvalue("instanceClass", BinaryIndexerFactory.class.getName());   //NOI18N
                f.methodvalue("instanceCreate", ConstrainedBinaryIndexer.class.getName(), "create");    //NOI18N
                f.instanceAttribute("delegate", ConstrainedBinaryIndexer.class);
                if (reg.requiredResource().length > 0) {
                    f.stringvalue("requiredResource", list(reg.requiredResource(), e)); //NOI18N
                }
                if (reg.mimeType().length > 0) {
                    f.stringvalue("mimeType", list(reg.mimeType(), e)); //NOI18N
                }
                if (reg.namePattern().length() > 0) {
                    f.stringvalue("namePattern", reg.namePattern()); //NOI18N
                }
                f.stringvalue("name", indexerName);         //NOI18N
                f.intvalue("version", reg.indexVersion());  //NOI18N
                f.write();
            } else {
                throw new LayerGenerationException("Annoated element is not a  subclass of BinaryIndexer.",e); //NOI18N
            }
        }
        return true;
    }

    private static String list(String[] arr, Element e) throws LayerGenerationException {
        if (arr.length == 1) {
            return arr[0];
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            if (s.indexOf(',') >= 0) {  //NOI18N
                throw new LayerGenerationException("',' is not allowed in the text", e);    //NOI18N
            }
            sb.append(s).append(",");   //NOI18N
        }
        return sb.substring(0, sb.length() - 1);
    }
}
