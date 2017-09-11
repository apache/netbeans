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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.maven.execute.model.io.xpp3;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.Writer;
import java.util.Iterator;
import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;
import org.netbeans.modules.maven.execute.model.ActionToGoalMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.execute.model.NetbeansActionProfile;

/**
 * Class NetbeansBuildActionXpp3Writer.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings({"unchecked", "deprecation", "rawtypes", "cast"})
public class NetbeansBuildActionXpp3Writer {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field NAMESPACE.
     */
    private String NAMESPACE;


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method write.
     * 
     * @param writer
     * @param actionToGoalMapping
     * @throws java.io.IOException
     */
    public void write(Writer writer, ActionToGoalMapping actionToGoalMapping)
        throws java.io.IOException
    {
        XmlSerializer serializer = new MXSerializer();
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  " );
        serializer.setProperty( "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n" );
        serializer.setOutput( writer );
        serializer.startDocument( actionToGoalMapping.getModelEncoding(), null );
        writeActionToGoalMapping( actionToGoalMapping, "actions", serializer );
        serializer.endDocument();
    } //-- void write(Writer, ActionToGoalMapping) 

    /**
     * Method writeActionToGoalMapping.
     * 
     * @param actionToGoalMapping
     * @param serializer
     * @param tagName
     * @throws java.io.IOException
     */
    private void writeActionToGoalMapping(ActionToGoalMapping actionToGoalMapping, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( actionToGoalMapping != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( actionToGoalMapping.getPackaging() != null )
            {
                serializer.startTag( NAMESPACE, "packaging" ).text( actionToGoalMapping.getPackaging() ).endTag( NAMESPACE, "packaging" );
            }
            if ( actionToGoalMapping.getActions() != null && actionToGoalMapping.getActions().size() > 0 )
            {
                for ( Iterator iter = actionToGoalMapping.getActions().iterator(); iter.hasNext(); )
                {
                    NetbeansActionMapping o = (NetbeansActionMapping) iter.next();
                    writeNetbeansActionMapping( o, "action", serializer );
                }
            }
            if (!actionToGoalMapping.getProfiles().isEmpty()) {
                serializer.startTag(NAMESPACE, "profiles");
                for (NetbeansActionProfile p : actionToGoalMapping.getProfiles()) {
                    writeNetbeansActionProfile(p, "profile", serializer);
                }
                serializer.endTag(NAMESPACE, "profiles");
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writeActionToGoalMapping(ActionToGoalMapping, String, XmlSerializer) 

    /**
     * Method writeNetbeansActionMapping.
     * 
     * @param netbeansActionMapping
     * @param serializer
     * @param tagName
     * @throws java.io.IOException
     */
    private void writeNetbeansActionMapping(NetbeansActionMapping netbeansActionMapping, String tagName, XmlSerializer serializer)
        throws java.io.IOException
    {
        if ( netbeansActionMapping != null )
        {
            serializer.startTag( NAMESPACE, tagName );
            if ( netbeansActionMapping.getActionName() != null )
            {
                serializer.startTag( NAMESPACE, "actionName" ).text( netbeansActionMapping.getActionName() ).endTag( NAMESPACE, "actionName" );
            }
            if ( netbeansActionMapping.getDisplayName() != null )
            {
                serializer.startTag( NAMESPACE, "displayName" ).text( netbeansActionMapping.getDisplayName() ).endTag( NAMESPACE, "displayName" );
            }
            if ( netbeansActionMapping.getBasedir() != null )
            {
                serializer.startTag( NAMESPACE, "basedir" ).text( netbeansActionMapping.getBasedir() ).endTag( NAMESPACE, "basedir" );
            }
            if ( netbeansActionMapping.getReactor() != null )
            {
                serializer.startTag( NAMESPACE, "reactor" ).text( netbeansActionMapping.getReactor() ).endTag( NAMESPACE, "reactor" );
            }
            if ( netbeansActionMapping.getPreAction() != null )
            {
                serializer.startTag( NAMESPACE, "preAction" ).text( netbeansActionMapping.getPreAction() ).endTag( NAMESPACE, "preAction" );
            }
            if ( netbeansActionMapping.isRecursive() != true )
            {
                serializer.startTag( NAMESPACE, "recursive" ).text( String.valueOf( netbeansActionMapping.isRecursive() ) ).endTag( NAMESPACE, "recursive" );
            }
            if ( netbeansActionMapping.getPackagings() != null && netbeansActionMapping.getPackagings().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "packagings" );
                for ( Iterator iter = netbeansActionMapping.getPackagings().iterator(); iter.hasNext(); )
                {
                    String packaging = (String) iter.next();
                    serializer.startTag( NAMESPACE, "packaging" ).text( packaging ).endTag( NAMESPACE, "packaging" );
                }
                serializer.endTag( NAMESPACE, "packagings" );
            }
            if ( netbeansActionMapping.getGoals() != null && netbeansActionMapping.getGoals().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "goals" );
                for ( Iterator iter = netbeansActionMapping.getGoals().iterator(); iter.hasNext(); )
                {
                    String goal = (String) iter.next();
                    serializer.startTag( NAMESPACE, "goal" ).text( goal ).endTag( NAMESPACE, "goal" );
                }
                serializer.endTag( NAMESPACE, "goals" );
            }
            if ( netbeansActionMapping.getProperties() != null && netbeansActionMapping.getProperties().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "properties" );
                for ( Iterator iter = netbeansActionMapping.getProperties().keySet().iterator(); iter.hasNext(); )
                {
                    String key = (String) iter.next();
                    String value = (String) netbeansActionMapping.getProperties().get( key );
                    serializer.startTag( NAMESPACE, "" + key + "" ).text( value ).endTag( NAMESPACE, "" + key + "" );
                }
                serializer.endTag( NAMESPACE, "properties" );
            }
            if ( netbeansActionMapping.getActivatedProfiles() != null && netbeansActionMapping.getActivatedProfiles().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "activatedProfiles" );
                for ( Iterator iter = netbeansActionMapping.getActivatedProfiles().iterator(); iter.hasNext(); )
                {
                    String activatedProfile = (String) iter.next();
                    serializer.startTag( NAMESPACE, "activatedProfile" ).text( activatedProfile ).endTag( NAMESPACE, "activatedProfile" );
                }
                serializer.endTag( NAMESPACE, "activatedProfiles" );
            }
            serializer.endTag( NAMESPACE, tagName );
        }
    } //-- void writeNetbeansActionMapping(NetbeansActionMapping, String, XmlSerializer) 

    private void writeNetbeansActionProfile(NetbeansActionProfile p, String tagName, XmlSerializer serializer)
    throws java.io.IOException {
        serializer.startTag( NAMESPACE, tagName);
        serializer.startTag( NAMESPACE, "id" ).text( p.getId()).endTag( NAMESPACE, "id");
        serializer.startTag( NAMESPACE, "actions");
        for (NetbeansActionMapping m : p.getActions()) {
            writeNetbeansActionMapping(m, "action", serializer);
        }
        serializer.endTag( NAMESPACE, "actions");
        serializer.endTag( NAMESPACE, tagName );
    } 

}
