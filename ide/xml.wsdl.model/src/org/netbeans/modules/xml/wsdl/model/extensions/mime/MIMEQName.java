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

