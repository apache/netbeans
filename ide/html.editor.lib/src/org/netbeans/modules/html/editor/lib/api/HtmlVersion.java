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
package org.netbeans.modules.html.editor.lib.api;

import org.netbeans.modules.html.editor.lib.dtd.DTD;
import org.openide.util.NbBundle;

/**
 *
 * @author marekfukala
 */
public enum HtmlVersion {

    HTML32(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_32"),
        "-//W3C//DTD HTML 3.2 Final//EN", //NOI18N
        null),
    HTML40_STRICT(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_40_STRICT"),
        "-//W3C//DTD HTML 4.0//EN",
        "http://www.w3.org/TR/REC-html40/strict.dtd"), //NOI18N

    HTML40_TRANSATIONAL(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_40_TRAN"),
        "-//W3C//DTD HTML 4.0 Transitional//EN",
        "http://www.w3.org/TR/REC-html40/loose.dtd"), //NOI18N

    HTML40_FRAMESET(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_40_FRAM"),
        "-//W3C//DTD HTML 4.0 Frameset//EN",
        "http://www.w3.org/TR/REC-html40/frameset.dtd"), //NOI18N

    HTML41_STRICT(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_401_STRICT"),
        "-//W3C//DTD HTML 4.01//EN",
        "http://www.w3.org/TR/html4/strict.dtd"), //NOI18N

    HTML41_TRANSATIONAL(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_401_TRAN"),
        "-//W3C//DTD HTML 4.01 Transitional//EN",
        "http://www.w3.org/TR/html4/loose.dtd"), //NOI18N

    HTML41_FRAMESET(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_401_FRAM"),
        "-//W3C//DTD HTML 4.01 Frameset//EN",
        "http://www.w3.org/TR/html4/frameset.dtd"), //NOI18N

    HTML5(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_5"), null, null), //no public id nor system id, just <!doctype html>

    XHTML5(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_X5"),
        null, //no public id nor system id, just <!doctype html>
        null,
        null,
        "http://www.w3.org/1999/xhtml",
        true),

    XHTML10_STICT(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_X10_STRICT"),
        "-//W3C//DTD XHTML 1.0 Strict//EN",
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
        null,
        "http://www.w3.org/1999/xhtml",
        true), //NOI18N

    XHTML10_TRANSATIONAL(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_X10_TRAN"),
        "-//W3C//DTD XHTML 1.0 Transitional//EN",
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
        null,
        "http://www.w3.org/1999/xhtml",
        true), //NOI18N

    XHTML10_FRAMESET(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_X10_FRAM"),
        "-//W3C//DTD XHTML 1.0 Frameset//EN",
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd",
        null,
        "http://www.w3.org/1999/xhtml",
        true), //NOI18N

    //XHTML 1.1 version fallbacks to XHTML 1.0 strict since the current SGML parser
    //cannot properly parse the XHTML1.1 dtd
    XHTML11(NbBundle.getMessage(HtmlVersion.class, "MSG_HTML_VERSION_X11"),
        "-//W3C//DTD XHTML 1.1//EN",
        "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
        "-//W3C//DTD XHTML 1.0 Strict//EN",
        "http://www.w3.org/1999/xhtml",
        true); //NOI18N

    private static final String DOCTYPE_PREFIX = "<!DOCTYPE html PUBLIC \""; //NOI18N
    private static final String HTML5_DOCTYPE = "<!DOCTYPE html>"; //NOI18N
    private static final String XHTML5_DOCTYPE = HTML5_DOCTYPE;

    public static HtmlVersion find(String publicId, String namespace) {
        if(publicId == null) {
            //x/html5
            return XHTML5.getDefaultNamespace().equals(namespace) ? XHTML5 : HTML5;
        } else {
            for (HtmlVersion version : HtmlVersion.values()) {
            if (publicId.equals(version.getPublicID())) {
                    return version;
                }
            }
        }
        return null;
    }

    /** The default html version. */
    private static final HtmlVersion DEFAULT_HTML_VERSION = HTML5;
    private static final HtmlVersion DEFAULT_XHTML_VERSION = XHTML5;
    public static HtmlVersion DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = null;
    
    public static HtmlVersion getDefaultVersion() {
        return DEFAULT_VERSION_UNIT_TESTS_OVERRIDE != null ? DEFAULT_VERSION_UNIT_TESTS_OVERRIDE : DEFAULT_HTML_VERSION;
    }

    public static HtmlVersion getDefaultXhtmlVersion() {
        return DEFAULT_VERSION_UNIT_TESTS_OVERRIDE != null ? DEFAULT_VERSION_UNIT_TESTS_OVERRIDE : DEFAULT_XHTML_VERSION;
    }


    private final String displayName;
    private final String publicID, systemID;
    private final String fallbackPublicID;
    private final String defaultNamespace;
    private boolean isXhtml;

    private HtmlVersion(String displayName, String publicID, String systemID) {
        this(displayName, publicID, systemID, null, null, false);
    }

    private HtmlVersion(String displayName, String publicID, String systemID, String fallbackPublicID, String defaultNamespace, boolean isXhtml) {
        this.publicID = publicID;
        this.systemID = systemID;
        this.defaultNamespace = defaultNamespace;
        this.isXhtml = isXhtml;
        this.fallbackPublicID = fallbackPublicID;
        this.displayName = displayName;
    }

    public String getPublicID() {
        return publicID;
    }

    public String getSystemId() {
        return systemID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    public String getDoctypeDeclaration() {
        switch (this) {
            case XHTML5:
                return XHTML5_DOCTYPE;
            case HTML5:
                return HTML5_DOCTYPE;
            default:
                StringBuilder b = new StringBuilder();
                b.append(DOCTYPE_PREFIX);
                b.append(getPublicID());
                b.append('"');

                if (getSystemId() != null) {
                    b.append(" \"");
                    b.append(getSystemId());
                    b.append('"');
                }
                b.append('>');

                return b.toString();
        }
    }

    public boolean isXhtml() {
        return this.isXhtml;
    }

    public DTD getDTD() {
        //use the fallback public id to get the DTD if defined, otherwise
        //use the proper public id. This is needed due to the lack of parser
        //for XHTML 1.1 file. Such files are parsed according to the XHTML1.0 DTD.
        String publicid = fallbackPublicID != null ? fallbackPublicID : publicID;

        return org.netbeans.modules.html.editor.lib.dtd.Registry.getDTD(publicid, null);
    }
}
