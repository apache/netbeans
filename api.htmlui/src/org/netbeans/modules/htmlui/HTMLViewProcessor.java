/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2014 Oracle. All Rights Reserved.
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
 */
package org.netbeans.modules.htmlui;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.openide.awt.ActionID;
import org.openide.filesystems.annotations.LayerBuilder;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jtulach
 */
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@ServiceProvider(service = Processor.class)
public class HTMLViewProcessor extends LayerGeneratingProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> hash = new HashSet<>();
        hash.add(OpenHTMLRegistration.class.getCanonicalName());
        return hash;
    }
    
    @Override
    protected boolean handleProcess(Set<? extends TypeElement> set, RoundEnvironment re) throws LayerGenerationException {
        for (Element e : re.getElementsAnnotatedWith(OpenHTMLRegistration.class)) {
            OpenHTMLRegistration reg = e.getAnnotation(OpenHTMLRegistration.class);
            if (reg == null || e.getKind() != ElementKind.METHOD) {
                continue;
            }
            if (!e.getModifiers().contains(Modifier.STATIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be static", e);
            }
            if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be public", e);
            }
            if (!((ExecutableElement)e).getParameters().isEmpty()) {
                error("Method annotated by @OpenHTMLRegistration should have no arguments", e);
            }
            if (!e.getEnclosingElement().getModifiers().contains(Modifier.PUBLIC)) {
                error("Method annotated by @OpenHTMLRegistration needs to be public in a public class", e);
            }
            
            ActionID aid = e.getAnnotation(ActionID.class);
            if (aid != null) {
                final LayerBuilder builder = layer(e);
                LayerBuilder.File actionFile = builder.
                        file("Actions/" + aid.category() + "/" + aid.id().replace('.', '-') + ".instance").
                        methodvalue("instanceCreate", "org.netbeans.modules.htmlui.Pages", "openAction");
                String abs = LayerBuilder.absolutizeResource(e, reg.url());
                try {
                    builder.validateResource(abs, e, reg, null, true);
                } catch (LayerGenerationException ex) {
                    if (System.getProperty("netbeans.home") != null) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Cannot find resource " + abs, e);
                    } else {
                        throw ex;
                    }
                }
                actionFile.stringvalue("url", abs);
                if (!reg.iconBase().isEmpty()) {
                    actionFile.stringvalue("iconBase", reg.iconBase());
                }
                actionFile.stringvalue("method", e.getSimpleName().toString());
                actionFile.stringvalue("class", e.getEnclosingElement().asType().toString());
                String[] techIds = reg.techIds();
                for (int i = 0; i < techIds.length; i++) {
                    actionFile.stringvalue("techId." + i, techIds[i]);
                }
//                actionFile.instanceAttribute("component", TopComponent.class, reg, null);
//                if (reg.preferredID().length() > 0) {
//                    actionFile.stringvalue("preferredID", reg.preferredID());
//                }
                actionFile.bundlevalue("displayName", reg.displayName(), reg, "displayName");
                actionFile.write();
            } else {
                error("@OpenHTMLRegistration needs to be accompanied with @ActionID annotation", e);
            }
            
        }
        return true;
    }

    private void error(final String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
