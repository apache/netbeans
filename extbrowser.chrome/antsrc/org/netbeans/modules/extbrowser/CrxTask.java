/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.extbrowser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.apache.tools.ant.BuildException;

/**
 * Ant task that builds CRX file (i.e., Chrome extension) from the specified
 * ZIP file.
 *
 * @author Jan Stola
 */
public class CrxTask {
    /** ZIP file from which the extension should be built. */
    private File src;
    /** File where the built extension should be stored. */
    private File destfile;
    /** Public key stored in the extension. */
    private File publickey;
    /** Private key used to sign the extension. */
    private File privatekey;

    /**
     * Sets the ZIP file from which the extension should be built.
     * 
     * @param src ZIP file from which the extension should be built.
     */
    public void setSrc(File src) {
        this.src = src;
    }

    /**
     * Sets the public key/certificate stored in the extension.
     * 
     * @param publickey public key/certificate stored in the extension.
     */
    public void setPublickey(File publickey) {
        this.publickey = publickey;
    }

    /**
     * Sets the private key used to sign the extension.
     * 
     * @param privatekey private key used to sign the extension.
     */
    public void setPrivatekey(File privatekey) {
        this.privatekey = privatekey;
    }

    /**
     * Sets the file where the built extension should be stored.
     * 
     * @param destfile file where the built extension should be stored.
     */
    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }

    /**
     * Returns the file where the built extension should be stored.
     * 
     * @return file where the build extension should be stored.
     */
    private File getDestfile() {
        if (destfile == null) {
            String zipName = src.getName();
            int index = zipName.lastIndexOf('.');
            String prefix = (index == -1) ? zipName : zipName.substring(0, index);
            String crxName = prefix + ".crx"; // NOI18N
            destfile = new File(src.getParent(), crxName);
        }
        return destfile;
    }

    /**
     * Executes this task, i.e., builds the CRX file.
     */
    public void execute() {
        if (src == null) {
            throw new BuildException("Zip file (src attribute) is not set!");
        }
        if (privatekey == null) {
            throw new BuildException("Private key is not set!");
        }
        if (publickey == null) {
            throw new BuildException("Public key is not set!");
        }
        try {
            // Get all necessary components
            byte[] zipBytes = readFile(src);
            byte[] privateKeyBytes = pemToDer(readFile(privatekey));
            byte[] publicKeyBytes = pemToDer(readFile(publickey));
            PrivateKey key = createPrivateKey(privateKeyBytes);
            byte[] signature = signature(zipBytes, key);

            // Write the content of the CRX file
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            FileOutputStream output = new FileOutputStream(getDestfile());
            output.write("Cr24".getBytes()); // NOI18N
            output.write(bb.putInt(0,2).array());
            output.write(bb.putInt(0,publicKeyBytes.length).array());
            output.write(bb.putInt(0,signature.length).array());
            output.write(publicKeyBytes);
            output.write(signature);
            output.write(zipBytes);
            output.close();
        } catch (IOException ioex) {
            throw new BuildException(ioex);
        } catch (GeneralSecurityException gsex) {
            throw new BuildException(gsex);
        }
    }

    /**
     * Calculates signature of the given bytes using the given key. 
     * 
     * @param bytesToSign bytes to sign.
     * @param key private key used for signing.
     * @return signature of the given bytes using the given key.
     * @throws GeneralSecurityException when the signature cannot be calculated.
     */
    private byte[] signature(byte[] bytesToSign, PrivateKey key) throws GeneralSecurityException {
        Signature signature = Signature.getInstance("SHA1withRSA"); // NOI18N
        signature.initSign(key);
        signature.update(bytesToSign);
        return signature.sign();
    }

    /**
     * Reads the content of the file and returns it as an array of bytes.
     * 
     * @param file file to read.
     * @return content of the file as an array of bytes.
     * @throws IOException when there is some problem with reading of the file.
     */
    private byte[] readFile(File file) throws IOException {
        int fileLength = (int)file.length();
        byte[] bytes = new byte[fileLength];
        DataInputStream input = new DataInputStream(new FileInputStream(file));
        input.readFully(bytes);
        input.close();
        return bytes;
    }

    /**
     * Converts the key/certificate from PEM to DER format.
     * In other words, it removes -----BEGIN CERTIFICATE----- header and
     * -----END CERTIFICATE----- footer and decodes the resulting
     * BASE64-encoded string.
     * 
     * @param bytes key/certificate in PEM format.
     * @return key/certificate in DER format.
     */
    private byte[] pemToDer(byte[] bytes) {
        String pem = new String(bytes).trim();
        int start = pem.indexOf('\n');
        int end = pem.lastIndexOf('\n');
        String body = pem.substring(start+1,end);
        return Base64.getMimeDecoder().decode(body);
    }

    /**
     * Creates a private key from its DER encoding.
     * 
     * @param der DER encoding of the private key.
     * @return private key corresponding to the given byte array.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    private PrivateKey createPrivateKey(byte[] der) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("RSA"); // NOI18N
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        return factory.generatePrivate(spec);
    }

}
