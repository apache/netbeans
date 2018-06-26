/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.facelets;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.web.jsfapi.api.Tag;

/**
 * Composite library descriptor. The first library has the precedence.
 *
 * @author marekfukala
 */
public class CompositeLibraryDescriptor implements LibraryDescriptor {

    private LibraryDescriptor first, second;

    public CompositeLibraryDescriptor(LibraryDescriptor first, LibraryDescriptor second) {
        this.first = first;
        this.second = second;
        
        if(first.getNamespace() != null 
                && second.getNamespace() != null 
                && !first.getNamespace().equals(second.getNamespace())) {
            //both must have the same namespace if there's any
            throw new IllegalArgumentException(String.format("Both libraries must declare the same namespace if there's any. "
                    + "Currently the first one declares %s and the second %s!", first.getNamespace(), second.getNamespace())); //NOI18N
        }
        
    }
    
    @Override
    public String getNamespace() {
        return first.getNamespace() != null ? first.getNamespace() : second.getNamespace();
    }
    
    @Override
    public String getPrefix() {
        return first.getPrefix() != null ? first.getPrefix() : second.getPrefix();
    }

     @Override
    public Map<String, Tag> getTags() {
        //merge
        Map<String, Tag> s = first.getTags();
        Map<String, Tag> t = second.getTags();

        Map<String, Tag> result = new HashMap<>();

        Collection<String> allTagNames = new HashSet<>();
        allTagNames.addAll(s.keySet());
        allTagNames.addAll(t.keySet());
        for(String tagName : allTagNames) {
            result.put(tagName, new ProxyTag(s.get(tagName), t.get(tagName)));
        }

        return result;
    }

    
}
