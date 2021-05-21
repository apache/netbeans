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

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Stolen from core by vkraemer.
 * 
 * @author Jiri Rechtacek
 */
public final class NbProxySelector extends ProxySelector {
    
    private ProxySelector original = null;
    private static Logger log = Logger.getLogger (NbProxySelector.class.getName ());
    private static Object useSystemProxies;
        
    /** Creates a new instance of NbProxySelector */
    public NbProxySelector () {
        original = ProxySelector.getDefault ();
        if (original == null) {
            log.warning ("No default system ProxySelector was found thus NetBeans ProxySelector won't delegate on it");
        } else {
            log.fine ("Override the original ProxySelector: " + original);
        }
        log.fine ("java.net.useSystemProxies has been set to " + useSystemProxies ());
        log.fine ("In launcher was detected netbeans.system_http_proxy: " + System.getProperty ("netbeans.system_http_proxy", "N/A"));
        log.fine ("In launcher was detected netbeans.system_socks_proxy: " + System.getProperty ("netbeans.system_socks_proxy", "N/A"));
        ProxySettings.addPreferenceChangeListener (new ProxySettingsListener ());
        copySettingsToSystem ();
    }
    
    public List<Proxy> select(URI uri) {
        List<Proxy> res = new ArrayList<Proxy> ();
        int proxyType = ProxySettings.getProxyType ();
        if (ProxySettings.DIRECT_CONNECTION == proxyType) {
            res = Collections.singletonList (Proxy.NO_PROXY);
        } else if (ProxySettings.AUTO_DETECT_PROXY == proxyType) {
            if (useSystemProxies ()) {
                if (original != null) {
                    res = original.select (uri);                   
                }
            } else {
                String protocol = uri.getScheme ();
                assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
                if (dontUseProxy (ProxySettings.SystemProxySettings.getNonProxyHosts (), uri.getHost ())) {
                    res.add (Proxy.NO_PROXY);
                }
                if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                    String ports = ProxySettings.SystemProxySettings.getHttpPort ();
                    if (ports != null && ports.length () > 0 && ProxySettings.SystemProxySettings.getHttpHost ().length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (ProxySettings.SystemProxySettings.getHttpHost (), porti));
                        res.add (p);
                    }
                } else { // supposed SOCKS
                    String ports = ProxySettings.SystemProxySettings.getSocksPort ();
                    String hosts = ProxySettings.SystemProxySettings.getSocksHost ();
                    if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                        int porti = Integer.parseInt(ports);
                        Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                        res.add (p);
                    }
                }
                if (original != null) {
                    res.addAll (original.select (uri));
                }
            }
        } else if (ProxySettings.MANUAL_SET_PROXY == proxyType) {
            String protocol = uri.getScheme ();
            assert protocol != null : "Invalid scheme of uri " + uri + ". Scheme cannot be null!";
            
            // handling nonProxyHosts first
            if (dontUseProxy (ProxySettings.getNonProxyHosts (), uri.getHost ())) {
                res.add (Proxy.NO_PROXY);
            }
            if (protocol.toLowerCase (Locale.US).startsWith("http")) {
                String hosts = ProxySettings.getHttpHost ();
                String ports = ProxySettings.getHttpPort ();
                if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                    int porti = Integer.parseInt(ports);
                    Proxy p = new Proxy (Proxy.Type.HTTP,  new InetSocketAddress (hosts, porti));
                    res.add (p);
                } else {
                    log.info ("Incomplete HTTP Proxy [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                    if (original != null) {
                        log.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                        res.addAll (original.select (uri));
                    }
                }
            } else { // supposed SOCKS
                String ports = ProxySettings.getSocksPort ();
                String hosts = ProxySettings.getSocksHost ();
                if (ports != null && ports.length () > 0 && hosts.length () > 0) {
                    int porti = Integer.parseInt(ports);
                    Proxy p = new Proxy (Proxy.Type.SOCKS,  new InetSocketAddress (hosts, porti));
                    res.add (p);
                } else {
                    log.info ("Incomplete SOCKS Server [" + hosts + "/" + ports + "] found in ProxySelector[Type: " + ProxySettings.getProxyType () + "] for uri " + uri + ". ");
                    if (original != null) {
                        log.finest ("Fallback to the default ProxySelector which returns " + original.select (uri));
                        res.addAll (original.select (uri));
                    }
                }
            }
            res.add (Proxy.NO_PROXY);
        } else {
            assert false : "Invalid proxy type: " + ProxySettings.getProxyType ();
        }
        log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () +
                ", Use HTTP for all protocols: " + ProxySettings.useProxyAllProtocols ()+
                "] returns " + res + " for URI " + uri);
        return res;
    }
    
    public void connectFailed (URI arg0, SocketAddress arg1, IOException arg2) {
        log.log  (Level.INFO, "connectionFailed(" + arg0 + ", " + arg1 +")", arg2);
    }
    
    // several modules listenes on these properties and propagates it futher
    private class ProxySettingsListener implements PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt.getKey ().startsWith ("proxy") || evt.getKey ().startsWith ("useProxy")) {
                copySettingsToSystem ();
            }
        }
    }
    
    private void copySettingsToSystem () {
        String host = null, port = null, nonProxyHosts = null;
        String sHost = null, sPort = null;
        String httpsHost = null, httpsPort = null;
        int proxyType = ProxySettings.getProxyType ();
        if (ProxySettings.DIRECT_CONNECTION == proxyType) {
            host = null;
            port = null;
            httpsHost = null;
            httpsPort = null;
            nonProxyHosts = null;
            sHost = null;
            sPort = null;
        } else if (ProxySettings.AUTO_DETECT_PROXY == proxyType) {
            host = ProxySettings.SystemProxySettings.getHttpHost ();
            port = ProxySettings.SystemProxySettings.getHttpPort ();
            httpsHost = ProxySettings.SystemProxySettings.getHttpsHost ();
            httpsPort = ProxySettings.SystemProxySettings.getHttpsPort ();
            nonProxyHosts = ProxySettings.SystemProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.SystemProxySettings.getSocksHost ();
            sPort = ProxySettings.SystemProxySettings.getSocksPort ();
        } else if (ProxySettings.MANUAL_SET_PROXY == proxyType) {
            host = ProxySettings.getHttpHost ();
            port = ProxySettings.getHttpPort ();
            httpsHost = ProxySettings.getHttpsHost ();
            httpsPort = ProxySettings.getHttpsPort ();
            nonProxyHosts = ProxySettings.getNonProxyHosts ();
            sHost = ProxySettings.getSocksHost ();
            sPort = ProxySettings.getSocksPort ();
        } else {
            assert false : "Invalid proxy type: " + proxyType;
        }
        setOrClearProperty ("http.proxyHost", host, false);
        setOrClearProperty ("http.proxyPort", port, true);
        setOrClearProperty ("http.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("https.proxyHost", httpsHost, false);
        setOrClearProperty ("https.proxyPort", httpsPort, true);
        setOrClearProperty ("https.nonProxyHosts", nonProxyHosts, false);
        setOrClearProperty ("socksProxyHost", sHost, false);
        setOrClearProperty ("socksProxyPort", sPort, true);
        log.fine ("Set System's http.proxyHost/Port/NonProxyHost to " + host + "/" + port + "/" + nonProxyHosts);
        log.fine ("Set System's https.proxyHost/Port to " + httpsHost + "/" + httpsPort);
        log.fine ("Set System's socksProxyHost/Port to " + sHost + "/" + sPort);
    }
    
    private void setOrClearProperty (String key, String value, boolean isInteger) {
        assert key != null;
        if (value == null || value.length () == 0) {
            System.clearProperty (key);
        } else {
            if (isInteger) {
                try {
                    Integer.parseInt (value);
                } catch (NumberFormatException nfe) {
                    log.log (Level.INFO, nfe.getMessage(), nfe);
                }
            }
            System.setProperty (key, value);
        }
    }

    // package-private for unit-testing
    static boolean dontUseProxy (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        // try IP adress first
        if (dontUseIp (nonProxyHosts, host)) {
            return true;
        } else {
            return dontUseHostName (nonProxyHosts, host);
        }

    }
    
    private static boolean dontUseHostName (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String token = st.nextToken ();
            int star = token.indexOf ("*");
            if (star == -1) {
                dontUseProxy = token.equals (host);
                if (dontUseProxy) {
                    log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);                    
                }
            } else {
                String start = token.substring (0, star - 1 < 0 ? 0 : star - 1);
                String end = token.substring (star + 1 > token.length () ? token.length () : star + 1);
                dontUseProxy = host.startsWith(start) && host.endsWith(end);
                if (dontUseProxy) {
                    log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host " + host + " found in nonProxyHosts: " + nonProxyHosts);                    
                }
            }
        }
        return dontUseProxy;
    }
    
    private static boolean dontUseIp (String nonProxyHosts, String host) {
        if (host == null) return false;
        
        String ip = null;
        try {
            ip = InetAddress.getByName (host).getHostAddress ();
        } catch (UnknownHostException ex) {
            log.log (Level.FINE, ex.getLocalizedMessage (), ex);
        }
        
        if (ip == null) {
            return false;
        }

        boolean dontUseProxy = false;
        StringTokenizer st = new StringTokenizer (nonProxyHosts, "|", false);
        while (st.hasMoreTokens () && !dontUseProxy) {
            String nonProxyHost = st.nextToken ();
            int star = nonProxyHost.indexOf ("*");
            if (star == -1) {
                dontUseProxy = nonProxyHost.equals (ip);
                if (dontUseProxy) {
                    log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host's IP " + ip + " found in nonProxyHosts: " + nonProxyHosts);
                }
            } else {
                // match with given dotted-quad IP
                try {
                    dontUseProxy = Pattern.matches (nonProxyHost, ip);
                    if (dontUseProxy) {
                        log.finest ("NbProxySelector[Type: " + ProxySettings.getProxyType () + "]. Host's IP" + ip + " found in nonProxyHosts: " + nonProxyHosts);
                    }
                } catch (PatternSyntaxException pse) {
                    // may ignore it here
                }
            }
        }
        return dontUseProxy;
    }
    
    // NetProperties is JDK vendor specific, access only by reflection
    static boolean useSystemProxies () {
        if (useSystemProxies == null) {
            try {
                Class<?> clazz = Class.forName ("sun.net.NetProperties");
                Method getBoolean = clazz.getMethod ("getBoolean", String.class);
                useSystemProxies = getBoolean.invoke (null, "java.net.useSystemProxies");
            } catch (Exception x) {
                log.log (Level.FINEST, "Cannot get value of java.net.useSystemProxies bacause " + x.getMessage(), x);
            }
        }
        return useSystemProxies != null && "true".equalsIgnoreCase (useSystemProxies.toString ());
    }
}
