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
