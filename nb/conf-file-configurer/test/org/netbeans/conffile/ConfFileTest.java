package org.netbeans.conffile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.netbeans.conffile.Main.NETBEANS_DEFAULT_OPTIONS;
import static org.netbeans.conffile.MemoryValue.MEGABYTE;
import static org.netbeans.conffile.MemoryValue.TERABYTE;

/**
 *
 * @author Tim Boudreau
 */
public class ConfFileTest {

    private static Path defaultConfig;
    private static Path modifiedConfig;

    private static final Set<String> DEFAULT_EXPECTED_KEYS
            = new HashSet<>(Arrays.asList("netbeans_default_userdir", "netbeans_default_cachedir",
                    "netbeans_default_options", "netbeans_jdkhome", "netbeans_extraclusters"));
    private static final Set<String> MODIFIED_EXPECTED_KEYS
            = new HashSet<>(Arrays.asList("netbeans_default_userdir", "netbeans_default_cachedir",
                    "netbeans_default_options", "netbeans_jdkhome"));
    private static final String DYN_TEST_FILE_SUFFIX = Long.toString(System.currentTimeMillis(), 36)
            + "-" + Integer.toString(ThreadLocalRandom.current().nextInt(), 36);
    private static final Set<Path> MAYBE_CREATED_FILES = new HashSet<>();
    private static int FILE_NAME_INC = 0;
    private static Path messyConfig;

    @Test
    public void testReadModifiedConfig() throws Exception {
        ConfFile cf = new ConfFile(modifiedConfig);
        Map<String, List<String>> data = cf.parse();
        assertEquals(MODIFIED_EXPECTED_KEYS, data.keySet());
        for (Map.Entry<String, List<String>> e : data.entrySet()) {
            List<String> expected;
            switch (e.getKey()) {
                case "netbeans_default_userdir":
                    expected = modified_netbeans_default_userdir;
                    break;
                case "netbeans_default_cachedir":
                    expected = modified_netbeans_default_cachedir;
                    break;
                case "netbeans_default_options":
                    expected = modified_netbeans_default_options;
                    break;
                case "netbeans_jdkhome":
                    expected = modified_netbeans_jdkhome;
                    break;
                default:
                    fail("Unexpected key in modified config: " + e.getKey() + " with " + e.getValue());
                    return;
            }
            assertEquals(expected, e.getValue(), "Wrong value for " + e.getKey());
        }
        MemoryValue xmx = ConfFile.findMemorySetting(MemoryValue.Kind.MAXIMUM_HEAP_SIZE, data);
        MemoryValue xms = ConfFile.findMemorySetting(MemoryValue.Kind.INITIAL_HEAP_SIZE, data);
        MemoryValue xss = ConfFile.findMemorySetting(MemoryValue.Kind.STACK_SIZE, data);

        assertNotNull(xmx, "-J-Xmx not found");
        assertNotNull(xms, "-J-Xms not found");
        assertNotNull(xss, "-J-Xss not found");

        assertEquals(1024 * MEGABYTE, xms.asBytes(), "Wrong value for xms");
        assertEquals(3072 * MEGABYTE, xmx.asBytes(), "Wrong value for xmx");
        assertEquals(2 * MEGABYTE, xss.asBytes(), "Wrong value for xss");

        String fontsize = ConfFile.findArgumentTo("fontsize", data);
        assertNotNull(fontsize, "Fontsize not found");
        assertEquals("16", fontsize, "Wrong value for font size");

        Set<JVMMemorySetting> js = ConfFile.allMemorySettings(data);
        assertNotNull(js);
        assertFalse(js.isEmpty());
        assertEquals(3, js.size());
        for (JVMMemorySetting s : js) {
            assertNotNull(s);
            switch (s.kind()) {
                case INITIAL_HEAP_SIZE:
                    assertEquals(xms, s.value());
                    break;
                case MAXIMUM_HEAP_SIZE:
                    assertEquals(xmx, s.value());
                    break;
                case STACK_SIZE:
                    assertEquals(xss, s.value());
                    break;
                case UNKNOWN:
                    fail("Should not have parsed any of " + js + " to unknown");
                default:
                    throw new AssertionError(s.kind());
            }
        }
        assertEquals("Tim Boudreau", ConfFile.findSystemPropertySetting("user.name", data), "Wrong user name");
        assertEquals("Arimo", ConfFile.findSystemPropertySetting("uiFontName", data), "Wrong uiFontName");
    }

