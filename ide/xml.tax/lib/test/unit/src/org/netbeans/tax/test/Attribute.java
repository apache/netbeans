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
package org.netbeans.tax.test;

import org.netbeans.tax.*;
import java.util.Iterator;

public class Attribute {

    public static void main (String args[]) throws Exception {
        TreeElement element = new TreeElement ("element");
        element.addAttribute ("a", "a");

        print ("New attribute 'a'.", element);

        //I can rename attribuite to existing one.
        element.addAttribute ("b", "b");

        print ("New attribute 'b'.", element);

        TreeAttribute attr = element.getAttribute ("b");
        attr.setQName ("a");
        
        print ("Change atribute 'b' name to 'a'!", element);
        
        // I can get not existing attribute.
        System.out.println ("Attribute 'b'!");
        print (element.getAttribute ("b"));
    }
    
    private static void print (String title, TreeElement element) {
        System.out.println ("-> " + title);
        
        Iterator it = element.getAttributes().iterator();
        while (it.hasNext()) {
            print ((TreeAttribute)it.next());
        }
        
        System.out.println ("");
    }
    
    private static void print (TreeAttribute a) {
        if ( a == null ) {
            System.out.println (a);
        } else {
            System.out.println (a.getQName() + " = \"" + a.getValue() + "\"");
        }
    }

}
