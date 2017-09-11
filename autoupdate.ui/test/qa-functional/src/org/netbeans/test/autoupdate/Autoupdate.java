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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.autoupdate;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.junit.NbModuleSuite;
import junit.framework.Test;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author michaelnazarov@netbeans.org
 */

public class Autoupdate extends JellyTestCase
{
  public Autoupdate( String arg0 )
  {
    super( arg0 );
  }

  public static Test suite( )
  {
    return NbModuleSuite.create(
      NbModuleSuite.createConfiguration( Autoupdate.class ).addTest(
          "GeneralChecks",
          "CheckSettings",
          "CheckInstalled",
          "CheckAvailablePlugins",
          "CheckUpdated",
          "CheckDownloaded"
        )
        .enableModules( ".*" )
        .clusters( ".*" )
        //.gui( true )
      );
  }

  protected void Sleep( int iTime )
  {
    try
    {
      Thread.sleep( iTime );
    }
    catch( InterruptedException ex )
    {
      System.out.println( "=== Interrupted sleep ===" );
    }
  }

  public void GeneralChecks( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );
    //try{ Dumper.dumpAll( "c:\\dump.txt" ); } catch( IOException ex ) { }

    // Wait for a while because tab selection might change.
    Sleep( 2000 );

    // Check tabs
    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    String[] asTabs =
    {
      "Updates",
      "Available Plugins",
      "Downloaded",
      "Installed",
      "Settings"
    };

    int iCount = jtTabs.getTabCount( );
    if( iCount != asTabs.length )
      fail( "Invalid number of tabs: " + iCount + ", expected: " + asTabs.length );
    for( int iIndex = 0; iIndex < asTabs.length; iIndex++ )
    {
      String sTitle = jtTabs.getTitleAt( iIndex );
      if( !sTitle.startsWith( asTabs[ iIndex ] ) )
        fail( "Invalid tab at index " + iIndex + ": \"" + sTitle + "\"" );
    }

    // Check buttons
    new JButtonOperator( jdPlugins, "Reload Catalog" );
    new JButtonOperator( jdPlugins, "Uninstall" );
    new JButtonOperator( jdPlugins, "Deactivate" );
    new JButtonOperator( jdPlugins, "Help" );

    // Check there is label
    new JLabelOperator( jdPlugins, "Search:" );

    // Check there is table operator
    new JTableOperator( jdPlugins, 0 );

    // Search text field
    JTextFieldOperator jtSearch = new JTextFieldOperator( jdPlugins, 0 );
    // Check does this field affect selection
    String sSelected = jtTabs.getTitleAt( jtTabs.getSelectedIndex( ) );
    System.out.println( "===" + sSelected );
    jtSearch.enterText( "java" );
    Sleep( 2000 );
    sSelected = jtTabs.getTitleAt( jtTabs.getSelectedIndex( ) );
    System.out.println( "===" + sSelected );
    if( !sSelected.matches( "Installed [(][0-9]+/[0-9]+[)]" ) )
      fail( "Invalid result of filtering." );
    jtSearch.enterText( "" );

