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

package tests;

import java.util.Vector;

/** Test class throwing exception to be caught by exception breakpoint.
 *
 * @author Jiri Kovalsky
 */
public class ThrowException {
    
    public static void main(String args[]) {
        Vector vector = new Vector();
        vector.add(new Integer(1));
        
        try {
            vector.get(1);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: There is no 2nd item.");
        }
        
        String number = (String) vector.get(0);
    }
}