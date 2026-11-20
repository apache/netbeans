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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Runs set of tests with `ant maven-install` command.
 */
class MavenInstallChecker {
    public static void main(String... args) throws Exception {
        if (args.length != 0) {
            throw new IllegalArgumentException("Don't specify any application arguments. Was: " + Arrays.toString(args));
        }

        String nbmInstallVersion = System.getProperty("nbm.install.version");
        if (nbmInstallVersion == null) {
            throw new IllegalArgumentException("Execute with -Dnbm.install.version=dev-SNAPSHOT or other value!");
        }

        {
            // testing: ant -f platform/openide.util.ui.svg maven-install
            // an NBM without public packages
            File pom = files(findMavenRepositoryRoot(), "org", "netbeans", "modules", "org-openide-util-ui-svg", nbmInstallVersion, "org-openide-util-ui-svg-" + nbmInstallVersion + ".pom");
            File nbm = files(findMavenRepositoryRoot(), "org", "netbeans", "modules", "org-openide-util-ui-svg", nbmInstallVersion, "org-openide-util-ui-svg-" + nbmInstallVersion + ".nbm");

            pom.delete();
            nbm.delete();
            assertFile(false, pom, "All previous debris removed");
            assertFile(false, nbm, "All previous debris removed");

            mavenInstall("platform", "openide.util.ui.svg", nbmInstallVersion);
            assertFile(true, pom, "pom.xml created");
            assertFile(true, nbm, "NBM file created");

            ZipFile nbmZip = new ZipFile(nbm);
            String nbmInfo = readEntry(nbmZip, "Info/info.xml");

            String expectedDistribution = "distribution=\"m2:/org.netbeans.modules:org-openide-util-ui-svg:" + nbmInstallVersion + ":nbm";
            if (!nbmInfo.contains(expectedDistribution)) {
                throw new AssertionError("Missing distribution attribute (e.g. " + expectedDistribution + ") in:\n" + takeNLines(nbmInfo, 10));
            }
        }

        {
            // testing: ant -f platform/openide.awt maven-install
            // an NBM with public packages
            File pom = files(findMavenRepositoryRoot(), "org", "netbeans", "api", "org-openide-awt", nbmInstallVersion, "org-openide-awt-" + nbmInstallVersion + ".pom");
            File nbm = files(findMavenRepositoryRoot(), "org", "netbeans", "api", "org-openide-awt", nbmInstallVersion, "org-openide-awt-" + nbmInstallVersion + ".nbm");

            pom.delete();
            nbm.delete();
            assertFile(false, pom, "All previous debris removed");
            assertFile(false, nbm, "All previous debris removed");

            mavenInstall("platform", "openide.awt", nbmInstallVersion);
            assertFile(true, pom, "pom.xml created");
            assertFile(true, nbm, "NBM file created");

            ZipFile nbmZip = new ZipFile(nbm);
            String nbmInfo = readEntry(nbmZip, "Info/info.xml");

            String expectedDistribution = "distribution=\"m2:/org.netbeans.api:org-openide-awt:" + nbmInstallVersion + ":nbm";
            if (!nbmInfo.contains(expectedDistribution)) {
                throw new AssertionError("Missing distribution attribute (e.g. " + expectedDistribution + ") in:\n" + takeNLines(nbmInfo, 10));
            }
        }

        System.err.println("MavenInstallChecker successfully finished");
        System.exit(0);
    }

    // <editor-fold defaultstate="collapsed" desc="Useful Utilities">
    private static void mavenInstall(String cluster, String module, String nbmInstallVersion) throws IOException, InterruptedException, URISyntaxException {
        List<String> cmd = List.of(
            "ant", "-q",
            "-f", files(findNetBeansRepositoryRoot(), cluster, module).getPath(),
            "-Dnbm.install.version=" + nbmInstallVersion,
            "maven-install"
        );
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.inheritIO();
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Failure executing `" + String.join(" ", cmd) + "` exit code: " + exitCode);
        }
    }

    private static File findMavenRepositoryRoot() {
        String home = System.getProperty("user.home");
        if (home == null) {
            throw new IllegalStateException("No $HOME!");
        }
        return files(new File(home), ".m2", "repository");
    }

    private static File findNetBeansRepositoryRoot() throws URISyntaxException {
        URL loc = MavenInstallChecker.class.getProtectionDomain().getCodeSource().getLocation();
        File script = new File(loc.toURI());
        File dir = script.getParentFile();
        while (dir != null && dir.isDirectory()) {
            var buildXml = files(dir, "nbbuild", "build.xml");
            if (buildXml.exists()) {
                return dir;
            }
            dir = dir.getParentFile();
        }
        throw new IllegalStateException("Cannot find NetBeans repository root at " + loc);
    }

    private static File files(File root, String... names) {
        File f = root;
        for (String n : names) {
            f = new File(f, n);
        }
        return f;
    }

    private static void assertFile(boolean expected, File file, String msg) {
        boolean isReal = file.exists();
        System.err.println("asserting " + file + " exists: " + isReal);
        if (isReal != expected) {
            throw new AssertionError(msg + ": " + file);
        }
    }

    private static String readEntry(ZipFile zip, String name) throws IOException {
        ZipEntry entry = zip.getEntry(name);
        if (entry == null) {
            throw new AssertionError("No " + name + " in " + zip.getName());
        }
        try (InputStream is = zip.getInputStream(entry)) {
            byte[] bytes = is.readAllBytes();
            return new String(bytes);
        }
    }

    private static String takeNLines(String text, int n) {
        return text.lines().limit(n).collect(Collectors.joining("\n"));
    }
    // </editor-fold>
}

