"use strict"

/**
 * Las actividades tienen en sus propiedades cual es la siguiente actividad y cual es la anterior.
 * De esta forma desde las actividades iniciales, se continua la ejecucion, habran actividades especificas
 * cuya funcionalidad sea dividir el workflow, trabajar en paralelo, aplicar operaciones logicas, etc. 
 */
module.exports = {
    name: "A workflow example",
    activities: {
        id: { params: {}, nextActivity: id2, previousActivity: null },
        id2: { params: {}, type: "fork", previousActivity: id, nextActivity: id3 },
        id3: { params: {}, previousActivity: id2, nextActivity: null },
        id4: { params: {}, previousActivity: id2, nextActivity: null }
    },
    staticResources: {
        myDBid3: { params: { user: "", password: ""}, parentActivity: [id] } 
    },
    flow: {
        // The initial activities in which the flow begins
        start : [id]
    }
};
