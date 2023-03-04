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

