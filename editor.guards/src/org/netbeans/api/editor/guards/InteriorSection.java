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
package org.netbeans.api.editor.guards;

import org.netbeans.modules.editor.guards.OffsetPosition;
import javax.swing.text.Position;
import org.netbeans.modules.editor.guards.InteriorSectionImpl;

/**
 * Represents an advanced guarded section.
 * It consists of three pieces: a header, body, and footer.
 * The header and footer are guarded but the body is not.
 */
public final class InteriorSection extends GuardedSection {
    /**
     * Creates new section.
     * @param name Name of the new section.
     */
    InteriorSection(InteriorSectionImpl impl) {
        super(impl);
    }

    InteriorSection(GuardedSection delegate, int offset) {
        super(delegate, offset);
    }
    
    /**
     * Sets the text of the body.
     * @param text the new text
     */
    public void setBody(String text) {
        if (getImpl() == null) throw new IllegalStateException();
        getImpl().setBody(text);
    }
    
    /**
     * Returns the contents of the body part of the section. If the
     * section is invalid the method returns null.
     * @return contents of the body or null, if the section is not valid.
     */
    public String getBody() {
        if (getImpl() == null) return getDelegate().getBody();
        return getImpl().getBody();
    }

    /**
     * Sets the text of the header.
     * @param text the new text
     */
    public void setHeader(String text) {
        if (getImpl() == null) throw new IllegalStateException();
        getImpl().setHeader(text);
    }

    /**
     * Returns the contents of the header part of the section. If the
     * section is invalid the method returns null.
     * @return contents of the header or null, if the section is not valid.
     */
    public String getHeader() {
        if (getImpl() == null) return getDelegate().getHeader();
        return getImpl().getHeader();
    }

    /**
     * Sets the text of the footer.
     * Note that the footer of the section must have exactly one line.
     * So, all interior newline characters will be replaced by spaces.
     *
     * @param text the new text
     */
    public void setFooter(String text) {
        if (getImpl() == null) throw new IllegalStateException();
        getImpl().setFooter(text);
    }

    /**
     * Returns the contents of the footer part of the guarded section.
     * The method will return null, if the section is not valid.
     * @return contents of the footer part, or null if the section is not valid.
     */
    public String getFooter() {
        if (getImpl() == null) return getDelegate().getFooter();
        return getImpl().getFooter();
    }
    
    /**
     * Returns a position where the body starts
     * @return the start position of the body part
     */
    public Position getBodyStartPosition() {
        if (getImpl() == null) return new OffsetPosition(getDelegate().getBodyStartPosition(), offset);
        return getImpl().getBodyStartPosition();
    }
    
    /**
     * Returns a position where the body ends
     * @return the end position of the body part
     */
    public Position getBodyEndPosition() {
        if (getImpl() == null) return new OffsetPosition(getDelegate().getBodyEndPosition(), offset);
        return getImpl().getBodyEndPosition();
    }
    
    InteriorSectionImpl getImpl() {
        return (InteriorSectionImpl) super.getImpl();
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("InteriorSection:"+name); // NOI18N
      try {
        buf.append("HEADER:\""); // NOI18N
        buf.append(header.getText());
        buf.append("\"");
        buf.append("BODY:\""); // NOI18N
        buf.append(body.getText());
        buf.append("\"");
        buf.append("BOTTOM:\""); // NOI18N
        buf.append(bottom.getText());
        buf.append("\"");
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      return buf.toString();
    }*/

    @Override
    InteriorSection getDelegate() {
        return (InteriorSection) super.getDelegate();
    }

    @Override
    GuardedSection clone(int offset) {
        return new InteriorSection(this, offset);
    }
}
