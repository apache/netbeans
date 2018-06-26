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
package org.netbeans.modules.javascript2.jsdoc;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.jsdoc.completion.AssingTag;
import org.netbeans.modules.javascript2.jsdoc.completion.DescriptionTag;
import org.netbeans.modules.javascript2.jsdoc.completion.LinkTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeDescribedTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeNamedTag;
import org.netbeans.modules.javascript2.jsdoc.completion.TypeSimpleTag;
import org.netbeans.modules.javascript2.jsdoc.model.JsDocElementType;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocAnnotationCompletionTagProvider extends AnnotationCompletionTagProvider {

    List<AnnotationCompletionTag> annotations = null;

    public JsDocAnnotationCompletionTagProvider(String name) {
        super(name);
    }

    @Override
    public synchronized List<AnnotationCompletionTag> getAnnotations() {
        if (annotations == null) {
            initAnnotations();
        }
        return annotations;
    }

    private void initAnnotations() {
        annotations = new LinkedList<AnnotationCompletionTag>();
        for (JsDocElementType type : JsDocElementType.values()) {
            if (type == JsDocElementType.UNKNOWN || type == JsDocElementType.CONTEXT_SENSITIVE) {
                continue;
            }

            switch (type.getCategory()) {
                case ASSIGN:
                    annotations.add(new AssingTag(type.toString()));
                    break;
                case DECLARATION:
                    annotations.add(new TypeSimpleTag(type.toString()));
                    break;
                case DESCRIPTION:
                    annotations.add(new DescriptionTag(type.toString()));
                    break;
                case LINK:
                    annotations.add(new LinkTag(type.toString()));
                    break;
                case NAMED_PARAMETER:
                    annotations.add(new TypeNamedTag(type.toString()));
                    break;
                case UNNAMED_PARAMETER:
                    annotations.add(new TypeDescribedTag(type.toString()));
                    break;
                default:
                    annotations.add(new AnnotationCompletionTag(type.toString(), type.toString()));
                    break;
            }
        }
    }
}
