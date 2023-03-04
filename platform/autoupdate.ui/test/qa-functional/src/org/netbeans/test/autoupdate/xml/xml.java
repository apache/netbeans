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

package org.netbeans.test.autoupdate.xml;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import java.util.regex.*;
import org.netbeans.test.autoupdate.Autoupdate;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class xml extends Autoupdate {
    
    public xml( String arg0 )
    {
      super( arg0 );
    }

  private void WaitTab( JTabbedPaneOperator tabs, String name, int count )
  {
    int iCount = 0;
    int iIndex = tabs.findPage( "Available" );
    //System.out.println( "+++" + iIndex );
    Pattern p = Pattern.compile( "Available Plugins [(]([0-9]+)[)]" );
    while( true )
    {
      String s = tabs.getTitleAt( iIndex );
      //System.out.println( "+++\"" + s + "\"" );
      Matcher m = p.matcher( s );
      if( m.find( ) )
      {
        int iAvailable = Integer.parseInt( m.group( 1 ) );
        if( iAvailable >= count )
          return; // SUCCESS
      }
      if( ++iCount > 60 )
        fail( "Too long wait for available plugins." );
      Sleep( 1000 );
    }
  }

  private JButtonOperator WaitButton( JDialogOperator dialog, String name )
  {
    int iCount = 0;
    while( true )
    {
      try
      {
        JButtonOperator jbButton = new JButtonOperator( dialog, name );
        if( jbButton.isEnabled( ) )
          return jbButton;
      }
      catch( JemmyException ex )
      {
        // Do nothing, wait for button again
      }
      if( ++iCount >= 100 )
        fail( "Unable to wait Finish button." );
      Sleep( 1000 );
    }
  }

  protected void InstallSOAInternal( )
  {
    // Open plugins dialog
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    // Wait for a while because tab selection might change.
    Sleep( 2000 );

    // Select force installation into shared folders
    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Settings" ) );
    JToggleButtonOperator jtbShared = new JToggleButtonOperator( jdPlugins, "Force install into shared directories" );
    jtbShared.clickMouse( );

    // Open Installed tab
    jtTabs.setSelectedIndex( jtTabs.findPage( "Installed" ) );
    JButtonOperator jbReload = new JButtonOperator( jdPlugins, "Reload Catalog" );
    jbReload.pushNoBlock( );

    // Wait till reload
    WaitTab( jtTabs, "Available", 25 );

    // Open available tab
    jtTabs.setSelectedIndex( jtTabs.findPage( "Available" ) );

    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );

    // Select SOA
    int iCellRow = jtTable.findCellRow( "SOA" );
    //jtTable.clickOnCell( iCellRow, 0 );
    iCellRow = jtTable.findCellRow( "XML Schema and WSDL" );
    jtTable.clickOnCell( iCellRow, 0 );

    // Click install
    // "Install"
    JButtonOperator jbInstall = new JButtonOperator( jdPlugins, "Install" );
    jbInstall.pushNoBlock( );

    // Find installer dialog
    JDialogOperator jdInstaller = new JDialogOperator( "Installer" );

    // Next
    JButtonOperator jbNext = new JButtonOperator( jdInstaller, "Next" );
    jbNext.push( );
    Sleep( 1500 );

    // Accept
    JToggleButtonOperator jtbAccept = new JToggleButtonOperator(
        jdInstaller,
        "I accept the terms in all of the license agreements."
      );
    jtbAccept.clickMouse( );

    // Install
    jbInstall = new JButtonOperator( jdInstaller, "Install" );
    jbInstall.push( );

    // Wait Finish button
    JButtonOperator jbFinish = WaitButton( jdInstaller, "Finish" );

    // Select later restart
    JRadioButtonOperator jrbRestart = new JRadioButtonOperator(
        jdInstaller,
        "Later"
      );
    jrbRestart.clickMouse( );

    // Close dialog
    jbFinish.push( );
    jdInstaller.waitClosed( );
    
    // Close dialog
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    return;
  }

  protected void UninstallSOAInternal( )
  {
    // Open plugins dialog
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    // Wait for a while because tab selection might change.
    Sleep( 2000 );

    // Open Installed tab
    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Installed" ) );

    // Select SOA
    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );
    int iCellRow = jtTable.findCellRow( "SOA" );
    jtTable.clickOnCell( iCellRow, 0 );

    // Click install
    // "Install"
    JButtonOperator jbUninstall = new JButtonOperator( jdPlugins, "Uninstall" );
    jbUninstall.pushNoBlock( );

    // Find installer dialog
    JDialogOperator jdUninstaller = new JDialogOperator( "Installer" );

    // Uninstall
    jbUninstall = new JButtonOperator( jdUninstaller, "Uninstall" );
    jbUninstall.push( );

    // Wait Finish button
    JButtonOperator jbFinish = WaitButton( jdUninstaller, "Finish" );

    // Select later restart
    JRadioButtonOperator jrbRestart = new JRadioButtonOperator(
        jdUninstaller,
        "Later"
      );
    jrbRestart.clickMouse( );

    // Close dialog
    jbFinish.push( );
    jdUninstaller.waitClosed( );
    
    // Close dialog
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

  }
}
