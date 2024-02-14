/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.websocket.wizard;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.Modifier;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.java.GenerationUtils;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.WizardDescriptor.ProgressInstantiatingIterator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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
public class WebSocketEndpointIterator 
    implements ProgressInstantiatingIterator<WizardDescriptor>
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
        SourceGroup[] sourceGroups = getJavaSourceGroups(project);    
        
        Panel<?> panel ;
        myPanel = new WebSocketPanel(wizard);
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
    public void uninitialize( WizardDescriptor wizard ) {
        myPanels = null;
        myPanel = null;
        myWizard = null;
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
    public void removeChangeListener( ChangeListener arg0 ) {
    }
    
    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.Iterator#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener( ChangeListener arg0 ) {
    }

    /* (non-Javadoc)
     * @see org.openide.WizardDescriptor.ProgressInstantiatingIterator#instantiate(org.netbeans.api.progress.ProgressHandle)
     */
    @Override
    public Set<?> instantiate( ProgressHandle handle ) throws IOException {
        handle.start();
        
        handle.progress(NbBundle.getMessage(WebSocketEndpointIterator.class, 
                "TXT_GenerateEndpoint"));                       // NOI18N
        
        FileObject targetFolder = Templates.getTargetFolder(getWizard());
        String name = Templates.getTargetName(getWizard());
        FileObject endpoint = GenerationUtils.createClass(targetFolder,
                name, null );
        
        generateEndpoint(endpoint);
        
        handle.finish();
        return Collections.singleton(endpoint);
    }
    
    private void generateEndpoint( FileObject endpoint ) throws IOException{
        JavaSource javaSource = JavaSource.forFileObject(endpoint);
        if ( javaSource == null ){
            return;
        }
        
        final String uri = getWizard().getProperty(WebSocketPanel.URI).toString();
        javaSource.runModificationTask( new Task<WorkingCopy>() {
            
            @Override
            public void run( WorkingCopy copy ) throws Exception {
                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                CompilationUnitTree compilationUnit = copy.getCompilationUnit();
                List<? extends Tree> decls = compilationUnit.getTypeDecls();
                if ( decls.isEmpty()){
                    return;
                }
                ClassTree tree = (ClassTree)decls.get(0);
                ClassTree newTree = tree; 
                TreeMaker treeMaker = copy.getTreeMaker();
                
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                AnnotationTree annotation = genUtils.createAnnotation(
                        "javax.websocket.server.ServerEndpoint",            // NOI18N
                            Collections.singletonList( treeMaker.Literal(uri))); 
                
                newTree = genUtils.addAnnotation(newTree, annotation);
                ModifiersTree modifiers = treeMaker.Modifiers(EnumSet.of(Modifier.PUBLIC));
                ModifiersTree noModifier = treeMaker.Modifiers(Collections.<Modifier>emptySet());
                List<VariableTree> params = Collections.singletonList(
                        treeMaker.Variable(noModifier, "message",        // NOI18N 
                        treeMaker.Type(String.class.getName()), null));
                Tree returnTree = treeMaker.Type(String.class.getName());
                MethodTree method = treeMaker.Method(
                        treeMaker.addModifiersAnnotation(modifiers,
                                genUtils.createAnnotation(
                                "javax.websocket.OnMessage")),   // NOI18N
                        "onMessage",                    // NOI18N                           
                        returnTree,
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{ return null; }",             // NOI18N                               
                        null);
                newTree = treeMaker.addClassMember(newTree, method);
                copy.rewrite(tree, newTree);
            }
        }).commit();
    }

    private void setSteps() {
        Object contentData = myWizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);  
        if ( contentData instanceof String[] ){
            String steps[] = (String[])contentData;
            steps[steps.length-1]=NbBundle.getMessage(WebSocketEndpointIterator.class, 
                    "TXT_CreateEndpoint");                      // NOI18N
            for( int i=0; i<myPanels.length; i++ ){
                Panel<?> panel = myPanels[i];
                JComponent component = (JComponent)panel.getComponent();
                component.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_DATA, steps);
                component.putClientProperty(
                        WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
            }
        }
    }
    
    private SourceGroup[] getJavaSourceGroups( Project project ) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<FileObject> testRoots = getTestRoots(project);
        List<SourceGroup> list= new ArrayList<SourceGroup>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            if ( !testRoots.contains( sourceGroup.getRootFolder())){
                list.add( sourceGroup );
            }
        }
        return list.toArray(new SourceGroup[0]);
    }
    
    private Set<FileObject> getTestRoots( Project project ){
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<FileObject> result = new HashSet<FileObject>();
        for (SourceGroup sourceGroup : sourceGroups) {
            result.addAll(getTestRoots(sourceGroup));
        }
        return result;
    }
    
    private Set<FileObject> getTestRoots(SourceGroup group ){
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(
                group.getRootFolder());
        if (rootURLs.length == 0) {
            return Collections.emptySet();
        }
        List<FileObject> sourceRoots = getFileObjects(rootURLs);
        if (sourceRoots.isEmpty()){
            return Collections.emptySet();
        }
        return new HashSet<FileObject>( sourceRoots);
    }
    
    private static List<FileObject> getFileObjects(URL[] urls) {
        List<FileObject> result = new ArrayList<FileObject>(urls.length);
        for (URL url : urls) {
            FileObject sourceRoot = URLMapper.findFileObject(url);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else {
                Logger.getLogger(WebSocketEndpointIterator.class.getName()).log(Level.INFO, 
                        "No FileObject found for the following URL: " + url);
            }
        }
        return result;
    }
    
    private WizardDescriptor getWizard(){
        return myWizard;
    }
    
    private WizardDescriptor myWizard;
    private WizardDescriptor.Panel[] myPanels;
    private Panel myPanel;
    private int myIndex;


}
