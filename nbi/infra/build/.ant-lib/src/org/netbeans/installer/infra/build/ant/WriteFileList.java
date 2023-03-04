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

package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author Dmitry Lipin
 */
public class WriteFileList extends Task{
    private String dir;
    private String output;
    private String mask;
    
    private void check(String s, String  desc) throws BuildException {
        if(s==null) {
            throw new BuildException("Error! Parameter '" + desc + "' can`t be null!!");
        }
    }
    
    private void write(StringBuilder sb, String s) {
        sb.append(s);
        sb.append(System.getProperty("line.separator"));
        
    }
    private void listFile(File parent, File f, StringBuilder sb) throws IOException {
        String path = f.getPath();
        String parentPath = parent.getPath();
        path = path.substring(parentPath.length());
        path = path.replaceAll("\\\\","/");
        if(path.length()>0) {
            path = path.substring(1);
        }
        
        if(f.isFile()) {
            if(path.length()>0 && path.matches(mask)) {
                write(sb, path);
            }
        } else if(f.isDirectory()) {
            
            if(path.length()>0) {
                path = path + "/";
                if(path.matches(mask)) {
                    write(sb, path);
                }
            }
            File  [] dirs  = f.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isDirectory());
                } }
            );
            
            for(File file: dirs) {
                listFile(parent, file, sb);
            }
            
            File  [] files  = f.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isFile() && !pathname.isDirectory());
                } }
            );
            for(File file: files) {
                listFile(parent, file, sb);
            }
        }
    }
    public void execute() throws BuildException {
        check(dir,"starting directory");
        check(output,"output file");
        check(mask,"file mask");
        
        File root = new File(dir);
        if (!root.equals(root.getAbsoluteFile())) {
            root = new File(getProject().getBaseDir(), dir);
        }
        File outFile = new File(output);
        if (!outFile.equals(outFile.getAbsoluteFile())) {
            outFile = new File(getProject().getBaseDir(), output);
        }
        FileOutputStream fos = null;
        
        log("Root directory : " + root);
        log("Output file : " + outFile);
        log("Mask  : "        + mask);
        
        try {
            StringBuilder sb = new StringBuilder();
            listFile(root, root, sb);
            fos = new FileOutputStream(outFile);
            fos.write(sb.toString().getBytes());
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if(fos!=null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    throw new BuildException(ex);
                }
            }
        }
        
    }
    
    public void setDir(final String dir) {
        this.dir = dir;
    }
    
    public void setOutput(final String output) {
        this.output = output;
    }
    
    public void setMask(final String mask) {
        this.mask = mask;
    }
    
}
