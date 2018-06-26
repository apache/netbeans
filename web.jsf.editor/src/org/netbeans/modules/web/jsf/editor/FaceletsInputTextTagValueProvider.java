/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.jsf.editor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.JsfUtils;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.modules.web.jsfapi.spi.InputTextTagValueProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author marekfukala
 */
@ServiceProvider(service=InputTextTagValueProvider.class)
public class FaceletsInputTextTagValueProvider implements InputTextTagValueProvider {

    private static final String INPUT_TEXT_TAG_NAME = "inputText"; //NOI18N
    private static final String VALUE_ATTR_NAME = "value"; //NOI18N

    @Override
    public Map<String, String> getInputTextValuesMap(FileObject fo) {
        try {
            if(!JsfUtils.isFaceletsFile(fo)) {
                return null;
            }

            Document doc = DataLoadersBridge.getDefault().getDocument(fo); //loads the document if not opened
            if (doc == null) {
                return null; //should not normally happen
            }
            final AtomicReference<HtmlParsingResult> result = new AtomicReference<>();
            Source source = Source.create(doc);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/html"); //NOI18N
                    if (ri != null) {
                        result.set((HtmlParsingResult) ri.getParserResult());
                    }
                }
            });

            HtmlParsingResult hresult = result.get();
            if(hresult == null) {
                return null;
            }

            String htmlNs = NamespaceUtils.getForNs(hresult.getNamespaces(), DefaultLibraryInfo.HTML.getNamespace());
            if (htmlNs != null) {
                String htmlLibPrefix = hresult.getNamespaces().get(htmlNs);
                if(htmlLibPrefix == null) {
                    htmlLibPrefix = DefaultLibraryInfo.HTML.getDefaultPrefix();
                }
                String tagName = new StringBuilder().append(htmlLibPrefix).append('.').append(INPUT_TEXT_TAG_NAME).toString();
                Collection<OpenTag> foundNodes = findValue(hresult.root(htmlNs).children(), tagName, new ArrayList<OpenTag>());

                Map<String, String> map = new HashMap<>();
                for (OpenTag node : foundNodes) {
                    Attribute attr = node.getAttribute(VALUE_ATTR_NAME);
                    if(attr != null) {
                        CharSequence value = attr.unquotedValue();
                        if(value != null) {
                            String svalue = value.toString();
                            String key = generateKey(svalue, map);
                            map.put(key, svalue);
                        }
                    }
                }
                return map;
            }

        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private Collection<OpenTag> findValue(Collection<Element> nodes, String tagName, Collection<OpenTag> foundNodes) {
        if (nodes == null) {
            return foundNodes;
        }
        
        for(Element e : nodes) {
            if(e.type() != ElementType.OPEN_TAG) {
                continue;
            }
            OpenTag openTag = (OpenTag)e;
            if(LexerUtils.equals(tagName, openTag.name(), true, false)) {
                foundNodes.add(openTag);
            } else {
                foundNodes = findValue(openTag.children(), tagName, foundNodes);
            }
        }
        return foundNodes;
    }

    private String generateKey(String value, Map<String, String> properties) {
        if (value.startsWith("#{")) {    //NOI18N
            value = value.substring(2, value.length()-1);
        }
        String result = value.substring(value.lastIndexOf(".")+1,value.length()).toLowerCase();
        int i=0;
        String tmp = result;
        while (properties.get(tmp) != null) {
            i++;
            tmp=result+i;
        }
        return result;
    }


}
