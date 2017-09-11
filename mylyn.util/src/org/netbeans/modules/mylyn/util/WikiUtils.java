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

    private final static Logger LOG = Logger.getLogger("org.netbeans.mylym.utils.WikiUtils"); //NOI18N

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
