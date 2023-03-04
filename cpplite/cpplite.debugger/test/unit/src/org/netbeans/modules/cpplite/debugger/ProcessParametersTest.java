/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cpplite.debugger;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Test;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;
import org.netbeans.junit.NbModuleSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests application parameters set through {@link ExplicitProcessParameters}.
 *
 * @author Martin Entlicher
 */
public class ProcessParametersTest extends AbstractDebugTest {

    private static final String CPP_APP =
            "#include <unistd.h>\n" +
            "#include <stdio.h>\n" +
            "#include <limits.h>\n" +
            "\n" +
            "int main(int argc, char **argv, char **envp) {\n" +
            "   char buffer[PATH_MAX];\n" +
            "   if (getcwd(buffer, sizeof(buffer)) != NULL) {\n" +
            "       printf(\"CWD:%s\\n\", buffer);\n" +
            "   } else {\n" +
            "       perror(\"getcwd() error\");\n" +
            "       return 1;\n" +
            "   }\n" +
            "   while (--argc) {\n" +
            "      printf(\"ARG:%s\\n\", *(++argv));\n" +
            "   }\n" +
            "   for (char **e = envp; *e; e++) {\n" +
            "      printf(\"ENV:%s\\n\", *e);\n" +
            "   }\n" +
            "   return 0;\n" +
            "}";

    public ProcessParametersTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testWorkingDir() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        File appWD = Files.createTempDirectory("TestCWD").toFile();
        try {
            ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                    workingDirectory(appWD).
                    build();
            startDebugging("main", wd, params);
            assertEquals(0, waitAppProcessExit());
            String output = stdOut.toString();
            assertEquals("CWD:" + appWD.getAbsolutePath(), output.substring(0, output.indexOf('\n')));
        } finally {
            appWD.delete();
        }
    }

    public void testArgumentsOwn() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        ExplicitProcessParameters params = ExplicitProcessParameters.empty();
        startDebugging(Arrays.asList(new File(wd, "main").getAbsolutePath(), "Param1", "Param 2"), params);
        assertEquals(0, waitAppProcessExit());
        assertArguments("Param1", "Param 2");
    }

    public void testArgumentsExplicit() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                arg("ExpParam1").
                arg("ExpParam 2").
                build();
        startDebugging(Arrays.asList(new File(wd, "main").getAbsolutePath()), params);
        assertEquals(0, waitAppProcessExit());
        assertArguments("ExpParam1", "ExpParam 2");
    }

    public void testArgumentsCombinedReplace() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                arg("ExpParam1").
                arg("ExpParam 2").
                build();
        startDebugging(Arrays.asList(new File(wd, "main").getAbsolutePath(), "Param1", "Param 2"), params);
        assertEquals(0, waitAppProcessExit());
        assertArguments("ExpParam1", "ExpParam 2");
    }

    public void testArgumentsCombinedAppend() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                arg("ExpParam1").
                arg("ExpParam 2").
                replaceArgs(false).
                build();
        startDebugging(Arrays.asList(new File(wd, "main").getAbsolutePath(), "Param1", "Param 2"), params);
        assertEquals(0, waitAppProcessExit());
        assertArguments("Param1", "Param 2", "ExpParam1", "ExpParam 2");
    }

    public void testEnvironmentVariables() throws Exception {
        File wd = getWorkDir();
        createSourceFile("main.cpp", wd, CPP_APP);
        compileCPP("main", wd);
        ExplicitProcessParameters params = ExplicitProcessParameters.builder().
                environmentVariable("TEST_VAR1", "test variable value 1").
                environmentVariable("TEST_VAR2", "test variable value 2").
                environmentVariable("USER", null).
                build();
        startDebugging("main", wd, params);
        assertEquals(0, waitAppProcessExit());
        String output = stdOut.toString();
        Map<String, String> env = new HashMap<>();
        String[] lines = output.split("\n");
        for (String line : lines) {
            if (line.startsWith("ENV:")) {
                line = line.substring("ENV:".length());
                String[] var = line.split("=");
                env.put(var[0], var[1]);
            }
        }
        assertEquals("test variable value 1", env.get("TEST_VAR1"));
        assertEquals("test variable value 2", env.get("TEST_VAR2"));
        assertFalse("Removed variable is present", env.containsKey("USER"));
    }

    public static Test suite() {
        return NbModuleSuite.emptyConfiguration()
                            .addTest(ProcessParametersTest.class)
                            .enableModules(".*", ".*")
                            .gui(false)
                            .suite();
    }

    private void assertArguments(String... args) {
        String output = stdOut.toString();
        int index1 = 0;
        int index2 = 0;
        for (int i = 0; i < args.length; i++) {
            index1 = output.indexOf("ARG:", index2);
            if (index1 < 0) {
                fail("Missing argument " + i + " : " + args[i]);
            }
            index2 = output.indexOf('\n', index1);
            assertEquals("ARG:" + args[i] , output.substring(index1, index2));
        }
        index1 = output.indexOf("ARG:", index2);
        if (index1 >= 0) {
            fail("An extra argument: " + output.substring(index1));
        }
    }
}
