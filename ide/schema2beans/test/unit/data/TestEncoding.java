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
 *	TestEncoding - Make sure we correctly deal with I18N encoding stuff.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import forteddl.*;


public class TestEncoding extends BaseTest {
    public static void main(String[] argv) {
        BaseTest o = new TestEncoding();
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
        Forteddl app;

        out("creating the bean graph");
        app = Forteddl.createGraph(new FileInputStream(getFullDocumentName()));
	
        //	Check that we can read the graph an it is complete
        out("bean graph created");
	
        out(app);
        
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

