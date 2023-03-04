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
 * FieldElem.java
 *
 * Created on June 26, 2000, 9:29 AM
 */
package org.netbeans.test.java.generating;

import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import junit.framework.Test;
import org.netbeans.test.java.Common;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;

/** <B>Java Module General API Test: FieldElement</B>
 * <BR><BR><I>What it tests:</I><BR>
 * Creating and handling with FieldElement.
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * FieldElements are created and customized using setters and then added using ClassElement.addField() into ClassElement.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <U>Fields are not inserted properly:</U><BR>
 * If there are some fields in .diff file.
 * <BR><BR><U>Fields have bad properties (e.g. modifiers, return type)</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (->Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><U>Exception occured:</I><BR>
 * See .log file for StackTrace
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */
public class FieldElem extends org.netbeans.test.java.XRunner {

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public FieldElem() {
        super("");
    }

    public FieldElem(java.lang.String testName) {
        super(testName);
    }

    /** "body" of this TestCase
     * @param o SourceElement - target for generating
     * @param log log is used for logging StackTraces
     * @throws Exception
     * @return true if test passed
     * false if failed
     */
    public boolean go(Object o, java.io.PrintWriter log) throws Exception {

        FileObject fo = (FileObject) o;
        JavaSource js = JavaSource.forFileObject(fo);

        //let's add some fields newField1 .. newField4        
        int i = 1;
        EnumSet<Modifier> set = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
        Common.addField(js, Common.getFieldName(i++), set, "boolean");

        set = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC);
        Common.addField(js, Common.getFieldName(i++), set, "int");

        set = EnumSet.of(Modifier.PROTECTED);
        Common.addField(js, Common.getFieldName(i++), set, "boolean");

        set = EnumSet.of(Modifier.SYNCHRONIZED, Modifier.PUBLIC);
        Common.addField(js, Common.getFieldName(i++), set, "float");

        set = EnumSet.of(Modifier.FINAL, Modifier.PUBLIC, Modifier.STATIC);
        Common.addField(js, Common.getFieldName(i++), set, "String");
        return true;
    }

    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceFieldElem";
        packageName = "org.netbeans.test.java.testsources";
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(FieldElem.class).enableModules(".*").clusters(".*"));
    }
}
