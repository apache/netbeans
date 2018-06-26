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

package org.netbeans.modules.javascript2.requirejs.editor;

import java.util.Collections;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class MappingCompletionItem implements CompletionProposal {
    
    private static ImageIcon REQUIREJS_ICON = null;
   
    private final int anchor;
    private final MappingHandle element;
    private final FileObject mapToFile;
    
    public MappingCompletionItem(final String mapping, FileObject toFile, final int anchor){
        this.element = new MappingHandle(mapping, toFile);
        this.anchor = anchor;
        this.mapToFile = toFile;
    }
    
    protected String getText() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getText().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MappingCompletionItem)) {
            return false;
        }

        MappingCompletionItem remote = (MappingCompletionItem) o;

        return getText().equals(remote.getText());
    }

    @Override
    public ImageIcon getIcon() {
        
        if (REQUIREJS_ICON == null) {
            REQUIREJS_ICON = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/requirejs/resources/requirejs.png")); //NOI18N
        }
        return REQUIREJS_ICON;
    }

    @Override
    public int getAnchorOffset() {
        return this.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return this.element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return getName() + (mapToFile != null && mapToFile.isFolder() ? "/" : "");
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        return getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        return "";
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        return element.getModifiers();
    }

    @Override
    public boolean isSmart() {
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return -1000;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    
    private static class MappingHandle extends SimpleHandle.DocumentationHandle {
        private final FileObject toFile;

        public MappingHandle(String name, final FileObject toFile) {
            super(name, ElementKind.FILE);
            this.toFile = toFile;
        }
        
        
        @Override
        @NbBundle.Messages({"mappingTo=Mapped to", 
            "mappedFile=file", "mappedFolder=folder", "mappedToVirtual=Mapped to non existing folder / file."})
        public String getDocumentation() {
            StringBuilder sb = new StringBuilder();
            sb.append(Bundle.mappingTo()).append(" "); //NOI18N
            if (toFile != null) {
                if (toFile.isFolder()) {
                    sb.append(Bundle.mappedFolder());
                } else {
                    sb.append(Bundle.mappedFile());
                }
                sb.append(FSCompletionUtils.writeFilePathForDocWindow(toFile));
            } else {
                return Bundle.mappedToVirtual();
            }
            return sb.toString();
        }
    }
}
