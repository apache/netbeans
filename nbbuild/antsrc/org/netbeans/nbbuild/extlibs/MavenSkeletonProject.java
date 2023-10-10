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
package org.netbeans.nbbuild.extlibs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task to check that external libraries have legitimate licenses, etc.
 */
public class MavenSkeletonProject extends Task {

    private File nball;

    public void setNball(File nball) {
        this.nball = nball;
    }

    private File mavenfolderFile;

    /**
     * Folder to put the skeleton project in.
     * @param report
     */
    public void setMavenFolder(File report) {
        this.mavenfolderFile = report;
    }

    private Set<String> modules;

    public @Override
    void execute() throws BuildException {
        try { // XXX workaround for http://issues.apache.org/bugzilla/show_bug.cgi?id=43398
            if (getProject().getProperty("allmodules") != null) {
                modules = new TreeSet<>(Arrays.asList(getProject().getProperty("allmodules").split("[, ]+")));
                modules.add("nbbuild");
            } else {
                Path nbAllPath = nball.toPath();
                try ( Stream<Path> walk = Files.walk(nbAllPath)) {
                    modules = new TreeSet<>(
                            walk.filter(p -> Files.exists(p.resolve("external/binaries-list")))
                                    .map(p -> nbAllPath.relativize(p))
                                    .map(p -> p.toString())
                                    .collect(Collectors.toSet())
                    );
                }
            }
            try {
                buildLibPomForMaven();
            } catch (IOException x) {
                throw new BuildException(x, getLocation());
            }

        } catch (NullPointerException | IOException x) {
            x.printStackTrace();
            throw new BuildException(x);
        }
    }

    private void buildLibPomForMaven() throws IOException {
        Path pseudoMaven = mavenfolderFile.toPath();
        if (Files.exists(pseudoMaven)) {
            try ( Stream<Path> walk = Files.walk(pseudoMaven)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }
        Path pseudoMavendirectory = Files.createDirectory(pseudoMaven);
        Path parentPom = Files.createFile(pseudoMavendirectory.resolve("pom.xml"));
        Files.write(parentPom, ("<project>"
                + "\n  <modelVersion>4.0.0</modelVersion>"
                + "\n  <groupId>com.mycompany.app</groupId>"
                + "\n  <artifactId>my-app</artifactId>"
                + "\n  <version>1</version>"
                + "\n  <packaging>pom</packaging>"
                + "<distributionManagement>\n"
                + "    <site><id>dummy</id><url>https://netbeans.apache.org/dummy</url><name>dummy</name></site>\n"
                + "  </distributionManagement>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        Files.write(parentPom, "\n  <modules>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        for (String module : modules) {
            File d = new File(new File(nball, module), "external");
            if (d.exists() && d.isDirectory()) {
                String moduleName = module.replace("/", "").replace(".", "");
                Files.write(parentPom, ("\n    <module>" + moduleName + "</module>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Path modulesPathFolder = Files.createDirectory(pseudoMavendirectory.resolve(moduleName));
                Path moduleparentPom = Files.createFile(modulesPathFolder.resolve("pom.xml"));
                Files.write(moduleparentPom, ("<project>"
                        + "\n  <modelVersion>4.0.0</modelVersion>"
                        + "\n  <parent><groupId>com.mycompany.app</groupId><artifactId>my-app</artifactId><version>1</version></parent>"
                        + "\n  <groupId>com.mycompany.app</groupId>"
                        + "\n  <artifactId>" + moduleName + "</artifactId>"
                        + "\n  <version>1</version>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                Files.write(moduleparentPom, "\n  <dependencies>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                File list = new File(d, "binaries-list");
                if (list.isFile()) {

                    try ( Reader r = new FileReader(list)) {
                        BufferedReader br = new BufferedReader(r);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (line.startsWith("#")) {
                                continue;
                            }
                            if (line.trim().length() == 0) {
                                continue;
                            }
                            String[] hashAndFile = line.split(" ", 2);
                            if (hashAndFile.length < 2) {
                                throw new BuildException("Bad line '" + line + "' in " + list);
                            }
                            if (MavenCoordinate.isMavenFile(hashAndFile[1])) {
                                MavenCoordinate coordinate = MavenCoordinate.fromGradleFormat(hashAndFile[1]);
                                Files.write(moduleparentPom, ("\n    <dependency>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                                Files.write(moduleparentPom, ("\n    <groupId>" + coordinate.getGroupId() + "</groupId>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                                Files.write(moduleparentPom, ("\n    <artifactId>" + coordinate.getArtifactId() + "</artifactId>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                                Files.write(moduleparentPom, ("\n    <version>" + coordinate.getVersion() + "</version>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                                Files.write(moduleparentPom, ("\n    <type>" + coordinate.getExtension() + "</type>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

                                if (coordinate.hasClassifier()) {
                                    Files.write(moduleparentPom, ("\n    <classifier>" + coordinate.getClassifier() + "</classifier>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                                }
                                Files.write(moduleparentPom, ("\n    </dependency>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                            }
                        }
                    }
                }
                Files.write(moduleparentPom, "\n  </dependencies>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                Files.write(moduleparentPom, "</project>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

            }
        }
        Files.write(parentPom, "\n  </modules>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        Files.write(parentPom, ("\n  <build>\n"
                + "        <pluginManagement>\n"
                + "            <plugins>\n"
                + "                <plugin>\n"
                + "                    <artifactId>maven-site-plugin</artifactId>\n"
                + "                    <version>4.0.0-M1</version>\n"
                + "                </plugin>\n"
                + "            </plugins>\n"
                + "        </pluginManagement>\n"
                + "  </build>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

        Files.write(parentPom, ("\n  <reporting>\n"
                + "    <plugins>\n"
                + "      <plugin>\n"
                + "        <groupId>org.owasp</groupId>\n"
                + "        <artifactId>dependency-check-maven</artifactId>\n"
                + "        <version>7.1.0</version>\n"
                + "        <configuration>\n"
                + "          <assemblyAnalyzerEnabled>false</assemblyAnalyzerEnabled>\n"
                + "          <failOnError>false</failOnError>\n"
                + "        </configuration>\n"
                + "        <reportSets>\n"
                + "          <reportSet>\n"
                + "            <reports>\n"
                + "              <report>aggregate</report>\n"
                + "            </reports>\n"
                + "          </reportSet>\n"
                + "        </reportSets>\n"
                + "      </plugin>\n"
                + "      \n"
                + "      <plugin>\n"
                + "        <groupId>org.codehaus.mojo</groupId>\n"
                + "        <artifactId>versions-maven-plugin</artifactId>\n"
                + "        <version>2.11.0</version>\n"
                + "        <reportSets>\n"
                + "          <reportSet>\n"
                + "            <reports>\n"
                + "              <report>dependency-updates-report</report>\n"
                + "            </reports>\n"
                + "          </reportSet>\n"
                + "        </reportSets>\n"
                + "      </plugin>\n"
                + "    </plugins>\n"
                + "  </reporting>").getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        Files.write(parentPom, "</project>".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);

    }

}
