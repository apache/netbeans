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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.jar.JarFile;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

/** Generates JNLP files for signed versions of the module JAR files.
 *
 * @author Jaroslav Tulach
 */
public class MakeMasterJNLP extends Task {
    /** the files to work on */
    private ResourceCollection files;
    
    public FileSet createModules()
    throws BuildException {
        FileSet fs = new FileSet();
        fs.setProject(getProject());
        addConfigured(fs);
        return fs;
    }

    public void addConfigured(ResourceCollection rc) throws BuildException {
        if (files != null) throw new BuildException("modules can be specified just once");
        files = rc;
    }
    
    private File target;
    public void setDir(File t) {
        target = t;
    }
    
    private String masterPrefix = "";
    public void setCodeBase(String p) {
        masterPrefix = p;
    }

    public void execute() throws BuildException {
        if (target == null) throw new BuildException("Output dir must be provided");
        if (files == null) throw new BuildException("modules must be provided");
        
        try {
            generateFiles();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    
    private void generateFiles() throws IOException, BuildException {
        for (Iterator<Resource> fileIt = files.iterator(); fileIt.hasNext();) {
            FileResource fr = (FileResource) fileIt.next();
            File jar = fr.getFile();

            if (!jar.canRead()) {
                throw new BuildException("Cannot read file: " + jar);
            }
            
            try (JarFile theJar = new JarFile(jar)) {
                String codenamebase = JarWithModuleAttributes.extractCodeName(theJar.getManifest().getMainAttributes());
                if (codenamebase == null) {
                    throw new BuildException("Not a NetBeans Module: " + jar);
                }
                if (codenamebase.equals("org.objectweb.asm.all")
                        && jar.getParentFile().getName().equals("core")
                        && jar.getParentFile().getParentFile().getName().startsWith("platform")) {
                    continue;
                }
                {
                    int slash = codenamebase.indexOf('/');
                    if (slash >= 0) {
                        codenamebase = codenamebase.substring(0, slash);
                    }
                }
                String dashcnb = codenamebase.replace('.', '-');
                
                File n = new File(target, dashcnb + ".ref");
                try (FileWriter w = new FileWriter(n)) {
                    w.write("    <extension name='" + codenamebase + "' href='" + this.masterPrefix + dashcnb + ".jnlp' />\n");
                }
            }
        }
        
    }
}
