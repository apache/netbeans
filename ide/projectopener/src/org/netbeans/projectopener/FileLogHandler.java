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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * 
 * @author Milan Kubec
 */
public class FileLogHandler extends Handler {
    
    private Writer writer;
    private String lSep = System.getProperty("line.separator");
    
    /** Creates a new instance of FileLogHandler */
    public FileLogHandler() {
        try {
            File f = new File(File.createTempFile("temp", null).getParentFile(), "projectopener.log");
            writer = new PrintWriter(new FileWriter(f));
            writer.write("--------------------------------------------------------------------------------" + lSep);
            writer.write("NetBeans Project Opener ver. " + WSProjectOpener.APP_VERSION + " - " + new Date().toString() + lSep);
            writer.write("JDK " + System.getProperty("java.version") + "; " + System.getProperty("java.vm.name") + " " 
                    + System.getProperty("java.vm.version") + lSep);
            writer.write(System.getProperty("os.name") + " version " + System.getProperty("os.version") + " running on " 
                    + System.getProperty("os.arch") + lSep);
            writer.write("--------------------------------------------------------------------------------" + lSep);
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void publish(LogRecord record) {
        try {
            writer.write(record.getMessage() + lSep);
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void flush() {
        try {
            writer.flush();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
    public void close() throws SecurityException {
        try {
            writer.close();
        } catch (IOException ex) {
            // ex.printStackTrace();
        }
    }
    
}
