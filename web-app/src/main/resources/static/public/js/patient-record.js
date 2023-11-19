const urlParams = new URLSearchParams(window.location.search)
const update = urlParams.get('update')
console.log(update)


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