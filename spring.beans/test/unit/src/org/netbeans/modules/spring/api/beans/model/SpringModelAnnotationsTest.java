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
