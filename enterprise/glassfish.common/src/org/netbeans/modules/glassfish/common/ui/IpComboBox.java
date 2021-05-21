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
package org.netbeans.modules.glassfish.common.ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.modules.glassfish.tooling.utils.NetUtils;

/**
 * Combo box to select IP address.
 * <p/>
 * @author Tomas Kraus
 */
public class IpComboBox extends JComboBox<IpComboBox.InetAddr> {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**  <code>127.0.0.1</code> host name. */
    public static final String IP_4_127_0_0_1_NAME = "localhost";

    /** RAW byte sequence for <code>127.0.0.1</code>. */
    private static final byte[] IP_4_127_0_0_1 = initIp127_0_0_1();

    /** Comparator for <code>InetAddr</code> instances to be sorted
     *  in combo box. */
    private static final IpComboBox.InetAddrComparator ipComparator
            = new IpComboBox.InetAddrComparator();

    /** Exception message for disabled constructors. */
    private static final String CONSTRUCTOR_EXCEPTION_MSG =
            "Data model for a combo box shall not be supplied in constructor.";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * RAW byte sequence for <code>127.0.0.1</code> address initializer.
     * <p/>
     * Helper method used to initialize {@link IP_4_127_0_0_1} constant value.
     * <p/>
     * @return RAW byte sequence for <code>127.0.0.1</code> address
     */
    private static byte[] initIp127_0_0_1() {
        try {
            return InetAddress.getByName("127.0.0.1").getAddress();
        // This shall be unreachable.
        } catch (UnknownHostException uhe) {
            throw new IllegalStateException(
                    "Could not initialize raw byte sequence for 127.0.0.1");
        }
    }

    /**
     * Verify if provided IP address represents <code>127.0.0.1</code>.
     * <p/>
     * @returns Value of <code>true</code> when provided IP address represents
     *          <code>127.0.0.1</code> or <code>false</code> otherwise.
     */
    public static boolean isLocalhost(InetAddress ip) {
        byte[] ipBytes = ip.getAddress();
        boolean result = IP_4_127_0_0_1.length == ipBytes.length;
        for (byte i = 0; result && i < ip.getAddress().length; i++)
            result = IP_4_127_0_0_1[i] == ipBytes[i];
        return result;
    }
    /**
     * Count number of loop back IP addresses in provided (@link Set).
     * <p/>
     * @param ips (@link Set) of IP addresses to analyze. Shall not be null.
     * @return Number of loop back IP addresses in provided (@link Set).
     */
    private static int loopBackCount(final Set<? extends InetAddress> ips) {
        int count = 0;
        for (InetAddress ip : ips) {
            if (ip.isLoopbackAddress()) {
                count++;
            }
        }
        return count;
    }

