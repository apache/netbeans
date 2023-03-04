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
 *	TestMergeExtendBaseBean - test the basic features.
 *
 */

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import org.netbeans.modules.schema2beans.*;

import java.beans.*;

import book.*;


public class TestMergeExtendBaseBean extends BaseTest {
    public static void main(String[] argv) {
        BaseTest o = new TestMergeExtendBaseBean();
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
    


    //
    // This is the reference number (in KB) for memory consumption for 50 clones
    // (simply ran the test to get the number). We check against this number
    // to make sure that any change in schema2beans does not introduce a 
    // significant increase of mem usage.
    //
    static float memUsageReference = 236;

    
    public class MyListener implements PropertyChangeListener {
        String			listenerName;
        Object			oldValue;
        Object			newValue;
        String			propertyName;
        Object			source;
        int				index;
        boolean			mute;
        boolean			remove;
        Bean			rootBean;
	
        public MyListener(Bean bean) {
            this.listenerName = bean.name();
            this.remove = false;
            this.mute = false;
            rootBean = bean._getRoot();
            out("new listener for " + this.listenerName);
        }
	
        public void reset() {
            this.oldValue = null;
            this.newValue = null;
            this.propertyName = null;
            this.source = null;
            this.index = -1;
        }
	
        public void propertyChange(PropertyChangeEvent e) {
            if (this.mute)
                return;
	    
            String k;
            this.oldValue = e.getOldValue();
            this.newValue = e.getNewValue();
            this.propertyName = e.getPropertyName();
            this.source = e.getSource();
            String n = this.propertyName;
            //System.out.println("propertyName="+n);
            this.index = GraphManager.getPropertyIndex(rootBean, n);
	    
            //new Exception().printStackTrace(System.out);
            if (this.newValue == null)
                k = "Rmv";
            else
                if (this.oldValue == null)
                    k = "Add";
                else {
                    k = "Chg";
                }
	    
            out("<" + k + " Lnr:" + this.listenerName + " Evt:" + n +
                " Src:" + this.source.getClass().getName() + ">");
            if (remove) {
                out("<" + GraphManager.getPropertyName(n) + "[" + this.index +
                    "]" + " - Parent: " + GraphManager.getPropertyParentName(n) + ">");
            } else {
                out("<" + GraphManager.getPropertyName(n) + "[" + this.index +
                    "]" + " - Parent: " + GraphManager.getPropertyParentName(n) +
                    "/" + GraphManager.getPropertyParent(rootBean, n).getClass().getName() + ">");
            }
	    
            print("oldValue=");
            print(this.oldValue);
            println("");
            print("newValue=");
            print(this.newValue);
            println("");
        }

        public void removeMode() {
            this.remove = true;
        }
	
        public Object oldValue() {
            return this.oldValue;
        }
	
        public String stringOldValue() {
            if (this.oldValue == null)
                return "<null>";
            else
                return this.oldValue.toString();
        }
	
        public Object newValue() {
            return this.newValue;
        }
	
        public String stringNewValue() {
            if (this.newValue == null)
                return "<null>";
            else
                return this.newValue.toString();
        }
	
        public String name() {
            return this.propertyName;
        }
	
        public void mute(boolean mute) {
            this.mute = mute;
        }
	
        public String toString() {
            return this.name() + " raised from source " +
                this.source.getClass().getName();
        }
    }
    
    private MyListener l1, l2, l3;
    
    void mute(boolean mute) {
        if (l1 != null)
            l1.mute(mute);
        if (l2 != null)
            l2.mute(mute);
        if (l3 != null)
            l3.mute(mute);
    }
    
    public void run()
        throws Exception {
        Book b1, b2, b3;
	
        this.readDocument("tm01_g1.xml");
        out("creating the bean graph1");
        b1 = Book.createGraph(doc);
	
        this.readDocument("tm01_g1.xml");
        out("creating the bean graph2");
        b2 = Book.createGraph(doc);
	
        l1 = new MyListener(b1);
        b1.addPropertyChangeListener(l1);
	
        //	b2 should never change, so we should never receive anything
        //	on this listener
        l2 = new MyListener(b2);
        b2.addPropertyChangeListener(l2);

        //
        //	Merge two identical graphs
        //
        setTest("Merge(Update) - two identical xml files");
        out("should not get any event");
        b1.merge(b2);
        check(b1.toString().equals(b2.toString()), " - no diff");
        check(b1.equals(b2), " - equals");
	
        //	One mixed up document, but should be the same
        setTest("Merge(Update) - two identical xml files (one mixed up)");
        this.readDocument("tm01_g2.xml");
        out("creating the bean graph3");
        b3 = Book.createGraph(doc);
	
        l3 = new MyListener(b3);
        b3.addPropertyChangeListener(l3);
	
        b3.merge(b1);
        
        //
        //	Change only one element of the second graph
        //
        setTest("Merge(Update) - changing one property");
        String s1 = "This is the new summary";
        String s2 = b2.getSummary();
	
        //	g1/g2
        this.mute(true);
        b2.setSummary(s1);
        out("should get one event for Summary");
        this.mute(false);
        b1.merge(b2);
	
        b1.write(System.out);
	
        check(b2.equals(b1), " - equals");
        //	g1/g3
        this.mute(true);
        b1.setSummary(s2);
        this.mute(false);
        out("should not get any event");
        b1.merge(b3);
        // All of the same contents, but in a different order is still different.
        check(b3.equals(b1), " - equals");

        this.mute(true);
        b2.setSummary(s2);
	
        //
        //	Mixing array of strings
        //
        setTest("Merge(Update) - mixing  array of strings");
        check(b1.getAuthor(0).equals(b2.getAuthor(0)), "initial match");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "initial match");
        check(b1.getAuthor(2).equals(b2.getAuthor(2)), "initial match");
        this.mute(false);
        String [] ss1 = b2.getAuthor();
        String [] ss2 = new String[3];
        ss2[0] = ss1[2];
        ss2[1] = ss1[1];
        ss2[2] = ss1[0];
        b2.setAuthor(ss2);
        check(b1.getAuthor(0).equals(b2.getAuthor(2)), "mixed");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "mixed");
        check(b1.getAuthor(2).equals(b2.getAuthor(0)), "mixed");

