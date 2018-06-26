/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.openide.util.NbBundle;

/**
 * Combo box containing all Java SE platforms registered in NetBeans
 * or subset of them.
 * <p/>
 * @author Tomas Kraus
 */
public class JavaPlatformsComboBox
        extends JComboBox<JavaPlatformsComboBox.Platform> {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Encapsulate {@see JavaPlatform} object and provide human readable
     * <code>toString()</code> output for combo box.
     */
    public static class Platform {

        /** Java SE platform reference. */
        private final JavaPlatform platform;

        /**
         * Creates an instance of <code>Platform</code> object and sets provided
         * {@see JavaPlatform} reference.
         * <p/>
         * @param platform Java SE platform reference.
         */
        Platform(final JavaPlatform platform) {
            this.platform = platform;
        }

        /**
         * Get Java SE platform reference.
         * <p/>
         * @return Java SE platform reference.
         */
        public JavaPlatform getPlatform() {
            return platform;
        }

        /**
         * Get {@see String} representation of this object.
         * <p/>
         * @return {@see String} representation of this object.
         */
        @Override
        public String toString() {
            return platform.getDisplayName();
        }

        /**
         * Check if this platform is the default platform.
         * <p/>
         * @return Value of <code>true</code> if this platform is the default
         *         platform or <code>false</code> otherwise.
         */
        public boolean isDefault() {
            return platform.equals(JavaPlatform.getDefault());
        }

    }

    /**
     * Comparator for <code>Platform</code> instances to be sorted in combo box.
     */
    public static class PlatformComparator implements Comparator<Platform> {

        /**
         * Compares display name values of <code>Platform</code> instances.
         * <p/>
         * @param p1 First <code>Platform</code> instance to be compared.
         * @param p2 Second <code>Platform</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final Platform p1, final Platform p2) {
            String d1 = p1 != null ? p1.toString() : null;
            String d2 = p2 != null ? p2.toString() : null;
            return d1 != null
                ? (d2 != null ? d1.compareTo(d2) : 1)
                :  (d2 != null ? -1 : 0);
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Exception message for disabled constructors. */
    private static final String CONSTRUCTOR_EXCEPTION_MSG =
            "Data model for a combo box shall not be supplied in constructor.";

    /** Empty platform display name from properties. */
    public static final String EMPTY_DISPLAY_NAME = NbBundle.getMessage(
            JavaPlatformsComboBox.class,
            "JavaPlatformsComboBox.emptyDisplayName");
    
    /** Comparator for <code>Platform</code> instances to be sorted
     *  in combo box. */
    private static final PlatformComparator platformComparator
            = new PlatformComparator();

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Convert array of {@see JavaPlatform} objects to array of {@see Platform}
     * objects.
     * <p/>
     * @param platformsIn An array of {@see JavaPlatform} objects
     *        to be converted.
     * @return An array of {@see Platform} objects containing
     *         <code>platformsIn</code>.
     */
    private static Platform[] toPlatform(JavaPlatform[] platformsIn) {
        int size = platformsIn != null ? platformsIn.length : 0;
        Platform[] platformsOut = new Platform[size];
        for(int i = 0; i < size; i++)
            platformsOut[i] = new Platform(platformsIn[i]);
        Arrays.sort(platformsOut, platformComparator);
        return platformsOut;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param comboBoxModel Data model for this combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final ComboBoxModel comboBoxModel)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param items An array of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final Object items[])
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see JavaPlatform}.
     * <p/>
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(JavaPlatform[])} instead.
     * @param items {@see Vector} of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     */
    public JavaPlatformsComboBox(final Vector<?> items)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Creates an instance of <code>JavaPlatformsComboBox</code> that contains
     * all Java SE platforms registered in NetBeans.
     */
    public JavaPlatformsComboBox() {
        super(new DefaultComboBoxModel(toPlatform(
                JavaPlatformManager.getDefault().getInstalledPlatforms())));
    }

    /**
     * Creates an instance of <code>JavaPlatformsComboBox</code> that contains
     * supplied list of Java SE platforms.
     * <p/>
     * @param platforms Java SE platforms to be set as data model for combo box.
     */
    public JavaPlatformsComboBox(final JavaPlatform[] platforms) {
        super(new DefaultComboBoxModel(toPlatform(platforms)));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Update content of data model to contain all Java SE platforms currently
     * registered in NetBeans
     */
    public void updateModel() {
        setModel(new DefaultComboBoxModel(toPlatform(
                JavaPlatformManager.getDefault().getInstalledPlatforms())));
    }

    /**
     * Update content of data model to contain supplied list
     * of Java SE platforms.
     * <p/>
     * @param platforms Java SE platforms to be set as data model for combo box.
     */
    public void updateModel(final JavaPlatform[] platforms) {
        setModel(new DefaultComboBoxModel(toPlatform(platforms)));
    }

    /**
     * Set selected item in the combo box display area to the provided Java SE
     * platform.
     * <p/>
     * @param platform Java SE platform to be set as selected. Default platform
     *                 will be used when <code>null</code> value is supplied.
     */
    @Override
    public void setSelectedItem(Object platform) {
        if (platform == null) {
            platform = JavaPlatform.getDefault();
        }
        if (platform instanceof JavaPlatform) {
            int i, count = dataModel.getSize();
            for (i = 0; i < count; i++) {
                if (((JavaPlatform) platform).getDisplayName().equals(
                        (dataModel.getElementAt(i))
                        .getPlatform().getDisplayName())) {
                    super.setSelectedItem(dataModel.getElementAt(i));
                    break;
                }
            }
        } else {
            super.setSelectedItem(platform);
        }
    }

}
