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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.ModelUnit;
import org.netbeans.api.java.source.ClasspathInfo;


/**
 * @author ads
 *
 */
public class JsfModelImplementation implements MetadataModelImplementation<JsfModel> {
    
    private JsfModelImplementation ( ModelUnit unit ){
        ClasspathInfo classpathInfo = ClasspathInfo.create(unit.getBootPath(), 
                unit.getCompilePath(), unit.getSourcePath());
        myHelper = AnnotationModelHelper.create(classpathInfo);
        myModel = new JsfModelImpl( unit , getHelper() );
    }

    
    public static MetadataModelImplementation<JsfModel> create( ModelUnit unit )
    {
        return new JsfModelImplementation( unit );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#isReady()
     */
    public boolean isReady() {
        return !getHelper().isJavaScanInProgress();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    public <R> R runReadAction(final  MetadataModelAction<JsfModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    public <R> Future<R> runReadActionWhenReady(
            final MetadataModelAction<JsfModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }
    
    AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private JsfModelImpl getModel(){
        return myModel;
    }
    
    private JsfModelImpl myModel;
    private final AnnotationModelHelper myHelper;

}
