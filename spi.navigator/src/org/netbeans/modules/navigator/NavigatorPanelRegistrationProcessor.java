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

package org.netbeans.modules.navigator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.annotations.LayerGeneratingProcessor;
import org.openide.filesystems.annotations.LayerGenerationException;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class NavigatorPanelRegistrationProcessor extends LayerGeneratingProcessor {

    @Override public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(NavigatorPanel.Registration.class.getCanonicalName(), NavigatorPanel.Registrations.class.getCanonicalName()));
    }

    @Override protected boolean handleProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws LayerGenerationException {
        if (roundEnv.processingOver()) {
            return false;
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(NavigatorPanel.Registration.class)) {
            NavigatorPanel.Registration r = e.getAnnotation(NavigatorPanel.Registration.class);
            if (r == null) {
                continue;
            }
            register(e, r);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(NavigatorPanel.Registrations.class)) {
            NavigatorPanel.Registrations rr = e.getAnnotation(NavigatorPanel.Registrations.class);
            if (rr == null) {
                continue;
            }
            for (NavigatorPanel.Registration r : rr.value()) {
                register(e, r);
            }
        }
        return true;
    }

    private void register(Element e, NavigatorPanel.Registration r) throws LayerGenerationException {
        String suffix = layer(e).instanceFile("dummy", null, null, r, null).getPath().substring("dummy".length()); // e.g. /my-Panel.instance
        layer(e).file(ProviderRegistry.PANELS_FOLDER + r.mimeType() + suffix).
                methodvalue("instanceCreate", LazyPanel.class.getName(), "create").
                instanceAttribute("delegate", NavigatorPanel.class, r, null).
                position(r.position()).
                bundlevalue("displayName", r.displayName()).
                write();
    }

}
