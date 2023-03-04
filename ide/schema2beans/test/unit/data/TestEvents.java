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
 *	TestEvents - test the events
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestBook.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *		listener on the root - change event for add/change/remove on the same root bean
 *		listener on the root - change event for add/change/remove 
 *			on a sub node bean (propagation)
 *		listener on the root and on a subnode: test no reception, both reception and 
 *			only one reception
 *		listener on a specific property
 *		remove listeners
 *		indexed and non indexed property events
 *		check utility methods to get the index, name and parent bean 
 *			from an event name (graphManager methods)
 *		remove a subtree - get event within the subtree (no propagation)
 *			and within the original tree (propagation)
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import java.beans.*;

import org.netbeans.modules.schema2beans.*;
import book.*;


public class TestEvents extends BaseTest
{
    public static void main(String[] argv) {
        BaseTest o = new TestEvents();
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
    
	public class MyListener implements PropertyChangeListener
	{
		GraphManager	gm;
		String			listenerName;
		Object			oldValue;
		Object			newValue;
		String			propertyName;
		Object			source;
		int				index;

		boolean			remove;
		
		//	Used to check that the event is triggered once the changed are done
		Chapter			tracePara;
		
		public MyListener(BaseBean bean)
		{
			this.listenerName = bean.name();
			gm = bean.graphManager();
			this.remove = false;
			out("new listener for " + this.listenerName);
		}

		public void reset()
		{
			this.oldValue = null;
			this.newValue = null;
			this.propertyName = null;
			this.source = null;
			this.index = -1;
		}

		public void traceParagraphs(Chapter c)
		{
			this.tracePara = c;
		}

		
		public void propertyChange(PropertyChangeEvent e)
		{
			this.oldValue = e.getOldValue();
			this.newValue = e.getNewValue();
			this.propertyName = e.getPropertyName();
			this.source = e.getSource();
			String n = this.propertyName;
			this.index = gm.getPropertyIndex(n);

			out("<Lnr:" + this.listenerName + " Evt:" + n + 
				" Src:" + this.source.getClass().getName() + ">");
			if (remove)
			{
				out("<" + gm.getPropertyName(n) + "[" + this.index + 
					"]" + " - Parent: " + gm.getPropertyParentName(n) + ">");
			}
			else
			{
				out("<" + gm.getPropertyName(n) + "[" + this.index + 
					"]" + " - Parent: " + gm.getPropertyParentName(n) +
					"/" + gm.getPropertyParent(n).getClass().getName() + ">");
			}

			if (this.tracePara != null)
			{
				String[] p = this.tracePara.getParagraph();
				for (int i=0; i<p.length; i++)
					out("From event listener: " + p[i]);
			}
			//out("received " + this.oldValue + "/" + this.newValue + "/" +
			//	this.propertyName);
		}

		public void removeMode()
		{
			this.remove = true;
		}
		
		public Object oldValue()
		{
			return this.oldValue;
		}

		public String stringOldValue()
		{
			if (this.oldValue == null)
				return "<null>";
			else
				return this.oldValue.toString();
		}

		public Object newValue()
		{
			return this.newValue;
		}

		public String stringNewValue()
		{
			if (this.newValue == null)
				return "<null>";
			else
				return this.newValue.toString();
		}
		
		public String name()
		{
			return this.propertyName;
		}

		public String toString()
		{
			return this.name() + " raised from source " +
				this.source.getClass().getName();
		}
	}
	
	public void run()
		throws Exception
	{
		Book book;

		this.readDocument();
		
		out("creating the bean graph");
		book = Book.createGraph(this.doc);
		GraphManager gm = book.graphManager();

		//	Check that we can read the graph an it is complete
		out("bean graph created");
		//out(book.toString());

		/*
		 *	Book
		 *	  Index[1,n]
		 *	         Word - String
		 *	         Ref[1,n]
		 *	                Page - String
		 *	                Line - String
		 *	  Chapter[1,n]
		 *	         Comment? - String
		 *	         Paragraph[0,n] - String
		 *	  Summary? - String
		 *	  Author[1,n] - String
		 *	  Good - Boolean
		 *	  Available - Boolean
		 */

		//
		//	Set a change event on the root
		//
		MyListener l = new MyListener(book);
		
		book.addPropertyChangeListener(l);
		
		setTest("simple change event on the root");
		l.reset();
		String s = "This book is about how to raise the event familly";
		//	Change a property on the root - this should raises an event
		book.setSummary(s);
		//	Check the received event
		check(l.oldValue() == null, "(old value)");
		check(l.newValue().equals(s), "(new value)");

		setTest("change the same property on the root");
		l.reset();
		String s2 = "This book is about nothing at all";
		//	Change a property on the root - this should raises an event
		book.setSummary(s2);
		//	Check the received event
		check(l.oldValue().equals(s), "(old value)");
		check(l.newValue().equals(s2), "(new value)");

		setTest("remove this same property");
		l.reset();
		//	Change a property on the root - this should raises an event
		book.setSummary(null);
		//	Check the received event
		check(l.oldValue().equals(s2), "(old value)");
		check(l.newValue() == null, "(new value)");


		//
		//	Keep the same event on the root, but change a property of another
		//	property (not a direct property of the root)
		//
		setTest("propagation of the event");
		Chapter c = book.getChapter(0);
		l.reset();
		s = c.getComment();
		s2 = "Comment on the first chapter";
		c.setComment(s2);
		check(l.oldValue.equals(s), "(oldvalue)");
		check(l.newValue.equals(s2), "(newvalue)");

		//	Add three paragraphs

		setTest("event on indexed property - add new");
		l.reset();
		s = "This is a new paragraph";
		int i = c.addParagraph(s);
		check(l.oldValue == null, "(no old value - new element)");
		check(l.newValue.equals(s), "(new value)");
		check(l.index == i, "(correct index)");

		setTest("event on indexed property - add new");
		l.reset();
		s2 = "This is another paragraph";
		int j = c.addParagraph(s2);
		check(l.oldValue == null, "(no old value - new element)");
		check(l.newValue.equals(s2), "(new value)");
		check(l.index == j, "(correct index)");

		setTest("event on indexed property - add new");
		l.reset();
		s2 = "This is yet another paragraph";
		j = c.addParagraph(s2);
		check(l.oldValue == null, "(no old element - new element)");
		check(l.newValue.equals(s2), "(new value)");
		check(l.index == j, "(correct index)");

		//	Remove the first added
		setTest("event on indexed property - remove index");
		l.reset();
		c.setParagraph(i, null);
		check(l.oldValue.equals(s), "(old value)");
		check(l.newValue == null, "(no new value)");
		check(l.index == i, "(correct index)");

		setTest("event on indexed property - reset new");
		l.reset();
		c.setParagraph(i, s);
		check(l.oldValue == null, "(old value)");
		check(l.newValue.equals(s), "(no new value)");
		check(l.index == i, "(correct index)");

		//
		//	Add another listener on an intermediate node and on a leaf
		//
		
		Chapter c2 = book.getChapter(1);
		
		MyListener l2 = new MyListener(c2);
		
		//	Get the events only for Paragraph changes
		c2.addPropertyChangeListener("Paragraph", l2);

		//	Check that we receive the event twice
		setTest("two listeners - both receiving");
		l2.reset();
		l.reset();
		s = "This is a brand new one";
		c2.addParagraph(s);
		check(l.oldValue == null, "(no old value)");
		check(l.newValue.equals(s), "(new value)");
		check(l.oldValue == l2.oldValue, "(same old value - both listeners)");
		check(l.newValue.equals(l2.newValue), "(same new value - both listeners)");
		check(l.index == l2.index, "(same index - both listeners)");
		check(l.source == l2.source, "(same source - both listeners)");

		//	Check that modifying the comment won't notify the Paragraph listener
		setTest("two listeners - one receiving");
		l2.reset();
		l.reset();
		s = "That's a new comment value";
		c2.setComment(s);
		check(l.newValue.equals(s), "(root listener: yep)");
		check(l2.newValue == null, "(chapter listener: noop)");


		//	Remove the book listener
		book.removePropertyChangeListener(l);
		setTest("one listener - no receiving");
		l2.reset();
		l.reset();
		s = "That's another new comment value";
		c2.setComment(s);
		check(l.newValue == null, "(root listener: noop)");
		check(l2.newValue == null, "(chapter listener: noop)");

		setTest("one listener - one receiving");
		l2.reset();
		l.reset();
		s = "That's a brand new paragraph";
		c2.addParagraph(s);
		check(l.newValue == null, "(root listener: noop)");
		check(l2.newValue.equals(s), "(chapter listener: yep)");

		setTest("no listener - no receiving");
		c2.removePropertyChangeListener("Paragraph", l2);
		l2.reset();
		l.reset();
		s = "That's yet another brand new paragraph";
		c2.addParagraph(s);
		check(l.newValue == null, "(root listener: noop)");
		check(l2.newValue == null, "(chapter listener: noop)");
		
		//	Register again the listeners
		book.addPropertyChangeListener(l);
		c2.addPropertyChangeListener("Paragraph", l2);
		l.removeMode();
		l2.removeMode();
		l2.reset();
		l.reset();
		book.removeChapter(c2);
		out("should have received paragraph events on Chapter and Chapter event on Book");

		//
		//	Make sure that the event is triggered after the property
		//	has changed.
		//
		c = new Chapter();
		c.addParagraph("1. this is a paragraph");
		c.addParagraph("2. this is a paragraph");
		c.addParagraph("3. this is a paragraph");
		c.addParagraph("4. this is a paragraph");
		out("should receive Chapter event and get 4 strings from the event");
		l.traceParagraphs(c);
		book.addChapter(c);
		String[] pp = c.getParagraph();
		String[] pp2 = new String[3];
		pp2[0] = pp[0];
		pp2[1] = pp[3];
		pp2[2] = pp[2];
		out("should receive Chapter event and get 3 strings from the event (1, 4, 3)");
		c.setParagraph(pp2);
		l.traceParagraphs(null);
 	}

}



