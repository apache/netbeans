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

import org.netbeans.modules.editor.guards.SimpleSectionImpl;

/**
 * Represents a simple guarded section.
 * It consists of one contiguous block.
 */
public final class SimpleSection extends GuardedSection {

    /**
     * Creates new section.
     * @param name Name of the new section.
     * @param bounds The range of the section.
     */
    SimpleSection(SimpleSectionImpl impl) {
        super(impl);
    }

    SimpleSection(GuardedSection delegate, int offset) {
        super(delegate, offset);
    }

    /**
     * Sets the text of the section.
     * @param text the new text
     */
    public void setText(String text) {
        if (getImpl() == null) throw new IllegalStateException();
        getImpl().setText(text);
    }

    /*
    public String toString() {
      StringBuffer buf = new StringBuffer("SimpleSection:"+name); // NOI18N
      buf.append("\"");
      try {
        buf.append(bounds.getText());
      }
      catch (Exception e) {
        buf.append("EXCEPTION:"); // NOI18N
        buf.append(e.getMessage());
      }
      buf.append("\"");
      return buf.toString();
    }*/

    SimpleSectionImpl getImpl() {
        return (SimpleSectionImpl) super.getImpl();
    }

    @Override
    GuardedSection clone(int offset) {
        return new SimpleSection(this, offset);
    }
}
