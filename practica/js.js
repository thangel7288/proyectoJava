let title = document.getElementById("title"); 

let mainDiv = document.querySelector(".main-div"); 
console.log(mainDiv);

let listItems = document.querySelectorAll("ul.list > li");
console.log(listItems);

title.innerText = ("Manipulando el DOOM");
title.innerHTML = "<em>holaaa</em>";

let oldDiv = document.querySelector(".old-div");
document.body.removeChild(oldDiv);
let newDiv = document.createElement("div");

newDiv.innerText = ("soy un nuevo texto");
document.body.appendChild(newDiv);

let button = document.querySelector("button");
button.addEventListener("click", function() {
    alert("boton presionado");
})

let input = document.querySelector("itemInput");
let addButton = document.querySelector("#addButton");
let list = document.querySelector("#dinamycList");

addButton.addEventListener("click", function ())

