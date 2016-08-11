/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package indentation;

import javaproject.JavaProject;
import static junit.framework.TestCase.assertNotNull;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class IndentationTest extends NbTestCase {
    
    private final Project project;
    private final FileObject indentationDir;
    
    public IndentationTest() {
        super("Indentation test");
        project = JavaProject.INSTANCE.getJavaProject();
        indentationDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("indentation");
    }
    
    private void doTest(String fileName) {
        
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(indentationDir);
    }
    
}
