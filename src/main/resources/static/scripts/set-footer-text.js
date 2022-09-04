// Updates the copyright footer
const today = new Date();
const year = today.getFullYear();

let elem = document.getElementById("copyright-footer");
elem.innerHTML = "©" + year + ' made with <i class="fa fa-heart" style="color:red"></i> by - <a target="_blank" rel="noopener noreferrer" href="https://dan-koller.github.io/"> Dan Koller</a>';