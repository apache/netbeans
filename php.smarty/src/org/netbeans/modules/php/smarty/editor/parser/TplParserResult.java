/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): Sebastian Hörl
 */
package org.netbeans.modules.php.smarty.editor.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

public class TplParserResult extends ParserResult {

    private boolean valid = true;
    private List<Error> errorList = new ArrayList<Error>();
    private List<Block> blockList = new ArrayList<Block>();

    protected TplParserResult(Snapshot snapshot) {
        super(snapshot);
    }

    public List<Error> getErrors() {
        return errorList;
    }

    public void addError(String description, int offset, int length) {
        errorList.add(new Error(description, offset, length, getSnapshot()));
    }

    public List<Block> getBlocks() {
        return blockList;
    }

    public void addBlock(Block block) {
        blockList.add(block);
    }

    @Override
    protected void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return errorList;
    }

    public static class Error implements org.netbeans.modules.csl.api.Error {

        private String description;
        private int offset;
        private int length;
        private Snapshot snapshot;

        public Error(String description, int offset, int length, Snapshot snapshot) {
            this.description = description;
            this.offset = offset;
            this.length = length;
            this.snapshot = snapshot;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        @Override
        public String getDisplayName() {
            return description;
        }

        @Override
        public String getKey() {
            return description;
        }

        @Override
        public FileObject getFile() {
            return snapshot.getSource().getFileObject();
        }

        @Override
        public int getStartPosition() {
            return offset;
        }

        @Override
        public int getEndPosition() {
            return offset + length;
        }

        @Override
        public boolean isLineError() {
            return false;
        }

        @Override
        public Severity getSeverity() {
            return Severity.ERROR;
        }

        @Override
        public Object[] getParameters() {
            return null;
        }
    }

    public static class Section {

        private final String function;
        private final OffsetRange offset;
        private final String text;

        public Section(String function, OffsetRange offset, String text) {
            this.function = function;
            this.offset = offset;
            this.text = text;
        }

        public String getName() {
            return function;
        }

        public OffsetRange getOffset() {
            return offset;
        }

        public String getText() {
            return text;
        }

        public int getFunctionNameLength() {
            return function.length();
        }
        
    }

    public static class Block {

        private final List<Section> sections = new LinkedList<Section>();

        public Block() {
        }

        public Block(Section section) {
            sections.add(section);
        }

        public void addSection(Section section) {
            sections.add(section);
        }

        public List<Section> getSections() {
            return sections;
        }

    }
}
