/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        private static final TestLookupContent lookupContent = new TestLookupContent();
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