        b1.merge(b2);
        // All of the same contents, but in a different order is still different.
        check(b2.equals(b1), " - equals");
        this.mute(true);
        b2.setAuthor(ss1);
        check(b1.getAuthor(0).equals(b2.getAuthor(2)), "reset match");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "reset match");
        check(b1.getAuthor(2).equals(b2.getAuthor(0)), "reset match");
	
        //
        //	Mixing array of beans
        //
        setTest("Merge(Update) - mixing  array of beans");
        check(b1.getChapter(0).equals(b2.getChapter(0)), "initial match");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "initial match");
        check(b1.getChapter(2).equals(b2.getChapter(2)), "initial match");
        this.mute(false);
        Chapter [] cc1 = b2.getChapter();
        Chapter [] cc2 = new Chapter[3];
        cc2[0] = cc1[2];
        cc2[1] = cc1[1];
        cc2[2] = cc1[0];
        b2.setChapter(cc2);
        check(b1.getChapter(0).equals(b2.getChapter(2)), "mixed");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "mixed");
        check(b1.getChapter(2).equals(b2.getChapter(0)), "mixed");

        b1.merge(b2);
        // All of the same contents, but in a different order is still different.
        check(b2.equals(b1), " - equals");
        this.mute(true);
        b2.setChapter(cc1);
        check(b1.getChapter(0).equals(b2.getChapter(2)), "reset match");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "reset match");
        check(b1.getChapter(2).equals(b2.getChapter(0)), "reset match");
	
        //
        //	Removing a String elt from an array
        //
        setTest("Merge(Update) - remove elt from array of strings");
        check(b1.getAuthor(0).equals(b2.getAuthor(0)), "initial match");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "initial match");
        check(b1.getAuthor(2).equals(b2.getAuthor(2)), "initial match");
        this.mute(false);
        b2 = (Book) b1.clone();
        s1 = b2.getAuthor(1);
        out("should get one remove event from deletion");
        b2.removeAuthor(s1);
        check(b1.getAuthor(0).equals(b2.getAuthor(0)), "match after rem");
        check(b1.getAuthor(2).equals(b2.getAuthor(1)), "match after rem");
        out("should get one Book remove event from merge");
        b1.merge(b2);
        check(b1.getAuthor(0).equals(b2.getAuthor(0)), "match");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "match");
        check(b1.sizeAuthor() == b2.sizeAuthor(), "correct size");
        check(b2.equals(b1), " - equals");
	
        //
        //	Adding a String elt from an array
        //
        setTest("Merge(Update) - add elt from array of strings");
        this.mute(false);
        out("should get one event for elt added");
        b2.addAuthor(s1);
        check(b2.getAuthor(2).equals(s1), "added");
        out("should get one event for elt added from merge");
        //out("Here is b1:");
        //b1.writeNoReindent(System.out);
        //out("Here is b2:");
        //b2.writeNoReindent(System.out);
        b1.merge(b2);
        //out("Here is b1:");
        //b1.writeNoReindent(System.out);
        check(b1.getAuthor(0).equals(b2.getAuthor(0)), "match");
        check(b1.getAuthor(1).equals(b2.getAuthor(1)), "match");
        check(b1.getAuthor(2).equals(b2.getAuthor(2)), "match");
        check(b1.sizeAuthor() == b2.sizeAuthor(), "correct size");
        check(b2.equals(b1), " - equals");
	
        //
        //	Removing a Bean elt from an array
        //
        setTest("Merge(Update) - remove elt from array of beans");
        check(b1.getChapter(0).equals(b2.getChapter(0)), "initial match");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "initial match");
        check(b1.getChapter(2).equals(b2.getChapter(2)), "initial match");
        this.mute(false);
        Chapter c1 = b2.getChapter(1);
        Chapter c2 = (Chapter)c1.clone();
        out("should get one remove event from deletion");
        b2.removeChapter(c1);
        check(b1.getChapter(0).equals(b2.getChapter(0)), "match after rem");
        check(b1.getChapter(2).equals(b2.getChapter(1)), "match after rem");
        out("should get one Chapter remove event from merge");
        b1.merge(b2);
        check(b1.getChapter(0).equals(b2.getChapter(0)), "match");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "match");
        check(b1.sizeChapter() == b2.sizeChapter(), "correct size");
        check(b2.equals(b1), " - equals");
	
        //
        //	Adding a Bean elt from an array
        //
        setTest("Merge(Update) - add elt from array of strings");
        this.mute(false);
        out("should get one event for elt added");
        b2.addChapter(c2);
        check(b2.getChapter(2).equals(c2), "added");
        out("should get one event for elt added from merge");
        b1.merge(b2);
        check(b1.getChapter(0).equals(b2.getChapter(0)), "match");
        check(b1.getChapter(1).equals(b2.getChapter(1)), "match");
        check(b1.getChapter(2).equals(b2.getChapter(2)), "match");
        check(b1.sizeChapter() == b2.sizeChapter(), "correct size");
        check(b2.equals(b1), " - equals");
	
	
        //
        //	Compare two graphs with missing nodes and elements
        //
        Book b4, b5, b6;
	
        this.readDocument("tm01_g1.xml");
        out("creating the bean graph1");
        b4 = Book.createGraph(doc);
	
        b5 = (Book)b4.clone();
        b6 = (Book)b4.clone();
	
        setTest("comparing graphs with 1 null elts");
        check(b4.sizeChapter() == 3);
        check(b5.sizeChapter() == 3);
	
        //	bean[] full / bean null
        check(b4.equals(b5));
	
        setTest("comparing graphs with null indexed elts");
        //	g1.bean[] has 1 null / g2.bean[] has 1 null / bean null
        b4.setChapter(1, null);
        b5.setChapter(2, null);
        check(!b4.equals(b5));
        b4.merge(b5);
        check(b4.sizeChapter() == 3, "correct new array sise");
        check(b4.getChapter(0).equals(b5.getChapter(0)), "elt ok");
        check(b4.getChapter(1).equals(b5.getChapter(1)), "elt ok");
        check(b4.getChapter(2) == null, "elt ok");
	
        setTest("comparing graphs with null single bean elt");
        //	g1.bean non null / g2.bean is null
        b4 = (Book)b6.clone();
        b5 = (Book)b6.clone();
        Content ct = new Content();
        ct.setTitle("This is a title");
        ct.setComment("And this is a comment");
        check(b4.equals(b5));
        b5.setContent(ct);
        check(!b4.equals(b5));
        b4.merge(b5);
        check(b4.getContent().equals(b5.getContent()));
	
        //	Clone an element which is not part of a graph
        Chapter c3 = new Chapter();
        c3.setComment("This is a comment");
        c3.setNumber("123");
        c3.addParagraph("This is a new paragraph");
        Chapter c4 = (Chapter)c3.clone();
	
        //	Add both elements to two identical graphs - should get two
        //	identical graphs
        setTest("cloning a new bean");
        Book b7 = (Book)b2.clone();
        Book b8 = (Book)b2.clone();
        b7.addChapter(c3);
        b8.addChapter(c4);
        check(c3.equals(c4), "objects equal");
        check(b7.equals(b8), "same graph once added");
	
        //
        //	Test the attributes. When we merge graphs, we need to make
        //	sure that the attributes are also merged.
        //
        this.readDocument("tm01_g2.xml");
        out("creating the bean graph1");
        b1 = Book.createGraph(doc);
	
        //	g3 and g2 elements are identicals, g3 has attributes,
        //	g2 has not.
        this.readDocument("tm01_g3.xml");
        out("creating the bean graph2");
        b2 = Book.createGraph(doc);

        // Make sure that we can clone a part of the graph without loosing
        // the attributes.
        BaseBean bb = b2.getChapter(0);
        //  Should see the chapter attribute
        out(bb.toString());

        b3 = (Book)b1.clone();
        String chapterPropertyName = b3.CHAPTER;
        chapterPropertyName = new String(chapterPropertyName+""); // see if we can get a different String, to test that intern is called properly.
        int index = b3.addValue(chapterPropertyName, bb.clone());
        bb = b3.getChapter(index);
        //  Should see the chapter attribute on this cloned element
        out(bb.toString());

        l1 = new MyListener(b1);
        b1.addPropertyChangeListener(l1);
	
        //	b2 should never change, so we should never receive anything
        //	on this listener
        l2 = new MyListener(b2);
        b2.addPropertyChangeListener(l2);

        //	The only events we should have when we merge are the 
        //	attributes events, because the graphs only differ by attr.
        setTest("Merging attributes");
        check(!b1.equals(b2), "shouldn't be equals (diff an attr)");
        /*
        out("b1:");
        b1.write(out);
        out("b2:");
        b2.write(out);
        //org.netbeans.modules.schema2beans.DDLogFlags.setDebug(true);
        */
        b1.merge(b2);
        /*
        out("b1:");
        b1.write(out);
        */
        // All of the same contents, but in a different order is still different.
        check(b1.equals(b2), "should be equals");
	
        //	Make sure that b1 has the attributes
        s1 = b1.getGood();
        check(s1 != null, "attr on root != null");
        if (s1 != null) {
            check(s1.equals("no"), "attr on root");
        }
        s1 = b1.getSummaryLength();
        check(s1 != null, "attr on summary != null");
        if (s1 != null) {
            check(s1.equals("123"), "attr on summary");
        }
        s1 = b1.getSummaryLang();
        check(s1 != null, "attr on summary != null");
        if (s1 != null) {
            check(s1.equals("us"), "attr on summary");
        }

        s1 = b1.getChapter(0).getTitle();
        out(s1);
        check(s1 != null, "attr on chapter != null");
        if (s1 != null) {
            check(s1.equals("First"), "attr on chapter");
        }

        s1 = b1.getChapter(1).getTitle();
        out(s1);
        check(s1 != null, "attr on chapter != null");
        if (s1 != null) {
            check(s1.equals("Second"), "attr on chapter");
        }
	
        //
        //  Make sure that we do not consume too much memory
        //

