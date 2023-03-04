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
 * RequiredFiles.java
 *
 * Created on May 10, 2005, 11:23 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.j2ee.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jungi
 */
public class RequiredFiles {

    private Set<String> files;
    
    /** 
     *   Creates a new instance of RequiredFiles from given <code>File</code>.
     *   @param f file containg the list of required files
     *   @throws IOException if some exception during reading <code>File f</code>
     *  occurs
     */
    public RequiredFiles(File f) throws IOException {
        init(f);
    }
    
    private void init(File f) throws IOException {
        files = new HashSet<String>();
        BufferedReader r = new BufferedReader(new FileReader(f));
        String s = null;
        while ((s = r.readLine()) != null) {
            if (s.startsWith("#")) {
                //skip comments
                continue;
            }
            files.add(s.replace('/',  File.separatorChar));
        }
    }
    
    /*
     *   Returns sorted list of required files (as String)
     */
    public Set<String> getRequiredFiles() {
        return new HashSet<String>(files);
    }
}
