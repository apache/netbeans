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
package org.netbeans.conffile;

import java.io.IOException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum of operating systems.
 *
 * @author Tim Boudreau
 */
public enum OS {

    WINDOWS,
    MAC_OS,
    SOLARIS,
    LINUX,
    UNKNOWN;

    /**
     * Remove command line switches which are only relevant on a different OS
     * than the one we are running on.
     *
     * @param a collection of line switches
     */
    public void removeIrrelevant(Collection<String> l) {
        l.removeAll(irrelevantSwitches());
    }

    public Set<String> irrelevantSwitches() {
        Set<String> result = new HashSet<>();
        if (this != WINDOWS) {
            result.add("-J-Dsun.java2d.noddraw=true");
            result.add("-J-Dsun.zip.disableMemoryMapping=true");
        }
        if (this != MAC_OS) {
            result.add("-J-Dapple.laf.useScreenMenuBar=true");
            result.add("-J-Dapple.awt.graphics.UseQuartz=true");
        }
        return result;
    }

    /**
     * Get the current OS.
     *
     * @return An OS
     */
    public static OS get() {
        if (System.getProperty("mrj.version") != null) {
            return MAC_OS;
        }
        String nm = System.getProperty("os.name");
        if (nm == null) { // ?
            return UNKNOWN;
        }
        nm = nm.toLowerCase();
        if (nm.contains("linux")) {
            return LINUX;
        } else if (nm.contains("win")) {
            return WINDOWS;
        } else if (nm.contains("sunos")) {
            return SOLARIS;
        } else if (nm.contains("mac")) {
            return MAC_OS;
        }
        return UNKNOWN;
    }

    public long getMemorySize() {
        switch (this) {
            case WINDOWS:
                // XXX how?

                break;
            case MAC_OS:
                String hwMemSize = runProgramAndFindLine("/usr/sbin/sysctl", "hw.memsize:", "hw.memsize");
                if (hwMemSize != null) {
                    Matcher mmm = MEMINFO_MAC.matcher(hwMemSize);
                    if (mmm.find()) {
                        return Long.parseLong(mmm.group(1).trim());
                    }
                }
                return 0;
            case SOLARIS:
                String memSize = runProgramAndFindLine("/usr/sbin/prtconf", "Memory size:");
                if (memSize == null) {
                    return 0;
                }
                Matcher mm = MEMSIZE_SOL.matcher(memSize);
                if (mm.find()) {
                    long amt = Long.parseLong(mm.group(1).trim());
                    String type = mm.group(2).trim();
                    switch (type) {
                        case "Megabytes":
                            amt *= 1024L * 1024L;
                            return amt;
                    }
                }
                break;
            case LINUX:
                String memTotal = readFileAndFindLine("/proc/meminfo", "MemTotal:");
                if (memTotal == null) {
                    return 0;
                }
                Matcher m = MEMINFO_LINUX.matcher(memTotal);
                if (m.find()) {
                    long amt = Long.parseLong(m.group(1).trim());
                    String unit = m.group(2).trim();
                    if ("kB".equals(unit)) {
                        return amt * 1024L;
                    }
                    return amt;
                }
        }
        return 0;
    }

    private static final Pattern MEMINFO_MAC = Pattern.compile("^\\s*?(\\d+)$");
    private static final Pattern MEMINFO_LINUX = Pattern.compile("^\\s*?(\\d+)\\s+(\\S*?)$");
    private static final Pattern MEMSIZE_SOL = Pattern.compile("^\\s*?(\\d+)\\s+(\\S*?)$");

