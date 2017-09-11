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
package org.openide.filesystems;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * @author Ales Novak
 */
final class PathElements {
    private static final String DELIMITER = "/"; // NOI18N

    /** Original name */
    private final String name;

    /** tokenizer */
    private StringTokenizer tokenizer;

    /** tokens */
    private final List<String> tokens;

    /** Creates new PathElements */
    public PathElements(String name) {
        this.name = name;
        tokenizer = new StringTokenizer(name, DELIMITER);
        tokens = new ArrayList<String>(10);
    }

    /**
     * @return original name
     */
    public String getOriginalName() {
        return name;
    }

    public Enumeration<String> getEnumeration() {
        return new EnumerationImpl(this);
    }

    synchronized boolean contains(int i) {
        if (tokens.size() <= i) {
            scanUpTo(i);
        }

        return (tokens.size() > i);
    }

    synchronized String get(int i) throws NoSuchElementException {
        if (tokens.size() <= i) {
            scanUpTo(i);
        }

        if (tokens.size() <= i) {
            throw new NoSuchElementException();
        }

        return tokens.get(i);
    }

    private synchronized void scanUpTo(int i) {
        if (tokenizer == null) {
            return;
        }

        if (tokens.size() > i) {
            return;
        }

        for (int k = tokens.size() - 1; (k < i) && tokenizer.hasMoreTokens(); k++) {
            tokens.add(tokenizer.nextToken());
        }

        if (!tokenizer.hasMoreTokens()) {
            tokenizer = null;
        }
    }

    /** Impl of enumeration */
    static final class EnumerationImpl implements Enumeration<String> {
        private PathElements elements;
        private int pos;

        EnumerationImpl(PathElements elements) {
            this.elements = elements;
            this.pos = 0;
        }

        /** From Enumeration */
        @Override
        public boolean hasMoreElements() {
            return elements.contains(pos);
        }

        /** From Enumeration */
        @Override
        public String nextElement() throws NoSuchElementException {
            return elements.get(pos++);
        }
    }
}
