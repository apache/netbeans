/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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