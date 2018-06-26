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
package org.netbeans.modules.javascript2.editor.doc;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.JsCompletionItem;
import org.netbeans.modules.javascript2.doc.api.JsDocumentationSupport;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTag;
import org.netbeans.modules.javascript2.doc.spi.AnnotationCompletionTagProvider;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationProvider;
import org.netbeans.modules.javascript2.doc.spi.ParameterFormat;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsDocumentationCodeCompletion {

    private static final String TAG_PREFIX = "@"; //NOI18N

    public static void complete(JsCompletionItem.CompletionRequest request, List<CompletionProposal> resultList) {
        int originalOffset = request.info.getSnapshot().getOriginalOffset(request.anchor);
        if (request.prefix != null && request.prefix.startsWith(TAG_PREFIX)) {
            completeAnnotation(request, resultList, originalOffset);
        } else {
            CharSequence text = request.info.getSnapshot().getText();
            if (TAG_PREFIX.charAt(0) == text.charAt(request.anchor - 1)) {
                request.prefix = request.prefix == null ? TAG_PREFIX : TAG_PREFIX + request.prefix;
                completeAnnotation(request, resultList, originalOffset - 1);
            }
        }
    }

    private static void completeAnnotation(JsCompletionItem.CompletionRequest request, List<CompletionProposal> resultList, int anchor) {
        JsDocumentationProvider documentationProvider = JsDocumentationSupport.getDocumentationProvider(request.result);

        // XXX - list of annotations could differ per context as in PHP (i.e. for type, method, field, ...)
        int orderingBase = 0;
        for (AnnotationCompletionTagProvider provider : documentationProvider.getAnnotationsProvider()) {
            orderingBase++;
            for (AnnotationCompletionTag tag : provider.getAnnotations()) {
                if (tag.getName().startsWith(request.prefix)) {
                    resultList.add(new JsDocumentationCodeCompletionItem(anchor, tag, provider.getName(), orderingBase));
                }
            }
        }
    }

    public static class JsDocumentationCodeCompletionItem implements CompletionProposal {

        private static final String ANNOTATION_ICON = "org/netbeans/modules/csl/source/resources/icons/annotation.png"; //NOI18N
        private static final ImageIcon IMAGE_ICON = new ImageIcon(ImageUtilities.loadImage(ANNOTATION_ICON));
        
        private final AnnotationCompletionTag tag;
        private final int anchorOffset;
        private final JsDocumentationElement elem;
        private final String providerName;
        private final int priority;

        public JsDocumentationCodeCompletionItem(int anchorOffset, AnnotationCompletionTag tag, String providerName, int priority) {
            this.tag = tag;
            this.anchorOffset = anchorOffset;
            this.providerName= providerName;
            this.priority = priority;
            elem = new JsDocumentationElement(tag.getName(), tag.getDocumentation());
        }

        @Override
        public int getAnchorOffset() {
            return anchorOffset;
        }

        @Override
        public ElementHandle getElement() {
            return elem;
        }

        @Override
        public String getName() {
            return tag.getName(); //NOI18N
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getSortText() {
            return priority + providerName + getName();
        }

        @Override
        public int getSortPrioOverride() {
            return 0;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            List<ParameterFormat> formats = tag.getParameters();
            if (formats != null) {
                for (ParameterFormat f : formats) {
                    String pre = f.getPre();
                    if (pre != null) {
                        formatter.appendText(pre);
                    }
                    formatter.parameters(true);
                    formatter.appendText(f.getParam());
                    formatter.parameters(false);
                    String post = f.getPost();
                    if (post != null) {
                        formatter.appendText(post);
                    }
                }
            }
            return formatter.getText();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return providerName;
        }

        @Override
        public ElementKind getKind() {
            return elem.getKind();
        }

        @Override
        public ImageIcon getIcon() {
            return IMAGE_ICON;
        }

        @Override
        public Set<Modifier> getModifiers() {
            return Collections.<Modifier>emptySet();
        }

        @Override
        public boolean isSmart() {
            return false;
        }

        @Override
        public String getCustomInsertTemplate() {
            return tag.getInsertTemplate();
        }
    }

}
