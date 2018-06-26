/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.latte.parser;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class LatteParserResult extends ParserResult {
    private final List<org.netbeans.modules.csl.api.Error> errors = new ArrayList<>();
    private final FileObject fileObject;

    LatteParserResult(Snapshot snapshot) {
        super(snapshot);
        fileObject = snapshot.getSource().getFileObject();
    }

    @Override
    public List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics() {
        return new ArrayList<>(errors);
    }

    public void addError(String description, int offset, int length) {
        errors.add(new Error(description, offset, length, fileObject));
    }

    @Override
    protected void invalidate() {
    }

    private static final class Error implements org.netbeans.modules.csl.api.Error {
        private final String description;
        private final int offset;
        private final int length;
        private final FileObject fileObject;

        private Error(String description, int offset, int length, FileObject fileObject) {
            this.description = description;
            this.offset = offset;
            this.length = length;
            this.fileObject = fileObject;
        }

        @Override
        public String getDisplayName() {
            return description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getKey() {
            return description;
        }

        @Override
        public FileObject getFile() {
            return fileObject;
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

}
