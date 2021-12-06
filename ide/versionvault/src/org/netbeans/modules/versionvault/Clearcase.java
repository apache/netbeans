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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault;

import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.client.Arguments;
import org.netbeans.modules.versionvault.client.ClearcaseClient;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.annotate.VcsAnnotationsProvider;
import org.netbeans.modules.versioning.annotate.VcsAnnotations;
import org.netbeans.modules.versioning.annotate.VcsAnnotation;
import org.openide.windows.InputOutput;
import org.openide.windows.IOProvider;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.netbeans.modules.versionvault.client.ClearcaseCommand;
import org.netbeans.modules.versionvault.client.AnnotateCommand;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.openide.util.Utilities;

import javax.swing.*;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versionvault.ui.diff.DiffAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.FileUtil;

/**
 * Main entry point for Clearcase functionality, use getInstance() to get the Clearcase object.
 * 
 * @author Maros Sandor
 */
public class Clearcase {

    private static Clearcase instance;
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.versionvault");
       
    public static synchronized Clearcase getInstance() {
        if (instance == null) {
            instance = new Clearcase();
            instance.init();
        }
        return instance;
    }

    /**
     * Format of one annotation line.
     */
    private final Pattern annotationLinePattern = Pattern.compile("#*\\s+(.+?)\\s+(.+?)\\s+(.+?)\\s+\\| (.*)");

    /**
     * Date format used in annotations.
     * TODO: does it depend on locale?
     */
    private final SimpleDateFormat annotationsDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private ClearcaseAnnotator   clearcaseAnnotator;
    private ClearcaseInterceptor clearcaseInterceptor;
    private FileStatusCache     fileStatusCache;
    
    /**
     * used for the majority of invoked cc commands but for those used:
     * - by status evaluation 
     * - getTopmostanagedParent
     * - when opening an external cc tool
     */
    private ClearcaseClient     client;

    /**
     * mind blocking other comands when evaluating if topmost
     */
    private ClearcaseClient     topmostManagedParentClient;
    private InputOutput         log;
    private RequestProcessor    rp;

    /**
     * Keeps folders known to be a Clearcase view root
     */
    private Set<File> managedRoots = Collections.synchronizedSet(new HashSet<File>(10));

    private Clearcase() {
    }

    private void init() {
        fileStatusCache = new FileStatusCache();
        clearcaseAnnotator = new ClearcaseAnnotator();
        clearcaseInterceptor = new ClearcaseInterceptor();
        client = new ClearcaseClient(true);        
        topmostManagedParentClient = new ClearcaseClient();        
    }

    public void printlnOut(String msg) {
        openLog();
        log.getOut().println(msg);
    }

    public void printlnErr(String msg) {
        log.getErr().println(msg);
    }
    
    private void openLog() {
        if (log == null || log.isClosed()) {
            log = IOProvider.getDefault().getIO(NbBundle.getMessage(Clearcase.class, "OutputWindow_Clearcase"), false);
            try {                
                log.getOut().reset();   // workaround, otherwise it writes to nowhere
            } catch (IOException e) {
                Utils.logError(this, e);
            }
        }
    }
    
    public void flushLog() {
        if (log != null) {
            log.getOut().close();        
            log.getErr().close();        
        }
    }

    public void focusLog() {
        if (log != null) {
            log.select();
        }
    }
    
    public ClearcaseClient getClient() {
        return client;
    }

    public ClearcaseAnnotator getAnnotator() {
        return clearcaseAnnotator;
    }    
            
