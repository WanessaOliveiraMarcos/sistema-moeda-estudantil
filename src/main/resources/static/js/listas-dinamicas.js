// Linhas que podem ser adicionadas e removidas num formulário (cursos e professores da instituição).
// Cada [data-lista] tem: .itens com as linhas, um <template> da linha nova com o índice __I__
// e um botão [data-adicionar]. Após remover, os índices são renumerados para o Spring
// receber lista[0], lista[1], ... sem buracos.
document.addEventListener('DOMContentLoaded', () => {
    for (const lista of document.querySelectorAll('[data-lista]')) {
        const itens = lista.querySelector('.itens');
        const modelo = lista.querySelector('template');

        const renumerar = () => {
            [...itens.children].forEach((item, indice) => {
                for (const campo of item.querySelectorAll('[name]')) {
                    campo.name = campo.name.replace(/\[\d+\]/, `[${indice}]`);
                }
            });
        };

        lista.querySelector('[data-adicionar]').addEventListener('click', () => {
            const html = modelo.innerHTML.replaceAll('__I__', itens.children.length);
            itens.insertAdjacentHTML('beforeend', html);
            itens.lastElementChild.querySelector('input')?.focus();
        });

        itens.addEventListener('click', evento => {
            if (evento.target.matches('[data-remover]')) {
                evento.target.closest('.item').remove();
                renumerar();
            }
        });
    }
});
