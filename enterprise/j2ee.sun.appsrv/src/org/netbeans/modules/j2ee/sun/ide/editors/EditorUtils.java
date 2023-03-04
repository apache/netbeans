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
/*
 * EditorUtils.java
 *
 * Created on September 16, 2003, 4:43 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

/**
 *
 * @author  nityad
 */
public class EditorUtils {

    /** Creates a new instance of EditorUtils */
    public EditorUtils() {
    }

    public static boolean isValidInt0(String str){
        int val;
        //represents 0 - MAX_INT range
        try
        {
            val = Integer.parseInt(str);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        if(val < 0)
        {
            return false;
        }
        return true;
    }     
    
    public static boolean isValidLong(String str){
        //represents -ve , 0  +ve range represented by Long
        try
        {
            Long.parseLong(str);
        }
        catch(NumberFormatException e)
        {
            return false;
        }
        return true;
    }
}
