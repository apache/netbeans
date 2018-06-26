/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        Node node = getNode().getAttributes().getNamedItem(NAME);
        if (node == null) {
            node = getNode().getOwnerDocument().createAttribute(NAME);
            getNode().appendChild(node);
        }
        node.setNodeValue(value);
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
