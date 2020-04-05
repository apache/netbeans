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
package org.netbeans.api.java.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import org.openide.util.WeakListeners;

/**
 * A {@link ComboBoxModel} holding {@link JavaPlatform} references. The selected
 * object can be a JavaPlatform or any object whose toString() method returns
 * a registered Java Platform ID. This platform id can be obtained from a
 * Java platform by getting its {@link #PROP_PLATFORM_ID} property.
 * <pre>
 *     JavaPlatform platform = &lt;some java platform&gt;
 *     String id = platform.getProperties().get(PROP_PLATFORM_ID);
 *     ...
 *     PlatformComboBoxModel model = new PlatformComboBoxModel();
 *     model.setSelectedItem(id);
 * </pre>
 *
 * @since 1.51
 * @author Laszlo Kishalmi
 */
public final class PlatformComboBoxModel extends AbstractListModel<JavaPlatform> implements ComboBoxModel<JavaPlatform>, PropertyChangeListener {

    /** The name of the platform unique id property in registered NetBeans */
    public static final String PROP_PLATFORM_ID = "platform.ant.name"; //NOI18N

    private static final Specification ALL_JAVA2SE = new Specification("J2SE", null); //NOI18N

    private static final Logger LOG = Logger.getLogger(PlatformComboBoxModel.class.getName());

    private final Specification specification;
    private JavaPlatform[] data;
    private Object sel;

    /**
     * Create a model containing all JavaSE platforms registered in the IDE.
     */
    public PlatformComboBoxModel() {
        this(ALL_JAVA2SE);
    }

    /**
     * Create a model containing Java platforms registered in the IDE that
     * matches with the provided {@link Specification}.
     *
     * @param specification filter for the Java Platforms
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public PlatformComboBoxModel(Specification specification) {
        this.specification = specification;
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        getPlatforms(jpm);
        jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public JavaPlatform getElementAt(int index) {
        return data[index];
    }

    @Override
    public void setSelectedItem(Object anItem) {
        boolean changed = sel != anItem;
        sel = anItem;
        if (changed) fireContentsChanged(this, 0, data.length);
    }

    @Override
    public Object getSelectedItem() {
        return sel;
    }

    /**
     * Get the selected item as {@link JavaPlatform}, if it is a Java platform,
     * return {@code null} otherwise;
     * 
     * @return the selected  {@link JavaPlatform} or {@code null} if the
     *         selection is not a valid platform.
     */
    public JavaPlatform getSelectedPlatform() {
        return sel instanceof JavaPlatform ? (JavaPlatform) sel : null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String current = sel instanceof JavaPlatform ? ((JavaPlatform) sel).getProperties().get(PROP_PLATFORM_ID) : sel.toString();
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        getPlatforms(jpm);
        JavaPlatform found = null;
        for (int i = 0; i < data.length; i++) {
            JavaPlatform pf = data[i];
            if (current.equals(pf.getProperties().get(PROP_PLATFORM_ID))) {
                found = pf;
                break;
            }
        }
        setSelectedItem(found != null ? found : current);
    }

    private void getPlatforms(JavaPlatformManager jpm) {
        data = jpm.getPlatforms(null, specification);
        if (LOG.isLoggable(Level.FINE)) {
            for (JavaPlatform jp : data) {
                LOG.log(Level.FINE, "Adding JavaPlaform: {0}", jp.getDisplayName()); //NOI18N
            }
        }
    }

}
