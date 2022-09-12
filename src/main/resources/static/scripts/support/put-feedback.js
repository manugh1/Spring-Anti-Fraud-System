function putFeedback() {
    let object = {
        transactionId: document.getElementById("transactionId").value,
        feedback: document.getElementById("feedback").value
    };

    let json = JSON.stringify(object);

    let xhr = new XMLHttpRequest();
    xhr.open("PUT", "/api/antifraud/transaction", false);
    xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
    xhr.send(json);

    if (xhr.status == 200 || xhr.status == 201) {
        alert("Feedback updated successfully for transaction " + object.transactionId);
    } else {
        alert("Error updating feedback for transaction " + object.transactionId + ". Status: " + xhr.status);
    }
}