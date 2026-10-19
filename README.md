# AutoBots — Automanager (ATVII)

Micro-serviço em **Java + Spring Boot** para o cadastro de clientes da AutoBots. A ATVI entregou o CRUD completo das entidades **Cliente**, **Documento**, **Endereço** e **Telefone**. Na ATVII, a API passou a atender os **quatro níveis do Modelo de Maturidade de Richardson (RMM)**, incluindo o nível 3, **HATEOAS**: cada resposta traz links para navegar pelos recursos.

Atividade prática ATVII — Prof. Dr. Eng. Gerson Penha.

---

## Sumário

1. [Requisitos](#1-requisitos)
2. [Como executar](#2-como-executar)
3. [Como saber se a aplicação está rodando](#3-como-saber-se-a-aplicação-está-rodando)
4. [Níveis de maturidade (RMM)](#4-níveis-de-maturidade-rmm)
5. [HATEOAS: como funcionam os links](#5-hateoas-como-funcionam-os-links)
6. [Dados de exemplo](#6-dados-de-exemplo)
7. [Endpoints](#7-endpoints)
8. [Roteiro de teste rápido](#8-roteiro-de-teste-rápido)
9. [Banco de dados (H2 Console)](#9-banco-de-dados-h2-console)
10. [Alterações feitas no código](#10-alterações-feitas-no-código)
11. [Problemas comuns](#11-problemas-comuns)

---

## 1. Requisitos

| Item | Versão | Observação |
|---|---|---|
| **JDK (Java)** | **17 ou superior** | Testado com o **JDK 21**. Só o JRE não basta: é preciso o **JDK**. |
| Maven | *não precisa instalar* | O projeto traz o **Maven Wrapper** (`mvnw`), que baixa o Maven sozinho na primeira execução. |
| Internet | na 1ª execução | Para baixar as dependências. Depois funciona offline. |
| Porta **8080** | livre | A aplicação sobe em `http://localhost:8080`. |
| Ferramenta HTTP | qualquer uma | Postman, Insomnia, Thunder Client (VS Code) ou `curl`. |

Tecnologias do projeto: Spring Boot 2.6.3 · Spring Data JPA · **Spring HATEOAS** · Banco H2 (em memória) · Lombok 1.18.34.

**Para verificar se o Java está instalado**, abra um terminal e digite:

```
java -version
```

A resposta deve mostrar a versão **17 ou maior** (ex.: `java version "21.0.x"`). Se o comando não for reconhecido, instale o JDK em <https://adoptium.net> ou <https://www.oracle.com/java/technologies/downloads/>.

---

## 2. Como executar

Escolha **uma** das três opções. Todas partem da pasta do projeto clonado.

### Opção A — Pelo terminal (mais simples)

Entre na pasta `automanager`, onde fica o arquivo `pom.xml`:

```
cd automanager
```

Execute:

| Sistema | Comando |
|---|---|
| Windows (PowerShell) | `.\mvnw.cmd spring-boot:run` |
| Windows (Prompt de Comando / cmd) | `mvnw.cmd spring-boot:run` |
| Linux / macOS | `./mvnw spring-boot:run` |

A primeira execução demora alguns minutos, porque baixa o Maven e as dependências. As próximas são rápidas.

Para **parar** a aplicação, pressione **Ctrl + C** no terminal.

### Opção B — Pelo Eclipse

> ⚠️ **Importante:** o Eclipse precisa do **plugin do Lombok** instalado. Sem ele, o projeto mostra dezenas de erros como `The method getNome() is undefined`, mesmo com o código correto. Faça o passo 1 antes de importar.

1. **Instalar o Lombok no Eclipse** (apenas uma vez):
   1. Baixe o `lombok.jar` em <https://projectlombok.org/download>.
   2. Execute o arquivo com um duplo clique, ou pelo terminal: `java -jar lombok.jar`.
   3. Na janela que abrir, selecione a instalação do Eclipse (clique em *Specify location* se ela não aparecer) e depois em **Install / Update**.
   4. **Reinicie o Eclipse.**
2. **Importar o projeto:** `File` → `Import...` → `Maven` → `Existing Maven Projects` → em *Root Directory* selecione a pasta **`automanager`** → `Finish`.
3. Aguarde o Eclipse baixar as dependências (barra de progresso no canto inferior direito).
4. **Executar:** abra `src/main/java/com/autobots/automanager/AutomanagerApplication.java`, clique com o botão direito → `Run As` → `Java Application`.

Se ainda houver erros depois de instalar o Lombok: clique com o botão direito no projeto → `Maven` → `Update Project...` (Alt+F5), e depois `Project` → `Clean...`.

Se o Eclipse reclamar da versão do Java: `Window` → `Preferences` → `Java` → `Installed JREs` e selecione um JDK 17 ou superior.

### Opção C — Pelo VS Code

1. Instale a extensão **Extension Pack for Java** (da Microsoft). Ela já inclui o suporte ao Lombok.
2. Abra a pasta `automanager` no VS Code.
3. Abra `AutomanagerApplication.java` e clique em **Run**, acima do método `main`.

---

## 3. Como saber se a aplicação está rodando

No final do log devem aparecer as linhas:

```
Tomcat started on port(s): 8080 (http)
Started AutomanagerApplication in X seconds
```

Para conferir, abra no navegador: <http://localhost:8080/cliente/1>. Deve aparecer o JSON do cliente com um bloco `_links` no final.

---

## 4. Níveis de maturidade (RMM)

O Modelo de Maturidade de Richardson classifica uma API REST em quatro níveis. Cada nível depende do anterior.

| Nível | Exigência | Como o automanager atende |
|---|---|---|
| **0** | Uso do protocolo HTTP | Toda a comunicação é feita por HTTP, com corpo em JSON. |
| **1** | URI única por recurso | Cada recurso tem o seu endereço: `/cliente/{id}`, `/telefone/{id}`, `/documento/{id}`, `/endereco/{id}`, além das coleções (`/cliente/clientes`, `/telefone/telefones` etc.). |
| **2** | Uso dos métodos HTTP e suas semânticas | `GET` consulta, `POST` cadastra, `PUT` atualiza e `DELETE` exclui. As respostas usam o status correto: `200`, `201`, `204`, `400`, `404` e `409` (veja a [seção 7](#7-endpoints)). O `POST` sempre cria um registro novo e ignora qualquer `id` enviado no corpo. |
| **3** | HATEOAS | Cada resposta traz links (`_links`) para o próprio recurso e para a coleção dele. O cliente traz também links para os seus telefones, documentos e endereço (veja a [seção 5](#5-hateoas-como-funcionam-os-links)). |

---

## 5. HATEOAS: como funcionam os links

HATEOAS (*Hypermedia As The Engine Of Application State*) significa que **a própria resposta indica para onde ir em seguida**. Quem consome a API não precisa montar URLs na mão: basta seguir os links.

Cada link tem um **nome** (*rel*) e um **endereço** (`href`):

| Nome | Aponta para |
|---|---|
| `self` | O próprio recurso (ex.: `/telefone/1`) |
| `clientes`, `telefones`, `documentos`, `enderecos` | A coleção completa daquela entidade |

### Regras

| Resposta | Links |
|---|---|
| Um recurso (`GET /{id}`, `POST`, `PUT`) | `self` + link da coleção |
| Uma lista (`GET` da coleção) | `self` em cada item |
| Um cliente (avulso ou na lista) | Os telefones, documentos e o endereço dentro dele também recebem o `self` |
| `DELETE` | Sem links: a resposta é `204` sem corpo |

### Exemplo: `GET /telefone/1`

```json
{
  "id": 1,
  "ddd": "21",
  "numero": "981234576",
  "_links": {
    "self":      { "href": "http://localhost:8080/telefone/1" },
    "telefones": { "href": "http://localhost:8080/telefone/telefones" }
  }
}
```

### Exemplo: `GET /cliente/2` (com itens relacionados)

```json
{
  "id": 2,
  "nome": "Maria da Silva",
  "nomeSocial": "Maria",
  "telefones": [
    {
      "id": 2, "ddd": "12", "numero": "991112222",
      "_links": { "self": { "href": "http://localhost:8080/telefone/2" } }
    }
  ],
  "documentos": [],
  "endereco": null,
  "_links": {
    "self":     { "href": "http://localhost:8080/cliente/2" },
    "clientes": { "href": "http://localhost:8080/cliente/clientes" }
  }
}
```

### Formato dos links nas listas

Nas **listas** (`/cliente/clientes`, `/telefone/telefones` etc.), os links aparecem em outro formato:

```json
"links": [ { "rel": "self", "href": "http://localhost:8080/telefone/1" } ]
```

O motivo: o Spring HATEOAS só usa o formato HAL (`_links`) quando o objeto devolvido é um `RepresentationModel`. Um item sozinho é, mas uma `List<...>` é uma lista comum do Java. O conteúdo dos links é o mesmo. Esse comportamento segue a estrutura do código-base disponibilizado para a atividade, que também devolve `List<...>`.

### Como foi implementado

| Peça | Papel |
|---|---|
| `spring-boot-starter-hateoas` (`pom.xml`) | Biblioteca do Spring para criar links |
| `extends RepresentationModel<...>` (entidades) | Dá a cada entidade o método `add(Link)` e o bloco de links no JSON |
| `AdicionadorLink<T>` (interface) | Contrato com `adicionarLink(T)` e `adicionarLink(List<T>)` |
| `AdicionadorLinkCliente`, `...Telefone`, `...Documento`, `...Endereco` | Montam os links de cada entidade com `WebMvcLinkBuilder.linkTo(methodOn(Controle.class).metodo(id))` |
| Controles | Chamam o adicionador antes de devolver a resposta |

O `methodOn(...)` **não executa** o método do controle. Ele só registra qual método foi chamado, e o `linkTo` lê a rota do `@GetMapping` para montar a URL. Assim, se uma rota mudar, os links acompanham automaticamente.

---

## 6. Dados de exemplo

O banco é **em memória**: começa vazio e é **apagado ao parar a aplicação**. Para facilitar os testes, **a cada inicialização** são cadastrados automaticamente:

| Cliente | id | Telefones | Documentos | Endereço |
|---|---|---|---|---|
| Pedro Alcântara de Bragança e Bourbon ("Dom Pedro") | **1** | id 1 — (21) 981234576 | id 1 — RG · id 2 — CPF | id 1 — Av. Atlântica, 1702, Rio de Janeiro |
| Maria da Silva ("Maria") | **2** | id 2 — (12) 991112222 | — | *sem endereço* |

Como os dados voltam ao estado inicial sempre que a aplicação reinicia, os ids da tabela acima podem ser usados nos testes.

---

## 7. Endpoints

URL base: `http://localhost:8080`

Em requisições **POST** e **PUT**, envie o corpo em **JSON** (cabeçalho `Content-Type: application/json`). Respostas com um único recurso usam o tipo `application/hal+json`.

### Códigos de resposta

| Código | Significado |
|---|---|
| `200 OK` | Consulta ou atualização realizada |
| `201 Created` | Cadastro realizado (o corpo traz o registro criado, com o id e os links) |
| `204 No Content` | Exclusão realizada |
| `400 Bad Request` | Dados obrigatórios faltando |
| `404 Not Found` | Id (do registro ou do cliente) não existe |
| `409 Conflict` | Cliente já possui endereço |

### 7.1 Cliente — `/cliente`

| Método | Rota | Descrição | Links na resposta |
|---|---|---|---|
| GET | `/cliente/clientes` | Lista todos os clientes | `self` em cada cliente e nos seus itens |
| GET | `/cliente/{id}` | Busca um cliente | `self`, `clientes` e `self` nos itens |
| POST | `/cliente/cadastro` | Cadastra um cliente | `self`, `clientes` e `self` nos itens |
| PUT | `/cliente/atualizar/{id}` | Atualiza um cliente | `self`, `clientes` e `self` nos itens |
| DELETE | `/cliente/excluir/{id}` | Exclui o cliente **e todos os seus telefones, documentos e endereço** | — |

Exemplo de **cadastro** (`POST /cliente/cadastro`). Telefones, documentos e endereço são opcionais e podem ser enviados junto:

```json
{
  "nome": "João da Silva",
  "nomeSocial": "João",
  "dataNascimento": "1990-03-20",
  "telefones": [ { "ddd": "11", "numero": "912345678" } ],
  "documentos": [ { "tipo": "CPF", "numero": "98765432100" } ],
  "endereco": { "cidade": "São Paulo", "rua": "Rua das Flores", "numero": "100" }
}
```

> No cadastro, qualquer `id` enviado no corpo (do cliente ou dos itens) é **ignorado**: o cadastro sempre cria registros novos.

Exemplo de **atualização** (`PUT /cliente/atualizar/1`): envie **apenas os campos que quer alterar**. Campos ausentes ou em branco são mantidos.

```json
{
  "nomeSocial": "Imperador",
  "endereco": { "codigoPostal": "22021002" },
  "telefones": [ { "id": 1, "numero": "999999999" } ]
}
```

> Na atualização do cliente, telefones e documentos só são **alterados** se forem informados com o `id`. Para **adicionar ou remover** telefones, documentos e endereço, use as rotas próprias de cada entidade (abaixo).

### 7.2 Telefone — `/telefone`

| Método | Rota | Descrição | Links na resposta |
|---|---|---|---|
| GET | `/telefone/telefones` | Lista todos os telefones | `self` em cada item |
| GET | `/telefone/{id}` | Busca um telefone | `self`, `telefones` |
| POST | `/telefone/cadastro/{clienteId}` | Adiciona um telefone ao cliente informado | `self`, `telefones` |
| PUT | `/telefone/atualizar/{id}` | Atualiza um telefone | `self`, `telefones` |
| DELETE | `/telefone/excluir/{id}` | Exclui um telefone | — |

```json
{ "ddd": "11", "numero": "912345678" }
```

### 7.3 Documento — `/documento`

| Método | Rota | Descrição | Links na resposta |
|---|---|---|---|
| GET | `/documento/documentos` | Lista todos os documentos | `self` em cada item |
| GET | `/documento/{id}` | Busca um documento | `self`, `documentos` |
| POST | `/documento/cadastro/{clienteId}` | Adiciona um documento ao cliente informado | `self`, `documentos` |
| PUT | `/documento/atualizar/{id}` | Atualiza um documento | `self`, `documentos` |
| DELETE | `/documento/excluir/{id}` | Exclui um documento | — |

```json
{ "tipo": "CNH", "numero": "12345678900" }
```

> O **número do documento é único** no sistema: não é possível cadastrar dois documentos com o mesmo número.

### 7.4 Endereço — `/endereco`

| Método | Rota | Descrição | Links na resposta |
|---|---|---|---|
| GET | `/endereco/enderecos` | Lista todos os endereços | `self` em cada item |
| GET | `/endereco/{id}` | Busca um endereço | `self`, `enderecos` |
| POST | `/endereco/cadastro/{clienteId}` | Cadastra o endereço do cliente (**cada cliente tem no máximo um**; se já tiver, retorna `409`) | `self`, `enderecos` |
| PUT | `/endereco/atualizar/{id}` | Atualiza um endereço | `self`, `enderecos` |
| DELETE | `/endereco/excluir/{id}` | Exclui um endereço | — |

No cadastro, os campos **`cidade`, `rua` e `numero` são obrigatórios** (sem eles retorna `400`):

```json
{
  "estado": "SP",
  "cidade": "Taubaté",
  "bairro": "Centro",
  "rua": "Rua B",
  "numero": "20",
  "codigoPostal": "12000000",
  "informacoesAdicionais": "Apto 12"
}
```

---

## 8. Roteiro de teste rápido

Sequência sugerida, com a aplicação **recém-iniciada** (dados de exemplo da seção 6):

| # | Requisição | Corpo (JSON) | Resultado esperado |
|---|---|---|---|
| 1 | `GET /cliente/1` | — | `200`, com `_links` `self` → `/cliente/1` e `clientes`; telefone, documentos e endereço com `self` |
| 2 | `GET /telefone/telefones` | — | `200`, cada telefone com `self` |
| 3 | `POST /telefone/cadastro/2` | `{"ddd":"12","numero":"988887777"}` | `201`, telefone com id 3 e links `self` → `/telefone/3` e `telefones` |
| 4 | `POST /documento/cadastro/2` | `{"tipo":"CNH","numero":"12345678900"}` | `201`, documento com id 3 e links |
| 5 | `POST /endereco/cadastro/2` | `{"cidade":"Taubaté","rua":"Rua B","numero":"20"}` | `201`, endereço com id 2 e links |
| 6 | `GET /cliente/2` | — | Maria com 2 telefones, 1 documento e endereço, todos com `self` |
| 7 | Seguir o link `self` do telefone 3 (`GET /telefone/3`) | — | `200`, o mesmo telefone |
| 8 | `PUT /endereco/atualizar/2` | `{"bairro":"Centro"}` | `200`, bairro preenchido e demais campos mantidos |
| 9 | `DELETE /telefone/excluir/3` | — | `204`, sem corpo |
| 10 | `POST /endereco/cadastro/1` | `{"cidade":"X","rua":"Y","numero":"1"}` | `409`, Dom Pedro já tem endereço |
| 11 | `POST /cliente/cadastro` | `{"id":2,"nome":"Teste"}` | `201`, cliente **novo** (id 3); Maria continua intacta |
| 12 | `GET /cliente/99` | — | `404` |
| 13 | `DELETE /cliente/excluir/1` | — | `204`. Em seguida, `GET /telefone/telefones` não mostra mais o telefone 1. |

### Usando `curl` (opcional)

Os comandos abaixo funcionam no **Linux, macOS e Git Bash**. No **PowerShell do Windows**, digite `curl.exe` em vez de `curl`. O `curl` sozinho é outro comando no PowerShell e não aceita essas opções. Em geral, é mais prático usar Postman ou Insomnia no Windows.

```bash
curl http://localhost:8080/cliente/1

curl -X POST http://localhost:8080/telefone/cadastro/2 \
     -H "Content-Type: application/json" \
     -d '{"ddd":"12","numero":"988887777"}'

curl -X PUT http://localhost:8080/endereco/atualizar/1 \
     -H "Content-Type: application/json" \
     -d '{"bairro":"Leme"}'

curl -X DELETE http://localhost:8080/telefone/excluir/1
```

---

## 9. Banco de dados (H2 Console)

Com a aplicação rodando, é possível visualizar as tabelas pelo navegador:

1. Acesse <http://localhost:8080/h2-console>
2. Preencha:

   | Campo | Valor |
   |---|---|
   | Driver Class | `org.h2.Driver` |
   | JDBC URL | `jdbc:h2:mem:automanager` |
   | User Name | `sa` |
   | Password | *(deixe vazio)* |

3. Clique em **Connect** e execute, por exemplo: `SELECT * FROM CLIENTE;`

As tabelas `CLIENTE_TELEFONES` e `CLIENTE_DOCUMENTOS` fazem a ligação entre o cliente e seus telefones e documentos. A coluna `ENDERECO_ID` da tabela `CLIENTE` aponta para o endereço. Os links do HATEOAS **não são gravados** no banco: eles são montados a cada resposta.

---

## 10. Alterações feitas no código

O código-base da ATVII trazia o HATEOAS iniciado apenas para o Cliente. As alterações foram:

| # | Onde | Situação no código-base | Alteração |
|---|---|---|---|
| 1 | Entidades | Só `Cliente` estendia `RepresentationModel` | `Telefone`, `Documento` e `Endereco` também estendem, com `@EqualsAndHashCode(callSuper = false)` para manter o `equals` do Lombok comparando só os dados |
| 2 | `modelo/` | Só existia `AdicionadorLinkCliente` | Criados `AdicionadorLinkTelefone`, `AdicionadorLinkDocumento` e `AdicionadorLinkEndereco` |
| 3 | `AdicionadorLinkCliente` | O cliente buscado por id recebia só o link `clientes`, **sem o `self`** | Todo recurso recebe `self`; o recurso avulso recebe também o link da coleção |
| 4 | `AdicionadorLinkCliente` | Telefones, documentos e endereço dentro do cliente não tinham links | Recebem o `self`, permitindo navegar do cliente até cada item |
| 5 | Controles | Buscas respondiam `302 Found`, que significa **redirecionamento** (fere o nível 2) | Buscas respondem `200 OK` |
| 6 | Controles | Lista vazia respondia `404` | Lista vazia responde `200` com `[]`, porque uma lista vazia é um resultado válido |
| 7 | Controles | `POST` e `PUT` respondiam sem corpo | Devolvem o recurso criado/atualizado, com os links |
| 8 | Controles | Links só no Cliente | Links em todas as rotas `GET`, `POST` e `PUT` das 4 entidades |
| 9 | `ClienteControle` | `POST /cliente/cadastro` com `id` no corpo **sobrescrevia** um cliente existente (e apagava os telefones dele) | O `id` do cliente e dos itens enviados é ignorado: o cadastro sempre cria registros novos |
| 10 | `ClienteControle` | Atualizar/excluir usavam `getById` (erro `500` com id inexistente) e recebiam o id no corpo | `findById` com resposta `404`, e o id vai na URL |
| 11 | `pom.xml` | — | Adicionada a dependência `spring-boot-starter-hateoas` |

### Estrutura do projeto

```
automanager/src/main/java/com/autobots/automanager/
├── AutomanagerApplication.java   → inicialização + dados de exemplo
├── controles/                    → rotas HTTP (Cliente, Telefone, Documento, Endereco)
├── entidades/                    → classes mapeadas para o banco (estendem RepresentationModel)
├── modelo/                       → regras de negócio
│   ├── AdicionadorLink.java          → interface dos adicionadores de links (HATEOAS)
│   ├── AdicionadorLink*.java         → links de Cliente, Telefone, Documento e Endereco
│   ├── *Atualizador.java             → atualização parcial dos dados
│   └── StringVerificadorNulo.java    → verifica textos nulos ou em branco
└── repositorios/                 → acesso ao banco (Spring Data JPA)
```

---

## 11. Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| `'java' não é reconhecido...` | Java não instalado ou fora do PATH | Instalar o JDK 17+ (seção 1) |
| `JAVA_HOME not found` ao rodar o `mvnw` | Variável de ambiente `JAVA_HOME` não configurada | Configurar `JAVA_HOME` apontando para a pasta do JDK |
| `mvnw : O termo 'mvnw' não é reconhecido` (PowerShell) | O PowerShell exige `.\` para arquivos da pasta atual | Usar `.\mvnw.cmd spring-boot:run` |
| `permission denied: ./mvnw` (Linux/macOS) | Arquivo sem permissão de execução | `chmod +x mvnw` |
| `Port 8080 was already in use` | Outra aplicação (ou outra instância desta) está usando a porta | Encerrar a outra aplicação, ou rodar em outra porta: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` |
| Eclipse mostra `The method getXxx() is undefined` | Plugin do Lombok não instalado no Eclipse | Seção 2, Opção B, passo 1 |
| Imports de `org.springframework.hateoas` não encontrados | Dependências do Maven não recarregadas depois de alterar o `pom.xml` | Eclipse: `Maven` → `Update Project...`. VS Code: `Java: Clean Java Language Server Workspace`. Terminal: `.\mvnw.cmd clean package` |
| Links aparecem como `"links": [...]` em vez de `"_links": {...}` | É uma resposta de **lista** | Comportamento esperado (seção 5, "Formato dos links nas listas") |
| Datas aparecem com um dia a mais ou horário estranho | As datas são exibidas em **UTC** (fuso de Brasília + 3h) | Comportamento esperado da serialização JSON |
