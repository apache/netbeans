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
package org.netbeans.core.network.utils;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import sun.net.spi.nameservice.NameService;

/**
 * A simple In-Memory implementation of a Name Service for Java. This
 * is useful for unit tests.
 * 
 * <p>
 * Uses dirty tricks (reflection) and classes from sun.* package. For unit
 * testing this is acceptable. For anything else: no!
 * 
 * <p>
 * Credit: The code is inspired by similar approach in Apache Kudo project. 
 * 
 * @author lbruun
 */
public class FakeDns extends IpAddressUtils {


    private final Map<String, InetAddress[]> forwardResolutions = new ConcurrentHashMap<>();
    private final Map<InetAddress, String> reverseResolutions = new ConcurrentHashMap<>();
    private final List<NameService> orgNameServices = new ArrayList<>();   
    private volatile List<NameService> installed = null;
    private final AtomicLong delayAnsweringByMs = new AtomicLong(0);

    public FakeDns() {
    }


    /**
     * Adds a hostname-to-address(es) mapping. This will also add the
     * corresponding reverse mapping(s).
     * @param hostname
     * @param ipAdresses
     */
    public void addForwardResolution(String hostname, InetAddress[] ipAdresses) {
        forwardResolutions.put(hostname, ipAdresses);
        for(InetAddress a : ipAdresses) {
            reverseResolutions.put(a, hostname);
        }
    }

    /**
     * Adds an address-to-hostname mapping. The 
     * {@link #addForwardResolution(java.net.InetAddress, java.lang.String) addReverseResolution()}
     * method will automatically add reverse mappings too, so this method is only
     * necessary if you want to add a reverse mapping for which no forward mapping
     * exist, i.e. a "standalone" reverse mapping.
     * 
     * @param ipAddress
     * @param hostname
     */
    public void addReverseResolution(InetAddress ipAddress, String hostname) {
        reverseResolutions.put(ipAddress, hostname);
    }

    /**
     * Install the fake DNS resolver into the Java runtime.
     * 
     * <p>
     * In Java, name services are chained. So if one name service doesn't 
     * respond (or responds with {@code UnknownHostException}) then the next
     * name service in the chain will be queried. If you want to do negative
     * tests, i.e. "this should give me UnknownHostException", then you'll have
     * to remove other name services too, so that only the FakeDns name service
     * is installed. You do this by setting {@code removeOthers} to {@code true}.
     * 
     * @param removeOthers if other name services should be removed
     */
    public synchronized void install(boolean removeOthers) {
        if (installed != null) {
            return;
        }
        List<NameService> nameServices = getKnownNameServices();
        if (nameServices != null) {

            if (removeOthers) {
                orgNameServices.clear();
                orgNameServices.addAll(nameServices);
                nameServices.clear();
            }
            // Install ourselves in position 0, ahead of everyone else
            nameServices.add(0, new NameServiceInMemory());

            installed = nameServices;
        } else {
            installed = Collections.singletonList(new NameServiceInMemory());
        }
    }

    /**
     * Puts Java's internal name service chain back to its original state.
     * Clears any forward or reverse mappings that has been put into the
     * FakeDns.
     */
    public synchronized void unInstall() {
        if (installed == null) {
            return;
        }
        List<NameService> nameServices = getKnownNameServices();
        if (nameServices != null) {

            NameService ns = nameServices.get(0);
            if (ns != null) {
                if (ns instanceof NameServiceInMemory) {
                    nameServices.remove(0);
                    installed = null;
                }
            }
            
            if (!orgNameServices.isEmpty()) {
                nameServices.addAll(orgNameServices);
                orgNameServices.clear();
            }
        }
        forwardResolutions.clear();
        reverseResolutions.clear();
    }

    /**
     * Sets a delay on answering, thereby simulating a DNS server which
     * is slow to respond.
     * 
     * <p>
     * Set to 0 (zero) to remove the delay again.
     * 
     * <p>
     * Beware that Java also has a name service cache, so our FakeDns name
     * service may not be queried at all as a we expect. Therefore, when 
     * testing for a delayed response it is best to it on a newly 
     * inserted mapping.
     * 
     * @param msWait milliseconds to wait before answering to calls
     */
    public void delayAnsweringBy(long msWait ) {
        delayAnsweringByMs.set(msWait);
    }
    
    private List<NameService> getKnownNameServices() {
        try {
            Field field = InetAddress.class.getDeclaredField("nameServices");
            field.setAccessible(true);
            return (List<NameService>) field.get(null);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            //throw Throwables.propagate(e);
        }
        return null;
    }

    @Override
    Callable<InetAddress[]> createDnsTimeoutTask(String host) {
        return () -> {
            List<NameService> namedServices = installed;
            if (namedServices == null) {
                return null;
            }
            for (NameService ns : namedServices) {
                InetAddress[] result = ns.lookupAllHostAddr(host);
                if (result != null) {
                    return result;
                }
            }
            return null;
        };
    }
    private class NameServiceInMemory implements NameService {

        @Override
        public InetAddress[] lookupAllHostAddr(String host)
                throws UnknownHostException {
            sleep();
            InetAddress[] inetAddresses = forwardResolutions.get(host);
            if (inetAddresses != null) {
                return inetAddresses;
            }

            // Since Java 7, JDK chains to the next name servicer provider 
            // automatically, so when we throw UnknownHostException, the 
            // resolution will not actually stop (provided there are more name 
            // service implementations in the chain), instead it will continue
            // to the next in the chain. This means that our FakeDns cannot 
            // be used to test cases where we want to test for a negative
            throw new UnknownHostException();
        }

        @Override
        public String getHostByAddr(byte[] addr) throws UnknownHostException {
            sleep();
            String hostname = reverseResolutions.get(InetAddress.getByAddress(addr));
            if (hostname != null) {
                return hostname;
            }

            throw new UnknownHostException();
        }
        
        private void sleep() {
            final long delay = delayAnsweringByMs.get();
            if (delay != 0) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
