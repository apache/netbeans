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
