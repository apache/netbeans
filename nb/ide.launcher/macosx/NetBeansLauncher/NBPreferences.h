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

#import <Cocoa/Cocoa.h>

@interface NBPreferences : NSObject
{
    IBOutlet NSButton *debug;
    IBOutlet NSPopUpButton *fontSize;
    IBOutlet NSTextField *jdkHome;
    IBOutlet NSPopUpButton *lookFeel;
    IBOutlet NSButton *quitImm;
    IBOutlet NSButton *runImm;
    IBOutlet NSTextField *userDirectory;
    IBOutlet NSFormCell *vmSizeForm;
    IBOutlet NSFormCell *extraParamsForm;
}
- (IBAction)revertExpert:(id)sender;
- (IBAction)revertUser:(id)sender;
- (IBAction)setUserdir:(id)sender;
- (NSArray *)allArguments;
- (void)writeDefaults;
- (NSWindow *)window;
- (NSString *)getLogFile;

#define DEFAULT_NAME_DEBUG @"DEBUG"
#define DEFAULT_NAME_JDKHOME @"JDKHOME"
#define DEFAULT_NAME_FONTSIZE @"FONTSIZE"
#define DEFAULT_NAME_LOOKFEEL @"LOOKFEEL"
#define DEFAULT_NAME_USERDIR @"USERDIR"
#define DEFAULT_NAME_RUNIMM @"RUNIMMEDIATELY"
#define DEFAULT_NAME_QUITIMM @"QUITIMMEDIATELY"
#define DEFAULT_NAME_NBHOME @"NETBEANSHOME"
#define DEFAULT_NAME_VMSIZE @"VMSIZE"
#define DEFAULT_NAME_EXTRAPARAMS @"EXTRAPARAMS"
#define DEFAULT_NAME_SET @"DEFAULTS_SET"

@end
