/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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


