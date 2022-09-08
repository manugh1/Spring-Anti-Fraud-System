function getCurrentDate() {
    let date = new Date();
    let year = date.getFullYear();
    let month = String(date.getMonth() + 1).padStart(2, '0');
    let day = String(date.getDate()).padStart(2, '0');
    let hours = String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');
    let seconds = String(date.getSeconds()).padStart(2, '0');

    return year + "-" + month + "-" + day + "T" + hours + ":" + minutes + ":" + seconds;
}

function postTransaction() {
    let object = {
        // Convert amount to float and replace comma with dot
        amount: parseFloat(document.getElementById("amount").value.replace(",", ".")),
        ip: document.getElementById("ip-address").value,
        number: document.getElementById("card-number").value,
        region: document.getElementById("region").value,
        date: getCurrentDate()
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("POST", '/api/antifraud/transaction', false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        // Separate the result and info field from the response
        let response = JSON.parse(xhr.responseText);
        let result = response.result;
        let info = response.info;

        alert("Transaction successfully submitted!\nInfo: " + info + "\nResult: " + result);
    } else {
        alert("Error submitting your transaction: " + xhr.status + " " + xhr.statusText);
    }
}