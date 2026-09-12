# StreamFIAP — Relatório do Bug Hunt (CP4)

---

## Bugs de comportamento (12/12)

### bug01 — Usuário não encontrado gerava erro 500 genérico
- **Sintoma:** `GET /api/usuarios/999` ou `POST /api/alugueis?usuarioId=999&...` retornava `500 Internal Server Error` sem mensagem clara.
- **Causa raiz:** o código usava `IllegalArgumentException` (genérica), sem handler no `GlobalExceptionHandler`.
- **Correção:** criada `UsuarioNaoEncontradoException` (mesmo padrão de `ConteudoNaoEncontradoException`), com handler correspondente.
- **Conceito da disciplina:** Hierarquia de exceções customizadas / consistência com `@RestControllerAdvice`.
- **Commit:** [fix: bug01](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/f9e62dcd5eb7204b2620ae1f24f44919269b4cc6)

### bug02 — `ClassificacaoIndicativaException` nunca chegava ao cliente
- **Sintoma:** alugar conteúdo com classificação acima da idade do usuário retornava `500 Internal Server Error`.
- **Causa raiz:** `GlobalExceptionHandler` não tinha `@ExceptionHandler` para essa exceção.
- **Correção:** adicionado o handler, retornando status apropriado com a mensagem.
- **Conceito da disciplina:** Cobertura completa do tratamento de exceções via `@RestControllerAdvice`.
- **Commit:** [fix: bug02](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/2e7960df5439db4bb839cef43866a069feb19d37)

### bug03 — Nome do usuário nunca era salvo
- **Sintoma:** `GET /api/usuarios/{id}` sempre retornava `"nome": null`.
- **Causa raiz:** construtor de `Usuario` fazia `nome = nome;` em vez de `this.nome = nome;`.
- **Correção:** `this.nome = nome;`
- **Conceito da disciplina:** Sombreamento de variável (shadowing) / encapsulamento.
- **Commit:** [fix: bug03](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/89a2868f541518e80fc1b3a9fafc51858583aa15)

### bug04 — Usuário sem créditos conseguia alugar
- **Sintoma:** usuário com `0` créditos alugava um filme de `R$9,90` sem erro.
- **Causa raiz:** `temCreditosSuficientes` estava com a lógica invertida (`preco >= creditos` em vez de `creditos >= preco`).
- **Correção:** `return this.creditos >= preco;`
- **Conceito da disciplina:** Expressões booleanas / nomeação de métodos coerente com o comportamento.
- **Commit:** [fix: bug04](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/2ae1754b2b5ee6bcda9749b1e91b7b0f43a6f86e)

### bug05 — Conteúdo indisponível podia ser alugado
- **Sintoma:** alugar um conteúdo com `disponivel: false` funcionava normalmente, sem nenhum erro.
- **Causa raiz:** `Usuario.alugar` nunca verificava `c.isDisponivel()` — a regra simplesmente não existia no código.
- **Correção:** adicionada a checagem, lançando `ConteudoIndisponivelException` quando indisponível.
- **Conceito da disciplina:** Regra de negócio no domínio (model) + exceção customizada.
- **Commit:** [fix: bug05](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/c9effdd15e2170802284a14cc780cdb79fd9f589)

### bug06 — Série perdia título, categoria, duração e classificação ao cadastrar
- **Sintoma:** `POST /api/conteudos/serie` retornava o objeto salvo com campos herdados nulos/zerados.
- **Causa raiz:** o construtor de `Serie` não chamava `super(...)`.
- **Correção:** adicionada a chamada ao construtor da superclasse.
- **Conceito da disciplina:** Herança — chamada obrigatória ao construtor da superclasse.
- **Commit:** [fix: bug06](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/bb0caf85ff45399c52220d9fc698909129ff9199)

### bug07 — Documentário cobrava R$9,90 em vez de ser gratuito
- **Sintoma:** preço de um documentário retornava `9.90` em vez de `0.00`.
- **Causa raiz:** `Documentario` não sobrescrevia `calcularPrecoAluguel()`, herdando o valor padrão da superclasse.
- **Correção:** adicionado `@Override public double calcularPrecoAluguel() { return 0.0; }`.
- **Conceito da disciplina:** Polimorfismo / sobrescrita de método.
- **Commit:** [fix: bug07](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/5dd50594c3d0bb5843dd5a5dcc143579a9739865)

### bug08 — Promoção de filme aumentava o preço em vez de dar desconto
- **Sintoma:** preço promocional de um filme saía maior que o preço normal.
- **Causa raiz:** `Filme.aplicarPromocao` calculava `preco * 1.2` (acréscimo de 20%) em vez de desconto.
- **Correção:** `return preco * 0.8;`
- **Conceito da disciplina:** Contrato de interface (`Promocionavel`) — implementação precisa cumprir o que a interface promete.
- **Commit:** [fix: bug08](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/7e388752cc59a9c6e710de9b9a00e2f3799c6836)

