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
package org.netbeans.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;

import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.loaders.DataObject;

import org.openide.filesystems.multifs.MultiXMLFSTest;
import org.openide.filesystems.multifs.MultiXMLFSTest.FileWrapper;
import org.openide.filesystems.ReadOnlyFSTest;
import org.openide.loaders.Utilities;

import org.netbeans.ProxyClassLoader;

import org.netbeans.performance.MapArgBenchmark;
import org.netbeans.performance.DataManager;
import org.netbeans.performance.DataDescriptor;

/**
 * Performance test for <code>ProxyClassLoader</code>
 * <em>Note:</em> originally it was created for <code>MultiURLClassLoader</code>
 * therefore the name, now just changed for ProxyClassLoader.
 */
public class MultiURLClassLoaderTest extends MapArgBenchmark implements DataManager {
    
    private static final String CLASS_NO_KEY = "CLASS_NO";
    
    private MultiXMLFSTest mfstest;
    private InstanceDataObject[] instanceObjects;
    
    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */    
    public MultiURLClassLoaderTest(String name) {
        super(name);
        setArgumentArray(createArguments());
        init();
    }
    
    /** Creates an argument array */
    private Map[] createArguments() {
        Map[] ret = new Map[] { createDefaultMap(), createDefaultMap() };
        ret[1].put(CLASS_NO_KEY, new Integer(1000));
        return ret;
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public MultiURLClassLoaderTest(String name, Object[] args) {
        super(name, args);
        init();
    }
    
    /** init */
    private void init() {
        mfstest = new MultiXMLFSTest(getName());
    }
    
    // things to override by the implementation of a particular Benchmark

    /** This method is called before the actual test method to allow
     * the benchmark to prepare accordingly to informations available
     * through {@link #getIterationCount}, {@link #getArgument} and {@link #getName}.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void setUp() throws Exception {
        int size = getIntValue(CLASS_NO_KEY);
        
        FileObject[] fileObjects = mfstest.setUpFileObjects(size);
        FileWrapper[] wrappers = mfstest.getFileWrappers();
        
        URLClassLoader[] parents = new URLClassLoader[wrappers.length];
        for (int i = 0; i < parents.length; i++) {
            parents[i] = new URLClassLoader(new URL[] { wrappers[i].getMnt().toURL() });
        }
        
//        MultiURLClassLoader multicloader = new MultiURLClassLoader(new URL[] {}, parents);
        ClassLoader multicloader = new ProxyClassLoader(parents);
        
        instanceObjects = fileObjects2InstanceDataObjects(fileObjects);
        setClassLoader(instanceObjects, multicloader);
    }
    
    private static InstanceDataObject[] fileObjects2InstanceDataObjects(FileObject[] fos) throws Exception {
        ArrayList list = new ArrayList(fos.length);
        for (int i = 0; i < fos.length; i++) {
            DataObject res = DataObject.find(fos[i]);
            if (res instanceof InstanceDataObject) {
                list.add(res);
            }
        }
        
        return (InstanceDataObject[]) list.toArray(new InstanceDataObject[list.size()]);
    }
    
    private static void setClassLoader(InstanceDataObject[] idos, ClassLoader cl) throws Exception {
        for (int i = 0; i < idos.length; i++) {
            Utilities.setCustomClassLoader(idos[i], cl);
        }
    }

    /** This method is called after every finished test method.
     * It is intended to be used to free all the resources allocated
     * during {@link #setUp} or the test itself.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void tearDown() throws Exception {
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        Map map = super.createDefaultMap();
        map.put(CLASS_NO_KEY, new Integer(500));
        map.put(MultiXMLFSTest.XMLFS_NO_KEY, new Integer(30));
        return map;
    }
    
    /** Called after tearDown()  */
    public void tearDownData() throws Exception {
        mfstest.tearDownData();
    }

    /** Called before setUp()  */
    public DataDescriptor createDataDescriptor() {
        return new MUCDataDescriptor(getIntValue(CLASS_NO_KEY), getIntValue(MultiXMLFSTest.XMLFS_NO_KEY));
    }
    
    /** Called before setUp()  */
    public void setUpData(DataDescriptor ddesc) throws Exception {
        MUCDataDescriptor dd = (MUCDataDescriptor) ddesc;
        DataDescriptor other = dd.getDD();
        
        int fileNo = getIntValue(CLASS_NO_KEY);
        Map map = (Map) getArgument();
        map.put(ReadOnlyFSTest.FILE_NO_KEY, new Integer(fileNo));
        
        mfstest.setParent(this);
        
        if (other == null) {
            other = mfstest.createDataDescriptor();
            dd.setDD(other);
        }
        
        mfstest.setUpData(other);
    }

    //--------------------- tests ---------------------
    /** MultiURLClassLoader */
    public void testInstanceClasses() throws Exception {
        for (int i = 0; i < instanceObjects.length; i++) {
            String klass = instanceObjects[i].instanceName();
            instanceObjects[i].instanceClass();
        }
    }
    
    /*
    public static void main(String[] args) throws Exception {
        MultiURLClassLoaderTest mcltest = new MultiURLClassLoaderTest("test");
        mcltest.setUp();
        
        System.out.println("ORDINARY: " + mcltest.wrappers[1].getClassLoader().loadClass("org.openide.filesystems.data10.JavaSrc15"));
        System.out.println("Multi: " + mcltest.multicloader.loadClass("org.openide.filesystems.data10.JavaSrc15"));
        System.out.println("Multi2: " + mcltest.multicloader.loadClass("org.openide.filesystems.data90.JavaSrc99"));
    }
     */
}
