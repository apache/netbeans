/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.spi;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.util.NbBundle;

/**
 * Class representing a remote configuration (e.g. FTP, SFTP).
 * <p>
 * This class is thread-safe, all subclasses should stay thread-safe as well.
 * @author Tomas Mysik
 * @see org.netbeans.modules.php.project.connections.RemoteConnections
 * @see org.netbeans.modules.php.project.connections.RemoteConnections#getRemoteConfigurations()
 */
public abstract class RemoteConfiguration {

    protected static final Logger LOGGER = Logger.getLogger(RemoteConfiguration.class.getName());

    protected final ConfigManager.Configuration cfg;

    private final String displayName;
    private final String name;

    // password keys, for keyring
    final String deprecatedPasswordKey;
    final String passwordKey;

    /**
     * Create new remote configuration based on the given {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration}.
     * @param cfg {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration} with configuration data.
     */
    public RemoteConfiguration(final ConfigManager.Configuration cfg) {
        if (cfg == null) {
            throw new NullPointerException("Configuration cannot be null");
        }

        this.cfg = cfg;
        name = cfg.getName();
        displayName = cfg.getDisplayName();
        deprecatedPasswordKey = getClass().getName() + "." + name + ".password"; // NOI18N
        passwordKey = "php.remote." + name + ".password"; // NOI18N
    }

    /**
     * Constructor for "dummy" remote configurations, see {@link Empty empty configuration}.
     */
    RemoteConfiguration(String name, String displayName) {
        assert name != null;
        assert displayName != null;

        this.name = name;
        this.displayName = displayName;

        this.cfg = null;
        deprecatedPasswordKey = null;
        passwordKey = null;
    }

    /**
     * Get the display name of this configuration.
     * @return the display name of this configuration.
     * @see #getName()
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the name of this configuration.
     * @return the name of this configuration.
     * @see #getDisplayName()
     */
    public String getName() {
        return name;
    }

    /**
     * Get the URL for the remote configuration, suitable e.g. for some hints etc.
     * @return configuration URL.
     */
    public String getUrl() {
        return getUrl(""); // NOI18N
    }

    /**
     * Get the URL for the remote configuration, suitable e.g. for some hints etc.
     * @param directory directory for which the URL is created (directory is usually appended to the URL).
     * @return configuration URL.
     * @see #getUrl()
     */
    public abstract String getUrl(String directory);

    /**
     * Get the initial remote directory on a server for this configuration.
     * It would be typically '/' for FTP.
     * @return the initial directory, never <code>null</code>.
     */
    public abstract String getInitialDirectory();

    /**
     * This method is called when this remote configuration is to be saved.
     * <p>
     * It should return <code>true</code> if the key/value is going to be saved somehow specially
     * (typical for e.g. password), <code>false</code> otherwise.
     * <p>
     * The default value is <code>false</code> (no special handling).
     * @param key the key from the input {@link org.netbeans.modules.php.project.connections.ConfigManager.Configuration configuration data}
     * @param value the value of the key
     * @return <code>true</code> if the key/value is going to be saved somehow specially, <code>false</code> otherwise
     * @see #notifyDeleted()
     */
    public boolean saveProperty(String key, String value) {
        return false;
    }

