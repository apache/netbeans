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

package org.openide.filesystems;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.*;
import org.openide.filesystems.test.StatFiles;
import org.openide.util.Lookup;

/**
 *
 * @author  vs124454, rm111737
 * @version
 */
public abstract class TestBaseHid extends MultiThreadedTestCaseHid {
    /** Support for events*/
    protected  List<FileEvent> fileChangedL = new ArrayList<FileEvent> ();
    protected List<FileEvent> fileDCreatedL  = new ArrayList<FileEvent> ();
    protected  List<FileEvent> fileFCreatedL  = new ArrayList<FileEvent> ();
    protected  List<FileEvent> fileDeletedL  = new ArrayList<FileEvent> ();
    protected  List<FileRenameEvent> fileRenamedL  = new ArrayList<FileRenameEvent> ();
    protected  List<FileAttributeEvent> fileAttrChangedL  = new ArrayList<FileAttributeEvent> ();

    private FileChangeListener defListener;
    private String resourcePrefix = "";
    
    static {        
        URL.setURLStreamHandlerFactory(Lookup.getDefault().lookup(URLStreamHandlerFactory.class));
    }    
    
    
    /** Creates new FSTest */    
    public TestBaseHid(String name) {
        super(name);
    }
    
    /** first filesystem allTestedFS[0]*/
    protected FileSystem  testedFS;
    
    /** array of filesystems that can be used for tests. All filesystems should
     * satisfy requirements for resources @see getResources () */
    protected FileSystem  allTestedFS[];
    private static SecurityManager defaultSecurityManager;
    /** If not null, file accesses are counted through custom SecurityManager. */
    public static StatFiles accessMonitor;
    
    @Override
    protected void setUp() throws Exception {                
        System.setProperty("workdir", getWorkDirPath());
        defListener = createFileChangeListener ();

        //FileSystemFactoryHid.destroyFileSystem (this.getName(),this);
        clearWorkDir();
        String[] resources = getResources (getName());        
        resourcePrefix = FileSystemFactoryHid.getResourcePrefix(this.getName(),this, resources);
        if (allTestedFS == null) {
            allTestedFS = FileSystemFactoryHid.createFileSystem(getName(),resources,this);
        }
        if (allTestedFS != null) testedFS = allTestedFS[0];
        // If not null, file accesses are counted through custom SecurityManager.
        if(accessMonitor != null) {
            if(defaultSecurityManager == null) {
                defaultSecurityManager = System.getSecurityManager();
            }
            System.setSecurityManager(accessMonitor);
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        // restore SecurityManager if previously changed
        if(accessMonitor != null) {
            System.setSecurityManager(defaultSecurityManager);
        }
        
        if (testedFS instanceof JarFileSystem) {
            testedFS.removeNotify();    
        }
        testedFS = null;
        allTestedFS = null;
    }

    protected final void registerDefaultListener (FileObject fo) {
        fo.addFileChangeListener(defListener);
    }
    
    protected final void registerDefaultListener (FileSystem fs) {
        fs.addFileChangeListener(defListener);
    }

    protected final void registerDefaultListener (Repository rep) {
        rep.addFileChangeListener(defListener);
    }

//
    private void reinitDefListener() {
        fileChangedL = new ArrayList<FileEvent> ();
        fileDCreatedL  = new ArrayList<FileEvent> ();
        fileFCreatedL  = new ArrayList<FileEvent> ();
        fileDeletedL  = new ArrayList<FileEvent> ();
        fileRenamedL  = new ArrayList<FileRenameEvent> ();
        fileAttrChangedL  = new ArrayList<FileAttributeEvent> ();
    }

    protected final void deregisterDefaultListener (FileObject fo) {
        reinitDefListener();
        fo.removeFileChangeListener(defListener);
    }

    protected final void deregisterDefaultListener (FileSystem fs) {
        reinitDefListener();        
        fs.removeFileChangeListener(defListener);
    }

    protected final void deregisterDefaultListener (Repository rep) {
        reinitDefListener();        
        rep.removeFileChangeListener(defListener);
    }
    
    
    /** Test can require some resources to be part of filesystem that will be tested
     * @return array of resources
     */
    protected abstract String[] getResources (String testName);// {return new String[] {};}
    
       
    public  final void fsTestFrameworkErrorAssert  (String message, boolean condition) {        
        fsAssert  ("Tests did not fail, but test framework contains errors: " + message,condition);
    }
    
    public  final void fsFail  (String message) {
        fail (message + " ["+ FileSystemFactoryHid.getTestClassName () + "]");
    }

    
    public  final void fsAssert  (String message, boolean condition) {
        assertTrue (message + " ["+ FileSystemFactoryHid.getTestClassName () + "]", condition);
    }
    
    public final void fsAssertEquals(String msg, Object o1, Object o2) {
        assertEquals(msg + " [" + FileSystemFactoryHid.getTestClassName() + "]", o1, o2);
    }
    
    public  final void fileChangedAssert  (String message, int expectedCount) {
        fileEventAssert (fileChangedL, message, expectedCount);        
    }

    public  final void fileDataCreatedAssert  (String message, int expectedCount) {
        fileEventAssert (fileDCreatedL, message, expectedCount);
    }

    public  final void fileFolderCreatedAssert  (String message, int expectedCount) {
        fileEventAssert (fileFCreatedL, message, expectedCount);
    }

    public  final void fileDeletedAssert  (String message, int expectedCount) {
        fileEventAssert (fileDeletedL, message, expectedCount);
    }
    
    public  final void fileRenamedAssert  (String message, int expectedCount) {
        fileEventAssert (fileRenamedL , message, expectedCount);
    }    
    
    public  final void fileAttributeChangedAssert (String message, int expectedCount) {
        fileEventAssert (fileAttrChangedL , message, expectedCount);        
    }
     
    private void fileEventAssert  (List list, String message, int expectedCount) {
        fsAssert (message+" Fired : " +list.size () + " ,but expected: " + expectedCount,expectedCount == list.size ()); 
    }
    
    protected FileChangeListener createFileChangeListener () {
     return new FileChangeAdapter () {
            public void fileChanged (FileEvent fe) {
                fileChangedL.add (fe);
            }
            public void fileDeleted (FileEvent fe) {
                fileDeletedL.add (fe);
            }
            public void fileFolderCreated (FileEvent fe) {
                fsAssert("Unexpected data file", fe.getFile().isFolder());
                fileFCreatedL.add (fe);
            }
            public void fileDataCreated (FileEvent fe) {
                fsAssert("Unexpected folder", fe.getFile().isData());                
                fileDCreatedL.add (fe);
            }
            public void fileRenamed (FileRenameEvent fe) {
                fileRenamedL.add (fe);
            }            
            
            public void fileAttributeChanged (FileAttributeEvent fe) {
                fileAttrChangedL.add (fe);
            }
        };   
    }

    protected String getResourcePrefix() {
        return resourcePrefix;
    }
}
