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
package org.netbeans.modules.web.beans.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.support.JavaSourceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.netbeans.modules.web.beans.testutilities.CdiTestUtilities;
import org.openide.util.Exceptions;


/**
 * @author ads
 *
 */
public class CommonTestCase extends JavaSourceTestCase {

    private final boolean jakartaVariant;

    public CommonTestCase(String testName, boolean jakartaVariant) {
        super(testName);
        this.jakartaVariant = jakartaVariant;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myUtilities = new CdiTestUtilities(srcFO, jakartaVariant);
        myUtilities.initAnnotations();
        tempDir = myUtilities.setupJakartaMarker();
        addCompileRoots(Arrays.asList(tempDir.toUri().toURL()));
    }

    @Override
    protected void tearDown() {
        try {
            myUtilities.deleteTree(tempDir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public MetadataModel<WebBeansModel> createBeansModel() throws IOException, InterruptedException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        return myUtilities.createBeansModel();
    }
    
    public TestWebBeansModelImpl createModelImpl() throws IOException {
        return createModelImpl(false);
    }
    
    public TestWebBeansModelImpl createModelImpl(boolean fullModel) throws IOException {
        IndexingManager.getDefault().refreshIndexAndWait(srcFO.getURL(), null);
        ModelUnit modelUnit = ModelUnit.create(
                ClassPath.getClassPath(srcFO, ClassPath.BOOT),
                ClassPath.getClassPath(srcFO, ClassPath.COMPILE),
                ClassPath.getClassPath(srcFO, ClassPath.SOURCE), null);
        return new TestWebBeansModelImpl(modelUnit, fullModel);
    }
    
    protected void inform( String message ){
        System.out.println(message);
    }
    
    protected void createQualifier(String name ) throws IOException{
        myUtilities.createQualifier(name);
    }
    
    protected void createInterceptorBinding(String name ) throws IOException{
        myUtilities.createInterceptorBinding(name);
    }
    
    public final void assertFindParameterResultInjectables(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findParameterInjectable(element, null, new AtomicBoolean(false));
        assertResultInjectables(result, injectables);
    }

    public final void assertFindParameterResultProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findParameterInjectable(element, null, new AtomicBoolean(false));
        assertResultProductions(result, injectables);
    }

    public final void assertFindParameterResultProductionsVar(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findParameterInjectable(element, null, new AtomicBoolean(false));
        assertResultProductions(result, true, injectables);
    }

    public final void assertFindVariableResultInjectables(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findVariableInjectable(element, null, new AtomicBoolean(false));
        assertResultInjectables(result, injectables);
    }

    public final void assertFindVariableResultProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findVariableInjectable(element, null, new AtomicBoolean(false));
        assertResultProductions(result, injectables);
    }

    public final void assertFindVariableResultProductionsVar(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String... injectables) {
        DependencyInjectionResult result = provider.findVariableInjectable(element, null, new AtomicBoolean(false));
        assertResultProductions(result, true, injectables);
    }

    public final void assertFindAllProductions(VariableElement element,
            TestWebBeansModelProviderImpl provider,
            String productionName , String enclosingClass ) {
        DependencyInjectionResult result = provider.findVariableInjectable(element, null, new AtomicBoolean(false));
        assertResultAllProductions(result, productionName , enclosingClass );
    }

    public final void assertResultInjectables(DependencyInjectionResult result, String... injectables) {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        Set<TypeElement> typeElements = ((ResultImpl) result).getTypeElements();
        if (injectables == null) {
            assertEquals("no injectables expected, but found: "+typeElements, 0, typeElements.size());
        }
        assertTrue("number of injectables does not match: returned="+typeElements+" expected="+Arrays.asList(injectables), injectables.length == typeElements.size());
        Set<String> set = new HashSet<String>();
        for (TypeElement injactable : typeElements) {
            set.add(injactable.getQualifiedName().toString());
        }
        for (String inj : injectables) {
            assertTrue("Result of typesafe resolution should contain " + inj
                    + " class definition in "+set, set.contains(inj));
        }
    }

    public final void assertResultProductions(DependencyInjectionResult result, String... producers) {
        assertResultProductions(result, false, producers);
    }

    public final void assertResultProductions(DependencyInjectionResult result, boolean variable, String... producers) {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        Set<Element> productions = ((ResultImpl) result).getProductions();
        if (producers == null) {
            assertEquals("no producers expected, but found: "+productions, 0, productions.size());
        }
        assertTrue("number of productions does not match: returned="+productions+" expected="+Arrays.asList(producers), producers.length == productions.size());
        Set<String> set = new HashSet<String>();
        for (Element injectable : productions) {
            if (variable) {
                assertTrue("injectable should be a production method," + " but found :"
                        + injectable.getKind(), injectable instanceof VariableElement);
            } else {
                assertTrue("injectable should be a production method," + " but found :"
                        + injectable.getKind(), injectable instanceof ExecutableElement);
            }
            set.add(injectable.getSimpleName().toString());
        }
        for (String prod : producers) {
            assertTrue("Result of typesafe resolution should contain " + prod
                    + " producer in "+set, set.contains(prod));
        }
    }

    public final void assertResultAllProductions(DependencyInjectionResult result, String productionName , 
            String enclosingClass) 
    {
        assertNotNull(result);
        assertTrue("not ResultImpl instance: "+result, result instanceof ResultImpl);

        Set<Element> productions = ((ResultImpl) result).getProductions();
        if (productionName == null) {
            assertEquals("no injectables expected, but found production element", 0, 
                    productions.size());
        }
        assertEquals( "Expected just one production element" , 1, productions.size() );
        Element production = productions.iterator().next();
        String name = production.getSimpleName().toString();
        
        assertEquals("Production element name should be "+productionName,productionName, name);
        Element parent = production.getEnclosingElement();
        assertTrue( parent instanceof TypeElement );
        String parentName = ((TypeElement)parent).getQualifiedName().toString();
        assertEquals( "Production enclosing class name should be "+enclosingClass,
                enclosingClass , parentName);
    }

    private CdiTestUtilities myUtilities;
    private Path tempDir;
}
