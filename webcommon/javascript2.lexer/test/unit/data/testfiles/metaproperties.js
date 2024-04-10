function demo() {
    if (new.target) {
        console.log("Invoked with new");
    } else {
        console.log("Invoked without new");
    }
}

console.log(import.meta.url);
console.log(import.meta.resolve("dummy.js"));