    @Test
    public void testReplacing() throws Exception {
        ConfFile cf = new ConfFile(modifiedConfig);
        Map<String, List<String>> data = cf.parse();

        Set<JVMMemorySetting> oldMem = ConfFile.allMemorySettings(data);

        DefaultOptionsReplacementChecker checker = new DefaultOptionsReplacementChecker();
        List<String> all = data.get(NETBEANS_DEFAULT_OPTIONS);
        LineSwitchWriter w = new LineSwitchWriter(all, checker);

        String oldFontSize = ConfFile.findArgumentTo("fontsize", data);
        assertNotNull(oldFontSize);

        w.appendOrReplaceArguments("--fontsize", "200");

        w.appendOrReplaceArguments("-J-Dfile.encoding=UTF-16");

        w.appendOrReplaceArguments("-J-Xmx1k");
        w.appendOrReplaceArguments("-J-Xms100");
        w.appendOrReplaceArguments("-J-Xss2T");
        w.appendOrReplaceArguments("-J-XX:-UseNUMA");
        data.put(NETBEANS_DEFAULT_OPTIONS, w.switches());

        Path repl = create("repl");
        cf.rewrite(repl, data);

        ConfFile cfb = new ConfFile(repl);
        Map<String, List<String>> nue = cfb.parse();

        String newFontSize = ConfFile.findArgumentTo("fontsize", nue);
        assertNotNull(newFontSize);
        assertEquals("200", newFontSize);

        Set<JVMMemorySetting> newMem = ConfFile.allMemorySettings(nue);
        assertNotEquals(oldMem, newMem);

        assertEquals(new MemoryValue("ignored", 1024, "-Xmx1k"), ConfFile.findMemorySetting(MemoryValue.Kind.MAXIMUM_HEAP_SIZE, nue));
        assertEquals(new MemoryValue("ignored", 100, "-Xms100"), ConfFile.findMemorySetting(MemoryValue.Kind.INITIAL_HEAP_SIZE, nue));
        assertEquals(new MemoryValue("ignored", 2 * TERABYTE, "-Xss2T"),
                ConfFile.findMemorySetting(MemoryValue.Kind.STACK_SIZE, nue));

        List<String> nudata = nue.get(NETBEANS_DEFAULT_OPTIONS);
        assertTrue(nudata.contains("-J-XX:-UseNUMA"));
        assertFalse(nudata.contains("-J-XX:+UseNUMA"));

        assertEquals(nudata.size(), new HashSet(nudata).size(), "Should be no duplicate entries");
    }

    @Test
    public void testRewriteDefaultConfig() throws Exception {
        ConfFile cf = new ConfFile(defaultConfig);
        Map<String, List<String>> data = cf.parse();
        List<String> ud = data.get("netbeans_default_userdir");
        assertNotNull(ud);
        ud.add("whoopty");
        ud.add("doo");

        String rew = cf.rewritten(data);
        assertTrue(rew.contains("whoopty"));
        assertTrue(rew.contains("doo"));
        Set<String> commentLines = cf.allComments();
        for (String oneComment : commentLines) {
            assertTrue(rew.contains(oneComment), "Missing comment '"
                    + oneComment + "' in rewritten text:\n-----------------------------\n"
                    + rew + "\n---------------------------------\n");
        }

        ConfFile nue = new ConfFile(createAndWrite(rew));
        List<String> got = nue.parse().get("netbeans_default_userdir");
        assertNotNull(got);
        assertTrue(got.contains("whoopty"));
        assertTrue(got.contains("doo"));
        assertTrue(got.contains("${DEFAULT_USERDIR_ROOT}/dev"));
    }

    @Test
    public void testReadDefaultConfig() throws Exception {
        ConfFile cf = new ConfFile(defaultConfig);
        Map<String, List<String>> data = cf.parse();
        assertEquals(DEFAULT_EXPECTED_KEYS, data.keySet());
        for (Map.Entry<String, List<String>> e : data.entrySet()) {
            List<String> expected;
            switch (e.getKey()) {
                case "netbeans_default_userdir":
                    expected = default_netbeans_default_userdir;
                    break;
                case "netbeans_default_cachedir":
                    expected = default_netbeans_default_cachedir;
                    break;
                case NETBEANS_DEFAULT_OPTIONS:
                    expected = default_netbeans_default_options;
                    break;
                case "netbeans_jdkhome":
                    expected = default_netbeans_jdkhome;
                    break;
                case "netbeans_extraclusters":
                    expected = default_netbeans_extraclusters;
                    break;
                default:
                    fail("Unexpected key in default config: " + e.getKey() + " with " + e.getValue());
                    return;
            }
            assertEquals(expected, e.getValue(), "Wrong value for " + e.getKey());
        }
    }

