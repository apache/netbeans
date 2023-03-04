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
 *	TestFind - test the find feature
 *
 *	Search property value, attribute value and any value.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;

import java.beans.*;

import book.*;


public class TestFind extends BaseTest
{
    public static void main(String[] argv) {
        BaseTest o = new TestFind();
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
		Book b1, b2, b3;

		this.readDocument();
		out("creating the bean graph");
		Book book = Book.createGraph(doc);

		//GraphManager.debug(true);
		//	Check that we can read the graph an it is complete
		out("bean graph created");

		//
		//	Find property value
		//
		setTest("Test findProperty -");
		String []r = book.findPropertyValue("Word", "Good book");
		check(r.length == 1, "found element");
		if (r.length > 0)
			out(r[0]);
		r = book.findPropertyValue("Line", "31");
		check(r.length == 2, "found elements");
		if (r.length > 1)
		{
			out(r[0]);
			out(r[1]);
		}
		
		r = book.findPropertyValue("Page", "1234");
		check(r.length == 0, "not found element");

		//
		//	Find property and/or attribute value
		//
		setTest("Test findValue -");
		r = book.findValue("31");
		check(r.length == 3, "found elements");
		if (r.length > 2)
		{
			out(r[0]);
			out(r[1]);
			out(r[2]);
		}

		r = book.findValue("E-Tool");
		check(r.length == 1, "found element");
		if (r.length > 0)
		{
			out(r[0]);
		}

		r = book.findValue("this is not in the XML document");
		check(r.length == 0, "not found element");

		r = book.findValue("yes");
		check(r.length == 2, "found elements");
		if (r.length > 1)
		{
			out(r[0]);
			out(r[1]);
		}

		r = book.findValue("no");
		check(r.length == 1, "found element");
		if (r.length > 0)
		{
			out(r[0]);
		}
		
		//
		//	Find attribute value
		//
		setTest("Test findAttributeValue -");
		r = book.findAttributeValue("Color", "red");
		check(r.length == 1, "found element");
		if (r.length > 0)
		{
			out(r[0]);
		}

		//	try with the dtd name (color instead of Color)
		r = book.findAttributeValue("color", "blue");
		check(r.length == 1, "found element");
		if (r.length > 0)
		{
			out(r[0]);
		}

		r = book.findAttributeValue("Color", "black");
		check(r.length == 0, "not found element");
		
		r = book.findAttributeValue("freq", "1");
		check(r.length == 2, "found elements");
		if (r.length > 1)
		{
			out(r[0]);
			out(r[1]);
		}
		
		r = book.findAttributeValue("Good", "no");
		check(r.length == 1, "found element");
		if (r.length > 0)
		{
			out(r[0]);
		}

		//
		//	Try to remove an element by value the by reference
		//
		
	}
}


