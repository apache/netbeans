/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.performance.j2se.dialogs;

import javax.swing.JButton;
import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.PluginsOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Proxy Configuration.
 *
 * @author mmirilovic@netbeans.org
 */
public class ProxyConfigurationTest extends PerformanceTestCase {

    private JButtonOperator openProxyButton;
    protected String BUTTON;
    private PluginsOperator pluginsOper;

    /**
     * Creates a new instance of ProxyConfiguration
     *
     * @param testName test name
     */
    public ProxyConfigurationTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ProxyConfiguration
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ProxyConfigurationTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar")
                .addTest(ProxyConfigurationTest.class)
                .suite();
    }

    public void testProxyConfiguration() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            @Override
            public boolean accept(JComponent c) {
                String className = c.getClass().getName();
                if ("javax.swing.JButton".equals(className) && "Proxy Settings".equals(((JButton)c).getText())) {
                    return false;
                }
                return !"javax.swing.JProgressBar".equals(c.getClass().getName());
            }

            @Override
            public String getFilterName() {
                return "Ignore search field cursor and Proxy Settings button animation";
            }
        });
        BUTTON = Bundle.getStringTrimmed("org.netbeans.modules.autoupdate.ui.Bundle", "SettingsTab.bProxy.text");
        pluginsOper = PluginsOperator.invoke();
        pluginsOper.selectSettings();
    }

    @Override
    public void prepare() {
        openProxyButton = new JButtonOperator(pluginsOper, BUTTON);
    }

    @Override
    public ComponentOperator open() {
        openProxyButton.pushNoBlock();
        return new NbDialogOperator("Options");
    }

    @Override
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((NbDialogOperator) testedComponentOperator).close();
        }
    }
    
    @Override
    public void shutdown() {
        if (pluginsOper != null && pluginsOper.isShowing()) {
            pluginsOper.close();
        }
    }
}
