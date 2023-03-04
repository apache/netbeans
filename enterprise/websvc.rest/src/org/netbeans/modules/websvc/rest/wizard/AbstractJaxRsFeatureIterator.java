/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.util.NbBundle;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;


/**
 * @author ads
 *
 */
abstract class AbstractJaxRsFeatureIterator implements 
    ProgressInstantiatingIterator<WizardDescriptor>
{

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.AsynchronousInstantiatingIterator#instantiate()
     */
    @Override
    public Set<?> instantiate() throws IOException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#initialize(org.openide.WizardDescriptor)
     */
    @Override
    public void initialize( WizardDescriptor wizard ) {
        myWizard = wizard;
        Project project = Templates.getProject(wizard);
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);    
        
        Panel<?> panel ;
        myPanel = createPanel(wizard);
        if (sourceGroups.length == 0) {
            SourceGroup[] genericSourceGroups = ProjectUtils.
                    getSources(project).getSourceGroups(Sources.TYPE_GENERIC);
            panel = Templates.buildSimpleTargetChooser(project,  genericSourceGroups).
                        bottomPanel( myPanel).create();
        } else {
            panel = JavaTemplates.createPackageChooser(project, sourceGroups, 
                            myPanel, true);
        }
        myPanels = new Panel[]{ panel };
        setSteps();        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.InstantiatingIterator#uninitialize(org.openide.WizardDescriptor)
     */
    @Override
    public void uninitialize( WizardDescriptor descriptor ) {
        myPanels = null;        
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener listener ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#current()
     */
    @Override
    public Panel<WizardDescriptor> current() {
        return myPanels[myIndex];
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return myIndex<myPanels.length-1;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#hasPrevious()
     */
    @Override
    public boolean hasPrevious() {
        return myIndex >0 ;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#name()
     */
    @Override
    public String name() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#nextPanel()
     */
    @Override
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;            
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#previousPanel()
     */
    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;         
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#removeChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void removeChangeListener( ChangeListener listener ) {
    }
    
    protected WizardDescriptor getWizard(){
        return myWizard;
    }
    
    protected abstract Panel<?> createPanel(WizardDescriptor wizard);
    
    protected abstract String getTitleKey();
    
    static MethodTree createMethod(GenerationUtils genUtils,TreeMaker maker, 
            String name ,LinkedHashMap<String,String> methodParams)
    {
        return createMethod(genUtils, maker, name, null, methodParams , null);
    }
    
    static MethodTree createMethod(GenerationUtils genUtils,TreeMaker maker, 
            String name ,String returnType,LinkedHashMap<String,String> methodParams)
    {
        return createMethod(genUtils, maker, name, returnType, methodParams, null );
    }
    
    static MethodTree createMethod(GenerationUtils genUtils,TreeMaker maker, 
            String name ,LinkedHashMap<String,String> methodParams, String body)
    {
        return createMethod(genUtils, maker, name, null, methodParams, body );
    }
    
    static MethodTree createMethod(GenerationUtils genUtils,TreeMaker maker, 
            String name , String returnType, LinkedHashMap<String,String> methodParams,
            String body)
    {
        ModifiersTree modifiers = maker.Modifiers(EnumSet.of(Modifier.PUBLIC));
        List<VariableTree> params=new ArrayList<VariableTree>();
        ModifiersTree noModifier = maker.Modifiers(Collections.<Modifier>emptySet());
        for(Entry<String,String> entry: methodParams.entrySet()){
            String paramName = entry.getKey();
            String paramType = entry.getValue();
            params.add(maker.Variable(noModifier, paramName, 
                    maker.Type(paramType), null));
        }
        Tree returnTree ;
        String resultBody ;
        if (returnType == null) {
            returnTree = maker.PrimitiveType(TypeKind.VOID);
            resultBody = "{}"; // NOI18N
        }
        else {
            returnTree = maker.Type(returnType);
            resultBody = "{ return null; }"; // NOI18N
        }
        if ( body!= null ){
            resultBody = body;
        }
        return maker.Method(
                maker.addModifiersAnnotation(modifiers, genUtils.createAnnotation(
                        Override.class.getCanonicalName())),
                name,                           
                returnTree,
                Collections.<TypeParameterTree>emptyList(),
                params,
                Collections.<ExpressionTree>emptyList(),
                resultBody,                               
                null);
    }

    private void setSteps() {
        Object contentData = myWizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);  
        if ( contentData instanceof String[] ){
            String steps[] = (String[])contentData;
            steps[steps.length-1]=NbBundle.getMessage(InterceptorIterator.class, 
                    getTitleKey());        // NOI18N
            for( int i=0; i<myPanels.length; i++ ){
                Panel<?> panel = myPanels[i];
                JComponent component = (JComponent)panel.getComponent();
                component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, 
                        steps);
                component.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            }
        }
    }
    
    private WizardDescriptor myWizard;
    private WizardDescriptor.Panel[] myPanels;
    private Panel myPanel;
    private int myIndex;
}
