/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.editor;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
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
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.api.LibraryComponent;
import org.netbeans.modules.web.jsfapi.api.LibraryInfo;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 *
 * @author Marek Fukala
 */
public final class HtmlSourceTask extends ParserResultTask<HtmlParserResult> {

    private static final String CSS_CLASS_MAP_PROPERTY_KEY = "cssClassTagAttrMap"; //semi api - defined in HtmlLexer
    private static final String CLASS = "Class"; //NOI18N
    private static final String CLASSES = "Classes"; //NOI18N
    private static final EnumSet<DefaultLibraryInfo> LIBRARIES_TO_SKIP = EnumSet.of(DefaultLibraryInfo.FACELETS, DefaultLibraryInfo.JSF,
            DefaultLibraryInfo.COMPOSITE, DefaultLibraryInfo.JSF_CORE, DefaultLibraryInfo.JSTL_CORE, DefaultLibraryInfo.JSTL_CORE_FUNCTIONS,
            DefaultLibraryInfo.PASSTHROUGH);

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            String mimeType = snapshot.getMimeType();
            if (mimeType.equals("text/html")) { //NOI18N
                return Collections.singletonList(new HtmlSourceTask());
            } else {
                return Collections.<SchedulerTask>emptyList();
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
        Map<String, Collection<String>> cssClassTagAttrMap = new HashMap<>();
        
        //lets build a map of tags containing attributes whose values are
        //supposed to represent a css class. The map is then put into the document's
        //input attributes and then html lexer takes this information into account
        //when lexing the html code
        for (Map.Entry<String, String> entry : result.getNamespaces().entrySet()) {
            String prefix = entry.getValue();
            if (prefix == null) {
                continue;
            }
            String namespace = entry.getKey();
            LibraryInfo libraryInfo = DefaultLibraryInfo.forNamespace(namespace);
            if (libraryInfo instanceof DefaultLibraryInfo dli && LIBRARIES_TO_SKIP.contains(dli)) {
                continue;
            }

            Library lib = sup.getLibrary(namespace);
            if (lib != null) {
                Collection<? extends LibraryComponent> components = lib.getComponents();
                for (LibraryComponent comp : components) {
                    Tag tag = comp.getTag();
                    if (tag == null) {
                        continue;
                    }
                    List<String> cssClassAttributes = tag.getAttributes().stream()
                            .map(Attribute::getName)
                            .filter(name -> name.endsWith(CLASS) || name.endsWith(CLASSES))
                            .toList();
                    if (!cssClassAttributes.isEmpty()) {
                        cssClassTagAttrMap.put(prefix + ":" + tag.getName(), cssClassAttributes);
                    }
                }
            }
        }

        inputAttributes.setValue(HTMLTokenId.language(), CSS_CLASS_MAP_PROPERTY_KEY, cssClassTagAttrMap, true);

    }
}
