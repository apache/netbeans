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
package org.netbeans.modules.spring.api.beans.model;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SpringModelAnnotationsTest extends CommonAnnotationTestCase {//  AnnotationSupportTestCase {

    public SpringModelAnnotationsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URL location = org.springframework.stereotype.Component.class.getProtectionDomain().getCodeSource().getLocation();
        addCompileRoots(Collections.singletonList(new URL( "jar:"+location.toString()+"!/")));
    }

    public void testModelInitialization() throws IOException, InterruptedException {
        addSpringBeanImplicitName();
        addSpringBeanDefinedName();
        addSpringBeanDefinedNameByValue();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(3, beans.size());
    }
    
    public void testModelRefresh() throws IOException, InterruptedException {
        addSpringBeanImplicitName();
        
        //model should contain one added Spring bean
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        
        addSpringBeanDefinedName();
        addSpringBeanDefinedNameByValue();

        //model should be refreshed for new created beans
        beans.clear();
        beans = getAnnotatedBeans(createSpringModel());
        assertEquals(3, beans.size());
    }
    
    public void testSpringBeanImplicitName() throws IOException, InterruptedException {
        addSpringBeanImplicitName();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanImplicitName");
        assertEquals(springBean.getNames().get(0), "beanImplicitName");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanImplicitName.java"));
    }
    
    public void testSpringBeanDefinedName() throws IOException, InterruptedException {
        addSpringBeanDefinedName();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanDefinedName");
        assertEquals(springBean.getNames().get(0), "definedName");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanDefinedName.java"));
    }
    
    public void testSpringBeanDefinedNameByValue() throws IOException, InterruptedException {
        addSpringBeanDefinedNameByValue();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanDefinedNameByValue");
        assertEquals(springBean.getNames().get(0), "byValue");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanDefinedNameByValue.java"));
    }
    
    public void testSpringBeanComponent() throws IOException, InterruptedException {
        addSpringBeanComponent();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanComponent");
        assertEquals(springBean.getNames().get(0), "beanComponent");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanComponent.java"));
    }
    
    public void testSpringBeanService() throws IOException, InterruptedException {
        addSpringBeanService();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanService");
        assertEquals(springBean.getNames().get(0), "beanService");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanService.java"));
    }
    
    public void testSpringBeanRepository() throws IOException, InterruptedException {
        addSpringBeanRepository();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanRepository");
        assertEquals(springBean.getNames().get(0), "beanRepository");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanRepository.java"));
    }
    
    public void testSpringBeanController() throws IOException, InterruptedException {
        addSpringBeanController();
        
        List<SpringBean> beans = getAnnotatedBeans(createSpringModel());
        assertEquals(1, beans.size());
        SpringBean springBean = beans.get(0);
        assertEquals(springBean.getClassName(), "foo.BeanController");
        assertEquals(springBean.getNames().get(0), "beanController");
        assertEquals(springBean.getLocation().getFile().getPath(), getUniversalPath(getWorkDir(), "src/foo/BeanController.java"));
    }

    private String getUniversalPath(File file, String relativePath) {
        return FileUtil.toFileObject(new File(file, relativePath)).getPath();
    }

    private void addSpringBeanImplicitName() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanImplicitName.java",
                "package foo; "
                + "import org.springframework.stereotype.Component; "
                + "@Component "
                + "public class BeanImplicitName  {"
                + "}");
    }

    private void addSpringBeanDefinedName() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanDefinedName.java",
                "package foo; "
                + "import org.springframework.stereotype.Component; "
                + "@Component(\"definedName\") "
                + "public class BeanDefinedName  {"
                + "}");
    }

    private void addSpringBeanDefinedNameByValue() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanDefinedNameByValue.java",
                "package foo; "
                + "import org.springframework.stereotype.Component; "
                + "@Component(value=\"byValue\") "
                + "public class BeanDefinedNameByValue  {"
                + "}");
    }
    
    private void addSpringBeanComponent() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanComponent.java",
                "package foo; "
                + "import org.springframework.stereotype.Component; "
                + "@Component "
                + "public class BeanComponent  {"
                + "}");
    }
    
    private void addSpringBeanService() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanService.java",
                "package foo; "
                + "import org.springframework.stereotype.Service; "
                + "@Service "
                + "public class BeanService  {"
                + "}");
    }
    
    private void addSpringBeanRepository() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanRepository.java",
                "package foo; "
                + "import org.springframework.stereotype.Repository; "
                + "@Repository "
                + "public class BeanRepository  {"
                + "}");
    }
    
    private void addSpringBeanController() throws IOException {
        TestUtilities.copyStringToFileObject(srcFO, "foo/BeanController.java",
                "package foo; "
                + "import org.springframework.stereotype.Controller; "
                + "@Controller "
                + "public class BeanController  {"
                + "}");
    }
}
