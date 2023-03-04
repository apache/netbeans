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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

/**
 *
 * @author Laszlo Kishalmi
 */
public class DebugTooling {

    final String buildDir;

    public DebugTooling(String buildDir) {
        this.buildDir = buildDir;
    }
    
    public NbProjectInfo loadInfo(File project, String... extraArgs) {
        NbProjectInfo ret = null;
        GradleConnector gconn = GradleConnector.newConnector();
        ProjectConnection pconn = gconn.forProjectDirectory(project).connect();
        BuildActionExecuter<NbProjectInfo> action = pconn.action(new NbProjectInfoAction());

        action.setJvmArguments(
                "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006",
                "-DNETBEANS_TOOLING_JAR=" + buildDir + "/libs/netbeans-gradle-tooling.jar"
        ); 
        
        ArrayList<String> args = new ArrayList<>();
        args.add("-I");
        args.add(buildDir + "/resources/main/nb-tooling.gradle");
        args.addAll(Arrays.asList(extraArgs));
        
        action.withArguments(args.toArray(new String[args.size()]));
        try {
            ret = action.run();
        } catch (GradleConnectionException | IllegalStateException ex) {
            System.err.println("Failed to retrieve project information for: " + project);
            ex.printStackTrace();
        } finally {
            try {
                pconn.close();
            } catch (NullPointerException ex) {
            }
        }

        return ret;
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar netbeans-gradle-tooling.jar <project dir> [<gradle args>...]");
            System.exit(1);
        }
        File projectDir = new File(args[0]);
        if (!projectDir.isDirectory()) {
            System.err.println("Project not found at: " + projectDir.getAbsolutePath());
            System.exit(1);            
        }
        String buildDir = System.getProperty("user.dir") + "/build";
        if (!new File(buildDir).isDirectory()) {
            System.err.println("This tool can be executed from it's project dir only");
            System.exit(1);                        
        }
        DebugTooling dt = new DebugTooling(buildDir);
        NbProjectInfo info = dt.loadInfo(projectDir, Arrays.copyOfRange(args, 1, args.length));
        if (info.hasException()) {
            System.err.println(info.getGradleException());
        } else {
            System.out.println(info);
        }
    }
    
    private static class NbProjectInfoAction implements Serializable, BuildAction<NbProjectInfo> {

        @Override
        public NbProjectInfo execute(BuildController bc) {
            return bc.getModel(NbProjectInfo.class);
        }
    }


}
