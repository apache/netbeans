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
package org.netbeans.modules.html.editor.api.actions;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.AbstractAction;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.ElementUtils;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public abstract class AbstractSourceElementAction extends AbstractAction {

    protected FileObject file;
    private String elementPath;

    public AbstractSourceElementAction(FileObject file, String elementPath) {
        this.file = file;
        this.elementPath = elementPath;
    }

    @Override
    public boolean isEnabled() {
        return elementPath != null;
    }

    public FileObject getFileObject() {
        return file;
    }

    /**
     * Resolves the {@link FileObject} and source element path to a parser
     * result and {@link OpenTag}.
     *
     * The returned value is not cached and will run a parsing api task each
     * time is called.
     *
     * @return An instance of {@link  SourceElementHandle}
     * exception is thrown.
     * @throws ParseException
     */
    public SourceElementHandle createSourceElementHandle() throws ParseException {
        final AtomicReference<SourceElementHandle> seh_ref = new AtomicReference<>();
        Source source = Source.create(file);
        ParserManager.parse(Collections.singleton(source), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html");
                if (ri == null) {
                    return;
                }

                HtmlParserResult result = (HtmlParserResult) ri.getParserResult();
                Snapshot snapshot = result.getSnapshot();

                Node root = result.root();
                OpenTag openTag = ElementUtils.query(root, elementPath);

                seh_ref.set(new SourceElementHandle(openTag, snapshot, file));

            }
        });
        return seh_ref.get();
    }

    public class SourceElementHandle {

        private final OpenTag openTag;
        private final Snapshot snapshot;
        private final FileObject file;

        private SourceElementHandle(OpenTag openTag, Snapshot snapshot, FileObject file) {
            this.openTag = openTag;
            this.snapshot = snapshot;
            this.file = file;
        }

        public OpenTag getOpenTag() {
            return openTag;
        }

        public Snapshot getSnapshot() {
            return snapshot;
        }

        public FileObject getFile() {
            return file;
        }
        
        public boolean isResolved() {
            return openTag != null;
        }
    }
}
