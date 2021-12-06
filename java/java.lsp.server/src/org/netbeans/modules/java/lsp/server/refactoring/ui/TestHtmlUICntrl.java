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
package org.netbeans.modules.java.lsp.server.refactoring.ui;

import net.java.html.json.Model;
import net.java.html.json.Function;
import net.java.html.json.Property;
import net.java.html.json.ComputedProperty;
import org.netbeans.api.htmlui.OpenHTMLRegistration;
import org.netbeans.api.htmlui.HTMLDialog;
import org.openide.util.NbBundle;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

/**
 * HTML page which displays a window and also a dialog.
 */
@Model(className = "TestHtmlUI", targetId = "", properties = {
    @Property(name = "text", type = String.class)
})
public final class TestHtmlUICntrl {

    @ComputedProperty
    static String templateName() {
        return "window";
    }

    @Function
    static void showDialog(TestHtmlUI model) {
        String reply = Pages.showTestHtmlUIDialog(model.getText());
        if ("OK".equals(reply)) {
            model.setText("Happy World!");
        } else {
            model.setText("Sad World!");
        }
    }

    @ActionID(
            category = "Tools",
            id = "org.netbeans.modules.java.lsp.server.refactoring.ui.TestHtmlUI"
    )
    @ActionReferences({
        @ActionReference(path = "Menu/Refactoring", position = 1125)
    })
    @NbBundle.Messages("CTL_TestHtmlUI=Open HTML Hello World!")
    @OpenHTMLRegistration(
            url = "TestHtmlUI.html",
            displayName = "#CTL_TestHtmlUI"
    //, iconBase="SET/PATH/TO/ICON/HERE"
    )
    public static TestHtmlUI onPageLoad() {
        return new TestHtmlUI("Hello World!").applyBindings();
    }

    //
    // dialog UI
    // 
    @HTMLDialog(url = "TestHtmlUI.html")
    static void showTestHtmlUIDialog(String t) {
        new TestHtmlUIDialog(t, false).applyBindings();
    }

    @Model(className = "TestHtmlUIDialog", targetId = "", properties = {
        @Property(name = "text", type = String.class),
        @Property(name = "ok", type = boolean.class),})
    static final class DialogCntrl {

        @ComputedProperty
        static String templateName() {
            return "dialog";
        }
    }
}
