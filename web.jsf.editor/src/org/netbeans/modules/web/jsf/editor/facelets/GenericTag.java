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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.web.jsfapi.api.Attribute;
import org.netbeans.modules.web.jsfapi.api.Tag;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public abstract class GenericTag implements Tag {

    /*
     * JSF spec 3.1.12 Render-Independent Properties: 
     * Read-Write: id, parent, rendered, rendererType, transient; 
     * Read-Only:  rendersChildren
    */
    private static final String[] DEFAULT_ATTRS = new String[]{"id", "parent", "rendered",
        "rendererType", "transient", "class" /* not in the spec */}; //NOI18N
    
    private final AtomicReference<Map<String, Attribute>> attrs = new AtomicReference<>();

    @Override
    public Collection<Attribute> getAttributes() {
        return getGenericAttributes().values();
    }

    @Override
    public Attribute getAttribute(String name) {
        return getGenericAttributes().get(name);
    }

    @Override
    public boolean hasNonGenenericAttributes() {
        return false;
    }

    protected Map<String, Attribute> getAdditionalGenericAttributes() {
        return Collections.emptyMap();
    }

    private Map<String, Attribute> getGenericAttributes() {
        if (attrs.compareAndSet(null, new HashMap<String, Attribute>())) {
            //add the default ID attribute
            for (String defaultAttributeName : DEFAULT_ATTRS) {
                if (getAttribute(defaultAttributeName) == null) {
                    attrs.get().put(defaultAttributeName, 
                            new Attribute.DefaultAttribute(defaultAttributeName, 
                            NbBundle.getMessage(GenericTag.class, new StringBuilder().append("HELP_").append(defaultAttributeName).toString()), false)); //NOI18N
                }
            }
            for (Map.Entry<String, Attribute> entry : getAdditionalGenericAttributes().entrySet()) {
                if (getAttribute(entry.getKey()) == null) {
                    attrs.get().put(entry.getKey(), entry.getValue());
                }
            }
        }

        return attrs.get();
    }
    
}
