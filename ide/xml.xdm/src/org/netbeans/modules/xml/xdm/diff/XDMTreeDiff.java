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
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.w3c.dom.NodeList;

/*
 * This class is the Facade to find diff between 2 XML documents as well
 * as merge the changes detected back to the original XDM model
 *
 * @author Ayub Khan
 */
public class XDMTreeDiff {
    
    private ElementIdentity eID;
    
    /** Creates a new instance of XDMTreeDiff */
    public XDMTreeDiff(ElementIdentity eID) {
        this.eID = eID;
    }
    
    public List<Difference> performDiff(Document doc1, Document doc2) {
//		System.out.println("doc1==>");
//		printDocument(doc1);
//		System.out.println("doc2==>");
//		printDocument(doc2);
        List<Difference> deList = new DiffFinder(eID).findDiff(doc1, doc2);
        return deList;
    }
    
    public List<Difference> performDiff(XDMModel model, Document doc2) {
        return performDiff(model.getDocument(), doc2);
    }
    
    public List<Difference> performDiffAndMutate(XDMModel model, Document doc2) {
        List<Difference> deList = performDiff(model.getDocument(), doc2);
        model.mergeDiff(deList);
        return deList;
    }
    
    public static void printDocument(Node node) {
        String name = node.getNodeName();
        if(node instanceof Element && node.getAttributes().getLength() > 0)
            name+=node.getAttributes().item(0).getNodeValue();
        System.out.println("node: "+name+" id:"+node.getId());
        NodeList childs = node.getChildNodes();
        for(int i=0;i<childs.getLength();i++)
            printDocument((Node) childs.item(i));
    }
}
