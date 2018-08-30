var a = x +
        `something ${(function () {
          console.log('Arguments:', process.argv.length);
    console.log(process.argv);
})} dsfsdfsd`;

var a = x +
        (function () {
          console.log('Arguments:', process.argv.length);
    console.log(process.argv);
});