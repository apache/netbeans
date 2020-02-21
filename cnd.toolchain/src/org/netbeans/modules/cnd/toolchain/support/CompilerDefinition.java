/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.toolchain.compilers.CompilerDefinitionAccessor;

public final class CompilerDefinition extends ArrayList<String> {
    
    static {
        CompilerDefinitionAccessor.register(new CompilerDefinitionAccessorImpl());
    }

    private List<Integer> userAddedDefinitions = new ArrayList<Integer>(0);

    public CompilerDefinition() {
        super();
    }

    public CompilerDefinition(int size) {
        super(size);
    }

    public CompilerDefinition(Collection<String> c) {
        super(c);
    }

    public boolean isUserAdded(int i) {
        return userAddedDefinitions.contains(i);
    }

    public void setUserAdded(boolean isUserAddes, int i) {
        if (isUserAddes) {
            if (!userAddedDefinitions.contains(i)) {
                userAddedDefinitions.add(i);
            }
        } else if (userAddedDefinitions.contains(i)) {
            userAddedDefinitions.remove(Integer.valueOf(i));
        }
    }

    public void sort() {
        Set<String> set = new HashSet<String>();
        for (Integer i : userAddedDefinitions) {
            if (i < size()) {
                set.add(get(i));
            }
        }
        Collections.sort(this, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
        userAddedDefinitions.clear();
        for (String s : set) {
            userAddedDefinitions.add(indexOf(s));
        }
    }
    
    private static class CompilerDefinitionAccessorImpl extends CompilerDefinitionAccessor {

        @Override
        public List<Integer> getUserAddedDefinitions(CompilerDefinition cdf) {
            return cdf.userAddedDefinitions;
        }
    
}
}
