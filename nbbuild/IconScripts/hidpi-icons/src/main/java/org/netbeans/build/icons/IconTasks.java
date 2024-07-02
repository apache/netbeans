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
package org.netbeans.build.icons;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import org.netbeans.build.icons.TypeTaggedString.ArtboardName;
import org.netbeans.build.icons.TypeTaggedString.IconPath;
import org.netbeans.build.icons.TypeTaggedString.Hash;

/**
 * <p>Script for maintaining mappings between bitmap icons (PNG and GIF files) and newer SVG
 * versions. See the the {@code IconScripts/README.txt} file for more information.
 *
 * <p>This script must be run with nbbuild/IconScripts/hidpi-icons as the working directory (as is
 * the default when running this file from the NetBeans IDE).
 */
public class IconTasks {
    private static final String LICENSE_HEADER = readLicenseHeader();

    public static void main(String[] args) throws IOException {
        final File NBSRC_DIR = new File(new File(System.getProperty("user.dir")), "../../../");
        final File ILLUSTRATOR_SVGS_DIR =
                new File(NBSRC_DIR, "nbbuild/IconScripts/illustrator_exports/");
        final File TABLES_DIR = new File(NBSRC_DIR, "nbbuild/IconScripts/tables/");
        final File ICON_HASHES_FILE = new File(TABLES_DIR, "icon-hashes.txt");
        final File MAPPINGS_FILE = new File(TABLES_DIR, "mappings.tsv");
        final File READY_ARTBOARDS_FILE = new File(TABLES_DIR, "ready-artboards.txt");
        final File ICONS_HTML_FILE = new File(NBSRC_DIR, "nbbuild/IconScripts/icons.html");
        boolean COPY_ILLUSTRATOR_SVGS =
                ILLUSTRATOR_SVGS_DIR.listFiles(f-> f.toString().endsWith(".svg")).length > 0;

        ImmutableMap<IconPath, Hash> iconHashesByFile =
                readIconHashesByFile(NBSRC_DIR, ICON_HASHES_FILE);
        ImmutableSetMultimap<Hash, IconPath> filesByHash = Util.reverse(iconHashesByFile);
        ImmutableMap<Hash, Dimension> dimensionsByHash = readImageDimensions(NBSRC_DIR, filesByHash);
        // iconHashesByFile.forEach((key, value) -> System.out.println(key + " : " + value));

        ImmutableMap<IconPath, ArtboardName> artboardByFile =
                readArtboardByFileMappings(NBSRC_DIR, MAPPINGS_FILE);
        // artboardByFile.forEach((key, value) -> System.out.println(key + " : " + value));

        ImmutableSet<ArtboardName> readyArtboards = readReadyArtboards(READY_ARTBOARDS_FILE);

        SetMultimap<ArtboardName,Hash> hashesByArtboard = LinkedHashMultimap.create();
        for (Entry<IconPath, ArtboardName> entry : artboardByFile.entrySet()) {
            IconPath ip = entry.getKey();
            ArtboardName artboard = entry.getValue();
            Hash hash = Util.getChecked(iconHashesByFile, ip);
            hashesByArtboard.put(artboard, hash);
        }

        Set<ArtboardName> unknownReadyArtboards =
                Sets.difference(readyArtboards, hashesByArtboard.keySet());
        if (!unknownReadyArtboards.isEmpty()) {
            throw new RuntimeException("Unknown artboards " + unknownReadyArtboards);
        }
        if (COPY_ILLUSTRATOR_SVGS) {
            for (ArtboardName artboard : readyArtboards) {
                File artboardSVGFile = getIllustratorSVGFile(ILLUSTRATOR_SVGS_DIR, artboard);
                if (!artboardSVGFile.exists()) {
                    throw new RuntimeException("File not found: " + artboardSVGFile);
                }
            }
        }

        // hashesByArtboard.asMap().forEach((key, value) -> System.out.println(key + " : " + value));

        Map<IconPath, ArtboardName> newArtboardByFile = Maps.newLinkedHashMap();

        for (Entry<ArtboardName, Hash> entry : hashesByArtboard.entries()) {
            ArtboardName artboard = entry.getKey();
            Hash hash = entry.getValue();
            for (IconPath ip : filesByHash.get(hash)) {
                Util.putChecked(newArtboardByFile, ip, artboard);
            }
        }

        ArtboardName UNASSIGNED_ARTBOARD = new ArtboardName("(no assigned artboard)");
        {
            List<Entry<IconPath,Dimension>> unassignedIcons = Lists.newArrayList();
            for (Entry<IconPath, Hash> entry : iconHashesByFile.entrySet()) {
                IconPath ip = entry.getKey();
                Hash hash = entry.getValue();
                Dimension dim = Util.getChecked(dimensionsByHash, hash);
                if (dim.width <= 64 && dim.height <= 64 && dim.width > 1 && dim.height > 1) {
                    unassignedIcons.add(new SimpleEntry(ip, dim));
                }
            }
            // Order unassigned icons by width, then by height.
            unassignedIcons.sort((e1, e2) -> {
                int ret = Integer.compare(e1.getValue().width, e2.getValue().width);
                if (ret == 0) {
                    ret = Integer.compare(e1.getValue().height, e2.getValue().height);
                }
                return ret;
            });
            for (Entry<IconPath,Dimension> entry : unassignedIcons) {
                newArtboardByFile.putIfAbsent(entry.getKey(), UNASSIGNED_ARTBOARD);
            }
        }

        // newArtboardByFile.forEach((key, value) -> System.out.println(key + " : " + value));
        ImmutableSetMultimap<ArtboardName, IconPath> filesByArtboard =
                Util.reverse(newArtboardByFile);

        for (ArtboardName artboard : readyArtboards) {
            final String svgContentToWrite;
            if (COPY_ILLUSTRATOR_SVGS) {
                // Existence was checked earlier.
                svgContentToWrite = prepareSVGWithInsertedLicense(ILLUSTRATOR_SVGS_DIR, artboard);
            } else {
                svgContentToWrite = null;
            }
            for (IconPath ip : filesByArtboard.get(artboard)) {
                if (shouldIgnoreFile(ip)) {
                    continue;
                }
                IconPath destSVG = getSVGIconPath(ip);
                // System.out.println(srcSVGFile + "\t" + destSVG);
                File destSVGFile = new File(NBSRC_DIR, destSVG.toString());
                if (svgContentToWrite != null) {
                    try (PrintWriter pw = createPrintWriter(destSVGFile)) {
                        pw.print(svgContentToWrite);
                    }
                } else {
                    if (!destSVGFile.exists()) {
                        throw new RuntimeException(destSVGFile + " does not exist, and no " +
                                "SVGs to copy exist in the illustrator_exports directory.");
                    }
                }
            }
        }

        /* The mappings file is assumed to be in a git repo so that the user of the script can
        see what changed from run to run. */
        try (PrintWriter mappingsPW = createPrintWriter(MAPPINGS_FILE);
             PrintWriter htmlPW = createPrintWriter(ICONS_HTML_FILE))
        {
            htmlPW.println(LICENSE_HEADER);
            htmlPW.println("<html>\n" +
                    "<head>\n" +
                    "<title>Icons</title>\n" +
                    "<base href='../../'>\n" +
                    "<style>\n" +
                    "table td, table td * { vertical-align: top; margin-left: 5px; }\n" +
                    "thead td { padding-right: 10px; padding-bottom: 10px; }\n" +
                    "td { padding-right: 10px; }\n" +
                    "thead { font-weight: bold; }\n" +
                    "</style>" +
                    "</head>" +
                    "<body>");
            htmlPW.println("<h1>NetBeans Bitmap and SVG Icons</h1>\n" +
                    "<table border='0' cellpadding='1' cellspacing='0'>\n" +
                    "<thead><tr><td>Artboard Name<td>SVG<td>Bitmap<td>Dim<td>" +
                    "Path of Bitmap in Source Repo (no icon image means same as previous)</tr></thead>");
            int artboardIdx = 0;
            Set<ArtboardName> artboardsInOrder = Sets.newLinkedHashSet();
            artboardsInOrder.addAll(Sets.filter(filesByArtboard.keySet(), a -> readyArtboards.contains(a)));
            artboardsInOrder.addAll(filesByArtboard.keySet());
            for (ArtboardName artboard : artboardsInOrder) {
                List<IconPath> ips = Lists.newArrayList(filesByArtboard.get(artboard));
                ips.removeIf(ip -> shouldIgnoreFile(ip));
                /* Make sure to retain the original order, except keep files with the same hash
                together. */
                Map<Hash,Integer> order = Maps.newLinkedHashMap();
                for (IconPath ip : ips) {
                    order.putIfAbsent(Util.getChecked(iconHashesByFile, ip), order.size());
                }
                ips.sort((ip1, ip2) -> Integer.compare(
                        Util.getChecked(order, Util.getChecked(iconHashesByFile, ip1)),
                        Util.getChecked(order, Util.getChecked(iconHashesByFile, ip2))));
                int subRowIdx = 0;
                Hash previousHash = null;
                for (IconPath ip : ips) {
                    Hash hash = Util.getChecked(iconHashesByFile, ip);
                    if (!UNASSIGNED_ARTBOARD.equals(artboard)) {
                        mappingsPW.println(artboard + "\t" + ip);
                    }

                    htmlPW.print(artboardIdx % 2 == 0 ? "<tr>" :
                            "<tr style='background: #eee'>");
                    if (subRowIdx == 0) {
                        htmlPW.print("<td rowspan='" + ips.size() + "'>" + artboard);
                        htmlPW.print("<td rowspan='" + ips.size() + "'>");
                        if (readyArtboards.contains(artboard)) {
                            htmlPW.print("<img src='" + getSVGIconPath(ip) + "'>");
                        }
                    }
                    htmlPW.print("<td>");
                    if (!hash.equals(previousHash)) {
                        htmlPW.print("<img src='" + ip + "'>");
                    }
                    htmlPW.print("<td>");
                    if (!hash.equals(previousHash)) {
                        Dimension dim = Util.getChecked(dimensionsByHash, hash);
                        htmlPW.print(dim.width + "x" + dim.height);
                    }
                    htmlPW.print("<td>" + ip);
                    htmlPW.println("</tr>");
                    previousHash = hash;
                    subRowIdx++;
                }
                artboardIdx++;
            }
            htmlPW.println("</table>");
            htmlPW.println("</body>");
        }
    }

