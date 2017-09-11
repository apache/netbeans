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


package org.netbeans.modules.editor.structure.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;


/**
 * An utilitity class containing various methods simplifying work with the DocumentElements.
 *
 * @author Marek Fukala
 * @version 1.0
 */
public final class DocumentModelUtils {

    /** Returns all model's elements. 
     * @return An array with document elements.
     */
    public static DocumentElement[] elements(DocumentModel model) {
        return model.elements();
    }
    
    
    /** Returns and element starting on the specified position with specied name and type.
     * If any of these conditions is not true (there isn't any element on the offset, or
     * there is an element, but the name or the type does't match) it returns null.
     *
     * @param startOffset - offset of the searched element
     * @param name - name of the element
     * @param type - type of the element
     *
     * @return the element or null, when there is not such an element.
     */
    public static DocumentElement findElement(DocumentModel model, int startOffset, String name, String type) throws BadLocationException {
        List els = model.getDocumentElements(startOffset);
        Iterator i = els.iterator();
        while(i.hasNext()) {
            DocumentElement de = (DocumentElement)i.next();
            if(de.getName().equals(name) && de.getType().equals(type)) return de;
        }
        return null; //no such element found
    }
    
    /** Returns a list of all document elements which are descendants of the givent DocumentElement. 
     *
     * @return list of document elements descendants or empty list if there isn't any descendant.
     */
    
    public static List<DocumentElement> getDescendants(DocumentElement de) {
        ArrayList<DocumentElement> desc = new ArrayList<DocumentElement>();
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            DocumentElement child = (DocumentElement)children.next();
            desc.add(child);
            desc.addAll(getDescendants(child));
        }
        return desc;
    }
    
    /** Dumps a tree like view of document element's children. 
     *  To see a hierarchical view of the entire model use dumpElementStructure(model.getRootElement());
     */
    public static void dumpElementStructure(DocumentElement de) {
        System.out.println("-------- ELEMENTS STRUCTURE --------");
        dumpElementStructure(de, 0);
    }
    
    private static void dumpElementStructure(DocumentElement de, int level) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < level; i++) {
            sb.append(' ');
        }
        String text = de.toString();
        sb.append(text);
        System.out.println(sb.toString());
        
        Iterator children = de.getChildren().iterator();
        while(children.hasNext()) {
            dumpElementStructure((DocumentElement)children.next(), level + 4);
        }
    }
    
    /** Dumps a list of existing elements in the model.
     *  This method is mainly used for testing purposes.*/
    public static void dumpModelElements(DocumentModel model) {
        model.debugElements();
    }
    
}
