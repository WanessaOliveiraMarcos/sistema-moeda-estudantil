# sistema-moeda-estudantil

## Histórias de Usuário

[Link para o documento de historias de usuario](docs/user-histories.md)



## LAB5- Release 3: funcionalidades e padrões de projeto

Na Release 3 o grupo escolheu três funcionalidades, cada uma resolvida com um padrão GoF:

| Funcionalidade | Padrão |
|---|---|
| 2.1 Configurações centralizadas do sistema | Singleton |
| 2.3 Ciclo de vida dos resgates | State |
| 2.6 Histórico e reversão de operações | Command |

### 2.1 Configurações centralizadas (Singleton)

**Problema.** Hoje a quantidade de moedas que o professor recebe por semestre (1.000) é uma constante no código, `SaldoMoedasProfessor.MOEDAS_POR_SEMESTRE`, e não existe prazo de validade para os cupons. Para mudar qualquer regra, é preciso alterar o código. Além disso, se cada módulo guardasse a sua própria cópia dos valores, eles poderiam ficar diferentes entre si.

**Por que Singleton.** Ele garante que exista uma única instância com as configurações e que todos os módulos acessem essa mesma instância. Quando o administrador altera um valor, todos passam a enxergá-lo. Um `@Service` do Spring não bastaria como garantia, porque nada impede de criar outro objeto com `new`. No Singleton, o construtor é privado.

**Como vamos fazer.**

- Criar a classe `ConfiguracaoSistema`, com construtor privado e `getInstance()`, no pacote `config`.
- Guardar os valores no banco, em uma tabela de chave e valor, e carregá-los na inicialização da aplicação.
- Atualizações passam por um `ConfiguracaoService`, que grava no banco e atualiza o Singleton. Só o administrador acessa a tela de configurações.
- O `ProfessorService` passa a ler o valor semestral do Singleton em vez da constante.
- Configurações previstas: moedas por semestre (1.000), validade do cupom em dias (30) e limite de moedas por envio.

O Singleton fica na memória de uma instância da aplicação, o que atende ao nosso caso, que roda uma instância só.

### 2.3 Ciclo de vida dos resgates (State)

**Problema.** O resgate é a entidade `TrocaVantagem`, que hoje não tem estado. O `ResgateService.resgatar` debita as moedas, gera o código e envia os e-mails de uma vez, e a empresa só confere o código por fora do sistema. Não há confirmação, cancelamento nem expiração. Ao incluir esses estados, as regras de transição virariam vários `if/else` nos services.

**Por que State.** Cada estado vira uma classe que sabe quais transições aceita. Uma transição proibida, como voltar de "confirmado" para "solicitado", é recusada dentro do próprio estado, sem condicionais espalhados.

**Estados e transições.**

| De | Para | Quando |
|---|---|---|
| Solicitado | Aguardando utilização | Cupom gerado e e-mails enviados |
| Solicitado | Cancelado | Falha ao emitir o cupom (as moedas voltam) |
| Aguardando utilização | Confirmado | A empresa informa o código |
| Aguardando utilização | Cancelado | Aluno ou administrador cancela (as moedas voltam) |
| Aguardando utilização | Expirado | O prazo de validade vence |

Confirmado, cancelado e expirado são estados finais.

**Como vamos fazer.**

- A `TrocaVantagem` passa a ser o contexto: ganha os campos `status` e `dataExpiracao` e repassa `confirmar()`, `cancelar()` e `expirar()` ao estado atual.
- Criar a interface `EstadoResgate` e uma classe para cada estado.
- O banco guarda só o enum `StatusResgate`, porque o JPA não persiste objetos de comportamento. O objeto de estado é obtido a partir do enum.
- O `ResgateService.resgatar` cria o resgate já no fluxo de estados, mantendo os e-mails que existem hoje.
- Novas ações: confirmação pela empresa (`VantagemController`) e cancelamento pelo aluno.
- Uma tarefa agendada marca como expirados os resgates vencidos. A data de expiração usa a validade definida no Singleton.

### 2.6 Histórico e reversão de operações (Command)

**Problema.** O envio de moedas (`MoedaService.enviar`) e o resgate (`ResgateService.resgatar`) executam direto e gravam em tabelas diferentes, `TransacaoMoedas` e `TrocaVantagem`. Nada liga uma operação a um estorno, e não existe cancelamento. O estorno não pode apagar a transação original: precisa criar uma operação compensatória e manter a ligação com a original.

**Por que Command.** Cada operação vira um objeto que guarda seus dados e sabe se executar e se compensar. Assim, quem pede a operação (o controller) fica separado de quem a executa, e a operação pode ser registrada, consultada, cancelada ou estornada.

**Como vamos fazer.**

- Interface `Operacao`, com `executar()` e `compensar()`.
- Comandos `EnviarMoedasCommand` e `ResgatarVantagemCommand`, que reaproveitam as regras dos services atuais.
- Um `OperacaoInvoker` que executa o comando, grava um `RegistroOperacao` (tipo, solicitante, status, data e referência à operação original) e oferece cancelar e estornar.
- Os controllers montam o comando e o entregam ao invoker em vez de chamar o service direto.
- No estorno, o registro original não é apagado: nasce um novo registro de estorno apontando para ele, e o original passa a "estornada".
- Estorno de envio de moedas: debita o aluno e devolve o valor ao saldo do professor, desde que o aluno ainda tenha o saldo.
- Estorno de resgate: usa o `cancelar()` do State e só vale enquanto o resgate aguarda utilização.
- Só o administrador faz estornos. O solicitante pode cancelar o que ainda não foi efetivado.

### Como as três se ligam

O Singleton fornece o prazo de validade que o State usa para calcular a expiração. O comando de resgate usa o State para cancelar e devolver as moedas. O comando de envio consulta o limite por envio guardado no Singleton.
