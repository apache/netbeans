/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netbeans.core.network.proxy.pac;

import java.net.Proxy;
import java.net.URI;
import java.util.List;

/**
 * PAC Script evaluator.
 * 
 * <p>This is a bridge between Java and the PAC script, which is implemented in 
 * JavaScript. {@code PacScriptEvaluator}s are created via 
 * {@link PacScriptEvaluatorFactory}.
 * 
 * @author lbruun
 */
public interface PacScriptEvaluator {
    
    /**
     * Returns the proxy/proxies appropriate to use for the given URI.
     * 
     * <p>
     * The method calls the JavaScript {@code FindProxyForURL(url, host)}
     * function in the PAC script (or alternatively the
     * {@code FindProxyForURLEx(url, host)} function), parses the result and
     * returns it as a prioritized list of proxies.
     * 
     * @param uri URI to get proxies for. 
     * @return
     * @throws PacValidationException when the result from the JavaScript function
     *    cannot be interpreted.
     */
    public List<Proxy> findProxyForURL(URI uri) throws PacValidationException ;
    
    /**
     * Returns if the Evaluator uses result caching or not. Result caching speeds
     * up execution as a call to Java method {@link #findProxyForURL(java.net.URI)} will
     * not result in a call to JavaScript function  {@code FindProxyForURL}
     * if the URL has been resolved previously. But the Evaluator cannot use
     * caching if the PAC Script uses methods which depends on time and the Evaluator
     * may therefore have decided to turn off result caching.
     * 
     * <p>
     * If an implementation of {@code PacScriptEvaluator} never uses result caching 
     * (because it simply isn't implemented) then this method will always return 
     * {@code false}.
     * 
     * @return 
     */
    public boolean usesCaching();


    /**
     * Gets the entry function to the PAC script which the engine uses. 
     * @return name of JavaScript function
     */
    public String getJsEntryFunction();
    
    /**
     * Gets relevant information about the engine, typically the name
     * of the JavaScript engine, version number, etc.
     * @return info
     */
    public String getEngineInfo();
    
    /**
     * Gets the JavaScript source code of the PAC script.
     * @return source code
     */
    public String getPacScriptSource();
}
