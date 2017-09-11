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


