
function checkPasswordMatch(confirmPassword) {
	if (confirmPassword.value != $("#password").val()) {
		confirmPassword.setCustomValidity("패스워드가 서로 다릅니다.");
	} else {
		confirmPassword.setCustomValidity("");
	}
}