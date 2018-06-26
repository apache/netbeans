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
