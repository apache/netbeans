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

package org.netbeans.modules.cnd.asm.model.lang;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public final class AsmElementBuilder {

    //private final AsmElementPath curPath;
    private final List<AsmElement> elements;

    public static AsmElementBuilder create(AsmElementPath curPath) {
        return new AsmElementBuilder(curPath, new LinkedList<AsmElement>());
    }

    AsmElementBuilder(AsmElementPath curPath, List<AsmElement> emptyList) {
        //this.curPath = curPath;
        this.elements = emptyList;
    }

    public List<AsmElement> get() {
        List<AsmElement> ret = new ArrayList<AsmElement>(elements);
        return ret; //Collections.unmodifiableList(ret);
    }

    public void add(AsmElement el) {
        elements.add(el);
    }

    public AsmElementPath addAndGetPath(AsmElement el) {
        elements.add(el);
        return null;
    }

    public int size() {
        return elements.size();
    }


}
