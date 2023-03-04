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

package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.el.ELException;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Severity;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 * {@code ParserResult} for Expression Language expressions.
 *
 * @author Erno Mononen
 */
public final class ELParserResult extends ParserResult {

    private final List<ELElement> elements = new ArrayList<>();

    private final FileObject file;

    public ELParserResult(Snapshot snapshot) {
        super(snapshot);
        this.file = snapshot.getSource().getFileObject();
    }

    public ELParserResult(FileObject fo) {
        super(null);
        this.file = fo;
    }

    public ELElement addValidElement(Node node, ELPreprocessor expression, OffsetRange embeddedOffset) {
        ELElement element = ELElement.valid(node, expression, embeddedOffset, getSnapshot());
        add(element);
        return element;
    }

    public ELElement addErrorElement(ELException error, ELPreprocessor expression, OffsetRange embeddedOffset) {
        ELElement element = ELElement.error(error, expression, embeddedOffset, getSnapshot());
        add(element);
        return element;
    }
    
    public List<ELElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public ELElement getElementAt(int offset) {
        for (ELElement each : elements) {
            if (each.getOriginalOffset().containsInclusive(offset)) {
                return each;
            }
        }
        return null;
    }

    public List<ELElement> getElementsTo(int offset) {
        List<ELElement> result = new ArrayList<>();
        for (ELElement each : elements) {
            if (each.getOriginalOffset().getStart() < offset) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean add(ELElement element) {
        return elements.add(element);
    }

    public FileObject getFileObject() {
        return file;
    }

    public boolean hasElements() {
        return !elements.isEmpty();
    }

    /**
     * @return  true if the result contains only valid EL expressions;
     *  false otherwise.
     */
    public boolean isValid() {
        for (ELElement each : elements) {
            if (!each.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void invalidate() {
    }

    @Override
    public String toString() {
        return "ELParserResult{" + "fileObject=" + getFileObject() + "elements=" + elements + '}';
    }

    @Override
    public List<? extends Error> getDiagnostics() {
        List<ELError> result = new ArrayList<>();
        for (ELElement each : elements) {
            if (!each.isValid()) {
                result.add(new ELError(each, file));
            }
        }
        return result;
    }

    private static class ELError implements Error.Badging {

        private final ELElement errorElement;
        private final FileObject file;

        public ELError(ELElement errorElement, FileObject file) {
            this.errorElement = errorElement;
            this.file = file;
        }

        @Override
        public String getDisplayName() {
            return errorElement.getError().getLocalizedMessage();
        }

        @Override
        public String getDescription() {
            return errorElement.getError().getLocalizedMessage();
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public FileObject getFile() {
            return file;
        }

        @Override
        public int getStartPosition() {
            return errorElement.getEmbeddedOffset().getStart();
        }

        @Override
        public int getEndPosition() {
            return errorElement.getEmbeddedOffset().getEnd();
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
            return new Object[0];
        }

        @Override
        public boolean showExplorerBadge() {
            return true;
        }
    }
}
