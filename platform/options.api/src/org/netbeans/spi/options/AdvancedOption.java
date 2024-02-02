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

import org.netbeans.modules.options.AdvancedOptionImpl;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * This class represents one category (like "Ant"
 * or "Form Editor") in Miscellaneous Panel of Options Dialog.
 * <p>Normally panels are registered using {@link OptionsPanelController.SubRegistration}.
 * They may also be registered in a layer manually as follows:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;folder name="Advanced"&gt;
 *         &lt;file name="FooAdvancedPanel.instance"&gt;
 *             &lt;attr name="instanceCreate" methodvalue="org.netbeans.spi.options.AdvancedOption.createSubCategory"/&gt;
 *             &lt;attr name="controller" newvalue="org.foo.ToDoOptionsController"/&gt;
 *             &lt;attr name="displayName" bundlevalue="org.foo.Bundle#LBL_Foo"/&gt;
 *             &lt;attr name="toolTip" bundlevalue="org.foo.Bundle#HINT_Foo"/&gt;
 *             &lt;attr name="keywords" bundlevalue="org.foo.Bundle#KW_Foo"/&gt;
 *             &lt;attr name="keywordsCategory" stringvalue="Advanced/FooSubTabInOptions"/&gt;
 *         &lt;/file&gt;
 *     &lt;/folder&gt;
 * &lt;/folder&gt;</pre>
 *
 * where:
 * <br><b>controller</b> should be an instance of <code>OptionsPanelController</code>
 * <br><b>displayName</b> should be a localized string for your tab display name
 * <br><b>toolTip</b> should be a localized string for your tab tool tip
 * <span class="nonnormative"><strong>Currently unused.</strong></span>
 * <br><b>keywords</b> should be localized keywords list, separated by comma in Bundle, for quickserach purposes
 * <br><b>keywordsCategory</b> should be relative path to your panel inside Options dialog
 * <br><br>
 * No explicit sorting recognized (may be sorted e.g. by display name).
 *
 * <p><b>Related documentation</b>
 *
 * <ul>
 * <li><a href="https://netbeans.apache.org/tutorials/nbm-options.html">NetBeans Options Window Module Tutorial</a>
 * </ul>
 *
 * @see OptionsCategory
 * @see OptionsPanelController 
 * @author Jan Jancura
 * @author Max Sauer
 */
public abstract class AdvancedOption {

    //xml entry names
    private static final String DISPLAYNAME = "displayName";
    private static final String TOOLTIP = "toolTip";
    private static final String KEYWORDS = "keywords";
    private static final String CONTROLLER = "controller";
    private static final String KEYWORDS_CATEGORY = "keywordsCategory";

    /**
     * @deprecated Use {@link OptionsPanelController.SubRegistration} instead.
     */
    @Deprecated
    protected AdvancedOption() {}

    /**
     * Returns name of category used in Advanced Panel of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getDisplayName ();
    
    /**
     * Returns tooltip to be used on category name.
     * <p class="nonnormative"><strong>Currently unused.</strong></p>
     * @return tooltip for this category
     */
    public abstract String getTooltip ();

    /**
     * Returns {@link OptionsPanelController} for this category. PanelController 
     * creates visual component to be used inside of Advanced Panel.
     *
     * @return new instance of {@link OptionsPanelController} for this advanced options 
     *         category
     */
    public abstract OptionsPanelController create ();

    /**
     * Factory method for creating instaces of Advanced option in a declarative
     * way by loading necessary values from layer.xml
     *
     * @param attrs attributes defined in layer
     * @return instance of <code>AdvancedOption</code>
     */
    static AdvancedOption createSubCategory(final Map attrs) {
        final String displayName = (String) attrs.get(DISPLAYNAME);
        String tooltip = (String) attrs.get(TOOLTIP);
        String keywords = (String) attrs.get(KEYWORDS);
        String keywordsCategory = (String) attrs.get(KEYWORDS_CATEGORY);
//        new Exception("preloading advanced options panel " + displayName).printStackTrace();
        return new AdvancedOptionImpl(new Callable<OptionsPanelController>() {
            public OptionsPanelController call() throws Exception {
                Object o = attrs.get(CONTROLLER);
//                new Exception("loading advanced options panel " + displayName + ": " + o).printStackTrace();
                if (o instanceof OptionsPanelController) {
                    return (OptionsPanelController) o;
                } else {
                    throw new Exception("got no controller from " + displayName + ": " + o);
                }
            }
        }, displayName, tooltip, keywords, keywordsCategory);
    }
}
