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
import java.io.*;
import book.*;

import org.netbeans.modules.schema2beans.*;


public class TestValid extends BaseTest
{
    public static void main(String[] argv) {
        BaseTest o = new TestValid();
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
        readDocument();
        
        //    Create the graph from the input stream 
        Book book = Book.createGraph(this.doc);

		out("Current XML document:");
		book.write(System.out);

		out("Should add paperback after chapter:");
		book.setPaperback(true);
		book.write(System.out);

		out("Should add summary after paperback:");
		book.setSummary("Summary of the book");
		book.write(System.out);

		out("Should add chapter before paperback");
		Chapter c = new Chapter();
		c.setTitle("title1");
		book.addChapter(c);
		c = new Chapter();
		c.setTitle("title2");
		book.addChapter(c);
		c = new Chapter();
		c.setTitle("title3");
		book.addChapter(c);
		c = new Chapter();
		c.setTitle("title4");
		book.addChapter(c);
		c = new Chapter();
		c.setTitle("title5");
		book.addChapter(c);

		//	Mix to index/Dom Node to test the property indexed case
		Chapter[] c2 = book.getChapter();
		c = c2[2];
		c2[2] = c2[0];
		c2[0] = c;
		c = c2[3];
		c2[3] = c2[1];
		c2[1] = c;
		book.setChapter(c2);
		
		out(book);

		out("Should add title as the first property of book:");
		book.setTitle("Title of the book");
		out(book);
		
		out("Should add price as the last property of book:");
		book.setPrice("19.99");
		out(book);
		
		out("Should add isbn before the price:");
		book.setIsbn("120394857");
		out(book);

		out("Should add ending at the end of the chapter:");
		c = book.getChapter(0);
		c.setEnding("And this is how this chapter ends.");
		out(book);
		
		out("Should add conclusion before the ending");
		c.setConclusion("And this concludes this chapter.");
		out(book);

		out("Should add Note with year before copyright");
		Note n = new Note();
		n.setYear("2000");
		n.setCopyright("1997");
		book.setNote(n);
		out(book);

		out("Should set Note with date before copyright");
		n = new Note();
		n.setDate("2001");
		n.setCopyright("1996");
		book.setNote(n);
		out(book);
	} 
}

