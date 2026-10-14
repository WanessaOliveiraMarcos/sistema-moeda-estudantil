# sistema-moeda-estudantil

## Historias de usuário

## Épico 1 — Autenticação e acesso

### US01 — Login no sistema
**Como** aluno, professor ou empresa parceira, **quero** acessar o sistema com login e senha, **para** utilizar apenas as funcionalidades do meu perfil.
**Prioridade:** Alta
**Critérios de aceitação:**
- O acesso a qualquer funcionalidade exige autenticação.
- Credenciais inválidas exibem mensagem de erro sem informar qual campo está incorreto.
- Após o login, o usuário é direcionado à área do seu perfil (aluno, professor ou empresa).
- É possível encerrar a sessão (logout).

### US02 — Controle de acesso por perfil
**Como** usuário do sistema, **quero** que cada perfil acesse somente suas funcionalidades, **para** garantir a segurança das informações.
**Prioridade:** Alta
**Critérios de aceitação:**
- Aluno não acessa funções de envio de moedas nem de cadastro de vantagens.
- Professor não acessa resgate de vantagens.
- Empresa parceira não acessa extratos de alunos ou professores.

---

## Épico 2 — Gestão de alunos

### US03 — Cadastro de aluno
**Como** aluno, **quero** me cadastrar informando nome, e-mail, CPF, RG, endereço, instituição de ensino e curso, **para** participar do sistema de mérito.
**Prioridade:** Alta
**Critérios de aceitação:**
- Todos os campos são obrigatórios.
- A instituição é escolhida a partir de uma lista de instituições pré-cadastradas.
- CPF e e-mail não podem estar duplicados.
- O aluno define login e senha no cadastro.
- O saldo inicial de moedas é zero.

### US04 — Consultar e atualizar dados do aluno
**Como** aluno, **quero** visualizar e editar meus dados cadastrais, **para** mantê-los atualizados.
**Prioridade:** Média
**Critérios de aceitação:**
- O aluno visualiza todos os dados do próprio cadastro.
- As alterações são validadas com as mesmas regras do cadastro.

### US05 — Excluir cadastro de aluno
**Como** aluno, **quero** excluir meu cadastro, **para** deixar de participar do sistema.
**Prioridade:** Média
**Critérios de aceitação:**
- O sistema solicita confirmação antes da exclusão.
- Após a exclusão, o aluno não consegue mais fazer login.

---

## Épico 3 — Gestão de empresas parceiras

### US06 — Cadastro de empresa parceira
**Como** empresa parceira, **quero** me cadastrar no sistema, **para** oferecer vantagens aos alunos.
**Prioridade:** Alta
**Critérios de aceitação:**
- A empresa informa nome, CNPJ, e-mail, login e senha.
- CNPJ e e-mail não podem estar duplicados.

### US07 — Consultar, atualizar e excluir empresa parceira
**Como** empresa parceira, **quero** visualizar, editar e excluir meus dados, **para** manter minha conta correta e atual.
**Prioridade:** Média
**Critérios de aceitação:**
- A empresa edita seus dados com validação.
- A exclusão exige confirmação.
- Ao excluir a empresa, suas vantagens deixam de ser exibidas aos alunos.

### US08 — Cadastrar vantagem
**Como** empresa parceira, **quero** cadastrar uma vantagem com descrição, foto e custo em moedas, **para** que os alunos possam trocá-la.
**Prioridade:** Alta
**Critérios de aceitação:**
- Descrição, foto do produto e custo (em moedas, maior que zero) são obrigatórios.
- A vantagem fica associada à empresa que a cadastrou.
- A vantagem passa a aparecer na listagem disponível para os alunos.

### US09 — Gerenciar vantagens
**Como** empresa parceira, **quero** listar, editar e remover minhas vantagens, **para** manter a oferta atualizada.
**Prioridade:** Média
**Critérios de aceitação:**
- A empresa visualiza apenas as próprias vantagens.
- Alterações de custo não afetam resgates já realizados.

---

## Épico 4 — Distribuição de moedas (professor)

