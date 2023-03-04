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

package org.netbeans.modules.xml.xdm.diff;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * class to test DiffFinder
 *
 * @author Ayub Khan
 */
public class DiffFinderTest extends TestCase {
    
    public DiffFinderTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
//		TestSuite suite = new TestSuite(DiffFinderTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new DiffFinderTest("testFindDiff1"));
        suite.addTest(new DiffFinderTest("testFindDiff2"));
        suite.addTest(new DiffFinderTest("testFindDiff3"));
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTest(new DiffFinderTest("testFindDiffPerf"));
//        suite.addTest(new DiffFinderTest("testFindDiffPerf2"));
        suite.addTest(new DiffFinderTest("testElementAddedBeforeChildAttributeChange"));
        suite.addTest(new DiffFinderTest("testElementAddedBeforeChildAttributeChange2"));
        return suite;
    }
    
    public void testFindDiff1() throws Exception {
        String FILE1 = "diff/schema3.xsd";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/schema4.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        //establish DOM element identities
        DefaultElementIdentity eID = new DefaultElementIdentity();
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        
//        long startTime=System.currentTimeMillis();
        DiffFinder dv = new DiffFinder(eID);
        List<Difference> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to compare schema1.xsd to schema2. "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        assertEquals( 7 , deList.size() );
        
//		DiffFinder.printDeList( deList );
    }
    
    public void testFindDiff2() throws Exception {
        String FILE1 = "diff/schema3.xsd";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/schema5.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        //establish DOM element identities
        DefaultElementIdentity eID = new DefaultElementIdentity();
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        
//        long startTime=System.currentTimeMillis();
        DiffFinder dv = new DiffFinder(eID);
        List<Difference> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to compare schema1.xsd to schema3. "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
        
        assertEquals( 10, deList.size());
//		DiffFinder.printDeList( deList );
    }
    
    public void testFindDiff3() throws Exception {
        String FILE1 = "diff/PurchaseOrder.xsd";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "diff/PurchaseOrderSyncTest.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        //establish DOM element identities
        DefaultElementIdentity eID = new DefaultElementIdentity();
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        
//        long startTime=System.currentTimeMillis();
        DiffFinder dv = new DiffFinder( eID );
        List<Difference> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
//		long endTime=System.currentTimeMillis();
//		System.out.println("\n\n::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to compare PurchaseOrder. "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
//		DiffFinder.printDeList( deList );
        assertEquals(deList.toString(), 11, deList.size() );
    }
    
    public void testFindDiffPerf() throws Exception {
        String FILE1 = "perf/J1_TravelItinerary.xsd";
        Document d1 = Util.getResourceAsDocument(FILE1);
        Lookup lookup = Lookups.singleton(d1);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel m1 = new XDMModel(ms);
        m1.sync();
        
        String FILE2 = "perf/J1_TravelItinerary.xsd";
        Document d2 = Util.getResourceAsDocument(FILE2);
        lookup = Lookups.singleton(d2);
        ms = new ModelSource(lookup, true);
        XDMModel m2 = new XDMModel(ms);
        m2.sync();
        
        //establish DOM element identities
        DefaultElementIdentity eID = new DefaultElementIdentity();
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        
        long startTime=System.currentTimeMillis();
        DiffFinder dv = new DiffFinder( eID );
        List<Difference> deList = dv.findDiff(m1.getDocument(), m2.getDocument());
//		long endTime=System.currentTimeMillis();
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::\n" +
//				"Total time to compare TravelItinerary: "+(endTime-startTime)+"ms");
//		System.out.println("::::::::::::::::::::::::::::::::::::::::::::");
//
//		DiffFinder.printDeList( deList );
        assertEquals( 0, deList.size() );
    }
    
    public void testFindDiffPerf2() throws Exception {
        List<Difference> diffs = Util.diff("diff/TravelItinerary1.xsd", "diff/TravelItinerary2.xsd");
        assertEquals(diffs.toString(), 4, diffs.size() );
    }
    
    public void testElementAddedBeforeChildAttributeChange() throws Exception {
        List<Difference> diffs = Util.diff("diff/test1.xml", "diff/test1_1.xml");
        assertEquals(diffs.toString(), 3, diffs.size() );
        assertTrue(((Change)diffs.get(2)).isAttributeChanged());
    }
    
    public void testElementAddedBeforeChildAttributeChange2() throws Exception {
        List<Difference> diffs = Util.diff("diff/test2.xml", "diff/test2_1.xml");
        assertEquals(diffs.toString(), 9, diffs.size());
        assertTrue(((Change)diffs.get(3)).isAttributeChanged());
        assertFalse(((Change)diffs.get(3)).isPositionChanged());
        assertFalse(((Change)diffs.get(4)).isAttributeChanged());
        assertTrue(((Change)diffs.get(4)).isPositionChanged());
    }
    
    String SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
}
