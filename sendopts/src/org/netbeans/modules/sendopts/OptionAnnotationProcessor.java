/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
            return SourceVersion.RELEASE_7;
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
