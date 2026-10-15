/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.xdm.diff;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.diff.Change.AttributeChange;
import org.netbeans.modules.xml.xdm.visitor.PositionFinderVisitor;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import javax.swing.text.Document;
import org.netbeans.editor.BaseKit;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ayub Khan
 */
public class XDMUtil {
    
    public enum ComparisonCriteria {
        EQUAL,
        IDENTICAL;
    }
    
    /**
     * Constructor for XDMUtil
     */
    public XDMUtil() {
    }

    /*
     * returns a pretty print version of xml doc, using given indentation
     */
    public String prettyPrintXML(String doc, String indentation) 
    throws UnsupportedEncodingException, IOException, BadLocationException {
        Document sd1 = new BaseDocument(true, "text/xml"); //NOI18N
        XDMModel m1 = createXDMModel(sd1, doc);
        Node root1 = m1.getDocument();
        
        Document sd2 = new BaseDocument(true, "text/xml"); //NOI18N
        XDMModel m2 = createXDMModel(sd2);
        m2.setPretty(true);
        m2.setIndentation(indentation);
        m2.sync();
        Node root2 = m2.getDocument();
        
        root2 = doPrettyPrint(m2, root2, root1);
        m2.flush();
                
        int firstChildPos1 = -1;
        Node firstChild1 = (Node) root1.getChildNodes().item(0);
        if(firstChild1 != null)
            firstChildPos1 = new PositionFinderVisitor().findPosition(
                    m1.getDocument(), firstChild1);        
        
        int firstChildPos2 = -1;
        Node firstChild2 = (Node) root2.getChildNodes().item(0);
        if(firstChild2 != null)
            firstChildPos2 = new PositionFinderVisitor().findPosition(
                    m2.getDocument(), firstChild2);  
        
        return (firstChildPos1==-1?doc:(sd1.getText(0, firstChildPos1) + 
                    sd2.getText(firstChildPos2, sd2.getLength() - firstChildPos2)));      
    }
    
    /*
     * compares 2 xml document contents using a criteria
     * 
     * @param firstDoc
     * @param secondDoc
     * @param type
     *   ComparisonCriteria.EQUAL - means Two documents are considered to be
     *      equal if they contain the same elements and attributes regardless
     *      of order.
     *   ComparisonCriteria.IDENTICAL -  means Two documents are considered to
     *      be "identical" if they contain the same elements and attributes in
     *      the same order.
     * filters -
     *      Whitespace diffs
     *      Namespace attribute diffs
     *      Namespace attribute prefix diffs
     *      Attribute whitespace diffs
     *
     * Use the next API (4 argument compareXML api) if you do not want to filter
     * all of the above 4 types of diffs
     */  
    public List<Difference> compareXML(String xml1, String xml2,
            XDMUtil.ComparisonCriteria criteria)
            throws Exception {
        return compareXML(xml1, xml2, criteria, true);
    }
    
