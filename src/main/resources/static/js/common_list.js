$(document).ready(function() {
	$(".link-delete").on("click", function(e) {
		e.preventDefault();
		showDeleteConfirmModal($(this), entityName);
	});
});

function clearFilter() {
	window.location = moduleURL;	
}

function showDeleteConfirmModal(link, entityName) {
	entityId = link.attr("entityId");
	
	$("#yesButton").attr("href", link.attr("href"));	
	$("#confirmText").text("정말 삭제하시겠습니까?");
	$("#confirmModal").modal();	
}

function handleDetailLinkClick(cssClass, modalId) {
	$(cssClass).on("click", function(e) {
		e.preventDefault();
		linkDetailURL = $(this).attr("href");
		$(modalId).modal("show").find(".modal-content").load(linkDetailURL);
	});		
}

function handleDefaultDetailLinkClick() {
	handleDetailLinkClick(".link-detail", "#detailModal");	
}