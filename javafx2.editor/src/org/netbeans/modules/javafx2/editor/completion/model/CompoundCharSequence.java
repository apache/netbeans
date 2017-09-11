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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author sdedic
 */
public final class CompoundCharSequence implements CharSequence {
    private List<CharSequence> parts;
    private int[] partBoundaries;
    private int startOffset;
    private volatile int partNo;
    private int len;
    
    public CompoundCharSequence(int startOffset, List<CharSequence> parts, int len) {
        this.startOffset = startOffset;
        this.parts = parts;
        this.partBoundaries = new int[parts.size()];
        
        if (startOffset >= parts.get(0).length()) {
            throw new IllegalArgumentException();
        }
        
        int pos = -startOffset;
        for (int i = 0; pos < len && i < parts.size(); i++) {
            partBoundaries[i] = (pos += parts.get(i).length());
        }
        if (len == -1) {
            this.len = partBoundaries[partBoundaries.length - 1];   
        } else {
            this.len = len;
        }
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= len) {
            throw new StringIndexOutOfBoundsException(index);
        }
        int pn = partNo;
        if (partBoundaries[pn] > index && (pn == 0 || partBoundaries[pn - 1] <= index)) {
            int start = pn == 0 ? 0 : partBoundaries[pn - 1];
            return parts.get(pn).charAt(index - start);
        } else {
            for (int i = 0; i < parts.size(); i++) {
                if (partBoundaries[i] > index) {
                    int start = i == 0 ? startOffset : partBoundaries[i - 1];
                    partNo = i;
                    return parts.get(i).charAt(index - start);
                }
            }
        }
        // should never happen.
        throw new StringIndexOutOfBoundsException(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (end > len) {
            throw new StringIndexOutOfBoundsException(end);
        }
        for (int i = 0; i < parts.size(); i++) {
            if (partBoundaries[i] > start) {
                int sO = i == 0 ? 0 : partBoundaries[i - 1];
                return new CompoundCharSequence(start - sO, parts.subList(i, parts.size()), end - start);
            }
        }
        throw new StringIndexOutOfBoundsException(start);
    }
}
