/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.gsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.html.editor.lib.api.elements.TreePath;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.elements.*;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 * Represents a handle for OpenTags obtained from HtmlParserResult. 
 * 
 * The handle may be held out of the parsing task and later resolved
 * back to node by {@link #resolve(org.netbeans.modules.csl.spi.ParserResult) 
 *
 * @author mfukala@netbeans.org
 */
public class HtmlElementHandle implements ElementHandle {

    private final FileObject fo;
    private final String name;
    private final String elementPath;
    private final int attributesHash;

    public HtmlElementHandle(OpenTag node, FileObject fo) {
        this.fo = fo;
        this.name = node.id().toString();
        this.elementPath = ElementUtils.encodeToString(new TreePath(node));
        this.attributesHash = computeAttributesHash(node);
    }
    
    private int computeAttributesHash(OpenTag node) {
        int hash = 11;
        for(Attribute a : node.attributes()) {
           hash = 37 * hash + a.name().hashCode();
           CharSequence value = a.value();
           hash = 37 * hash + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }

    @Override
    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public String getMimeType() {
        return HtmlKit.HTML_MIME_TYPE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIn() {
        return null;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.TAG;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    @Override
    public boolean signatureEquals(ElementHandle handle) {
        if (!(handle instanceof HtmlElementHandle)) {
            return false;
        }
        HtmlElementHandle htmlHandle = (HtmlElementHandle)handle;
        return htmlHandle.elementPath.equals(elementPath);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.elementPath != null ? this.elementPath.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HtmlElementHandle other = (HtmlElementHandle) obj;
        if ((this.elementPath == null) ? (other.elementPath != null) : !this.elementPath.equals(other.elementPath)) {
            return false;
        }
        if ((this.attributesHash != other.attributesHash)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return elementPath;
    }
    
    public OpenTag resolve(ParserResult result) {
        if(!(result instanceof HtmlParserResult)) {
            return null;
        }
        
        HtmlParserResult htmlParserResult = (HtmlParserResult)result;
        Node root = htmlParserResult.root();
        
        return ElementUtils.query(root, elementPath);
    }
        
    @Override
    public OffsetRange getOffsetRange(ParserResult result) {
        OpenTag node = resolve(result);
        if(node == null) {
            return OffsetRange.NONE;
        }
        
        Snapshot snapshot = result.getSnapshot();
        int dfrom = snapshot.getOriginalOffset(node.from());
        int dto = snapshot.getOriginalOffset(node.semanticEnd());
        
        return dfrom != -1 && dto != -1 ? new OffsetRange(dfrom, dto) : OffsetRange.NONE;
    }
    
}
