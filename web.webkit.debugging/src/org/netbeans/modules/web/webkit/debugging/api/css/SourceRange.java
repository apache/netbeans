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
package org.netbeans.modules.web.webkit.debugging.api.css;

import org.json.simple.JSONObject;

/**
 * Text range within a resource.
 *
 * @author Jan Stola
 */
public class SourceRange {
    /** Start offset of the range (inclusive). */
    private final int start;
    /** End offset of the range (exclusive). */
    private final int end;
    /** Start line of the range. */
    private final int startLine;
    /** Start column of the range (inclusive). */
    private final int startColumn;
    /** End line of the range. */
    private final int endLine;
    /** End column of the range (exclusive). */
    private final int endColumn;

    /**
     * Creates a new {@code SourceRange} that corresponds to the given JSONObject.
     *
     * @param range JSONObject describing the source range.
     */
    SourceRange(JSONObject range) {
        if (range.containsKey("start")) { // NOI18N
            start = ((Number)range.get("start")).intValue(); // NOI18N
            end = ((Number)range.get("end")).intValue(); // NOI18N
        } else {
            start = end = -1;
        }
        if (range.containsKey("startLine")) { // NOI18N
            startLine = ((Number)range.get("startLine")).intValue(); // NOI18N
            startColumn = ((Number)range.get("startColumn")).intValue(); // NOI18N
            endLine = ((Number)range.get("endLine")).intValue(); // NOI18N
            endColumn = ((Number)range.get("endColumn")).intValue(); // NOI18N
        } else {
            startLine = startColumn = endLine = endColumn = -1;
        }
    }

    /**
     * Returns the start offset of the range (inclusive).
     *
     * @return start of the range (inclusive).
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the end offset of the range (exclusive).
     *
     * @return end of the range (exclusive).
     */
    public int getEnd() {
        return end;
    }

    /**
     * Returns the start line of the range.
     * 
     * @return start line of the range.
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the start column of the range (inclusive).
     * 
     * @return start column of the range (inclusive).
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Returns the end line of the range.
     * 
     * @return end line of the range.
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Returns the end column of the range (exclusive).
     * 
     * @return end column of the range (exclusive).
     */
    public int getEndColumn() {
        return endColumn;
    }

}
