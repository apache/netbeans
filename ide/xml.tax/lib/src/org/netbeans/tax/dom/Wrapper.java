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

package org.netbeans.tax.dom;

import org.w3c.dom.*;
import org.netbeans.tax.*;

/**
 *
 * @author  Petr Kuzel
 */
public class Wrapper {


    public static Attr wrap(TreeAttribute attr) {
        return new AttrImpl(attr);
    }

    public static Element wrap(TreeElement element) {
        return new ElementImpl(element);
    }

    public static Text wrap(TreeText text) {
        return new TextImpl(text);
    }

    public static Document wrap(TreeDocumentRoot document) {
        return new DocumentImpl(document);
    }
    
    public static DocumentType wrap(TreeDocumentType documentType) {
        return new DocumentTypeImpl(documentType);
    }
    
    public static Comment wrap(TreeComment comment) {
        return new CommentImpl(comment);
    }
    
    static NodeList wrap(TreeObjectList list) {
        return new NodeListImpl(list);
    }
    
    static NamedNodeMap wrap(TreeNamedObjectMap map) {
        return new NamedNodeMapImpl(map);
    }
    
    public static Node wrap(TreeObject object) {
        if (object == null) return null;
        if (object instanceof TreeAttribute) {
            return wrap((TreeAttribute) object);
        } else if (object instanceof TreeElement) {
            return wrap((TreeElement) object);            
        } else if (object instanceof TreeText) {
            return wrap((TreeText) object);
        } else if (object instanceof TreeDocumentRoot) {
            return wrap((TreeDocumentRoot) object);
        } else if (object instanceof TreeDocumentType) {
            return wrap((TreeDocumentType) object);
        } else if (object instanceof TreeComment) {
            return wrap((TreeComment) object);
        } else {
            throw new RuntimeException("Cannot wrap: " + object.getClass());
        }
    }
}
