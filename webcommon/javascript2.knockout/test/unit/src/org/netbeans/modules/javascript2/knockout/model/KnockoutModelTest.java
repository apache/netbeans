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
package org.netbeans.modules.javascript2.knockout.model;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.modules.javascript2.model.api.IndexedElement;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.Model;
import org.netbeans.modules.javascript2.model.JsFunctionImpl;
import org.netbeans.modules.javascript2.model.JsFunctionReference;
import org.netbeans.modules.javascript2.model.ModelTestBase;
import org.netbeans.modules.javascript2.model.api.Index;
import org.netbeans.modules.javascript2.types.api.TypeUsage;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Pisl
 */
public class KnockoutModelTest extends ModelTestBase {
    
    public KnockoutModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        KnockoutModelInterceptor.disabled = true;
    }

    public void testKnockout() throws Exception {
        String file = "testfiles/model/knockout-3.2.0.debug.js";
        if (!new File(getDataDir(), file).canRead()) {
            return;
        }
        FileObject fo = getTestFile(file);

        Model model = getModel(file);
        JsObject ko = model.getGlobalObject().getProperty("ko");

        // HACK remove ko.ko
        ko.getProperties().remove("ko");

        // HACK fix observableArray
        // extend the fn and result with certain methods from Array
        JsObject observableArray = ko.getProperty("observableArray");
        if (observableArray instanceof JsFunction) {
            JsFunction func = (JsFunction) observableArray;
            func.addReturnType(new TypeUsage("ko.observableArray.result", -1, true));

            Set<String> arrayMethods = new HashSet<String>();
            Collections.addAll(arrayMethods,
                    "pop", "push", "reverse", "shift", "sort", "splice", "unshift", "slice");
            JsObject fn = observableArray.getProperty("fn");
            JsObject result = observableArray.getProperty("result");
            if (fn != null) {
                Index index = Index.get(fo);
                for (IndexedElement elem : index.getProperties("Array.prototype")) {
                    if (arrayMethods.contains(elem.getName())) {
                        IndexedElement.FunctionIndexedElement felem = (IndexedElement.FunctionIndexedElement) elem;
                        List<Identifier> params = new ArrayList<Identifier>(felem.getParameters().size());
                        for (String paramName : felem.getParameters().keySet()) {
                            params.add(new Identifier(paramName, OffsetRange.NONE));
                        }

                        JsFunction function = new JsFunctionImpl(func, fn,
                                new Identifier(elem.getName(), OffsetRange.NONE), params, OffsetRange.NONE, null, null);
                        fn.addProperty(elem.getName(), function);
                        result.addProperty(elem.getName(),
                                new JsFunctionReference(result, new Identifier(elem.getName(), OffsetRange.NONE),
                                function, false, null));
                    }
                }
            }
        }

        // XXX remove additional (generated ?) objects - 3.2.0
        List<String> names = new ArrayList<String>();
        Collections.addAll(names, "$bindings", "$data", "$dataFn", "$parents", "$rawData", "$root", "knockout-3.2.0.debug_L5220");
        for (String name : names) {
            ko.getProperties().remove(name);
        }

        final StringWriter sw = new StringWriter();
        Model.Printer p = new Model.Printer() {

            @Override
            public void println(String str) {
                // XXX hacks improving the model
                //String real = str;
                //real = real.replaceAll("_L28.ko", "ko");
                sw.append(str).append("\n");
            }
        };
        model.writeObject(p, ko, true);
        assertDescriptionMatches(fo, sw.toString(), false, ".model", true);
    }

    public void testExtend1() throws Exception {
        checkModel("testfiles/model/extend1.js");
    }

    public void testExtend2() throws Exception {
        checkModel("testfiles/model/extend2.js");
    }

    public void testBindings1() throws Exception {
        checkModel("testfiles/model/bindings1.js");
    }

    public void testBindings2() throws Exception {
        checkModel("testfiles/model/bindings2.js");
    }

    public void testBindings3() throws Exception {
        checkModel("testfiles/model/bindings3.js");
    }

    public void testBindingsExpenses() throws Exception {
        checkModel("testfiles/model/bindingsExpenses.js");
    }

    public void testIssue233001() throws Exception {
        checkModel("testfiles/model/issue233001.js");
    }

    @Override
    protected DefaultLanguageConfig getPreferredLanguage() {
        return new JsTestBase.TestJsLanguage();
    }
    
    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new LinkedList<FileObject>(ClasspathProviderImplAccessor.getJsStubs());
        cpRoots.add(FileUtil.toFileObject(new File(getDataDir(), "/testfiles/model")));
        return Collections.singletonMap(
            JS_SOURCE_ID,
            ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }
}
