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
package org.netbeans.modules.junit;

import java.util.Comparator;
import org.netbeans.api.project.libraries.Library;

/**
 * Comparator of JUnit libraries - compares versions of JUnit libraries.
 *
 * @author  Marian Petras
 */
final class JUnitLibraryComparator implements Comparator<Library> {

    public int compare(Library l1, Library l2) {
        String name1 = l1.getName().toLowerCase();
        String name2 = l2.getName().toLowerCase();

        if (name1.equals(name2)) {
            return 0;
        } else if (name1.equals("junit")) {                             //NOI18N
            return -1;
        } else if (name2.equals("junit")) {                             //NOI18N
            return 1;
        }

        final String[] parts1 = name1.substring(5).split("_|\\W");      //NOI18N
        final String[] parts2 = name2.substring(5).split("_|\\W");      //NOI18N
        final int min = Math.min(parts1.length, parts2.length);
        for (int i = 0; i < min; i++) {
            int partCmp = parts1[i].compareTo(parts2[i]);
            if (partCmp != 0) {
                return partCmp;
            }
        }
        return parts2.length - parts1.length;
    }

}