    /**
     * This method is called after this configuration is deleted.
     * <p>
     * Usually, in this method any cleanup can be done, typically remove {@link #saveProperty(String, String) customly saved properties} etc.
     * <p>
     * <b>WARNING:</b> the only known property is {@link #name}, all other properties are <code>null</code>.
     * @see #saveProperty(String, String)
     */
    public void notifyDeleted() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteConfiguration other = (RemoteConfiguration) obj;
        if (name != other.name && (name == null || !name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [displayName: "); // NOI18N
        sb.append(displayName);
        sb.append(", name: "); // NOI18N
        sb.append(name);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    /**
     * Dummy implementation of {@link RemoteConfiguration}.
     */
    public static final class Empty extends RemoteConfiguration {
        public Empty(String name, String displayName) {
            super(name, displayName);
        }

        @Override
        public String getUrl(String directory) {
            return ""; // NOI18N
        }

        @Override
        public String getInitialDirectory() {
            return "/";
        }
    }

    /**
     * Read value of the given key as a number. If number is not found in the given configuration
     * (or value is not a number), the default value is returned (and set in the configuration).
     * @param key key of the number
     * @param defaultValue default value if any error occurs
     * @return value of the given key as a number or defaultValue if any error occurs
     */
    protected int readNumber(String key, int defaultValue) {
        String currentValue = cfg.getValue(key);
        if (StringUtils.hasText(currentValue)) {
            try {
                return Integer.parseInt(currentValue);
            } catch (NumberFormatException nfe) {
                LOGGER.log(Level.INFO, "Exception while parsing number of '" + key + "'", nfe);
            }
        }
        cfg.putValue(key, String.valueOf(defaultValue));
        return defaultValue;
    }

    /**
     * Read value of the given key as a boolean. If boolean is not found in the given configuration
     * (or value is not a boolean), the default value is returned (and set in the configuration).
     * @param key key of the boolean
     * @param defaultValue default value if any error occurs
     * @return value of the given key as a boolean or defaultValue if any error occurs
     */
    protected boolean readBoolean(String key, boolean defaultValue) {
        String value = cfg.getValue(key);
        if (value != null) {
            return Boolean.valueOf(value);
        }
        cfg.putValue(key, String.valueOf(defaultValue));
        return defaultValue;
    }

    /**
     * {@link PhpProjectUtils#resolveEnum(Class, String, Enum) Resolve enum} and remember
     * it in the configuration.
     * @param <T> enum type
     * @param enumClass enum class
     * @param key key of the enum
     * @param defaultValue default value if any error occurs
     * @return value of the given key as an enum or defaultValue if any error occurs
     * @see PhpProjectUtils#resolveEnum(Class, String, Enum)
     */
    protected <T extends Enum<T>> T readEnum(Class<T> enumClass, String key, T defaultValue) {
        T value = PhpProjectUtils.resolveEnum(enumClass, cfg.getValue(key), defaultValue);
        cfg.putValue(key, value.name());
        return value;
    }

    protected String readPassword(String key) {
        if (cfg == null) {
            throw new IllegalStateException("Cannot read password, no configuration provided");
        }
        String oldPassword = cfg.getValue(key, true);
        if (oldPassword != null) {
            return oldPassword;
        }
        String password = readPasswordFromKeyring();
        if (password != null) {
            // password must be present in the configuration (option panel reads it)
            cfg.putValue(key, password, true);
        }
        return password;
    }

    private String readPasswordFromKeyring() {
        // new password key
        char[] newPassword = Keyring.read(passwordKey);
        if (newPassword != null) {
            return new String(newPassword);
        }
        // deprecated password key
        newPassword = Keyring.read(deprecatedPasswordKey);
        if (newPassword != null) {
            return new String(newPassword);
        }
        return null;
    }

    protected void savePassword(final String password, final String type) {
        if (password == null) {
            // password not set at all, do nothing
            //  (e.g. when user opens Remote Configurations and clicks OK but not all configs were displayed [= passwords were not fetched from keyring])
            return;
        }
        if (StringUtils.hasText(password)) {
            Keyring.save(passwordKey, password.toCharArray(),
                    NbBundle.getMessage(RemoteConfiguration.class, "MSG_PasswordFor", getDisplayName(), type));
            // remove old password key
            Keyring.delete(deprecatedPasswordKey);
        } else {
            deletePassword();
        }
    }

    protected void deletePassword() {
        Keyring.delete(passwordKey);
        // remove old password key
        Keyring.delete(deprecatedPasswordKey);
    }

}
