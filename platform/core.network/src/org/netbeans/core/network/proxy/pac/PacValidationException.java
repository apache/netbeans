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

/**
 * Thrown whenever there are errors in the evaluation of the result
 * returned from the {@code FindProxyForURL(url, host)} function
 * in the PAC JavaScript.
 * 
 * <p>
 * In NetScape's original paper for the PAC script they define that 
 * the result of the function must be a string, made up of the following 
 * building blocks, separated by a semicolon:
 * <pre>
 *   DIRECT|PROXY &lt;host&gt;:&lt;port&gt;|SOCKS &lt;host&gt;:&lt;port&gt;
 * </pre>
 * 
 * For example, a legal return value might be:
 * <pre>
 *   PROXY anaconda:8099; PROXY 192.168.1.42:8100; DIRECT
 * </pre>
 * 
 * 
 * @author lbruun
 */
public class PacValidationException extends Exception {

    public PacValidationException(String value, String msg) {
        super("Cannot interpret value \"" + value  + "\" : " + msg);
    }
    
    public PacValidationException(String msg) {
        super(msg);
    }
}
