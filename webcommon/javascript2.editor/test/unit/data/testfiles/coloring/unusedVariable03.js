MySpace.addOrRemoveCSSClassName = function(
    add,
    domElement,
    className) // TODO move into some "domutils" class
  {
    var func = (add)
                 ? adf.mf.internal.amx.addCSSClassName
                 : adf.mf.internal.amx.removeCSSClassName;

    return func(domElement, className);
  };