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
 * deleteDir.java
 *
 * Created on 30 January 2007, 22:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.qa.form;

import java.io.File;

/**
 *
 * @author Jana Maleckova
 */
public class DeleteDir {
    
    /** Creates a new instance of deleteDir */
       
    public static void delDir(String dirPath) {
        File f = new File(dirPath);
        System.out.println(dirPath);
        if (f.exists()) {
            if (f.delete()== false) {
                File[] files = f.listFiles();
                for (int i=0;i<files.length;i++){
                    File deletedFile = files[i];
                    if (deletedFile.delete()== false){
                        delDir(files[i].getAbsolutePath());
                    }
                }
                f.delete();
            }         
        }
    }
    
 
}
