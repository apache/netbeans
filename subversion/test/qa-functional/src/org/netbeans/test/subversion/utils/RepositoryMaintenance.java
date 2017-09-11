/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.test.subversion.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author peter
 */
public final class RepositoryMaintenance {
    
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            String[] files = folder.list();
            for (int i = 0; i < files.length; i++) {
                deleteFolder(new File(folder, files[i]));
            }    
        }
        folder.delete();
    }
    
    public static int loadRepositoryFromFile(String repoPath, String dumpPath){
        int value = -1;
        
        File repo = new File(repoPath);
        repo.mkdir();
        File dump = new File(dumpPath);
        boolean gzip = false;
        if (!dump.isFile()) {
            File dumpgz = new File(dumpPath + ".gz");
            if (dumpgz.isFile()) {
                dump = dumpgz;
                gzip = true;
            }
        }
        
        File tmpOutput = new File(repo.getParent() + File.separator + "output.txt");
                
        StreamHandler shFile;
        StreamHandler shError;
        StreamHandler shOutput;
        
        try {
            String[] cmd = {"svnadmin", "load", repo.getCanonicalPath()};
            InputStream fis = new FileInputStream(dump);
            if (gzip) {
                fis = new GZIPInputStream(fis);
            }
            FileOutputStream fos = new FileOutputStream(tmpOutput);
            Process p = Runtime.getRuntime().exec(cmd);
            shFile = new StreamHandler(fis, p.getOutputStream());
            shError = new StreamHandler(p.getErrorStream(), System.err);
            shOutput = new StreamHandler(p.getInputStream(), fos);
            shFile.start();
            shError.start();
            shOutput.start();
            value = p.waitFor();
            shFile.join();
            shError.join();
            shOutput.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return value;
    }
    
    public static int createRepository(String path) {
        int value = -1;
        
        File file = new File(path);
        file.mkdirs();
        
        String[] cmd = {"svnadmin", "create", path};
        
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            value = p.waitFor();   
        } catch (IOException e) {
            System.out.println("ex");
        } catch (InterruptedException e) {
            System.out.println("ex");
        }
        
        return value;
    }
    
    public static String changeFileSeparator(String path, boolean backed) {
        String changedPath = "";
        if (!backed) {
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == '\\') {
                    changedPath += '/';
                } else {
                    changedPath += path.charAt(i); 
                }       
            }
        } else {
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    changedPath += '\\' + '\\';
                } else {
                    changedPath += path.charAt(i); 
                }       
            }
        }
        if (changedPath.startsWith("/")) 
            changedPath = changedPath.substring(1, changedPath.length());
        return changedPath;
    }
    
}


/*create user/password - test/test
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + File.separator + "conf" + File.separator + "passwd"));
            String line = "[users]";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "test = test";
            bw.append(line, 0, line.length());
            bw.flush();
            bw.close();
        } catch (IOException e) {
        }    
        //rw access to repository for test user and r access for anonymous
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + File.separator + "conf" + File.separator + "authz"));
            String line = "[/]";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "test = rw";
            bw.append(line, 0, line.length());
            bw.newLine();
            line = "* = r";
            bw.append(line, 0, line.length());
            bw.flush();
            bw.close();
        } catch (IOException e) {
        } */   
