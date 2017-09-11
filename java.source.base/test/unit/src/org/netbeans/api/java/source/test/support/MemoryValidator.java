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
package org.netbeans.api.java.source.test.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class MemoryValidator extends NbTestCase {
    
    private static final boolean ENABLED = Boolean.getBoolean("org.netbeans.api.java.source.test.support.MemoryValidator.enable");
    
    private TestCase delegate;
    
    /** Creates a new instance of MemoryValidator */
    public MemoryValidator(TestCase delegate) {
        super(delegate.getName());
        
        this.delegate = delegate;
    }
    
    public static Test wrap(Test t) {
        if (t instanceof TestCase) {
            return wrap((TestCase) t);
        }
        if (t instanceof TestSuite) {
            return wrap((TestSuite) t);
        }
        
        throw new IllegalArgumentException("Unknown type to wrap");
    }
    
    public static TestCase wrap(TestCase t) {
        return new MemoryValidator(t);
    }
    
    public static TestSuite wrap(TestSuite t) {
        TestSuite result = new TestSuite();
        
        for (int cntr = 0; cntr < t.testCount(); cntr++) {
            result.addTest(wrap(t.testAt(cntr)));
        }
        
        return result;
    }

    private static Logger log = Logger.getLogger(MemoryValidator.class.getName());
    protected @Override void runTest() throws Throwable {
        delegate.runBare();
        
        if (ENABLED) {
            //if the tests passes, check if all the DataObjects created during the test are reclaimable.
            //the same for all corresponding JavaSources.
            long start = System.currentTimeMillis();
            long end = -1;
            
            try {
                Collection<FileObject> allFileObjects = null;
                
                try {
                    Class poolClass = Class.forName("org.openide.loaders.DataObjectPool");
                    Method getPOOL = poolClass.getDeclaredMethod("getPOOL", new Class[0]);
                    getPOOL.setAccessible(true);
                    Object pool = getPOOL.invoke(null, new Object[0]);
                    Field m = poolClass.getDeclaredField("map");
                    m.setAccessible(true);
                    
                    Map<FileObject, Object> map = (Map) m.get(pool);
                    
                    allFileObjects = new HashSet(map.keySet());
                } catch  (ThreadDeath t) {
                    throw t;
                } catch (Throwable t) {
                    if (log.isLoggable(Level.SEVERE))
                        log.log(Level.SEVERE, t.getMessage(), t);
                }
                
                if (allFileObjects != null) {
                    for (Iterator<FileObject> i = allFileObjects.iterator(); i.hasNext(); ){
                        FileObject file = i.next();
                        
                        i.remove();
                        
                        String name = FileUtil.getFileDisplayName(file);
                        DataObject d = DataObject.find(file);
                        JavaSource s = JavaSource.forFileObject(d.getPrimaryFile());
                        
                        if (s != null) {
                            Reference rD = new WeakReference(d);
                            Reference sD = new WeakReference(s);
                            
                            file = null;
                            d = null;
                            s = null;
                            
                            NbTestCase.assertGC(name, rD);
                            NbTestCase.assertGC(name, sD);
                        }
                    }
                }
                
                end = System.currentTimeMillis();
            } finally {
                if (end != (-1)) {
                    log(getName() + ": reference check took: " + (end - start));
                } else {
                    log(getName() + ": reference check failed");
                }
            }
        }
    }

}
