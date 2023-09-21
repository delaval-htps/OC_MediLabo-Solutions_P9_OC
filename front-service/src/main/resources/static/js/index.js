let btnToCreatePatient = document.getElementById('btn-creation-patient')
let formToCreatePatient = document.getElementById('form-creation-patient')


// display patient record when click on table row

function rowClicked(patientId) {
    location.href = "/patient-record/" + patientId
}

// toggle form to create new patient
//TODO if there is a bindingresult then button add new patient must display cancel

function toggleFormPatient() {
    if (formToCreatePatient.style.display === 'none') {
        formToCreatePatient.style.display = "block"
        btnToCreatePatient.classList.remove('btn-primary')
        btnToCreatePatient.classList.add('btn-outline-primary')
        btnToCreatePatient.innerText = 'Cancel'
    } else if (fieldsOnError != ''){
        formToCreatePatient.style.display = "none";
        btnToCreatePatient.classList.add('btn-primary')
        btnToCreatePatient.classList.remove('btn-outline-primary')
        btnToCreatePatient.innerText = 'Add new patient'
    }
}
