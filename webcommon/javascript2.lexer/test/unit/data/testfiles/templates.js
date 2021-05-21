`string text`

`string text line 1
 string text line 2`

`string text ${expression} string text`

console.log(`string text line 1
string text line 2`);
// "string text line 1
// string text line 2"

var a = 5;
var b = 10;
console.log(`Fifteen is ${a + b} and\nnot ${2 * a + b}.`);
// "Fifteen is 15 and
// not 20."

var a = 5;
var b = 10;

function tag(strings, ...values) {
  console.log(strings[0]); // "Hello "
  console.log(strings[1]); // " world "
  console.log(values[0]);  // 15
  console.log(values[1]);  // 50

  return "Bazinga!";
}

tag`Hello ${ a + b } world ${ a * b}`;
// "Bazinga!"

function tag(strings, ...values) {
  console.log(strings.raw[0]); 
  // "string text line 1 \\n string text line 2"
}

tag`string text line 1 \n string text line 2`;

String.raw`Hi\n${2+3}!`;
// "Hi\\n5!"

`${console.warn("this is",this)}`; // "this is" Window

let a = 10;
console.warn(`${a+=20}`); // "30"
console.warn(a); // 30
