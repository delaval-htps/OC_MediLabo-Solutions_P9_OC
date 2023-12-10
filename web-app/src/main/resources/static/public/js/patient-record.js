const urlParams = new URLSearchParams(window.location.search)
var patientUpdate = urlParams.get('patient_update')
var noteStage = urlParams.get('note_update')
/**
 * noteStage represents stage of notes:
 * - first one : 'all' , we have the list of all notes for the patient 
 * - second one : 'edition', we have just edit one note without be able to modify it
 * - third one : 'update' , we have one note for update it 
 */
console.log('patientUpdate :' + patientUpdate)
console.log('noteUpdate :' + noteStage)
console.log('note: ' + JSON.stringify(note))

/* part for patient*/

if (patientUpdate === "true") {
    enablePersonalInformation()
}
if (patientUpdate === "false") {
    disablePersonalInformation()
}
function disablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = "none"
    document.querySelector('#form-update-patient fieldset').setAttribute("disabled", "true")
    document.getElementById('genre').classList.replace('form-select', 'form-control')
    document.getElementById('dateOfBirth').disabled = true
    document.querySelector('.bi-pencil-square').style.display = 'block'
    document.querySelector('.bi-x-square').style.display = 'none'
    patientUpdate = false
}

function enablePersonalInformation() {
    document.querySelector('#div-submit-btn').style.display = 'flex'
    document.querySelector('#form-update-patient fieldset').removeAttribute("disabled")
    document.getElementById('genre').classList.replace('form-control', 'form-select')
    document.getElementById('dateOfBirth').disabled = false
    document.querySelector('.bi-pencil-square').style.display = 'none'
    document.querySelector('.bi-x-square').style.display = 'block'
    patientUpdate = true
}

/* part for note creation */
var noteForm = document.querySelector('#note-creation-form')
var noteTable = document.querySelector('#notes-table')
var noteDate = document.querySelector('#creation-note-date')
var creationNoteBtn = document.querySelector('#creation-note-btn')
var saveNoteBtn = document.querySelector('#save-note-btn')
var noteContent = document.querySelector('#creation-note-content')

console.log("noteDate.value: " + noteDate.value);


/**
 * case of bindingResult only for note we have to keep form open with new date
 * case of note not null (row clicked to view note selected) display information of note
 */
if ((Object.keys(fieldsOnError).length != 0 && patientUpdate == undefined) || note.id != null) {
    noteForm.style.display = ''
    noteTable.style.display = 'none'
    creationNoteBtn.innerText = 'Cancel'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
    /**
     * case of just display a note 
     */
    if (noteStage === 'false') {
        saveNoteBtn.style.display = 'none'
        noteContent.setAttribute('readonly', true)
    }
    /**
     * case of update note
     */
    if (noteStage === 'true') {
        saveNoteBtn.innerText = 'Update note'
        noteForm.action = "/notes/update/" + note.id + "?patient_update=" + patientUpdate
    }
}

// if (noteStage === 'all') {
//     noteForm.style.display = 'none'
//     noteTable.style.display = 'block'
// }

// if (noteStage === 'edition') {
//     saveNoteBtn.style.display = 'none'
//     noteContent.setAttribute('readonly', true)
//     noteForm.style.display = ''
//     noteTable.style.display = 'none'
//     creationNoteBtn.innerText = 'Cancel'
//     noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
// }

// if (noteStage === 'update'){
//     saveNoteBtn.innerText = 'Update note' 
//     noteForm.action = "/notes/update/" + note.id + "?patient_update=" + patientUpdate
//     saveNoteBtn.style.display = 'block'
//     noteContent.removeAttribute('readonly')
//     noteForm.style.display = ''
//     noteTable.style.display = 'none'
//     creationNoteBtn.innerText = 'Cancel'
//     noteDate.value =  noteDate.value
// }


/**
 * function to display form to create a note 
 * @param {*} btn btn of creation of note that was clicked 
 */
function toggleNoteCreationForm(btn) {
    // when click to cancel creation or update : reset form field
    if (btn.innerText === 'Cancel' || successMessage != undefined) {
        resetFieldNoteForm()
    }
    btn.innerText = btn.innerText === 'Create a note' ? 'Cancel' : 'Create a note'
    noteDate.value = noteDate.value.length === 0 ? new Date(Date.now()).toISOString().replace('T', ' ').split('.')[0] : noteDate.value
    noteTable.style.display = noteTable.style.display === 'none' ? '' : 'none'
    noteForm.style.display = noteForm.style.display === 'none' ? '' : 'none'
    noteForm.action = "/notes/create?patient_update=" + patientUpdate
}

/**
 * function to display selected note when its row was clicked using fields of creation form 
 * @param {*} patientId id of related patient of note
 * @param {*} noteId id of note selected
 */
function notesRowClicked(patientId, noteId) {
    location.href = "/notes/" + noteId + "/patient/" + patientId + "?note_update=false&patient_update=" + patientUpdate
}

/**
 * reset all value of form for creation or update note
 */
function resetFieldNoteForm() {
    document.querySelectorAll("#note-creation-form .field-note").forEach(element => {
        element.value = ''
        console.log(element)
    })
    noteContent.removeAttribute('readonly')
    saveNoteBtn.style.display = 'block'
    saveNoteBtn.innerText = 'Save note'
}

function updateNote(urlNoteToUpdate) {
    location.href = urlNoteToUpdate + patientUpdate
}

function deleteNote(urlNoteToDelete) {
    const deleteModal = new bootstrap.Modal('#deleteNoteModal')
    document.querySelector('.modal-footer a').setAttribute("href", urlNoteToDelete + patientUpdate)
    deleteModal.show()
}

/**
 * function for btn delete for note that is on the same row that update button.
 * allow us to stop propagation of function on rowclick() 
 * @param {*} e 
 */
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