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
package org.netbeans.api.htmlui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import net.java.html.json.Model;
import net.java.html.json.Property;

// @start region="ask"
@Model(className = "AskCtrl", targetId = "", properties = {
    @Property(name = "ok", type = boolean.class)
})
public final class AskQuestion implements ActionListener {
    @HTMLDialog(url = "dialog.html", className = "AskPages")
    static HTMLDialog.OnSubmit showHelloWorld(boolean checked) {
        AskCtrl model = new AskCtrl(checked).applyBindings();
        return (buttonId) -> {
            // called when user presses a button
            System.out.println("User selected: " + buttonId);
            // return false to prevent dialog from closing
            return true;
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // shows dialog with a question, checkbox is checked by default
        // AskPages is automatically generated class
        AskPages.showHelloWorld(true);
    }
}
// @end region="ask"
