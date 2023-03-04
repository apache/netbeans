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
 *	TestFinalWebApp - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestFinalWebApp.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *	single String: get/set/remove/set/get
 *	boolean (from true): get/set true/get/set false/get/set true/get
 *	boolean (from false): get/set false/get/set true/get/set false/get
 *	String[]: get/set (null & !null)/add/remove
 *	Bean: remove/set(null)/create bean and graph of beans/set/add
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import webapp.*;

public class TestFinalWebApp extends BaseTest {
    public static void main(String[] argv) {
        TestFinalWebApp o = new TestFinalWebApp();
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
        WebApp webApp;

        this.readDocument();

        out("creating the bean graph");
        webApp = WebApp.read(doc);
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
        webApp.write(out);

        out("Adding filters");
        FilterType filter = new FilterType();
        filter.addDisplayName("filter BLUE");
        filter.addDisplayNameXmlLang("en");
        webApp.addFilter(filter);
        filter = new FilterType();
        filter.addDisplayName("filter GREEN");
        webApp.write(out);
        out("Validating");
        try {
            webApp.validate();
            out("No validation problem");
        } catch (webapp.WebApp.ValidateException e) {
            out("Got exception: "+e.getMessage());
        }

        WebApp copy1 = new WebApp(webApp);
        copy1.write(out);
    }
}
