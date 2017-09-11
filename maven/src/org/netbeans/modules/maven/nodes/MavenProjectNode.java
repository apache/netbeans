/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.CharConversionException;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import static org.netbeans.modules.maven.nodes.Bundle.*;
import org.netbeans.modules.maven.spi.IconResources;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.ProjectProblemsProvider.ProjectProblem;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.xml.XMLUtil;

/** A node to represent project root.
 *
 * @author Milos Kleint
 */
public class MavenProjectNode extends AbstractNode {

    private static final Logger LOGGER = Logger.getLogger(MavenProjectNode.class.getName());

    @Messages("ICON_BrokenProjectBadge=Project loading failed or was not complete")
    private static final String toolTipBroken = "<img src=\"" + MavenProjectNode.class.getClassLoader().getResource(IconResources.BROKEN_PROJECT_BADGE_ICON) + "\">&nbsp;" + ICON_BrokenProjectBadge();

     private final NbMavenProjectImpl project;
     private final ProjectInformation info;

     public MavenProjectNode(Lookup lookup, NbMavenProjectImpl proj) {
        super(NodeFactorySupport.createCompositeChildren(proj, "Projects/org-netbeans-modules-maven/Nodes"), lookup); //NOI18N
        this.project = proj;
        info = ProjectUtils.getInformation(project);
        info.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent event) {
                String prop = event.getPropertyName();
                if (ProjectInformation.PROP_NAME.equals(prop)) {
                    fireNameChange(null, null);
                } else if (ProjectInformation.PROP_DISPLAY_NAME.equals(prop)) {
                    fireDisplayNameChange(null, getDisplayName());
                } else if (ProjectInformation.PROP_ICON.equals(prop)) {
                    fireIconChange();
                    fireOpenedIconChange();
                }
            }
        });
        proj.getLookup().lookup(ProjectProblemsProvider.class).addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (ProjectProblemsProvider.PROP_PROBLEMS.equals(evt.getPropertyName())) {
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run() {
                             fireNameChange(null, getName());
                             fireDisplayNameChange(null, getDisplayName());
                             fireShortDescriptionChange(null, getShortDescription());
                         }
                     });

                 }
             }
         });
    }

    public @Override String getName() {
        return project.getProjectDirectory().toURI().toString();
    }

    @Override
    public String getDisplayName() {
        return info.getDisplayName();
    }

    public @Override String getHtmlDisplayName() {
        if (!project.isMavenProjectLoaded()) {
            return null;
        }
        String packaging = project.getOriginalMavenProject().getPackaging();
        if (project.getLookup().lookup(SpecialIcon.class) != null) {
            return null;
        }
        try {
            return XMLUtil.toElementContent(getDisplayName()) + " <font color='!controlShadow'>" + packaging + "</font>";
        } catch (CharConversionException x) {
            return null;
        }
    }

    @Override
    public Image getIcon(int param) {
        Icon icon = info.getIcon();
        if (icon == null) {
            LOGGER.log(Level.WARNING, "no icon in {0}", info);
            return super.getIcon(param);
        }
        return ImageUtilities.icon2Image(icon);
    }

    @Override
    public Image getOpenedIcon(int param) {
        return getIcon(param);
    }

    public @Override Action[] getActions(boolean param) {
        return CommonProjectActions.forType("org-netbeans-modules-maven"); // NOI18N
    }

    @Messages({
        "TXT_FailedProjectLoadingDesc=This project could not be loaded by the NetBeans Connector. "
            + "That usually means something is wrong with your pom.xml, or plugins are missing. "
            + "Select \"Show and Resolve Problems\" from the project's context menu for additional information.",
        "LBL_DefaultDescription=A Maven-based project",
        "TXT_Loading=Loading...",
        "DESC_Project1=Location:",
        "DESC_Project2=GroupId:",
        "DESC_Project3=ArtifactId:",
        "DESC_Project4=Version:",
        "DESC_Project5=Packaging:",
        "DESC_Project6=Description:",
        "DESC_Project7=Problems:"
    })
    @Override public String getShortDescription() {
        StringBuilder buf = new StringBuilder();
        String desc;
        MavenProject mp = project.isMavenProjectLoaded() ? project.getOriginalMavenProject() : null;
        if(mp != null) {
            if (NbMavenProject.isErrorPlaceholder(mp)) {
                desc = TXT_FailedProjectLoadingDesc();
            } else {
                //TODO escape the short description
                desc = mp.getDescription();
                if (desc == null) {
                    desc = LBL_DefaultDescription();
                }
            }            
        } else {
            desc = TXT_Loading();
        }
        buf.append("<html><i>").append(DESC_Project1()).append("</i><b> ").append(FileUtil.getFileDisplayName(project.getProjectDirectory())).append("</b><br><i>"); //NOI18N
        if (mp != null) {
            buf.append(DESC_Project2()).append("</i><b> ").append(mp.getGroupId()).append("</b><br><i>");//NOI18N
            buf.append(DESC_Project3()).append("</i><b> ").append(mp.getArtifactId()).append("</b><br><i>");//NOI18N
            buf.append(DESC_Project4()).append("</i><b> ").append(mp.getVersion()).append("</b><br><i>");//NOI18N
            buf.append(DESC_Project5()).append("</i><b> ").append(mp.getPackaging()).append("</b><br><i>");//NOI18N
        }
        buf.append(DESC_Project6()).append("</i> ").append(breakPerLine(desc, DESC_Project5().length()));//NOI18N
        Collection<? extends ProjectProblem> problems = project.getLookup().lookup(ProjectProblemsProvider.class).getProblems();
        if (!problems.isEmpty()) {
            buf.append("<br><b>").append(DESC_Project7()).append("</b><br><ul>");//NOI18N
            for (ProjectProblem elem : problems) {
                buf.append("<li>").append(elem.getDisplayName()).append("</li>");//NOI18N
            }
            buf.append("</ul>");//NOI18N
        }
        // it seems that with ending </html> tag the icon descriptions are not added.
//        buf.append("</html>");//NOI18N
        return buf.toString();
    }

    private String breakPerLine(String string, int start) {
        StringBuilder buf = new StringBuilder();
        StringTokenizer tok = new StringTokenizer(string, " ", true);//NOI18N
        int charCount = start;
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            charCount = charCount + token.length();
            if (charCount > 50) {
                charCount = 0;
                buf.append("<br>");//NOI18N
            }
            buf.append(token);
        }
        return buf.toString();

    }

}
