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
package org.netbeans.modules.php.latte.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.php.latte.csl.LatteLanguage;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@MimeRegistration(mimeType = LatteLanguage.LATTE_MIME_TYPE, service = HtmlExtension.class)
public class LatteHtmlExtension extends HtmlExtension {
    private static final Collection<CustomAttribute> CUSTOM_ATTRIBUTES = new ArrayList<>();
    static {
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:href")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:class")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:input")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:attr")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:input")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:inner-input")); //NOI18N

        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-if")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-ifset")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-ifCurrent")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-for")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-foreach")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-while")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-cache")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-snippet")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-block")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-define")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-capture")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-syntax")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-form")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-label")); //NOI18N
        CUSTOM_ATTRIBUTES.add(new LatteCustomAttribute("n:tag-input")); //NOI18N
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        return attribute.name().toString().startsWith("n:"); //NOI18N
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName ) {
        return CUSTOM_ATTRIBUTES;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        // COPIED FROM AngularHtmlExtension
        /********** ++ **********/
        List<CompletionItem> items = new ArrayList<>();
        Element element = context.getCurrentNode();
        if (element != null) {
            switch (element.type()) {
                case OPEN_TAG:
                    OpenTag ot = (OpenTag) element;
                    String name = ot.unqualifiedName().toString();
                    Collection<CustomAttribute> customAttributes = getCustomAttributes(name);
                    for (CustomAttribute ca : customAttributes) {
                        items.add(new LatteAttributeCompletionItem(ca, context.getCCItemStartOffset()));
                    }
                    break;
                default:
                    // no-op
            }
        }
        if (context.getPrefix().length() > 0) {
            //filter the items according to the prefix
            Iterator<CompletionItem> itr = items.iterator();
            while (itr.hasNext()) {
                CharSequence insertPrefix = itr.next().getInsertPrefix();
                if (insertPrefix != null) {
                    if (!LexerUtils.startsWith(insertPrefix, context.getPrefix(), true, false)) {
                        itr.remove();
                    }
                }
            }
        }
        return items;
        /********** -- **********/
    }

}
