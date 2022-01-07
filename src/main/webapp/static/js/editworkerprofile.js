function updateProfileEditForm(data) {
    $("#edit-name").val(data.name);
    $("#edit-firstname").val(data.firstName);
    $("#edit-patronym").val(data.patronym);
    $("#edit-surname").val(data.surname);
    $("#edit-birthdate").val(data.birthDate);
    $("#edit-skills").val(data.skills);
    $("#edit-phone").val(data.contacts.phoneNumber);
    $("#edit-email").val(data.contacts.email);
    $("#edit-vk").val(data.contacts.vk);
    $("#edit-telegram").val(data.contacts.telegram);
    $("#edit-facebook").val(data.contacts.facebook);
    $("#edit-other").val(data.contacts.other);
}

function updateProfile() {
    if ($(document).ready() && profileCache && !isNaN(profileCache.id)) {
        rest("GET", "/" + profileCache.id + "/profile", null, function (data) {
            console.log(data);
            updateWorkerProfile(data);
            updateProfileEditForm(data);
            profileCache = data;
        });
    }
}

$(document).on('click', '#edit-profile-button', function () {
    if (profileCache && !isNaN(profileCache.id)) {
        updateProfileEditForm(profileCache);
    } else {
        updateProfile();
    }
});

$(document).on('submit','#profile-edit-form', function (event) {
    event.preventDefault();

    let element = $("#profile-edit-submit");
    element.attr("disabled", "disabled");
    element.addClass("inactivated");
    setTimeout(function () {
        $("#profile-edit-submit").removeAttr("disabled").removeClass("inactivated");
    }, 5000);

    let form = {};

    form['profileName'] = $("#edit-name").val();
    form['firstName'] = $("#edit-firstname").val();
    form['patronym'] = $("#edit-patronym").val();
    form['surname'] = $("#edit-surname").val();
    form['birthDate'] = $("#edit-birthdate").val();
    form['sex'] = $("input[type='radio'][name='sex']:checked").val();
    form['skills'] = $("#edit-skills").val();
    form['phoneNumber'] = $("#edit-phone").val();
    form['email'] = $("#edit-email").val();
    form['vk'] = $("#edit-vk").val();
    form['telegram'] = $("#edit-telegram").val();
    form['facebook'] = $("#edit-facebook").val();
    form['other'] = $("#edit-other").val();

    rest("POST", "/worker/" + profileCache.id, form, function (data) {
        if (data.error) {
            console.error("POST " + api() + "/worker/" + profileCache.id, data.errors[0], data.error);
            Swal.fire({
                text: data.error,
                type: 'error',
                confirmButtonColor: '#fb2a79'
            });
            return;
        }
        console.log(data);

        updateWorkerProfile(data);
        updateProfileEditForm(data);
        profileCache = data;

        Swal.fire({
            text: 'Изменения успешно сохранены',
            confirmButtonColor: '#195fd4'
        });
    });
});