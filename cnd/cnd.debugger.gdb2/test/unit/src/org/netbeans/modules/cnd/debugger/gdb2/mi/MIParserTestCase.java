/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 */
public class MIParserTestCase extends TestCase {

    public MIParserTestCase() {
    }

    @Test
    public void testFullnameOctal() {
        String testLine = "E:\\\\test\\\\\\314\\356\\350";
        MIParser parser = new MIParser("Cp1251");
        parser.setup("*stopped,fullname=\"" + testLine + "\"");
        MIRecord result = parser.parse();
        assertEquals("E:\\\\test\\\\Мои", result.results().getConstValue("fullname"));
    }
    
    @Test
    public void testFullnameOctal2() {
        String testLine = "E:\\\\test\\\\Not \\314\\356\\350";
        MIParser parser = new MIParser("Cp1251");
        parser.setup("*stopped,fullname=\"" + testLine + "\"");
        MIRecord result = parser.parse();
        assertEquals("E:\\\\test\\\\Not Мои", result.results().getConstValue("fullname"));
    }
    
    @Test
    public void testValueQuotes() {
        String testLine = "\\\"a\\\"";
        MIParser parser = new MIParser("Cp1251");
        parser.setup("*stopped,value=\"" + testLine + "\"");
        MIRecord result = parser.parse();
        assertEquals(testLine, result.results().getConstValue("value"));
    }
    
    @Test
    public void testCorruptedStopped() {
        String testLine = "*stopped,{name=\"var6\",value=\"false\",in_scope=\"true\",type_changed=\"false\",has_more=\"0\"}";
        MIParser parser = new MIParser("Cp1251");
        parser.setup(testLine);
        MIRecord result = parser.parse();
        result.results().valueOf("xxx");
    }
    
    @Test
    public void testMultipleLocationBreakpoint() {
        String testLine = "15^done,bkpt={number=\"2\",type=\"breakpoint\","
                + "disp=\"keep\",enabled=\"y\",addr=\"<MULTIPLE>\",times=\"0\","
                + "original-location=\"Customer::Customer\"},{number=\"2.1\","
                + "enabled=\"y\",addr=\"0x0000000000403efe\","
                + "func=\"Customer::Customer(Customer const&)\","
                + "file=\"customer.h\","
                + "fullname=\"/home/henk/tmp/Quote_2/customer.h\",line=\"38\"},"
                + "{number=\"2.2\",enabled=\"y\",addr=\"0x0000000000404707\","
                + "func=\"Customer::Customer(std::string, int)\","
                + "file=\"customer.cc\","
                + "fullname=\"/home/henk/tmp/Quote_2/customer.cc\",line=\"35\"}";
        MIParser parser = new MIParser("Cp1251");
        parser.setup(testLine);
        MIRecord result = parser.parse();
        MITList resultList = result.results();
        
        assertEquals(3, resultList.size());
        assertEquals(
                "bkpt={number=\"2\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\",addr=\"<MULTIPLE>\",times=\"0\",original-location=\"Customer::Customer\"}",
                resultList.get(0).toString()
        );
        assertEquals(
                "bkpt={number=\"2.1\",enabled=\"y\",addr=\"0x0000000000403efe\",func=\"Customer::Customer(Customer const&)\",file=\"customer.h\",fullname=\"/home/henk/tmp/Quote_2/customer.h\",line=\"38\"}",
                resultList.get(1).toString()
        );
        assertEquals(
                "bkpt={number=\"2.2\",enabled=\"y\",addr=\"0x0000000000404707\",func=\"Customer::Customer(std::string, int)\",file=\"customer.cc\",fullname=\"/home/henk/tmp/Quote_2/customer.cc\",line=\"35\"}",
                resultList.get(2).toString()
        );
    }
    
    @Test
    public void testMacOSBreakpoint() {
        String testLine = "12^done,bkpt={number=\"2\",type=\"breakpoint\","
                + "disp=\"del\",enabled=\"y\",addr=\"0x00001f0f\",func=\"main\","
                + "file=\"src/args.c\",line=\"39\","
                + "shlib=\"/Users/tester/NetBeansProjects/Arguments_3/dist/Debug/GNU-MacOSX/arguments_3\","
                + "times=\"0\"},"
                + "time={wallclock=\"0.00037\",user=\"0.00034\","
                + "system=\"0.00003\",start=\"1346852609.987786\","
                + "end=\"1346852609.988157\"}";
        MIParser parser = new MIParser("Cp1251");
        parser.setup(testLine);
        MIRecord result = parser.parse();
        MITList resultList = result.results();
        
        assertEquals(2, resultList.size());
        assertEquals(
            "bkpt={number=\"2\",type=\"breakpoint\",disp=\"del\",enabled=\"y\","
                + "addr=\"0x00001f0f\",func=\"main\",file=\"src/args.c\","
                + "line=\"39\","
                + "shlib=\"/Users/tester/NetBeansProjects/Arguments_3/dist/Debug/GNU-MacOSX/arguments_3\","
                + "times=\"0\"}",
            resultList.get(0).toString()
        );
        assertEquals(
            "time={wallclock=\"0.00037\",user=\"0.00034\",system=\"0.00003\","
                + "start=\"1346852609.987786\",end=\"1346852609.988157\"}",
            resultList.get(1).toString()
        );
    }
    
    public void testAsyncBreakpoint() {
        String testLine = "=breakpoint-created,bkpt={number=\"2\","
                + "type=\"breakpoint\",disp=\"keep\",enabled=\"y\","
                + "addr=\"0x0000000000403175\",func=\"main(int, char**)\","
                + "file=\"quote.cc\",fullname=\"/home/henk/tmp/Quote_1/quote.cc\","
                + "line=\"123\",times=\"0\",original-location=\"quote.cc:123\"}";
        MIParser parser = new MIParser("Cp1251");
        parser.setup(testLine);
        MIRecord result = parser.parse();
        MITList resultList = result.results();
        
        assertEquals(1, resultList.size());
        assertEquals(
                "{number=\"2\",type=\"breakpoint\",disp=\"keep\",enabled=\"y\","
                    + "addr=\"0x0000000000403175\",func=\"main(int, char**)\","
                    + "file=\"quote.cc\",fullname=\"/home/henk/tmp/Quote_1/quote.cc\","
                    + "line=\"123\",times=\"0\",original-location=\"quote.cc:123\"}",
                resultList.valueOf("bkpt").toString());
    }
}