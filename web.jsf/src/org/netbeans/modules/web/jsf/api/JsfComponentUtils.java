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
package org.netbeans.modules.web.jsf.api;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.libraries.LibraryFactory;
import org.netbeans.spi.project.libraries.LibraryImplementation3;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.Parameters;

/**
 * Contains utilities methods for JSF components plugins.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JsfComponentUtils {

    private JsfComponentUtils() {
    }

    /**
     * Recreates library with maven-pom content. If the library already contains
     * maven-pom content, nothing happen then.
     * <p>
     * Should be replaced once will be possible to enhance library content over API.
     *
     * @param library original library to be cloned
     * @param poms {@code List} of maven-pom {@code URI}s
     * @return library with pom content (the original library if the content wasn't added)
     * @throws IOException when original library cannot be deleted or recreated
     * @deprecated Instead of enhancing Ant libraries for Maven contents, another memory library should be created and
     * added to project using {@link ProjectClassPathModifier#addLibraries(org.netbeans.api.project.libraries.Library[],
     * org.openide.filesystems.FileObject, java.lang.String)}. Library enhances by "maven-pom" needn't to work since
     * {@code LibraryImplementation3}. Use {@link #createMavenDependencyLibrary(java.lang.String, java.lang.String[],
     * java.lang.String[])} instead.
     * @see #createMavenDependencyLibrary(java.lang.String, java.lang.String[], java.lang.String[]) 
     */
    public static Library enhanceLibraryWithPomContent(final Library library, final List<URI> poms) throws IOException {
        Parameters.notNull("library", library);     //NOI18N
        Parameters.notNull("poms", poms);           //NOI18N
        List<URL> mavenContent = library.getContent("maven-pom"); //NOI18N
        final String name = library.getName();
        if (mavenContent == null || mavenContent.isEmpty()) {
            final Runnable call = new Runnable() {
                @Override
                public void run() {
                    // copy existing contents
                    final String type = library.getType();
                    final String name = library.getName();
                    final String displayName = library.getDisplayName();
                    final String desc = library.getDescription();
                    Map<String, List<URI>> content = new HashMap<String, List<URI>>();
                    content.put("classpath", library.getURIContent("classpath")); //NOI18N
                    content.put("src", library.getURIContent("src")); //NOI18N
                    content.put("javadoc", library.getURIContent("javadoc")); //NOI18N

                    // include references to maven-pom artifacts
                    content.put("maven-pom", poms); //NOI18N

                    try {
                        LibraryManager.getDefault().removeLibrary(library);
                        LibraryManager.getDefault().createURILibrary(type, name, displayName, desc, content);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    } catch (IllegalArgumentException iae) {
                        Exceptions.printStackTrace(iae);
                    }
                }
            };
            Mutex.EVENT.writeAccess(call);
        }
        return LibraryManager.getDefault().getLibrary(name);
    }

    /**
     * Reformats given {@code DataObject}.
     * @param dob {@code DataObject} to reformat.
     * @since 1.35
     */
    public static void reformat(@NonNull DataObject dob) {
        Parameters.notNull("dob", dob); //NOI18N
        try {
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                return;
            }

            final StyledDocument doc = ec.openDocument();
            final Reformat reformat = Reformat.get(doc);

            reformat.lock();
            try {
                NbDocument.runAtomicAsUser(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                reformat.unlock();
                ec.saveDocument();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Enhances existing data object for content.
     * @param dob data object to be enhanced
     * @param find text element where text should be included
     * @param enhanceBy enhancing content
     * @since 1.35
     */
    public static void enhanceFileBody(@NonNull DataObject dob, @NonNull final String find, @NonNull final String enhanceBy) {
        Parameters.notNull("dob", dob);             //NOI18N
        Parameters.notNull("find", find);           //NOI18N
        Parameters.notNull("enhanceBy", enhanceBy); //NOI18N
        try {
            EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                return;
            }

            final StyledDocument doc = ec.openDocument();
            try {
                NbDocument.runAtomicAsUser(doc, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int position = doc.getText(0, doc.getLength()).indexOf(find);
                            // if element wasn't found - it isn't likely new project with sample index.html page and
                            // there is probably not wished to have it changed, so don't do it
                            if (position >= 0) {
                                doc.insertString(position, enhanceBy + "\n", null); //NOI18N
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ec.saveDocument();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Says whether the given webModule is Maven based or not. It should prevent dependency of JSF component suite
     * libraries on Maven or Ant based modules.
     * @param webModule to be examined
     * @return {@code true} when the webModule is Maven based, {@code false} otherwise
     * @since 1.43
     */
    public static boolean isMavenBased(@NonNull WebModule webModule) {
        Parameters.notNull("webModule", webModule); //NOI18N
        FileObject fo = webModule.getDocumentBase();
        if (fo == null) {
            // FIXME this is just best effort, perhaps there should be WebModule.getProject()
            FileObject[] sources = webModule.getJavaSources();
            if (sources.length > 0) {
                fo = sources[0];
            }
        }
        Project project = FileOwnerQuery.getOwner(fo);
        AntArtifactProvider antArtifactProvider = project.getLookup().lookup(AntArtifactProvider.class);
        return antArtifactProvider == null;
    }

    /**
     * Creates library used for Maven project extending by given pom resources.
     * @param name name of the library
     * @param mavenDeps maven dependencies to store in project pom.xml
     * @param mavenRepos maven repositories to store in project pom.xml
     * @return created library
     * @since 1.43
     */
    public static Library createMavenDependencyLibrary(@NonNull String name, @NonNull String[] mavenDeps, @NonNull String[] mavenRepos) {
        Parameters.notNull("name", name);               //NOI18N
        Parameters.notNull("mavenDeps", mavenDeps);     //NOI18N
        Parameters.notNull("mavenRepos", mavenRepos);   //NOI18N
        LibraryImplementation3 libImpl = CommonProjectUtils.createJavaLibraryImplementation(
                name, new URL[0], new URL[0], new URL[0], mavenDeps, mavenRepos);
        return LibraryFactory.createLibrary(libImpl);
    }

}
