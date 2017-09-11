/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.javafx2.editor.completion.model.FxNode;
import org.netbeans.modules.javafx2.editor.completion.model.TextPositions;
import org.netbeans.modules.javafx2.editor.completion.model.XmlTreeNode;
import org.openide.util.Enumerations;

/**
 * Encapsulates non-public data 
 *
 * @author sdedic
 */
public class NodeInfo implements XmlTreeNode, TextPositions {
    private int    type;
    private int    start;
    private int    end = -1;
    
    private int    contentStart = -1;
    private int    contentEnd = -1;

    /**
     * BeanInfo, EventInfo or PropertyInfo
     */
    private Object resolvedInfo;
    
    private String tagName;
    
    private List<FxNode>    attributes = Collections.emptyList();

    private List<FxNode>    children = Collections.emptyList();
    
    private boolean includeEnd;
    
    public static NodeInfo newNode() {
        return new NodeInfo(-1);
    }
    
    static NodeInfo syntheticNode() {
        return new NodeInfo(-1);
    }
    
    public NodeInfo(int start) {
        this.start = start;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
    
    public NodeInfo startAt(int start) {
        this.start = start;
        return this;
    }
    
    public NodeInfo makeAttribute() {
        this.type = 1;
        return this;
    }

    public NodeInfo makePI() {
        this.type = 2;
        return this;
    }
    
    public boolean isAttribute() {
        return type == 1;
    }
    
    public boolean isElement() {
        return type == 0;
    }

    public int getStart() {
        return offset(start);
    }

    public int getEnd() {
        return offset(end);
    }

    public int getContentStart() {
        return offset(contentStart);
    }

    public int getContentEnd() {
        return offset(contentEnd);
    }
    
    public NodeInfo endsAt(int e, boolean includeEnd) {
        this.end = e;
        this.includeEnd = includeEnd;
        return this;
    }

    public void markIncludeEnd() {
        this.includeEnd = true;
    }
    
    public NodeInfo endsAt(int e) {
        this.end = e;
        return this;
    }
    
    public NodeInfo endContent(int pos) {
        this.contentEnd = pos;
        return this;
    }
    
    public NodeInfo startContent(int pos) {
        this.contentStart = pos;
        return this;
    }
    
    public void addChild(FxNode n) {
        if (children.isEmpty()) {
            children = new ArrayList<FxNode>();
        }
        children.add(n);
    }
    
    public List<FxNode> getAttributes() {
        return getImmutable(attributes);
    }
    
    public List<FxNode> getChildren() {
        return getImmutable(children);
    }
    
    private List<FxNode> getImmutable(List<FxNode> nodes) {
        return nodes.isEmpty() ? Collections.<FxNode>emptyList() : Collections.unmodifiableList(nodes);
    }
    
    public Enumeration<FxNode> getEnclosedNodes() {
        return Enumerations.concat(Collections.enumeration(attributes), Collections.enumeration(children));
    }
    
    int makeFuzzy(int pos) {
        return -pos -1;
    }
    
    int offset(int o) {
        return o >= -1 ? o : (- o) - 1;
    }
    
    public boolean contentContains(int position, boolean caret) {
        if (contentStart == -1) {
            return false;
        }
        int s = offset(contentStart);
        if (contentEnd == -1) {
            if (end == -1) {
                return false;
            }
            if (s == offset(end) && position == s && caret) {
                return true;
            }
            return (s <= position) &&
                   (offset(end) > position || (includeEnd && offset(end) == position));
        } else {
            int e = offset(contentEnd);
            
            if (s == e && position == s && caret) {
                return true;
            }
            return (s <= position) &&
                   (e > position || (caret && e == position));
        }
    }
    
    public boolean contains(int position, boolean caret) {
        if (start == end && position == start && caret) {
            return true;
        }
        if (position < start || (position == start && caret)) {
            return false;
        }
        int e = end;
        boolean incEnd = includeEnd;
        if (end == -1) {
            return false;
        } else if (end < 0) {
            e = (-end) - 1;
            incEnd = true;
        }
        return position < e || (incEnd && e == position);
    }

    @Override
    public boolean isDefined(Position pos) {
        int p;
        
        switch (pos) {
            case ContentEnd:
                p = contentEnd;
                break;
            case ContentStart:
                p = contentStart;
                break;
            case End:
                p = end;
                break;
            case Start:
                p = start;
                break;
            default:
                throw new IllegalArgumentException();
        }
        return p >= 0;
    }
    
    
}
