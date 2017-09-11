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
