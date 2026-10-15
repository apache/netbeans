/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            if ( netbeansActionMapping.getOptions()!= null && netbeansActionMapping.getOptions().size() > 0 )
            {
                serializer.startTag( NAMESPACE, "options" );
                for ( Iterator iter = netbeansActionMapping.getOptions().keySet().iterator(); iter.hasNext(); )
                {
                    String key = (String) iter.next();
                    String value = (String) netbeansActionMapping.getOptions().get( key );
                    serializer.startTag( NAMESPACE, "" + key + "" ).text( value ).endTag( NAMESPACE, "" + key + "" );
                }
                serializer.endTag( NAMESPACE, "options" );
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
        if (p.getDisplayName() != null) {
            serializer.startTag(NAMESPACE, "displayName" ).text( p.getId()).endTag( NAMESPACE, "displayName");  // NOI18N
        }
        serializer.startTag( NAMESPACE, "actions");
        for (NetbeansActionMapping m : p.getActions()) {
            writeNetbeansActionMapping(m, "action", serializer);
        }
        serializer.endTag( NAMESPACE, "actions");
        serializer.endTag( NAMESPACE, tagName );
    } 

}
