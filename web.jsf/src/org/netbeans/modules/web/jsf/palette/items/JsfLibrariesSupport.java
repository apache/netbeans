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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
