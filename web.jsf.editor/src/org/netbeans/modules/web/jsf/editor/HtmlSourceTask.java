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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.web.jsf.editor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 * 
 * @author Marek Fukala
 */
public final class HtmlSourceTask extends ParserResultTask<HtmlParserResult> {

    private static final String CSS_CLASS_MAP_PROPERTY_KEY = "cssClassTagAttrMap"; //semi api - defined in HtmlLexer
    private static final String STYLE_CLASS_ATTR_NAME = "styleClass"; //NOI18N

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();
            if (mimeType.equals("text/html")) { //NOI18N
                return Collections.singletonList(new HtmlSourceTask());
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }

    @Override
    public int getPriority() {
        return 50; //todo use reasonable number
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }

    @Override
    public void cancel() {
        //no-op
    }

    @Override
    public void run(HtmlParserResult result, SchedulerEvent event) {
        Source source = result.getSnapshot().getSource();

        //embedding stuff: process only xhtml file contents, while the task needs to be bound to text/html
        if (!source.getMimeType().equals(JsfUtils.XHTML_MIMETYPE)) { //NOI18N
            return;
        }

        JsfSupportImpl sup = JsfSupportImpl.findFor(source); //activate the jsf support
        if (sup == null) {
            return;
        }

        //enable EL support it this xhtml file
        //TODO possibly add if(jsf_used()) { //enable el }
        Document doc = result.getSnapshot().getSource().getDocument(true);
        if (doc == null) {
            return;
        }
        InputAttributes inputAttributes = (InputAttributes) doc.getProperty(InputAttributes.class);
        if (inputAttributes == null) {
            inputAttributes = new InputAttributes();
//            inputAttributes.setValue(HTMLTokenId.language(), "enable el", new Object(), false); //NOI18N
            doc.putProperty(InputAttributes.class, inputAttributes);
        }

        //enable css class embedding in default facelets libraries tags
        //TODO this should be done in some more generic way but so far I haven't
        //found a way how to get an info if a tag's attribute represents css class or not.
        //It seems that almost only html library contains such tags, we should
        //probably create some metadata also for third party libraries

        //check if the default html library is defined
        String prefix = NamespaceUtils.getForNs(result.getNamespaces(), DefaultLibraryInfo.HTML.getNamespace());
        if (prefix != null) {
            //html lib declared, lets build a map of tags containing attributes whose values are
            //supposed to represent a css class. The map is then put into the document's
            //input attributes and then html lexer takes this information into account
            //when lexing the html code
            Map<String, Collection<String>> cssClassTagAttrMap = new HashMap<>();
            Library lib = sup.getLibrary(DefaultLibraryInfo.HTML.getNamespace());
            if (lib != null) {
                Collection<? extends LibraryComponent> components = lib.getComponents();
                for (LibraryComponent comp : components) {
                    Tag tag = comp.getTag();
                    //hacking datatable's attributes embedding - waiting for Tomasz' tag metadata API
                    if ("dataTable".equals(tag.getName())) { //NOI18N
                        cssClassTagAttrMap.put(prefix + ":" + tag.getName(),
                                Arrays.asList(new String[]{STYLE_CLASS_ATTR_NAME,
                                    "headerClass", "footerClass", "rowClasses", "columnClasses", "captionClass"})); //NOI18N
                    } else {
                        if (tag.getAttribute(STYLE_CLASS_ATTR_NAME) != null) {
                            cssClassTagAttrMap.put(prefix + ":" + tag.getName(), Collections.singletonList(STYLE_CLASS_ATTR_NAME));
                        }
                    }
                }
            }

            inputAttributes.setValue(HTMLTokenId.language(), CSS_CLASS_MAP_PROPERTY_KEY, cssClassTagAttrMap, true);

        } else {
            //remove the map, the html library is not declared (anymore)
            inputAttributes.setValue(HTMLTokenId.language(), CSS_CLASS_MAP_PROPERTY_KEY, null, true);
        }


    }
}

