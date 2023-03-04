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
package org.netbeans.api.htmlui;

import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;

// @start region="controller" 
@Model(className = "UI", targetId = "", properties = {
    @Property(name = "text", type = String.class)
})
public final class UICntrl {
    // callback when Submit button is pressed
    @Function
    static void submit(UI model) {
        System.err.println("You are feeling " + model.getText());
    }

    //
    // register the action to the UI
    //
    @ActionID(
            category = "Tools",
            id = "my.sample.HtmlHelloWorld"
    )
    @ActionReferences({
        @ActionReference(path = "Menu/Tools"),
        @ActionReference(path = "Toolbars/File"),})
    @NbBundle.Messages("CTL_OpenHtmlHelloWorld=Open HTML Hello World!")
    @OpenHTMLRegistration(
            url = "ui.html",
            displayName = "#CTL_OpenHtmlHelloWorld"
    )
    public static UI onPageLoad() {
        return new UI("Hello World!").applyBindings();
    }
}
// @end region="controller" 
