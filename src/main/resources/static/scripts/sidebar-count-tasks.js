// Count all the <li> elements in the dropdown menu
var count = document.getElementById("tasks-menu").getElementsByTagName("li").length;
// Set the count in the badge
document.getElementById("tasks-count").innerHTML = count;