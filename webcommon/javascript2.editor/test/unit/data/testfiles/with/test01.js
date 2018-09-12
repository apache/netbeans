var roman = new Man("Roman", "Php");

roman.getFirstName();
console.log();  
with(roman) {
   console.log(getFirstName()); 
}