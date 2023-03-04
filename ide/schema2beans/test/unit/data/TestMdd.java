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
 *	TestMdd - test the attribute features
 *
 *	TestMdd.dtd and TestMdd.xml has to be kept in sync with this test.
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import book.*;


public class TestMdd extends BaseTest
{
    public static void main(String[] argv) {
        BaseTest o = new TestMdd();
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
    
	public void run()
		throws Exception
	{
		Book book;

		this.readDocument();
		out("creating the bean graph");
		//out(DDFactory.XmlToString(doc));
		book = Book.createGraph(doc);
		out("bean graph created");
		
		//	Check Wrapper class and float scalar
		{
			setTest("get non scalar wrapper object");
			Object obj = book.getDate();
			check(obj instanceof book.MyDate);
			out(obj.toString());

			setTest("get float scalar value");
			float f = book.getPrice();
			float ref = (float)10.95;
			float f2 = (float)9.99;
			check(f == ref);
			out("Setting new price to " + f2);
			book.setPrice(f2);
			f = book.getPrice();
			check(f == f2);
			out("Price and date should match", book.dumpDomNode(2));
		}

		//	Check Character
		{
			setTest("get char scalar value");
			Index idx = book.getIndex(0);
			char c = idx.getAlpha();
			check(c == 'a');
			idx.setAlpha('x');
			check(idx.getAlpha() == 'x');
			out("Alpha should be 'x'", idx.dumpDomNode(2));
		}
		
		//	Check Integer as an indexed property
		{
			setTest("get int/int[] scalar values");
			Ref ref = book.getIndex(0).getRef(0);
			int[] i = ref.getLine();
			check(i.length == 3);
			check(ref.getPage() == 22);
			check(i[0] == 12);
			check(i[1] == 22);
			check(i[2] == 32);
			i[2] = 323;
			ref.setLine(i);
			check(ref.getPage() == 22);
			check(ref.getLine(0) == 12);
			check(ref.getLine(1) == 22);
			check(ref.getLine(2) == 323);
			out("Lines should be 12/22/323", ref.dumpDomNode(2));
		}
	}
}


