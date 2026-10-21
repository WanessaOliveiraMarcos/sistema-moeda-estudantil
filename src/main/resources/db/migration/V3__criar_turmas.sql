-- Turmas: relação entre professor e aluno. Um professor tem várias turmas e um aluno
-- se matricula em várias (limite de 40 alunos por turma, conferido na aplicação).
CREATE TABLE turma (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(255) NOT NULL,
    semestre     VARCHAR(6)   NOT NULL,
    professor_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_turma_professor FOREIGN KEY (professor_id) REFERENCES professor (id)
);

CREATE TABLE matricula (
    turma_id BIGINT NOT NULL,
    aluno_id BIGINT NOT NULL,
    PRIMARY KEY (turma_id, aluno_id),
    CONSTRAINT fk_matricula_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id)
);

-- Critérios de troca da turma: regras para os alunos enviarem moedas entre si
CREATE TABLE criterio_troca (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    turma_id      BIGINT       NOT NULL,
    descricao     VARCHAR(255) NOT NULL,
    valor_maximo  INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT ck_criterio_valor CHECK (valor_maximo > 0),
    CONSTRAINT fk_criterio_turma FOREIGN KEY (turma_id) REFERENCES turma (id)
);

-- Envio de moedas entre alunos da mesma turma; só acontece depois que o professor autoriza
CREATE TABLE transferencia_aluno (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    turma_id        BIGINT       NOT NULL,
    remetente_id    BIGINT       NOT NULL,
    destinatario_id BIGINT       NOT NULL,
    criterio_id     BIGINT       NOT NULL,
    valor           INT          NOT NULL,
    mensagem        VARCHAR(500),
    status          VARCHAR(20)  NOT NULL,
    data_hora       DATETIME(6)  NOT NULL,
    decidida_em     DATETIME(6),
    motivo_recusa   VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT ck_transferencia_valor CHECK (valor > 0),
    CONSTRAINT ck_transferencia_status CHECK (status IN ('PENDENTE', 'APROVADA', 'RECUSADA')),
    CONSTRAINT fk_transferencia_turma FOREIGN KEY (turma_id) REFERENCES turma (id),
    CONSTRAINT fk_transferencia_remetente FOREIGN KEY (remetente_id) REFERENCES aluno (id),
    CONSTRAINT fk_transferencia_destinatario FOREIGN KEY (destinatario_id) REFERENCES aluno (id),
    CONSTRAINT fk_transferencia_criterio FOREIGN KEY (criterio_id) REFERENCES criterio_troca (id)
);
