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

package org.netbeans.spi.options;

import java.awt.Image;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.modules.options.OptionsCategoryImpl;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;

/**
 * This class represents one category (like "Fonts &amp; Colors"
 * or "Editor") in Options Dialog.
 * <p>Normally panels are registered using one of the annotations in {@link OptionsPanelController}.
 * They may also be registered in a layer manually as follows:
 *
 *   <pre style="background-color: rgb(255, 255, 153);">
 *   &lt;folder name="OptionsDialog"&gt;
 *       &lt;file name="General.instance"&gt;
 *           &lt;attr name="instanceCreate" methodvalue="org.netbeans.spi.options.OptionsCategory.createCategory"/&gt;
 *           &lt;attr name="title" bundlevalue="org.netbeans.core.ui.options.general.Bundle#CTL_General_Options_Title"/&gt;
 *           &lt;attr name="categoryName" bundlevalue="org.netbeans.core.ui.options.general.Bundle#CTL_General_Options"/&gt;
 *           &lt;attr name="iconBase" stringvalue="org/netbeans/modules/options/resources/generalOptions.png"/&gt;
 *           &lt;attr name="controller" newvalue="org.netbeans.core.ui.options.general.GeneralOptionsPanelController"/&gt;
 *           &lt;attr name="keywords" bundlevalue="org.netbeans.core.ui.options.general.Bundle#KW_General"/&gt;
 *           &lt;attr name="keywordsCategory" stringvalue="General"/&gt;
 *           &lt;attr name="description" bundlevalue="org.netbeans.core.ui.options.general.Bundle#CTL_General_Options_Description"/&gt;
 *           &lt;attr name="position" intvalue="100"/&gt;
 *       &lt;/file&gt;
 *   &lt;/folder&gt;</pre>
 *
 * where:
 * <br><b>controller</b> should be an instance of <code>OptionsPanelController</code>
 * <br><b>title</b> should be a localized string where title of your tab inside OD is stored
 * <span class="nonnormative"><strong>Currently unused.</strong></span>
 * <br><b>categoryName</b> should be a localized string for your tab's category name
 * <br><b>iconBase</b> should be relative path to icon wou wish to display inside OD
 * <br><b>keywords</b> should be localized keywords list, separated by comma in Bundle, for quickserach purposes
 * <br><b>keywordsCategory</b> should be relative path to your panel inside Options dialog
 * <br><b>description</b> should be a localized string where your tab description is stored
 * <span class="nonnormative"><strong>Currently unused.</strong></span>
 *
 * <br><br>
 * Or, when registering a category with sub-panels, instead of
 * <pre style="background-color: rgb(255, 255, 153);">
 *            &lt;attr name="controller" newvalue="org.netbeans.core.ui.options.general.GeneralOptionsPanelController"/&gt;
 * </pre>
 * there is an option to use
 * <pre style="background-color: rgb(255, 255, 153);">
 *            &lt;attr name="advancedOptionsFolder" stringvalue="OptionsDialog/JavaOptions"/&gt;
 * </pre>
 * and supply a folder where instaces of <code>AdvancedOption</code> should be
 * registered. Its instances would be found automatically and shown as sub-panels
  <br><br>
 * Use standard {@code position} attributes to sort items registered in layers.
 *
 * @see AdvancedOption
 * @see OptionsPanelController
 *
 * @author Jan Jancura
 * @author Max Sauer
 */
public abstract class OptionsCategory {

    //xml entry names
    private static final String TITLE = "title"; // NOI18N
    private static final String CATEGORY_NAME = "categoryName"; // NOI18N
    private static final String ICON = "iconBase"; // NOI18N
    private static final String CONTROLLER = "controller"; // NOI18N
    private static final String KEYWORDS = "keywords"; // NOI18N
    private static final String KEYWORDS_CATEGORY = "keywordsCategory"; // NOI18N
    private static final String ADVANCED_OPTIONS_FOLDER = "advancedOptionsFolder"; // NOI18N

    /**
     * Returns base name of 32x32 icon (gif, png) used in list on the left side of
     * Options Dialog. See {@link AbstractNode#setIconBase} method for more info.
     *
     * @deprecated  This method will not be a part of NB50! Use
     *              {@link #getIcon} instead.
     * @return base name of 32x32 icon
     */
    @Deprecated
    public String getIconBase () {
        return null;
    }
    
    /**
     * Returns 32x32 icon used in list on the top of
     * Options Dialog.
     *
     * @return 32x32 icon
     */
    public Icon getIcon () {
        Icon res = ImageUtilities.loadImageIcon (getIconBase () + ".png", true);
        if (res == null)
            res = ImageUtilities.loadImageIcon( getIconBase () + ".gif", true);
        return res;
        }

    /**
     * Returns name of category used in list on the top side of
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getCategoryName ();

    /**
     * This text will be used in title component on the top of Options Dialog
     * when your panel will be selected.
     * <p class="nonnormative"><strong>Currently unused.</strong></p>
     * @return title of this panel
     */
    public abstract String getTitle ();

    /**
     * Returns new {@link OptionsPanelController} for this category. PanelController
     * creates visual component to be used inside of the Options Dialog.
     * You should not do any time-consuming operations inside
     * the constructor, because it blocks initialization of OptionsDialog.
     * Initialization should be implemented in update method.
     *
     * @return new instance of PanelController for this options category
     */
    public abstract OptionsPanelController create ();

    /**
     * Creates instance of <code>OptionsCategory</code> based on layer.xml
     * attribute values
     *
     * @param attrs attributes loaded from layer.xml
     * @return new <code>OptionsCategory</code> instance
     */
    static OptionsCategory createCategory(final Map attrs) {
        final String title = (String) attrs.get(TITLE);
        String categoryName = (String) attrs.get(CATEGORY_NAME);
        String iconBase = (String) attrs.get(ICON);
        String keywords = (String) attrs.get(KEYWORDS);
        String keywordsCategory = (String) attrs.get(KEYWORDS_CATEGORY);
        String advancedOptionsCategory = (String) attrs.get(ADVANCED_OPTIONS_FOLDER);
//        new Exception("preloading options panel " + title).printStackTrace();
        return new OptionsCategoryImpl(title, categoryName, iconBase, new Callable<OptionsPanelController>() {
            public OptionsPanelController call() throws Exception {
                Object o = attrs.get(CONTROLLER);
//                new Exception("loading options panel " + title + ": " + o).printStackTrace();
                if (o instanceof OptionsPanelController) {
                    return (OptionsPanelController) o;
                } else {
                    throw new Exception("got no controller from " + title + ": " + o);
                }
            }
        }, keywords, keywordsCategory, advancedOptionsCategory);
    }
}
