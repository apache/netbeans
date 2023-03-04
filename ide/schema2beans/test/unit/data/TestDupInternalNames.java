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
 *	TestDupInternalNames - test what happens when different complexTypes
 *                         have the same named element declaration
 *                         inside of them.  Duplicate type names should
 *                         get renamed.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestDupInternalNames.xml
 *	file and this java test should be kept in sync.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import java.math.*;

import root.*;


public class TestDupInternalNames extends BaseTest {
    public static void main(String[] argv) {
        TestDupInternalNames o = new TestDupInternalNames();
        if (argv.length > 0)
            o.setDocumentDir(argv[0]);
        try {
            o.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
    
    public void run() throws Exception {
        Root dupInternalNames;

        this.readDocument();

        out("creating the bean graph");
        dupInternalNames = Root.read(doc);
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
        dupInternalNames.write(out);

        AnalogInput in = dupInternalNames.getIn();
        in.setPointNumber(new BigInteger("5"));
        AnalogOutput o = dupInternalNames.getOut();
        o.setPointNumber(new BigInteger("89"));
        dupInternalNames.write(out);
    }
}
