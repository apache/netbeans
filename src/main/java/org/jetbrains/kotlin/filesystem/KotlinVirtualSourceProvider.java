/** *****************************************************************************
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
 ****************************************************************************** */
package org.jetbrains.kotlin.filesystem;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.List;
import java.util.Set;
import kotlin.Pair;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = VirtualSourceProvider.class)
public class KotlinVirtualSourceProvider implements VirtualSourceProvider {

    @Override
    public Set<String> getSupportedExtensions() {
        return Sets.newHashSet("kt");
    }

    @Override
    public boolean index() {
        return false;
    }

    @Override
    public void translate(Iterable<File> files, File sourceRoot, Result result) {
        for (File file : files) {
            List<byte[]> codeList = getByteCode(file);
            if (codeList.isEmpty()) continue;
            
            File normalizedFile = FileUtil.normalizeFile(file);
            FileObject fo = FileUtil.toFileObject(normalizedFile);
            if (fo == null) continue;
            
            List<Pair<ClassNode, String>> list = JavaStubGenerator.INSTANCE.gen(codeList);
            for (Pair<ClassNode, String> nameAndStub : list) {
                String code = nameAndStub.getSecond();
                int lastIndexOfSlash = nameAndStub.getFirst().name.lastIndexOf("/");
                String packageName;
                if (lastIndexOfSlash != -1) {
                    packageName = nameAndStub.getFirst().name.substring(0, lastIndexOfSlash);
                } else packageName = nameAndStub.getFirst().name;
                result.add(normalizedFile, packageName, fo.getName(), code);
            }
        }
    }
    
    private List<byte[]> getByteCode(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        Project project = ProjectUtils.getKotlinProjectForFileObject(fo);
        KtFile ktFile = ProjectUtils.getKtFile(fo);
        AnalysisResultWithProvider result = KotlinParser.getAnalysisResult(ktFile, project);
        
        return KotlinLightClassGeneration.INSTANCE.getByteCode(fo, project, result.getAnalysisResult());
    }
    
}
