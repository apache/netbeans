// assigning to exports will not modify module, must use module.exports
module.exports = function(width) {
  return {
    area: function() {
      return width * width;
    }
  };
}