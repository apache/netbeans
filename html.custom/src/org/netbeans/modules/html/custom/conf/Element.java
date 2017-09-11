/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.custom.conf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author marek
 */
public abstract class Element {

    private final String name, description, documentation, documentationURL;
    private final Collection<String> contextNames = new ArrayList<>();
    private final Tag parent;
    private final boolean required;

    
    public Element(String name, String description, String documentation, String documentationURL, Tag parent, boolean required, String... contexts) {
        this.name = name;
        this.description = description;
        this.documentation = documentation;
        this.documentationURL = documentationURL;
        this.parent = parent;
        this.contextNames.addAll(Arrays.asList(contexts));
        this.required = required;
        
        //if the element is not context-free then its context is also the parent element
        if(parent != null) {
            contextNames.add(parent.getName());
        }
    }

    public boolean isRequired() {
        return required;
    }
    
    public void addContext(String tagName) {
        contextNames.add(tagName);
    }

    public String getName() {
        return name;
    }

    public Collection<String> getContexts() {
        return contextNames;
    }

    public Tag getParent() {
        return parent;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getDocumentationURL() {
        return documentationURL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=");
        sb.append(name);
        if (parent != null) {
            sb.append(',');

            sb.append("parent=");
            sb.append(parent != null ? parent.getName() : null);
        }
        if (getContexts() != null) {
            sb.append(',');
            sb.append("contexts={");
            for (String ctx : getContexts()) {
                sb.append(ctx);
                sb.append(',');
            }
            sb.append("}");
        }
        if (description != null) {
            sb.append(',');
            sb.append("description=");
            sb.append(description);
        }
        if (documentation != null) {
            sb.append(',');
            sb.append("doc=");
            sb.append(documentation);
        }

        if (documentationURL != null) {
            sb.append(',');
            sb.append("doc_url=");
            sb.append(documentationURL);
        }

        return sb.toString();
    }

}
