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
 * Creates PAC Script evaluator.
 * 
 * @author lbruun
 */
public interface PacScriptEvaluatorFactory {
    
    /**
     * Creates a PAC evaluator based on the given JavaScript source code.
     * 
     * <p>
     * The evaluator must treat the {@code pacSource} as untrusted and evaluate
     * it in a sandbox.
     * 
     * <p>
     * The method will throw {@link PacParsingException} if the JavaScript input
     * cannot be parsed/interpreted. In this case you may opt to either do
     * nothing, report to logging system or use a 
     * {@link #getNoOpEvaluator() no-op evaluator} as an alternative.
     * 
     * @param pacSource The JavaScript source code of the PAC script, as a string.
     *    To be a correct PAC script, it must implement the 
     *    {@code FindProxyForURL(url, host)} function. Most often the PAC script
     *    is downloaded from a network location.
     * @return
     * @throws PacParsingException if the source code cannot be parsed
     */
    public PacScriptEvaluator createPacScriptEvaluator(String pacSource) throws PacParsingException;
    
    /**
     * Gets a no-op PAC evaluator, meaning one which always returns {@code Proxy.NO_PROXY}
     * for any call to {@link PacScriptEvaluator#findProxyForURL(java.net.URI)}
     * 
     * @return 
     */
    public PacScriptEvaluator getNoOpEvaluator();
    
}
