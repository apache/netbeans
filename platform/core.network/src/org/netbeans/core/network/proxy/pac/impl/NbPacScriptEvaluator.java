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
package org.netbeans.core.network.proxy.pac.impl;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.netbeans.core.network.utils.SimpleObjCache;
import org.netbeans.core.network.proxy.pac.PacHelperMethods;
import org.netbeans.core.network.proxy.pac.PacJsEntryFunction;
import org.netbeans.core.network.proxy.pac.PacValidationException;
import org.netbeans.core.network.proxy.pac.PacParsingException;
import org.openide.util.Exceptions;
import org.netbeans.core.network.proxy.pac.PacScriptEvaluator;
import org.netbeans.core.network.proxy.pac.PacUtils;
import org.openide.util.Lookup;

/**
 * NetBeans implementation of a PAC script evaluator. This implementation
 * is the one returned by {@link NbPacScriptEvaluatorFactory}.
 * 
 * <h3>Features comparison</h3>
 * There are differences between how browsers have implemented the PAC
 * evaluation functionality. In the following the Apache NetBeans implementation
 * (this class) is pitched against some of the major browsers.<br><br>
 * 
 * <table summary="" style="table-layout: fixed; width:100%;" border="1" cellpadding="10" cellspacing="0">
 *   <tr><th class="tablersh">Behavior</th>
 *       <th>Apache<br>NetBeans</th>
 *       <th>Chrome<br>{@code 61.0.3163.100}</th>
 *       <th>Firefox<br>{@code 56.0.1}</th>
 *       <th>IE 11<br>{@code 11.608.15063.0}</th>
 *       <th>Edge<br>{@code 40.15063.0.0}</th>
 *   </tr>
 *   <tr>
 *       <td class="tablersh">Entry point functions supported:<br>{@code FindProxyForURL()} and {@code FindProxyForURLEx()}</td>
 *       <td>Both are supported. If both exist in the same PAC script then {@code FindProxyForURLEx()} is used.</td>
 *       <td>Only {@code FindProxyForURL()}</td>
 *       <td>Only {@code FindProxyForURL()}</td>
 *       <td>Both.<br>Only one of them may be present.</td>
 *       <td>Both.<br>Only one of them may be present.</td>
 *   </tr>
 *   <tr>
 *      <td class="tablersh">Security: Sandboxed execution of PAC script</td>
 *      <td>Yes</td>
 *      <td>???</td>
 *      <td>???</td>
 *      <td>Yes</td>
 *      <td>Yes</td>
 *   </tr>
 *   <tr>
 *       <td class="tablersh">Security: Stripped URL<br>(Value passed in {@code url} parameter)</td>
 *       <td>Yes<sup>1</sup></td>
 *       <td>Yes<sup>1</sup><br>(but strangely not for HTTP, only for HTTPS)</td>
 *       <td>Yes<sup>2</sup></td>
 *       <td>Yes</td>
 *       <td>Yes</td>
 *   </tr>
 *   <tr>
 *       <td class="tablersh">Extended return value support<br>Netscape spec only allowed:<ul><li>{@code DIRECT}</li><li>{@code PROXY host:port}</li><li>{@code SOCKS host:port}</li></ul></td>
 *       <td>In addition:<sup>3</sup><ul><li>{@code HTTP host:port}</li><li>{@code HTTPS host:port}</li><li>{@code SOCKS4 host:port}</li><li>{@code SOCKS5 host:port}</li></ul></td>
 *       <td>???</td>
 *       <td>In addition:<ul><li>{@code HTTP host:port}</li><li>{@code HTTPS host:port}</li><li>{@code SOCKS4 host:port}</li><li>{@code SOCKS5 host:port}</li></ul></td>
 *       <td>???</td>
 *       <td>???</td>
 *   </tr>
 *   <tr>
 *       <td class="tablersh">Uses result cache</td>
 *       <td>Yes<br>(based on {@code url} value)</td>
 *       <td>Yes</td>
 *       <td>???</td>
 *       <td>Yes<br>(based on {@code host} value)</td>
 *       <td>???</td>
 *   </tr>
 *   <tr>
 *       <td class="tablersh">Support for <a href="https://msdn.microsoft.com/en-us/library/windows/desktop/gg308476(v=vs.85).aspx">getClientVersion()</a></td>
 *       <td>Yes<br>(returns "1.0")</td>
 *       <td>No</td>
 *       <td>No</td>
 *       <td>Yes<br>(returns "1.0")</td>
 *       <td>Yes<br>(returns "1.0")</td>
 *   </tr>
 *   <tr>
 *      <td class="tablersh">Use of result cache
 *           when script uses time sensitive functions:<br>
 *           {@code weekdayRange()}, {@code dateRange()} and {@code timeRange()}
 *       </td>
 *      <td>Result cache not used</td>
 *      <td>???</td>
 *      <td>???</td>
 *      <td>???</td>
 *      <td>???</td>
 *   </tr>
 *   <tr>
 *      <td class="tablersh">Name lookup timeout<sup>4</sup></td>
 *      <td>Explicit.<br>(see {@link NbPacHelperMethods#DNS_TIMEOUT_MS DNS_TIMEOUT_MS})</td>
 *      <td>???</td>
 *      <td>Yes</td>
 *      <td>???</td>
 *      <td>???</td>
 *   </tr>
 *   <tr>
 *      <td class="tablersh">Support for Date/Time functions with range crossing a boundary<sup>5</sup></td>
 *      <td>Yes</td>
 *      <td>???</td>
 *      <td>Yes<br>since v49</td>
 *      <td>???</td>
 *      <td>???</td>
 *   </tr>
 *   <tr>
 *      <td class="tablersh">myIpAddress()<sup>6</sup></td>
 *      <td>Pretty good. Uses JNI. (At least far better than text book Java approach which consistently yields incorrect result)</td>
 *      <td>Uses UDP to try to find IP address. Seems to be reliable.</td>
 *      <td>???</td>
 *      <td>???</td>
 *      <td>???</td>
 *   </tr>
 * </table>
 * 1) The following is removed: <i>{@code user-info}</i> and everything after the 
 *    host name, however the value passed to the script always ends with a '/' 
 *    character.<br>
 * 2) Same as (1), except that <i>{@code user-info}</i> is not removed.<br>
 * 3) However, when converted to Java {@link java.net.Proxy} object, all return 
 *    types must be mapped to Java Proxy types {@code DIRECT}, 
 *    {@code HTTP} and {@code SOCKS}, which means some finer grained information
 *    is lost. (but is irrelevant)<br>
 * 4) An implementation not using an explicit timeout will be at the mercy of 
 *    the underlying OS or runtime environment. For example, for 2 name servers, 
 *    the default timeout in JRE is 30 seconds.<br>
 * 5) If date/time functions ({@code weekdayRange()}, {@code dateRange()} and 
 *    {@code timeRange()}) allow values that crosses a value boundary. 
 *    For example {@code timeRange(22,3}) for the range from 10 pm to 3 am, or 
 *    {@code dateRange("DEC","MAR"}) for the range from Dec 1st to March 31st.<br>
 * 6) How good is the {@code myIpAddress()} (or {@code myIpAddressEx()}) function
 *    at finding the host's correct IP address, in particular on a multi-homed
 *    computer.
 *
 * <h3>Customization</h3>
 * The implementation for 
 * {@link org.netbeans.core.network.proxy.pac.PacHelperMethods PacHelperMethods} is
 * found via the global lookup. If you are unhappy with the implementation
 * of the Helper Functions you can replace with your own.
 * 
 * @author lbruun
 */
