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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hanz
 */
public class TestComparator {

    private String      text;
    private boolean     failed = false;

    public TestComparator (String text) {
        this.text = text;
    }

    public void check (String line) {
        if (failed) return;
        if (!text.startsWith (line)) {
            failed = true;
            throw new IllegalArgumentException (line + "\nBut expecting:\n" + text);
        }
        text = text.substring (line.length ());
        if (text.startsWith ("\n"))
            text = text.substring (1);
    }

    private Map<Class,Map<Object,Integer>> classToObjects = new HashMap<Class,Map<Object,Integer>> ();

    public int get (Object o) {
        if (failed) return -1;
        Map<Object,Integer> objects = classToObjects.get (o.getClass ());
        if (objects == null) {
            objects = new HashMap<Object,Integer> ();
            classToObjects.put (o.getClass (), objects);
        }
        Integer i = objects.get (o);
        if (i == null) {
            i = objects.size () + 1;
            objects.put (o, i);
        }
        return i;
    }

    public String getResult () {
        return text;
    }
}


