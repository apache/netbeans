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
import java.util.Collections;
import java.util.List;

/**
 * PAC Script Evaluator which can be used as a last resort. 
 * Always returns {@code NO_PROXY}.
 * 
 * @author lbruun
 */
public class PacScriptEvaluatorNoProxy implements PacScriptEvaluator {

    @Override
    public List<Proxy> findProxyForURL(URI u) {
        return Collections.singletonList(Proxy.NO_PROXY);
    }

    @Override
    public boolean usesCaching() {
        return false;
    }

    @Override
    public String getJsEntryFunction() {
        return "NONE";
    }

    @Override
    public String getEngineInfo() {
        return "Dummy engine. Always returns NO_PROXY";
    }

    @Override
    public String getPacScriptSource() {
        return "function FindProxyForURL(url, host) {\n"
           +   "    return \"DIRECT\";\n"
           +   "};";
    }
    
    
}
