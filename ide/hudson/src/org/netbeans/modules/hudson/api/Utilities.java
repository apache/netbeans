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

package org.netbeans.modules.hudson.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.hudson.Installer;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.FAILURE;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.SUCCESS;
import static org.netbeans.modules.hudson.api.HudsonJobBuild.Result.UNSTABLE;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.UIExtension;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.w3c.dom.Element;

/**
 * Helper utility class
 */
public class Utilities {

    private Utilities() {}
    
    public static boolean isSupportedVersion(HudsonVersion version) {
        // Check for null
        if (null == version)
            return false;
        
        // Version check
        if (version.compareTo(HudsonVersion.SUPPORTED_VERSION) < 0)
            return false;
        
        return true;
    }

    /**
     * Encode a path segment or longer path of a URI (such as a job name) suitably for a URL.
     * @param path a path segment, or several such segments separated by slashes, with possible initial and/or trailing slashes
     * @return the same with spaces and unsafe characters escaped
     * @throws IllegalArgumentException if there is some other URI problem
     */
    public static String uriEncode(String path) {
        Parameters.notNull("segment", path);
        try {
            return new URI(null, path, null).toASCIIString();
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(x);
        }
    }

    /**
     * Mostly inverse of {@link #uriEncode}, but only decodes a single path segment.
     */
    public static String uriDecode(String string) {
        String d = URI.create(string).getPath();
        if (d.contains("/")) { // NOI18N
            throw new IllegalArgumentException(d);
        }
        return d;
    }

        /**
         * Evaluate an XPath expression.
         * @param expr an XPath expression
         * @param xml a DOM context
         * @return the string value, or null
         */
        public static synchronized String xpath(String expr, Element xml) {
            try {
                return xpath.evaluate(expr, xml);
            } catch (XPathExpressionException x) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, "cannot evaluate '" + expr + "'", x);
                return null;
            }
        }
        private static final XPath xpath = XPathFactory.newInstance().newXPath();

    /**
     * Quickly check whether Hudson support is in use.
     *
     * @return True if Hudson support is in use, false otherwise.
     */
    public static boolean isHudsonSupportActive() {
        return Installer.active();
    }

    /**
     * Make an instance persistent.
     *
     * @param instance Hudson instance to persist.
     */
    public static void persistInstance(HudsonInstance instance) {
        if (instance instanceof HudsonInstanceImpl) {
            ((HudsonInstanceImpl) instance).makePersistent();
        }
    }

    /**
     * Get appropriate {@link HudsonJob.Color} instance for a build.
     *
     * @param build Hudson Job Build.
     * @return Appropriate color, depending on the result of the build.
     */
    public static HudsonJob.Color getColorForBuild(HudsonJobBuild build) {
        switch (build.getResult()) {
            case SUCCESS:
                return HudsonJob.Color.blue;
            case UNSTABLE:
                return HudsonJob.Color.yellow;
            case FAILURE:
                return HudsonJob.Color.red;
            default:
                return HudsonJob.Color.grey;
        }
    }

    /**
     * Return icon for a Hudson job.
     *
     * @param job Job to get icon for.
     * @return Icon representing current state of the job.
     */
    public static Icon getIcon(HudsonJob job) {
        return makeIcon(job.getColor().iconBase());
    }

    /**
     * Return icon for a Hudson Job build.
     *
     * @param build Build to get icon for.
     * @return Icon representing current state of the build.
     */
    public static Icon getIcon(HudsonJobBuild build) {
        return makeIcon(Utilities.getColorForBuild(build).iconBase());
    }

    /**
     * Make an icon for the provided iconBase.
     */
    private static Icon makeIcon(String iconBase) {
        return ImageUtilities.image2Icon(
                ImageUtilities.loadImageIcon(iconBase, false).getImage());
    }

    /**
     * Show a build in the UI.
     *
     * A UI extendsion is needed to perform this method. If no extension is
     * registered, this method does nothing. See {@link UIExtension}.
     *
     * @param build Hudson Build to show.
     */
    public static void showInUI(HudsonJobBuild build) {
        UIExtension ext = findUIExtension();
        if (ext != null) {
            ext.showInUI(build);
        }
    }

    /**
     * Show a job in the UI.
     *
     * A UI extendsion is needed to perform this method. If no extension is
     * registered, this method does nothing. See {@link UIExtension}.
     *
     * @param job Hudson Job to show.
     */
    public static void showInUI(HudsonJob job) {
        UIExtension ext = findUIExtension();
        if (ext != null) {
            ext.showInUI(job);
        }
    }

    /**
     * Find UI extension.
     *
     * @return The UI extension if registered, null otherwise.
     */
    private static UIExtension findUIExtension() {
        return Lookup.getDefault().lookup(UIExtension.class);
    }

    /**
     * Check whether a URL represents a valid Hudson root.
     *
     * @param url The URL, which should end with a slash (/) character.
     * @return {@link HudsonURLCheckResult#OK} if the URL is valid, or some
     * other value if the URL cannot be used for a hudson instance.
     * @since hudson/2.4
     */
    public static HudsonURLCheckResult checkHudsonURL(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection connection = new ConnectionBuilder().homeURL(u).url(
                    new URL(u, "?checking=redirects")).httpConnection(); // NOI18N
            String sVersion = connection.getHeaderField("X-Hudson"); // NOI18N
            connection.disconnect();
            if (sVersion == null) {
                return HudsonURLCheckResult.WRONG_VERSION;
            }
            HudsonVersion version = new HudsonVersion(sVersion);
            if (!Utilities.isSupportedVersion(version)) {
                return HudsonURLCheckResult.WRONG_VERSION;
            }
            if (!"checking=redirects".equals(connection.getURL().getQuery())) { // NOI18N
                return HudsonURLCheckResult.INCORRECT_REDIRECTS;
            }
            return HudsonURLCheckResult.OK;
        } catch (IOException x) {
            Logger.getLogger(Utilities.class.getName()).log(Level.INFO, null, x);
            return HudsonURLCheckResult.OTHER_ERROR;
        }
    }

    /**
     * Enumeration of values that can be returned from
     * {@link #checkHudsonURL(String)}. Value {@link #OK} means that the URL
     * represents a valid Hudson instance, other values indicates possible
     * problems. New values can be added in the future.
     *
     * @since hudson/2.4
     */
    public enum HudsonURLCheckResult {

        OK, WRONG_VERSION, INCORRECT_REDIRECTS, OTHER_ERROR;

        public boolean isOK() {
            return this == OK;
        }
    }
}