//        //  Ignore the first one
//        this.getKMemUsage();
//
//        int k1 = this.getKMemUsage();
//		
//        Book newBook;
//
//        this.readDocument("tm01_g3.xml");
//        out("creating the bean graph for memory test");
//        newBook = Book.createGraph(doc);
//		
//        int maxLoop = 50;
//        BaseBean[] aBook = new BaseBean[maxLoop];
//        for(int loop=0; loop<maxLoop; loop++) {
//            aBook[loop] = (BaseBean)newBook.clone();
//        }
//
//        int k2 = this.getKMemUsage() - k1;
//
//        float diff = (float)(k2 - memUsageReference);
//
//        if (diff > 0) {
//            //	We consume more memory than excepted
//            float p = diff/memUsageReference*100;
//            if (p > 20.0) {
//                out("It seems that the last schema2beans code changes have increased the memory consumption by " + p + "%");
//                out("If this is expected and acceptable, change the memUsageReference value in TestMergeExtendBaseBean.java, to be " + k2);
//            }
//        } else {
//            //	We consume less memory than expected
//            float p = Math.abs(diff)/memUsageReference*100;
//            if (p > 25.0) {
//                out("It seems that the last schema2beans code changes have decreased the memory consumption by " + p + "% !!!");
//                out("Please, change the memUsageReference value in TestMergeExtendBaseBean.java, to be " + k2);
//            }   
//        }
//        out("memory test done");

        readDocument("tm01_g1.xml");
        b1 = Book.createGraph(doc);
        readDocument("tm01_g4.xml");
        out("creating the bean graph for the comment merge test");
        Book commentedGraph = Book.createGraph(doc);
        b1.merge(commentedGraph);
        out(b1);
    }
}