public class NbPacScriptEvaluator implements PacScriptEvaluator {

    private static final Logger LOGGER = Logger.getLogger(NbPacScriptEvaluator.class.getName());


    private static final String JS_HELPER_METHODS_INSTANCE_NAME = "jsPacHelpers";
    
    private final boolean canUseURLCaching;
    private final PacScriptEngine scriptEngine;
    private final SimpleObjCache<URI,List<Proxy>> resultCache;
    
    private static final String PAC_PROXY = "PROXY";
    private static final String PAC_DIRECT = "DIRECT";
    private static final String PAC_SOCKS = "SOCKS";
    private static final String PAC_SOCKS4_FFEXT = "SOCKS4"; // Mozilla Firefox extension. Not part of original Netscape spec.
    private static final String PAC_SOCKS5_FFEXT = "SOCKS5"; // Mozilla Firefox extension. Not part of original Netscape spec.
    private static final String PAC_HTTP_FFEXT = "HTTP"; // Mozilla Firefox extension. Not part of original Netscape spec.
    private static final String PAC_HTTPS_FFEXT = "HTTPS"; // Mozilla Firefox extension. Not part of original Netscape spec.
    private final boolean nashornJava8u40Available;
    private final String pacScriptSource;


    public NbPacScriptEvaluator(String pacSourceCocde) throws PacParsingException {
        this.pacScriptSource = pacSourceCocde;
        nashornJava8u40Available = getNashornJava8u40Available();
        scriptEngine = getScriptEngine(pacSourceCocde);
        canUseURLCaching = !usesTimeDateFunctions(pacSourceCocde);
        if (canUseURLCaching) {
            resultCache = new SimpleObjCache<>(100);
        } else {
            resultCache = null;
        }
    }

