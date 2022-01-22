function archiveTask() {
    const taskId = Todo.getValue("currentTask");
    const profile = Todo.getValue("profileCache");
    const resultEstimate = parseInt($('.stars-list .star.selected').last().data('value'), 10);

    if (taskId && !isNaN(taskId) && profile.id && !isNaN(profile.id)) {
        rest("POST", `/manager/${profile.id}/tasks/${taskId}/archive`, resultEstimate, function (data) {
            if (data.error) {
                console.error("POST " + api() + `/manager/${profile.id}/tasks/${taskId}/archive`, data.status.value, data.error);
                showError(data.error);
                return;
            }
            redirectTimer(1, root() + "/my?ok=" + 'archive');
        });
    } else {
        console.error("ERROR: ", profile, taskId);
    }
}

window.addEventListener("DOMContentLoaded", () => {

    const element = $('.stars-list .star');
    /* 1. Visualizing things on Hover - See next part for action on click */
    element.on('mouseover', function(){
        const onStar = parseInt($(this).data('value'), 10); // The star currently mouse on

        // Now highlight all the stars that's not after the current hovered star
        $(this).parent().children('.star').each(function(e){
            if (e < onStar) {
                $(this).addClass('rate100');
            }
            else {
                $(this).removeClass('rate100');
            }
        });

    }).on('mouseout', function(){
        $(this).parent().children('.star').each(function(e){
            $(this).removeClass('rate100');
        });
    });


    /* 2. Action to perform on click */
    element.click(function(){
        const onStar = parseInt($(this).data('value'), 10); // The star currently selected
        const stars = $(this).parent().children('.star');

        for (let i = 0; i < stars.length; i++) {
            $(stars[i]).removeClass('selected');
        }

        for (let i = 0; i < onStar; i++) {
            $(stars[i]).addClass('selected');
        }
    });

    $('#send-to-archive-btn').click(function () {
        archiveTask();
    });
});