/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seembedded.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seembedded.platform.ConnectionMethod;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatform;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatformProbe;
import org.netbeans.modules.java.j2seembedded.platform.RemotePlatformProvider;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 * @author Roman Svitanic
 */
class RemotePlatformIt implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    public static final String PROP_DISPLAYNAME = "displayName"; //NOI18N
    public static final String PROP_HOST = "host"; //NOI18N
    public static final String PROP_PORT = "port"; //NOI18N
    public static final String PROP_USERNAME = "username"; //NOI18N
    public static final String PROP_PASSWORD = "password"; //NOI18N
    public static final String PROP_KEYFILE = "keyfile"; //NOI18N
    public static final String PROP_PASSPHRASE = "passphrase"; //NOI18N
    public static final String PROP_JREPATH = "jrePath"; //NOI18N
    public static final String PROP_WORKINGDIR = "workingDir"; //NOI18N
    public static final String PROP_SYS_PROPERTIES = "sysProperties";   //NOI18N
    public static final String PROP_BUILDSCRIPT = "buildscript";    //NOI18N
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizard;
    private String[] names;
    private int index;


    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public String name() {
        return names[index];
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        index++;
    }

    @Override
    public void previousPanel() {
        index--;
    }

    @Override
    public void addChangeListener(@NonNull ChangeListener listener) {
        Parameters.notNull("listener", listener); //NOI18N
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(@NonNull final ChangeListener listener) {
        Parameters.notNull("listener", listener); //NOI18N
        changeSupport.removeChangeListener(listener);
    }


    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        panels = (WizardDescriptor.Panel<WizardDescriptor>[]) new WizardDescriptor.Panel<?>[] {
            new SetUpRemotePlatform.Panel()
        };
        names = new String[] {
            NbBundle.getMessage(RemotePlatformIt.class, "TXT_SetUpRemotePlatform") //NOI18N
        };
        index = 0;
        ((JComponent) panels[0].getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, names);
    }

    @Override
    public Set<JavaPlatform> instantiate() throws IOException {
        String displayName = (String) wizard.getProperty(PROP_DISPLAYNAME); //Platform name from wizard        
        String host = (String) wizard.getProperty(PROP_HOST);
        int port = (Integer) wizard.getProperty(PROP_PORT);
        String username = (String) wizard.getProperty(PROP_USERNAME);
        String password = null;
        String keyFile = null;
        String passphrase = null;
        if (wizard.getProperty(PROP_PASSWORD) != null) {
            password = (String) wizard.getProperty(PROP_PASSWORD);
        } else {
            keyFile = (String) wizard.getProperty(PROP_KEYFILE);
            passphrase = (String) wizard.getProperty(PROP_PASSPHRASE);
        }
        String jrePath = (String) wizard.getProperty(PROP_JREPATH);
        String workingDir = wizard.getProperty(PROP_WORKINGDIR) != null && ((String) wizard.getProperty(PROP_WORKINGDIR)).length() > 0
                ? (String) wizard.getProperty(PROP_WORKINGDIR) : "/home/" + username + "/NetBeansProjects/"; //NOI18N
        final Pair<Map<String,String>,Map<String,String>> p = RemotePlatformProbe.getSystemProperties(
                (Properties) wizard.getProperty(PROP_SYS_PROPERTIES));
        final RemotePlatform prototype = RemotePlatform.prototype(
                displayName,
                p.first(),
                p.second());
        try {
            prototype.setInstallFolder(new URI(jrePath));
            prototype.setWorkFolder(new URI(workingDir));
            final ConnectionMethod cm =
                password != null ?
                    ConnectionMethod.sshPassword(host, port, username, password) :
                    ConnectionMethod.sshKey(host, port, username, new File(keyFile), passphrase);
            prototype.setConnectionMethod(cm);
            return Collections.<JavaPlatform>singleton(
                RemotePlatformProvider.createNewPlatform(prototype));
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
    

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
        names = null;
        index = -1;
        File buildScript = (File) wizard.getProperty(PROP_BUILDSCRIPT);
        if (buildScript != null) {
            buildScript.delete();
        }
    }    

}
