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

package org.netbeans.modules.groovy.editor.api.completion;

/**
 *
 * @author schmidtm
 */
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author schmidtm
 */
public class FieldCCTest extends GroovyCCTestBase {

    String TEST_BASE = "testfiles/completion/field/";

    public FieldCCTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        super.setUp();
    }

    @Override
    protected String getTestType() {
        return ".";
    }

    // uncomment this to have logging from GroovyLexer
    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
        // we are only interested in a single logger, so we set its level in setUp(),
        // as returning Level.FINEST here would log from all loggers
    }

    protected @Override Map<String, ClassPath> createClassPathsForTest() {
        Map<String, ClassPath> map = super.createClassPathsForTest();
        map.put(ClassPath.SOURCE, ClassPathSupport.createClassPath(new FileObject[] {
            FileUtil.toFileObject(getDataFile("/testfiles/completion/field")) }));
        return map;
    }

    public void testFields1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields1.groovy", "\"User $nom^\"", true);
    }

    public void testFields2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields1.groovy", "println \"Hi: $ad^\"", true);
    }

    /**
     * Groovy fields (not properties) with private modifiers are accessible.
     */
    public void testFields2_modifiers() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "new Helper().builder^Property.inheritIO()", true);
    }
    
    /**
     * But java class fields respect the access modifiers. Must not contain 'classLoader0' property.
     */
    public void testFields2_javaModifiers() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "Helper.class.classL^oader", true);
    }
    
    public void testFields2_propertyChain1() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "someFile.parent^File.mkdirs()", true);
    }

    public void testFields2_propertyChain2() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "someFile.absoluteFile.parent^File.mkdirs()", true);
    }

    public void testFields2_propertyChain3() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "someFile.absoluteFile.parentFile.mkd^irs()", true);
    }

    /**
     * Cannot work ATM, see NETBEANS-5964
     * @throws Exception 
    public void testFields2_propertyChain4() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "(someFile.canonicalFile.getParentFile).mkd^irs()", true);
    }
     */
    
    /**
     * See NETBEANS-5991 -- bug in type parameters substitution. Note that JDK9 added j.u.Properties#get(Object) -> Object
     * while JDK8 had no such method. Since V (unsubstituted superclass' parameter) != Object, JDK9+ reports get() -> Object
     * (signature of superclass' get() -> V is the same, so it is ignored) and 2 getOrDefault as the superclass' signature contains 'V' 
     * and is therefore different.
     */
    public void testFields2_javaPropertyReference() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "System.properties.ge^t(", true);
    }
    
    public void testFields2_otherClassProperty() throws Exception {
        checkCompletion(TEST_BASE + "" + "Fields2.groovy", "new Helper().builderProperty.inher^itIO()", true);
    }
}