    @Test
    public void testAlterAndRewriteDefaultConfig() throws Exception {
        ConfFile cf = new ConfFile(defaultConfig);
        Map<String, List<String>> data = cf.parse();
        assertEquals(DEFAULT_EXPECTED_KEYS, data.keySet());

        List<String> ud = data.get("netbeans_default_userdir");
        assertNotNull(ud);
        ud.add("whoopty");
        ud.add("doo");

        List<String> opts = data.get(NETBEANS_DEFAULT_OPTIONS);
        assertNotNull(opts);
        assertTrue(opts.remove("-J-Djdk.gtk.version=2.2"));
        assertTrue(opts.remove("-J-Dplugin.manager.check.updates=false"));
        opts.add(0, "--some-stuff-here");
        opts.add(1, "--some-more-stuff-here");

        List<String> home = data.get("netbeans_jdkhome");
        assertNotNull(home);
        home.clear();

        data.put("wookies", Arrays.asList("Chewbacca", "ChewbaccaGirl"));

        data.put("emptiness", Collections.emptyList());

        assertNotNull(data.remove("netbeans_extraclusters"));

        String rew = cf.rewritten(data);

        Path rewrite = createAndWrite(rew);
        ConfFile nue = new ConfFile(rewrite);
        Map<String, List<String>> rewrittenAndRead = nue.parse();

        Set<String> expectedKeys = new HashSet<>(DEFAULT_EXPECTED_KEYS);
        expectedKeys.add("wookies");
        expectedKeys.add("emptiness");
        expectedKeys.remove("netbeans_extraclusters");

        assertEquals(expectedKeys, rewrittenAndRead.keySet());
        assertEquals(data, rewrittenAndRead);
    }

    @Test
    public void testDuplicatesAndSloppiness() throws Exception {
        ConfFile cf = new ConfFile(messyConfig);
        Map<String, List<String>> data = cf.parse();
        List<String> opts = data.get(NETBEANS_DEFAULT_OPTIONS);
        assertNotNull(opts);
        assertFalse(opts.isEmpty());
        assertTrue(new HashSet<>(opts).size() < opts.size());

    }

    private static Path create(String prefix) {
        Path tmp = Paths.get(System.getProperty("java.io.tmpdir"));
        Path result = tmp.resolve(prefix + "-" + DYN_TEST_FILE_SUFFIX + "-" + ++FILE_NAME_INC + ".conf");
        MAYBE_CREATED_FILES.add(result);
        return result;
    }

