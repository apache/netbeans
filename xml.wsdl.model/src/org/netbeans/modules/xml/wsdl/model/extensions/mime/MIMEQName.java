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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wsdl.model.extensions.mime;

import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author jyang
 */
public enum MIMEQName {

    CONTENT(createMIMEQName("content")),
    MULTIPART_RELATED(createMIMEQName("multipartRelated")),
    PART(createMIMEQName("part")),
    MIME_XML(createMIMEQName("mimeXml"));
    private static Set<QName> qnames = null;
    private final QName qName;
    public static final String MIME_NS_URI = "http://schemas.xmlsoap.org/wsdl/mime/";
    public static final String MIME_NS_PREFIX = "mime";
    public static final String ATTR_PART = "part";
    public static final String ELEM_CONTENT = "content";
    public static final String ELEM_MULTIPART_RELATED = "multipartRelated";
    public static final String ELEM_MIME_XML = "mimeXml";

    public static QName createMIMEQName(String localName) {
        return new QName(MIME_NS_URI, localName, MIME_NS_PREFIX);
    }

    MIMEQName(QName name) {
        qName = name;
    }

    public QName getQName() {
        return qName;
    }

    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (MIMEQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
}

