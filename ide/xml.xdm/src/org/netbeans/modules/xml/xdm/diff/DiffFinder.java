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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * This class is used by XDMTreeDiff (Both Sync and Undo/Redo) to find difference
 * between 2 XML and returns {@code List<DiffEvent>}
 *
 * @author Ayub Khan
 */
public class DiffFinder {
    protected DiffFinder() {
    }
    
    public DiffFinder(ElementIdentity eID) {
        this.eID = eID;
    }
    
        /*
         * findDiff between 2 XML documents
         *
         * @param d1 - an XML document
         * @param d2 - an XML document
         */
    public List<Difference> findDiff(Document d1, Document d2) {
        this.oldDoc = d1;
        this.newDoc = d2;
        
        List<Difference> deList = new ArrayList<Difference>();
        List<Change.Type> changes = checkChange( d1, d2, 0, 0);
        if (changes.size() > 0) {
            markChange( new ArrayList<Node>(), d1, d2, 0, 0, changes,
                        new ArrayList<Node>(), deList );
        }
        
        List<Node> ancestors1 = new ArrayList<Node>();
        List<Node> ancestors2 = new ArrayList<Node>();
        ancestors1.add(oldDoc);
        ancestors2.add(newDoc);
        compareChildren(ancestors1, ancestors2, deList);
        
        //remove any (sequential) position change events
        if( deList.size() > 0 ) {
            deList = findOptimized(deList);
        }
        
        return deList;
    }
    
    protected void compareChildren(List<Node> ancestors1, 
            List<Node> ancestors2, List<Difference> deList) {
        Node parent1 = ancestors1.get(0);
        Node parent2 = ancestors2.get(0);
        NodeList p1ChildNodes = parent1.getChildNodes();
        NodeList p2ChildNodes = parent2.getChildNodes();
        
        if ( p1ChildNodes == NodeListImpl.EMPTY &&
                p2ChildNodes == NodeListImpl.EMPTY )
            return;
        
        cInfo1 = new ChildInfo(parent1);
        cInfo2 = new ChildInfo(parent2);
        List<Node> p2ChildList = getChildList(parent2);
        
        List<int[]> pairList = new ArrayList<int[]>();
        List<Node> foundList = new ArrayList<Node>();
        if ( p1ChildNodes != null ) {
            int length = p1ChildNodes.getLength();
            for ( int i = 0; i < length; i++ ) {
                Node child = (Node) p1ChildNodes.item(i);
                Node foundNode = null;
                if ( child instanceof Element ) {
                    foundNode = findMatch( (Element)child, p2ChildList, parent1);
                } else if ( child instanceof Text ) {
                    foundNode = findMatch( (Text)child, p2ChildList);
                }
                if( foundNode == null ) {
                    List<Node> path1 = copy(ancestors1);
                    List<Node> path2 = copy(ancestors2);
                    markDelete(path1, child, i, cInfo1.getSiblingBefore( child ),
                            path2, deList );
                } else {
                    cInfo2.addMatchNode( foundNode, child);
                    foundList.add( foundNode );
                    p2ChildList.remove( foundNode );
                    int[] pair = new int[] {i,cInfo2.getIndex(foundNode)};
                    pairList.add( pair );
                }
            }
            
            for ( int i = 0; i < p2ChildList.size() ; i++ ) {
                Node child = (Node) p2ChildList.get(i);
                SiblingInfo sInfo = new SiblingInfo(child, p2ChildNodes, foundList);
                Node originalSiblingBefore = null;
                if (sInfo.getNode() != null) {
                    originalSiblingBefore = cInfo2.getMatchNode(sInfo.getNode());
                }
                
                int absolutePos = cInfo2.getIndex( child );
                markAdd( copy(ancestors1), child, absolutePos, sInfo.getPosition(),
                        originalSiblingBefore, copy(ancestors2), deList );
            }
            
            //sort pairs
            pairList.sort(new PairComparator());
            
            for ( int i=0; i < pairList.size(); i++ ) {
                int[] pair = pairList.get(i);
                int px1 = pair[0];
                int px2 = pair[1];
                Node p1 = (Node) parent1.getChildNodes().item(px1);
                Node p2 = (Node) parent2.getChildNodes().item(px2);
                
                int tp1 = cInfo1.getPosition(p1);
                int tp2 = cInfo2.getPosition(p2);
                
                //check node content and position change
                List<Change.Type> changes = checkChange( p1, p2, tp1, tp2);
                if (changes.size() > 0) {
                    markChange( copy(ancestors1), p1, p2, px1, px2, changes, 
                            copy(ancestors2), deList );
                }
            }
            //we are done with the maps, clear
            cInfo1 = null;
            cInfo2 = null;
            
            for ( int i=0; i < pairList.size(); i++ ) {
                int[] pair = pairList.get(i);
                int px1 = pair[0];
                int px2 = pair[1];
                Node p1 = (Node) parent1.getChildNodes().item(px1);
                Node p2 = (Node) parent2.getChildNodes().item(px2);
                if ( p1 instanceof Element ) {
                    ancestors1.add(0, p1);
                    ancestors2.add(0, p2);
                    //Since p1 and p2 are similar nodes, now compare their childrens
                    compareChildren( ancestors1, ancestors2, deList );
                    ancestors1.remove(0);
                    ancestors2.remove(0);
                }
            }
        }
    }
    