    // Close by button
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    endTest( );
  }

  public void CheckSettings( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Settings" ) );

    // Check buttons
    JButtonOperator jbEdit = new JButtonOperator( jdPlugins, "Edit" );
    JButtonOperator jbRemove = new JButtonOperator( jdPlugins, "Remove" );
    JButtonOperator jbAdd = new JButtonOperator( jdPlugins, "Add" );
    JButtonOperator jbProxy = new JButtonOperator( jdPlugins, "Proxy Settings" );
    new JToggleButtonOperator( jdPlugins, "Force install into shared directories" );

    // Check table
    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );
    int iOriginalRows = jtTable.getRowCount( );

    // Check there is combo box
    new JComboBoxOperator( jdPlugins, 0 );

    // Check each of specified centers is accessible
    /*
    for( int i = 0; i < iOriginalRows; i++ )
    {
      // Select row
      jtTable.clickOnCell( i, 1 );
      Sleep( 100 );

      // Press edit and get address
      jbEdit.pushNoBlock( );
      JDialogOperator jdUpdate = new JDialogOperator( "Update Center Customizer" );
      JTextFieldOperator jtUrl = new JTextFieldOperator( jdUpdate, 0 );
      String sAddr = jtUrl.getText( );
      JButtonOperator jbClose = new JButtonOperator( jdUpdate, "Cancel" );
      jbClose.push( );
      jdUpdate.waitClosed( );

      // Download data
      String sContent = "";
      try
      {
        URL u = new URL( sAddr );
        HttpURLConnection http = ( HttpURLConnection )u.openConnection( );
        http.setConnectTimeout( 30000 );
        http.setReadTimeout( 60000 );
        http.connect( );
        InputStream is = http.getInputStream( );
        byte[] b = new byte[ 1024 ];
        int iReaden;
        while( -1 != ( iReaden = is.read( b ) ) )
        {
          //sContent = sContent + new String( b, 0, iReaden );
        }
        http.disconnect( );
        is.close( );
      }
      catch( java.net.MalformedURLException ex )
      {
        System.out.println( "Error: " + ex.getMessage( ) + "\n" );
        fail( "Malformed URL used for specified update center: " + sAddr );
      }
      catch( IOException ex )
      {
        System.out.println( "Error: " + ex.getMessage( ) + "\n" );
        fail( "Unable to download data from specified update center: " + sAddr );
      }
    }
    */

    // Edit for default selection
    jbEdit.pushNoBlock( );
    JDialogOperator jdUpdate = new JDialogOperator( "Update Center Customizer" );
    JTextFieldOperator jtUrl = new JTextFieldOperator( jdUpdate, 0 );
    JTextFieldOperator jtName = new JTextFieldOperator( jdUpdate, 1 );
    String sUrl = jtUrl.getText( );
    String sName = jtName.getText( );
    JButtonOperator jbClose = new JButtonOperator( jdUpdate, "Cancel" );
    jbClose.push( );
    jdUpdate.waitClosed( );

    // Add new center
    jbAdd.pushNoBlock( );
    jdUpdate = new JDialogOperator( "Update Center Customizer" );
    // Check OK is disabled
    JButtonOperator jbOk = new JButtonOperator( jdUpdate, "OK" );
    if( jbOk.isEnabled( ) )
      fail( "OK button originally enabled." );

    // Use already existing name
    jtName = new JTextFieldOperator( jdUpdate, 1 );
    String sDefault = jtName.getText( );
    jtName.setText( sName );
    if( jbOk.isEnabled( ) )
      fail( "OK button enabled for duplicate name." );
    jtName.setText( sDefault );

    // Enter correct url
    jtUrl = new JTextFieldOperator( jdUpdate, 0 );
    jtUrl.setText( sUrl );
    Sleep( 500 );
    if( !jbOk.isEnabled( ) )
      fail( "OK button was not enabled after text entering." );
    JToggleButtonOperator jtCheck = new JToggleButtonOperator( jdUpdate, "Check for updates automatically" );
    jtCheck.setSelected( false );
    jbOk.push( );
    jdUpdate.waitClosed( );
    if( ( 1 + iOriginalRows ) != jtTable.getRowCount( ) )
      fail( "Failed to add new center." );

    // Deselect existing centers and check number of available plugins
      // ToDo

    // Try to remove
    jtTable.clickOnCell( iOriginalRows, 1 );
    jbRemove.pushNoBlock( );
    JDialogOperator jdConfirm = new JDialogOperator( "Question" );
    JButtonOperator jbNo = new JButtonOperator( jdConfirm, "No" );
    jbNo.push( );
    jdConfirm.waitClosed( );

    // Remove
    jbRemove.pushNoBlock( );
    jdConfirm = new JDialogOperator( "Question" );
    JButtonOperator jbYes = new JButtonOperator( jdConfirm, "Yes" );
    jbYes.push( );
    jdConfirm.waitClosed( );
    // Check removed
    if( iOriginalRows != jtTable.getRowCount( ) )
      fail( "Failed to remove center." );

    // Check proxy settings are accessible
    jbProxy.pushNoBlock( );
    JDialogOperator jdOptions = new JDialogOperator( "Options" );
    new JLabelOperator( jdOptions, "Proxy Settings:" );
    JButtonOperator jbCancel = new JButtonOperator( jdOptions, "Cancel" );
    jbCancel.push( );
    jdOptions.waitClosed( );

    // Check periods
      // ToDo

    // Close by button
    jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    endTest( );
  }

  public void CheckInstalled( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Installed" ) );

    // HERE THE TESTS

    // Click reload and wait results
    JButtonOperator jbReload = new JButtonOperator( jdPlugins, "Reload Catalog" );
    jbReload.push( );
    Sleep( 5000 );
    boolean bRedo = true;
    int iCount = 0;
    while( bRedo )
    {
      try
      {
        Sleep( 1000 );
        new JLabelOperator( jdPlugins, "Checking for updates in " );
        if( 60 <= ++iCount )
          fail( "Reloading is too long." );
      }
      catch( JemmyException ex )
      {
        bRedo = false;
      }
    }

    // Check buttons
    JButtonOperator jbUninstall = new JButtonOperator( jdPlugins, "Uninstall" );

    // Check table
    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );
    int iOriginalRows = jtTable.getRowCount( );

    for( int i = 0; i < 10; i++ )
    {
      // Check uninstall disabled
      if( jbUninstall.isEnabled( ) )
        fail( "Uninstall button enabled without selection." );
      // Click first column
      jtTable.clickOnCell( i, 0 );
      // Check uninstall enabled
      if( !jbUninstall.isEnabled( ) )
        fail( "Uninstall button disabled with selection." );

      // Check 
        // ToDo

      // Click first column
      jtTable.clickOnCell( i, 0 );
      // Check uninstall disabled
      if( jbUninstall.isEnabled( ) )
        fail( "Uninstall button enabled without selection." );
    }

    // Close by button
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    endTest( );
  }

  public void CheckAvailablePlugins( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Available Plugins" ) );

    Sleep( 1000 );

    // Check buttons
    JButtonOperator jbReload = new JButtonOperator( jdPlugins, "Reload Catalog" );
    JButtonOperator jbInstall = new JButtonOperator( jdPlugins, "Install" );

    // Check table
    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );
    int iOriginalRows = jtTable.getRowCount( );

    for( int i = 0; i < iOriginalRows; i++ )
    {
      // Check install disabled
      if( jbInstall.isEnabled( ) )
        fail( "Install button enabled without selection." );
      // Click first column
      jtTable.clickOnCell( i, 0 );
      // Check install enabled
      if( !jbInstall.isEnabled( ) )
        fail( "Install button disabled with selection." );

      // Check 
        // ToDo

      // Click first column
      jtTable.clickOnCell( i, 0 );
      // Check install disabled
      if( jbInstall.isEnabled( ) )
        fail( "Install button enabled without selection." );
    }

    // Close by button
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );
    endTest( );
  }

  public void CheckUpdated( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Updated" ) );

    // Close by button
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    endTest( );
  }

  public void CheckDownloaded( )
  {
    startTest( );

    // Open
    new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenuNoBlock("Tools|Plugins");

    JDialogOperator jdPlugins = new JDialogOperator( "Plugins" );

    JTabbedPaneOperator jtTabs = new JTabbedPaneOperator( jdPlugins, 0 );
    jtTabs.setSelectedIndex( jtTabs.findPage( "Downloaded" ) );

    // Check buttons
    JButtonOperator jbAdd = new JButtonOperator( jdPlugins, "Add Plugins..." );
    JButtonOperator jbInstall = new JButtonOperator( jdPlugins, "Install" );

    // Check table
    JTableOperator jtTable = new JTableOperator( jdPlugins, 0 );
    int iOriginalRows = jtTable.getRowCount( );

    // Close by button
    JButtonOperator jbClose = new JButtonOperator( jdPlugins, "Close" );
    jbClose.push( );
    jdPlugins.waitClosed( );

    endTest( );
  }

}
