/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        suite.addTest(new DiffFinderTest("testFindDiffPerf"));
        suite.addTest(new DiffFinderTest("testFindDiffPerf2"));
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
