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
package org.netbeans.modules.java.lsp.server.htmlui.demo;

import java.util.Arrays;
import java.util.List;
import net.java.html.json.Model;
import net.java.html.json.Property;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.ModelOperation;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.openide.util.NbBundle;
import org.openide.awt.ActionID;
import org.openide.awt.StatusDisplayer;

@Model(className = "HelloWorld", targetId = "", instance = true, builder = "with", properties = {
    @Property(name = "selectedModifier", type = String.class),
    @Property(name = "name", type = String.class),
    @Property(name = "returnType", type = String.class),
    @Property(name = "parameters", type = Parameter.class, array = true)
})
public final class HelloWorldCntrl {
    @Model(className = "Parameter", properties = {
        @Property(name = "type", type = String.class),
        @Property(name = "name", type = String.class)
    })
    static class ParameterCntrl {
    }

    private RefactoringData data;

    @ModelOperation
    void assignData(HelloWorld model, RefactoringData data) {
        this.data = data;
    }

    @Function
    void doRefactoring(HelloWorld model) {
        StatusDisplayer.getDefault().setStatusText("use data: " + data + " and model " + model);
    }

    @Function
    void moveUpParameter(HelloWorld model, Parameter data) {
        final List<Parameter> arr = model.getParameters();
        int index = arr.indexOf(data);
        if (index > 0) {
            Parameter other = arr.get(index - 1);
            arr.set(index, other);
            arr.set(index - 1, data);
        }
    }

    @Function
    void moveDownParameter(HelloWorld model, Parameter data) {
        final List<Parameter> arr = model.getParameters();
        int index = arr.indexOf(data);
        if (index != -1 && index + 1 < arr.size()) {
            Parameter other = arr.get(index + 1);
            arr.set(index, other);
            arr.set(index + 1, data);
        }
    }

    @Function
    void removeParameter(HelloWorld model, Parameter data) {
        model.getParameters().remove(data);
    }

    @ComputedProperty
    static List<String> availableModifiers() {
        return Arrays.asList("public", "protected", "package-package", "private");
    }

    @ComputedProperty
    static String preview(
        String selectedModifier, String returnType, String name, List<Parameter> parameters
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(selectedModifier).append(" ").append(returnType);
        sb.append(" ").append(name).append("(");
        String sep = "";
        for (Parameter p : parameters) {
            sb.append(sep);
            sb.append(p.getType()).append(" ").append(p.getName());
            sep = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    @ActionID(
            category = "Tools",
            id = "org.netbeans.modules.java.lsp.server.protocol.HelloWorld"
    )
    @NbBundle.Messages("CTL_HelloWorld=Open HTML Hello World!")
    @OpenHTMLRegistration(
            url = "HelloWorld.html",
            displayName = "#CTL_HelloWorld"
    //, iconBase="SET/PATH/TO/ICON/HERE"
    )
    public static HelloWorld onPageLoad() {
        final HelloWorld model = new HelloWorld();
        model.
            withName("openSource").
            withReturnType("boolean").
            withSelectedModifier("public").
            withParameters(
                new Parameter("Lookup.Provider", "project"),
                new Parameter("String", "className"),
                new Parameter("String", "methodName"),
                new Parameter("String", "signature"),
                new Parameter("int", "line")
            ).
            assignData(new RefactoringData());

        return model.applyBindings();
    }

    static final class RefactoringData {
    }
}
