/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.tasklist.todo.settings;

/**
 *
 * @author jpeska
 */
public class CommentTags {

    private String lineComment = "";
    private String blockCommentStart = "";
    private String blockCommentEnd = "";
    private boolean lineCommentEnabled = false;
    private boolean blockCommentEnabled = false;


    public CommentTags(String lineComment, String blockCommentStart, String blockCommentEnd) {
        this.lineComment = lineComment;
        lineCommentEnabled = !lineComment.isEmpty();
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        blockCommentEnabled = !blockCommentStart.isEmpty();
    }

    public CommentTags(String blockCommentStart, String blockCommentEnd) {
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        blockCommentEnabled = true;
    }

    public CommentTags(String lineComment, boolean lineCommentEnabled, String blockCommentStart, String blockCommentEnd, boolean blockCommentEnabled) {
        this.lineComment = lineComment;
        this.lineCommentEnabled = lineCommentEnabled;
        this.blockCommentStart = blockCommentStart;
        this.blockCommentEnd = blockCommentEnd;
        this.blockCommentEnabled = blockCommentEnabled;
    }

    public CommentTags(String lineComment) {
        this.lineComment = lineComment;
        lineCommentEnabled = true;
    }

    public CommentTags() {
    }

    public String getLineComment() {
        return lineComment;
    }

    public void setLineComment(String lineComment) {
        this.lineComment = lineComment;
    }

    public String getBlockCommentStart() {
        return blockCommentStart;
    }

    public void setBlockCommentStart(String blockCommentStart) {
        this.blockCommentStart = blockCommentStart;
    }

    public String getBlockCommentEnd() {
        return blockCommentEnd;
    }

    public void setBlockCommentEnd(String blockCommentEnd) {
        this.blockCommentEnd = blockCommentEnd;
    }

    public boolean isLineCommentEnabled() {
        return lineCommentEnabled;
    }

    public void setLineCommentEnabled(boolean lineCommentEnabled) {
        this.lineCommentEnabled = lineCommentEnabled;
    }

    public boolean isBlockCommentEnabled() {
        return blockCommentEnabled;
    }

    public void setBlockCommentEnabled(boolean blockCommentEnabled) {
        this.blockCommentEnabled = blockCommentEnabled;
    }
}