    @Override
    public List<Proxy> findProxyForURL(URI uri) throws PacValidationException {

        List<Proxy> jsResultAnalyzed;
        
        // First try the cache
        if (resultCache != null) {
            jsResultAnalyzed = resultCache.get(uri);
            if (jsResultAnalyzed != null) {
                return jsResultAnalyzed;
            }
        }
        try {
            Object jsResult = scriptEngine.findProxyForURL(PacUtils.toStrippedURLStr(uri), uri.getHost()); 
            jsResultAnalyzed = analyzeResult(uri, jsResult);
            if (canUseURLCaching && (resultCache != null)) {
                resultCache.put(uri, jsResultAnalyzed);   // save the result in the cache
            }
            return jsResultAnalyzed;
        } catch (NoSuchMethodException ex) {
            // If this exception occur at this time it is really, really unexpected. 
            // We already gave the function a test spin in the constructor.
            Exceptions.printStackTrace(ex);
            return Collections.singletonList(Proxy.NO_PROXY);
        } catch (ScriptException ex) {
            LOGGER.log(Level.WARNING, "Error when executing PAC script function " + scriptEngine.getJsMainFunction().getJsFunctionName() + " : ", ex);
            return Collections.singletonList(Proxy.NO_PROXY);
        } catch (Exception ex) {  // for runtime exceptions
            if (ex.getCause() != null) {
                if (ex.getCause() instanceof ClassNotFoundException) {
                    // Is someone trying to break out of the sandbox ?
                    LOGGER.log(Level.WARNING, "The downloaded PAC script is attempting to access Java class ''{0}'' which may be a sign of maliciousness. You should investigate this with your network administrator.", ex.getCause().getMessage());
                    return Collections.singletonList(Proxy.NO_PROXY);
                }
            }
            // other unforseen errors
            LOGGER.log(Level.WARNING, "Error when executing PAC script function " + scriptEngine.getJsMainFunction().getJsFunctionName() + " : ", ex);
            return Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public boolean usesCaching() {
        return (canUseURLCaching && (resultCache != null));
    }

    @Override
    public String getJsEntryFunction() {
        return scriptEngine.getJsMainFunction().getJsFunctionName();
    }

    @Override
    public String getEngineInfo() {
        ScriptEngineFactory factory = scriptEngine.getScriptEngine().getFactory();
        return factory.getEngineName() + " version " + factory.getEngineVersion();
    }

    @Override
    public String getPacScriptSource() {
        return this.pacScriptSource;
    }
    
    

    private PacScriptEngine getScriptEngine(String pacSource) throws PacParsingException {

        try {
            String helperJSScript = getHelperJsScriptSource();
            LOGGER.log(Level.FINER, "PAC Helper JavaScript :\n{0}", helperJSScript);
            
            ScriptEngine engine;
            if (nashornJava8u40Available) {
                engine = getNashornJSScriptEngine();
            } else {
                engine = getGenericJSScriptEngine();
            }
            
            LOGGER.log(Level.FINE, "PAC script evaluator using:  {0}", getEngineInfo(engine));
            
            
            PacHelperMethods pacHelpers = Lookup.getDefault().lookup(PacHelperMethods.class);
            if (pacHelpers == null) { // this should be redundant but we take no chances
                pacHelpers = new NbPacHelperMethods();
            }
            Bindings b = engine.createBindings();
            b.put(JS_HELPER_METHODS_INSTANCE_NAME, pacHelpers);
            engine.setBindings(b, ScriptContext.ENGINE_SCOPE);
            
            engine.eval(pacSource);
            engine.eval(helperJSScript);

            // Do some minimal testing of the validity of the PAC Script.
            final PacJsEntryFunction jsMainFunction;
            if (nashornJava8u40Available) {
                jsMainFunction = testScriptEngine(engine, true);
            } else {
                jsMainFunction = testScriptEngine(engine, false);
            }
            
            return new PacScriptEngine(engine, jsMainFunction);
        } catch (ScriptException ex) {
            throw new  PacParsingException(ex);
        }
    }
    
    private boolean getNashornJava8u40Available() {
        try {
            Class<?> klass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
    
    private ScriptEngine getNashornJSScriptEngine() {
        NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
        return factory.getScriptEngine(new ClassFilterPacHelpers());
    }
    
    private ScriptEngine getGenericJSScriptEngine() {
        // The result of the statements below may be Rhino, but more likely
        // - since Java 8 - it will be a Nashorn engine.
        ScriptEngineManager factory = new ScriptEngineManager();
        return factory.getEngineByName("JavaScript");
    }
    
    /**
     * Test if the main entry point, function FindProxyForURL()/FindProxyForURLEx(), 
     * is available.
     */
    private PacJsEntryFunction testScriptEngine(ScriptEngine eng, boolean doDeepTest) throws PacParsingException {
        if (isJsFunctionAvailable(eng, PacJsEntryFunction.IPV6_AWARE.getJsFunctionName(), doDeepTest)) {
            return PacJsEntryFunction.IPV6_AWARE;
        }
        if (isJsFunctionAvailable(eng, PacJsEntryFunction.STANDARD.getJsFunctionName(), doDeepTest)) {
            return PacJsEntryFunction.STANDARD;
        }
        throw new PacParsingException("Function " + PacJsEntryFunction.STANDARD.getJsFunctionName() + " or " + PacJsEntryFunction.IPV6_AWARE.getJsFunctionName() + " not found in PAC Script.");
    }

    private boolean isJsFunctionAvailable(ScriptEngine eng, String functionName, boolean doDeepTest) {
        // We want to test if the function is there, but without actually 
        // invoking it.        
        Object obj = eng.get(functionName);
        
        if (!doDeepTest && obj != null) {  
            // Shallow test. We've established that there's
            // "something" in the ENGINE_SCOPE with a name like
            // functionName, and we *hope* it is a function, but we really don't
            // know, therefore we call it a shallow test.
            return true;
        }
        
        // For Nashorn post JDK8u40 we can do even deeper validation
        // using the ScriptObjectMirror class. This will not work for Rhino.
        if (doDeepTest && obj != null) {
            if (obj instanceof ScriptObjectMirror) {
                    ScriptObjectMirror  som = (ScriptObjectMirror) obj;
                    if (som.isFunction()) {
                        return true;
                    }
            }
        }        
        return false;
    }
    

    private String getHelperJsScriptSource() throws PacParsingException {
        return HelperScriptFactory.getPacHelperSource(JS_HELPER_METHODS_INSTANCE_NAME);
    }


    /**
     * Does the script source make reference to any of the date/time functions
     * (timeRange(), dateRange(), weekdayRange()) ?
     *
     * @param pacScriptSource
     * @return
     */
    private static boolean usesTimeDateFunctions(String pacScriptSource) {
        // Will be called only once so there's little to be gained by precompiling 
        // the regex statement.

        Pattern pattern = Pattern.compile(".*(timeRange\\s*\\(|dateRange\\s*\\(|weekdayRange\\s*\\().*", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(pacScriptSource);
        return matcher.matches();
    }

    private String getEngineInfo(ScriptEngine engine) {
        StringBuilder sb = new StringBuilder();
        ScriptEngineFactory f = engine.getFactory();
        sb.append("LanguageName=");
        sb.append("\"").append(f.getLanguageName()).append("\"");
        sb.append(" ");
        sb.append("LanguageVersion=");
        sb.append("\"").append(f.getLanguageVersion()).append("\"");
        sb.append(" ");
        sb.append("EngineName=");
        sb.append("\"").append(f.getEngineName()).append("\"");
        sb.append(" ");
        sb.append("EngineNameAliases=");
        sb.append(Arrays.toString(f.getNames().toArray(new String[f.getNames().size()])));
        sb.append(" ");
        sb.append("EngineVersion=");
        sb.append("\"").append(f.getEngineVersion()).append("\"");
        return sb.toString();
    }

    /**
     * Translates result from JavaScript into list of java proxy types.
     * 
     * The string returned from the JavaScript function (input to this
     * method) can contain any number of the following building blocks, 
     * separated by a semicolon:
     * 
     *   DIRECT
     *       Connections should be made directly, without any proxies.
     * 
     *   PROXY host:port
     *       The specified proxy should be used.
     * 
     *   SOCKS host:port
     *       The specified SOCKS server should be used.
     * 
     * 
     * @param uri
     * @param proxiesString
     * @return 
     */
    private List<Proxy> analyzeResult(URI uri, Object proxiesString) throws PacValidationException {
        if (proxiesString == null) {
            LOGGER.log(Level.FINE, "Null result for {0}", uri);
            return null;
        }
        
        StringTokenizer st = new StringTokenizer(proxiesString.toString(), ";"); //NOI18N
        List<Proxy> proxies = new LinkedList<>();
        while (st.hasMoreTokens()) {
            String proxySpec = st.nextToken().trim();
            proxies.add(getProxy(proxySpec));
        }
        return proxies;
    }

    private static Proxy getProxy(String proxySpec) throws PacValidationException {
         if (proxySpec.equals(PAC_DIRECT)) {
             return Proxy.NO_PROXY;
         }      
        
        String[] ele = proxySpec.split(" +"); // NOI18N
        if (ele.length != 2) {
            throw new PacValidationException("The value \"" + proxySpec + "\" has incorrect format");
        }
        final Proxy.Type proxyType;
        switch(ele[0]) {
            case PAC_PROXY:
            case PAC_HTTP_FFEXT:
            case PAC_HTTPS_FFEXT:
                proxyType = Proxy.Type.HTTP;
                break;
            case PAC_SOCKS:
            case PAC_SOCKS4_FFEXT:
            case PAC_SOCKS5_FFEXT:
                proxyType = Proxy.Type.SOCKS;
                break;
            default:
                throw new PacValidationException("The value \"" + ele[0] + "\" is an unknown proxy type");
        }
        
        String hostAndPortNo = ele[1];
        int i = hostAndPortNo.lastIndexOf(":"); // NOI18N
        if (i <= 0 || i == (hostAndPortNo.length() - 1)) {
            throw new PacValidationException("The string \"" + ele[1] + "\" has no port number");
        }

        String host = hostAndPortNo.substring(0, i);
        String portStr = hostAndPortNo.substring(i+1);

        int portNo = -1;
        try {
            portNo =  Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            throw new PacValidationException("The portno value \"" + portStr + "\" cannot be converted to an integer");
        }
        
        return new Proxy(proxyType, new InetSocketAddress(host, portNo));
    }
    
    
    private static class PacScriptEngine  {
        private final ScriptEngine scriptEngine;
        private final PacJsEntryFunction jsMainFunction;
        private final Invocable invocable;

        public PacScriptEngine(ScriptEngine scriptEngine, PacJsEntryFunction jsMainFunction) {
            this.scriptEngine = scriptEngine;
            this.jsMainFunction = jsMainFunction;
            this.invocable = (Invocable) scriptEngine;
        }

        public PacJsEntryFunction getJsMainFunction() {
            return jsMainFunction;
        }

        public ScriptEngine getScriptEngine() {
            return scriptEngine;
        }

        public Invocable getInvocable() {
            return invocable;
        }
        
        public Object findProxyForURL(String url, String host) throws ScriptException, NoSuchMethodException {
            return invocable.invokeFunction(jsMainFunction.getJsFunctionName(), url, host);
        }
    }
        
}
