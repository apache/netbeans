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

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.spi.CustomizerCookie;
import org.openide.util.Lookup;

/**
 * GlassFish server properties customizer.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishPropertiesCustomizer extends JTabbedPane {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * GlassFish server properties customizer events handler.
     */
    private static class CustomizerListener implements AncestorListener {

        /** GlassFish server instance being customized. */
        private final GlassfishInstance instance;

        /**
         * Creates an instance of GlassFish server properties customizer
         * events handler.
         * <p/>
         * @param instance GlassFish server instance being customized.
         */
        CustomizerListener(final GlassfishInstance instance) {
            this.instance = instance;
        }

        /**
         * Called when the source or one of its ancestors is made visible.
         * <p/>
         * Currently do nothing.
         * <p/>
         * @param event An event reported to listener.
         */
        @Override
        public void ancestorAdded(final AncestorEvent event) {}

        /**
         * Called when the source or one of its ancestors is made invisible.
         * <p/>
         * Persist updated properties.
         * <p/>
         * @param event An event reported to listener.
         */
        @Override
        public void ancestorRemoved(final AncestorEvent event) {
            LOGGER.log(Level.INFO, "Storing {0} atributes",
                    instance.getDisplayName());
            try {
                // #254197
                // this is weird, but prevents reintroducing the instance that
                // has just been removed from panel (and thus the customizer as
                // well)
                if(instance.getInstanceProvider().getInstance(instance.getUrl()) != null) {
                    GlassfishInstance.writeInstanceToFile(instance);
                }
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store GlassFish server attributes", ex);
            }
        }

        /**
         * Called when either the source or one of its ancestors is moved.
         * <p/>
         * Currently do nothing.
         * <p/>
         * @param event An event reported to listener.
         */
        @Override
        public void ancestorMoved(final AncestorEvent event) {}
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassFishPropertiesCustomizer.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Customizer events listener. */
    private final CustomizerListener customizerListener;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of GlassFish server properties customizer.
     * <p/>
     * @param instance GlassFish server instance being customized.
     * @param lookup   GlassFish server instance local lookup.
     */
    public GlassFishPropertiesCustomizer(
            GlassfishInstance instance, Lookup lookup) {
        customizerListener = new CustomizerListener(instance);
        addAncestorListener(customizerListener);
        JPanel commonCustomizer = instance.isRemote()
                ? new InstanceRemotePanel(instance)
                : new InstanceLocalPanel(instance);
        JPanel vmCustomizer = new VmCustomizer(instance);

        Collection<JPanel> pages = new LinkedList<>();
        Collection<? extends CustomizerCookie> lookupAll
                = lookup.lookupAll(CustomizerCookie.class);
        for(CustomizerCookie cookie : lookupAll) {
            pages.addAll(cookie.getCustomizerPages());
        }
        pages.add(vmCustomizer);
        add(commonCustomizer);
        for (JPanel page : pages) {
            add(page);
        }
    }

}
