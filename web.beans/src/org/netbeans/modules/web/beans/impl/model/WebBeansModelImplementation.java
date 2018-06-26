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
package org.netbeans.modules.web.beans.impl.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation;
import org.netbeans.modules.web.beans.api.model.AbstractModelImplementation;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public class WebBeansModelImplementation extends AbstractModelImplementation 
    implements MetadataModelImplementation<WebBeansModel>
{

    protected WebBeansModelImplementation( ModelUnit unit ){
        super( unit );
        myManagers = new HashMap<String, PersistentObjectManager<BindingQualifier>>();
        myStereotypedManagers = new HashMap<String, PersistentObjectManager<StereotypedObject>>();
        myHelper = AnnotationModelHelper.create( getModelUnit().getClassPathInfo() );
    }
    
    public static MetadataModelImplementation<WebBeansModel> createMetaModel( 
            ModelUnit unit )
    {
        return new WebBeansModelImplementation( unit );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.AbstractModelImplementation#getBeansModel()
     */
    @Override
    public BeansModel getBeansModel() {
        return super.getBeansModel();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#isReady()
     */
    @Override
    public boolean isReady() {
        return !getHelper().isJavaScanInProgress();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    @Override
    public <R> R runReadAction( final MetadataModelAction<WebBeansModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTask(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation#runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction)
     */
    @Override
    public <R> Future<R> runReadActionWhenReady(
            final MetadataModelAction<WebBeansModel, R> action )
            throws MetadataModelException, IOException
    {
        return getHelper().runJavaSourceTaskWhenScanFinished(new Callable<R>() {
            @Override
            public R call() throws Exception {
                return action.run(getModel());
            }
        });
    }
    
    protected AnnotationModelHelper getHelper() {
        return myHelper;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.AbstractModelImplementation#getModel()
     */
    @Override
    protected WebBeansModel getModel() {
        return super.getModel();
    }
    
    Map<String,PersistentObjectManager<BindingQualifier>> getManagers(){
        return myManagers;
    }
    
    PersistentObjectManager<BindingQualifier> getManager( String annotationFQN ){
        PersistentObjectManager<BindingQualifier> result = getManagers().get(
                annotationFQN);
        if ( result == null ) {
            result  = getHelper().createPersistentObjectManager( 
                    new AnnotationObjectProvider( getHelper(), annotationFQN));
            getManagers().put(  annotationFQN , result);
        }
        return result;
    }
    
    PersistentObjectManager<BindingQualifier> getNamedManager(){
        return getManager( FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION );
    }
    
    PersistentObjectManager<NamedStereotype> getNamedStereotypesManager(){
        if ( myStereotypesManager == null ){
            myStereotypesManager = getHelper().createPersistentObjectManager(
                    new NamedStereotypeObjectProvider( getHelper()));
        }
        return myStereotypesManager;
    }
    
    PersistentObjectManager<StereotypedObject> getStereotypedManager( 
            String stereotype )
    {
        PersistentObjectManager<StereotypedObject> result = 
            getStereotypedManagers().get(stereotype);
        if ( result == null ) {
            result  = getHelper().createPersistentObjectManager( 
                    new StereotypedObjectProvider( stereotype, getHelper()));
            getStereotypedManagers().put(  stereotype , result);
        }
        return result;
    }
    
    Map<String,PersistentObjectManager<StereotypedObject>> getStereotypedManagers(){
        return myStereotypedManagers;
    }
    
    PersistentObjectManager<DecoratorObject> getDecoratorsManager(){
        if ( myDecoratorsManager == null ){
            myDecoratorsManager = getHelper().createPersistentObjectManager( 
                    new DecoratorObjectProvider( getHelper()));
        }
        return myDecoratorsManager;
    }
    
    PersistentObjectManager<InterceptorObject> getInterceptorsManager(){
        if ( myInterceptorsManager == null ){
            myInterceptorsManager = getHelper().createPersistentObjectManager( 
                    new InterceptorObjectProvider( getHelper()));
        }
        return myInterceptorsManager;
    }
    
    Set<String> adjustStereotypesManagers(){
        Set<String> stereotypes = getStereotypedManagers().keySet();
        Collection<NamedStereotype> namedStereotypes = getNamedStereotypesManager().
            getObjects();
        Set<String> existingStereotypes = new HashSet<String>(namedStereotypes.size());
        for (NamedStereotype namedStereotype : namedStereotypes) {
            if( namedStereotype!=null && namedStereotype.getTypeElement()!=null) {
                String name = namedStereotype.getTypeElement().getQualifiedName().
                    toString();
                if ( !stereotypes.contains( name)){
                    getStereotypedManager(name);
                }
                existingStereotypes.add( name );
            }
        }
        if ( existingStereotypes.size() == getStereotypedManagers().keySet().size()){
            return existingStereotypes;
        }
        for (Iterator<String> iterator = getStereotypedManagers().keySet().iterator();
            iterator.hasNext(); ) 
        {
            String stereotype = iterator.next();
            if ( !existingStereotypes.contains( stereotype)){
                iterator.remove();
            }
        }
        return existingStereotypes;
    }
    
    private Map<String,PersistentObjectManager<BindingQualifier>> myManagers;
    private PersistentObjectManager<NamedStereotype> myStereotypesManager;
    private PersistentObjectManager<DecoratorObject> myDecoratorsManager;
    private PersistentObjectManager<InterceptorObject> myInterceptorsManager;
    private Map<String,PersistentObjectManager<StereotypedObject>> myStereotypedManagers; 
    private AnnotationModelHelper myHelper;
}
