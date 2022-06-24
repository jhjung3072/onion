$(document).ready(function() {
	$(".linkRemove").on("click", function(evt) {
		evt.preventDefault();
		removeProduct($(this));
	});		
});

function showEmptyWatchlist() {
	$("#sectionEmptyWatchlistMessage").removeClass("d-none");
}

function removeProduct(link) {
	let url = link.attr("href");

	$.ajax({
		type: "DELETE",
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		}
	}).done(function(response) {
		let rowNumber = link.attr("rowNumber");
		removeProductHTML(rowNumber);

		showModalDialog("관심목록", response);
		
	}).fail(function() {
		showErrorModal("상품 삭제중에 에러가 발생했습니다.");
	});				
}

function removeProductHTML(rowNumber) {
	$("#row" + rowNumber).remove();
	$("#blankLine" + rowNumber).remove();
}

