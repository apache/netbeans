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

package org.openide.awt;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Radim
 */
public class SwingBrowserTest extends TestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SwingBrowserTest.class);
    }

    public SwingBrowserTest(String testName) {
        super(testName);
    }

    public void testSimpleUseOfBrowser() throws Exception {
        System.out.println("testSimpleUseOfBrowser");
        // simulates 41891, maybe
        final HtmlBrowser.Impl impl = new SwingBrowserImpl();
        final JFrame f = new JFrame();
        final URL url = new URL("test", "localhost", -1, "simple", new MyStreamHandler());
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Component comp = impl.getComponent();
                f.add(comp);
                f.setVisible(true);
                impl.setURL(url);
            }
        });
        waitForLoading(url, f, impl);
        
    }

    public void testDeadlockWithDebug() throws Exception {
        System.out.println("testDeadlockWithDebug");
        // simulates 71450 - without this special property it fails but that's why we debug with this property set
        String oldPropValue = System.getProperty("org.openide.awt.SwingBrowserImpl.do-not-block-awt");
        System.setProperty("org.openide.awt.SwingBrowserImpl.do-not-block-awt", "true");
        
        final HtmlBrowser.Impl impl = new SwingBrowserImpl();
        final JFrame f = new JFrame();
        final Semaphore s = new Semaphore(1);
        final URL url = new URL("test", "localhost", -1, "simple", new MyStreamHandler(s, null));
        
        s.acquire();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Component comp = impl.getComponent();
                f.add(comp);
                f.setVisible(true);
                impl.setURL(url);
            }
        });
        System.out.println("browser visible, URL set");
        // now the browser is waiting for input stream
        
        // when failing it waits for Semaphore s but this is almost the same 
        // as waiting for reading from socket
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                impl.reloadDocument();
            }
        });
        System.out.println("reload called");
        s.release();
        System.setProperty("org.openide.awt.SwingBrowserImpl.do-not-block-awt", (oldPropValue != null)? oldPropValue: "false");
        
        waitForLoading(url, f, impl);
    }

    public void testDeadlockWithDebugWithoutProperty() throws Exception {
        System.out.println("testDeadlockWithDebug");
        // simulates 71450
        
        final HtmlBrowser.Impl impl = new SwingBrowserImpl();
        final JFrame f = new JFrame();
        final Semaphore s = new Semaphore(1);
        final URL url = new URL("test", "localhost", -1, "simple", new MyStreamHandler(s, null));
        
        s.acquire();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Component comp = impl.getComponent();
                f.add(comp);
                f.setVisible(true);
                impl.setURL(url);
            }
        });
        System.out.println("browser visible, URL set");
        // now the browser is waiting for input stream
        
        // when failing it waits for Semaphore s but this is almost the same 
        // as waiting for reading from socket
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                impl.reloadDocument();
            }
        });
        System.out.println("reload called");
        s.release();
        
        waitForLoading(url, f, impl);
    }

    public void testDeadlockWithJdk6() throws Exception {
        System.out.println("testDeadlockWithJdk6");
        // simulates another problem in 71450
        // fails on JDK6.0 b99 (passes on JDK6b92 or JDK5u9)
        final HtmlBrowser.Impl impl = new SwingBrowserImpl();
        final JFrame f = new JFrame();
        final Semaphore s = new Semaphore(1);
        final Semaphore s2 = new Semaphore(1);
        final URL url = new URL("test", "localhost", -1, "simple", new MyStreamHandler(s, s2));
        final URL url2 = new URL("test", "localhost", -1, "simple2", new MyStreamHandler(null, null));
        
        s.acquire();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Component comp = impl.getComponent();
                f.add(comp);
                f.setVisible(true);
                impl.setURL(url);
            }
        });
        System.out.println("browser visible, URL set");
        // now the browser is waiting for input stream
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.out.println("before 2nd setURL");
                // allow to read the stream
                s.release();
                try {
                    s2.acquire();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail(ex.getMessage());
                }
                
                impl.setURL(url2);
                s2.release();
                System.out.println("after 2nd setURL");
            }
        });
        System.out.println("new URL requested");
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                impl.getURL();
            }
        });
        System.out.println("getURL called");
        waitForLoading(url2, f, impl);
    }
    
    private void waitForLoading(final URL url, final JFrame f, final HtmlBrowser.Impl impl) 
            throws InvocationTargetException, InterruptedException {

        for (int i = 0; i < 10 && f.isVisible(); i++) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    URL current = impl.getURL();
                    if (url.equals(current)) {
                        f.setVisible(false);
                        f.dispose();
                    }
                }
            });
            Thread.sleep(i*100);
        }
    }
    
    private static class MyStreamHandler extends URLStreamHandler {
        private Semaphore sIn;
        private Semaphore sOut;
        
        MyStreamHandler() {}
        MyStreamHandler(Semaphore s, Semaphore s2) {
            sIn = s;
            sOut = s2;
            try {
                if (sOut != null) {
                    sOut.acquire();
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
        }
        
        protected URLConnection openConnection(URL u) throws IOException {
            return new MyConnection(sIn, sOut, u);
        }
    }
    
    private static class MyConnection extends HttpURLConnection {
        private Semaphore sIn;
        private Semaphore sOut;
        
        protected MyConnection(Semaphore s, Semaphore s2, URL u) {
            super(u);
            sIn = s;
            sOut = s2;
        }

        public void connect() throws IOException {
        }

        public @Override InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream("blabla".getBytes());
        }

        public void disconnect() {
            // noop
        }

        public boolean usingProxy() {
            return false;
        }

        public @Override int getResponseCode() throws IOException {
            System.out.println("connecting "+toString()+" ... isEDT = "+SwingUtilities.isEventDispatchThread());
//            Thread.dumpStack();
            if (sIn != null) {
                try {
                    sIn.acquire();
                    if (sOut != null) {
                        sOut.release();
                    }
                    System.out.println("aquired in lock, released out "+toString());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail(ex.getMessage());
                }
                sIn.release();
            }
            System.out.println("... connected "+toString());
            
            return super.getResponseCode();
        }

        public @Override String toString() {
            return "MyConnection ["+url.toExternalForm()+" sIn "+sIn+" sOut "+sOut+"]";
        }
        

    }
}
