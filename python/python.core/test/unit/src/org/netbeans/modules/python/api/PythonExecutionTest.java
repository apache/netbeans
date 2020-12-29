/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.Future;
import org.netbeans.junit.NbTestCase;
import org.openide.util.io.ReaderInputStream;

public class PythonExecutionTest extends NbTestCase{

    public PythonExecutionTest(String name) {
        super(name);
    }
//    public void testJythonExecution() throws Exception{
//
//    }
    public void testPythonExecution() throws Exception{
        System.out.println("Run python test");
        PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName("Python Console Test");
        pyexec.setWorkingDirectory(getDataSourceDir().getAbsolutePath());
        pyexec.setCommand("/usr/bin/python2");
        pyexec.setScript(getTestFile("HelloWorld.py").getAbsolutePath());            
        pyexec.setCommandArgs("-u");
        Future<Integer> result  =  pyexec.run();
        
        assertEquals(0, result.get().intValue());
    }
    
    public void testPythonWriter() throws Exception{
        String command = "/usr/bin/python2";
        PythonPlatform platform = new PythonPlatform("testid");
        PythonExecution pye = new PythonExecution();
        pye.setCommand(command);
        pye.setDisplayName("Python Properties");
        File info = getTestFile("platform_info.py");
             
        pye.setScript(info.getAbsolutePath());
        pye.setShowControls(false);
        pye.setShowInput(false);
        pye.setShowWindow(false);
        pye.setShowProgress(false);
        pye.setShowSuspended(false);
        pye.setWorkingDirectory(info.getAbsolutePath().substring(0, info.getAbsolutePath().lastIndexOf(File.separator)));
        pye.attachOutputProcessor();
        Future<Integer> result  =  pye.run();
        
        assertEquals(0, result.get().intValue());
        //pye.waitFor();
        Properties prop = new Properties();
        prop.load(new ReaderInputStream(pye.getOutput()));
        platform.setInterpreterCommand(prop.getProperty("python.command"));
        platform.setName(prop.getProperty("platform.name"));

        assertEquals(command, platform.getInterpreterCommand());

    }
    public void testPythonPath() throws Exception{
        String command = "/usr/bin/python2";
        PythonPlatform platform = new PythonPlatform("testid");
        PythonExecution pye = new PythonExecution();
        pye.setCommand(command);
        pye.setDisplayName("Python Properties");
        File info = getTestFile("platform_info.py");

        pye.setScript(info.getAbsolutePath());
        pye.setShowControls(false);
        pye.setShowInput(false);
        pye.setShowWindow(false);
        pye.setShowProgress(false);
        pye.setShowSuspended(false);
        pye.setWorkingDirectory(info.getAbsolutePath().substring(0, info.getAbsolutePath().lastIndexOf(File.separator)));
        pye.attachOutputProcessor();
        Future<Integer> result  =  pye.run();

        assertEquals(0, result.get().intValue());
        //pye.waitFor();
        Properties prop = new Properties();
        prop.load(new ReaderInputStream(pye.getOutput()));
        platform.setInterpreterCommand(prop.getProperty("python.command"));
        platform.setName(prop.getProperty("platform.name"));
        platform.addPythonPath(prop.getProperty("python.path").split(File.pathSeparator));

        assertEquals(command, platform.getInterpreterCommand());

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
