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

package org.netbeans.projectopener;

import junit.framework.TestCase;
import junit.framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Milan Kubec
 */
public class ArgsHandlerTest extends TestCase {
    
    public ArgsHandlerTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of getArgValue method, of class org.netbeans.projectopener.ArgsHandler.
     */
    public void testGetArgValue() {
        
        System.out.println("getArgValue");
        
        String prjURLString = "http://www.someurl.com/TheProject.zip";
        String nbVersionString = "1.2.3";
        String mainPrjNameString = "TheMainProject";
        String args[] = new String[] { "-projecturl", prjURLString, 
                                       "-minversion", nbVersionString, 
                                       "-mainproject", mainPrjNameString, 
                                       "-showgui", "-otherarg" };
        List<String> list = new ArrayList<String>();
        list.add("showgui");
        list.add("otherarg");
        
        ArgsHandler handler = new ArgsHandler(args);
        assertEquals(prjURLString, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(mainPrjNameString, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
        String args2[] = new String[] { "-projecturl", 
                                        "-minversion", nbVersionString, 
                                        "-showgui" };
        list.clear();
        list.add("showgui");
        
        handler = new ArgsHandler(args2);
        assertEquals(null, handler.getArgValue("projecturl"));
        assertEquals(nbVersionString, handler.getArgValue("minversion"));
        assertEquals(null, handler.getArgValue("mainproject"));
        assertEquals(list, handler.getAdditionalArgs());
        
    }

    /**
     * Test of getAdditionalArgs method, of class org.netbeans.projectopener.ArgsHandler.
     */
    // public void testGetAdditionalArgs() { }
    
}
