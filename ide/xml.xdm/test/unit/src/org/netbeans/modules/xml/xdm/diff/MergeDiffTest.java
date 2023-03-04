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
 * MergeDiffTest.java
 * JUnit based test
 *
 * Created on February 2, 2006, 4:15 PM
 */

package org.netbeans.modules.xml.xdm.diff;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * class to test MergeDiff
 * Make sure cover all these test cases:
 *   1. just change tokens
 *   2. just attribute
 *   3. both token and attribute change
 *   4. just children
 *   5. just position change
 *   6. both postion change and add or delete
 *   7. all-in-one
 *
 * @author Ayub Khan
 */
public class MergeDiffTest extends TestCase {
    
    public MergeDiffTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    static DefaultElementIdentity createEid() {
        //Establish DOM element identities
        DefaultElementIdentity eid = new DefaultElementIdentity();
        //Following values are suitable for Schema and WSDL documents
        eid.addIdentifier( "id" );
        eid.addIdentifier( "name" );
        eid.addIdentifier( "ref" );
        return eid;
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
//		TestSuite suite = new TestSuite(MergeDiffTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new MergeDiffTest("testMerge1"));
        suite.addTest(new MergeDiffTest("testMerge2"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new MergeDiffTest("testMerge3"));
        suite.addTest(new MergeDiffTest("testMerge4"));
        suite.addTest(new MergeDiffTest("testMerge5"));
        suite.addTest(new MergeDiffTest("testMerge6"));
        suite.addTest(new MergeDiffTest("testMerge7"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new MergeDiffTest("testMerge8"));
        suite.addTest(new MergeDiffTest("testMerge9"));
        suite.addTest(new MergeDiffTest("testMerge10"));
        suite.addTest(new MergeDiffTest("testPrettyPrint"));
        suite.addTest(new MergeDiffTest("testSelfClosing"));
        suite.addTest(new MergeDiffTest("testAddChangeAttributeSameNode"));
        suite.addTest(new MergeDiffTest("testElementAddedBeforeChildAttributeChange"));
        suite.addTest(new MergeDiffTest("testPosChange"));
        suite.addTest(new MergeDiffTest("testPosChange2"));
        suite.addTest(new MergeDiffTest("testPosChange3"));
        suite.addTest(new MergeDiffTest("testPosChange3_2"));
        suite.addTest(new MergeDiffTest("testPosChange3_3"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new MergeDiffTest("testMergeVenetianBlindPO"));
        suite.addTest(new MergeDiffTest("testForwardReorderNodeWithChangesOnChildren"));
        suite.addTest(new MergeDiffTest("testMergeReformatDiff"));
        return suite;
    }
    
    public void testMerge1() throws Exception {
        
        String FILE1 = "diff/schema3.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/schema5.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge schema1.xsd to schema3.xsd: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge2() throws Exception {
        
        String FILE1 = "diff/PurchaseOrder.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/PurchaseOrderSyncTest.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge PurchaseOrder.xsd: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge3() throws Exception {
        
        String FILE1 = "diff/TravelItinerary1.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/TravelItinerary2.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
        long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge TravelItinerary: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge4() throws Exception {
        
        String FILE1 = "diff/testaddshape.xml";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/addshape.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge addshape.xml: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge5() throws Exception {
        
        String FILE1 = "diff/testbase.xml";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/commentTextChanged.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge6() throws Exception {
        
        String FILE1 = "diff/TestOperations.wsdl";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/TestOperations_after.wsdl";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge7() throws Exception {
        
        String FILE1 = "diff/VehicleReservationService.wsdl";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/Vehicle_PartnerLinkChanged2.wsdl";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge textchange.xml: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge8() throws Exception {
        
        String FILE1 = "diff/UBL-CommonAggregateComponents-1.0.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/UBL-CommonAggregateComponents-1.0.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
        originalModel.flush();
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()),
                originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge9() throws Exception {
        
        String FILE1 = "diff/schema3.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/schema4.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge schema1.xsd to schema3.xsd: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testMerge10() throws Exception {
        
        String FILE1 = "diff/schema4.xsd";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/schema5.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge schema1.xsd to schema3.xsd: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public void testPrettyPrint() throws Exception {
        
        String FILE1 = "diff/prettyprint1.xml";
        Document originalDocument = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(originalDocument);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel originalModel = new XDMModel(ms);
        originalModel.sync();
        
        String FILE2 = "diff/prettyprint2.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
//        long startTime=System.currentTimeMillis();
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(originalModel, m2.getDocument());
        
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to merge schema1.xsd to schema3.xsd: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        originalModel.flush();
        
//		System.out.println("original doc: \n["+originalDocument.getText(0,originalDocument.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), originalDocument.getText(0,originalDocument.getLength()));
    }
    
    public static void merge(XDMModel model, List<Difference> diffs) {
        model.mergeDiff(diffs);
        model.flush();
    }
    
    public void testSelfClosing() throws Exception {
        XDMModel mod1 = Util.loadXDMModel("not_selfClosing.xml");
        XDMModel mod2 = Util.loadXDMModel("selfClosing.xml");
        List<Difference> diffs = Util.diff(mod1, mod2);
        assertEquals(diffs.toString(), 2, diffs.size());
        merge(mod1, diffs);
        List<Difference> zeroDiffs = Util.diff(mod1, mod2);
        assertTrue("before:"+diffs.toString()+" after:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }
    
    public void testAddChangeAttributeSameNode() throws Exception {
        XDMModel mod1 = Util.loadXDMModel("diff/PurchaseOrder2.xsd");
        XDMModel mod2 = Util.loadXDMModel("diff/PurchaseOrderSyncTest2.xsd");
        List<Difference> diffs = Util.diff(mod1, mod2);
        //assertEquals(diffs.toString(), 4, diffs.size());
        merge(mod1, diffs);
        List<Difference> zeroDiffs = Util.diff(mod1, mod2);
        assertTrue("before:"+diffs.toString()+" after:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }
    
    public void testElementAddedBeforeChildAttributeChange() throws Exception {
        String FILE1 = "diff/test2.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/test2_1.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
        assertEquals(diffs.toString(), 9, diffs.size());
        assertTrue(((Change)diffs.get(3)).isAttributeChanged());
        assertFalse(((Change)diffs.get(3)).isPositionChanged());
        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//		System.out.println("original doc: \n["+d1.getText(0,d1.getLength())+"]");
//		System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }
    
    public void testPosChange() throws Exception {
        String FILE1 = "diff/posChange1.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/posChange1_1.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
        assertEquals(diffs.toString(), 4, diffs.size());
//        assertFalse(((Change)diffs.get(2)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(2)).isPositionChanged());
//        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//        System.out.println("diff: \n["+diffs.toString()+"]");
//        System.out.println("resulting doc: \n["+d1.getText(0,d1.getLength())+"]");
//        System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }    
    
    public void testPosChange2() throws Exception {
        String FILE1 = "diff/posChange2.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/posChange2_1.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
        assertEquals(diffs.toString(), 5, diffs.size());
//        assertFalse(((Change)diffs.get(2)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(2)).isPositionChanged());
//        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//        System.out.println("original doc: \n["+d1.getText(0,d1.getLength())+"]");
//        System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }    
        
    public void testPosChange3() throws Exception {
        String FILE1 = "diff/posChange3.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/posChange3_1.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
        assertEquals(diffs.toString(), 4, diffs.size());
//        assertFalse(((Change)diffs.get(2)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(2)).isPositionChanged());
//        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//        System.out.println("diff: \n["+diffs.toString()+"]");
//        System.out.println("resulting doc: \n["+d1.getText(0,d1.getLength())+"]");
//        System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }    
        
    public void testPosChange3_2() throws Exception {
        String FILE1 = "diff/posChange3.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/posChange3_2.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
//        assertEquals(diffs.toString(), 4, diffs.size());
//        assertFalse(((Change)diffs.get(2)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(2)).isPositionChanged());
//        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//        System.out.println("diff: \n["+diffs.toString()+"]");
//        System.out.println("resulting doc: \n["+d1.getText(0,d1.getLength())+"]");
//        System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }
    
    public void testPosChange3_3() throws Exception {
        String FILE1 = "diff/posChange3.xml";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/posChange3_3.xml";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        List<Difference> diffs = Util.diff(m1, m2);
//        assertEquals(diffs.toString(), 4, diffs.size());
//        assertFalse(((Change)diffs.get(2)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(2)).isPositionChanged());
//        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
//        assertTrue(((Change)diffs.get(4)).isPositionChanged());
        
        XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
        treeDiff.performDiffAndMutate(m1, m2.getDocument());
        
        m1.flush();
        
//        System.out.println("diff: \n["+diffs.toString()+"]");
//        System.out.println("resulting doc: \n["+d1.getText(0,d1.getLength())+"]");
//        System.out.println("target doc: \n["+d2.getText(0,d2.getLength())+"]");
        assertEquals("original document should be equivalent to merged document",
                d2.getText(0,d2.getLength()), d1.getText(0,d1.getLength()));
        
        List<Difference> zeroDiffs = Util.diff(m1, m2);
        assertTrue("before:"+diffs.toString()+"\nafter:"+zeroDiffs.toString(), zeroDiffs.isEmpty());
    }

    public void testMergeVenetianBlindPO() throws Exception {
        javax.swing.text.Document doc1 = Util.getResourceAsDocument("resources/po_venetian.xsd");
        XDMModel mod1 = Util.loadXDMModel(doc1);
        javax.swing.text.Document doc2 = Util. getResourceAsDocument("resources/po.xsd");
        XDMModel mod2 = Util.loadXDMModel(doc2);
        List<Difference> diffs = Util.diff(mod1, mod2);
        for (Iterator<Difference> i = diffs.iterator(); i.hasNext();) {
            Difference d = i.next();
            if (d instanceof Change) {
                Change c = (Change) d;
                if (c.isPositionChanged() && c.getOldNodeInfo().getParent().getId() == 1) {
                    //FIXME assertFalse("There should be no position changes", true);
                    i.remove();
                }
            }
        }
        merge(mod1, diffs);
        String text1 = doc1.getText(0, doc1.getLength());
        String text2 = doc2.getText(0, doc2.getLength());
        //FIXME
        //assertEquals(text2, text1);
    }
    
    public void testForwardReorderNodeWithChangesOnChildren() throws Exception {
        javax.swing.text.Document doc1 = Util.getResourceAsDocument("diff/posChange4.xml");
        XDMModel mod1 = Util.loadXDMModel(doc1);
        javax.swing.text.Document doc2 = Util.getResourceAsDocument("diff/posChange4_WithChildrenChanges.xml");
        XDMModel mod2 = Util.loadXDMModel(doc2);
        List<Difference> diffs = Util.diff(mod1, mod2);
        
        merge(mod1, diffs);
        String text1 = doc1.getText(0, doc1.getLength());
        String text2 = doc2.getText(0, doc2.getLength());
        assertEquals(text2, text1);
    }
    
    public void testMergeReformatDiff() throws Exception {
        javax.swing.text.Document doc1 = Util.getResourceAsDocument("diff/newWSDL1.wsdl");
        XDMModel mod1 = Util.loadXDMModel(doc1);
        javax.swing.text.Document doc2 = Util.getResourceAsDocument("diff/newWSDL1_reformat.wsdl");
        XDMModel mod2 = Util.loadXDMModel(doc2);
        List<Difference> diffs = Util.diff(mod1, mod2);
        merge(mod1, diffs);
        String text1 = doc1.getText(0, doc1.getLength());
        String text2 = doc2.getText(0, doc2.getLength());
        assertEquals(text2, text1);
    }
    
    DefaultElementIdentity eID = createEid();
}
