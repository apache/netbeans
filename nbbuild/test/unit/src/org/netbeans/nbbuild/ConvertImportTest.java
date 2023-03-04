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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.*;
import org.netbeans.junit.*;

/**
 *
 * @author pzajac
 */
public class ConvertImportTest extends NbTestCase {
    private File testFile;
    public ConvertImportTest(java.lang.String testName) {
        super(testName);
    }

    
    public void testConvertImport() throws IOException {
       String xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       String xmlOut =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../templates/xtest-unit.xml\"/>\n" +
           "</project>";

       String xmlOutPrefix =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"${test.dist.dir}/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       
       createFile(xml);
       
       ConvertImport convert = new ConvertImport();
       convert.setFile(testFile); 
       convert.setOldName("templates/xtest-unit.xml");
       convert.setNewPath("../templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOut);
       
       createFile(xml);
       convert.setPropertyPrefixName("test.dist.dir");
       convert.setNewPath("templates/xtest-unit.xml");
       convert.execute();
       assertNewXml(xmlOutPrefix); 
 
        xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<!-- <import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n-->" +
           "</project>";
       createFile(xml);
       convert.execute();
       assertNewXml(xml);

       xml =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<!-- file -->" +
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"../../../nbbuild/templates/xtest-unit.xml\"/>\n" +
           "</project>";
       xmlOut =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<!-- file -->" +               
           "<project name=\"ant/freeform/test-unit\" basedir=\".\" default=\"all\">\n" +
           "<import file=\"${test.dist.dir}/xx\"/>\n" +
           "</project>";
       createFile(xml);
       convert.setNewPath("xx");
       convert.execute();
       assertNewXml(xmlOut);
       
    }

    private File createFile(String xml) throws IOException {
       testFile = new File(getWorkDir(),"testFile.xml");
        try (PrintStream ps = new PrintStream(testFile)) {
            ps.print(xml);
        }
       return testFile;
    }

    private void assertNewXml(String xmlOut) throws IOException {
        File file = new File(getWorkDir(),"ref.xml");
        PrintStream ps = new PrintStream(file);
        ps.print(xmlOut);
        assertFile(testFile,file);
    }
}
