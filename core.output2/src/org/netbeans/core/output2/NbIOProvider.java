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

package org.netbeans.core.output2;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.api.io.Hyperlink;
import org.netbeans.api.io.OutputColor;
import org.netbeans.api.io.ShowOperation;
import org.netbeans.spi.io.InputOutputProvider;
import org.netbeans.spi.io.support.Hyperlinks;
import org.netbeans.spi.io.support.OutputColorType;
import org.netbeans.spi.io.support.OutputColors;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.IOSelect;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;

/**
 * Supplies Output Window implementation through Lookup.
 * @author Jesse Glick, Tim Boudreau
 */
@ServiceProviders({
    @ServiceProvider(service=IOProvider.class, position=100),
    @ServiceProvider(service=InputOutputProvider.class, position=100)
})
public final class NbIOProvider extends IOProvider implements
        InputOutputProvider<InputOutput, OutputWriter, Integer, Integer> {

    private static final WeakHashMap<IOContainer, PairMap> containerPairMaps =
            new WeakHashMap<IOContainer, PairMap>();

    private static final String STDOUT = NbBundle.getMessage(NbIOProvider.class,
        "LBL_STDOUT"); //NOI18N

    private static final String NAME = "output2"; // NOI18N
    
    public OutputWriter getStdOut() {
        if (Controller.LOG) {
            Controller.log("NbIOProvider.getStdOut");
        }
        NbIO stdout = (NbIO) getIO(STDOUT, false);
        NbWriter out = stdout.writer();

        NbIO.post(new IOEvent(stdout, IOEvent.CMD_CREATE, true));
        //ensure it is not closed
        if (out != null && out.isClosed()) {
            try {
                out.reset();
                out = (NbWriter) stdout.getOut();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
                stdout = (NbIO) getIO(STDOUT, true);
                out = (NbWriter) stdout.getOut();
            }
        } else {
            out = (NbWriter) stdout.getOut();
        }
        return out;
    }
    
    
    @Override
    public InputOutput getIO(String name, boolean newIO) {
        return getIO (name, newIO, new Action[0], null);
    }
    
    @Override
    public InputOutput getIO(String name, Action[] toolbarActions) {
        return getIO (name, true, toolbarActions, null);
    }

    @Override
    public InputOutput getIO(String name, Action[] additionalActions, IOContainer ioContainer) {
        return getIO(name, true, additionalActions, ioContainer);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public InputOutput getIO(String name, boolean newIO,
            Action[] toolbarActions, IOContainer ioContainer) {
        if (Controller.LOG) {
            Controller.log("GETIO: " + name + " new:" + newIO);
        }
        IOContainer realIoContainer = ioContainer == null
                ? IOContainer.getDefault() : ioContainer;
        NbIO result;
        synchronized (containerPairMaps) {
            PairMap namesToIos = containerPairMaps.get(realIoContainer);
            result = namesToIos != null ? namesToIos.get(name) : null;
        }
        if (result == null || newIO) {
            result = new NbIO(name, toolbarActions, realIoContainer);
            synchronized (containerPairMaps) {
                PairMap namesToIos = containerPairMaps.get(realIoContainer);
                if (namesToIos == null) {
                    namesToIos = new PairMap();
                    containerPairMaps.put(realIoContainer, namesToIos);
                }
                namesToIos.add(name, result);
            }
            NbIO.post(new IOEvent(result, IOEvent.CMD_CREATE, newIO));
        }
        return result;
    }
    
    
    static void dispose (NbIO io) {
        IOContainer ioContainer = io.getIOContainer();
        if (ioContainer == null) {
            ioContainer = IOContainer.getDefault();
        }
        synchronized (containerPairMaps) {
            PairMap namesToIos = containerPairMaps.get(ioContainer);
            if (namesToIos != null) {
                namesToIos.remove(io);
                if (namesToIos.isEmpty()) {
                    containerPairMaps.remove(ioContainer);
                }
            }
        }
    }

    @Override
    public String getId() {
        return getName();
    }

    @Override
    public InputOutput getIO(String name, boolean newIO, Lookup lookup) {
        Action[] actions = lookup.lookup(Action[].class);
        IOContainer container = lookup.lookup(IOContainer.class);
        return getIO(name, newIO,
                actions == null ? new Action[0] : actions,
                container);
    }

    @Override
    public Reader getIn(InputOutput io) {
        return io.getIn();
    }

    @Override
    public OutputWriter getOut(InputOutput io) {
        return io.getOut();
    }

    @Override
    public OutputWriter getErr(InputOutput io) {
        return io.getErr();
    }

    @Override
    public void print(InputOutput io, OutputWriter writer, String text,
            Hyperlink link, OutputColor outputColor, boolean printLineEnd) {
        Color awtColor = outputColorToAwtColor(io, outputColor);
        OutputListener listener = hyperlinkToOutputListener(link);
        boolean listenerImportant = link != null && Hyperlinks.isImportant(link);
        if (io instanceof NbIO) {
            OutWriter out = ((NbIO) io).out();
            if (out != null) {
                out.print(text, listener, listenerImportant, awtColor, null,
                        OutputKind.OUT, printLineEnd);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Lookup getIOLookup(InputOutput io) {
        if (io instanceof NbIO) {
            return ((NbIO) io).getLookup();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void resetIO(InputOutput io) {
        if (io instanceof NbIO) {
            ((NbIO) io).reset();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void showIO(InputOutput io, Set<ShowOperation> operations) {
          if (operations.contains(ShowOperation.OPEN)
                && operations.contains(ShowOperation.MAKE_VISIBLE)
                && operations.size() == 2) {
            io.select();
        } else {
            IOSelect.select(io, showOperationsToIoSelect(operations));
        }
    }

    @Override
    public void closeIO(InputOutput io) {
        io.closeInputOutput();
    }

    @Override
    public boolean isIOClosed(InputOutput io) {
        return io.isClosed();
    }

    @Override
    public Integer getCurrentPosition(InputOutput io,
            OutputWriter writer) {
        if (io instanceof NbIO) {
            OutWriter out = ((NbIO) io).out();
            int size = 0;
            if (out != null) {
                size = out.getLines().getCharCount();
            }
            return size;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void scrollTo(InputOutput io, OutputWriter writer,
            Integer position) {
        if (io instanceof NbIO) {
            NbIO.post(new IOEvent((NbIO) io, IOEvent.CMD_SCROLL, position));
        }
    }

    @Override
    public Integer startFold(InputOutput io, OutputWriter writer,
            boolean expanded) {
        if (io instanceof NbIO) {
            return ((NbIO) io).startFold(expanded);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void endFold(InputOutput io, OutputWriter writer, Integer fold) {
        if (io instanceof NbIO) {
            ((NbIO) io).endFold(fold);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setFoldExpanded(InputOutput io, OutputWriter writer,
            Integer fold, boolean expanded) {
        if (io instanceof NbIO) {
            NbIO nbIO = ((NbIO) io);
            if (expanded) {
                nbIO.out().getLines().showFoldAndParentFolds(fold);
            } else {
                nbIO.out().getLines().hideFold(fold);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getIODescription(InputOutput io) {
        if (io instanceof NbIO) {
            return ((NbIO) io).getToolTipText();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void setIODescription(InputOutput io, String description) {
        if (io instanceof NbIO) {
            ((NbIO) io).setTooltipText(description);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Translate set of {@link ShowOperation}s to set of
     * {@link IOSelect.AdditionalOperation}s.
     */
    private Set<IOSelect.AdditionalOperation> showOperationsToIoSelect(
            Set<ShowOperation> operations) {
        Set<IOSelect.AdditionalOperation> res
                = EnumSet.noneOf(IOSelect.AdditionalOperation.class);
        for (ShowOperation so : operations) {
            switch (so) {
                case OPEN:
                    res.add(IOSelect.AdditionalOperation.OPEN);
                    break;
                case MAKE_VISIBLE:
                    res.add(IOSelect.AdditionalOperation.REQUEST_VISIBLE);
                    break;
                case ACTIVATE:
                    res.add(IOSelect.AdditionalOperation.REQUEST_ACTIVE);
                    break;
            }
        }
        return res;
    }

    /**
     * Convert a hyperlink to an output listener.
     *
     * @param link The hyperlink.
     * @return The wrapping output listener.
     */
    private static OutputListener hyperlinkToOutputListener(
            final Hyperlink link) {

        if (link == null) {
            return null;
        }
        return new OutputListener() {

            @Override
            public void outputLineSelected(OutputEvent ev) {
            }

            @Override
            public void outputLineAction(OutputEvent ev) {
                Hyperlinks.invoke(link);
            }

            @Override
            public void outputLineCleared(OutputEvent ev) {
            }
        };
    }

    private static Color outputColorToAwtColor(InputOutput io,
            OutputColor color) {

        if (color == null) {
            return null;
        }
        OutputColorType type = OutputColors.getType(color);
        if (type == OutputColorType.RGB) {
            return new Color(OutputColors.getRGB(color));
        } else {
            switch (type) {
                case DEBUG:
                    return IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG);
                case FAILURE:
                    return IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE);
                case WARNING:
                    return IOColors.getColor(io, IOColors.OutputType.LOG_WARNING);
                case SUCCESS:
                    return IOColors.getColor(io, IOColors.OutputType.LOG_SUCCESS);
                default:
                    return null;
            }
        }
    }
}

