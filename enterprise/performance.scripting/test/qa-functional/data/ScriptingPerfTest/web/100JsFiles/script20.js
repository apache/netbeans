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


var customersObj;

//function to show all customers
function showCustomers() {
    var app = new CustomerDB();
    var resources = app.getResources();
    for(i=0;i<resources.length;i++) {
        var resource = resources[i];
        if(resource instanceof Customers) {
            customersObj = resource;
            var customers = customersObj.getItems();
            var headers = new Array();
            headers[0] = 'ID';
            headers[1] = 'Name';
            headers[2] = 'Email';
            headers[3] = 'Address';
            headers[4] = 'Action';
            var node = document.getElementById('vw_pl_content');
            node.innerHTML = createCustomersTable(headers, customers) ;
            doShowContent('vw_pl');
        }
    }   
}
