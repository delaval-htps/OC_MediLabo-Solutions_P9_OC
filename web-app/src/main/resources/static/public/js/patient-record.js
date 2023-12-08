const urlParams = new URLSearchParams(window.location.search)
const update = urlParams.get('update')
console.log(update)

/* part for patient*/

if (update == 'true' || fieldsOnError == {}) {
    document.querySelector('.bi-pencil-square').style.display = 'none'
} else {
    disablePersonalInformation()
}

function disablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = "none"
    document.querySelector('#form-update-patient fieldset').setAttribute("disabled", "true")
    document.getElementById('genre').classList.replace('form-select', 'form-control')
    document.getElementById('dateOfBirth').disabled = true
    document.querySelector('.bi-pencil-square').style.display = 'block'
    document.querySelector('.bi-x-square').style.display = 'none'
}

function enablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = 'flex'
    document.querySelector('#form-update-patient fieldset').removeAttribute("disabled")
    document.getElementById('genre').classList.replace('form-control', 'form-select')
    document.getElementById('dateOfBirth').disabled = false
    document.querySelector('.bi-pencil-square').style.display = 'none'
    document.querySelector('.bi-x-square').style.display = 'block'
}

/* part for note creation */
console.log(fieldsOnError)

var noteForm = document.querySelector('#note-creation-form')
var noteTable = document.querySelector('#notes-table')

/* case of bindingResult we have to keep form open with new date*/
if (Object.keys(fieldsOnError).length != 0) {
    noteForm.style.display = ''
    noteTable.style.display = 'none'
    document.querySelector('#creation-note-btn').innerText = 'Cancel'
    document.querySelector('#creation-note-date').value = new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0]
}

function toggleNoteCreationForm(element) {
    element.innerText = element.innerText === 'Create a note' ? 'Cancel' : 'Create a note'
    document.querySelector('#creation-note-date').value = new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0]
    noteTable.style.display = noteTable.style.display === 'none' ? '' : 'none'
    noteForm.style.display = noteForm.style.display === 'none' ? '' : 'none'
}

function notesRowClicked(patientId,noteId) {
    location.href = "/notes/" + noteId + "/patient/" + patientId 
}