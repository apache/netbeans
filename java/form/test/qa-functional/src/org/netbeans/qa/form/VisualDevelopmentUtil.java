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

package org.netbeans.qa.form;

import java.io.*;

public class VisualDevelopmentUtil {
    public static String JAVA_VERSION = System.getProperty("java.version");
    
    public static String readFromFile(String filename) throws IOException   {
        File f = new File(filename); 
        int size = (int) f.length();        
        int bytes_read = 0;
        FileInputStream in = new FileInputStream(f);
        byte[] data = new byte [size];
        while(bytes_read < size)
            bytes_read += in.read(data, bytes_read, size-bytes_read);
        return new String(data);
    }
    
    public static void copy(File src, File dst) throws IOException {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    
    
}
