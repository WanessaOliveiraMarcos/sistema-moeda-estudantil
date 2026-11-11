# Diagramas de Sequência: Lab04S03

Caso de uso desta sprint: troca de vantagem pelo aluno, mais o diagrama de sequência geral.
Arquitetura MVC com Spring Boot: SecurityFilterChain, Controller, Service, Repository e banco de dados.

## 1. Diagrama de sequência geral

```mermaid
sequenceDiagram
    actor Professor
    actor Aluno
    actor Empresa as EmpresaParceira
    participant Sec as Spring Security
    participant MC as MoedaController
    participant CC as CatalogoController
    participant VC as VantagemController
    participant MS as MoedaService
    participant VS as VantagemService
    participant RS as ResgateService
    participant ES as EmailService
    participant DB as Banco de dados

    Note over Professor,DB: 1. Autenticacao (todos os perfis)
    Professor->>Sec: POST /login (email, senha)
    Sec->>DB: UsuarioDetailsService.loadUserByUsername(email)
    DB-->>Sec: usuario (perfil)
    Sec-->>Professor: sessao autenticada

    Note over Professor,DB: 2. Professor envia moedas
    Professor->>MC: POST /moedas/enviar
    MC->>MS: enviar(professorId, form)
    MS->>DB: valida turma e saldo, atualiza saldos, grava TransacaoMoedas
    MS-->>MC: transacao
    MS-)ES: Email de moedas recebidas (apos commit)
    ES-->>Aluno: e-mail

    Note over Professor,DB: 3. Empresa cadastra vantagem
    Empresa->>VC: POST /minhas-vantagens
    VC->>VS: salvar(empresaId, form)
    VS->>DB: grava foto no storage e Vantagem

    Note over Professor,DB: 4. Aluno consulta o catalogo e troca moedas
    Aluno->>CC: GET /vantagens
    CC->>VS: catalogo(busca, empresaId, ordem)
    VS-->>CC: vantagens
    Aluno->>CC: POST /vantagens/:id/resgatar
    CC->>RS: resgatar(alunoId, vantagemId)
    RS->>DB: valida saldo, desconta, gera codigo, grava TrocaVantagem
    RS-)ES: Email cupom (aluno) e Email conferencia (empresa)
    ES-->>Aluno: e-mail com cupom e codigo
    ES-->>Empresa: e-mail com codigo para conferencia

    Note over Professor,DB: 5. Empresa confere o resgate
    Empresa->>VC: GET /minhas-vantagens/resgates
    VC->>VS: listarResgates(empresaId)
    VS-->>VC: trocas com aluno e codigo
```

## 2. Troca de vantagem (aluno)

```mermaid
sequenceDiagram
    actor Aluno
    participant Sec as SecurityFilterChain
    participant CC as CatalogoController
    participant RS as ResgateService
    participant AR as AlunoRepository
    participant VR as VantagemRepository
    participant TVR as TrocaVantagemRepository
    participant EV as ApplicationEventPublisher
    participant ES as EmailService
    participant MAIL as JavaMailSender
    actor Empresa as EmpresaParceira

    Aluno->>Sec: POST /vantagens/:id/resgatar
    Sec->>Sec: exige perfil ALUNO
    Sec->>CC: resgatar(id, aluno, redirect)
    CC->>RS: resgatar(alunoId, vantagemId)
    RS->>AR: buscarParaAtualizar(alunoId)
    AR-->>RS: aluno
    RS->>VR: findById(vantagemId)
    VR-->>RS: vantagem
    alt saldo do aluno menor que o valor da vantagem
        RS-->>CC: RegraNegocioException (saldo insuficiente)
        CC-->>Aluno: redirect /vantagens com mensagem de erro
    else saldo suficiente
        RS->>RS: desconta o valor do saldo do aluno
        loop ate gerar codigo inedito
            RS->>RS: novoCodigo() formato MOE-XXXX-XXXX
            RS->>TVR: existsByCodigo(codigo)
        end
        RS->>TVR: save(troca com aluno, vantagem, valor, codigo)
        RS->>EV: publishEvent(Email cupom para o aluno)
        RS->>EV: publishEvent(Email conferencia para a empresa)
        RS-->>CC: troca
        CC-->>Aluno: redirect /extrato (mensagem com o codigo do cupom)
        Note over EV,ES: apos o commit da transacao, em outra thread
        EV->>ES: enviar(email do aluno)
        ES->>MAIL: send(mensagem)
        MAIL-->>Aluno: e-mail com cupom e codigo
        EV->>ES: enviar(email da empresa)
        ES->>MAIL: send(mensagem)
        MAIL-->>Empresa: e-mail com o mesmo codigo para conferencia
    end
```