    ClearcaseInterceptor getInterceptor() {
        return clearcaseInterceptor;
    }

    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }
    
    /**
     * Serializes requests 
     */
    public RequestProcessor getRequestProcessor() {        
        if(rp == null) {
           rp = new RequestProcessor("ClearCase-tasks");
        }        
        return rp;
    }
    
    /**
     * Tests <tt>.ccrc</tt> directory itself.  
     */
    public boolean isAdministrative(File file) {
        String name = file.getName();
        return isAdministrative(name) && file.isDirectory();
    }

    public boolean isAdministrative(String fileName) {
        // XXX
        return fileName.equals(".ccrc"); // NOI18N
    }
    
    //  - lsvob returns the topmost folder for dynamic views on *nix
    //  - lsview -properties -full returns some usefull info about views (snapshot dynamic etc.)
    // 
    public File getTopmostManagedParent(File file) {
        
        Clearcase.LOG.finer("getTopmostManagedParent " + file);
        
        if(!ClearcaseClient.isAvailable()) return null;
        
        if(file == null) {            
            return null;
        }
        
        File[] roots = managedRoots.toArray(new File[managedRoots.size()]);
        for (File root : roots) {
            if(Utils.isAncestorOrEqual(root, file)) {
                Clearcase.LOG.finest("getTopmostManagedParent cached root " + root +  " for " + file);
                return root;
            }
        }
        Clearcase.LOG.finest("getTopmostManagedParent no cached root for " + file);
        
        // first check if it is a snapshot view. it's quite cheap 
        // compared to the following logic, so run it first
        File ancestor = getTopmostSnapshotViewAncestor(file);        
        if(ancestor != null) {
            // we found the view.dat metadata, but to get sure we still
            // should check via cleartool ls
            IsVersionedCommand cmd = new IsVersionedCommand(file);
            topmostManagedParentClient.exec(cmd, false);            
            if(cmd.hasFailed()) {
                Exception ex = cmd.getThrownException();
                if(ex instanceof ClearcaseUnavailableException) {
                    // clearcase is not installed
                    Clearcase.LOG.finest("getTopmostManagedParent - clearcase not installed: " + ex.getMessage());
                } else {                    
                    Clearcase.LOG.log(Level.WARNING, ex.getMessage());
                }
                return null;
            }
            if(cmd.isVersioned()) {
                managedRoots.add(ancestor);
                Clearcase.LOG.finest("getTopmostManagedParent found snapshot root " + ancestor +  " for " + file);
                return ancestor;
            }
            Clearcase.LOG.finest("getTopmostManagedParent snapshot ancestor " + ancestor + " for " + file + " not versioned");
            // even if under a view.dat folder - yet still unversioned. Lets see if it could be from a dynamic view
        }
        
        // doesn't seem to be a snapshot, try it the hard way
        File parent = file.isDirectory() ? file : file.getParentFile();
        boolean versioned = false;
        while(parent != null) {
            if(!parent.exists()) {
                // IsVersionedCommand works only for existing files !!!
                // we have to look for some existing versioned parent
                file = parent;
                parent = file.getParentFile();    
                continue;
            }
            IsVersionedCommand cmd = new IsVersionedCommand(parent);
            topmostManagedParentClient.exec(cmd, false);       
            if(cmd.hasFailed()) {
                Exception ex = cmd.getThrownException();
                if(ex instanceof ClearcaseUnavailableException) {
                    // clearcase is not installed
                    Clearcase.LOG.finest("getTopmostManagedParent - clearcase not installed: " + ex.getMessage());
                } else {                    
                    Clearcase.LOG.log(Level.WARNING, ex.getMessage());
                }
                return null;
            }
            if(cmd.isVersioned()) {
                file = parent;
                parent = file.getParentFile();                                        
                versioned = true;
            } else if(!versioned) {
                Clearcase.LOG.finest("getTopmostManagedParent no root for " + file);
                return null;    
            } else {
                managedRoots.add(file);
                Clearcase.LOG.finest("getTopmostManagedParent found root " + file);
                return file;
            }    
        }            
        Clearcase.LOG.finest("getTopmostManagedParent no root for " + file);
        return null;       
    }

    public File getTopmostSnapshotViewAncestor(File file) {
        File topmost = null;
        String viewMetaDataName = Utilities.isWindows() ? "view.dat" : ".view.dat";
        for (; file != null; file = file.getParentFile()) {
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            File parent = file.getParentFile();
            if(parent == null) {
                break;
            }
            if (new File(parent, viewMetaDataName).isFile()) {
                topmost = file;
                break;
            }
        }
        return topmost;        
    }
    
    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = Clearcase.getInstance().getFileStatusCache().getInfo(workingCopy);        
        if (!workingCopy.exists() || ((info.getStatus() & FileInformation.STATUS_DIFFABLE) == 0) ) return;
        try {
            File original = VersionsCache.getInstance().getRemoteFile(workingCopy, VersionsCache.REVISION_BASE, true);
            if(!workingCopy.exists()) return;
            if (original == null) {
                throw new IOException("Unable to get BASE revision of " + workingCopy);
            }
            Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
        } catch (IOException e) {
            Utils.logError(this, e);
        }
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status. 
     * All files and folders that are within a VOB are considered managed. 
     * 
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     * @see #IsVersionedCommand
     */ 
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof ClearcaseVCS;
    }    
    
    public String getExtendedNamingSymbol() {
        // TODO: determine from clearcase 
        return "@@";
    }

    /**
     * Gets annotations provider for the supplied context.
     *  
     * @param ctx
     * @return VcsAnnotationsProvider
     */
    public VcsAnnotationsProvider getAnnotationsProvider(VCSContext ctx) {
        return new ClearcaseAnnotationsProvider(ctx);
    }

    /**
     * Executes the annotate command, parses the output and returns list of annotations.
     * 
     * @return List<VcsAnnotation> list of annotations
     */
    private List<VcsAnnotation> fetchVersioningAnnotations(File file) {
        try {
            File annFile = File.createTempFile("clearcase-", ".ann");
            annFile.deleteOnExit();
            AnnotateCommand ac = new AnnotateCommand(file, annFile);
            getClient().exec(ac, true);
            List<VcsAnnotation> annotations = processAnnotations(annFile);
            annFile.delete();
            return annotations;
        } catch (IOException e) {
            Utils.logError(this, e);
        } catch (ClearcaseException e) {
            Utils.logError(this, e);
        } catch (ParseException e) {
            Utils.logError(this, e);
        }
        return null;
    }

    private List<VcsAnnotation> processAnnotations(File annFile) throws IOException, ClearcaseException, ParseException {
        List<VcsAnnotation> anns = new ArrayList<VcsAnnotation>(10);
        BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(annFile)));
        String line;
        int idx = 0;
        while ((line = r.readLine()) != null) {
            idx++;
            Matcher m = annotationLinePattern.matcher(line);
            if (m.matches()) {
                String dateTime     = m.group(1).trim();
                Date date = null;
                try {
                    date = annotationsDateFormat.parse(dateTime);
                } catch (ParseException e) {
                    // OK, ignore this so that rest of annotations are still useful
                    Utils.logWarn(this, e);
                }
                String author       = m.group(2).trim();
                String revision     = m.group(3).trim();
                String text         = m.group(4);
                VcsAnnotation ann = new VcsAnnotation(idx, author, revision, date, text, "");
                anns.add(ann);
            } else {
                throw new ClearcaseException("Invalid annotations format: " + line);
            }
        }
        r.close();
        return anns;
    }

    private class ClearcaseAnnotationsProvider extends VcsAnnotationsProvider {
        
        private final VCSContext context;

        public ClearcaseAnnotationsProvider(VCSContext context) {
            this.context = context;
        }

        public VcsAnnotations getAnnotations() {
            return new ClearcaseVcsAnnotations();
        }
        
        private class ClearcaseVcsAnnotations extends VcsAnnotations {

            protected ClearcaseVcsAnnotations() {
                super();
            }

            @Override
            public VcsAnnotation[] getAnnotations() {
                List<VcsAnnotation> anns = fetchVersioningAnnotations(context.getRootFiles().iterator().next());
                return anns.toArray(new VcsAnnotation[anns.size()]);
            }

            @Override
            public Action[] getActions(VcsAnnotation annotation) {
                String prevRevision = ClearcaseUtils.previousRevision(annotation.getRevision());
                return prevRevision != null ? 
                            new Action[] { new DiffForAnnotationAction(annotation, prevRevision) } :
                            new Action[] { };
            }

            private class DiffForAnnotationAction extends AbstractAction {
                
                private final VcsAnnotation annotation;
                private final String        prevRevision;

                public DiffForAnnotationAction(VcsAnnotation annotation, String prevRevision) {
                    this.prevRevision = prevRevision;
                    putValue(Action.NAME, NbBundle.getMessage(Clearcase.class, "Annotations_DiffTo", prevRevision, annotation.getRevision())); //NOI18N
                    this.annotation = annotation;
                }

                @Override
                public boolean isEnabled() {
                    return prevRevision != null && !prevRevision.trim().equals("");
                }

                public void actionPerformed(ActionEvent e) {
                    try {
                        diffToPrevious(annotation);
                    } catch (IOException e1) {
                        Utils.logError(this, e1);
                    }
                }

                private void diffToPrevious(VcsAnnotation annotation) throws IOException {
                    String selectedRevision = annotation.getRevision();
                    File baseFile = FileUtil.normalizeFile(context.getRootFiles().iterator().next());
                    DiffAction.diff(baseFile, prevRevision, selectedRevision);
                }
            }
        }
    }

    private static class IsVersionedCommand extends ClearcaseCommand {
        
        private File file;
        private List<String> output = new ArrayList<String>(1);

        public IsVersionedCommand(File file) {
            this.file = file;
        }
        
        public String toString() {
            return "ls -long -directory " + file.getAbsolutePath();
        }

        @Override
        public void prepareCommand(Arguments arguments) throws ClearcaseException {
            arguments.add("ls");
            arguments.add("-long");
            arguments.add("-directory"); // so what if not? get sure you don't list a folders children
            arguments.add(file.getAbsoluteFile());
            
            output.clear();
        }                       
        @Override
        public void outputText(String line) {
            // boring
        }

        @Override
        public void errorText(String line) {
            output.add(line); // just get sure and store the whole output. evaluate it later.
        }
                
        boolean isVersioned() {            
            for(String line : output) {
                if(line != null && line.indexOf("cleartool: Error: Pathname is not within a VOB") > -1) {
                    return false;
                }      
            }
            return true;
        }
    };        
}
