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
function Man (firstName, lastName, street, town, zip) {
    
    var address = createAddress(street, town, zip);
    
    function Address (streetp, townp, zipp) {
        var street = streetp,
            town = townp,
            zip = zipp;
        
        this.printAddress = function() {
            formatter.println("Address:")
            formatter.addIndent(4);
            formatter.println("Stree: " + street)
            formatter.println("Town: " + town);
            formatter.println("Zip: " + zip);
            formatter.removeIndent(4);
            var tmp = createAddress(street, town, zip+1);
            formatter.println("Bigger zip: " + tmp.getZip());
        }    
        
        this.correct = function() {
            if (zip < 15000) {
                zip = 15000;
            }
            
            return this;
        }
        
        this.getZip =  function() {
            return zipp;
        }
        
    }
    
    /*
      This function is not visible outside.   
    */
    function createAddress(street, town, zip) {
        return new Address(street, town, zip);
    }
    
    this.getAddress = function () {
        return address;
    }
    
    this.print = function () {
        formatter.println("Man info:");
        formatter.addIndent(4);
        formatter.println("First name: " + firstName);
        formatter.println("Last name: " + lastName);
        this.getAddress().printAddress();
        formatter.removeIndent(4);
        return this;
    }
   
}

var person = new Man("Josef", "Scriptu", "Delnicka", "Prague", 85);
person.print();
formatter.println("");
formatter.println("Corrected address: ")
person.getAddress().correct().printAddress();