    private static PrintWriter createPrintWriter(File file) throws IOException {
        // See https://stackoverflow.com/questions/1014287/is-there-a-way-to-make-printwriter-output-to-unix-format/14749004
        return new PrintWriter(new BufferedOutputStream(new FileOutputStream(file))) {
            @Override
            public void println() {
                write('\n');
            }
        };
    }

    private static IconPath getSVGIconPath(IconPath ip) {
        // Extension is verified in BitmapFile's constructor.
        String path = ip.toString();
        return new IconPath(path.substring(0, path.length() - 4) + ".svg");
    }

    private static File getIllustratorSVGFile(File illustratorSVGsDir, ArtboardName artboard) {
        Preconditions.checkNotNull(artboard);
        return new File(illustratorSVGsDir, "icon_" + artboard + ".svg");
    }

    private static boolean shouldIgnoreFile(IconPath ip) {
        String s = ip.toString();
        return s.startsWith("ide/usersguide/javahelp/") ||
                /* The window system icons have paint()-based vector icon implementations. See
                https://github.com/apache/netbeans/pull/859 */
                s.startsWith("platform/o.n.swing.tabcontrol/") ||
                s.startsWith("platform/openide.awt/") && s.contains("close_"); // close/bigclose
    }

    /**
     * Read the mappings.tsv file, where each line should state an artboard name followed by
     * (tab-separated) a relative filename of a PNG or GIF file. The existence of each file is
     * validated. For each artboard, the first file in the iteration order is the one that should
     * serve as a template for the SVG file.
     */
    private static ImmutableMap<IconPath, ArtboardName> readArtboardByFileMappings(File nbsrcDir, File file) throws IOException {
        Map<IconPath, ArtboardName> ret = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length == 2) {
                    String artboard = parts[0].trim();
                    String filePath = parts[1].trim();
                    if (filePath.endsWith(".svg")) {
                        throw new RuntimeException("File mapping cannot be to an SVG file.");
                    }
                    File actualFile = new File(nbsrcDir, filePath);
                    if (!actualFile.exists()) {
                        throw new RuntimeException("File does not exist: " + actualFile);
                    }
                    IconPath iconPath = new IconPath(filePath);
                    ArtboardName artboardName = new ArtboardName(artboard);
                    if (shouldIgnoreFile(iconPath)) {
                        throw new RuntimeException(
                                "Ignore list should not match mapped icon (" + iconPath + ")");
                    }
                    Util.putChecked(ret, new IconPath(filePath), artboardName);
                } else {
                    throw new RuntimeException("Invalid line: " + line);
                }
            }
        }
        return ImmutableMap.copyOf(ret);
    }

    /**
     * Read the icon-hashes.txt file, which should contain a SHA-256 hash followed by
     * (space-separated) a relative filename of PNG and GIF files in the NetBeans source directory.
     * The existence of each file is validated, but the hashes are assumed to be correct.
     */
    private static ImmutableMap<IconPath, Hash> readIconHashesByFile(File nbsrcDir, File file) throws IOException {
        Map<IconPath, Hash> ret = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+", 2);
                if (parts.length == 2) {
                    String hash = parts[0].trim();
                    String filePath = parts[1].trim();
                    File actualFile = new File(nbsrcDir, filePath);
                    if (!actualFile.exists()) {
                        throw new RuntimeException("File does not exist: " + actualFile);
                    }
                    Util.putChecked(ret, new IconPath(filePath), new Hash(hash));
                } else {
                    throw new RuntimeException("Invalid line: " + line);
                }
            }
        }
        return ImmutableMap.copyOf(ret);
    }

    private static ImmutableSet<ArtboardName> readReadyArtboards(File file) throws IOException {
        Set<ArtboardName> ret = Sets.newLinkedHashSet();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Util.addChecked(ret, new ArtboardName(line.trim()));
            }
        }
        return ImmutableSet.copyOf(ret);
    }

    private static String prepareSVGWithInsertedLicense(File illustratorSVGsDir, ArtboardName artboard) throws IOException {
        StringBuilder ret = new StringBuilder();
        File srcFile = getIllustratorSVGFile(illustratorSVGsDir, artboard);
        try (BufferedReader br = new BufferedReader(new FileReader(srcFile))) {
            String line;
            boolean firstLine = true;
            String EXPECTED_FIRST_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
            while ((line = br.readLine()) != null) {
                if (firstLine && !line.equals(EXPECTED_FIRST_LINE)) {
                    throw new RuntimeException(srcFile + ": First line was not " +
                            EXPECTED_FIRST_LINE);
                }
                ret.append(line).append("\n");
                if (firstLine) {
                    ret.append(LICENSE_HEADER);
                }
                firstLine = false;
            }
        }
        return ret.toString();
    }

    private static String readLicenseHeader() {
        StringBuilder ret = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                IconTasks.class.getClassLoader()
                .getResourceAsStream("org/netbeans/build/icons/license_xml_header.txt"))))
        {
            String line;
            while ((line = br.readLine()) != null) {
                ret.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ret.toString();
    }

    private static @Nullable Dimension readImageDimension(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null)
          throw new IOException("ImageIO.read returned null for " + file);
        int width = image.getWidth();
        int height = image.getHeight();
        return new Dimension(width, height);
    }

    private static ImmutableMap<Hash, Dimension> readImageDimensions(
            File nbsrcDir, ImmutableSetMultimap<Hash, IconPath> filesByHash)
            throws IOException
    {
        Map<Hash, Dimension> ret = Maps.newLinkedHashMap();
        for (Entry<Hash, IconPath> entry : filesByHash.entries()) {
            Hash hash = entry.getKey();
            IconPath ip = entry.getValue();
            if (ret.containsKey(hash)) {
                continue;
            }
            File file = new File(nbsrcDir, ip.toString());
            Dimension dim = readImageDimension(file);
            Util.putChecked(ret, hash, dim);
        }
        return ImmutableMap.copyOf(ret);
    }
}
