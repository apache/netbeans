/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2008-2009 Sun
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
package org.netbeans.modules.editor.url;

import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class HyperlinkImpl implements HyperlinkProviderExt {
    public static final Logger LOG = Logger.getLogger(HyperlinkImpl.class.getName());

    private static final int TIME_VALID = 24 * 60 * 60 * 1000;
    private static final int TIME_INVALID = 60 * 1000;

    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
    }

    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
        return getHyperlinkSpan(doc, offset, type) != null;
    }

    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
        if (!(doc instanceof BaseDocument)) {
            return null;
        }
        
        try {
            BaseDocument bdoc = (BaseDocument) doc;
            int start = Utilities.getRowStart(bdoc, offset);
            int end = Utilities.getRowEnd(bdoc, offset);

            for (int[] span : Parser.recognizeURLs(DocumentUtilities.getText(doc, start, end - start))) {
                if (span[0] + start <= offset && offset <= span[1] + start) {
                    return new int[] {
                        span[0] + start,
                        span[1] + start
                    };
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public void performClickAction(Document doc, int offset, HyperlinkType type) {
        int[] span = getHyperlinkSpan(doc, offset, type);

        if (span == null) {
            Toolkit.getDefaultToolkit().beep();
            return ;
        }

        try {
            String urlText = doc.getText(span[0], span[1] - span[0]);
            URL url = new URL(urlText);

            URLDisplayer.getDefault().showURL(url);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(HyperlinkImpl.class, "WARN_Invalid_URL", ex.getMessage()));
            LOG.log(Level.FINE, null, ex);
        }
    }

    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
        int[] span = getHyperlinkSpan(doc, offset, type);

        if (span == null) {
            return null;
        }

        String title = getTitleImpl(doc, span);

        if (title != null && title.length() == 0) {
            title = null;
        }

        return title;
    }

    private static String getTitleImpl(Document doc, int[] span) {
        try {
            String urlText = doc.getText(span[0], span[1] - span[0]);
            URL url = new URL(urlText);

            Preferences p = NbPreferences.forModule(HyperlinkImpl.class);
            
            p = p.node("url");

            String timestampKey = url.toExternalForm() + "-timestamp";//NOI18N
            String titleKey = url.toExternalForm() + "-title";//NOI18N

            long prevTime = p.getLong(timestampKey, Long.MIN_VALUE);
            long lastModified = System.currentTimeMillis();

            if (prevTime >= lastModified) {
                return p.get(titleKey, null);
            }

            String title = readTitle(url);

            if (title == null) {
                title = p.get(titleKey, null);

                if (title != null) {
                    return title;
                }
            }

            p.putLong(timestampKey,lastModified + (title != null ? TIME_VALID : TIME_INVALID));

            if (title != null) {
                p.put( titleKey, title);
            } else {
                p.remove(titleKey);
            }

            return title;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (MalformedURLException ex) {
            LOG.log(Level.FINE, null, ex);
        }

        return null;
    }

    private static final Pattern TITLE = Pattern.compile("<title>(.*)</title>");//NOI18N
    private static final int MAX_NUM_BYTES_TO_READ = 10 * 1024;
    private static String readTitle(URL url) {
        ByteArrayOutputStream baos = null;
        InputStream ins = null;
        URLConnection c = null;
        
        try {
            c = url.openConnection();

            String encoding = null;
            try {
                encoding = c.getContentEncoding();
            } catch (Throwable ex) {
                org.netbeans.editor.Utilities.setStatusText(EditorRegistry.lastFocusedComponent(), "Invalid URL");
                return null;
            }

            if (encoding == null) {
                encoding = Parser.decodeContentType(c.getContentType());
            }

            baos = new ByteArrayOutputStream();
            ins = c.getInputStream();

            int read;
            
            int numBytesRead = 0;
            while ((read = ins.read()) != (-1) && numBytesRead < MAX_NUM_BYTES_TO_READ) {
                baos.write(read);
                numBytesRead++;
            }

            ins.close();
            baos.close();

            String content = new String(baos.toByteArray(), encoding != null ? encoding : Charset.defaultCharset().name());
            Matcher m = TITLE.matcher(content);

            if (m.find()) {
                return m.group(1).trim();
            } else {
                return "";
            }
        } catch (IOException ex) {
            LOG.log(Level.FINE, url != null ? url.toString() : null, ex);
        } catch (IllegalArgumentException iae) {
            // #198333
            LOG.log(Level.FINE, url != null ? url.toString() : null, iae);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        return null;
    }
}
