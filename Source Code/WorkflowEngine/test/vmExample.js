"use strict"

/**
 * The vm module provides APIs for compiling and running code within V8 Virtual Machine contexts.
 * TODO: The vm module is not a security mechanism. Do not use it to run untrusted code.
 * The term "sandbox" is used throughout these docs simply to refer to a separate context,
 * and does not confer any security guarantees.
 * TODO: One can provide the context by "contextifying" a sandbox object.
 * The sandboxed code treats any property in the sandbox like a global variable.
 * ! Any changes to global variables caused by the sandboxed code are reflected in the sandbox object.
 */

const vm = require('vm');

const x = 1;

const sandbox = { x: 2 };
vm.createContext(sandbox); // Contextify the sandbox.

const code = 'x += 40; var y = 17;';
// `x` and `y` are global variables in the sandboxed environment.
// Initially, x has the value 2 because that is the value of sandbox.x.
vm.runInContext(code, sandbox);

console.log(sandbox.x); // 42
console.log(sandbox.y); // 17

console.log(x); // 1; y is not defined.