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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.apt.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.openide.util.CharSequences;

/**
 *
 */
public final class APTIncludePathStorage {
    private final ConcurrentMap<CharSequence, List<IncludeDirEntry>> allIncludes = new ConcurrentHashMap<CharSequence, List<IncludeDirEntry>>();
    
    public APTIncludePathStorage() {
    }

    public List<IncludeDirEntry> get(CharSequence configID,  List<IncludePath> includes) {
        CharSequence key = CharSequences.create(configID);
        List<IncludeDirEntry> list = allIncludes.get(key);
        if (list == null) {
            // create new one with light char sequences and put in map
            list = new ArrayList<IncludeDirEntry>(includes.size());
            for (IncludePath cs : includes) {
                IncludeDirEntry inclEntry = IncludeDirEntry.get(cs.getFSPath(), cs.isFramework(), cs.ignoreSysRoot());
                list.add(inclEntry);
            }
            List<IncludeDirEntry> old = allIncludes.putIfAbsent(key, list);
            if (old != null) {
                list = old;
            }
        } else {
            for (IncludeDirEntry e : list) {
                e.resetNonExistanceFlag();
            }
        }
        return list;
    }
    
    public void dispose() {
        allIncludes.clear();
    }
}
