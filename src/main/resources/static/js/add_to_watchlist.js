$(document).ready(function() {
	$("#buttonAdd2Watchlist").on("click", function(evt) {
		addToWatchlist();
	});
});

function addToWatchlist() {
	url = contextPath + "watchlist/add/" + productId;
	$.ajax({
		type: "POST",
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		}
	}).done(function(response) {
		showModalDialog("관심목록", response);
	}).fail(function() {
		showErrorModal("상품을 관심목록에 담는 중에 에러가 발생했습니다.");
	});
}