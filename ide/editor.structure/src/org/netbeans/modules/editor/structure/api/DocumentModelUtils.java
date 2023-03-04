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
