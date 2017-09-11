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
package org.netbeans.modules.spring.api.beans.model;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.spring.beans.model.impl.SpringModelImplementation;

/**
 * Factory for getting or creating {@code MetadataModel<SpringModel>}.
 * 
 * @author marfous
 */
public final class SpringModelFactory {

    private static final Logger LOGGER = Logger.getLogger(SpringModelFactory.class.getName());    
    
    //protected due to JUnit tests
    protected static HashMap<ModelUnit, WeakReference<MetadataModel<SpringModel>>> 
            MODELS = new HashMap<ModelUnit, WeakReference<MetadataModel<SpringModel>>>();

    private SpringModelFactory() {
    }

    /**
     * Returns {@link MetadataModel} for given {@link ModelUnit}. If the 
     * {@code MetadataModel} isn't created and cached, new one originates.
     * 
     * @param unit {@link ModelUnit} of involved project (btw, {@code ModelUnit} can be
     * easily created by {@code SpringModelSupport}.
     * @return existing metamodel for given {@code ModelUnit} or created new one; never null
     */    
    public static synchronized MetadataModel<SpringModel> getMetaModel(ModelUnit unit) {
        WeakReference<MetadataModel<SpringModel>> reference = MODELS.get(unit);
        MetadataModel<SpringModel> metadataModel = null;
        if (reference != null) {
            metadataModel = reference.get();
        }
        if (metadataModel == null) {
            LOGGER.log(Level.FINE, "Metadata model not found in cache for model unit: {0}, reference: {1}", new Object[]{unit, reference});
            metadataModel = createMetaModel(unit);
            if (reference == null) {
                LOGGER.log(Level.FINE, "No reference found, creating new one.");
                reference = new WeakReference<MetadataModel<SpringModel>>(metadataModel);
            }
            MODELS.put(unit, reference);
        }
        return metadataModel;
    }

    /**
     * Creates new {@link MetadataModel} for given {@link ModelUnit}.
     * @param unit {@link ModelUnit} of involved project
     * @return newly created {@link MetadataModel<SrpingModel>}
     */
    public static MetadataModel<SpringModel> createMetaModel(ModelUnit unit) {
        LOGGER.log(Level.FINE, "Creating metadata model for model unit: {0}", unit);
        return MetadataModelFactory.createMetadataModel(SpringModelImplementation.createMetaModel(unit));
    }
}
