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

package org.netbeans.modules.schema2beans;

/**
 * @author cliffwd
 * A QName class for dealing with XML Qualified Names; basically,
 * a namespace and a localpart.
 *
 * This class is intended solely for those who for some reason can't
 * use javax.xml.namespace.QName.  See that class for documentation.
 * Remember that prefix is not part of equals or hashCode.
 */
public class QName {
    private String namespaceURI;
    private String localPart;
    private String prefix;

    public QName(String localPart) {
        this("", localPart, "");
    }

    public QName(String namespaceURI, String localPart) {
        this(namespaceURI, localPart, "");
    }

    public QName(String namespaceURI, String localPart, String prefix) {
        if (namespaceURI == null)
            namespaceURI = "";
        this.namespaceURI = namespaceURI;
        if (localPart == null)
            throw new IllegalArgumentException("localPart == null");
        this.localPart = localPart;
        this.prefix = prefix;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getLocalPart() {
        return localPart;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof QName))
            return false;
        QName q = (QName) o;
        if (!namespaceURI.equals(q.namespaceURI))
            return false;
        if (!localPart.equals(q.localPart))
            return false;
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37*result + namespaceURI.hashCode();
        result = 37*result + localPart.hashCode();
        return result;
    }

    public String toString() {
        if ("".equals(namespaceURI))
            return localPart;
        else
            return "{"+namespaceURI+"}"+localPart;
    }

    public static QName valueOf(String asString) {
        int pos = asString.indexOf('}');
        if (pos < 0) {
            return new QName(asString);
        } else {
            String ns = asString.substring(1, pos-1);
            String localPart = asString.substring(pos+1);
            return new QName(ns, localPart);
        }
    }
}
