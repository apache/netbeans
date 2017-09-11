/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.api.editor.document;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;

/**
 * Represents a line-oriented Document. The Document implementation
 * is able to find Element for paragraph (line) that contains the specified
 * position and also transform the position to a visual column index (takes
 * tab expansion settings into account).
 * <br/>
 * To obtain LineDocument instance the {@link LineDocumentUtils#as(javax.swing.text.Document, java.lang.Class) }
 * or {@link LineDocumentUtils#asRequired(javax.swing.text.Document, java.lang.Class) } may be used:
 * <code>
 * <pre>
 *   Document doc = ...
 *   LineDocument lineDoc = LineDocumentUtils.asRequired(doc, LineDocument.class);
 * </pre>
 * </code>
 *
 * @author sdedic
 */
public interface LineDocument extends Document {
    /**
     * Returns an element that represent a line which contains position 'pos'.
     * @param pos position to find
     * @return line represented as Element
     */
    public Element getParagraphElement(int pos);
    
    
    /**
     * Creates s Swing position that maintains a bias to the offset it 
     * is anchored at. 
     * @param offset offset for the position
     * @param bias backward/forward bias
     * @return Position instance
     * @throws BadLocationException if offset points outside document content
     */
    public Position createPosition(int offset, Position.Bias bias) throws BadLocationException;

}
