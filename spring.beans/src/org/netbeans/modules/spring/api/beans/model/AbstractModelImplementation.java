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

import org.netbeans.modules.spring.spi.beans.SpringModelProviderFactory;
import org.netbeans.modules.spring.spi.beans.SpringModelProvider;
import java.util.Collection;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

import org.openide.util.Lookup;

/**
 * Abstract model for holding fields with Spring annotation support meta data.
 * 
 * @author Martin Fousek <marfous@netbeans.org>
 */
public abstract class AbstractModelImplementation {

    private ModelUnit modelUnit;
    private SpringModel model;
    private AnnotationModelHelper helper;
    private SpringModelProvider provider;

    protected AbstractModelImplementation(ModelUnit unit) {
        modelUnit = unit;
        model = new SpringModel(this);
        helper = AnnotationModelHelper.create(unit.getClassPathInfo());
        Collection<? extends SpringModelProviderFactory> factories =
                Lookup.getDefault().lookupAll(SpringModelProviderFactory.class);
        for (SpringModelProviderFactory factory : factories) {
            provider = factory.createSpringModelProvider(this);
            if (provider != null) {
                break;
            }
        }

    }

    /**
     * Returns {@code ModelUnit} of the Model.
     * @return {@code ModelUnit} with classpath informations
     */
    public ModelUnit getModelUnit() {
        return modelUnit;
    }

    /**
     * Returns specific implementation of {@code SpringModel} for this Model.
     * @return specific {@code SpringModel} class
     */    
    protected SpringModel getModel() {
        return model;
    }

    /**
     * Returns {@code AnnotationModelHelper} of the Model.
     * @return {@code AnnotationModelHelper} of the Model
     */    
    public AnnotationModelHelper getHelper() {
        return helper;
    }

    /**
     * Returns {@code SpringModelProvider} for current Model.
     * @return {@code SpringModelProvider}
     */        
    protected SpringModelProvider getProvider() {
        return provider;
    }
}
