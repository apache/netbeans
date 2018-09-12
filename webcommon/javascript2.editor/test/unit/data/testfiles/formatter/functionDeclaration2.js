
function compute1() {
    a = something + 5;
}

function compute2(a, b) {
    a = something + 5;
}

function compute3(a) {
    a = something + 5;
}

  (function dummy() {
      a =5;
  }).test ();


  function Car (color, maker) {
    this.color = color;
    this.maker = maker;

    this.getColor = function () {
        return this.color;
    }
    this.getMaker = function (a) {
        return this.maker;
    }
}