/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        for (Iterator fileIt = files.iterator(); fileIt.hasNext();) {
            FileResource fr = (FileResource) fileIt.next();
            File jar = fr.getFile();

            if (!jar.canRead()) {
                throw new BuildException("Cannot read file: " + jar);
            }
            
            JarFile theJar = new JarFile(jar);
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
            FileWriter w = new FileWriter(n);
            w.write("    <extension name='" + codenamebase + "' href='" + this.masterPrefix + dashcnb + ".jnlp' />\n");
            w.close();
                
                
            theJar.close();
        }
        
    }
}
