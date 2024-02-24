#Signature file v4.1
#Version 1.56

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface org.netbeans.api.editor.guards.DocumentGuards
meth public abstract boolean isPositionGuarded(int,boolean)
meth public abstract int adjustPosition(int,boolean)
meth public abstract int findNextBlock(int,boolean)

CLSS public abstract org.netbeans.api.editor.guards.GuardedSection
meth public boolean contains(javax.swing.text.Position,boolean)
meth public boolean isValid()
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public javax.swing.text.Position getCaretPosition()
meth public javax.swing.text.Position getEndPosition()
meth public javax.swing.text.Position getStartPosition()
meth public void deleteSection()
meth public void removeSection()
meth public void setName(java.lang.String) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds delegate,impl,offset

CLSS public final org.netbeans.api.editor.guards.GuardedSectionManager
meth public java.lang.Iterable<org.netbeans.api.editor.guards.GuardedSection> getGuardedSections()
meth public org.netbeans.api.editor.guards.InteriorSection createInteriorSection(javax.swing.text.Position,java.lang.String) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.guards.InteriorSection findInteriorSection(java.lang.String)
meth public org.netbeans.api.editor.guards.SimpleSection createSimpleSection(javax.swing.text.Position,java.lang.String) throws javax.swing.text.BadLocationException
meth public org.netbeans.api.editor.guards.SimpleSection findSimpleSection(java.lang.String)
meth public org.netbeans.api.editor.guards.SimpleSection protectSimpleRegion(javax.swing.text.Position,javax.swing.text.Position,java.lang.String) throws javax.swing.text.BadLocationException
meth public static org.netbeans.api.editor.guards.GuardedSectionManager getInstance(javax.swing.text.StyledDocument)
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.api.editor.guards.InteriorSection
meth public java.lang.String getBody()
meth public java.lang.String getFooter()
meth public java.lang.String getHeader()
meth public javax.swing.text.Position getBodyEndPosition()
meth public javax.swing.text.Position getBodyStartPosition()
meth public void setBody(java.lang.String)
meth public void setFooter(java.lang.String)
meth public void setHeader(java.lang.String)
supr org.netbeans.api.editor.guards.GuardedSection

CLSS public final org.netbeans.api.editor.guards.SimpleSection
meth public void setText(java.lang.String)
supr org.netbeans.api.editor.guards.GuardedSection

CLSS public abstract interface org.netbeans.spi.editor.guards.GuardedEditorSupport
meth public abstract javax.swing.text.StyledDocument getDocument()

CLSS public abstract interface org.netbeans.spi.editor.guards.GuardedRegionMarker
meth public abstract void protectRegion(int,int)
meth public abstract void unprotectRegion(int,int)

CLSS public abstract org.netbeans.spi.editor.guards.GuardedSectionsFactory
cons public init()
meth public abstract org.netbeans.spi.editor.guards.GuardedSectionsProvider create(org.netbeans.spi.editor.guards.GuardedEditorSupport)
meth public static org.netbeans.spi.editor.guards.GuardedSectionsFactory find(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.editor.guards.GuardedSectionsProvider
meth public abstract java.io.Reader createGuardedReader(java.io.InputStream,java.nio.charset.Charset)
meth public abstract java.io.Writer createGuardedWriter(java.io.OutputStream,java.nio.charset.Charset)

CLSS public abstract org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider
cons protected init(org.netbeans.spi.editor.guards.GuardedEditorSupport)
cons protected init(org.netbeans.spi.editor.guards.GuardedEditorSupport,boolean)
innr public final Result
intf org.netbeans.spi.editor.guards.GuardedSectionsProvider
meth public abstract char[] writeSections(java.util.List<org.netbeans.api.editor.guards.GuardedSection>,char[])
meth public abstract org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider$Result readSections(char[])
meth public final java.io.Reader createGuardedReader(java.io.InputStream,java.nio.charset.Charset)
meth public final org.netbeans.api.editor.guards.InteriorSection createInteriorSection(java.lang.String,int,int,int,int) throws javax.swing.text.BadLocationException
meth public final org.netbeans.api.editor.guards.SimpleSection createSimpleSection(java.lang.String,int,int) throws javax.swing.text.BadLocationException
meth public java.io.Writer createGuardedWriter(java.io.OutputStream,java.nio.charset.Charset)
supr java.lang.Object
hfds impl,useReadersWritersOnSet

CLSS public final org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider$Result
 outer org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider
cons public init(char[],java.util.List<org.netbeans.api.editor.guards.GuardedSection>)
meth public char[] getContent()
meth public java.util.List<org.netbeans.api.editor.guards.GuardedSection> getGuardedSections()
supr java.lang.Object
hfds content,sections

