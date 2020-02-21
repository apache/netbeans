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
package org.netbeans.modules.cnd.dwarfdump.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.dwarfdump.CompilationUnitInterface;
import org.netbeans.modules.cnd.dwarfdump.Dwarf;
import org.netbeans.modules.cnd.dwarfdump.Dwarf.CompilationUnitIterator;
import org.netbeans.modules.cnd.dwarfdump.exception.WrongFileFormatException;


/**
 *
 */
public class TraceDwarf {

    private TraceDwarf() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String objFileName = args[0];
        Dwarf dump = null;
        try {
            Dwarf.LOG.log(Level.FINE, "TraceDwarf.");  // NOI18N
            dump = new Dwarf(objFileName);
            CompilationUnitIterator units = dump.iteratorCompilationUnits();
            int idx = 0;
            if (units.hasNext()) {
                Dwarf.LOG.log(Level.FINE, "\n**** Done. Compilation units were found"); // NOI18N
                while(units.hasNext()) {
                    CompilationUnitInterface compilationUnit = units.next();
                    Dwarf.LOG.log(Level.FINE, "{0}: {1}", new Object[]{++idx, compilationUnit.getSourceFileName()});// NOI18N
                }

            }
        } catch (FileNotFoundException ex) {
            // Skip Exception
            Dwarf.LOG.log(Level.FINE, "File not found {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
        } catch (WrongFileFormatException ex) {
            Dwarf.LOG.log(Level.FINE, "Unsuported format of file {0}: {1}", new Object[]{objFileName, ex.getMessage()});  // NOI18N
        } catch (IOException ex) {
            Dwarf.LOG.log(Level.FINE, "Exception in file " + objFileName, ex);  // NOI18N
        } catch (Throwable ex) {
            Dwarf.LOG.log(Level.FINE, "Exception in file " + objFileName, ex);  // NOI18N
        } finally {
            if (dump != null) {
                dump.dispose();
            }
        }
    }
}
