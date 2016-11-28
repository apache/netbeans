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
package org.jetbrains.kotlin.indexer;

import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinIndexer extends EmbeddingIndexer {

    private static final Object LOCK = new Object(){};
    
    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
//        final KotlinParserResult result = (KotlinParserResult) parserResult;
//        AnalysisResultWithProvider analysisResult = result.getAnalysisResult();
//        
//        if (analysisResult == null) {
//            return;
//        }
//        
//        final FileObject fo = result.getSnapshot().getSource().getFileObject();
//        final AnalysisResult res = analysisResult.getAnalysisResult();
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                generateLightClass(fo, result.getProject(), res);
//            }
//        });
//        thread.start();
    }
    
//    private void generateLightClass(FileObject fo, Project project, AnalysisResult analysisResult) {
//        synchronized (LOCK) {
//            KotlinLightClassGeneration.INSTANCE.generate(fo, project, analysisResult);
//        }
//    }
    
}
