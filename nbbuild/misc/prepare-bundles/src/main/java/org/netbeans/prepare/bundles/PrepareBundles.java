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
package org.netbeans.prepare.bundles;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * Prepare bundles and license files for a group of node modules.
 */
public class PrepareBundles {

    private static final List<Pattern> LICENSE_FILE_NAMES = Arrays.asList(
        Pattern.compile("license", Pattern.CASE_INSENSITIVE),
        Pattern.compile("LICENSE.txt", Pattern.CASE_INSENSITIVE),
        Pattern.compile("LICENSE-MIT.txt", Pattern.CASE_INSENSITIVE),
        Pattern.compile("LICENSE.md", Pattern.CASE_INSENSITIVE)
    );
    private static final String nl = "\n";

    public static void main(String... args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        if (args.length != 2) {
            throw new IllegalStateException("Requires two parameters: location of the bundles directory, and the location of the NetBeans checkout.");
        }

        Path targetDir = Paths.get(args[0]);
        Path packagesDir = targetDir.resolve("package");
        new ProcessBuilder("npm", "install").directory(packagesDir.toFile()).inheritIO().start().waitFor();
        Path bundlesDir = targetDir.resolve("bundles");
        Files.createDirectories(bundlesDir);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(bundlesDir)) {
            for (Path bundle : ds) {
                Files.delete(bundle);
            }
        }

        Path licensesDir = targetDir.resolve("licenses");

