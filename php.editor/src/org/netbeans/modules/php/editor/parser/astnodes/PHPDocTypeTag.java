/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.List;
import org.netbeans.modules.php.spi.annotation.AnnotationParsedLine;

/**
 * Represents a type in the tags like @param, @return, @throws etc.
 * There can be more types associated with one return value or param.
 *
 * @author Petr Pisl
 */
public class PHPDocTypeTag extends PHPDocTag {

    private final List<PHPDocTypeNode> types;
    protected String documentation;

    public PHPDocTypeTag(int start, int end, AnnotationParsedLine kind, String value, List<PHPDocTypeNode> types) {
        super(start, end, kind, value);
        this.types = types;
        this.documentation = null;
    }

    /**
     *
     * @return list of PHPDocNode or PHPDocStaticAccessType
     */
    public List<PHPDocTypeNode> getTypes() {
        return types;
    }

    @Override
    public String getDocumentation() {
        if (documentation == null && types.size() > 0) {
            PHPDocTypeNode lastType = types.get(0);
            boolean isLastNodeArray = lastType.isArray();
            for (PHPDocTypeNode node : types) {
                if (lastType.getEndOffset() < node.getEndOffset()) {
                    lastType = node;
                    isLastNodeArray = node.isArray();
                }
            }
            int indexAfterTypeWithoutArrayPostfix = getValue().indexOf(lastType.getValue()) + lastType.getValue().length();
            if (isLastNodeArray) {
                String documentationWithArrayPrefix = getValue().substring(indexAfterTypeWithoutArrayPostfix).trim();
                int firstSpace = documentationWithArrayPrefix.indexOf(" "); //NOI18N
                int firstTab = documentationWithArrayPrefix.indexOf("\t"); //NOI18N
                int min = -1;
                if (firstSpace > 0 && (firstSpace < firstTab || firstTab == -1)) {
                    min = firstSpace;
                } else if (firstTab > 0 && (firstTab < firstSpace || firstSpace == -1)) {
                    min = firstTab;
                }
                if (min == -1) {
                    documentation = ""; //NOI18N
                } else {
                    documentation = getValue().substring(indexAfterTypeWithoutArrayPostfix + min).trim();
                }
            } else {
                documentation = getValue().substring(indexAfterTypeWithoutArrayPostfix).trim();
            }
        }
        return documentation;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


}
