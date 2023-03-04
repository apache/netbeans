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
 *	TestBookXMLSchema - test the basic features.
 *
 *	The following test assumes that we know the content of the
 *	graph as we get elements, add and change them. Therefore, the TestBookXMLSchema.xml
 *	file and this java test should be kept in sync.
 *
 * 	Test the following:
 *
 *	single String: get/set/remove/set/get
 *	boolean (from true): get/set true/get/set false/get/set true/get
 *	boolean (from false): get/set false/get/set true/get/set false/get
 *	String[]: get/set (null & !null)/add/remove
 *	Bean: remove/set(null)/create bean and graph of beans/set/add
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;
import book.*;


public class TestBookXMLSchema extends BaseTest {
    public static void main(String[] argv) {
        TestBookXMLSchema o = new TestBookXMLSchema();
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
	throws Exception {
	Book book;
	
	this.readDocument();

	out("creating the bean graph");
	book = Book.createGraph(doc);
	
	//	Check that we can read the graph an it is complete
	out("bean graph created");
	
	//out(book.toString());
	//out(book.clone().toString());
	//((BaseBean)book.clone()).write(System.out);
	
	//
	//	Single string test
	//
	
	String s1, s2;
	
	//	Get a String element and change it
	{
	    setTest("single String - get/set");
	    s1 = book.getSummary();
	    s1 += "Some more dynamic notes on the summary.";
	    book.setSummary(s1);
	    s2 = book.getSummary();
	    check(s1.equals(s2));
	}
	
	//	Remove a final string element
	{
	    setTest("single String - remove");
	    book.setSummary(null);
	    s2 = book.getSummary();
	    check(s2==null);
	    out("should not have a 'summary' element:",
	    book.dumpDomNode("summary", 1));
	}
	
	//	Adding a final string
	{
	    setTest("single String - set new");
	    book.setSummary(s1);
	    s2 = book.getSummary();
	    check(s1.equals(s2));
	    out("should have a 'summary' element:",
	    book.dumpDomNode("summary", 1));
	}
	
	//
	//	Boolean test - from initial value true
	//
	
	{
	    boolean b1, b2;
	    setTest("boolean - get value (from true)");
	    b1 = book.isGood();
	    check(b1==true);
	    out("should have a 'good' element:",
	    book.dumpDomNode("good", 1));
	    
	    setTest("boolean - change to same value (true)");
	    book.setGood(b1);
	    check(b1==book.isGood());
	    out("should still have a 'good' element:",
	    book.dumpDomNode("good", 1));
	    
	    setTest("boolean - change to false");
	    book.setGood(!b1);
	    check(b1!=book.isGood());
	    out("should have a 'good' element:",
	    book.dumpDomNode("good", 1));
	    
	    setTest("boolean - change back to true");
	    book.setGood(b1);
	    check(b1==book.isGood());
	    out("should have a 'good' element:",
	    book.dumpDomNode("good", 1));
	}
	
	//
	//	Boolean test - from initial value false
	//
	{
	    boolean b1, b2;
	    setTest("boolean - get value (from false)");
	    b1 = book.isAvailable();
	    check(b1==false);
	    out("should not have an 'available' element:",
	    book.dumpDomNode("available", 1));
	    
	    setTest("boolean - change to same value (false)");
	    book.setAvailable(b1);
	    check(b1==book.isAvailable());
	    out("should have an 'available' element:",
	    book.dumpDomNode("available", 1));
	    
	    setTest("boolean - change to true");
	    book.setAvailable(!b1);
	    check(b1!=book.isAvailable());
	    out("should have now an 'available' element:",
	    book.dumpDomNode("available", 1));
	    
	    setTest("boolean - change back to false");
	    book.setAvailable(b1);
	    check(b1==book.isAvailable());
	    out("should have an 'available' element:",
	    book.dumpDomNode("available", 1));
	}
	
	
	//
	//	Array of string test
	//
	//	The tests are performed on the Paragraph string array,
	//	member of the Chapter bean.
	//	The book contains three chapter. There is one chapter
	//	with no paragraph, one with one paragraph and one with
	//	two paragraph. This convers the three cases: empty array,
	//	array with only one element, and array with more than one
	//	element.
	//
	
	{
	    out("String[] - search for the string arrays");
	    out("found " + book.sizeChapter() + " chapter in book");
	    for (int i=0; i<book.sizeChapter(); i++) {
		Chapter c = book.getChapter(i);
		out("chapter " + (i+1) + " has " + c.sizeParagraph() +
		" paragraphs:",	c.dumpDomNode("paragraph", 1));
	    }
	}
	
	//	Test array size = 0
	{
	    Chapter c = null;
	    
	    setTest("String[0] - search for empty array");
	    for (int i=0; i<book.sizeChapter(); i++) {
		Chapter c2 = book.getChapter(i);
		if (c2.sizeParagraph() == 0) {
		    c = c2;
		    break;
		}
	    }
	    check(c != null);
	    
	    int tmp[] = {-1, 0, 1, 999};
	    
	    //	Check for out of bound exception
	    for (int i=0; i<tmp.length; i++) {
		setTest("String[0] - out of bounds access [" + tmp[i] +
		"]");
		s1 = "This is a new paragraph";
		try {
		    c.setParagraph(tmp[i], s1);
		    check(false, "didn't get an exception");
		}
		catch(IndexOutOfBoundsException e) {
		    check(true, "(IndexOutOfBoundsException)");
		}
		catch(Exception ee) {
		    check(false, ee.getMessage());
		}
	    }
	    
	    //	Adding an element
	    setTest("String[0] - add a string to the array");
	    c.addParagraph(s1);
	    if (c.sizeParagraph() != 1)
		err("array size != 1");
	    s2 = c.getParagraph(0);
	    check(s1.equals(s2));
	    out("should contain one paragraph:",
	    c.dumpDomNode("paragraph", 1));
	    
	    setTest("String[0] - add another string to the array");
	    c.addParagraph(s1);
	    if (c.sizeParagraph() != 2)
		err("array size != 2");
	    s2 = c.getParagraph(1);
	    check(s1.equals(s2));
	    out("should contain two paragraphs:",
	    c.dumpDomNode("paragraph", 1));
	    
	    setTest("String[0] - compare paragraphs");
	    s1 = c.getParagraph(0);
	    s2 = c.getParagraph(1);
	    check(s1.equals(s2), "(same content)");
	    check(s1 != s2, "(different instances)");
	    
	    setTest("String[0] - remove element");
	    c.setParagraph(0, null);
	    check(c.sizeParagraph() == 2);
	    out("should contain one paragraph:",
	    c.dumpDomNode("paragraph", 1));
	    
	    setTest("String[0] - access elements");
	    s2 = c.getParagraph(0);
	    check(s2 == null, "(first, should be null)");
	    s2 = c.getParagraph(1);
	    check(s2 != null, "(second, should be !null)");
	    
	    s1 = "This is another new paragraph";
	    setTest("String[0] - set again first element");
	    c.setParagraph(0, s1);
	    check(c.sizeParagraph() == 2);
	    out("should contain two paragraphs:",
	    c.dumpDomNode("paragraph", 1));
	    
	    setTest("String[0] - removing all elements");
	    s1 = c.getParagraph(1);
	    int i1 = c.removeParagraph(s1);
	    check(i1 == 1, "(removed correct index)");
	    check(c.sizeParagraph() == 1, "(size is now 1)");
	    s1 = c.getParagraph(0);
	    int i2 = c.removeParagraph(s1);
	    check(i2 == 0, "(removed correct index)");
	    check(c.sizeParagraph() == 0, "(size is now 0)");
	    out("should contain no paragraph:",
	    c.dumpDomNode("paragraph", 1));
	    
	}
	
	//	Test array size = 1
	{
	    Chapter c = null;
	    s1 = "This is again another new paragraph";
	    
	    setTest("String[0] - search for array of size 1");
	    for (int i=0; i<book.sizeChapter(); i++) {
		Chapter c2 = book.getChapter(i);
		if (c2.sizeParagraph() == 1) {
		    c = c2;
		    break;
		}
	    }
	    check(c != null);
	    
	    int tmp[] = {-1, 1, 999};
	    
	    //	Check for out of bound exception
	    for (int i=0; i<tmp.length; i++) {
		setTest("String[0] - out of bounds access [" + tmp[i] +
		"]");
		try {
		    c.setParagraph(tmp[i], s1);
		    check(false, "didn't get an exception");
		}
		catch(IndexOutOfBoundsException e) {
		    check(true, "(IndexOutOfBoundsException)");
		}
		catch(Exception ee) {
		    check(false, ee.getMessage());
		}
	    }
	    
	    //	Adding an element
	    setTest("String[0] - add a string to the array");
	    c.addParagraph(s1);
	    if (c.sizeParagraph() != 2)
		err("array size != 2");
	    s2 = c.getParagraph(1);
	    check(s1.equals(s2));
	    out("should contain two paragraphs:",
	    c.dumpDomNode("paragraph", 1));
	    
	    setTest("String[0] - compare paragraphs");
	    s1 = c.getParagraph(0);
	    s2 = c.getParagraph(1);
	    check(!s1.equals(s2), "(different content)");
	    
	    setTest("String[0] - add more paragraphs");
	    s1 = "I always wanted to be a kind of a big paragraph " +
	    " as I always dreamed to fill an XML paragraph element " +
	    " only to make sure it could be large enough stuffed with " +
	    " useless characters.";
	    int size = 100;
	    for (int i=0; i<size; i++)
		c.addParagraph(s1);
	    check(c.sizeParagraph() == size+2 ,
	    "(" + size + " new paragraphs)");
	    
	    setTest("String[0] - removing paragraphs (leaving 10)");
	    for (int i=0; i<size-8; i++)
		c.removeParagraph(s1);
	    check(c.sizeParagraph() == 10);
	    out("should contain 10 paragraphs:",
	    c.dumpDomNode("paragraph", 1));
	}
	
	
	//
	//	Bean elements test
	//
	//	The following tests that we can create a brand new bean,
	//	populate it with other beans an insert it into an existing
	//	bean graph.
	//
	//	The bean elements test are using the Index and Ref beans
	//	of the Book bean graph:
	//
	//	Book
	// 	  Index[1,n]
	// 	         Word - String
	//	         Ref[1,n]
	// 	                Page - String
	// 	                Line - String
	//		...
	//
	//
	{
	    Index idx;
	    
	    int size = book.sizeIndex();
	    out("book has " + size + " Index beans",
	    book.dumpDomNode("index", 1));
	    
	    setTest("remove a bean w/ remove()");
	    idx = book.getIndex(0);
	    int i1 = book.removeIndex(idx);
	    check(i1==0, "(correct element removed)");
	    check(book.sizeIndex()==size-1, "(correct Index array size)");
	    out("book should have " + (size - 1) + " Index beans",
	    book.dumpDomNode("index", 1));
	    
	    setTest("remove another bean w/ set(null)");
	    idx = book.getIndex(0);
	    book.setIndex(0, null);
	    check(book.sizeIndex()==size-1, "(correct Index array size)");
	    out("book should have " + (size - 2) + " Index beans",
	    book.dumpDomNode("index", 1));
	    
	    setTest("add an empty bean to the graph");
	    idx = new Index();
	    book.addIndex(idx);
	    check(book.sizeIndex()==size, "(correct Index array size)");
	    out("book should have " + (size - 1) + " Index beans",
	    book.dumpDomNode("index", 1));
	    
	    setTest("add a subtree bean");
	    Ref r = new Ref();
	    r.setPage(122);
	    r.setLine(32);
	    idx = new Index();
	    idx.addRef(r);
	    r = new Ref();
	    r.setPage(1);
	    r.setLine(3);
	    idx.addRef(r);
	    idx.setWord("who");
	    book.setIndex(0, idx);
	    out("book should have " + size + " Index beans",
	    book.dumpDomNode("index", 1));
	    out("idx should have 2 refs",
	    idx.dumpDomNode(3));
	    check(book.sizeIndex() == size);
	    
	    setTest("add another subtree bean");
	    r = new Ref();
	    r.setPage(22);
	    r.setLine(2);
	    idx = new Index();
	    idx.setWord("what");
	    idx.addRef(r);
	    book.addIndex(idx);
	    out("book should have " + (size+1) + " Index beans",
	    book.dumpDomNode("index", 1));
	    out("idx should have 1 ref",
	    idx.dumpDomNode(3));
	    check(book.sizeIndex() == size+1);
	    
	    setTest("should failed adding the same instance subtree");
	    try {
		book.addIndex(idx);
		check(false, "didn't get an exception");
	    }
	    catch(IllegalArgumentException e) {
		check(true, "\n >> caught: " + e);
	    }
	    catch(Exception ee) {
            ee.printStackTrace();
		check(false, "\n >> got wrong type of exception: " + ee);
	    }
	    
	    setTest("add the same cloned tree");
	    int i2 = book.addIndex((Index)book.getIndex(0).clone());
	    out("book should have " + (i2+1) + " Index beans",
	    book.dumpDomNode("index", 1));
	    
	    out("Initial Index is:",
	    book.getIndex(0).dumpDomNode(3));
	    out("New Index should be identical:",
	    book.getIndex(i2).dumpDomNode(3));
	    
	    check(book.sizeIndex() == i2+1);
	}
	
	//
	//	Bean elements test 2
	//
	//	The following tests that we can create, add, remove beans
	//	and values of a graph that is not attached to a DOM tree.
	//
	{
	    Index idx = new Index();
	    idx.setWord("oops");
	    setTest("add beans");
	    Ref r = new Ref();
	    r.setPage(999);
	    r.setLine(9);
	    idx.addRef(r);
	    r = new Ref();
	    r.setPage(888);
	    r.setLine(8);
	    idx.addRef(r);
	    check(idx.sizeRef()==2, "(correct size of array)");
	    out("should have nothing to print: ", idx.dumpDomNode(1));
	    setTest("remove an element");
	    idx.removeRef(r);
	    check(idx.sizeRef()==1, "(correct size of array)");
	    setTest("remove last element");
	    idx.setRef(0, null);
	    check(idx.sizeRef()==1, "(correct size)");
	    r = new Ref();
	    r.setPage(77);
	    r.setLine(7);
	    idx.setRef(0, r);
	    out("add an element and test getValues:", idx.dumpBeanNode());
	}
	
	//
	//	The following test creates a brand new bean graph and therefore
	//	a new DOM graph.
	//
	setTest("creating the root for a brand new graph");
	book = Book.createGraph();
	check(book != null);
	
	setTest("populating the graph");
	book.setGood(false);
	book.setAvailable(true);
	book.setSummary("This book is about avoiding summaries at the end of books");
	Chapter c = new Chapter();
	c.setComment("What's a good summary in chapter 1");
    c.setComment2("Additional comment");
	c.addParagraph("This is the first paragraph");
	c.addParagraph("This is a second paragraph");
	book.addChapter(c);
	
	c = new Chapter();
	book.addChapter(c);
	c.setComment("Yet another comment for chapter 2");
	c.addParagraph("only one paragraph in this second chapter");
	
	c = new Chapter();
	book.addChapter(c);
	
	Index i = new Index();
	i.setWord("summary");
	
	Ref r = new Ref();
	r.setPage(22);
	r.setLine(12);
	i.addRef(r);
	
	r = new Ref();
	i.addRef(r);
	r.setPage(4);
	r.setLine(5);
	
	book.addIndex(i);
	
	check(true);
	
	out("print the new graph DOM nodes:",
	book.dumpDomNode(9));
	
	out("Re-read the original XML file for array getter/setter testing");
	this.readDocument();
	book = Book.createGraph(doc);
	out("bean graph created");
	
	{
	    setTest("check Index[] getter() method");
	    Index []a1 = book.getIndex();
	    check(a1.length == book.sizeIndex(), "(correct array size)");
	    check(a1[0] == book.getIndex(0), "(same first element)");
	    check(a1[1] == book.getIndex(1), "(same second element)");
	    check(a1[0] != book.getIndex(1), "([0] != [1])");
	    setTest("check Chapter[] getter() method");
	    Chapter []a2 = book.getChapter();
	    check(a2.length == book.sizeChapter(), "(correct array size)");
	    check(a2[0] == book.getChapter(0), "(same first element)");
	    check(a2[1] == book.getChapter(1), "(same second element)");
	    check(a2[2] == book.getChapter(2), "(same third element)");
	    check(a2[0] != book.getChapter(2), "([0] != [2])");
	    setTest("check Chapter.Paragraph getter() method");
	    String []p = a2[0].getParagraph();
	    check(p.length == book.getChapter(0).sizeParagraph(),
	    "(correct array size)");
	    p = a2[1].getParagraph();
	    check(p.length == book.getChapter(1).sizeParagraph(),
	    "(correct array size)");
	    p = a2[2].getParagraph();
	    check(p.length == book.getChapter(2).sizeParagraph(),
	    "(correct array size)");
	    setTest("change the Chapter[] order");
	    int []s = new int[3];
	    s[0] = a2[0].sizeParagraph();
	    s[1] = a2[1].sizeParagraph();
	    s[2] = a2[2].sizeParagraph();
	    Chapter []a3 = new Chapter[3];
	    a3[2] = (Chapter)a2[0];
	    a3[1] = (Chapter)a2[1];
	    a3[0] = (Chapter)a2[2];
	    book.setChapter(a3);
	    check(book.getChapter(2).sizeParagraph()==s[0],
	    "(reversed first element)");
	    check(book.getChapter(1).sizeParagraph()==s[1],
	    "(same second element)");
	    check(book.getChapter(0).sizeParagraph()==s[2],
	    "(reversed third element)");

        setTest("Chapter going in and out of Book");
        out("Make sure the contents of a Chapter object remain intact after removing that Chapter from the Book");
        Chapter undecidedChapter = new Chapter();
        undecidedChapter.setComment("This chapter may not make it into the book.");
        out(undecidedChapter.getComment());
        book.addChapter(undecidedChapter);
        out(book.sizeChapter());
        check(book.getChapter(book.sizeChapter()-1) == undecidedChapter, "undecided chapter is in there");
        book.removeChapter(undecidedChapter);
        out("make sure undecidedChapter is intact");
        out(undecidedChapter.getComment());
        out("make sure book is intact");
        out(book.sizeChapter());
        out("Put the chapter back in.");
        book.addChapter(undecidedChapter);
        out(book);
	}
	
	//	Read again the document and play with the choice properties
	this.readDocument();
	out("creating the bean graph");
	book = Book.createGraph(doc);
	
	setTest("check if the root has any choice prop");
	Iterator it = book.listChoiceProperties();
	check(!it.hasNext(), "(none found)");
	check(book.listChoiceProperties("Chapter") == null, "(none found on Chapter prop)");
	check(book.listChoiceProperties("index") == null, "(none found on index prop)");
	setTest("test extra prop");
	
	BaseProperty[] bps;
	Extra e = book.getExtra();
	it = e.listChoiceProperties();
	check(it.hasNext(), "(at least one)");
	//#90905: the iterator returns the elements in different order 
        // for jdk5 and 6, so we need to put them to a list to maintain the order.
        List<BaseProperty[]> propertiesList = new ArrayList<BaseProperty[]>();
        while (it.hasNext()) {
	    propertiesList.add((BaseProperty[])it.next());
	}
        Collections.sort(propertiesList, new Comparator<BaseProperty[]>(){ 
            public int compare(BaseProperty[] o1, BaseProperty []o2) {
                return o1[0].getName().compareTo(o2[0].getName());
            }});
            
        for (BaseProperty[] each : propertiesList){
            out("Getting a set of choice properties:");
	    this.printChoiceProperties(each);
        }
	
	out("Getting the same list 3 times:");
	bps = e.listChoiceProperties("Size");
	this.printChoiceProperties(bps);
	bps = e.listChoiceProperties("size-cm");
	this.printChoiceProperties(bps);
	bps = e.listChoiceProperties("size-inches");
	this.printChoiceProperties(bps);
	
	out("Getting twice the same list:");
	bps = e.listChoiceProperties("WeightKg");
	this.printChoiceProperties(bps);
	bps = e.listChoiceProperties("weight-lb");
	this.printChoiceProperties(bps);
	
	check(!e.isChoiceProperty("color"), " color not choice prop");
	
	//
	//	This tests the DDParser class
	//
	//this.out(book.dumpBeanNode());
	
	this.parse(book, "/Book/Good");
	this.parse(book, "/Good");
	this.parse(book, "Summary/");
	book.setSummary(null);
	this.parse(book, "Summary");
	this.parse(book, "Available");
	this.parse(book, "Extra");
	this.parse(book, "Chapter");
	this.parse(book, "Chapter/Comment");
	this.parse(book, "Chapter/Paragraph");
	this.parse(book, "index/ref/line");
	
	out(book.dumpBeanNode());
	this.parse(book, "/Book/Index.Word=E-Tools/Ref/Page");
	this.parse(book, "/Book/Index.Word=E-Tool/Ref/Page");
	this.parse(book, "/Book/Index.Word=E-Tool/Ref.Line=15/Page");
	this.parse(book, "/Book/Index.Word=E-Tool/Ref.Line=15/Page=22");
	this.parse(book, "/Book/Index.Word=E-Tool/Ref.Line=15/Page=5");
	
	book = new Book();
	out(book.dumpBeanNode());
	this.parse(book, "/Book/Index.Word=E-Tools/Ref.Line=15/Page!");
	out(book.dumpBeanNode());
	this.parse(book, "/Book/Chapter.Comment=My Comment");
	out(book.dumpBeanNode());
	this.parse(book, "/Book/Chapter.Comment=My Comment!");
	out(book.dumpBeanNode());
	this.parse(book, "/Book/Index.Word=the word/Ref.Line=10/Page=20!");
	out(book.dumpBeanNode());
	
	//  Make sure that we can merge (this test the case of merge
	//  without attribute which is covered by TestMerge)
	setTest("simple merge");
	this.readDocument();
	out("creating the bean graph");
	book = Book.createGraph(doc);
	Book b2 = (Book)book.clone();
	s1 = "Some more dynamic notes on the summary.";
	b2.setSummary(s1);
	b2.getChapter(0).setComment("New Comment");
	book.merge(b2);
	check(book.isEqualTo(b2), "identical graphs");

	//  Merge two elements that are not part of the graph
	Chapter c1 = new Chapter();
	Chapter c2 = new Chapter();
	c1.merge(c2);

    }
    
    
    void parse(BaseBean bean, String parse) {
	out("Parsing " + parse);
	DDParser p = new DDParser(bean, parse);
	while (p.hasNext()) {
	    Object o = p.next();
	    if (o != null) {
		if (o instanceof BaseBean)
		    this.out(((BaseBean)o).dumpBeanNode());
		else
		    this.out(o.toString());
	    }
	    else
		this.out("null");
	}
    }
    
    void printChoiceProperties(BaseProperty[] bps) {
	if (bps == null)
	    err("got null instead a BaseProperty[] instance");
	else {
	    for (int l=0; l<bps.length; l++)
		check(bps[l].isChoiceProperty(), bps[l].getDtdName());
	}
    }
}

