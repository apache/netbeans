/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            String localPart = asString.substring(pos+1, asString.length());
            return new QName(ns, localPart);
        }
    }
}
