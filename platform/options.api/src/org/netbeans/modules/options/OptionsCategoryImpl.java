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

package org.netbeans.modules.options;

import java.awt.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * OptionsCategory implementation class. Used by factory method from
 * <code>OptionsCategory</code> as instance created from layer.xml values
 * 
 * @author Max Sauer
 */
public class OptionsCategoryImpl extends OptionsCategory {

    //category fields
    private String title;
    private String categoryName;
    private String iconBase;
    private ImageIcon icon;
    private Callable<OptionsPanelController> controller;
    private String keywords;
    private String keywordsCategory;
    private String advancedOptionsFolder; //folder for lookup

    public OptionsCategoryImpl(String title, String categoryName, String iconBase, Callable<OptionsPanelController> controller, String keywords, String keywordsCategory, String advancedOptionsFolder) {
        this.title = title;
        this.categoryName = categoryName;
        this.iconBase = iconBase;
        this.controller = controller;
        this.advancedOptionsFolder = advancedOptionsFolder;
        this.keywords = keywords;
        this.keywordsCategory = keywordsCategory;
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            Icon res = ImageUtilities.loadImageIcon(iconBase, true);
            if (res != null) {
                return res;
            }
            res = ImageUtilities.loadImageIcon(iconBase + ".png", true);
            if (res != null) {
                return res;
            }
            res = ImageUtilities.loadImageIcon(iconBase + ".gif", true);
            return res;
        }
        return icon;
    }

    @Override
    public String getCategoryName () {
        return categoryName;
    }

    @Override
    public String getTitle () {
        return title;
    }

    @Override
    public OptionsPanelController create() {
        if (advancedOptionsFolder != null) {
            return new TabbedController(advancedOptionsFolder);
        } else {
            try {
                return controller.call();
            } catch (Exception x) {
                Exceptions.printStackTrace(x);
                return new TabbedController("<error>"); // NOI18N
            }
        }
    }

    final Set<String> getKeywordsByCategory() {
	if (keywords != null) {
	    return Collections.singleton(keywords);
	} else {
	    return Collections.emptySet();
	}
    }

    @Override
    public String toString() {
        return "OptionsCategoryImpl{" + "title=" + title + ", categoryName=" + categoryName + ", iconBase=" + iconBase + ", icon=" + icon + ", controller=" + controller + ", keywords=" + keywords + ", keywordsCategory=" + keywordsCategory + ", advancedOptionsFolder=" + advancedOptionsFolder + '}';
    }
}
