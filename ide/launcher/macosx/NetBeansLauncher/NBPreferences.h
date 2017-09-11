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
