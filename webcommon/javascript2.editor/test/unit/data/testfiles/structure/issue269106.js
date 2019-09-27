
var thrower = { [Symbol.toPrimitive]: () => FAIL };

// Tests that a native conversion function is included in the
// stack trace.
function testTraceNativeConversion(nativeFunc) {
  var nativeFuncName = nativeFunc.name;
  try {
    nativeFunc(thrower);
    assertUnreachable(nativeFuncName);
  } catch (e) {
    assertTrue(e.stack.indexOf(nativeFuncName) >= 0, nativeFuncName);
  }
}

