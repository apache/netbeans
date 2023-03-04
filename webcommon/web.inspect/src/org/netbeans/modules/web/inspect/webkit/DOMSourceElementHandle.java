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

package org.netbeans.modules.web.inspect.webkit;

import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.SourceElementHandle;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.inspect.actions.Resource;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.openide.filesystems.FileObject;

/**
 * Handle that provides a resolution of a WebKit node to a source node.
 *
 * @author Jan Stola
 */
public class DOMSourceElementHandle implements SourceElementHandle {
    /** WebKit node. */
    private final Node webKitNode;
    /** Owning project of the node. */
    private final Project project;

    /**
     * Creates a new {@code DOMSourceElementHandle}.
     * 
     * @param webKitNode WebKit node.
     * @param project owning project of the node.
     */
    DOMSourceElementHandle(Node webKitNode, Project project) {
        this.webKitNode = webKitNode;
        this.project = project;
    }

    @Override
    public FileObject getFileObject() {
        FileObject result = null;
        String documentURL = null;
        Node node = webKitNode;
        do {
            documentURL = node.getDocumentURL();
            node = node.getParent();
        } while ((documentURL == null) && (node != null));
        if (documentURL != null) {
           result = new Resource(project, documentURL).toFileObject();
        }
        return result;
    }

    @Override
    public org.netbeans.modules.html.editor.lib.api.elements.Node resolve(Parser.Result result) {
        org.netbeans.modules.html.editor.lib.api.elements.Node node = null;
        if (result instanceof HtmlParsingResult) {
            HtmlParsingResult htmlResult = (HtmlParsingResult)result;
            node = Utilities.findNode(htmlResult, webKitNode);
        }
        return node;
    }

}
