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

package org.netbeans.modules.maven.runjar;

import junit.framework.TestCase;

/**
 * @author mkleint
 */
public class PropertySplitterTest extends TestCase {
    
    public PropertySplitterTest(String testName) {
        super(testName);
    }
    
    public void testNextPair() {
        PropertySplitter instance = new PropertySplitter("exec=\"tes t1\"");
        String result = instance.nextPair();
        assertEquals("exec=\"tes t1\"", result);
        instance = new PropertySplitter("exec=tes t1\nexec2=te st2");
        result = instance.nextPair();
        assertEquals("exec=tes t1", result);
        result = instance.nextPair();
        assertEquals("exec2=te st2", result);
        
        instance = new PropertySplitter("exec=\"test1 exec2=test2\"");
        result = instance.nextPair();
        assertEquals("exec=\"test1 exec2=test2\"", result);
        //Issue MEVENIDE-600
        instance = new PropertySplitter("netbeans.jar.run.workdir=\"C:\\Documents and Settings\\Anuradha\\My Documents\\NetBeansProjects\\mavenproject4\"");
        result = instance.nextPair();
        assertEquals("netbeans.jar.run.workdir=\"C:\\Documents and Settings\\Anuradha\\My Documents\\NetBeansProjects\\mavenproject4\"", result);
        
        instance = new PropertySplitter("exec=\"test1 exec2=test2\"\nexec2=\"test3==test3\"");
        result = instance.nextPair();
        assertEquals("exec=\"test1 exec2=test2\"", result);
        result = instance.nextPair();
        assertEquals("exec2=\"test3==test3\"", result);

        instance = new PropertySplitter("\"-Dfoo bar=baz quux\" whatever");
        instance.setSeparator(' ');
        assertEquals("\"-Dfoo bar=baz quux\"", instance.nextPair());
        assertEquals("whatever", instance.nextPair());
        assertEquals(null, instance.nextPair());
        
        instance = new PropertySplitter("foo=1\\\n2\\\n3\nbar=123");
        assertEquals("foo=123", instance.nextPair());
        assertEquals("bar=123", instance.nextPair());
        assertEquals(null, instance.nextPair());
        
    } 
    
    public void testIssue211686() {
        PropertySplitter instance = new PropertySplitter("-Dmaven.home=\"C:\\Program Files\\NetBeans Dev 201204240400\\java\\maven\" -Xms10m -classpath %classpath test.mavenproject17.App");
        instance.setSeparator(' ');
        assertEquals("-Dmaven.home=\"C:\\Program Files\\NetBeans Dev 201204240400\\java\\maven\"", instance.nextPair());

        instance = new PropertySplitter("-Dmaven.home='C:\\Program Files\\NetBeans Dev 201204240400\\java\\maven' -Xms10m -classpath %classpath test.mavenproject17.App");
        instance.setSeparator(' ');
        assertEquals("-Dmaven.home='C:\\Program Files\\NetBeans Dev 201204240400\\java\\maven'", instance.nextPair());
        
        // and embed the double quotes in quotes..
        instance = new PropertySplitter("-Dmaven.home='C:\\Program Files\\NetBeans Dev 2012\" \"04240400\\java\\maven' -Xms10m -classpath %classpath test.mavenproject17.App");
        instance.setSeparator(' ');
        assertEquals("-Dmaven.home='C:\\Program Files\\NetBeans Dev 2012\" \"04240400\\java\\maven'", instance.nextPair());
        
        // and embed the quotes in double quotes..
        instance = new PropertySplitter("-Dmaven.home='C:\\Program Files\\NetBeans Dev 2012\" \"04240400\\java\\maven' -Xms10m -classpath %classpath test.mavenproject17.App");
        instance.setSeparator(' ');
        assertEquals("-Dmaven.home='C:\\Program Files\\NetBeans Dev 2012\" \"04240400\\java\\maven'", instance.nextPair());

    }

}