    private List<Node> copy(List<Node> l) {
        return new ArrayList<Node>(l);
    }
    
    public class PairComparator implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            int[] pair1 = (int[]) o1;
            int[] pair2 = (int[]) o2;
            int px2_1 = pair1[1];
            int px2_2 = pair2[1];
            if(px2_1 < px2_2)
                return -1;
            else if(px2_1 > px2_2)
                return +1;
            else
                return 0;
        }
    }
    
    public static NodeInfo.NodeType getNodeType(final Node child) 
    throws DOMException {
        NodeInfo.NodeType nodeType = NodeInfo.NodeType.ELEMENT;
        if ( child instanceof Text )
            if ( isWhiteSpaceOnly((Text) child) )
                nodeType = NodeInfo.NodeType.WHITE_SPACE;
            else
                nodeType = NodeInfo.NodeType.TEXT;
        else if ( child instanceof Attribute )
            nodeType = NodeInfo.NodeType.ATTRIBUTE;
        return nodeType;
    }
    
    protected List<Change.Type> checkChange(final Node n1, final Node n2,
            final int p1, final int p2) {
        List<Change.Type> changes = checkChange(n1, n2);
        if(p1 != p2)
            changes.add(Change.Type.POSITION);
        return changes;
    }
    
    protected List<Change.Type> checkChange(final Node p1, final Node p2) {
        List<Change.Type> changes = new ArrayList<Change.Type>();
        if ( ! checkTokensEqual(p1, p2)) {
            changes.add(Change.Type.TOKEN);
        }
        if (p1 instanceof Element && p2 instanceof Element) {
            if ( ! checkAttributesEqual((Element)p1, (Element)p2)) {
                changes.add(Change.Type.ATTRIBUTE);
            }
        } else if (! p1.getClass().isAssignableFrom(p2.getClass()) ||
                ! p2.getClass().isAssignableFrom(p1.getClass())) {
            changes.add(Change.Type.UNKNOWN);
        }
        return changes;
    }
    
    protected boolean checkTokensEqual(final Node p1, final Node p2) {
        if (p1 instanceof NodeImpl && p2 instanceof NodeImpl) {
            List<Token> t1List = ((NodeImpl)p1).getTokens();
            List<Token> t2List = ((NodeImpl)p2).getTokens();
            return compareTokenEquals( t1List, t2List );
        }
        return false;
    }
    
    protected boolean checkAttributesEqual(final Element p1, final Element p2) {
        if (p1 == null || p2 == null) return false;
        NamedNodeMap nm1 = p1.getAttributes();
        NamedNodeMap nm2 = p2.getAttributes();
        if( nm1.getLength() != nm2.getLength() ) return false;
        
        for ( int i = 0; i < nm1.getLength(); i++ ) {
            Node attr2 = (Node) nm2.getNamedItem(nm1.item(i).getNodeName());
            if ( attr2 == null ) return false;
            if(nm2.item(i) != attr2) return false;
            List<Token> t1List = ((NodeImpl)nm1.item(i)).getTokens();            
            List<Token> t2List = ( (NodeImpl) attr2 ).getTokens();
            boolean status = compareTokenEquals( t1List, t2List );
            if ( !status ) return false;
        }
        return true;
    }
    
    protected boolean compareTokenEquals(List<Token> t1List, 
            List<Token> t2List) {
        if( t1List.size() != t2List.size() )
            return false;
        
        //compare element tokens
        for ( int i=0; i<t1List.size(); i++ ) {
            Token t1 = t1List.get( i );
            Token t2 = t2List.get( i );
            if ( t1.getValue().intern() !=  t2.getValue().intern() )
                return false;
        }
        
        return true;
    }
    
    protected Node findMatch(Element child, List<Node> childNodes, 
            org.w3c.dom.Node parent1) {
        for (Node otherChild : childNodes ) {
            if ( otherChild instanceof Element ) {
                if ( ((DefaultElementIdentity)eID).compareElement( 
                        child, (Element) otherChild , parent1, this.oldDoc, this.newDoc) )
                    return otherChild;
            }
        }
        return null;
    }
    
    protected Node findMatch(Text child, List<Node> childNodes) {
        for (Node otherChild:childNodes) {
            if ( otherChild instanceof Text &&
                    compareText( child, (Text) otherChild ) )
                return otherChild;
        }
        return null;
    }
    
    protected Difference createAddEvent(List<Node> ancestors1, Node n, 
            int absolutePos,
            List<Node> ancestors2) {
        assert n != null : "add node null";
        return new Add( getNodeType( n ), ancestors1, ancestors2, n, absolutePos);
    }
    
    protected Difference createDeleteEvent(List<Node> ancestors1, Node n, int pos,
            List<Node> ancestors2) {
        assert n != null : "delete node null";
        return new Delete( getNodeType( n ), ancestors1, ancestors2, n, pos) ;
    }
    
    protected Difference createChangeEvent(List<Node> ancestors1, Node n1, Node n2,
            int n1Pos, int n2Pos, List<Change.Type> changes, List<Node> ancestors2) {
        assert n1 != null && n2 != null : "change nodes null";
        if(n1 instanceof Element)
            assert n1.getLocalName().equals(n2.getLocalName());
        Change de = new Change(getNodeType(n1), ancestors1, ancestors2, n1,
                n2, n1Pos, n2Pos, changes);

        if (de.getNewNodeInfo().getNewAncestors().size() > 0) {
            assert de.getNewNodeInfo().getNode().getId() != 
                    de.getNewNodeInfo().getNewAncestors().get(0).getId();
        }
        
        return de;
    }
    
    protected void markAdd(List<Node> ancestors1, Node n, int absolutePos, 
            int posFromSibling,
            Node siblingBefore, List<Node> ancestors2, List<Difference> deList) {
        deList.add( createAddEvent( ancestors1, n, absolutePos, ancestors2 ) );
    }
    
    protected void markDelete(List<Node> ancestors1, Node n, int pos, Node siblingBefore,
            List<Node> ancestors2, List<Difference> deList) {
        deList.add( createDeleteEvent( ancestors1, n, pos, ancestors2 ));
    }
    
    protected void markChange(List<Node> ancestors1, Node n1, Node n2, int n1Pos, int n2Pos,
            List<Change.Type> changes, List<Node> ancestors2, List<Difference> deList) {
        deList.add( createChangeEvent( ancestors1, n1, n2, n1Pos, n2Pos, changes, ancestors2 ) );
    }
        
    public static List<Difference> filterWhitespace(List<Difference> deList) {
        List<Difference> returnList = new ArrayList<Difference>();
        for ( Difference de:deList ) {
            NodeInfo.NodeType nodeType = de.getNodeType();
            if ( de.getNodeType() != NodeInfo.NodeType.WHITE_SPACE )
                returnList.add(de);
        }
        return returnList;
    }
    
    public static List<Node> getPathToRoot(Node node) {
        assert node.getOwnerDocument() != null;
        List<Node> pathToRoot = new PathFromRootVisitor().findPath(
                node.getOwnerDocument(), node);
        //assert pathToRoot != null && pathToRoot.size() > 0;
        return pathToRoot;
    }
    
    public List<Difference> findOptimized(List<Difference> deList) {
        if(deList == null || deList.isEmpty()) return Collections.emptyList();
        List<Difference> optimizedList = new ArrayList<Difference>();
        HashMap<Node, List<Difference>> deMap = new HashMap<Node, List<Difference>>();
        List<Node> parentList = new ArrayList<Node>();
        for ( Difference de: deList ) {
            Node parent = de.getOldNodeInfo().getParent();
            List<Difference> childDeList = deMap.get(parent);
            if ( childDeList == null ) {
                parentList.add(parent);
                childDeList = new ArrayList<Difference>();
                deMap.put( parent, childDeList );
            }
            childDeList.add( de );
        }
        
        for (Node parent:parentList) {
            List<Difference> childDeList = deMap.get(parent);
            childDeList.sort(new PairComparator2());
            HashMap<Difference, Integer> oldPosMap = new HashMap<Difference, Integer>();
            for ( int i=0; i < childDeList.size(); i++ ) {
                Difference de = childDeList.get(i);
                modifyPositionFromIndex( i+1, childDeList, de , oldPosMap);
            }
            
            //Now remove position change events
            for ( int i=0; i < childDeList.size(); i++ ) {
                Difference de = childDeList.get(i);
                Integer oldPos = oldPosMap.get(de);
                int px1 = oldPos!=null?oldPos.intValue():
                    de.getOldNodeInfo().getPosition();
                int px2 = de.getNewNodeInfo().getPosition();
                if(de instanceof Change &&
                        ((Change)de).isPositionChanged()) {
                    if(px1 == px2 && px1 != -1)
                        ((Change)de).setPositionChanged(false);//reset position change
                    if(((Change)de).isValid())
                        optimizedList.add(de);
                } else
                    optimizedList.add(de);
            }
        }
        return optimizedList;
    }
    
    public class PairComparator2 implements java.util.Comparator {
        public int compare(Object o1, Object o2) {
            Difference de1 = (Difference) o1;
            Difference de2 = (Difference) o2;
            int px2_1 = de1.getNewNodeInfo().getPosition();
            int px2_2 = de2.getNewNodeInfo().getPosition();
            if(px2_1 < px2_2)
                return -1;
            else if(px2_1 > px2_2)
                return +1;
            else
                return 0;
        }
    }
    
    protected void modifyPositionFromIndex(int index,
            List<Difference> childDeList, Difference de,
            HashMap<Difference, Integer> oldPosMap) {
//        int x = de.getOldNodeInfo().getPosition();
        Integer oldx = oldPosMap.get(de);
        int x = oldx!=null?oldx.intValue():
            de.getOldNodeInfo().getPosition();
        int y = de.getNewNodeInfo().getPosition();
        for ( int i=index; i < childDeList.size(); i++ ) {
            Difference cde = childDeList.get(i);
            Integer oldp1 = oldPosMap.get(cde);
            int p1 = oldp1!=null?oldp1.intValue():
                cde.getOldNodeInfo().getPosition();
            int p2 = cde.getNewNodeInfo().getPosition();
  
            if( de instanceof Add &&
                    x==-1 && y>=0 && y<=p1) {
                oldPosMap.put(cde, p1+1);
            } else if( de instanceof Delete &&
                    x>=0 && y==-1 && x<=p1) {
                oldPosMap.put(cde, p1-1);
            } else if( de instanceof Change && x!=y) {
                if(x>p1 && y<=p1)
                    oldPosMap.put(cde, p1+1);
                else if(y>p1 && x<=p1)
                    oldPosMap.put(cde, p1-1);
            }
        }
    }
    
    public static boolean isPossibleWhiteSpace(String text) {
        return text.length() > 0 &&
                Character.isWhitespace(text.charAt(0)) &&
                Character.isWhitespace(text.charAt(text.length()-1));
    }
    
    public static boolean isWhiteSpaceOnly(Text txt) {
        String tn = "";
        if(((NodeImpl)txt).getTokens().size() == 1)
            tn = ((NodeImpl)txt).getTokens().get(0).getValue();
        else
            tn = txt.getNodeValue();
        return isPossibleWhiteSpace(tn) &&
                tn.trim().length() == 0;
    }
    
    protected boolean compareText(Text n1, Text n2) {
        if( isWhiteSpaceOnly(n1) && isWhiteSpaceOnly(n2))
            return compareWhiteSpaces( n1, n2 );
        else
            return compareTextByValue( n1, n2 );
    }
    
    protected boolean compareWhiteSpaces(Text n1, Text n2) {
        Node nodeBefore1 = cInfo1.getSiblingBefore( n1 );
        Node nodeBefore2 = cInfo2.getSiblingBefore( n2 );
        boolean siblingCompare = false;
        if( nodeBefore1 == null && nodeBefore2 == null )
            siblingCompare = true;
        else if ( nodeBefore1 instanceof Element && 
                nodeBefore2 instanceof Element ) {
            if( cInfo2.getMatchNode(nodeBefore2) == nodeBefore1 ||
                    eID.compareElement( (Element) nodeBefore1, 
                    (Element) nodeBefore2, this.oldDoc, this.newDoc ) )
                siblingCompare = true;
        } else if ( nodeBefore1 instanceof Text && nodeBefore2 instanceof Text &&
                nodeBefore1.getNodeValue().intern() == 
                nodeBefore2.getNodeValue().intern() )
            siblingCompare = true;
        
        if ( siblingCompare )
            return compareTextByValue( n1, n2 );
        
        return false;
    }
    
    protected boolean compareTextByValue(Text n1, Text n2) {
        return n1.getNodeValue().intern() == n2.getNodeValue().intern();
    }
    
    protected List<Node> getChildList(Node parent) {
        NodeList childs = parent.getChildNodes();
        List<Node> childList = new ArrayList<Node>(childs.getLength());
        for ( int i = 0; i < childs.getLength() ; i++ ) {
            Node child = (Node) childs.item(i);
            childList.add( child );
        }
        return childList;
    }
    
    static class ChildInfo {
        
        public ChildInfo(Node parent) {
            NodeList childNodes = parent.getChildNodes();
            compareNodeMap = new HashMap<Node, Node>();
            siblingBeforeMap = new HashMap<Node, Node>(childNodes.getLength());
            posMap = new HashMap<Node, int[]>(childNodes.getLength());
            HashMap<Class, Integer> posCounter = new HashMap<Class, Integer>(7);
            Node siblingBefore = null;
            for ( int i = 0; i < childNodes.getLength() ; i++ ) {
                Node child = (Node) childNodes.item(i);
                siblingBeforeMap.put( child, siblingBefore );
                siblingBefore = child;
                // TODO adding Comment and CData makes more
                // buckets and thus the changes are relative to
                // the type of child. If comment, cdata, and
                // text should be considered as one then the
                // a method needs to be invoked to determine
                // the correct bucket to put the count into
                Class bucket = getBucket(child);
                Integer count = posCounter.get(bucket);
                if(count == null)
                    posCounter.put(bucket, -1);
                int newCount = posCounter.get(bucket) + 1;
                posCounter.put(bucket, newCount);
                int[] pos = new int[] {i,newCount};
                posMap.put( child, pos );
            }
        }
        
        /**
         * @return correct class to determine relative position
         */
        private Class getBucket(Node child) {
            return child instanceof Text ? Text.class:child.getClass();
        }
        
        public Node getSiblingBefore(Node n) {
            return siblingBeforeMap.get(n);
        }
        
        public int getIndex(Node n) {
            return posMap.get(n)[0];
        }
        
        public int getPosition(Node n) {
            return posMap.get(n)[1];
        }
        
        public Node getMatchNode(Node n) {
            return compareNodeMap.get(n);
        }
        
        public void addMatchNode(Node n1, Node n2) {
            compareNodeMap.put(n1, n2);
        }
        
        public void clear() {
            compareNodeMap.clear();
            siblingBeforeMap.clear();
            posMap.clear();
        }
        
        HashMap<Node, int[]> posMap;
        HashMap<Node, Node>  siblingBeforeMap;
        HashMap<Node, Node> compareNodeMap;
    }
    
    static class SiblingInfo {
        
        public SiblingInfo(Node child, NodeList p2ChildNodes, List<Node> foundList) {
            for ( int j=0; j < p2ChildNodes.getLength(); j++ ) {
                Node node = (Node) p2ChildNodes.item(j);
                if ( node == child ) {
                    if ( j-1 >= 0 ) {
                        for ( int k=j-1; k >= 0 ; k-- ) {//go backwards and find 
                            //a node that hasn't changed its pos
                            if ( p2ChildNodes.item( k ) instanceof Element &&
                                    foundList.contains( p2ChildNodes.item( k ) ) ) {
                                siblingBefore = (Node) p2ChildNodes.item( k ) ;
                                relativePos = j-k;
                                break;
                            }
                        }
                    }
                    if ( siblingBefore != null )
                        break;
                }
            }
        }
        
        public Node getNode() {
            return siblingBefore;
        }
        
        public int getPosition() {
            return relativePos;
        }
        
        Node siblingBefore;
        int relativePos = 0;
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private ElementIdentity eID;
    
    private ChildInfo cInfo1;
    
    private ChildInfo cInfo2;
    
    private Document oldDoc;
    
    private Document newDoc;
}