### bug09 — Série nunca custava R$4,90 por temporada
- **Sintoma:** alugar uma série de 5 temporadas debitava `9.90` fixo em vez de `24.50`.
- **Causa raiz:** `Serie.calcularPrecoAluguel(double desconto)` tinha assinatura diferente da superclasse — era um overload, não um override; o método realmente chamado continuava sendo o de `Conteudo` (9.90 fixo).
- **Correção:** removido o parâmetro e adicionado `@Override`.
- **Conceito da disciplina:** Override vs. overload; uso de `@Override` para detectar o erro em tempo de compilação.
- **Commit:** [fix: bug09](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/4e7682b3ea76c765ea1e7da0813e8e7941d2af48)

### bug10 — Filtro por categoria nunca retornava resultados corretos
- **Sintoma:** `GET /api/conteudos/categoria/FICCAO` retornava lista vazia mesmo havendo conteúdos com essa categoria.
- **Causa raiz:** `if (c.getCategoria() == categoria)` comparava Strings por referência (`==`) em vez de valor (`.equals()`).
- **Correção:** substituído pelo uso do `conteudoRepository.findByCategoria(categoria)`, já existente no repository (essa correção resolveu ao mesmo tempo o bug de comparação e a duplicação de lógica — por isso o commit está catalogado junto do item de Clean Code correspondente).
- **Conceito da disciplina:** `==` vs `.equals()` em Java / uso idiomático do Spring Data JPA.
- **Commit:** [refactor: clean04 (inclui a correção do bug de comparação)](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/9d9209eb42f70bf7e7040e9cbe83c691cf6ad71e)

### bug11 — Buscar conteúdo inexistente retornava 200 vazio em vez de 404
- **Sintoma:** `GET /api/conteudos/999` retornava `200 OK` com corpo vazio, em vez de `404` com mensagem.
- **Causa raiz:** `ConteudoController.buscarPorId` tinha um `catch (Exception e) {}` vazio que engolia a `ConteudoNaoEncontradoException`.
- **Correção:** removido o try/catch, deixando a exceção propagar para o `GlobalExceptionHandler`.
- **Conceito da disciplina:** Propagação vs. supressão de exceções.
- **Commit:** [hotfix: Bug1 (reenvio de arquivos)](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/ccba1c75f3a67000895a05b512140883872379ad)

### bug12 — Cadastro de usuário falhava (id nunca gerado)
- **Sintoma:** `POST /api/usuarios` falhava no banco (violação de chave primária) ou salvava com id nulo.
- **Causa raiz:** `Usuario.id` estava anotado só com `@Id`, sem `@GeneratedValue`, diferente de `Conteudo.id`.
- **Correção:** `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;`
- **Conceito da disciplina:** Mapeamento JPA / geração automática de chave primária.
- **Commit:** [fix: bug12](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/2ab397c)

---

## Ajustes de Clean Code (6/6)

### clean01 — Comentário contradizia o código
- **Onde:** `Usuario.debitarCreditos`
- **Problema:** comentário dizia "adiciona os créditos", mas o código subtraía.
- **Correção:** texto do comentário corrigido para refletir o comportamento real.
- **Conceito da disciplina:** Comentários devem estar sincronizados com o código.
- **Commit:** [refactor: clean01](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/056471b16a520ed6231dc9f7bc07ce1c37647787)

### clean02 — Método morto e código comentado
- **Onde:** `ConteudoController`
- **Problema:** método `calcularDescontoAntigo` nunca chamado + bloco de código comentado (regra de cupom).
- **Correção:** ambos removidos.
- **Conceito da disciplina:** Eliminar código morto; comentários devem explicar o "porquê", não guardar código morto.
- **Commit:** [refactor: clean02](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/dd11ee58aee6a0073c6809f7e6e741b6c0b9c804)

### clean03 — Campo público quebrava o encapsulamento
- **Onde:** `Conteudo.duracaoMinutos`
- **Problema:** campo `public`, inconsistente com o resto da classe (private + getter/setter).
- **Correção:** campo tornado `private`; acessos no controller ajustados para usar o getter.
- **Conceito da disciplina:** Encapsulamento.
- **Commit:** [refactor: clean03](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/17e60cd784064928cf8c5d4512d2f2122b63d2f3)

