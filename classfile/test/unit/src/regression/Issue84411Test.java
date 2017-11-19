/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package regression;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author tball
 */
public class Issue84411Test extends TestCase {
    
    public Issue84411Test(String testName) {
        super(testName);
    }
    
    /**
     * Test whether the SwitchData.class from Java 6 build 71 can be read 
     * successfully.  Issue 84411 reported that an IndexOutOfBoundsException
     * was thrown due to an invalid name_attribute_index in one of that 
     * class's Code attributes.
     */
    public void test84411() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/SwitchData.class");
        ClassFile classFile = new ClassFile(classData);
        classFile.toString();
    }
}
