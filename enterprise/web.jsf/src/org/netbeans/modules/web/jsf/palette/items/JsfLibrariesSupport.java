/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.palette.items;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.DataLoadersBridge;
import org.netbeans.modules.html.editor.lib.api.HtmlParsingResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.web.common.api.WebUtils;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.api.Library;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfLibrariesSupport {

    private JTextComponent tc;
    private Map<DefaultLibraryInfo, LibraryImport> map = new EnumMap<DefaultLibraryInfo, LibraryImport>(DefaultLibraryInfo.class);
    private JsfSupport jsfs;

    public JsfLibrariesSupport(JTextComponent tc) {
        this.tc = tc;
        initLibraries(tc);
    }

    static JsfLibrariesSupport get(JTextComponent tc) {
        return new JsfLibrariesSupport(tc);
    }

    private void initLibraries(JTextComponent tc) {
        Document doc = tc.getDocument();
        FileObject file = DataLoadersBridge.getDefault().getFileObject(doc);
        if(file == null) {
            return ;
        }
        jsfs = JsfSupportProvider.get(file);
        if (jsfs == null) {
            return;
        }

        //1. check if the http://java.sun.com/jsf/html library is declared and if so under what prefix
        Source source = Source.create(doc);
        final AtomicReference<HtmlParsingResult> result = new AtomicReference<HtmlParsingResult>();
        try {
            ParserManager.parse(Collections.singletonList(source), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ResultIterator htmlRi = WebUtils.getResultIterator(resultIterator, "text/html"); //NOI18N
                    if (htmlRi != null) {
                        Parser.Result pr = htmlRi.getParserResult();
                        if (pr instanceof HtmlParsingResult) {
                            result.set((HtmlParsingResult) pr);
                        }
                    }
                }
            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }

        HtmlParsingResult htmlresult = result.get();
        Map<String, Collection<String>> ns2prefixes = htmlresult != null
                ? htmlresult.getSyntaxAnalyzerResult().getAllDeclaredNamespaces()
                : Collections.<String, Collection<String>>emptyMap();
        
        for (DefaultLibraryInfo libraryInfo : DefaultLibraryInfo.values()) {
            LibraryImport libraryimport = new LibraryImport();

            Library lib = jsfs.getLibrary(libraryInfo.getNamespace());
            libraryimport.lib = lib;

            Collection<String> prefixes = ns2prefixes.get(libraryInfo.getNamespace());
            if (prefixes == null && libraryInfo.getLegacyNamespace() != null) {
                prefixes = ns2prefixes.get(libraryInfo.getLegacyNamespace());
            }
            if (libraryInfo.getLegacyNamespace() != null && ns2prefixes.get(libraryInfo.getLegacyNamespace()) != null) {
                prefixes.addAll(ns2prefixes.get(libraryInfo.getLegacyNamespace()));
            }
            libraryimport.declaredPrefix = prefixes != null && !prefixes.isEmpty() ? prefixes.iterator().next() : null;

            map.put(libraryInfo, libraryimport);
        }


    }

    public void importLibraries(DefaultLibraryInfo... linfos) {
        Map<Library, String> toimport = new HashMap<Library, String>();
        for (DefaultLibraryInfo li : linfos) {
            LibraryImport limport = map.get(li);
            assert limport != null;
            if (limport.declaredPrefix == null) {
                //not imported yet, lets do it
                toimport.put(limport.lib, null); //lets use the default prefix
            }
        }
        LibraryUtils.importLibrary(tc.getDocument(), toimport, jsfs.isJsf22Plus());
    }

    /** @return the library default prefix in the case it hasn't been declared yet or the declared prefix */
    public String getLibraryPrefix(DefaultLibraryInfo li) {
        LibraryImport limport = map.get(li);
        if (limport == null) {
            return li.getDefaultPrefix();
        }
        return limport.declaredPrefix != null ? limport.declaredPrefix : limport.lib.getDefaultPrefix();
    }

    public boolean isJsf22Plus() {
        return jsfs.isJsf22Plus();
    }

    private static class LibraryImport {
        public Library lib;
        public String declaredPrefix;
    }

}
