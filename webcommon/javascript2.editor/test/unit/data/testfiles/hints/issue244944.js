function nic() {
    if (place instanceof Place && place.isValid()) {
        this[identifier] = place;
    } else {
        throw new TypeError("Provided place for '" + identifier + "' is not a valid option. ")
    }  
} 