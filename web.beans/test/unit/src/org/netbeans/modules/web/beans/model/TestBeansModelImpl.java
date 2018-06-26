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
package org.netbeans.modules.web.beans.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.web.beans.api.model.BeanArchiveType;

import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.xml.AlternativeElement;
import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.BeanClassContainer;
import org.netbeans.modules.web.beans.xml.Decorators;
import org.netbeans.modules.web.beans.xml.Interceptors;
import org.netbeans.modules.web.beans.xml.BeanClass;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansModel;
import org.netbeans.modules.web.beans.xml.WebBeansModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.omg.PortableInterceptor.Interceptor;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 *
 */
class TestBeansModelImpl implements BeansModel {
    
    TestBeansModelImpl(FileObject sourceRoot ) 
    {
        FileObject fileObject = sourceRoot.getFileObject("beans.xml");
        if ( fileObject != null ) {
            myModel = WebBeansModelFactory.getInstance().getModel(
                    getModelSource(fileObject));
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.BeansModel#getAlternativeClasses()
     */
    @Override
    public Set<String> getAlternativeClasses() {
        Set<String> result = new HashSet<String>();
        for( BeanClass clazz : getAlternativeElement(BeanClass.class)){
            result.add( clazz.getBeanClass() );
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.BeansModel#getAlternativeStereotypes()
     */
    @Override
    public Set<String> getAlternativeStereotypes() {
        Set<String> result = new HashSet<String>();
        for( Stereotype stereotype : getAlternativeElement(Stereotype.class)){
            result.add( stereotype.getStereotype() );
        }
        return result;
    }
    
    private <T extends AlternativeElement> List<T> getAlternativeElement( 
            Class<T> clazz)
    {
        if ( myModel == null ){
            return Collections.emptyList();
        }
        List<Alternatives> children = 
            myModel.getBeans().getChildren( Alternatives.class);
        List<T> result = new LinkedList<T>();
        for (Alternatives alternative : children) {
            List<T> elements = alternative.getChildren( clazz );
            result.addAll( elements );
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.BeansModel#getDecoratorClasses()
     */
    @Override
    public LinkedHashSet<String> getDecoratorClasses() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if ( myModel == null ){
            return result;
        }
        List<Decorators> children = 
            myModel.getBeans().getChildren( Decorators.class);
        collectBeanClasses(result, children);
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.BeansModel#getIntercetorClasses()
     */
    @Override
    public LinkedHashSet<String> getInterceptorClasses() {
        LinkedHashSet<String> result = new LinkedHashSet<String>();
        if ( myModel == null ){
            return result;
        }
        List<Interceptors> children = 
            myModel.getBeans().getChildren( Interceptors.class);
        collectBeanClasses(result, children);
        return result;
    }

    private <T extends BeanClassContainer> void collectBeanClasses( 
            LinkedHashSet<String> resultCollection, List<T> containers )
    {
        for (T container : containers) {
            List<BeanClass> beansClasses = container.getBeansClasses();
            for (BeanClass beanClass : beansClasses) {
                resultCollection.add(beanClass.getBeanClass());
            }
        }
    }
    
    private ModelSource getModelSource( FileObject fileObject )
    {
        try {
            return Utilities.createModelSource( fileObject,false);
        } catch (CatalogModelException ex) {
            Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                ex.getMessage(), ex);   // NOI18N
        }
        return null;
    }

    private WebBeansModel myModel;

    @Override
    public BeanArchiveType getBeanArchiveType() {
        return BeanArchiveType.EXPLICIT;
    }

    @Override
    public boolean isCdi11OrLater() {
        return true;
    }
}
