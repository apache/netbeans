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
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.output;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.maven.execute.AbstractOutputHandler;

/**
 *
 * @author  Anuradha G (anuradha@codehaus.org)
 * 
 * Test case for issue http://jira.codehaus.org/browse/MEVENIDE-637
 */
public class MEVENIDE637Test extends TestCase {

    public MEVENIDE637Test(java.lang.String testName) {
        super(testName);
    }
   
    public void testSeparatorSplit() {
         String aString="Some text \n a and some more .. \n and more..";//linux and unix
         
         List<String> strs = AbstractOutputHandler.splitMultiLine(aString);
         
         assertEquals(strs.size(), 3);
         assertEquals("Some text ", strs.get(0));
         assertEquals(" a and some more .. ", strs.get(1));
         assertEquals(" and more..", strs.get(2));
         
         aString="Some text \r a and some more .. \r and more..";//Mac
         
         strs = AbstractOutputHandler.splitMultiLine(aString);
         
         assertEquals(strs.size(), 3);

         assertEquals("Some text ", strs.get(0));
         assertEquals(" a and some more .. ", strs.get(1));
         assertEquals(" and more..", strs.get(2));
         
         aString="Some text \r\n a and some more .. \r\n and more..";//Windows
         
         strs = AbstractOutputHandler.splitMultiLine(aString);

         assertEquals(strs.size(), 3);
         assertEquals("Some text ", strs.get(0));
         assertEquals(" a and some more .. ", strs.get(1));
         assertEquals(" and more..", strs.get(2));
         
         //MEVENIDE-637
         aString="\r\n\nMojo: \n\n  org.apache.maven.plugins:maven-compiler-plugin:2.0.2:compile" +
                 "\n\nFAILED for project:" +
                 "\n\n  example:ExampleProject:jar:1.0-SNAPSHOT\n\nReason:\n\nC:" +
                 "\\ExampleProject\\src\\main\\java\\example\\App.java:[11,8] cannot find symbol" +
                 "\n\nsymbol  : class MyObject\n\nlocation: class example.App";
         strs = AbstractOutputHandler.splitMultiLine(aString);

         assertEquals(strs.size(), 8);
         
         assertEquals("Mojo: ", strs.get(0));
         
    }
 
}
