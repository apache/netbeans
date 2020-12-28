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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.source.elements;

import org.netbeans.modules.csl.api.ElementKind;

/**
 *
 * @author Tor Norbye
 */
public class IndexedMethod extends IndexedElement {
    private String[] params;

    public IndexedMethod(String name, ElementKind kind, String url, String module, String clz, String signature) {
        super(name, kind, url, module, clz, signature);
    }

    public String[] getParams() {
        if (params == null) {
            //int argsBegin = name.length()+3;
            int argsBegin = getAttributeSection(IndexedElement.ARG_INDEX);
            int argsEnd = attributes.indexOf(';', argsBegin);
            assert argsEnd != -1 : attributes;
            if (argsEnd > argsBegin) {
                String paramString = attributes.substring(argsBegin, argsEnd);
                params = paramString.split(",");
            } else {
                params = new String[0];
            }
        }

        return params;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedMethod other = (IndexedMethod)obj;
        if (this.attributes != other.attributes && (this.attributes == null || !this.attributes.equals(other.attributes))) {
            return false;
        }
        if (this.clz != other.clz && (this.clz == null || !this.clz.equals(other.clz))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.attributes != null ? this.attributes.hashCode() : 0);
        hash = 53 * hash + (this.clz != null ? this.clz.hashCode() : 0);
        return hash;
    }
}
