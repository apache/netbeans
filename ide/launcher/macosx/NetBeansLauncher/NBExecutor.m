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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

#import "NBExecutor.h"
#import "NBPreferences.h"

#define NB_SCRIPT @"bin/netbeans"

@implementation NBExecutor

- (IBAction)execute:(id)sender
{	NSTask *nbTask=[NSTask new];

	[preferences writeDefaults];
	[nbTask setCurrentDirectoryPath:netbeansHome];
	[nbTask setLaunchPath:[netbeansHome stringByAppendingPathComponent:NB_SCRIPT]];
	[nbTask setArguments:[preferences allArguments]];
	// NSLog([[nbTask arguments] description]);
	[nbTask launch];
	if ([[NSUserDefaults standardUserDefaults] integerForKey:DEFAULT_NAME_QUITIMM])
		[NSApp performSelector:@selector(terminate:) withObject:nil afterDelay:10];
	else
		[NSApp hide:nil];
	[nbTask release];
}

- (IBAction)openLog:(id)sender
{	NSString *logFile=[preferences getLogFile];

	if (logFile)
		[[NSWorkspace sharedWorkspace] openFile:logFile];
}

- (void)applicationWillTerminate:(NSNotification *)aNotification
{
	[self release];
}

- (BOOL)testNetbeansHome:(NSString *)home
{	if ([home length])
	{	NSString *script=[home stringByAppendingPathComponent:NB_SCRIPT];
		NSFileManager *fm=[NSFileManager defaultManager];
	
		return [fm isExecutableFileAtPath:script];
	}
	return NO;
}

- (NSString *)findNetbeans
{	NSString *defaultHome=[[NSBundle mainBundle] pathForResource:@"netbeans" ofType:@""];
	
	if (![self testNetbeansHome:defaultHome])
	{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];
		NSString *nbHome=[def stringForKey:DEFAULT_NAME_NBHOME];

		if  (![self testNetbeansHome:nbHome])
		{	NSString *newHome=nil;
		
			do
			{	int ret;
				NSOpenPanel *panel;
				NSString *directory;
				
				ret=NSRunAlertPanel(NSLocalizedString(@"NetBeans Launcher",@"Title of alert when NetBeans IDE root was not found"),
					NSLocalizedString(@"Cannot find NetBeans IDE root directory",@"Message indicating that IDE root was not found"),
					NSLocalizedString(@"Quit",@"Quit."),
					NSLocalizedString(@"Find...",@"Tile of button, which is used to display fileselector to locate IDE root"),
					nil);
				if (ret==NSAlertDefaultReturn)
					[NSApp terminate:nil];
				panel=[NSOpenPanel openPanel];
				[panel setCanChooseFiles:NO];
				[panel setCanChooseDirectories:YES];
				[panel runModal];
				directory=[panel filename];
				if (directory)
					newHome=directory;
			}while(![self testNetbeansHome:newHome]);
			[def setObject:newHome forKey:DEFAULT_NAME_NBHOME];
			return newHome;
		}
		return nbHome;
	}
	return defaultHome;
}

- (void)applicationDidFinishLaunching:(NSNotification *)aNotification
{	NSUserDefaults *def=[NSUserDefaults standardUserDefaults];

	netbeansHome=[[self findNetbeans] retain];
	if (![def integerForKey:DEFAULT_NAME_SET]) 
		[[preferences window] makeKeyAndOrderFront:nil];
	if ([def integerForKey:DEFAULT_NAME_RUNIMM])
		[self execute:nil];
}

- (BOOL)validateMenuItem:(NSMenuItem *)anItem {	
	if ([anItem tag]==OPEN_LOG_TAG)
	{	if (![preferences getLogFile])
			return NO;
	}
    return YES;
}


- (void)awakeFromNib
{	[fileMenu setAutoenablesItems:YES];
}

- (void)dealloc
{	[preferences release];
	[netbeansHome release];
	[super dealloc];
}

@end
