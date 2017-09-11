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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;


/** Make sure automatic dependencies are parsed only on first start.
 * 
 * @author Jaroslav Tulach
 */
public class AutomaticDependenciesCachedTest extends NbTestCase {
    public AutomaticDependenciesCachedTest(String name) {
        super(name);
    }

    public static Test suite() throws IOException {
        assertFalse("Not consulted yet", LogConfig.consulted);
        System.setProperty("java.util.logging.config.class", LogConfig.class.getName());
        LogManager.getLogManager().readConfiguration();
        assertTrue("LogConfig class consulted", LogConfig.consulted);
        
        NbTestSuite s = new NbTestSuite();
        
        s.addTest(
            NbModuleSuite.emptyConfiguration().
            addTest(AutomaticDependenciesCachedTest.class, "testFirstStart").
            enableModules("platform\\d*", ".*").enableClasspathModules(false).
            honorAutoloadEager(true).gui(false).suite()
        );
        s.addTest(
            NbModuleSuite.emptyConfiguration().
            addTest(AutomaticDependenciesCachedTest.class, "testSecondStart").
            enableModules("platform\\d*", ".*").enableClasspathModules(false).
            reuseUserDir(true).honorAutoloadEager(true).gui(false).suite()
        );
        
        return s;
    }
    
    public void testFirstStart() {
        assertNotNull("Automatic dependencies parsed", System.getProperty("log.msg"));
        System.getProperties().remove("log.msg");
    }
    
    public void testSecondStart() {
        assertNull("Automatic dependencies should not be parsed on subsequent start", 
            System.getProperty("log.msg")
        );
    }
    
    private static final class Observer extends Handler {
        private final Logger logger;

        public Observer(Logger logger) {
            this.logger = logger;
        }
        
        
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("Parsing automatic dependencies")) {
                System.setProperty("log.msg", record.getMessage());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
            logger.addHandler(this);
        }
        
    }
    
    public static final class LogConfig {
        static boolean consulted;
        
        public LogConfig() {
            consulted = true;
            final Logger logger = Logger.getLogger("org.netbeans.core.startup.AutomaticDependencies");
            Observer o = new Observer(logger);
            o.setLevel(Level.ALL);
            logger.setLevel(Level.ALL);
            logger.addHandler(o);
        }
    }
}
