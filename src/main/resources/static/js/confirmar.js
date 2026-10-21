// Formulários com data-confirmar pedem confirmação antes de enviar (ex.: resgate de vantagem).
document.addEventListener('submit', evento => {
    const mensagem = evento.target.dataset.confirmar;
    if (mensagem && !confirm(mensagem)) {
        evento.preventDefault();
    }
});