    /*
     * compares 2 xml document contents using a criteria
     * 
     * @param firstDoc
     * @param secondDoc
     * @param type
     *   ComparisonCriteria.EQUAL - means Two documents are considered to be
     *      equal if they contain the same elements and attributes regardless
     *      of order.
     *   ComparisonCriteria.IDENTICAL -  means Two documents are considered to
     *      be "identical" if they contain the same elements and attributes in
     *      the same order.
     * @param ignoreWhiteSpace - filters whitespace diffs
     */    
    public List<Difference> compareXML(String firstDoc,  
            String secondDoc, ComparisonCriteria type, boolean filterWhiteSpace) 
    throws BadLocationException, IOException {
        Document sd1 = new BaseDocument(true, "text/xml"); //NOI18N
        XDMModel m1 = createXDMModel(sd1);
        sd1.remove(0, XML_PROLOG.length());
        sd1.insertString(0, firstDoc, null);
        m1.sync();
        fDoc = m1.getDocument();        
        
        Document sd2 = new BaseDocument(true, "text/xml"); //NOI18N
        sd2.getText(0, sd2.getLength());
        XDMModel m2 = createXDMModel(sd2);        
        sd2.remove(0, XML_PROLOG.length());
        sd2.insertString(0, secondDoc, null);
        m2.setPretty(true);
        m2.sync();
        sDoc = m2.getDocument();
        
        XDUDiffFinder dif = new XDUDiffFinder(createElementIdentity());
        List<Difference> diffs = dif.findDiff(m1.getDocument(), m2.getDocument());
        if(filterWhiteSpace)
            diffs = XDUDiffFinder.filterWhitespace(diffs);//filter whitespace diffs
        if(type == ComparisonCriteria.EQUAL) {//remove order change diffs
            List<Difference> filteredDiffs = new ArrayList<Difference>();
            for(Difference d:diffs) {
                if(d instanceof Change) {
                    Change c = (Change)d;
                    if(c.isPositionChanged())//node (element/text) pos change
                        if(!c.isTokenChanged() && !c.isAttributeChanged())
                            continue;
                    if(c.isAttributeChanged() && !c.isTokenChanged()) {//attr change only
                        List<Change.AttributeDiff> removeList = 
                                new ArrayList<Change.AttributeDiff>();
                        List<Change.AttributeDiff> attrChanges = c.getAttrChanges();
                        for(int i=0;i<attrChanges.size();i++) {
                            if(attrChanges.get(i) instanceof Change.AttributeChange) {
                                Change.AttributeChange ac = 
                                    (Change.AttributeChange) attrChanges.get(i);
                                if(ac.isPositionChanged() && !ac.isTokenChanged())//attr pos change only
                                    removeList.add(ac);
                            }
                        }
                        for(int i=0;i<removeList.size();i++)
                            c.removeAttrChanges(removeList.get(i));
                        if(c.getAttrChanges().size() == 0) //filter this diff
                            continue;
                    }
                    filteredDiffs.add(d);
                } else {
                    filteredDiffs.add(d);
                }
            }
            return filteredDiffs;
        }
        
        //remove pseudo attr position changes
        removePseudoAttrPosChanges(diffs);
        
        filterSchemaLocationDiffs(diffs);
        
        return diffs;
    }

    private ElementIdentity createElementIdentity() {
        //Establish DOM element identities
        ElementIdentity eID = new XDElementIdentity();
        //Following values are suitable for Schema and WSDL documents
        //these default values can be reset by eID.reset() call
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        return eID;
    }
    
    private XDMModel createXDMModel(Document sd)
    throws BadLocationException, IOException {
        return createXDMModel(sd, "");
    }
    
    private XDMModel createXDMModel(Document sd, String content) 
    throws BadLocationException, IOException {
        boolean foundXMLProlog = true;
        if(content.indexOf("<?xml") == -1) //insert xml prolog, otherwise XMLSyntaxParser will fail
            sd.insertString(0, XML_PROLOG+content, null);
        else
            sd.insertString(0, content, null);
        Lookup lookup = Lookups.singleton(sd);
        ModelSource ms = new ModelSource(lookup, true);
        XDMModel model = new XDMModel(ms);
        model.sync();
        return model;
    }
    
    
    private Node doPrettyPrint(XDMModel m2, Node n2, Node n1) {
        Node newNode = null;        
        NodeList childs1 = n1.getChildNodes();
        int count = 0;
        for(int i=0;i<childs1.getLength();i++) {
            n1 = (NodeImpl) childs1.item(i);
            newNode = ((NodeImpl)n1).cloneNode(true, false);           
            List<Node> ancestors = m2.add(n2, newNode, count++);
            n2 = ancestors.get(0);
        }
        List<Node> ancestors = new ArrayList<Node>();
        fixPrettyText(m2, n2, ancestors, "");
        n2 = ancestors.get(0);
        return n2;
    }    
    
