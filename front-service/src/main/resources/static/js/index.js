let btnToCreatePatient = document.getElementById('btn-creation-patient')
let divToCreatePatient = document.getElementById("patient-creation-div")
let formPatient = document.getElementById("form-patient")

// display patient record when click on table row

function rowClicked(patientId) {
    location.href = "/patient-record/" + patientId
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
        formPatient.reset()
        if (fieldsOnError !={}){
            document.location.reload();
        }
    }
}
