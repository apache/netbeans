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

"use strict";
(function () {

    function App() {

        var self = this;
        self.messages = ko.observableArray([]);
        self.currentMessage = ko.observable("");

        self.buttonEnabled = ko.computed(function(){
           return self.currentMessage().length > 0;
        });

        self.sendText = function () {
            window.console.log("sending");
            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function () {
                if (this.readyState === 4 && this.status === 200) {
                    receive(this.responseText);
                }
            };
            xhr.open("GET", "api/string/" + encodeURIComponent(self.currentMessage()), true);
            xhr.send();

        };


        function receive(responseText) {
            self.messages.push(JSON.parse(responseText));
        }

        ko.components.register("messages", {
            viewModel: function () {
                this.messages = self.messages;
            },

            template: "<table><thead><tr><th>Original message</th><th>Response</th></tr></thead><tbody data-bind='foreach: messages'><tr><td data-bind='text: original'></td><td data-bind='text: reverted'></td></tr></tbody></table>"
        });
        ko.components.register("mytitle", {
            viewModel: function (params) {
                this.title = ko.observable(params.title);
            },

            template: "<h2 data-bind='text: title'></h2>"
        });

    }

    ko.applyBindings(new App());

})();
