function WAFunction(stack) {
    var wa = stack.pop() + stack.pop();
    if (stack.pop() <= stack.pop()) {
        stack.pop = stack.pop;
    }
    if (stack.pop() > stack.pop()) {
        stack.push(stack.pop() + stack.pop()); 
    }
}