    private void fixPrettyText(XDMModel m, final Node n, List<Node> ancestors, String indent) {
        Node parent = n;
        int index = m.getIndentation().length();
        NodeList childs = parent.getChildNodes();
        List<Node> visitList = new ArrayList<Node>();
        for(int i=0;i<childs.getLength();i++) {
            Node child = (Node) childs.item(i);
            if(checkPrettyText(child)) {
                Text txt = (Text) ((NodeImpl)child).cloneNode(true);
                if(i < childs.getLength()-1 || ancestors.size() == 0)
                    txt.setText("\n"+indent);
                else {
                    String lastTextIndent = "\n";
                    if(m.getIndentation().length() < indent.length() )
                        lastTextIndent += indent.substring(m.getIndentation().length());
                    txt.setText(lastTextIndent);
                }
                List<Node> ancestors2 = m.modify(child, txt);
                parent = ancestors2.get(0);
            }
            else if(childs.item(i) instanceof Element)
                visitList.add((Node)childs.item(i));
        }
        ancestors.add(parent);
        for(int i=0;i<visitList.size();i++) {
            fixPrettyText(m, (Node)visitList.get((i)), ancestors, indent+m.getIndentation());
        }
        visitList.clear(); //no need to keep it beyond here
    }    

    public static boolean checkPrettyText(Node txt) {
        if (txt instanceof Text) {
            if ((((NodeImpl)txt).getTokens().size() == 1) &&
                    isWhitespaceOnly(((NodeImpl)txt).getTokens().get(0).getValue())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isWhitespaceOnly(String tn) {
        return XDUDiffFinder.isPossibleWhiteSpace(tn) &&
                tn.trim().length() == 0;
    }
    

    public static int findPosition(final Node n) {
        return new PositionFinderVisitor().findPosition(
                (Node)n.getOwnerDocument(), n);
    }
    
    /*
     * filters or removes diffs that are attr position changes
     */
    public static void removePseudoAttrPosChanges(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter attibute position changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
                    int delCount = 0;
                    int addCount = 0;
                    for(Change.AttributeDiff attrdif:attrdiffs) {
                        if(attrdif instanceof Change.AttributeDelete)
                            delCount++;
                        else if(attrdif instanceof Change.AttributeAdd)
                            addCount++;
                        else if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(attrChange.isPositionChanged() && !attrChange.isTokenChanged()) {
                                if((attrChange.getOldAttributePosition() - delCount + addCount) == 
                                        attrChange.getNewAttributePosition())
                                    removeAttrs.add(attrdif);
                            }
                        }
                    }
                    for(Change.AttributeDiff attrdif:removeAttrs) {
                        c.removeAttrChanges(attrdif);
                    }
                    if(size > 0 && c.getAttrChanges().size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }

        diffs.removeAll(removeDiffs);
    }
    
    /*
     * filters or removes diffs that are attr position changes
     */
    public static void filterAttributeOrderChange(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter attibute position changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() && !c.isTokenChanged()) {
                    List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
                    int size = attrdiffs.size();
                    List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
                    for(Change.AttributeDiff attrdif:attrdiffs) {
                        if(attrdif instanceof Change.AttributeChange) {
                            Change.AttributeChange attrChange =
                                    (AttributeChange) attrdif;
                            if(attrChange.isPositionChanged() && !attrChange.isTokenChanged())
                                removeAttrs.add(attrdif);
                        }
                    }
                    for(Change.AttributeDiff attrdif:removeAttrs) {
                        c.removeAttrChanges(attrdif);
                    }
                    if(size > 0 && c.getAttrChanges().size() == 0)
                        removeDiffs.add(dif);
                }
            }
        }

