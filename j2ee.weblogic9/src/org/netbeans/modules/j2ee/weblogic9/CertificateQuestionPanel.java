/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class CertificateQuestionPanel extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(CertificateQuestionPanel.class.getName());

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Creates new form CertificatePanel
     */
    public CertificateQuestionPanel(X509Certificate cert) {
        initComponents();
        setCertificateInfo(cert);
    }


    @NbBundle.Messages({
        "LBL_Description=Description",
        "LBL_Value=Value",
        "LBL_IssuedTo=<html><b>Issued To</b></html>",
        "LBL_IssuedBy=<html><b>Issued By</b></html>",
        "LBL_SerialNumber=Serial Number",
        "LBL_CommonName=Common Name (CN)",
        "LBL_Organization=Organization (O)",
        "LBL_OrganizationalUnit=Organizational Unit (OU)",
        "LBL_ValidityPeriod=<html><b>Validity Period</b></html>",
        "LBL_BeginsOn=Begins On",
        "LBL_ExpiresOn=Expires On",
        "LBL_Fingerprints=<html><b>Fingerprints</b></html>",
        "LBL_MD5Fingerprint=MD5 Fingerprint",
        "LBL_SHA1Fingerprint=SHA-1 Fingerprint"
    })
    private void setCertificateInfo(X509Certificate cert) {
        BigInteger serialNumber = cert.getSerialNumber();
        byte[] byteValue = serialNumber.toByteArray();
        String certSerialNumber = bytesToHex(byteValue);

	String md5 = getCertFingerPrint("MD5", cert); // NOI18N
	String sha1 = getCertFingerPrint("SHA1", cert); // NOI18N

        Map<String, String> subject = getDNString(cert.getSubjectDN().toString());
        Map<String, String> issuer = getDNString(cert.getIssuerDN().toString());

        DateFormat format = DateFormat.getDateInstance();
        String[] columnNames = { Bundle.LBL_Description(), Bundle.LBL_Value() };

	Object[][] data = {
	    { Bundle.LBL_IssuedTo(), null },
            { Bundle.LBL_CommonName(),
                subject.get("CN") },  // NOI18N
            { Bundle.LBL_Organization(),
                subject.get("O") }, // NOI18N
            { Bundle.LBL_OrganizationalUnit(),
                subject.get("OU") }, // NOI18N
            { Bundle.LBL_SerialNumber(),
                certSerialNumber },

            { Bundle.LBL_IssuedBy(), null },
            { Bundle.LBL_CommonName(),
                issuer.get("CN") }, // NOI18N
            { Bundle.LBL_Organization(),
                issuer.get("O") }, // NOI18N
            { Bundle.LBL_OrganizationalUnit(),
                issuer.get("OU") }, // NOI18N

            { Bundle.LBL_ValidityPeriod(), null },
            { Bundle.LBL_BeginsOn(), format.format(cert.getNotBefore()) },
            { Bundle.LBL_ExpiresOn(), format.format(cert.getNotAfter()) },

	    { Bundle.LBL_Fingerprints(), null },
	    { Bundle.LBL_MD5Fingerprint(), md5},
	    { Bundle.LBL_SHA1Fingerprint(), sha1}
        };


	certificateTable.setModel(new DefaultTableModel(data, columnNames) {
            @Override
	    public boolean isCellEditable(int row, int col) {
		return false;
	    }
	});

        certificateTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        certificateTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        certificateTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	certificateTable.repaint();
    }

    private static Map<String, String> getDNString(String dnString) {
        int len = dnString.length();
        boolean inQuote = false;
        boolean inKey = true;

        Map<String, String> result = new HashMap<String, String>();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < len; i++) {
            char ch = dnString.charAt(i);

            if (!inQuote) {
                if (inKey && ch == '=') {
                    inKey = false;
                    continue;
                }
                if (ch == ',') {
                    result.put(key.toString().trim(), value.toString().trim());
                    key.setLength(0);
                    value.setLength(0);
                    inKey = true;
                    continue;
                }
            }

            if (ch == '\"' || ch == '\'') {
                inQuote = !inQuote;
                continue;
            }

            if (inKey) {
                key.append(ch);
            } else {
                value.append(ch);
            }
        }

        result.put(key.toString().trim(), value.toString().trim());
        return result;
    }

    private static String getCertFingerPrint(String mdAlg, X509Certificate cert) {
        try {
            byte[] encCertInfo = cert.getEncoded();
            MessageDigest md = MessageDigest.getInstance(mdAlg);
            byte[] digest = md.digest(encCertInfo);
            return bytesToHex(digest);
        } catch (CertificateEncodingException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        certificateScrollPane = new javax.swing.JScrollPane();
        certificateTable = new javax.swing.JTable();
        messageScrollPane = new javax.swing.JScrollPane();
        messageTextArea = new javax.swing.JTextArea();

        certificateScrollPane.setViewportView(certificateTable);

        messageScrollPane.setBorder(null);

        messageTextArea.setEditable(false);
        messageTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        messageTextArea.setColumns(20);
        messageTextArea.setLineWrap(true);
        messageTextArea.setText(org.openide.util.NbBundle.getMessage(CertificateQuestionPanel.class, "MSG_Explanation")); // NOI18N
        messageTextArea.setWrapStyleWord(true);
        messageScrollPane.setViewportView(messageTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(messageScrollPane)
            .addComponent(certificateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(messageScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(certificateScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane certificateScrollPane;
    private javax.swing.JTable certificateTable;
    private javax.swing.JScrollPane messageScrollPane;
    private javax.swing.JTextArea messageTextArea;
    // End of variables declaration//GEN-END:variables
}
