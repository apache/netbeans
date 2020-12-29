/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.debugger;

import java.io.File;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.junit.NbTestCase;

public class DebuggerConceptTest extends NbTestCase {

    public DebuggerConceptTest(String name) {
        super(name);
    }

    public void testStartDebugger() throws Exception{
        PythonExecution pye = new PythonExecution();

    }
    protected File getTestFile(String relFilePath) {
        File wholeInputFile = new File(getDataSourceDir(), relFilePath);
        if (!wholeInputFile.exists()) {
            NbTestCase.fail("File " + wholeInputFile + " not found.");
        }
        return wholeInputFile;
    }
      protected File getDataSourceDir() {
        // Check whether token dump file exists
        // Try to remove "/build/" from the dump file name if it exists.
        // Otherwise give a warning.
        File inputFile = getDataDir();
        String inputFilePath = inputFile.getAbsolutePath();
        boolean replaced = false;
        if (inputFilePath.contains(pathJoin("build", "test"))) {
            inputFilePath = inputFilePath.replace(pathJoin("build", "test"), pathJoin("test"));
            replaced = true;
        }
        if (!replaced && inputFilePath.contains(pathJoin("test", "work", "sys"))) {
            inputFilePath = inputFilePath.replace(pathJoin("test", "work", "sys"), pathJoin("test", "unit"));
            replaced = true;
        }
        if (!replaced) {
            System.err.println("Warning: Attempt to use dump file " +
                    "from sources instead of the generated test files failed.\n" +
                    "Patterns '/build/test/' or '/test/work/sys/' not found in " + inputFilePath
            );
        }
        inputFile = new File(inputFilePath);
        assertTrue(inputFile.exists());

        return inputFile;
    }
      private static String pathJoin(String... chunks) {
        StringBuilder result = new StringBuilder(File.separator);
        for (String chunk : chunks) {
            result.append(chunk).append(File.separatorChar);
        }
        return result.toString();
    }

}
