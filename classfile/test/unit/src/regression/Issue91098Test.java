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
import java.util.List;
import junit.framework.TestCase;
import junit.framework.*;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author Jan Lahoda
 */
public class Issue91098Test extends TestCase {
    
    public Issue91098Test(String testName) {
        super(testName);
    }
    
    public void testAttributeLoading() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/test91098.class");
        ClassFile classFile = new ClassFile(classData);
        classFile.toString();
    }
    
    public void testHasDeprecatedAttribute() throws Exception {
        InputStream classData = 
            getClass().getResourceAsStream("datafiles/test91098.class");
        ClassFile classFile = new ClassFile(classData);
        Method meth = classFile.getMethod("<init>", "(Ljava/lang/String;II)V");
        List<Parameter> params = meth.getParameters();
        assertEquals(params.size(), 3);  // declared parameter, plus two for internal enum params
        Parameter param = params.get(0);
        Annotation[] annotations = param.getAnnotations().toArray(new Annotation[0]);
        assertEquals(annotations.length, 1);
        ClassName type = annotations[0].getType();
        assertEquals(type.getExternalName(), "java.lang.Deprecated");
    }
}
