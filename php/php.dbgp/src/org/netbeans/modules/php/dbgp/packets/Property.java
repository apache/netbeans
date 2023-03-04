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
package org.netbeans.modules.php.dbgp.packets;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.UnsufficientValueException;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class Property extends BaseMessageChildElement {

    static final String PROPERTY = "property"; // NOI18N
    private static final String NUMCHILDREN = "numchildren"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N
    private static final String KEY = "key"; // NOI18N
    private static final String ADDRESS = "address"; // NOI18N
    private static final String PAGESIZE = "pagesize"; // NOI18N
    private static final String PAGE = "page"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String FULL_NAME = "fullname"; // NOI18N
    private static final String TYPE = "type"; // NOI18N
    private static final String CLASS_NAME = "classname"; // NOI18N
    private static final String CONSTANT = "constant"; // NOI18N
    private static final String CHILDREN = "children"; // NOI18N
    private static final String FACET = "facet"; // NOI18N
    static final String SIZE = "size"; // NOI18N

    Property(Node node) {
        super(node);
    }

    public String getName() {
        return getAttribute(NAME);
    }

    public void setName(String value) {
        Node node = getNode();
        if (node instanceof Element) {
            Element element = (Element) node;
            element.setAttribute(NAME, value);
        }
    }

    public String getFullName() {
        return getAttribute(FULL_NAME);
    }

    public String getType() {
        return getAttribute(TYPE);
    }

    public String getClassName() {
        return getAttribute(CLASS_NAME);
    }

    public boolean isConstant() {
        return getInt(CONSTANT) > 0;
    }

    public boolean hasChildren() {
        return getInt(CHILDREN) > 0;
    }

    public int getSize() {
        return getInt(SIZE);
    }

    public int getPage() {
        return getInt(PAGE);
    }

    public int getPageSize() {
        return getInt(PAGESIZE);
    }

    public int getAddress() {
        return getInt(ADDRESS);
    }

    public String getKey() {
        return getAttribute(KEY);
    }

    public String getFacet() {
        return getAttribute(FACET);
    }

    public Encoding getEncoding() {
        String enc = getAttribute(ENCODING);
        return Encoding.forString(enc);
    }

    public int getChildrenSize() {
        return getInt(NUMCHILDREN);
    }

    public List<Property> getChildren() {
        List<Node> nodes = getChildren(PROPERTY);
        List<Property> result = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            result.add(new Property(node));
        }
        return result;
    }

    public byte[] getValue() throws UnsufficientValueException {
        String value = DbgpMessage.getNodeValue(getNode());
        byte[] result;
        try {
            result = Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            result = new byte[0];
        }
        return getValue(result);
    }

    public String getStringValue() throws UnsufficientValueException {
        Encoding enc = getEncoding();
        if (Encoding.BASE64.equals(enc)) {
            Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (session != null) {
                SessionId sessionId = session.lookupFirst(null, SessionId.class);
                if (sessionId != null) {
                    DebugSession debugSession = SessionManager.getInstance().getSession(sessionId);
                    if (debugSession != null) {
                        String projectEncoding = debugSession.getOptions().getProjectEncoding();
                        try {
                            return new String(getValue(), projectEncoding);
                        } catch (UnsupportedEncodingException ex) {
                            Exceptions.printStackTrace(ex);
                            return "";
                        }
                    }
                }
            }
            return new String(getValue(), Charset.defaultCharset());
        }
        String result = DbgpMessage.getNodeValue(getNode());
        try {
            if (result != null && result.getBytes(DbgpMessage.ISO_CHARSET).length < getSize()) {
                throw new UnsufficientValueException();
            }
        } catch (UnsupportedEncodingException e) {
            assert false;
            return "";
        }
        return result;
    }

    public static boolean equals(Property one, Property two) {
        if (one == null) {
            return two == null;
        } else {
            byte[] value;
            try {
                value = one.getValue();
            } catch (UnsufficientValueException e) {
                return false;
            }
            if (two == null) {
                return false;
            }
            byte[] secondValue;
            try {
                secondValue = two.getValue();
            } catch (UnsufficientValueException e) {
                return false;
            }
            return Arrays.equals(value, secondValue);
        }
    }

    private byte[] getValue(byte[] bytes) throws UnsufficientValueException {
        if (bytes.length >= getSize()) {
            return bytes;
        } else {
            throw new UnsufficientValueException();
        }
    }

    public enum Encoding {
        BASE64,
        NONE;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

        static Encoding forString(String str) {
            Encoding[] encodings = Encoding.values();
            for (Encoding encoding : encodings) {
                if (encoding.toString().equals(str)) {
                    return encoding;
                }
            }
            return null;
        }

    }

}
