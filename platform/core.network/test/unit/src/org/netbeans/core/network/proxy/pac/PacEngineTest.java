/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.core.network.proxy.pac.impl.NbPacScriptEvaluatorFactory;
import org.netbeans.junit.NbTestCase;

/**
 *
 */
public class PacEngineTest extends NbTestCase {
    
    public PacEngineTest() {
        super("Pac Evaluator Test");
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

   
   

    /**
     * Test of toSemiColonListStr method, of class PacUtils.
     */
    @Test
    public void testEngine() throws PacParsingException, IOException, URISyntaxException, PacValidationException {
        System.out.println("toSemiColonListStr");

        PacScriptEvaluatorFactory factory = new NbPacScriptEvaluatorFactory();
        
        testPacFile("pac-test1.js", factory, 1, true);
        testPacFile("pac-test2.js", factory, 3, true);
        testPacFile("pac-test3.js", factory, 1, false);
        testPacFileMalicious("pac-test-sandbox-breakout.js", factory);

        testPacFile2("pac-test4.js", factory);
    }

    @Test
    public void testEngine2() throws IOException, PacParsingException, URISyntaxException, PacValidationException {

        File pacFilesDir = new File(getDataDir(), "pacFiles");
        byte[] b = Files.readAllBytes((new File(pacFilesDir, "wpad.dat")).toPath());
        String pacSource = new String(b, StandardCharsets.UTF_8);

        PacScriptEvaluatorFactory factory = new NbPacScriptEvaluatorFactory();
        PacScriptEvaluator evaluator = factory.createPacScriptEvaluator(pacSource);

        List<Proxy> proxiesFQDN = evaluator.findProxyForURL(new URI("https://www.heise.de"));
        assertNotNull(proxiesFQDN);
        assertEquals(1, proxiesFQDN.size());
        assertEquals(Proxy.Type.HTTP, proxiesFQDN.get(0).type());
        assertEquals("www-proxy.us.oracle.com", ((InetSocketAddress) proxiesFQDN.get(0).address()).getHostString());
        assertEquals(80, ((InetSocketAddress) proxiesFQDN.get(0).address()).getPort());
        List<Proxy> simpleName = evaluator.findProxyForURL(new URI("https://localserver"));
        assertNotNull(simpleName);
        assertEquals(1, simpleName.size());
        assertEquals(Proxy.Type.DIRECT, simpleName.get(0).type());
    }

    private String getPacSource(String pacFileName) throws IOException {
        File pacFilesDir = new File(getDataDir(), "pacFiles2");
        byte[] b = Files.readAllBytes((new File(pacFilesDir, pacFileName)).toPath());
        return new String(b, StandardCharsets.UTF_8);
    }

    private void testPacFile(String pacFileName, PacScriptEvaluatorFactory factory, int expectedReturnedProxies, boolean usesCaching) throws IOException, PacParsingException, URISyntaxException, PacValidationException {
        String pacSource = getPacSource(pacFileName);

        PacScriptEvaluator pacEvaluator = factory.createPacScriptEvaluator(pacSource);

        URI testURL = new URI("http://netbeans.apache.org");  // doesn't actually matter which URL we use
        List<Proxy> proxies = pacEvaluator.findProxyForURL(testURL);

        assertEquals(expectedReturnedProxies, proxies.size());
        assertEquals(usesCaching, pacEvaluator.usesCaching());
    }

    private void testPacFile2(String pacFileName, PacScriptEvaluatorFactory factory ) throws IOException, PacParsingException, URISyntaxException, PacValidationException {
        String pacSource = getPacSource(pacFileName);

        PacScriptEvaluator pacEvaluator = factory.createPacScriptEvaluator(pacSource);

        URI testURL = new URI("http://netbeans.apache.org");  // doesn't actually matter which URL we use
        List<Proxy> proxies = pacEvaluator.findProxyForURL(testURL);

        assertEquals(Collections.singletonList(Proxy.NO_PROXY), proxies);
    }
    
    private void testPacFileMalicious(String pacFileName, PacScriptEvaluatorFactory factory ) throws IOException, PacParsingException, URISyntaxException, PacValidationException {
        String pacSource = getPacSource(pacFileName);

        PacScriptEvaluator pacEvaluator = factory.createPacScriptEvaluator(pacSource);

        URI testURL = new URI("http://netbeans.apache.org");  // doesn't actually matter which URL we use
        List<Proxy> proxies = pacEvaluator.findProxyForURL(testURL);

        assertEquals(Collections.singletonList(Proxy.NO_PROXY), proxies);
    }
}
