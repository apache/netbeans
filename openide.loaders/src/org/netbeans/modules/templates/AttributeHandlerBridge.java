/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.templates;

import org.openide.loaders.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.templates.CreateDescriptor;
import org.openide.filesystems.FileObject;
import org.netbeans.api.templates.CreateFromTemplateAttributes;
import org.netbeans.modules.openide.loaders.DataObjectAccessor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Bridges loader-based handler registration to the fileobject-based one. Provides
 * compatibility with NB &lt;= 8.0.1. New clients are encouraged to use the new
 * {@link CreateFromTemplateAttributes} interface directly.
 * 
 * @author sdedic
 */
@ServiceProvider(service = CreateFromTemplateAttributes.class, position = Integer.MIN_VALUE + 1)
public class AttributeHandlerBridge implements CreateFromTemplateAttributes {
    private Lookup.Result<CreateFromTemplateAttributesProvider> providers;

    public AttributeHandlerBridge() {
        providers = Lookup.getDefault().lookupResult(CreateFromTemplateAttributesProvider.class);
    }
    
    @Override
    public Map<String, ?> attributesFor(CreateDescriptor desc) {
        FileObject template = desc.getTemplate();
        FileObject target = desc.getTarget();
        Collection<? extends CreateFromTemplateAttributesProvider> c = providers.allInstances();
        if (c.isEmpty()) {
            return Collections.emptyMap();
        }
        DataObject d;
        DataFolder fld;
        
        try {
            d = DataObject.find(template);
            fld = DataFolder.findFolder(target);
        } catch (DataObjectNotFoundException ex) {
            // ???
            Exceptions.printStackTrace(ex);
            return Collections.emptyMap();
        }
        HashMap<String,Object> all = new HashMap<String,Object>();
        for (CreateFromTemplateAttributesProvider p : c) {
            // must use getName, since some features may rely on that null propagates to the Provider 
            // if the initiator does not specify a name.
            Map<String, ? extends Object> map = p.attributesFor(d, fld, DataObjectAccessor.DEFAULT.getOrigName());
            if (map != null) {
                for (Map.Entry<String,? extends Object> e : map.entrySet()) {
                    all.put(e.getKey(), e.getValue());
                }
            }
        }
        
        return all;
    }
}
