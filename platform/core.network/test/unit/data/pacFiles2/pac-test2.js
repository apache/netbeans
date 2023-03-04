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


// 
// A very simple PAC file
// Returns a list of 3 proxies.
//

function FindProxyForURL(url, host)
{




    url = url.toLowerCase();
    host = host.toLowerCase();
    alert("This is pac-test2.js");


    return "PROXY localhost:8081; PROXY localhost:8082; DIRECT";
}