    private static Path createAndWrite(String body) throws IOException {
        Path nue = create("netbeans-gen");
        Files.write(nue, body.getBytes(UTF_8), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        return nue;
    }

    @BeforeAll
    public static void copyConfFiles() throws IOException {
        defaultConfig = create("netbeans-default");
        assertNotNull(defaultConfig);
        readAndWrite("netbeans-default.conf", defaultConfig);
        modifiedConfig = create("netbeans-modified");
        assertNotNull(modifiedConfig);
        readAndWrite("netbeans.conf", modifiedConfig);
        messyConfig = create("netbeans-messy.conf");
        readAndWrite("netbeans-messy.conf", messyConfig);
    }

    @AfterAll
    public static void deleteConfFiles() throws IOException {
        for (Path p : MAYBE_CREATED_FILES) {
            if (Files.exists(p)) {
                Files.delete(p);
            }
        }
    }

    private static void readAndWrite(String resource, Path dest) throws IOException {
        try (InputStream in = ConfFileTest.class.getResourceAsStream(resource)) {
            try (OutputStream out = Files.newOutputStream(dest, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
//                in.transferTo(out); // jdk9
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }

    private static final List<String> default_netbeans_default_userdir = Arrays.asList(new String[]{
        "${DEFAULT_USERDIR_ROOT}/dev"
    });

    private static final List<String> default_netbeans_default_cachedir = Arrays.asList(new String[]{
        "${DEFAULT_CACHEDIR_ROOT}/dev"
    });

    private static final List<String> default_netbeans_default_options = Arrays.asList(new String[]{
        "-J-XX:+UseStringDeduplication",
        "-J-Xss2m",
        "-J-Dnetbeans.logger.console=true",
        "-J-ea",
        "-J-Djdk.gtk.version=2.2",
        "-J-Dapple.laf.useScreenMenuBar=true",
        "-J-Dapple.awt.graphics.UseQuartz=true",
        "-J-Dsun.java2d.noddraw=true",
        "-J-Dsun.java2d.dpiaware=true",
        "-J-Dsun.zip.disableMemoryMapping=true",
        "-J-Dplugin.manager.check.updates=false",
        "-J-Dnetbeans.extbrowser.manual_chrome_plugin_install=yes",
        "-J--add-opens=java.base/java.net=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang.ref=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-J--add-opens=java.base/java.security=ALL-UNNAMED",
        "-J--add-opens=java.base/java.util=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "-J--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED",
        "-J--add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED",
        "-J--add-modules=jdk.jshell",
        "-J--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "-J--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED",
        "-J--add-exports=java.desktop/com.sun.beans.editors=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.swing=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.awt.im=ALL-UNNAMED",
        "-J--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
        "-J--add-exports=java.management/sun.management=ALL-UNNAMED",
        "-J--add-exports=java.base/sun.reflect.annotation=ALL-UNNAMED",
        "-J--add-exports=jdk.javadoc/com.sun.tools.javadoc.main=ALL-UNNAMED",
        "-J-XX:+IgnoreUnrecognizedVMOptions"
    });

    private static final List<String> default_netbeans_jdkhome = Arrays.asList(new String[]{
        "/path/to/jdk"
    });

    private static final List<String> default_netbeans_extraclusters = Arrays.asList(new String[]{
        "/absolute/path/to/cluster1:/absolute/path/to/cluster2"
    });

    private static final List<String> modified_netbeans_default_userdir = Arrays.asList(new String[]{
        "${DEFAULT_USERDIR_ROOT}/dev"
    });

    private static final List<String> modified_netbeans_default_cachedir = Arrays.asList(new String[]{
        "${DEFAULT_CACHEDIR_ROOT}/dev"
    });

    private static final List<String> modified_netbeans_default_options = Arrays.asList(new String[]{
        "--fontsize",
        "16",
        "-J-ea",
        "-J-Xss2m",
        "-J-Xms1024M",
        "-J-Xmx3072M",
        "-J-XX:+UseNUMA",
        "-J-XX:+UseFastAccessorMethods",
        "-J-XX:+UseFastEmptyMethods",
        "-J-XX:+UseNUMAInterleaving",
        "-J-XX:+UseTransparentHugePages",
        "-J-XX:+UseLargePages",
        "-J-XX:+UseLargePagesInMetaspace",
        "-J-XX:+UseLargePagesIndividualAllocation",
        "-J-XX:+UseG1GC",
        "-J-XX:MaxGCPauseMillis=250",
        "-J-XX:+UseStringDeduplication",
        "-J-XX:ReservedCodeCacheSize=96M",
        "-J-DuiFontName=Arimo",
        "-J-Dhidpi=true",
        "-J-Dsun.java2d.dpiaware=true",
        "-J-Dnb.cellrenderer.antialiasing=false",
        "-J-Dswing.aatext=true",
        "-J-Dorg.netbeans.editor.aa.extra.hints=false",
        "-J-Dawt.useSystemAAFontSettings=lcd",
        "-J-Dnetbeans.logger.console=true",
        "-J-Dapple.laf.useScreenMenuBar=true",
        "-J-Dapple.awt.graphics.UseQuartz=true",
        "-J-Dsun.java2d.noddraw=true",
        "-J-Dplugin.manager.check.updates=false",
        "-J-Dnetbeans.extbrowser.manual_chrome_plugin_install=yes",
        "-J-Djdk.gtk.version=3",
        "-J-Dnetbeans.winsys.statusLine.in.menuBar=true",
        "-J-Duser.name='Tim Boudreau'",
        "-J-Dnetbeans.security.nocheck=true",
        "-J-Dfile.encoding=UTF-8",
        "-J-DCachingArchiveProvider.disableCtSym=true",
        "-J--add-opens=java.base/java.net=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang.ref=ALL-UNNAMED",
        "-J--add-opens=java.base/java.lang=ALL-UNNAMED",
        "-J--add-opens=java.base/java.security=ALL-UNNAMED",
        "-J--add-opens=java.base/java.util=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing.text=ALL-UNNAMED",
        "-J--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt=ALL-UNNAMED",
        "-J--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
        "-J--add-opens=java.prefs/java.util.prefs=ALL-UNNAMED",
        "-J--add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED",
        "-J--add-modules=jdk.jshell",
        "-J--add-exports=java.desktop/sun.awt=ALL-UNNAMED",
        "-J--add-exports=java.desktop/java.awt.peer=ALL-UNNAMED",
        "-J--add-exports=java.desktop/com.sun.beans.editors=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.swing=ALL-UNNAMED",
        "-J--add-exports=java.desktop/sun.awt.im=ALL-UNNAMED",
        "-J--add-exports=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
        "-J--add-exports=java.management/sun.management=ALL-UNNAMED",
        "-J--add-exports=java.base/sun.reflect.annotation=ALL-UNNAMED",
        "-J--add-exports=jdk.javadoc/com.sun.tools.javadoc.main=ALL-UNNAMED",
        "-J-XX:+IgnoreUnrecognizedVMOptions"
    });

    private static final List<String> modified_netbeans_jdkhome = Arrays.asList(new String[]{
        "/opt/jdk11"
    });
}
