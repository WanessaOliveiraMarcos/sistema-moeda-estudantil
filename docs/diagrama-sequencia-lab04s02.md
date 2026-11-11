# Diagramas de Sequência: Lab04S02

Casos de uso desta sprint: cadastro de vantagem (empresa parceira) e listagem de vantagens (aluno).
Arquitetura MVC com Spring Boot: SecurityFilterChain, Controller, Service, Repository e banco de dados.

## 1. Cadastro de vantagem (empresa parceira)

```mermaid
sequenceDiagram
    actor Empresa as EmpresaParceira
    participant Sec as SecurityFilterChain
    participant VC as VantagemController
    participant VS as VantagemService
    participant EPS as EmpresaParceiraService
    participant ST as StorageService
    participant VR as VantagemRepository
    participant DB as Banco de dados

    Empresa->>Sec: POST /minhas-vantagens (nome, descricao, valor, foto)
    Sec->>Sec: exige perfil EMPRESA
    Sec->>VC: salvar(empresa, form, result, model, redirect)
    alt form invalido (@Valid no VantagemForm)
        VC-->>Empresa: tela vantagens/form com erros
    else form valido
        VC->>VS: salvar(empresaId, form)
        alt cadastro sem foto
            VS-->>VC: RegraNegocioException (arquivo)
            VC-->>Empresa: tela com erro
        else com foto
            VS->>EPS: buscar(empresaId)
            EPS-->>VS: empresa
            VS->>ST: salvarImagem(arquivo, vantagens)
            alt arquivo nao e JPG, PNG ou GIF
                ST-->>VS: IllegalArgumentException
                VS-->>VC: RegraNegocioException (arquivo)
                VC-->>Empresa: tela com erro
            else imagem valida
                ST-->>VS: caminho da foto
                VS->>ST: excluirSeRollback(foto)
                VS->>VR: save(vantagem)
                VR->>DB: INSERT vantagem
                DB-->>VR: vantagem persistida
                VR-->>VS: vantagem
                VS-->>VC: vantagem
                VC-->>Empresa: redirect /minhas-vantagens (Vantagem salva com sucesso)
            end
        end
    end
```

## 2. Listagem de vantagens (aluno)

```mermaid
sequenceDiagram
    actor Aluno
    participant Sec as SecurityFilterChain
    participant CC as CatalogoController
    participant VS as VantagemService
    participant VR as VantagemRepository
    participant EPS as EmpresaParceiraService
    participant DB as Banco de dados

    Aluno->>Sec: GET /vantagens (busca, empresaId, ordem)
    Sec->>Sec: exige perfil ALUNO
    Sec->>CC: catalogo(busca, empresaId, ordem, model)
    CC->>VS: catalogo(busca, empresaId, ordem)
    VS->>VS: monta o Sort (nome, menor ou maior custo)
    VS->>VR: buscarCatalogo(termo, empresaId, sort)
    VR->>DB: SELECT vantagem (filtros nulos ignorados)
    DB-->>VR: vantagens
    VR-->>VS: List de Vantagem
    VS-->>CC: List de Vantagem
    CC->>EPS: listar()
    EPS-->>CC: empresas (para o filtro)
    CC-->>Aluno: tela vantagens/catalogo
```
