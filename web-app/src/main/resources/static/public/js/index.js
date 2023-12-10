let btnToCreatePatient = document.getElementById('btn-creation-patient')
let divToCreatePatient = document.getElementById("patient-creation-div")
let formPatient = document.getElementById("form-patient")

// display patient record when click on table row

function rowClicked(patientId) {

    location.href = "/patients/" + patientId + "?patient_update=false"
}

// toggle form to create new patient

function toggleFormPatient() {
    if (divToCreatePatient.style.display === 'none') {
        divToCreatePatient.style.display = "block"
        btnToCreatePatient.classList.remove('btn-primary')
        btnToCreatePatient.classList.add('btn-outline-primary')
        btnToCreatePatient.innerText = 'Cancel'
    } else {
        divToCreatePatient.style.display = "none";
        btnToCreatePatient.classList.add('btn-primary')
        btnToCreatePatient.classList.remove('btn-outline-primary')
        btnToCreatePatient.innerText = 'Add new patient'

        document.querySelectorAll('#form-patient .field-patient').forEach(element => {
            element.querySelector('ul').innerHTML = ''
            console.log(element.querySelector('.form-control'))
            element.querySelector('.form-control').classList.remove('is-invalid')
            element.querySelector('.form-control').value = ''
        });
    }
}

function deletePatient(urlPatientToDelete) {
    const deleteModal = new bootstrap.Modal('#deleteModal')
    document.querySelector('.modal-footer a').setAttribute("href",urlPatientToDelete)
    deleteModal.show()
}

function myEventHandler(e) {
    if (!e)
        e = Event;

    //IE9 & Other Browsers
    if (e.stopPropagation) {
        e.stopPropagation();
    }
    //IE8 and Lower
    else {
        e.cancelBubble = true;
    }
}