### US10 — Receber moedas semestralmente
**Como** professor, **quero** receber 1.000 moedas a cada semestre, acumulando o saldo não utilizado, **para** reconhecer meus alunos ao longo do tempo.
**Prioridade:** Alta
**Critérios de aceitação:**
- A cada novo semestre, 1.000 moedas são adicionadas ao saldo corrente do professor.
- Moedas não distribuídas não expiram e são somadas ao crédito do semestre seguinte.
- O crédito semestral é registrado como transação no extrato.

### US11 — Enviar moedas a um aluno
**Como** professor, **quero** enviar moedas a um aluno informando o motivo do reconhecimento, **para** valorizar bom comportamento e participação em aula.
**Prioridade:** Alta
**Critérios de aceitação:**
- O professor seleciona o aluno, informa a quantidade e uma mensagem obrigatória.
- O envio só é permitido se o saldo do professor for suficiente.
- Ao concluir, o saldo do professor é debitado e o do aluno é creditado.
- A transação é registrada no extrato de ambos.
- O professor só envia moedas a alunos da própria instituição.

### US12 — Notificar aluno por e-mail
**Como** aluno, **quero** ser notificado por e-mail ao receber moedas, **para** saber quando fui reconhecido.
**Prioridade:** Alta
**Critérios de aceitação:**
- O e-mail é enviado automaticamente após o envio da moeda.
- O e-mail contém o nome do professor, a quantidade de moedas e a mensagem de reconhecimento.

### US13 — Pré-cadastro de professores
**Como** instituição parceira, **quero** que a lista de professores seja carregada no sistema, **para** que eles possam distribuir moedas sem precisar se cadastrar.
**Prioridade:** Média
**Critérios de aceitação:**
- Cada professor possui nome, CPF e departamento.
- Cada professor está explicitamente vinculado a uma instituição.
- O professor acessa o sistema com login e senha previamente definidos.

---

## Épico 5 — Extrato e saldo

### US14 — Consultar extrato do professor
**Como** professor, **quero** consultar meu extrato, **para** acompanhar meu saldo e as moedas que enviei.
**Prioridade:** Alta
**Critérios de aceitação:**
- Exibe o saldo atual de moedas.
- Lista os envios com data, aluno, quantidade e motivo, além dos créditos semestrais.

### US15 — Consultar extrato do aluno
**Como** aluno, **quero** consultar meu extrato, **para** acompanhar meu saldo e minhas movimentações.
**Prioridade:** Alta
**Critérios de aceitação:**
- Exibe o saldo atual de moedas.
- Lista recebimentos (data, professor, quantidade, mensagem) e trocas (data, vantagem, custo).

---

## Épico 6 — Resgate de vantagens (aluno)

### US16 — Visualizar vantagens disponíveis
**Como** aluno, **quero** ver as vantagens cadastradas com descrição, foto, custo e empresa, **para** escolher a que desejo trocar.
**Prioridade:** Alta
**Critérios de aceitação:**
- A listagem exibe foto, descrição, custo em moedas e empresa parceira.
- O aluno identifica quais vantagens consegue pagar com seu saldo atual.

### US17 — Resgatar vantagem
**Como** aluno, **quero** trocar minhas moedas por uma vantagem, **para** aproveitar descontos e produtos das empresas parceiras.
**Prioridade:** Alta
**Critérios de aceitação:**
- O resgate só é permitido com saldo suficiente.
- O custo da vantagem é debitado do saldo do aluno.
- O sistema gera um código único para a troca.
- A transação é registrada no extrato do aluno.

### US18 — Receber cupom por e-mail
**Como** aluno, **quero** receber um e-mail com o cupom e o código da troca, **para** utilizá-lo presencialmente.
**Prioridade:** Alta
**Critérios de aceitação:**
- O e-mail é enviado automaticamente após o resgate.
- Contém a vantagem resgatada, a empresa e o código gerado pelo sistema.

### US19 — Notificar empresa parceira sobre o resgate
**Como** empresa parceira, **quero** receber um e-mail quando um aluno resgatar uma de minhas vantagens, **para** conferir a troca no atendimento presencial.
**Prioridade:** Alta
**Critérios de aceitação:**
- O e-mail é enviado automaticamente após o resgate.
- Contém o nome do aluno, a vantagem e o mesmo código enviado ao aluno, permitindo a conferência.