    private String readFileAndFindLine(String file, String prefix) {
        prefix = prefix.trim();
        try {
            // MemTotal:        8040156 kB
            for (String line : Files.readAllLines(Paths.get(file), US_ASCII)) {
                line = line.trim();
                if (line.startsWith(prefix)) {
                    return line.substring(prefix.length()).trim();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, "reading " + file, ex);
            return null;
        }
        return null;
    }

    private String runProgramAndFindLine(String prog, String prefix, String... args) {
        Path path = Paths.get(prog);
        if (!Files.exists(path) || !Files.isExecutable(path)) {
            return null;
        }
        List<String> all = new ArrayList<>(Arrays.asList(args));
        all.add(0, prog);
        ProcessBuilder pb = new ProcessBuilder(all);

        Path tmp = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tmpOutput = tmp.resolve(OS.class.getName() + "-" + System.currentTimeMillis()
                + "-" + System.nanoTime() + "-" + ThreadLocalRandom.current().nextInt(500) + ".out");
        pb.redirectOutput(tmpOutput.toFile());
        try {
            Process proc = pb.start();
            while (proc.isAlive()) { // JDK 8 compatibility - cannot use process.onExit
                Thread.sleep(100);
            }
            String result = readFileAndFindLine(tmpOutput.toString(), prefix);
            if (Files.exists(tmpOutput)) {
                Files.delete(tmpOutput);
            }
            return result;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.FINE, null, ex);
        }
        return null;
    }

    public enum ChassisType {
        NOTEBOOK,
        DESKTOP,
        UNKNOWN
    }

    private static String replacePunctuation(String s) {
        int max = s.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < max; i++) {
            char c = s.charAt(i);
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                sb.append(c);
            } else {
                sb.append('-');
            }
        }
        return sb.toString();
    }

    public void appendSignature(StringBuilder sb) {
        sb.append(name().toLowerCase()).append('-');
        switch (this) {
            case LINUX:
                sb.append(getMemorySize()).append('-');
                sb.append(systemType().name().toLowerCase());
                Path dmii = Paths.get("/sys/class/dmi/id/modalias");
                if (Files.exists(dmii)) {
                    try {
                        String s = new String(Files.readAllBytes(dmii), US_ASCII);
                        sb.append(s.toLowerCase());
                        return;
                    } catch (IOException ex) {
                        Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                sb.append("unknown-dmi");
                break;
            case MAC_OS:

        }
    }

    /**
     * Used to determine if this is likely to be a laptop, which is more likely
     * to have multiple displays.
     *
     * @return A chassis type
     */
    public ChassisType systemType() {
        switch (this) {
            case LINUX:
                Path path = Paths.get("/sys/class/dmi/id/chassis_type");
                if (Files.exists(path)) {
                    try {
                        String s = new String(Files.readAllBytes(path), US_ASCII);
                        int type = Integer.parseInt(s);
                        switch (type) {
                            case 8: // portable
                            case 9: // laptop
                            case 10: // notebook
                            case 11: // hand held
                            case 12: // docking station
                            case 14: // sub notebook
                                return ChassisType.NOTEBOOK;
                            case 3: // desktop
                            case 4: // low profile desktop
                            case 5: // pizza box
                            case 6: // mini tower
                            case 7: // tower
                            case 15: // space saving
                            case 16: // lunch box
                            case 17: // main system chassis
                            case 23: // rack mount chassis
                            case 24: // sealed case pc
                                return ChassisType.DESKTOP;
                            case 18: // expansion chassis
                            case 19: // sub chassis
                            case 20: // bus expansion chassis
                            case 21: // peripheral chassis
                            default:
                                return ChassisType.UNKNOWN;
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(OS.class.getName()).log(Level.FINE, null, ex);
                    }
                }
                path = Paths.get("/sys/class/power_supply");
                if (Files.exists(path) && Files.isDirectory(path)) {
                    try {
                        // Could be pendantic and check e.g.
                        // /sys/class/power_supply/BAT0/type = Battery, not mains
                        if (Files.list(path).count() > 1) {
                            return ChassisType.NOTEBOOK;
                        } else {
                            return ChassisType.UNKNOWN;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
                        return ChassisType.UNKNOWN;
                    }
                }
            // dmidecode --string chassis-type
            // would also work, but requires superuser privileges and
            // may not be installed
            case MAC_OS:
                String line = runProgramAndFindLine("/usr/sbin/system_profiler",
                        "      Model Identifier:",
                        "SPHardwareDataType");
                if (line != null) {
                    if (line.contains("MacBook")) {
                        return ChassisType.NOTEBOOK;
                    } else {
                        return ChassisType.DESKTOP;
                    }
                }
            case WINDOWS:
            // ? something using wmic?
            default:
                return ChassisType.UNKNOWN;
        }
    }
}
