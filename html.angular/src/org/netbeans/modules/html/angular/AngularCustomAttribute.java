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
package org.netbeans.modules.html.angular;

import java.io.IOException;
import org.netbeans.modules.html.angular.model.Directive;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.angular.model.DirectiveConvention;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.HelpResolver;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class AngularCustomAttribute implements CustomAttribute {

    private static final Logger LOGGER = Logger.getLogger(AngularCustomAttribute.class.getSimpleName());
    private static Map<DirectiveConvention, Collection<CustomAttribute>> dc2attr = null;
    private static Collection<CustomAttribute> attributes = null;

    public static synchronized Collection<CustomAttribute> getCustomAttributes(DirectiveConvention convention) {
        if (dc2attr == null) {
            //init
            dc2attr = new EnumMap<>(DirectiveConvention.class);
            for (DirectiveConvention dc : DirectiveConvention.values()) {
                Collection<CustomAttribute> attrs = new ArrayList<>();
                for (Directive ad : Directive.values()) {
                    attrs.add(new AngularCustomAttribute(ad, dc));
                }
                dc2attr.put(dc, attrs);
            }
        }
        return dc2attr.get(convention);
    }

    public static synchronized Collection<CustomAttribute> getCustomAttributes() {
        if (attributes == null) {
            //init
            attributes = new ArrayList<>();
            for (DirectiveConvention dc : DirectiveConvention.values()) {
                attributes.addAll(getCustomAttributes(dc));
            }
        }
        return attributes;
    }
    private Directive directive;
    private DirectiveConvention convention;

    public AngularCustomAttribute(Directive directive, DirectiveConvention convetion) {
        this.directive = directive;
        this.convention = convetion;
    }

    @Override
    public String getName() {
        return directive.getAttributeName(convention);
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public HelpItem getHelp() {
        return new HelpItem() {
            @Override
            public String getHelpHeader() {
                return new StringBuilder().append("<h2>").append(directive.getAttributeName(convention)).append("</h2>").toString(); //NOI18N
            }

            @Override
            public String getHelpContent() {
                return AngularDoc.getDefault().getDirectiveDocumentation(directive);
            }

            @Override
            public URL getHelpURL() {
                try {
                    return new URL(directive.getExternalDocumentationURL());
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }

            @Override
            public HelpResolver getHelpResolver() {
                return AJS_HELP_RESOLVER;
            }
        };
    }

    @Override
    public boolean isValueRequired() {
        return directive.isAttributeValueTypicallyUsed();
    }
    private static final HelpResolver AJS_HELP_RESOLVER = new HelpResolver() {
        @Override
        public URL resolveLink(URL baseURL, String relativeLink) {
            LOGGER.log(Level.FINE, "relativeLink = ''{0}''", relativeLink); //NOI18N
            LOGGER.log(Level.FINE, "baseURL = ''{0}''", baseURL); //NOI18N

            try {
                //test if the relative link isn't an absolute link (http://site.org/file)
                URI u = new URI(relativeLink);
                if (u.isAbsolute()) {
                    LOGGER.log(Level.FINE, "resolved to = ''{0}''", u.toURL()); //NOI18N
                    return u.toURL();
                }
            } catch (MalformedURLException | URISyntaxException ex) {
                LOGGER.log(Level.FINE, null, ex); //basically ignore the exception
            }


            String link = null;

            if (relativeLink.startsWith("#")) {
                assert baseURL != null : "Base URL must be provided for local relative links (anchors)."; //NOI18N
                String base = baseURL.toExternalForm();
                //link within the same file
                int hashIdx = base.indexOf('#');
                if (hashIdx != -1) {
                    base = base.substring(0, hashIdx);
                }
                link = base + relativeLink;
            } else {
                //link contains a filename
                if (baseURL != null) {
                    URL url = getRelativeURL(baseURL, relativeLink);
                    LOGGER.log(Level.FINE, "resolved to = ''{0}''", url); //NOI18N
                    return url;
                }
            }
            if (link != null) {
                try {
                    URL url = new URI(link).toURL();
                    LOGGER.log(Level.FINE, "resolved to = ''{0}''", url); //NOI18N
                    return url;
                } catch (URISyntaxException | MalformedURLException ex) {
                    LOGGER.log(Level.FINE, null, ex); //basically ignore the exception
                }
            }
            
            LOGGER.fine("cannot be resolved!"); //NOI18N
            return null;
        }

        private URL getRelativeURL(URL baseurl, String link) {
            if(link.trim().isEmpty()) {
                return null;
            }
            
            if (link.startsWith("./")) {
                link = link.substring(2);
            }
            String url = baseurl.toString();
            int index;
            if (link.trim().charAt(0) == '#') {
                index = url.indexOf('#');
                if (index > -1) {
                    url = url.substring(0, url.indexOf('#'));
                }
                url = url + link;
            } else {
                index = 0;
                url = url.substring(0, url.lastIndexOf('/'));
                while ((index = link.indexOf("../", index)) > -1) {      //NOI18N
                    url = url.substring(0, url.lastIndexOf('/'));
                    link = link.substring(index + 3);
                }
                url = url + "/" + link; // NOI18N
            }
            try {
                return new URL(url);
            } catch (java.net.MalformedURLException e) {
                LOGGER.log(Level.FINE, null, e); //basically ignore the exception
            }
            return null;
        }

        @Override
        public String getHelpContent(URL url) {
            try {
                return Utils.getContentAsString(url, null);
            } catch (IOException ex) {
                String message = Bundle.doc_cannotGet(url.toString());
                LOGGER.log(Level.INFO, message); //NOI18N
                return message;
            }
        }

        
    };
}
