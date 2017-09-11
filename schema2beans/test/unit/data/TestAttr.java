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
 *	TestAttr - test the attribute features
 *
 *	TestAttr.dtd and TestAttr.xml has to be kept in sync with this test.
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import book.*;


public class TestAttr extends BaseTest
{
    public static void main(String[] argv) {
        BaseTest o = new TestAttr();
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
		boolean			mute;
		boolean			remove;
		
		public MyListener(BaseBean bean)
		{
			this.listenerName = bean.name();
			gm = bean.graphManager();
			this.remove = false;
			this.mute = false;
			out("new listener for " + this.listenerName);
		}

		public void reset()
		{
			this.oldValue = null;
			this.newValue = null;
			this.propertyName = null;
			this.source = null;
		}
		
		public void propertyChange(PropertyChangeEvent e)
		{
			if (this.mute)
				return;
			
			String k;
			this.oldValue = e.getOldValue();
			this.newValue = e.getNewValue();
			this.propertyName = e.getPropertyName();
			this.source = e.getSource();
			
			String 	n = this.propertyName;
			int 	i = gm.getPropertyIndex(n);
			String 	pn;
			
			if (gm.isAttribute(n))
			{
				pn = "Attr:" + gm.getPropertyName(n);
				if (i != -1)
					pn += "[" + i + "]";
				pn += "." + gm.getAttributeName(n);
			}
			else
			{
				pn = "Prop:" + gm.getPropertyName(n);
				if (i != -1)
					pn += "[" + i + "]";
			}
				
			if (this.newValue == null)
				k = "Rmv";
			else
			if (this.oldValue == null)
				k = "Add";
			else
				k = "Chg";
			
			out("<" + k + " Lnr:" + this.listenerName + " Evt:" + n + 
				" Src:" + this.source.getClass().getName() + ">");
			
			if (remove)
			{
				out("<" +  pn + " - ParentName: " +
					gm.getPropertyParentName(n) + ">");
			}
			else
			{
				BaseBean bb = gm.getPropertyParent(n);
				String nm = "<no class>";
				
				if (bb != null)
					nm = bb.getClass().getName();

				out("<" + pn + " - ParentName: " +
					gm.getPropertyParentName(n) +
					" - ParentClass:" + nm + ">");
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

		public void mute(boolean mute)
		{
			this.mute = mute;
		}
		
		public String toString()
		{
			return this.name() + " raised from source " +
				this.source.getClass().getName();
		}
	}

	private MyListener l1;

	public void run()
		throws Exception
	{
		Book book;

		this.readDocument();
		out("creating the bean graph");
		//out(DDFactory.XmlToString(doc));
		book = Book.createGraph(doc);
		out("bean graph created");

		l1 = new MyListener(book);
		book.addPropertyChangeListener(l1);
		l1.mute(true);
				
		//out(book.dumpBeanNode());
		//out(((BaseBean)book.clone()).dumpBeanNode());
		//((BaseBean)book.clone()).write(System.out);
		
		//	Get/Change an enum attribute on the root
		{
			String s1, s2;
			
			setTest("get enum attribute from root");
			s1 = book.getAttributeValue("Good");
			check(s1.equals("no"));
			out("Changing to another value - should get an event");
			s2 = "yes";
			l1.mute(false);
			book.setAttributeValue("good", s2);
			l1.mute(false);
			s1 = book.getAttributeValue("good");
			check(s1.equals(s2));
			out("Book DOM content should be yes", book.dumpDomNode(0));
			out("Changing to a non-enum value (should get an exception)");
			boolean gotException = false;
			try
			{
				book.setAttributeValue("good", "maybe");
			}
			catch(IllegalArgumentException e)
			{
				check(true, "got the proper exception");
				gotException = true;
			}
			catch(Exception e)
			{
				check(false, "got the wrong exception type: " +
					  e.getClass().getName() + ", it should be " +
					  "IllegalArgumentException");
				gotException = true;
			}
			if (!gotException)
				check(false, "didnt' get any exception");
		}

		//	Get/Change attributes on a non-root element
		{
			String s1, s2;
			
			setTest("get #REQUIRED attribute");
			s1 = book.getAttributeValue("Summary", "length");
			check(s1.equals("132"));
			out("Changing to another value");
			s2 = "133";
			book.setAttributeValue("Summary", "length", s2);
			s1 = book.getAttributeValue("Summary", "length");
			check(s1.equals(s2));

			setTest("get #IMPLIED attribute");
			s1 = book.getAttributeValue("Summary", "lang");
			check(s1.equals("en"));
			out("Setting a new value");
			s2 = "fr";
			book.setAttributeValue("Summary", "lang", s2);
			s1 = book.getAttributeValue("Summary", "lang");
			check(s1.equals(s2));
			s2 = "";
			book.setAttributeValue("Summary", "lang", s2);
			s1 = book.getAttributeValue("Summary", "lang");
			check(s1.equals(s2));
			book.setAttributeValue("Summary", "lang", null);
			s1 = book.getAttributeValue("Summary", "lang");
			check(s1 == null);
			s2 = "fr";
			book.setAttributeValue("Summary", "lang", s2);

			
			setTest("get #FIXED attribute");
			s1 = book.getAttributeValue("Summary", "size");
			check(s1.equals("12"));
			out("Summary DOM content should be 133/fr/12",
				book.dumpDomNode(1));
			out("Setting a new value (should get an exception)");
			s2 = "15";
			boolean gotException = false;
			try
			{
				book.setAttributeValue("Summary", "size", s2);
			}
			catch(IllegalStateException e)
			{
				check(true, "got the proper exception");
				gotException = true;
			}
			catch(Exception e)
			{
				check(false, "got the wrong exception type: " +
					  e.getClass().getName() + ", it should be " +
					  "IllegalStateException");
				gotException = true;
			}
			if (!gotException)
				check(false, "didnt' get any exception");
		}

		//	Set from non defined
		{
			String s1, s2, s3, s4;
			
			setTest("get/set non set #IMPLIED attribute");
			Chapter c = book.getChapter(0);
			out("Chapter DOM should have no attribute",
				c.dumpDomNode(0));
			s1 = c.getAttributeValue("title");
			check(s1 == null);
			s2 = "My chapter";
			c.setAttributeValue("title", s2);
			s1 = c.getAttributeValue("title");
			check(s1.equals(s2));
			out("Chapter DOM should have one title attribute",
				c.dumpDomNode(0));

			//	Check that we access the same from the bean itself
			//	and from the parent that contains the attribute.
			setTest("access from parent & current bean");
			s1 = book.getAttributeValue("Chapter", 0, "title");
			s2 = c.getAttributeValue("title");
			check(s1.equals(s2));			

			//	Mix the elements, the attributes should follow
			setTest("attribute stick with elt when mixing");
			s1 = book.getAttributeValue("Chapter", 0, "title");
			s2 = book.getAttributeValue("Chapter", 1, "title");
			check(s1 != null);
			check(s2 == null);
			Chapter[] ac = book.getChapter();
			c = ac[1];
			ac[1] = ac[0];
			ac[0] = c;
			book.setChapter(ac);
			// Attribute of 0 should be what 1 was, and 1 what 0 was
			s3 = book.getAttributeValue("Chapter", 0, "title");
			s4 = book.getAttributeValue("Chapter", 1, "title");
			check(s3 == null);
			check(s4.equals(s1));
			
			setTest("get/set non set #IMPLIED attribute (idx != 0)");
			c = book.getChapter(2);
			out("Chapter DOM should have no attribute",
				c.dumpDomNode(0));
			s1 = c.getAttributeValue("title");
			check(s1 == null);
			s2 = "My chapter2";
			c.setAttributeValue("title", s2);
			s1 = c.getAttributeValue("title");
			check(s1.equals(s2));
			out("Chapter DOM should have one title attribute",
				c.dumpDomNode(0));
		}

		//	Test unknown attribute
		{
			String s1;
			
			setTest("get unknown attribute");
			boolean gotException = false;
			try
			{
				s1 = book.getAttributeValue("Summary", "Splash");
			}
			catch(IllegalArgumentException e)
			{
				check(true, "got the proper exception");
				gotException = true;
			}
			catch(Exception e)
			{
				check(false, "got the wrong exception type: " +
					  e.getClass().getName() + ", it should be " +
					  "IllegalArgumentException");
				gotException = true;
			}
			if (!gotException)
				check(false, "didnt' get any exception");
		}

		//	Add a brand new element with attributes
		{
			l1.mute(true);
                        String s1, s2;
			setTest("add a brand new element with default attributes");
			Index idx = new Index();
			int i = book.addIndex(idx);
			s1 = idx.getAttributeValue("cross-ref");
			s2 = book.getAttributeValue("Index", i, "CrossRef");
			check(s1.equals(s2));
			out("should have created: cross-ref & glossary, and not color",
				book.dumpDomNode(1));

			//	Add a brand new element, setting attributes
			setTest("add a brand new element, setting attributes");
			idx = new Index();
			idx.setAttributeValue(Index.CROSSREF, "yes");
			idx.setAttributeValue("color", "blue");
			idx.setWord("my word");
			idx.setAttributeValue("word", "freq", "123");
			book.setIndex(i, idx);
			out("should have created: cross-ref (yes), glossary (nope) " +
				"and color (blue)", book.dumpDomNode(3));
		}

		//	Dynamic parsing of the graph of beans
		{
			BaseBean root = book.graphManager().getBeanRoot();
			this.parseGraph(root, "\t");
		}

        out("Make sure that default attributes get set.");
        Book anotherBook = Book.createGraph();
        anotherBook.setSummary("This is my summary.");
        out(anotherBook);

        setTest("attributes as properties");
        book.setGood("no");
        check("no".equals(book.getGood()));
        out("Checking chapter title");
        Chapter chap0 = book.getChapter(0);
        chap0.setTitle("My title");
        out(chap0.getTitle());

		/*setTest("get non set attribute");
		book.setAttributeValue("Summary", "lang", "");
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		book.write(bout);
		out("--------------------");
		out(bout.toString());

		book.setAttributeValue("Summary", "lang", null);
		String s1 = book.getAttributeValue("Summary", "lang");
		check(s1 == null);
		bout = new ByteArrayOutputStream();
		book.write(bout);
		out("---- 222 ----------------");
		out(bout.toString());
		*/

        // Test cloned attributes
        Chapter tca = new Chapter();
        tca.setTitle("Dolly: A Good Clone & A Day");
        out("Title before cloning:");
        out(tca.getTitle());
        Chapter theClone = (Chapter) tca.clone();
        out("Title after cloning:");
        out(theClone.getTitle());
        Book fullGraph = Book.createGraph();
        fullGraph.addChapter(theClone);
        out("And here is the clone in it's own graph");
        out(fullGraph);
	}

	void parseGraph(BaseBean bean, String indent)
	{
		if (bean == null)
			return;
		
		out(indent + "[" + bean.name() + "]"); 
		
		BaseProperty[] props = bean.listProperties();
		for (int i=0; i<props.length; i++)
		{
			BaseProperty p = props[i];
			String		 name = p.getName();

			//	Prop name & size
			String str = "<" + name;
			if (p.isIndexed())
				str += "[" + p.size() + "]";
			str += "> - " + p.getPropertyClass();
			out(indent + str);

			//	Prop attributes
			String[] attrs = p.getAttributeNames();
			for (int j=0; j<attrs.length; j++)
				out(indent + " a:" + attrs[j]);

			//	recurse
			if (p.isBean() && p.isIndexed())
			{
				BaseBean[] ba = (BaseBean[])bean.getValues(name);
				for (int k=0; k<ba.length; k++)
					this.parseGraph(ba[k], indent + "\t");
			}
			else
			if (p.isBean())
			{
				BaseBean b = (BaseBean)bean.getValue(name);
				this.parseGraph(b, indent + "\t");				
			}
		}
	}
}