### clean04 — Reimplementação manual de consulta já existente
- **Onde:** `ConteudoController.listarPorCategoria`
- **Problema:** filtrava manualmente em loop em vez de usar `ConteudoRepository.findByCategoria`, já existente.
- **Correção:** chamada direta ao método do repository (essa correção também resolveu o bug10, de comparação de Strings).
- **Conceito da disciplina:** Uso idiomático do Spring Data JPA / eliminação de duplicação.
- **Commit:** [refactor: clean04](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/9d9209eb42f70bf7e7040e9cbe83c691cf6ad71e)

### clean05 — Nome de variável sem significado
- **Onde:** `Usuario.alugar`
- **Problema:** variável `p` (preço) sem nome descritivo.
- **Correção:** renomeada para `preco`.
- **Conceito da disciplina:** Nomeação significativa de variáveis (legibilidade).
- **Commit:** [refactor: clean05](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/159f90a77c9f7aed180efc57939080a77f8cfc97)

### clean06 — Números mágicos espalhados pelo código
- **Onde:** `Conteudo`, `Filme`, `Serie`
- **Problema:** valores como `9.90`, `5.00`, `4.90`, `0.8` usados diretamente no código, sem nome.
- **Correção:** extraídos para constantes nomeadas (`PRECO_BASE`, `ADICIONAL_ESTREIA`, `PRECO_POR_TEMPORADA`, `DESCONTO_PROMOCAO`).
- **Conceito da disciplina:** Eliminação de números mágicos / legibilidade e manutenibilidade.
- **Commit:** [refactor: clean06](https://github.com/BsWasKilled/FiapStreamBugHunt/commit/7eb14c235e8a9990e06633659dad420568b7a4d7)

---

## Integrantes

| Nome | RM | Principais contribuições |
|------|----|-----|
| João Vitor Angeloti (usuário GitHub: jvsen211) | 563473 | bugs 01, 02, 03, 04, 05, 06, 07, 08, 09, 12 |
| Bernardo (usuário GitHub: BsWasKilled) | 565776 | clean01, 02, 03, 04, 05, 06 |

## Reflexões

1. **Qual foi o bug mais difícil de encontrar, e por quê?**
   O bug da `Serie.calcularPrecoAluguel(double desconto)` foi o mais traiçoeiro. O código parecia certo à primeira vista — tinha um método com o cálculo correto (`4.90 * numeroTemporadas`) e até um comentário explicando a regra. O problema é que esse método tinha um parâmetro que não existia na superclasse, então nunca era chamado de verdade: quem rodava era o método herdado de `Conteudo`, que retorna `9.90` fixo. Só percebemos comparando a assinatura desse método com a da superclasse, e reparando que faltava um `@Override`.

2. **Qual foi o bug mais fácil, e por quê?**
   O `nome = nome;` no construtor de `Usuario`. Bastou ler a linha uma vez para perceber que faltava o `this.` — é um erro clássico de shadowing que qualquer IDE já destaca visualmente.

3. **Algum bug só apareceu depois de corrigir outro (bug em cascata)? Qual?**
   Sim. Antes de corrigirmos a checagem de disponibilidade em `Usuario.alugar`, não dava pra perceber que o preço da série também estava errado, porque os primeiros testes com série falhavam antes mesmo de chegar no cálculo de preço (por causa do `super()` ausente, que zerava os dados). Só depois de corrigir o construtor da `Serie` foi possível perceber que o preço continuava saindo errado.

4. **O que vocês fariam diferente numa próxima entrega parecida?**
   Seríamos mais disciplinados em manter um commit por correção desde o início, sem misturar mudanças de arquivos diferentes no mesmo commit. Em alguns momentos, uma correção emergencial de sintaxe (como reenviar arquivos que faltaram no primeiro commit) acabou levando junto outras correções sem essas aparecerem separadamente na mensagem — o que dificulta rastrear exatamente quando cada bug foi resolvido.

5. **Qual conceito da disciplina vocês sentem que entenderam melhor depois desse trabalho?**
   A diferença entre **override e overload** ficou muito mais clara depois do bug da série — ver na prática como um método com assinatura levemente diferente (um parâmetro a mais) simplesmente não sobrescreve nada e passa despercebido sem o `@Override` foi um aprendizado que não teria vindo só de teoria.

6. **Trabalhar em grupo, dividindo bugs entre os integrantes, ajudou ou atrapalhou o processo?**
   Ajudou a paralelizar o trabalho (um focando nos bugs de comportamento, outro no Clean Code), mas também gerou alguns imprevistos de Git — merges com conflitos, e até uma correção que foi perdida e precisou ser refeita depois de um merge malfeito. Aprendemos na prática a importância de rodar `git pull` com frequência e de testar o projeto depois de cada merge, não só depois de cada commit individual.
