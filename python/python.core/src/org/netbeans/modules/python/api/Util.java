/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.python.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;

public final class Util {
    private static final String USE_PROXY_AUTHENTICATION = "useProxyAuthentication"; // NOI18N
    private static final String PROXY_AUTHENTICATION_USERNAME = "proxyAuthenticationUsername"; // NOI18N
    private static final String PROXY_AUTHENTICATION_PASSWORD = "proxyAuthenticationPassword"; // NOI18N
    public static String readAsString(final InputStream is) throws IOException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtil.copy(is, baos);
            return baos.toString("UTF-8"); // NOI18N
        } finally {
            is.close();
        }
    }
    public static void adjustProxy(final ProcessBuilder pb) {
        String proxy = Util.getNetBeansHttpProxy();
        if (proxy != null) {
            Map<String, String> env = pb.environment();
            if ((env.get("HTTP_PROXY") == null) && (env.get("http_proxy") == null)) { // NOI18N
                env.put("HTTP_PROXY", proxy); // NOI18N
                env.put("http_proxy", proxy); // NOI18N
            }
            // PENDING - what if proxy was null so the user has TURNED off
            // proxies while there is still an environment variable set - should
            // we honor their environment, or honor their NetBeans proxy
            // settings (e.g. unset HTTP_PROXY in the environment before
            // launching plugin?
        }
    }

    /**
     * FIXME: get rid of the whole method as soon as some NB Proxy API is
     * available.
     */
    private static String getNetBeansHttpProxy() {
        String host = System.getProperty("http.proxyHost"); // NOI18N

        if (host == null) {
            return null;
        }

        String portHttp = System.getProperty("http.proxyPort"); // NOI18N
        int port;

        try {
            port = Integer.parseInt(portHttp);
        } catch (NumberFormatException e) {
            port = 8080;
        }

        Preferences prefs = NbPreferences.root().node("org/netbeans/core"); // NOI18N
        boolean useAuth = prefs.getBoolean(USE_PROXY_AUTHENTICATION, false);
        String auth = "";
        if (useAuth) {
            auth = prefs.get(PROXY_AUTHENTICATION_USERNAME, "") + ":" + prefs.get(PROXY_AUTHENTICATION_PASSWORD, "") + '@'; // NOI18N
        }

        // Gem requires "http://" in front of the port name if it's not already there
        if (host.indexOf(':') == -1) {
            host = "http://" + auth + host; // NOI18N
        }

        return host + ":" + port; // NOI18N
    }

    private static final String FIRST_TIME_KEY = "platform-manager-called-first-time"; // NOI18N

    public static Preferences getPythonPreferences() {
        return NbPreferences.forModule(PythonPlatformManager.class);
    }

    public static boolean isFirstPlatformTouch() {
        return getPythonPreferences().getBoolean(FIRST_TIME_KEY, true);
    }

    public static void setFirstPlatformTouch(boolean b) {
        getPythonPreferences().putBoolean(FIRST_TIME_KEY, b);
    }

    // Tested in PythonUtilsTest
    public static List<FileObject> findUniqueRoots(List<FileObject> originalRoots) {
        List<FileObject> roots = new ArrayList<>(originalRoots);
        int n = roots.size();
        if (n > 1) {
            for (int i = 0; i < n; i++) {
                FileObject f1 = roots.get(i);
                if (f1 == null) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (i == j) {
                        continue;
                    }
                    FileObject f2 = roots.get(j);
                    if (f1 == f2 || f2 == null) {
                        continue;
                    }
                    if (FileUtil.isParentOf(f1, f2)) {
                        roots.set(j, null);
                    }
                }
            }
        }

        List<FileObject> uniqueRoots = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            FileObject fo = roots.get(i);
            if (fo != null) {
                uniqueRoots.add(fo);
            }
        }

        return uniqueRoots;
    }
}
