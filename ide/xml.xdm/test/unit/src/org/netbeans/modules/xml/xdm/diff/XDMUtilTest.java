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
 * XMLModelTest.java
 * JUnit based test
 *
 * Created on August 5, 2005, 12:13 PM
 */

package org.netbeans.modules.xml.xdm.diff;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeChange;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeDiff;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author Ayub Khan
 */
public class XDMUtilTest extends TestCase {
    
    public XDMUtilTest(String testName) {
        super(testName);
    }
    
    public void testPrettyPrintXML() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String xml = readXMLString("diff/xdu/prettyprint1_1.xml");
        String expected = readXMLString("diff/xdu/prettyprint1_2.xml");        
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }
    
    public void testPrettyPrintXML2() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String xml = readXMLString("diff/xdu/prettyprint2_1.xml");
        String expected = readXMLString("diff/xdu/prettyprint2_2.xml");  
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }    

    public void testPrettyPrintAddNamespace() throws Exception {
        XDMUtil util = new XDMUtil();
        String xml1 = readXMLString("diff/xdu/pp-add-ns-decl-1.xml");
        String xml1_pretty = util.prettyPrintXML(xml1, "    ");
        String xml2 = readXMLString("diff/xdu/pp-add-ns-decl-2.xml");
        assertEquals(xml2, xml1_pretty);
    }

    public void testPrettyPrintXMLNegative() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        String expected = XDMUtil.XML_PROLOG;
        String xml = XDMUtil.XML_PROLOG;
        String changed = util.prettyPrintXML(xml, indent);
        assertEquals("pretty print", expected, changed);
    }
    
    public void testCompareXMLEquals() throws Exception {
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.EQUAL;
        //Only Element and Attribute order change
        String xml1 = readXMLString("diff/xdu/equals1_1.xml");
        String xml2 = readXMLString("diff/xdu/equals1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print", 0, diffs.size());
        
        //Attribute value change. Element and Attribute order change
        xml1 = readXMLString("diff/xdu/equals1_3.xml");
        xml2 = readXMLString("diff/xdu/equals1_4.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 1, diffs.size());
        
        //Attribute added. Element and Attribute order change
        xml1 = readXMLString("diff/xdu/equals1_5.xml");
        xml2 = readXMLString("diff/xdu/equals1_6.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 1, diffs.size());
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertEquals("pretty print attribute change size", 1,
                ((Change)diffs.get(0)).getAttrChanges().size());
    }
    
    public void testCompareXMLIdentical() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Element and Attribute order change
        String xml1 = readXMLString("diff/xdu/identical1_1.xml");
        String xml2 = readXMLString("diff/xdu/identical1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print", 2, diffs.size());
        
        //Attribute value change. Element and Attribute order change
        xml1 = readXMLString("diff/xdu/identical1_3.xml");
        xml2 = readXMLString("diff/xdu/identical1_4.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size());//1 - Attr pos, value change, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
        
        List<Change.AttributeDiff> attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 2, attrDiffs.size());
        Change.AttributeChange change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        Change.AttributeChange change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertFalse("pretty print attribute no token change", change2.isTokenChanged());
        
        //Attribute added. Element and Attribute order change
        xml1 = readXMLString("diff/xdu/identical1_5.xml");
        xml2 = readXMLString("diff/xdu/identical1_6.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size()); //1 - Attr pos, value change + add, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
        
        attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 3, attrDiffs.size());
        change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertFalse("pretty print attribute no token change", change2.isTokenChanged());
        Change.AttributeAdd add = (Change.AttributeAdd) attrDiffs.get(2);
        assertEquals("pretty print attribute pos only change", 2, add.getNewAttributePosition());
        
        //Attribute added. Element and Attribute order change + Ignore whitespaces
        xml1 = readXMLString("diff/xdu/identical1_7.xml");
        xml2 = readXMLString("diff/xdu/identical1_8.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("pretty print size", 2, diffs.size()); //1 - Attr pos, value change + add, 1 - Element pos change
        assertTrue("pretty print attribute change", ((Change)diffs.get(0)).isAttributeChanged());
        assertTrue("pretty print element pos change", ((Change)diffs.get(1)).isPositionChanged());
       
        attrDiffs = ((Change)diffs.get(0)).getAttrChanges();
        assertEquals("pretty print attribute change size", 3, attrDiffs.size());
        change1 = (Change.AttributeChange) attrDiffs.get(0);
        assertTrue("pretty print attribute pos & token change", change1.isPositionChanged());
        assertTrue("pretty print attribute pos & token change", change1.isTokenChanged());
        change2 = (Change.AttributeChange) attrDiffs.get(1);
        assertTrue("pretty print attribute pos only change", change2.isPositionChanged());
        assertTrue("pretty print attribute no token change", change2.isTokenChanged());
        add = (Change.AttributeAdd) attrDiffs.get(2);
        assertEquals("pretty print attribute pos only change", 2, add.getNewAttributePosition());
    }
    
    public void testCompareXMLIdentical2() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only NS Attribute delete and add
        String xml1 = readXMLString("diff/xdu/identical2_1.xml");
        String xml2 = readXMLString("diff/xdu/identical2_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    }
    
    /**
     * Test the comparision of defaultnamespace and no defautl but element has namsespace
     */
    public void testComparePrefix() throws Exception {
        String xml1 = readXMLString("diff/xdu/pfx1_1.xml");
        String xml2 = readXMLString("diff/xdu/pfx1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 0, diffs.size());
    }
    
    
    /**
     * Test extra unused namespace
     */
    public void testCompareExtraNamespace() throws Exception {
        String xml1 = readXMLString("diff/xdu/extrans1_1.xml");
        String xml2 = readXMLString("diff/xdu/extrans1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareExtraNamespace is equal?", 0, diffs.size());
    }
    
    
/* Test both xml has schemaLocation defined and there's extra space in one xml between elements.
 */
    public void testCompareWhitespaceOutofElement_SchemaLoc() throws Exception {
        String xml1 = readXMLString("diff/xdu/wsschemaloc1_1.xml");
        String xml2 = readXMLString("diff/xdu/wsschemaloc1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareWhitespaceOutofElement_SchemaLoc?", 0, diffs.size());
    }
    
    
/* Test both xml with NO schemaLocation defined and there's extra space in one xml between elements.
 */
    public void testCompareWhitespaceOutofElement_NoSchemaLoc() throws Exception {
        String xml1 = readXMLString("diff/xdu/wsnoschemaloc1_1.xml");
        String xml2 = readXMLString("diff/xdu/wsnoschemaloc1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareWhitespaceOutofElement_NoSchemaLoc?", 0, diffs.size());
    }
    
    /**
     * Test extra whitespace betweeen attributes
     */
    public void testCompareExtraWhiteSpaceBetweenAttr() throws Exception {
        String xml1 = readXMLString("diff/xdu/extrawsattr1_1.xml");
        String xml2 = readXMLString("diff/xdu/extrawsattr1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareExtraWhiteSpaceBetweenAttr is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWSchemaLocation() throws Exception {
        String xml1 = readXMLString("diff/xdu/schemalocation1_1.xml");
        String xml2 = readXMLString("diff/xdu/schemalocation1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 0, diffs.size());
    }
    
    /**
     * Test the comparision of xml with different url for same prefix
     */
    public void testCompareXMLSamePrefixDifferentURL() throws Exception {
        String xml1 = readXMLString("diff/xdu/samepfx1_1.xml");
        String xml2 = readXMLString("diff/xdu/samepfx1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 2, diffs.size());
    }
    
    /**
     * Test the comparision of xml with different url for same prefix
     */
    public void testCompareXMLSamePrefixDifferentURL2() throws Exception {
        String xml1 = readXMLString("diff/xdu/samepfx2_1.xml");
        String xml2 = readXMLString("diff/xdu/samepfx2_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 2, diffs.size());
    }
    
    /**
     * Test the comparision of xml with same url for different prefix
     */
    public void testCompareXMLDifferentPrefixSameURL() throws Exception {
        String xml1 = readXMLString("diff/xdu/difpfx1_1.xml");
        String xml2 = readXMLString("diff/xdu/difpfx1_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testComparePrefix is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWithWhitespace() throws Exception {
        String xml1 = readXMLString("diff/xdu/textchange2_1.xml");
        String xml2 = readXMLString("diff/xdu/whitespace.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 0, diffs.size());
    }
    
    /**
     * Test xml one has schema location and the other one does not
     */
    public void testCompareXMLWithTextChange() throws Exception {
        String xml1 = readXMLString("diff/xdu/textchange2_1.xml");
        String xml2 = readXMLString("diff/xdu/textchange2_2.xml");
        
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        
        List<Difference> diffs = compareXML(xml1, xml2 , criteria);
        assertEquals("testCompareXMLWSchemaLocation is equal?", 1, diffs.size());
    }
    
    public void testFilterAttributeOrderChange() throws Exception {
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Attribute order change
        String xml1 = readXMLString("diff/xdu/attrorder1_1.xml");
        String xml2 = readXMLString("diff/xdu/attrorder1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr order & token change", 2, 
                ((Change)diffs.get(0)).getAttrChanges().size());//x and y changed positions
        XDMUtil.filterAttributeOrderChange(diffs);  //<- new filter for ignoring attr changes
        assertEquals("attr order & token change", 0, diffs.size());
        
        //Only Attribute order and token change
        xml1 = readXMLString("diff/xdu/attrorder2_1.xml");
        xml2 = readXMLString("diff/xdu/attrorder2_2.xml");
        diffs = compareXML(xml1, xml2, criteria);
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr order & token change", 2, 
                ((Change)diffs.get(0)).getAttrChanges().size());
        XDMUtil.filterAttributeOrderChange(diffs);  //<- new filter for ignoring attr changes
        assertEquals("attr order & token change", 1, diffs.size());
        assertEquals("attr token change", 1, 
                ((Change)diffs.get(0)).getAttrChanges().size());
    }
    
    public void testFindOffsets() throws Exception {
        XDMUtil util = new XDMUtil();
        String indent = "    ";
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        //Only Element and Attribute order change
        String xml1 = readXMLString("diff/xdu/findoffset1_1.xml");
        String xml2 = readXMLString("diff/xdu/findoffset1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("find Offsets", 2, diffs.size());
        
        Difference d = diffs.get(1);
        assertTrue("diff: ", d instanceof Change);
        NodeInfo oldInfo = d.getOldNodeInfo();
        Node oldNode = oldInfo.getNode();
        NodeInfo info = d.getNewNodeInfo();
        Node node = info.getNode();
        if(oldNode != null)
            assertEquals("old change begin: ", 50, XDMUtil.findPosition(oldNode));
        if(node != null)
            assertEquals("new change begin: ", 45, XDMUtil.findPosition(node));

        d = diffs.get(0);
        assertTrue("diff: ", d instanceof Change);
        oldInfo = d.getOldNodeInfo();
        oldNode = oldInfo.getNode();
        info = d.getNewNodeInfo();
        node = info.getNode();
        if(oldNode != null)
            assertEquals("old change begin: ", 22, XDMUtil.findPosition(oldNode));
        if(node != null)
            assertEquals("new change begin: ", 22, XDMUtil.findPosition(node));            
        if(d instanceof Change) {
            List<AttributeDiff> attrDiffs = ((Change)d).getAttrChanges();
            AttributeDiff ad = attrDiffs.get(0);
            assertTrue("attr diff: ", ad instanceof AttributeChange);
            Attribute oldAttr = ad.getOldAttribute();
            Attribute attr = ad.getNewAttribute();
            if(oldAttr != null)
                assertEquals("old attr change begin: ", 28, XDMUtil.findPosition(oldAttr));
            if(attr != null)
                assertEquals("new attr change begin: ", 35, XDMUtil.findPosition(attr));

            ad = attrDiffs.get(1);
            assertTrue("attr diff: ", ad instanceof AttributeChange);
            oldAttr = ad.getOldAttribute();
            attr = ad.getNewAttribute();
            if(oldAttr != null)
                assertEquals("old attr change begin: ", 35, XDMUtil.findPosition(oldAttr));
            if(attr != null)
                assertEquals("new attr change begin: ", 28, XDMUtil.findPosition(attr));
        }
    }
    
    public void testCompareXMLIdenticalExtraEmptyNS() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        String xml1 = readXMLString("diff/xdu/extraemptyns1_1.xml");
        String xml2 = readXMLString("diff/xdu/extraemptyns1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    }    
    
    public void testCompareXMLIdenticalExtraEmptyNS2() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        String xml1 = readXMLString("diff/xdu/extraemptyns2_1.xml");
        String xml2 = readXMLString("diff/xdu/extraemptyns2_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    } 
    
    public void testCompareXMLIdenticalExtraEmptyNS3() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        String xml1 = readXMLString("diff/xdu/extraemptyns3_1.xml");
        String xml2 = readXMLString("diff/xdu/extraemptyns3_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    } 
    
    public void testCompareXMLIdenticalExtraEmptyNS4() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        String xml1 = readXMLString("diff/xdu/extraemptyns4_1.xml");
        String xml2 = readXMLString("diff/xdu/extraemptyns4_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 0, diffs.size());
    }     
    
    public void testCompareWithDiffInLeafNodes() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.IDENTICAL;
        String xml1 = readXMLString("diff/xdu/textchange1_1.xml");
        String xml2 = readXMLString("diff/xdu/textchange1_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 2, diffs.size());
    }    
    
    public void testCompareIssue114941() throws Exception {
        XDMUtil util = new XDMUtil();
        XDMUtil.ComparisonCriteria criteria = XDMUtil.ComparisonCriteria.EQUAL;
        String xml1 = readXMLString("diff/xdu/test114941_1.xml");
        String xml2 = readXMLString("diff/xdu/test114941_2.xml");
        List<Difference> diffs = compareXML(xml1, xml2, criteria);
        assertEquals("compare identical XML", 3, diffs.size());
    }    

    private String readXMLString(String path) throws Exception {
        Document doc = Util.getResourceAsDocument(path);
        return doc.getText(0, doc.getLength());
    }
    
    public List<Difference> compareXML(String xml1, String xml2,
            XDMUtil.ComparisonCriteria criteria)
            throws Exception {
        return new XDMUtil().compareXML(xml1, xml2, criteria);
    }    
}
