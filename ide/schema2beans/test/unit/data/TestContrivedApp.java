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
 *	TestBook - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestBook.xml
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

import org.netbeans.modules.schema2beans.*;
import application.*;


public class TestContrivedApp extends BaseTest {
    public static void main(String[] argv) {
        BaseTest o = new TestContrivedApp();
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
        Application app;

        out("creating the bean graph");

        app = Application.createGraph(new FileInputStream(getFullDocumentName()));
	
        //	Check that we can read the graph and it is complete
        out("bean graph created");
	
        out(app);
        application.Module module = app.getModule();
        out("module.myaltDd = "+module.getMyAltDd());
        out("Make sure XML metacharacters get escapped");
        module.setMyAltDd("Foo & Co");
        out(app);
        out("Make sure alt-dd is still there with no whitespace");
        module.setMyAltDd("");
        out(app);
        out("alt-dd goes away now");
        module.setMyAltDd(null);
        out(app);
        module.setAlternateNameEjb("blue");
        out(app);
        //Module optionalModule = new Module();
        //optionalModule.setAlternateNameEjb("optional module");
        //app.setModule2(optionalModule);
        //out(app);
        
        return;
    }
	
    void parse(BaseBean bean, String parse) {
        out("Parsing " + parse);
        DDParser p = new DDParser(bean, parse);
        while (p.hasNext()) {
            Object o = p.next();
            if (o != null) {
                if (o instanceof BaseBean)
                    this.out(((BaseBean)o).dumpBeanNode());
                else
                    this.out(o.toString());
            }
            else
                this.out("null");
        }
    }
    
    void printChoiceProperties(BaseProperty[] bps) {
        if (bps == null)
            err("got null instead a BaseProperty[] instance");
        else {
            for (int l=0; l<bps.length; l++)
                check(bps[l].isChoiceProperty(), bps[l].getDtdName());
        }
    }
}

