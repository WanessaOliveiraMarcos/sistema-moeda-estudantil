-- Modelo inicial do Sistema de Moeda Estudantil (OrganizacaoEntidades-lab03)

CREATE TABLE instituicao (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    nome     VARCHAR(255) NOT NULL,
    endereco VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE curso (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(255) NOT NULL,
    semestres      INT,
    instituicao_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_curso_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
);

CREATE TABLE departamento (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(255) NOT NULL,
    instituicao_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_departamento_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id)
);

-- Herança JOINED: aluno, professor e empresa_parceira compartilham o id de usuario
CREATE TABLE usuario (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    senha VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
);

CREATE TABLE aluno (
    id             BIGINT       NOT NULL,
    nome           VARCHAR(255) NOT NULL,
    cpf            VARCHAR(11)  NOT NULL,
    rg             VARCHAR(255) NOT NULL,
    endereco       VARCHAR(255) NOT NULL,
    instituicao_id BIGINT       NOT NULL,
    curso_id       BIGINT       NOT NULL,
    saldo_moedas   INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_aluno_cpf UNIQUE (cpf),
    CONSTRAINT ck_aluno_saldo CHECK (saldo_moedas >= 0),
    CONSTRAINT fk_aluno_usuario FOREIGN KEY (id) REFERENCES usuario (id),
    CONSTRAINT fk_aluno_instituicao FOREIGN KEY (instituicao_id) REFERENCES instituicao (id),
    CONSTRAINT fk_aluno_curso FOREIGN KEY (curso_id) REFERENCES curso (id)
);

CREATE TABLE professor (
    id              BIGINT       NOT NULL,
    nome            VARCHAR(255) NOT NULL,
    cpf             VARCHAR(11)  NOT NULL,
    departamento_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_professor_cpf UNIQUE (cpf),
    CONSTRAINT fk_professor_usuario FOREIGN KEY (id) REFERENCES usuario (id),
    CONSTRAINT fk_professor_departamento FOREIGN KEY (departamento_id) REFERENCES departamento (id)
);

CREATE TABLE empresa_parceira (
    id          BIGINT       NOT NULL,
    nome        VARCHAR(255) NOT NULL,
    responsavel VARCHAR(255) NOT NULL,
    telefone    VARCHAR(255) NOT NULL,
    endereco    VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_empresa_usuario FOREIGN KEY (id) REFERENCES usuario (id)
);

-- Semestre no formato AAAA/S, ex.: 2026/2
CREATE TABLE saldo_moedas_professor (
    id            BIGINT     NOT NULL AUTO_INCREMENT,
    professor_id  BIGINT     NOT NULL,
    semestre      VARCHAR(6) NOT NULL,
    valor_inicial INT        NOT NULL,
    valor_final   INT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_saldo_professor_semestre UNIQUE (professor_id, semestre),
    CONSTRAINT ck_saldo_valor_final CHECK (valor_final >= 0),
    CONSTRAINT fk_saldo_professor FOREIGN KEY (professor_id) REFERENCES professor (id)
);

CREATE TABLE transacao_moedas (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    professor_id BIGINT       NOT NULL,
    aluno_id     BIGINT       NOT NULL,
    valor        INT          NOT NULL,
    motivo       VARCHAR(500) NOT NULL,
    data_hora    DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_transacao_valor CHECK (valor > 0),
    CONSTRAINT fk_transacao_professor FOREIGN KEY (professor_id) REFERENCES professor (id),
    CONSTRAINT fk_transacao_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id)
);

-- foto: caminho relativo ao storage, ex.: vantagens/3f2a...png
CREATE TABLE vantagem (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    nome       VARCHAR(255)  NOT NULL,
    descricao  VARCHAR(1000) NOT NULL,
    foto       VARCHAR(255)  NOT NULL,
    valor      INT           NOT NULL,
    empresa_id BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_vantagem_valor CHECK (valor > 0),
    CONSTRAINT fk_vantagem_empresa FOREIGN KEY (empresa_id) REFERENCES empresa_parceira (id)
);

CREATE TABLE troca_vantagem (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    aluno_id    BIGINT       NOT NULL,
    vantagem_id BIGINT       NOT NULL,
    valor       INT          NOT NULL,
    codigo      VARCHAR(255) NOT NULL,
    data_hora   DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_troca_codigo UNIQUE (codigo),
    CONSTRAINT fk_troca_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id),
    CONSTRAINT fk_troca_vantagem FOREIGN KEY (vantagem_id) REFERENCES vantagem (id)
);