    // TODO: Currently default selection is marked in returned array only and
    // component does not know about index id this default value. Setting
    // default value as selected requires to iterate over model array.
    /**
     * Convert array of {@see InetAddress} objects to array of {@see InetAddr}
     * objects.
     * <p/>
     * @param ipsIn An array of {@see InetAddress} objects to be converted.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     * @return An array of {@see InetAddr} objects containing
     *         <code>ipsIn</code>.
     */
    private static IpComboBox.InetAddr[] toInetAddr(
            final Set<? extends InetAddress> ipsIn, final boolean lopbackOnly) {
        int size = ipsIn == null ? 0
                : lopbackOnly ? loopBackCount(ipsIn) : ipsIn.size();
        IpComboBox.InetAddr[] ipsOut = new IpComboBox.InetAddr[size];
        int i = 0;
        for (InetAddress ip : ipsIn) {
            if (!lopbackOnly || ip.isLoopbackAddress()) {
                ipsOut[i++] = new IpComboBox.InetAddr(ip, false);
            }
        }
        Arrays.sort(ipsOut, ipComparator);
        // Set default IP to 1st loopback address available.
        boolean gotDefault = false;
        for (int j = 0; j < ipsOut.length; j++) {
            if (ipsOut[j].getIp().isLoopbackAddress()) {
                ipsOut[j].def = true;
                gotDefault = true;
                break;
            }
        }
        // Set default IP to 1st address in the list when no loopback address
        // was fiound.
        if (!gotDefault && ipsOut.length > 0) {
            ipsOut[0].def = true;
        }
        return ipsOut;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @param comboBoxModel Data model for this combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     */
    @Deprecated
    public IpComboBox(final ComboBoxModel comboBoxModel)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @param items An array of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     */
    @Deprecated
    public IpComboBox(final Object items[])
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Default {@see JComboBox} constructor is disabled because it's content
     * is retrieved from an array of {@see InetAddr}.
     * <p/>
     * @param items {@see Vector} of objects to insert into the combo box.
     * @throws UnsupportedOperationException is thrown any time
     *         this constructor is called.
     * @deprecated Use {@see #JavaPlatformsComboBox()}
     *             or {@see #JavaPlatformsComboBox(InetAddr[])} instead.
     */
    @Deprecated
    public IpComboBox(final Vector<?> items)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(CONSTRUCTOR_EXCEPTION_MSG);
    }

    /**
     * Creates an instance of <code>IpComboBox</code> that contains
     * all IP addresses of this host.
     * <p/>
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public IpComboBox(final boolean lopbackOnly) {
        super(new DefaultComboBoxModel(
                toInetAddr(NetUtils.getHostIP4s(), lopbackOnly)));
    }

    /**
     * Creates an instance of <code>IpComboBox</code> that contains
     * supplied list of IP addresses.
     * <p/>
     * @param ips IP addresses to be set as data model for combo box.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public IpComboBox(final Set<? extends InetAddress> ips,
            final boolean lopbackOnly) {
        super(new DefaultComboBoxModel(toInetAddr(ips, lopbackOnly)));
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Update content of data model to contain all IP addresses of this host.
     * <p/>
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public void updateModel(final boolean lopbackOnly) {
        setModel(new DefaultComboBoxModel(
                toInetAddr(NetUtils.getHostIP4s(), lopbackOnly)));
    }

    /**
     * Update content of data model to contain supplied list
     * of IP addresses.
     * <p/>
     * @param ips IP addresses to be set as data model for combo box.
     * @param lopbackOnly Convert loop back addresses only when
     *                    <code>true</code>. 
     */
    public void updateModel(final Set<? extends InetAddress> ips,
            final boolean lopbackOnly) {
        setModel(new DefaultComboBoxModel(toInetAddr(ips, lopbackOnly)));
    }

    /**
     * Get selected item from the combo box display area as IP address.
     * <p/>
     * User modified content 
     * <p/>
     * @return Selected item from the combo box display area as IP address
     *         or <code>null</code> when there is no selected IP address.
     */
    public InetAddress getSelectedIp() {
        Object item = getSelectedItem();
        if (item instanceof InetAddr) {
            return ((InetAddr)item).getIp();
        } else if (item instanceof String) {
            InetAddress ip;
            try {
                ip = InetAddress.getByName(((String)item).trim());
            } catch (UnknownHostException | SecurityException ex) {
                ip = null;
            }
            return ip;
        }
        return null;
    }

    /**
     * Get selected item from the combo box display area.
     * <p/>
     * @return Selected item from the combo box display area.
     */
    @Override
    public Object getSelectedItem() {
        Object item = super.getSelectedItem();
        if ((item instanceof InetAddr)
                || (item instanceof String) || item == null) {
            return item;
        } else {
            throw new IllegalStateException("Selected item is not IP adress");
        }
    }

    /**
     * Set selected item in the combo box display area to the provided
     * IP address.
     * <p/>
     * @param ip IP address to be set as selected. Default IP address
     *           will be used when provided address is not found.
     */
    public void setSelectedIp(final InetAddress ip) {
        int i, count = dataModel.getSize();
        int defaultIndex = -1;
        boolean isSelectedSet = false;
        for (i = 0; i < count; i++) {
            InetAddr element = dataModel.getElementAt(i);
            // Passed IP address has highest priority.
            if (ip.equals(element.getIp())) {
                super.setSelectedItem(element);
                isSelectedSet = true;
                break;
            // Searching for default item in parallel.
            } else if (element.isDefault()) {
                defaultIndex = i;
            }
        }
        // Set default or first available when passed IP was noit found.
        if (!isSelectedSet && count > 0) {
            super.setSelectedItem(dataModel.getElementAt(
                    defaultIndex >= 0 ? defaultIndex : 0));
        }
    }

    /**
     * Set selected item in the combo box display area to the provided
     * IP address.
     * <p/>
     * Expecting values from previous states of combo box which can be<ul>
     * <li><code>null</code> to select default value</li>
     * <li>or <code>String</code> when combo box content was modified
     *     by user</li>
     * <li>or <code>InetAddr</code> and <code>InetAddress</code> values
     * containing previous selections.</li></ul>
     * <p/>
     * @param ip IP address to be set as selected. Default IP address
     *           will be used when <code>null</code> value is supplied.
     */
    @Override
    public void setSelectedItem(Object ip) {
        // String means user modified value. Do not rewrite editor content.
        if (ip instanceof String) {
            return;
        }
        // Select default value for null.
        if (ip == null) {
            int i, count = dataModel.getSize();
            for (i = 0; i < count; i++) {
                if (dataModel.getElementAt(i).isDefault()) {
                    super.setSelectedItem(dataModel.getElementAt(i));
                    break;
                }
            }
        }
        // Select IP address from list .
        if (ip instanceof InetAddr) {
            setSelectedIp(((InetAddr)ip).getIp());
        }
        // Select IP address from list.
        if (ip instanceof InetAddress) {
            setSelectedIp((InetAddress)ip);
        // Pass unknown instance to parrent method.
        } else {
            super.setSelectedItem(ip);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes                                                          //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Encapsulate {@see InetAddress} object and provide human readable
     * <code>toString()</code> output for combo box.
     */
    public static class InetAddr {

        /** IP address reference. */
        private final InetAddress ip;

        /** Mark default IP address. */
        private boolean def;

        /**
         * Creates an instance of <code>InetAddr</code> object and sets provided
         * {@see InetAddress} reference.
         * <p/>
         * @param addr IP address reference.
         */
        InetAddr(final InetAddress ip, final boolean def) {
            this.ip = ip;
            this.def = def;
        }

        /**
         * Get IP address reference.
         * <p/>
         * @return IP address reference.
         */
        public InetAddress getIp() {
            return ip;
        }

        /**
         * Get {@see String} representation of this object.
         * <p/>
         * @return {@see String} representation of this object.
         */
        @Override
        public String toString() {
            return isLocalhost(ip) ? IP_4_127_0_0_1_NAME : ip.getHostAddress();
        }

        /**
         * Check if this platform is the default platform.
         * <p/>
         * @return Value of <code>true</code> if this platform is the default
         *         platform or <code>false</code> otherwise.
         */
        public boolean isDefault() {
            return def;
        }

    }

    /**
     * Comparator for <code>InetAddr</code> instances to be sorted in combo box.
     */
    public static class InetAddrComparator implements Comparator<InetAddr> {

        /** Comparator for {@link InetAddress} instances to be sorted. */
        private static final NetUtils.InetAddressComparator
                INET_ADDRESS_COMPARATOR = new NetUtils.InetAddressComparator();

        /**
         * Compares values of <code>InetAddr</code> instances.
         * <p/>
         * @param ip1 First <code>InetAddr</code> instance to be compared.
         * @param ip2 Second <code>InetAddr</code> instance to be compared.
         * @return A negative integer, zero, or a positive integer as the first
         *         argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(final InetAddr ip1, final InetAddr ip2) {
            return INET_ADDRESS_COMPARATOR.compare(ip1.getIp(), ip2.getIp()); 
        }

    }

}
