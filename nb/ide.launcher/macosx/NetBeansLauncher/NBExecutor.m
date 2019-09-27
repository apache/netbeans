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
