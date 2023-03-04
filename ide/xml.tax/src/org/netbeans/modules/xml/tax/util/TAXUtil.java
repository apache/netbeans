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
package org.netbeans.modules.xml.tax.util;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import javax.swing.SwingUtilities;

import org.openide.xml.XMLUtil;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import org.netbeans.tax.*;
import org.netbeans.tax.io.TreeStreamResult;
import org.netbeans.tax.io.TreeWriter;

/**
 *
 * @author Libor Kramolis
 */
public final class TAXUtil {

    /**
     * Try to set new value to the attribute. Method <code>XMLUtil.toAttributeValue</code> is used
     * to convert value to correct attribute value.
     *
     * @see org.openide.xml.XMLUtil#toAttributeValue
     */
    public static void setAttributeValue (TreeAttribute attribute, String value) throws TreeException {
        try {
            attribute.setValue (XMLUtil.toAttributeValue (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    
    /**
     * Try to set new value to the text. Method <code>XMLUtil.toElementContent</code> is used
     * to convert value to correct element content.
     *
     * @see org.openide.xml.XMLUtil#toElementContent
     */
    public static void setTextData (TreeText text, String value) throws TreeException {
        try {
            text.setData (XMLUtil.toElementContent (value));
        } catch (CharConversionException exc) {
            throw new TreeException (exc);
        }
    }
    


    /**
     */
    public static void notifyWarning (final String message) {
        SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    NotifyDescriptor nd = new NotifyDescriptor.Message
                        (message, NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault ().notify (nd);
                }
            });
    }

    /**
     */
    public static String printableValue (String value) {
        if (value == null)
            return "<null>"; // NOI18N
        
        int ch;
        int MAX_LENGTH = 33;
        int len = Math.min (value.length (), MAX_LENGTH);
        
        StringBuffer sb = new StringBuffer (2 * len);
        for (int i = 0; i < len; i++) {
            ch = value.charAt (i);
            if ('\r' == ch) {
                sb.append ("\\r"); // NOI18N
            } else if ('\n' == ch) {
                sb.append ("\\n"); // NOI18N
            } else if ('\t' == ch) {
                sb.append ("\\t"); // NOI18N
            } else if ('\b' == ch) {
                sb.append ("\\b"); // NOI18N
            } else if ('\f' == ch) {
                sb.append ("\\f"); // NOI18N
            } else {
                sb.append ((char)ch);
            }
        }
        if (value.length () > len)
            sb.append ("..."); // NOI18N
        
        return sb.toString ();
    }

    /**
     */
    public static void notifyTreeException (TreeException exc) {
        notifyWarning (exc.getMessage());
    }

    /*
     *
     */
    public static String treeToString(TreeDocumentRoot doc) throws IOException {

        StringWriter out = new StringWriter();
        TreeStreamResult result = new TreeStreamResult(out);
        TreeWriter writer = result.getWriter(doc);

        try {
            writer.writeDocument();
            return out.toString();
        } catch (TreeException ex) {
            throw new IOException("Cannot read tree " +  ex.getMessage()); // NOI18N

        } finally {
            try {
                out.close();
            } catch (IOException ioex) {
                // do not know
            }
        }

    }

    public static byte[] treeToByteArray(TreeDocumentRoot doc) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(1024 * 8);
        TreeStreamResult result = new TreeStreamResult(out);
        TreeWriter writer = result.getWriter(doc);

        try {
            writer.writeDocument();
            byte[] array = out.toByteArray();
            return array;
        } catch (TreeException ex) {
            throw new IOException("Cannot read tree " +  ex.getMessage()); // NOI18N

        } finally {
            try {
                out.close();
            } catch (IOException ioex) {
                // do not know
            }
        }
    }

}
