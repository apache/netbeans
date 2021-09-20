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

import net.java.html.json.Model;
import net.java.html.json.Function;
import net.java.html.json.Property;
import net.java.html.json.ComputedProperty;
import net.java.html.json.OnPropertyChange;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.openide.util.NbBundle;
import org.openide.awt.ActionID;

@Model(className = "HelloWorld", targetId = "", properties = {
    @Property(name = "text", type = String.class),
    @Property(name = "upper", type = boolean.class),
    @Property(name = "update", type = boolean.class),
    @Property(name = "helloMessage", type = String.class)
})
public final class HelloWorldCntrl {
    @ComputedProperty
    static int count(String text) {
        return text == null ? 0 : text.length();
    }

    @ComputedProperty
    static boolean canSayHello(String text, boolean update) {
        return text != null && !text.isEmpty() && !update;
    }

    @ComputedProperty
    static boolean displaySayHello(String text, boolean update) {
        return !update;
    }

    @Function
    static void sayHello(HelloWorld model) {
        updateHelloMessage(model);
        model.setText("");
    }

    private static void updateHelloMessage(HelloWorld model) {
        String name = model.getText();
        if (model.isUpper()) {
            name = name.toUpperCase();
        }
        model.setHelloMessage("Hello " + name + "!");
    }

    @OnPropertyChange({ "text", "update", "upper" })
    static void updateImmediately(HelloWorld model) {
        if (model.isUpdate()) {
            updateHelloMessage(model);
        }
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
        return new HelloWorld().applyBindings();
    }
}
