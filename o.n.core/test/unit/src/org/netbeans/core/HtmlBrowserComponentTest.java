/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ObjectInput;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Factory;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author stan
 */
public class HtmlBrowserComponentTest {

    public HtmlBrowserComponentTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLookup() {
        System.out.println( "getLookup" );
        //The content of HtmlBrowser.Impl lookup must appear in browser TopComponent lookup
        HtmlBrowser.Factory factory = new HtmlBrowser.Factory() {
            @Override
            public Impl createHtmlBrowserImpl() {
                return new TestBrowserImpl();
            }
        };
        HtmlBrowserComponent browser = new HtmlBrowserComponent( factory, false, false);
        browser.open();
        assertSame( TestBrowserImpl.lookupContent, browser.getLookup().lookup( TestLookupContent.class ) );
    }

    private static class TestLookupContent {

    }

    private static class TestBrowserImpl extends HtmlBrowser.Impl {

        private final JComponent theComponent = new JPanel();
        private final static TestLookupContent lookupContent = new TestLookupContent();
        private final Lookup lookup = Lookups.singleton( lookupContent );

        @Override
        public Component getComponent() {
            return theComponent;
        }

        @Override
        public void reloadDocument() {
        }

        @Override
        public void stopLoading() {
        }

        @Override
        public void setURL( URL url ) {
        }

        @Override
        public URL getURL() {
            return null;
        }

        @Override
        public String getStatusMessage() {
            return null;
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        public boolean isForward() {
            return false;
        }

        @Override
        public void forward() {
        }

        @Override
        public boolean isBackward() {
            return false;
        }

        @Override
        public void backward() {
        }

        @Override
        public boolean isHistory() {
            return false;
        }

        @Override
        public void showHistory() {
        }

        @Override
        public void addPropertyChangeListener( PropertyChangeListener l ) {
        }

        @Override
        public void removePropertyChangeListener( PropertyChangeListener l ) {
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }
    };
}
