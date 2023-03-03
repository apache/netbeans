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

package org.netbeans.installer.product.components;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Text.ContentType;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardComponent;

public abstract class ProductConfigurationLogic {
    private Product product;
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract void install(
            final Progress progress) throws InstallationException;
    
    public abstract void uninstall(
            final Progress progress) throws UninstallationException;
    
    public abstract List<WizardComponent> getWizardComponents();
    
    // product getter/setter ////////////////////////////////////////////////////////
    protected final Product getProduct() {
        return product;
    }
    
    final void setProduct(final Product product) {
        this.product = product;
    }
    
    // validation ///////////////////////////////////////////////////////////////////
    public String validateInstallation() {
        if (getProduct().getStatus() == Status.INSTALLED) {
            final File installLocation = getProduct().getInstallationLocation();
            if (installLocation == null) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.null");
            }
            if (!installLocation.exists()) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.missing",
                        installLocation);
            }
            if (!installLocation.isDirectory()) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.file",
                        installLocation);
            }
            if (FileUtils.isEmpty(installLocation)) {
                return ResourceUtils.getString(
                        ProductConfigurationLogic.class,
                        "PCL.validation.directory.empty",
                        installLocation);
            }
        }
        
        return null;
    }
    
    // product properties ///////////////////////////////////////////////////////////
    protected final String getProperty(String name) {
        return getProperty(name, true);
    }
    
    protected final String getProperty(String name, boolean parse) {
        final String value = product.getProperty(name);
        
        if (parse) {
            return value != null ? parseString(value) : null;
        } else {
            return value;
        }
    }
    
    protected final void setProperty(final String name, final String value) {
        product.setProperty(name, value);
    }
    
    // various documentation/legal getters //////////////////////////////////////////
    public Text getLicense() {
        final String text = parseString(
                "$R{" + StringUtils.asPath(getClass()) + "/license.txt;" + 
                StringUtils.ENCODING_UTF8 + "}");
        return text == null ? null : new Text(text, ContentType.PLAIN_TEXT);
    }
    
    public Map<String, Text> getThirdPartyLicenses() {
        return null;
    }

    public boolean requireLegalArtifactSaving() {
        return true;
    }
    
    public Text getThirdPartyLicense() {
        return null;
    }
    
    public Text getReleaseNotes() {
        return null;
    }
    
    public Text getReadme() {
        return null;
    }
    
    public Text getDistributionReadme() {
        return null;
    }
    
    public Text getInstallationInstructions() {
        return null;
    }
    
    // various informational probes /////////////////////////////////////////////////
    public boolean registerInSystem() {
        return true;
    }
    
    public String getSystemDisplayName() {
        return product.getDisplayName();
    }
    
    public boolean allowModifyMode() {
        return true;
    }
    
    /**
     * Specifies whether some special handling should be applied to the product's
     * files when placing them on disk. If this method returns <code>true</code>,
     * then the files of the product will be "wrapped" in the standard MacOS 
     * application directories structure. Also the {@link #getExecutable()} and
     * {@link #getIcon()} methods will be called and will be expected to return 
     * proper values for the product's executable and icon so that they are 
     * symlinked from appropriate locations.
     * 
     * @return Whether the product's files should be wrapped with the standard 
     *      Mac OS application directories structure.
     */
    public boolean wrapForMacOs() {
        return false;
    }
    
    /**
     * Specifies whether the the installation directory for the product should end
     * with Mac OS's specific extension - <code>.app</code>.
     * 
     * @return <code>true</code> - if the installation directory must end with 
     *      <code>.app</code>, <code>false</code> - otherwise.
     */
    public boolean requireDotAppForMacOs() {
        return false;
    }
    
    /**
     * @deprecated Use <code>getProhibitedInstallationPathParts</code> instead.
     */
    @Deprecated
    public boolean prohibitExclamation() {
        return true;
    }
    /**
     * Get the array of strings with each element deprecating the specific paths.<br><br>
     * One char length elements are treated as the single deprecated char.<br>    String.contains() is used for the check.<br><br>
     * Two and more chars length elements are treated as the regexp patterns.<br>    String.matches() is used for the check<br><br>
     * @return Array of prohibited path parts. 
     */
    public String [] getProhibitedInstallationPathParts() {
        return new String [] {"!", File.pathSeparator};
    }
    
    public String getExecutable() {
        return null;
    }
    
    public String getIcon() {
        return null;
    }
    
    public int getLogicPercentage() {
        return 10;
    }
    /** 
     * Get additional information about the product that is used during system integration
     */
    public Map <String, Object> getAdditionalSystemIntegrationInfo() {
        return new HashMap<String, Object>();
    }
    // installation behavior ////////////////////////////////////////////////////////
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }
    
    // helper methods for system utils and resource utils ///////////////////////////
    protected final String parseString(String string) {
        return SystemUtils.resolveString(string, product.getClassLoader());
    }
    
    protected final File parsePath(String path) {
        return SystemUtils.resolvePath(path, product.getClassLoader());
    }
    
    protected final String getString(String key) {
        return ResourceUtils.getString(getClass(), key);
    }

    protected final Map <Locale, String> getStrings(String key) {
        return ResourceUtils.getStrings(getClass(), key);
    }
    protected final Map <Locale, String> getStrings(String key, Object... arguments) {
        return ResourceUtils.getStrings(getClass(), key, arguments);
    }
    
    protected final String getString(String key, Object... arguments) {
        return ResourceUtils.getString(getClass(), key, arguments);
    }
    
    protected final InputStream getResource(String path) {
        return ResourceUtils.getResource(path, product.getClassLoader());
    }
}
