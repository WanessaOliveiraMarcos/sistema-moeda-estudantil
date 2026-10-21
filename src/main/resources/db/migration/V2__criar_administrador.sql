-- Perfil administrador: vê os dados gerais do sistema (herança JOINED, como os outros perfis)
CREATE TABLE administrador (
    id   BIGINT       NOT NULL,
    nome VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (id) REFERENCES usuario (id)
);