        diffs.removeAll(removeDiffs);
    }
        
    /*
     * filters or removes diffs that are schemalocation attr "xsi:schemaLocation='some url'"
     */
    public static void filterSchemaLocationDiffs(final List<Difference> diffs) {
        List<Difference> removeDiffs = new ArrayList<Difference>();
        for(Difference dif:diffs) {
            if(dif instanceof Change) {
                Change c = (Change)dif;
                //filter namespace attibute changes only
                if(c.isAttributeChanged() && !c.isPositionChanged() &&
                        !c.isTokenChanged() && removeSchemaLocationAttrDiffs(c)) {
                    removeDiffs.add(dif);
                }
            }
        }

        diffs.removeAll(removeDiffs);
    }
    
    /*
     * removes attr diffs that are ns attr "prefix:schemaLocation='some url'"
     */
    public static boolean removeSchemaLocationAttrDiffs(Change c) {
        List<Change.AttributeDiff> attrdiffs = c.getAttrChanges();
        int size = attrdiffs.size();
        List<Change.AttributeDiff> removeAttrs = new ArrayList<Change.AttributeDiff>();
        for(Change.AttributeDiff attrdif:attrdiffs) {
            Attribute oldAttr = attrdif.getOldAttribute();
            Attribute newAttr = attrdif.getNewAttribute();
            if(oldAttr != null && oldAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
            else if(newAttr != null && newAttr.getName().endsWith(SCHEMA_LOCATION))
                removeAttrs.add(attrdif);
        }
        for(Change.AttributeDiff attrdif:removeAttrs) {
            c.removeAttrChanges(attrdif);
        }
        if(size > 0 && attrdiffs.size() == 0)
            return true;
        return false;
    }
    
    public class XDElementIdentity extends DefaultElementIdentity {

        /**
         * Creates a new instance of DefaultElementIdentity
         */
        public XDElementIdentity() {
            super();
        }

        protected boolean compareElement(org.w3c.dom.Element n1, org.w3c.dom.Element n2, org.w3c.dom.Node parent1, org.w3c.dom.Document doc1, org.w3c.dom.Document doc2) {
            String qName1 = n1.getLocalName();
            String qName2 = n2.getLocalName();
            String ns1 = ((Node)n1).getNamespaceURI((org.netbeans.modules.xml.xdm.nodes.Document) doc1);
            String ns2 = ((Node)n2).getNamespaceURI((org.netbeans.modules.xml.xdm.nodes.Document) doc2);

            if ( qName1.intern() !=  qName2.intern() )
                return false;
            if(!((ns1 == null || ns1.equals("")) && (ns2 == null || ns2.equals("")))) {//can determine ns
                if ( !(ns1 == null && ns2 == null) &&
                        !(ns1 != null && ns2 != null && ns1.intern() == ns2.intern() ) )
                    return false;
            }

            if(parent1 == doc1) return true; //if root no need to compare other identifiers

            return compareAttr( n1, n2);
        }
    }
    
    public class XDUDiffFinder extends DiffFinder {

        public XDUDiffFinder(ElementIdentity eID) {
            super(eID);
        }

        public List<Change.Type> checkChange(final Node p1, final Node p2) {
            List<Change.Type> changes = new ArrayList<Change.Type>();
            if (p1 instanceof Element && p2 instanceof Element) {
                if ( ! checkAttributesEqual((Element)p1, (Element)p2)) {
                    changes.add(Change.Type.ATTRIBUTE);
                }
            }
            return changes;
        }

        protected boolean checkAttributesEqual(final Element p1, final Element p2) {
            if (p1 == null || p2 == null) return false;
            NamedNodeMap nm1 = p1.getAttributes();
            NamedNodeMap nm2 = p2.getAttributes();
            //if( nm1.getLength() != nm2.getLength() ) return false;

            for ( int i = 0; i < nm1.getLength(); i++ ) {
                Node attr1 = (Node) nm1.item(i);
                if(attr1.getNodeName().startsWith("xmlns"))
                    continue;
                Node attr2 = (Node) nm2.getNamedItem(attr1.getNodeName());
                if ( attr2 == null ) return false;
                if(nm2.item(i) != attr2) return false;
                if(!attr1.getNodeValue().equals(attr2.getNodeValue()))
                    return false;
            }
            return true;
        } 

        protected boolean compareTextByValue(Text n1, Text n2) {
            return n1.getNodeValue().equals(n2.getNodeValue());
        }    
    }
    
    static org.netbeans.modules.xml.xdm.nodes.Document fDoc;
    static org.netbeans.modules.xml.xdm.nodes.Document sDoc;
    
    public static final String NS_PREFIX = "xmlns";
    public static final String SCHEMA_LOCATION = "schemaLocation";    
    public static final String XML_PROLOG = "<?xml version=\"1.0\"?>\n";
}
