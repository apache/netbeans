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

package org.netbeans.modules.debugger.jpda.ant;

import com.sun.jdi.VMOutOfMemoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.debugger.jpda.EditorContext;


/**
 * Ant task to reload classes in VM for running debugging session. 
 *
 * @author David Konecny
 */
public class JPDAReload extends Task {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    private List filesets = new ArrayList ();
 
    /**
     * FileSet with .class files to reload. The base dir of the fileset is expected
     * to be classpath root for these classes.
     */
    public void addFileset (FileSet fileset) {
        filesets.add (fileset);
    }
    
    @Override
    public void execute() throws BuildException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JPDAReload.execute(), filesets = "+filesets);
        }
        if (filesets.size() == 0) {
            throw new BuildException ("A nested fileset with class to refresh in VM must be specified.");
        }
        
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (debuggerEngine == null) {
            throw new BuildException ("No debugging sessions was found.");
        }
        JPDADebugger debugger = debuggerEngine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            throw new BuildException("Current debugger is not JPDA one.");
        }
        if (!debugger.canFixClasses ()) {
            throw new BuildException("The debugger does not support Fix action.");
        }
        if (debugger.getState () == JPDADebugger.STATE_DISCONNECTED) {
            throw new BuildException ("The debugger is not running");
        }
        
        System.out.println ("Classes to be reloaded:");
        
        FileUtils fileUtils = FileUtils.getFileUtils();
        Map map = new HashMap ();
        EditorContext editorContext = DebuggerManager.getDebuggerManager().lookupFirst(null, EditorContext.class);

        Iterator it = filesets.iterator ();
        while (it.hasNext ()) {
            FileSet fs = (FileSet) it.next ();
            DirectoryScanner ds = fs.getDirectoryScanner (getProject ());
            String fileNames[] = ds.getIncludedFiles ();
            File baseDir = fs.getDir (getProject ());
            int i, k = fileNames.length;
            for (i = 0; i < k; i++) {
                File f = fileUtils.resolveFile (baseDir, fileNames [i]);
                if (f != null) {
                    FileObject fo = FileUtil.toFileObject(f);
                    if (fo != null) {
                        try {
                            String url = classToSourceURL (fo);
                            if (url != null)
                                editorContext.updateTimeStamp (debugger, url);
                            InputStream is = fo.getInputStream ();
                            long fileSize = fo.getSize ();
                            byte[] bytecode = new byte [(int) fileSize];
                            is.read (bytecode);
                            // remove ".class" from and use dots for for separator
                            String className = fileNames [i].substring (
                                    0, 
                                    fileNames [i].length () - 6
                                ).replace (File.separatorChar, '.');
                            map.put (
                                className, 
                                bytecode
                            );
                            System.out.println (" " + className);
                        } catch (IOException ex) {
                            ex.printStackTrace ();
                        }
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Reloaded classes: "+map.keySet());
        }
        if (map.size () == 0) {
            System.out.println (" No class to reload");
            return;
        }
        String error = null;
        try {
            debugger.fixClasses (map);
        } catch (UnsupportedOperationException uoex) {
            error = "The virtual machine does not support this operation: "+uoex.getLocalizedMessage();
        } catch (NoClassDefFoundError ncdfex) {
            error = "The bytes don't correspond to the class type (the names don't match): "+ncdfex.getLocalizedMessage();
        } catch (VerifyError ver) {
            error = "A \"verifier\" detects that a class, though well formed, contains an internal inconsistency or security problem: "+ver.getLocalizedMessage();
        } catch (UnsupportedClassVersionError ucver) {
            error = "The major and minor version numbers in bytes are not supported by the VM. "+ucver.getLocalizedMessage();
        } catch (ClassFormatError cfer) {
            error = "The bytes do not represent a valid class. "+cfer.getLocalizedMessage();
        } catch (ClassCircularityError ccer) {
            error = "A circularity has been detected while initializing a class: "+ccer.getLocalizedMessage();
        } catch (VMOutOfMemoryException oomex) {
            error = "Out of memory in the target VM has occurred during class reload.";
        }
        if (error != null) {
            getProject().log(error, Project.MSG_ERR);
            throw new BuildException(error);
        }
    }
    
    private String classToSourceURL (FileObject fo) {
        try {
            ClassPath cp = ClassPath.getClassPath (fo, ClassPath.EXECUTE);
            FileObject root = cp.findOwnerRoot (fo);
            String resourceName = cp.getResourceName (fo, '/', false);
            if (resourceName == null) {
                getProject().log("Can not find classpath resource for "+fo+", skipping...", Project.MSG_ERR);
                return null;
            }
            int i = resourceName.indexOf ('$');
            if (i > 0)
                resourceName = resourceName.substring (0, i);
            FileObject[] sRoots = SourceForBinaryQuery.findSourceRoots 
                (root.getURL ()).getRoots ();
            ClassPath sourcePath = ClassPathSupport.createClassPath (sRoots);
            FileObject rfo = sourcePath.findResource (resourceName + ".java");
            if (rfo == null) return null;
            return rfo.getURL ().toExternalForm ();
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace ();
            return null;
        }
    }
}
