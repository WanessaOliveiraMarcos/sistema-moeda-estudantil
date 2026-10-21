// Mostra no select de curso só os cursos da instituição escolhida.
// O servidor também valida (AlunoService), então funciona mesmo sem JavaScript.
document.addEventListener('DOMContentLoaded', () => {
    const instituicao = document.getElementById('instituicaoId');
    const curso = document.getElementById('cursoId');
    if (!instituicao || !curso) {
        return;
    }

    const filtrarCursos = () => {
        const instituicaoId = instituicao.value;
        let cursoSelecionadoValido = false;
        for (const opcao of curso.options) {
            if (!opcao.value) {
                continue;
            }
            const pertence = opcao.dataset.instituicao === instituicaoId;
            opcao.hidden = !pertence;
            opcao.disabled = !pertence;
            if (pertence && opcao.selected) {
                cursoSelecionadoValido = true;
            }
        }
        if (!cursoSelecionadoValido) {
            curso.value = '';
        }
        curso.options[0].textContent = instituicaoId ? 'Selecione...' : 'Escolha a instituição primeiro';
    };

    instituicao.addEventListener('change', filtrarCursos);
    filtrarCursos();
});
