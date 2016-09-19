/*******************************************************************************
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
package org.jetbrains.kotlin.refactorings.rename

import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.spi.support.ModificationResult
import org.netbeans.modules.csl.spi.support.ModificationResult.Difference
import org.netbeans.modules.refactoring.spi.BackupFacility
import org.netbeans.modules.refactoring.spi.Transaction
import org.openide.filesystems.FileObject
import org.openide.text.PositionBounds
import org.openide.util.Exceptions

/*
  @author Alexander.Baratynski
  Created on Sep 13, 2016
*/

class KotlinTransaction(val renameMap: Map<FileObject, List<OffsetRange>>, 
                        val newName: String, val oldName: String) : Transaction {
    
    private var commited = false
    private val ids = arrayListOf<BackupFacility.Handle>()
    
    override fun commit() {
        val result = ModificationResult()
        for ((file, range) in renameMap) {
            val posBounds = createPositionBoundsForFO(file, range)
            result.addDifferences(file, posBounds.map{ Difference(Difference.Kind.CHANGE, it.begin, it.end, oldName, newName) })
        }
        if (commited) {
            ids.map{ it.restore() }
        } else {
            commited = true
            ids.add(BackupFacility.getDefault().backup(result.modifiedFileObjects))
            result.commit()
        }
    }
    
    override fun rollback() { 
        ids.map{ it.restore() } 
    }
    
}