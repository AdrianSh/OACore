"use strict";
const path = require("path");
const express = require("express");

const app = express();

app.use('/css', express.static(path.join(__dirname, "css")));
app.use('/img', express.static(path.join(__dirname, "img")));
app.use('/js', express.static(path.join(__dirname, "js")));


app.use((request, response, next) => { next(new Error("pagina no encontrada: " + request.url)); });
app.use((error, request, response, next) => {
	response.status(500); // Código 500: Internal server error
	response.render("error", { mensaje: error.message, pila: error.stack });
});

app.listen(3000, function (err) {
	if (err) {
		console.error("No se pudo inicializar el servidor: " + err.message);
	} else {
		console.log("Servidor arrancado en el puerto 3000");
	}
});