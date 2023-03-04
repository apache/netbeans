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
