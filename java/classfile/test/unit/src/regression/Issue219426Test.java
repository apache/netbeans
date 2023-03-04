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
/*
 * Issue84411Test.java
 * JUnit based test
 *
 * Created on September 9, 2006, 9:01 AM
 */

package regression;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import junit.framework.TestCase;
import org.netbeans.modules.classfile.*;

/**
 *
 * @author tball
 * @author Tomas Zezula
 */
public class Issue219426Test extends TestCase {

    public Issue219426Test(String testName) {
        super(testName);
    }

    public void test84411() throws Exception {
        final InputStream classData = new ByteArrayInputStream(DATA);
        try {
            final ClassFile c = new ClassFile(classData);
            fail("Invalid class name [, exception expected.");    //NOI18N
        } catch (InvalidClassFormatException e) {
            //OK, expected
        } finally {
            classData.close();
        }
    }

    private static final byte[] DATA = {
        (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x2D,
        (byte) 0x00, (byte) 0x0E, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x5B, (byte) 0x0A, (byte) 0x00,
        (byte) 0x0D, (byte) 0x00, (byte) 0x07, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x6A, (byte) 0x61,
        (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61, (byte) 0x6E, (byte) 0x67, (byte) 0x2F,
        (byte) 0x4F, (byte) 0x62, (byte) 0x6A, (byte) 0x65, (byte) 0x63, (byte) 0x74, (byte) 0x01, (byte) 0x00,
        (byte) 0x0A, (byte) 0x53, (byte) 0x6F, (byte) 0x75, (byte) 0x72, (byte) 0x63, (byte) 0x65, (byte) 0x46,
        (byte) 0x69, (byte) 0x6C, (byte) 0x65, (byte) 0x01, (byte) 0x00, (byte) 0x06, (byte) 0x3C, (byte) 0x69,
        (byte) 0x6E, (byte) 0x69, (byte) 0x74, (byte) 0x3E, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x6D,
        (byte) 0x61, (byte) 0x69, (byte) 0x6E, (byte) 0x0C, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x0C,
        (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x43, (byte) 0x6F, (byte) 0x64, (byte) 0x65, (byte) 0x07,
        (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x0D, (byte) 0x48, (byte) 0x65, (byte) 0x6C,
        (byte) 0x6C, (byte) 0x6F, (byte) 0x2D, (byte) 0x57, (byte) 0x6F, (byte) 0x72, (byte) 0x6C, (byte) 0x64,
        (byte) 0x2E, (byte) 0x6A, (byte) 0x01, (byte) 0x00, (byte) 0x16, (byte) 0x28, (byte) 0x5B, (byte) 0x4C,
        (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61, (byte) 0x6E,
        (byte) 0x67, (byte) 0x2F, (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x67,
        (byte) 0x3B, (byte) 0x29, (byte) 0x56, (byte) 0x01, (byte) 0x00, (byte) 0x03, (byte) 0x28, (byte) 0x29,
        (byte) 0x56, (byte) 0x07, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x09,
        (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x11, (byte) 0x00, (byte) 0x01,
        (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x2A, (byte) 0xB7,
        (byte) 0x00, (byte) 0x02, (byte) 0xB1, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x09, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x0B, (byte) 0x00, (byte) 0x01, (byte) 0x00,
        (byte) 0x08, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0D, (byte) 0x00, (byte) 0x02, (byte) 0x00,
        (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xB1, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0A,
    };
}
