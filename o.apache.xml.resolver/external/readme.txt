Created by: Samaresh Panda
Last updated: 08-22-2007
Current Version being used: xml-commons-resolver-1.2

resolver.jar is a third party library being used in Netbeans.
This came from http://xml.apache.org/commons/ with Apache license 2.0.

Earlier, it was patched on xml-commons-resolver-1.1 to cater to our needs.
See resolver.patch file for the patches applied xml-commons-resolver-1.1.
The patch on org.apache.xml.resolver.tools.CatalogResolver.java fixes
these two issues:

http://www.netbeans.org/issues/show_bug.cgi?id=98212
http://www.netbeans.org/issues/show_bug.cgi?id=112679

Hence we have applied the following patches on xml-commons-resolver-1.2:

1. Add a new API to org.apache.xml.resolver.Catalog.java:

  /**
   * Return all registered public IDs.
   */
  public Iterator getPublicIDs() {
      Vector v = new Vector();
      Enumeration enumeration = catalogEntries.elements();

      while (enumeration.hasMoreElements()) {
        CatalogEntry e = (CatalogEntry) enumeration.nextElement();
        if (e.getEntryType() == PUBLIC) {
            v.add(e.getEntryArg(0));
        }
      }
      return v.iterator();
  }

2. Handle null in org.apache.xml.resolver.CatalogManager.java::readProperties()
  + if (propertyFile == null) return;

Note: 29 Jan 2009 Marek Slama

As we cannot put forked resolver package into debian repository we had to use following workaround.

1. We added new method into org.apache.xml.resolver.Catalog.java as described above. It is compatible
change.

2. We added 2 new classes org.apache.xml.resolver.NbCatalogManager.java and org.apache.xml.resolver.tools.NbCatalogResolver.java
into resolver with necessary incompatible changes and all usages of CatalogManager and CatalogResolver in NB codebase
are replaced by NbCatalogManager and NbCatalogResolver.
