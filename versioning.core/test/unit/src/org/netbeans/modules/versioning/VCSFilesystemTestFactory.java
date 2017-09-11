package org.netbeans.modules.versioning;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSetup;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tomas
 */
public abstract class VCSFilesystemTestFactory extends NbTestSetup {

    public VCSFilesystemTestFactory(Test test) {
        super(test);
        registerMap (test);
    }

    /**
     * Determines the root folder under which the factory creates files.
     * 
     * @return 
     * @throws IOException 
     */
    protected abstract String getRootPath() throws IOException;
    
    /**
     * Creates a file with a full path determined by the factories common root
     * and the given relative path.
     * 
     * @param path
     * @return
     * @throws IOException 
     */
    protected abstract FileObject createFile(String path) throws IOException;
    
    /**
     * Creates a folder with a full path determined by the factories common root.
     * and the given relative path, 
     * 
     * @param path 
     * @return
     * @throws IOException 
     */
    protected abstract FileObject createFolder(String path) throws IOException;
    
    /**
     * Set the file with the given relative path as read-only.
     * 
     * @param path
     * @throws IOException 
     */
    protected abstract void setReadOnly(String path) throws IOException;

    /**
     * Deletes the file with the given relative path. 
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void delete(String path) throws IOException;
    
    /**
     * Move the files with the given relative paths
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void move(String from, String to) throws IOException;
    
    /**
     * Copy the files with the given relative paths
     * 
     * @param path
     * @throws IOException 
     */
    public abstract void copy(String from, String to) throws IOException;
    
    public static VCSFilesystemTestFactory getInstance (Test test) {
        VCSFilesystemTestFactory factory = getFromMap (test);
        return factory;
    }

    private static Map<Test, List<VCSFilesystemTestFactory>> map = new HashMap<Test, List<VCSFilesystemTestFactory>> ();

    private synchronized void registerMap (Test test) {
        if (test instanceof TestSuite) {
            Enumeration en = ((TestSuite)test).tests ();
            while (en.hasMoreElements()) {
                Test tst = (Test)en.nextElement();
                if (tst instanceof TestSuite)
                    registerMap (tst);
                else {
                    addToMap (tst);
                }
            }
        } else {
            addToMap (test);
        }
    }

    private synchronized void addToMap (Test test) {
        List<VCSFilesystemTestFactory> s = map.get (test);
        if (s == null) {
            s = new LinkedList<VCSFilesystemTestFactory>();
        }
        s.add(this);
        map.put(test ,s );
    }

    private synchronized static VCSFilesystemTestFactory getFromMap (Test test) {
        LinkedList s = (LinkedList) map.get (test);
        VCSFilesystemTestFactory  retVal;
        try {
            retVal = (VCSFilesystemTestFactory) s.getLast();
        } catch (NoSuchElementException x ) {
            System.out.println("exc: "+ test + " : " );
            throw x;
        }
        return retVal;
    }

}
