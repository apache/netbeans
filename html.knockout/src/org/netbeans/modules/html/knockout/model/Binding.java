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
package org.netbeans.modules.html.knockout.model;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.html.knockout.KOHelpItem;

/**
 * KO binding types.
 *
 * http://knockoutjs.com/documentation/introduction.html
 *
 * TODO add metadata for the bindings
 *
 * @author marekfukala
 */
public enum Binding implements KOHelpItem {

    //text and appearance
    visible,
    text,
    textInput,
    html,
    css,
    style,
    attr,
    //control flow
    foreach,
    _if, //real name is "IF"
    ifnot,
    with,
    //fields
    click,
    event,
    submit,
    enable,
    disable,
    value,
    hasfocus,
    checked,
    options,
    selectedOptions,
    uniqueName,
    //rendering
    component,
    template;

    public static final String DOC_CHARSET = "UTF-8"; //NOI18N
    
    private static final String DOC_URL_BASE = "http://knockoutjs.com/documentation/"; //NOI18N
    private static final String DOC_URL_POSTFIX = "-binding.html"; //NOI18N

    private static final Map<String, Binding> NAMES2BINDINGS = new HashMap<>();
    static {
        for(Binding d : values()) {
            NAMES2BINDINGS.put(d.getName(), d);
        }
    }
    
    public static Binding getBinding(String name) {
        return NAMES2BINDINGS.get(name);
    }
    
    /**
     * Gets the binding name.
     *
     * Use this instead of {@link #name()}.
     *
     * @return name of the KO binding.
     */
    @NonNull
    @Override
    public String getName() {
        return name().charAt(0) == '_' ? name().substring(1) : name();
    }

    @Override
    public String getExternalDocumentationURL() {
        return new StringBuilder()
                .append(DOC_URL_BASE)
                .append(getName().equals(Binding.textInput.name()) ? getName().toLowerCase() : getName()) // workaround for issue #246945
                .append(DOC_URL_POSTFIX)
                .toString();
    }
}
