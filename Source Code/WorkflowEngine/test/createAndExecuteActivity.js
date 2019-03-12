"use strict"

const Activity = require('./../my_modules/WorkflowEngine/Activities/Activity');

let echoActivity = new Activity("echo", "console.log('Preparando recursos...'); var x = 'Un valor cool';", "console.log(x);",
    "if(nextActivity.tName == 'lola') { console.log('Si que es lola la siguiente actividad.'); var result = 'El resultado del valor cool'; }", { nextActivity: "lola" });


console.log('Preparing...');
echoActivity.prepare();
console.log('Executing...');
echoActivity.execute();
console.log('Publishing...');
echoActivity.publish();