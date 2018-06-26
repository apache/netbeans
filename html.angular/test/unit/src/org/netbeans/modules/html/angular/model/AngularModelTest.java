/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.model;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.JsObjectImpl;
import org.netbeans.modules.javascript2.model.ModelElementFactoryAccessor;
import org.netbeans.modules.javascript2.model.ModelTestBase;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Roman Svitanic
 */
public class AngularModelTest extends ModelTestBase {

    public AngularModelTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        AngularModelInterceptor.disabled = true;
    }

    public void testAngular() throws Exception {
        String file = "model/angular-1.3.6.js";
        if (!new File(getDataDir(), file).canRead()) {
            return;
        }
        FileObject fo = getTestFile(file);

        Model model = getModel(file);

        // Get anonymous object which is an argument of "extend" called in
        // publishExternalAPI function.
        // Generated property names will vary in different angular versions.
        JsObject angularObj = model.getGlobalObject()
                .getProperty("angular-1_3_6_L6")
                .getProperty("publishExternalAPI")
                .getProperty("angular-1.3.6Anonym$13");

        // XXX remove all generated properties/functions that start with $$
        List<String> names = new ArrayList<>();
        for (String name : angularObj.getProperties().keySet()) {
            if (name.startsWith("$$")) {
                names.add(name);
            }
        }
        Collections.addAll(names, "callbacks", "getTestability", "version");
        for (String name : names) {
            angularObj.getProperties().remove(name);
        }

        JsObject moduleFunc = model.getGlobalObject()
                .getProperty("angular-1_3_6_L6")
                .getProperty("setupModuleLoader")
                .getProperty("angular-1_3_6_L1683")
                .getProperty("module");
        moduleFunc.getModifiers().remove(Modifier.PRIVATE);
        moduleFunc.getModifiers().add(Modifier.PUBLIC);

        angularObj.addProperty("module", moduleFunc);

        ModelElementFactory elementFactory = ModelElementFactoryAccessor.getDefault().createModelElementFactory();
        JsObject newAngularObject = elementFactory.newObject(angularObj.getParent(), "angular", angularObj.getOffsetRange(), true);
        for (JsObject prop : angularObj.getProperties().values()) {
            newAngularObject.addProperty(prop.getName(), prop);
            ((JsObjectImpl) prop).setParent(newAngularObject);
        }
        newAngularObject.getModifiers().remove(Modifier.PRIVATE);
        newAngularObject.getModifiers().add(Modifier.PUBLIC);

        final StringWriter sw = new StringWriter();
        Model.Printer p = new Model.Printer() {

            @Override
            public void println(String str) {
                sw.append(str).append("\n");
            }
        };
        model.writeObject(p, newAngularObject, true);
        assertDescriptionMatches(fo, sw.toString(), false, ".model", true);
    }

}
