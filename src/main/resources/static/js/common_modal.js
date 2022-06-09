function showModalDialog(title, message) {
	$("#modalTitle").text(title);
	$("#modalBody").text(message);
	$("#modalDialog").modal();
}

function showErrorModal(message) {
	showModalDialog("에러", message);
}

function showWarningModal(message) {
	showModalDialog("경고", message);
}	