        Files.createDirectories(licensesDir);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(licensesDir)) {
            for (Path license : ds) {
                Files.delete(license);
            }
        }

        Path externalDir = targetDir.resolve("external");
        Files.createDirectories(externalDir);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(externalDir)) {
            for (Path external : ds) {
                Files.delete(external);
            }
        }

        StringBuilder sb = new StringBuilder();
        Map<List<String>, LicenseUses> tokens2Projects = new HashMap<>();
        Map<String, LicenseDescription> project2License = new HashMap<>();
        Map<String, String> project2Notice = new HashMap<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(packagesDir.resolve("node_modules"));
            Writer binariesList = new OutputStreamWriter(Files.newOutputStream(bundlesDir.resolve("binaries-list")), "UTF-8")) {
            for (Path module : ds) {
                if (".bin".equals(module.getFileName().toString())) continue;
                if (".package-lock.json".equals(module.getFileName().toString())) continue;
                if ("@types".equals(module.getFileName().toString())) continue;
                if ("@vscode".equals(module.getFileName().toString())) {
                    try (DirectoryStream<Path> sds = Files.newDirectoryStream(module)) {
                        for (Path sModule : sds) {
                            checkModule(sModule, sb, tokens2Projects, project2License, bundlesDir, targetDir, externalDir, binariesList);
                        }
                    }
                    continue;
                }
                if ("@ungap".equals(module.getFileName().toString())) {
                    module = module.resolve("promise-all-settled");
                }
                checkModule(module, sb, tokens2Projects, project2License, bundlesDir, targetDir, externalDir, binariesList);
            }
        }
        if (sb.length() > 0) {
            throw new IllegalStateException(sb.toString());
        }

        Map<String, String> project2LicenseKey = new HashMap<>();

        Map<List<String>, String> knownLicenseTokens2LicenseKey = new HashMap<>();
        Path nb_all = Paths.get(args[1]);

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(nb_all.resolve("nbbuild").resolve("licenses"))) {
            for (Path license : ds) {
                knownLicenseTokens2LicenseKey.put(licenseTextToTokens(readString(license)), license.getFileName().toString());
            }
        }

        for (Entry<List<String>, LicenseUses> e : tokens2Projects.entrySet()) {
            LicenseUses use = e.getValue();
            String licenseName = knownLicenseTokens2LicenseKey.get(e.getKey());
            if (licenseName == null) {
                licenseName = use.key + "-" + use.projects.stream().collect(Collectors.joining("-"));
                try (OutputStream out = Files.newOutputStream(licensesDir.resolve(licenseName))) {
                    out.write(use.licenseText.getBytes("UTF-8"));
                }
            }
            for (String prj : use.projects) {
                project2LicenseKey.put(prj, licenseName);
            }
        }
        for (Entry<String, LicenseDescription> e : project2License.entrySet()) {
            LicenseDescription licenseDesc = e.getValue();
            Path projectLicenseFile = bundlesDir.resolve(e.getKey() + "-" + licenseDesc.version + "-license.txt");
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(projectLicenseFile), "UTF-8")) {
                w.write("Name: " + licenseDesc.name + nl);
                w.write("Description: " + licenseDesc.description + nl);
                w.write("Version: " + licenseDesc.version + nl);
                w.write("License: " + project2LicenseKey.get(e.getKey()) + nl);
                w.write("Origin: " + licenseDesc.homepage + nl);
                w.write(nl);
                w.write(licenseDesc.licenseText);
            }
            String notice = project2Notice.get(e.getKey());
            if (notice != null) {
                Path projectNoticeFile = bundlesDir.resolve(e.getKey() + "-" + licenseDesc.version + "-notice.txt");
                try (Writer w = new OutputStreamWriter(Files.newOutputStream(projectNoticeFile), "UTF-8")) {
                    w.write(notice);
                }
            }
        }


    }

    private static void checkModule(Path module, StringBuilder sb, Map<List<String>, LicenseUses> tokens2Projects, Map<String, LicenseDescription> project2License, Path bundlesDir, Path targetDir, Path externalDir, final Writer binariesList) throws NoSuchAlgorithmException, IOException, IllegalStateException, JsonSyntaxException {
        Path packageJson = module.resolve("package.json");
        if (!Files.isReadable(packageJson)) {
            throw new IllegalStateException("Cannot find package.json for: " + module.getFileName());
        }
        String packageJsonText = readString(packageJson);
        Map<String, Object> packageJsonData = new Gson().fromJson(packageJsonText, HashMap.class);
        String name = (String) packageJsonData.get("name");
        String version = (String) packageJsonData.get("version");
        String description = (String) packageJsonData.get("description");
        String homepage = (String) packageJsonData.get("homepage");
        String licenseKey = packageJsonData.get("license").toString();
        String licenseText = null;
        try (DirectoryStream<Path> insideModule = Files.newDirectoryStream(module)) {
            for (Path licenseCandidate : insideModule) {
                String fileName = licenseCandidate.getFileName().toString();
                
                if (LICENSE_FILE_NAMES.stream().anyMatch(p -> p.matcher(fileName).matches())) {
                    licenseText = readString(module.resolve(licenseCandidate));
                    break;
                }
            }
        }
        String hardcodedLicenseName = name + "-" + version + "-license";
        URL hardcodedLicense = PrepareBundles.class.getResource(hardcodedLicenseName);
        if (hardcodedLicense != null) {
            StringBuilder licenseTextBuffer = new StringBuilder();
            try (InputStream in = hardcodedLicense.openStream();
                    Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                int read;
                while ((read = r.read()) != (-1)) {
                    licenseTextBuffer.append((char) read);
                }
            }
            licenseText = licenseTextBuffer.toString();
        } else if (licenseText == null) {
            sb.append("Cannot find license for: ").
                    append(module.getFileName()).
                    append(" in ").
                    append(hardcodedLicenseName).
                    append("\n");
            return;
        }
        Path thirdpartynoticestxt = module.resolve("thirdpartynotices.txt");
        if (Files.isReadable(thirdpartynoticestxt)) {
            licenseText = "Parts of this work are licensed:\n" +
                    licenseText +
                    "\n\n" +
                    "Parts of this work are licensed:\n" +
                    readString(thirdpartynoticestxt);
        }
        //fill project2Notice here if needed
        
        List<String> tokens = licenseTextToTokens(licenseText);
        String licenseTextFin = licenseText;
        tokens2Projects.computeIfAbsent(tokens, t -> new LicenseUses(licenseKey, licenseTextFin)).projects.add(module.getFileName().toString());
        project2License.put(module.getFileName().toString(), new LicenseDescription(name, version, description, homepage, licenseKey, licenseText));
        Path bundle = bundlesDir.resolve(module.getFileName() + "-" + version + ".zip");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(bundle));
                Stream<Path> files = Files.walk(module, FileVisitOption.FOLLOW_LINKS)) {
            Path moduleFinal = module;
            files.forEach(p -> {
                if (p == moduleFinal) return ;
                try {
                    String relative = moduleFinal.getParent().relativize(p).toString();
                    boolean isDir = Files.isDirectory(p);
                    ZipEntry ze = new ZipEntry(relative + (isDir ? "/" : ""));
                    out.putNextEntry(ze);
                    if (!isDir) {
                        if (relative.equals("package.json")) {
                            out.write(packageJsonText.replace(targetDir.toString(), "").getBytes("UTF-8"));
                        } else {
                            Files.copy(p, out);
                        }
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        }
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(Files.readAllBytes(bundle));
        StringBuilder hash = new StringBuilder();
        for (byte b : md.digest()) {
            hash.append(String.format("%02X", b));
        }
        Path external = externalDir.resolve(hash + "-" + bundle.getFileName());
        Files.copy(bundle, external);
        binariesList.write(hash + " " + bundle.getFileName().toString() + nl);
    }

    private static String readString(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    private static List<String> licenseTextToTokens(String licenseText) {
        return Arrays.asList(licenseText.replaceAll("[ \n\r\t]+", " ").split(" "));
    }
    private static class LicenseDescription {
        public final String name;
        public final String version;
        public final String description;
        public final String homepage;
        public final String licenseKey;
        public final String licenseText;
        public final List<String> bundles = new ArrayList<>();

        public LicenseDescription(String name, String version, String description, String homepage, String licenseKey, String licenseText) {
            this.name = name;
            this.version = version;
            this.description = description;
            this.homepage = homepage;
            this.licenseKey = licenseKey;
            this.licenseText = licenseText;
        }

        @Override
        public String toString() {
            return "LicenseDescription{" + "version=" + version + ", description=" + description + ", homepage=" + homepage + ", licenseKey=" + licenseKey + ", licenseText=" + licenseText + ", bundles=" + bundles + '}';
        }

    }
    private static class LicenseUses {
        public final String key;
        public final String licenseText;
        public final List<String> projects = new ArrayList<>();

        public LicenseUses(String key, String licenseText) {
            this.key = key;
            this.licenseText = licenseText;
        }

    }
}
