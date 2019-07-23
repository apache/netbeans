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
 *******************************************************************************/
package org.jetbrains.kotlin.projectsextensions.gradle

import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper.doInitialScan
import org.netbeans.api.progress.ProgressHandleFactory
import org.netbeans.api.project.Project
import org.netbeans.spi.project.ui.ProjectOpenedHook
import kotlin.concurrent.thread

/**
 *
 * @author baratynskiy
 */
class GradleProjectOpenedHook(private val project: Project) : ProjectOpenedHook() {

    override fun projectOpened() {
        thread {
            KotlinProjectHelper.postTask(Runnable {
                val progressBar = ProgressHandleFactory.createHandle("Loading Kotlin environment")
                progressBar.start()
                KotlinEnvironment.getEnvironment(project)
                progressBar.finish()
            })
            
            project.doInitialScan()
        }
    }

    override fun projectClosed() {}
    
}