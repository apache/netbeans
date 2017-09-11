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
