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

package org.netbeans.modules.options;

import java.lang.String;
import java.util.Arrays;
import org.netbeans.spi.options.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.openide.util.Exceptions;

/**
 * Advanced Option implementation class. Used by factory method from
 * <code>AdvancedOption</code> as instance created from layer.xml values
 *
 * @author Max Sauer
 */
public class AdvancedOptionImpl extends AdvancedOption {
    
    private String displayName;
    private String tooltip;
    private String keywords;
    private Callable<OptionsPanelController> controller;
    private String keywordsCategory;

    @SuppressWarnings("deprecation")
    public AdvancedOptionImpl(Callable<OptionsPanelController> controller, String displayName, String tooltip, String keywords, String keywordsCategory) {
        this.controller = controller;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.keywords = keywords;
        this.keywordsCategory = keywordsCategory;
    }

    @Override
    public String getDisplayName () {
        return displayName;
    }

    @Override
    public String getTooltip () {
        return tooltip;
    }

    /**
     * Provides list of options for this category
     * @return list of keywords for each optioncategory sub-panel
     */
    public Set<String> getKeywordsByCategory() {
	if (keywords != null) {
	    return Collections.singleton(keywords);
	} else {
	    return Collections.emptySet();
	}
    }

    @Override
    public OptionsPanelController create() {
        try {
            return controller.call();
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
            return new TabbedController("<error>"); // NOI18N
        }
    }

}
