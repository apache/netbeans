/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
