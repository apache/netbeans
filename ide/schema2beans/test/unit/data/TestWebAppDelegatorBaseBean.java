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
 *	TestWebAppDelegatorBaseBean - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestWebAppDelegatorBaseBean.xml
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


public class TestWebAppDelegatorBaseBean extends BaseTest {
    public static void main(String[] argv) {
        TestWebAppDelegatorBaseBean o = new TestWebAppDelegatorBaseBean();
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
        WebAppDelegator webApp;

        this.readDocument();

        out("creating the bean graph");
        webApp = WebAppDelegator.createGraph(doc);

        //	Check that we can read the graph and it is complete
        out("bean graph created");
        webApp.write(out);

        out("making some minor changes");
        webApp.setDescription(0, "Changed the description");
        webApp.getFilter(0).setFilterClass("foo");
        webApp.write(out);

        out("set some goodnesses");
        SecurityConstraintType sc = new SecurityConstraintType();
        webApp.addSecurityConstraint(sc);
        sc.setAuthConstraint(new AuthConstraintType());
        sc.getAuthConstraint().addGoodPresidentCandidate(true);
        sc.getAuthConstraint().addGoodPresidentCandidate(false);
        boolean firstCandiateGoodness = sc.getAuthConstraint().isGoodPresidentCandidate(0);
        check(firstCandiateGoodness == true, "firstCandiateGoodness is good");
        boolean[] allCandidateGoodnesses = sc.getAuthConstraint().getGoodPresidentCandidate();
        check(allCandidateGoodnesses.length == 2, "2 candidates goodness");
        check(allCandidateGoodnesses[0] == true, "candidate 0 is good");
        check(allCandidateGoodnesses[1] == false, "candidate 1 is not good");
        webApp.write(out);

        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (org.netbeans.modules.schema2beans.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        webApp.setVersion("2.4");
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (org.netbeans.modules.schema2beans.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.addWebResourceCollection(new WebResourceCollectionType());
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (org.netbeans.modules.schema2beans.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.getWebResourceCollection(0).setWebResourceName("blue");
        try {
            webApp.validate();
            check(false, "Failed to get validate exception");
        } catch (org.netbeans.modules.schema2beans.ValidateException e) {
            check(true, "Got good validate exception: "+e.getMessage());
        }
        sc.getWebResourceCollection(0).addUrlPattern("*.html");
        webApp.write(out);
        webApp.validate();

        out("Add some descriptions with xml:lang attributes.");
        webApp.setDescriptionXmlLang(0, "en");
        webApp.addDescription("Das ist mein App.");
        webApp.setDescriptionXmlLang(1, "de");
        webApp.validate();
        webApp.write(out);

        out("Test multiple things");
        check("thing1".equals(webApp.getThing().getFilterName()), "thing1");
        check("thing2".equals(webApp.getThing2().getFilterName()), "thing2");
        check("thing3".equals(webApp.getThing3().getFilterName()), "thing3");

        webApp._setSchemaLocation("http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
        webApp.write(out);
    }
}
