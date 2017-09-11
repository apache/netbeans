/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
