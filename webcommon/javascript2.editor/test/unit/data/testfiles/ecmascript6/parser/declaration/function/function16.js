function $H(object) {
  if (object && object.constructor == Hash) return object;
  return new Hash(object);
};