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
 *	TestBadNames - test some bad names
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import badnames.*;


public class TestBadNames extends BaseTest {
    public static void main(String[] argv) {
        TestBadNames o = new TestBadNames();
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
        BadNames bn;

        this.readDocument();

        out("creating the bean graph");
        bn = BadNames.createGraph(doc);

        //	Check that we can read the graph and it is complete
        out("bean graph created");
        bn.write(out);

        out(bn);
        out(bn.dumpBeanNode());
        ElT e = bn.getElT();
        // This ought not to be null, but is.
        out("e="+e);

        e = bn.newElT();
        bn.setElT(e);
        out(bn.dumpBeanNode());
        e.setProperty2(0);
        e.setPackage("5");
        e.setClass2("other c");
        bn.write(out);
    }
}
