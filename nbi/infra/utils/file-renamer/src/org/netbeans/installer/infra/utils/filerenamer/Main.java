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

package org.netbeans.installer.infra.utils.filerenamer;

import java.io.File;

/**
 *
 * @author Kirill Sorokin
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        final File file = new File("D:/temp/data");
        
        final String token       = "_";
        final String replacement = ",";
        
        for (File source: file.listFiles()) {
            if (source.isFile()) {
                System.out.println(source);
                
                final String name = source.getName();
                final File target = new File(
                        source.getParentFile(), 
                        name.replace(token, replacement));
                
                System.out.println("    ... " + target);
                source.renameTo(target);
            }
        }
    }
    
}
