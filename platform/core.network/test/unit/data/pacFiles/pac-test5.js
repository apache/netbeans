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


function FindProxyForURL(url, host)
{
    var reply = "";
    if (typeof engine !== 'undefined') {
        reply += (typeof engine) + ":";
    }
    if (typeof Java !== 'undefined') {
        reply += (typeof Java) + ":";
    }
    if (typeof jsPacHelpers !== 'undefined') {
        reply += "jsPacHelpers:" + jsPacHelpers;
    }

    // Make everything lower case.
    url = url.toLowerCase();
    host = host.toLowerCase();
    if (isPlainHostName(host)) return reply + "DIRECT";
    return reply + "PROXY www-proxy.us.oracle.com:80";
}
