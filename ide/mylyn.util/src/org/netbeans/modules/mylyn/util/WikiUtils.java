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
package org.netbeans.modules.mylyn.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;
import org.netbeans.modules.mylyn.util.wiki.WikiEditPanel;
import org.openide.util.Lookup;

/**
 *
 * @author jpeska
 */
public class WikiUtils {

    private static final Logger LOG = Logger.getLogger("org.netbeans.mylym.utils.WikiUtils"); //NOI18N

    private WikiUtils () {
    }

    public static WikiPanel getWikiPanel(String wikiLanguage, boolean editing, boolean switchable) {
        return new WikiEditPanel(wikiLanguage, editing, switchable);
    }

    public static String getHtmlFormatText(String wikiFormatText, String language) {
        if (language == null || language.isEmpty()) {
            LOG.log(Level.FINE, "Wiki language name is empty"); //NOI18N
            return null;
        }
        MarkupLanguage markupLanguage;
        ClassLoader originalContextCL = null;
        try {
            originalContextCL = setupContextClassLoader();
            markupLanguage = ServiceLocator.getInstance().getMarkupLanguage(language);
        } catch (IllegalArgumentException ex) {
            // issue #258571
            String msg = ex.getMessage();
            if(msg.startsWith("Cannot load markup language")) { // NOI18N
                LOG.log(Level.INFO, null, ex);       
                markupLanguage = null;
            } else {
                throw ex;
            }
        } finally {
            restoreContextClassLoader(originalContextCL);
        }
        if (markupLanguage == null) {
            LOG.log(Level.FINE, "Markup language for name {0} not found",language); //NOI18N
            return null;
        }
        MarkupParser parser = new MarkupParser(markupLanguage);
        String dirtyHtml = parser.parseToHtml(wikiFormatText);
        return cleanHtmlTags(dirtyHtml);
    }

    private static String cleanHtmlTags(String html) {
        html = html.replaceFirst("<\\?xml.*?>", ""); //NOI18N
        html = html.replaceAll("<html.*?>", "<html>"); //NOI18N
        html = html.replaceAll("<head>.*</head>", ""); //NOI18N

        boolean remove = html.contains("<body><p>"); //NOI18N
        if (remove) {
            // remove first <p> gap, looks ugly in the UI...
            html = html.replaceFirst("<body><p>", "<body>"); //NOI18N
            html = html.replaceFirst("</p>", ""); //NOI18N
        }
        return html;
    }

    // In JDeveloper we need the NetBeans system classloader as the thread context CL
    // in order to find wikitext registrations. The Equinox' ContextFinder used by default
    // is not usable. Unfortunatelly it's not possible to set the context classloader in
    // JDeveloper explicitly (no entry point). In NetBeans this code effectively does nothing.
    private static ClassLoader setupContextClassLoader() {
        ClassLoader systemCL = Lookup.getDefault().lookup(ClassLoader.class);
        if (systemCL != null) {
            ClassLoader currentContextCL = Thread.currentThread().getContextClassLoader();
            if (currentContextCL != null && currentContextCL != systemCL) {
                Thread.currentThread().setContextClassLoader(systemCL);
                return currentContextCL;
            }
        }
        return null;
    }

    private static void restoreContextClassLoader(ClassLoader originalContextCL) {
        if (originalContextCL != null) {
            Thread.currentThread().setContextClassLoader(originalContextCL);
        }
    }
}
