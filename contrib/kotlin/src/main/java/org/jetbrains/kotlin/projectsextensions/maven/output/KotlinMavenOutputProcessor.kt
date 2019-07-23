/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.kotlin.projectsextensions.maven.output

import java.io.File
import org.jetbrains.kotlin.navigation.netbeans.openFileAtPosition
import org.netbeans.modules.maven.api.output.OutputProcessor
import org.netbeans.modules.maven.api.output.OutputVisitor
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil
import org.openide.windows.OutputEvent
import org.openide.windows.OutputListener

class KotlinMavenOutputProcessor : OutputProcessor {

    override fun processLine(line: String, visitor: OutputVisitor) {
        val fileName = line.substringBefore(":")
        if (fileName == line) return

        val file = File(fileName)
        if (!file.exists()) return
        
        val fo = FileUtil.toFileObject(FileUtil.normalizeFile(File(fileName))) ?: return
        val colon = line.indexOf(":")

        fun position(first: Char, second: Char): Pair<Int, Int>? {
            val start = line.indexOf(first) + 1
            val end = line.indexOf(second)
            val split = if (first == '[') "," else ", "
            val positions = line.substring(start, end).split(split)
            
            if (positions.size != 2) return null

            return Pair(positions[0].toInt(), positions[1].toInt())
        }

        val position = (if (line[colon + 1] == '[') position('[', ']') else position('(', ')')) ?: return

        visitor.outputListener = CompilationErrorAnnotation(fo, position.first, position.second)
    }

    override fun getRegisteredOutputSequences() = arrayOf("mojo-execute#kotlin:compile",
            "mojo-execute#kotlin:test-compile")

    override fun sequenceFail(sequenceId: String?, visitor: OutputVisitor?) {}

    override fun sequenceEnd(sequenceId: String?, visitor: OutputVisitor?) {}

    override fun sequenceStart(sequenceId: String?, visitor: OutputVisitor?) {}
}

class CompilationErrorAnnotation(private val file: FileObject,
                                 private val lineNumber: Int,
                                 private val columnNumber: Int) : OutputListener {

    override fun outputLineAction(event: OutputEvent) {
        file.openFileAtPosition(lineNumber, columnNumber)
    }

    override fun outputLineSelected(event: OutputEvent?) {}

    override fun outputLineCleared(event: OutputEvent?) {}
}