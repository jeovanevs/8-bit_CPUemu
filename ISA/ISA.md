# LittleEmu — Especificação da Arquitetura do Conjunto de Instruções (ISA)

Este documento descreve a especificação técnica completa da arquitetura do conjunto de instruções (**ISA - Instruction Set Architecture**) do **LittleEmu**, um processador didático de 8 bits baseado no modelo de Von Neumann.

---

## 1. Visão Geral do Hardware

| Característica | Especificação | Descrição |
| :--- | :--- | :--- |
| **Largura da Palavra** | 8 bits (1 byte) | Cada dado ou instrução ocupa exatamente 8 bits. |
| **Espaço de Endereçamento** | 4 bits (16 bytes) | Endereços de memória válidos vão de `0` a `15` (`0x0` a `0xF` / `0000` a `1111`). |
| **Memória RAM** | 16 posições de 8 bits | Memória compartilhada para código e dados. |
| **Registradores de Uso Geral** | 4 registradores (8 bits cada) | **`A`**, **`B`**, **`C`** e **`D`**. |
| **Registradores Especiais** | 2 registradores (8 bits cada) | **`Address Register`** (PC / MAR) e **`Instruction Register`** (IR). |
| **Aritmética da ULA** | 8 bits com sinal | Representação em complemento de dois (faixa de `-128` a `+127`). |
| **Flags de Status** | 3 flags na Unidade de Controle | **`O`** (Overflow), **`Z`** (Zero) e **`N`** (Negative). |

---

## 2. Layout e Codificação das Instruções

Cada instrução possui exatamente **8 bits**, organizados em um dos seguintes formatos:

### Formato M — Memória (Carga e Armazenamento)
Usado pelas instruções `LOAD_*` e `STORE_*`:
```text
 7   6   5   4   3   2   1   0
+---+---+---+---+---+---+---+---+
|    OPCODE     |  ENDEREÇO RAM |
+---+---+---+---+---+---+---+---+
  4 bits (0-3)     4 bits (4-7)
```
* **Bits 7..4:** Código da operação (Opcode).
* **Bits 3..0:** Endereço da RAM (`0000` a `1111` / `0` a `15`).

---

### Formato R — Registrador para Registrador (Aritmética)
Usado pelas instruções `ADD` e `SUB`:
```text
 7   6   5   4   3   2   1   0
+---+---+---+---+---+---+---+---+
|    OPCODE     | REG1  | REG2  |
+---+---+---+---+---+---+---+---+
  4 bits (0-3)   2 bits  2 bits
```
* **Bits 7..4:** Código da operação (`1000` para `ADD`, `1001` para `SUB`).
* **Bits 3..2:** Registrador de Destino / Primeiro Operando (`REG1`).
* **Bits 1..0:** Registrador de Origem / Segundo Operando (`REG2`).
* **Operação:** `REG1 <- REG1 (op) REG2`.

#### Codificação dos Registradores:
| Código Binário | Registrador | Em Decimal |
| :---: | :---: | :---: |
| `00` | Registrador A (`Reg_A`) | `0` |
| `01` | Registrador B (`Reg_B`) | `1` |
| `10` | Registrador C (`Reg_C`) | `2` |
| `11` | Registrador D (`Reg_D`) | `3` |

---

### Formato J — Desvio / Salto (Jumps)
Usado por instruções de salto incondicional e condicional:
```text
 7   6   5   4   3   2   1   0
+---+---+---+---+---+---+---+---+
|    OPCODE     | ENDEREÇO ALVO |
+---+---+---+---+---+---+---+---+
  4 bits (0-3)     4 bits (4-7)
```
* **Bits 7..4:** Código da operação de salto (`1010` a `1110`).
* **Bits 3..0:** Endereço da RAM de destino do salto (`XXXX`).

---

### Formato C — Controle (Halt)
```text
 7   6   5   4   3   2   1   0
+---+---+---+---+---+---+---+---+
| 1   1   1   1 | 0   0   0   0 |
+---+---+---+---+---+---+---+---+
     OPCODE        Não utilizado
```

---

## 3. Tabela Completa de Instruções

| Opcode | Mnemônico | Formato | Operação (RTL) | Descrição |
| :---: | :--- | :---: | :--- | :--- |
| `0000` | **`LOAD_D XXXX`** | M | `D <- RAM[XXXX]` | Carrega no Registrador D o byte da RAM no endereço `XXXX`. |
| `0001` | **`LOAD_B XXXX`** | M | `B <- RAM[XXXX]` | Carrega no Registrador B o byte da RAM no endereço `XXXX`. |
| `0010` | **`LOAD_A XXXX`** | M | `A <- RAM[XXXX]` | Carrega no Registrador A o byte da RAM no endereço `XXXX`. |
| `0011` | **`LOAD_C XXXX`** | M | `C <- RAM[XXXX]` | Carrega no Registrador C o byte da RAM no endereço `XXXX`. |
| `0100` | **`STORE_A XXXX`** | M | `RAM[XXXX] <- A` | Grava o valor do Registrador A no endereço `XXXX` da RAM. |
| `0101` | **`STORE_B XXXX`** | M | `RAM[XXXX] <- B` | Grava o valor do Registrador B no endereço `XXXX` da RAM. |
| `0110` | **`STORE_C XXXX`** | M | `RAM[XXXX] <- C` | Grava o valor do Registrador C no endereço `XXXX` da RAM. |
| `0111` | **`STORE_D XXXX`** | M | `RAM[XXXX] <- D` | Grava o valor do Registrador D no endereço `XXXX` da RAM. |
| `1000` | **`ADD XX, YY`** | R | `XX <- XX + YY` | Soma `XX` com `YY`, salva em `XX` e atualiza as flags `O`, `Z`, `N`. |
| `1001` | **`SUB XX, YY`** | R | `XX <- XX - YY` | Subtrai `YY` de `XX`, salva em `XX` e atualiza as flags `O`, `Z`, `N`. |
| `1010` | **`JUMP XXXX`** | J | `PC <- XXXX` | Salto incondicional: transfere a execução para o endereço `XXXX`. |
| `1011` | **`JUMP_NEG XXXX`**| J | Se `N == 1`:<br>`PC <- XXXX`<br>Senão: `PC <- PC + 1` | Salto condicional se negativo: salta se a flag `N` estiver ativa. |
| `1100` | **`JUMP_ZRO XXXX`**| J | Se `Z == 1`:<br>`PC <- XXXX`<br>Senão: `PC <- PC + 1` | Salto condicional se zero: salta se a flag `Z` estiver ativa. |
| `1101` | **`JUMP_ABV XXXX`**| J | Se `Z == 0` e `N == 0`:<br>`PC <- XXXX`<br>Senão: `PC <- PC + 1` | Salto condicional se acima de zero: salta se o resultado for estritamente positivo ($> 0$). |
| `1110` | **`JUMP_OFW XXXX`**| J | Se `O == 1`:<br>`PC <- XXXX`<br>Senão: `PC <- PC + 1` | Salto condicional se overflow: salta se a flag `O` estiver ativa. |
| `1111` | **`HALT`** | C | `STOP` | Interrompe o ciclo de clock e suspende a execução da CPU. |

---

## 4. Flags de Status e Comportamento da ULA

As flags ficam localizadas na Unidade de Controle e **são alteradas exclusivamente pelas instruções `ADD` e `SUB`**. Quando alteradas, seus valores permanecem inalterados até que uma nova operação aritmética seja executada.

1. **`O_flag` (Overflow):**
   * Ativada (`true`) quando o resultado matemático da operação excede a capacidade de representação de 8 bits com sinal ($-128$ a $+127$).
   * *Exemplo:* $100 + 50 = 150 > 127 \implies O = 1$.

2. **`Z_flag` (Zero):**
   * Ativada (`true`) quando o resultado da operação for exatamente igual a zero ($0$).
   * *Exemplo:* $5 - 5 = 0 \implies Z = 1$.

3. **`N_flag` (Negative):**
   * Ativada (`true`) quando o resultado da operação for negativo ($< 0$).
   * *Exemplo:* $3 - 8 = -5 \implies N = 1$.

---

## 5. Ciclo de Instrução da CPU

A execução de qualquer instrução no LittleEmu segue os 3 estágios clássicos:

```text
+-------------------+      +--------------------+      +--------------------+
|  1. FETCH (Busca) | ---> | 2. DECODE (Decod.) | ---> | 3. EXECUTE (Exec.) |
+-------------------+      +--------------------+      +--------------------+
          ^                                                       |
          +-------------------------------------------------------+
```

1. **FETCH (Busca):**
   * O endereço atual presente no `Address Register` (Program Counter) é enviado ao barramento de endereços.
   * O sinal de controle `readEnable` da RAM é ativado.
   * O byte armazenado na posição da RAM correspondente é transferido pelo barramento de dados para o `Instruction Register` (IR).

2. **DECODE (Decodificação):**
   * A Unidade de Controle decodifica os 4 bits superiores do IR para determinar o Opcode.
   * Os 4 bits inferiores são interpretados como endereço (`XXXX`) ou divididos em pares de bits para selecionar os registradores (`XX`, `YY`).

3. **EXECUTE (Execução):**
   * A operação correspondente é disparada nos barramentos, ULA ou registradores.
   * **Controle do Program Counter:**
     * Para instruções normais (`LOAD`, `STORE`, `ADD`, `SUB`) ou saltos condicionais não satisfeitos, o `Address Register` é incrementado em `+1`.
     * Para saltos satisfeitos (`JUMP` ou condições verdadeiras de `JUMP_NEG`, `JUMP_ZRO`, `JUMP_ABV`, `JUMP_OFW`), o endereço alvo `XXXX` é carregado diretamente no `Address Register`.
     * Para a instrução `HALT`, a thread de clock é interrompida.

---

## 6. Guia de Sintaxe do Montador (Assembly)

O editor integrado do LittleEmu aceita arquivos `.asm` ou `.txt` com as seguintes regras de formatação:

### Bases Numéricas Suportadas
Valores e endereços podem ser escritos em três sistemas numéricos usando sufixos (maiúsculos ou minúsculos):
* **Binário:** sufixo `B` ou `b` (ex.: `0010B`, `1101B`)
* **Decimal:** sufixo `D` ou `d` (ex.: `13D`, `5D`)
* **Hexadecimal:** sufixo `H` ou `h` (ex.: `Eh`, `Ah`)

### Referência a Registradores
Em instruções aritméticas (`ADD` e `SUB`), os registradores podem ser identificados por letras ou por número da base:
* Por letra: `ADD A, B`
* Por decimal: `ADD 0D, 1D`
* Por binário: `ADD 00B, 01B`
* Por hexadecimal: `ADD 0H, 1H`

### Comentários e Diretivas
* Linhas iniciadas por `#` são tratadas como diretivas e ignoradas durante a montagem (ex.: `#ASSEMBLY`).
* Comentários de linha são iniciados com `//` e descartados.
* Linhas em branco e múltiplos espaços/tabulações são normalizados automaticamente.
* O programa montado pode ter no máximo **16 instruções**, correspondentes ao tamanho total da RAM.

---

## 7. Exemplos Práticos de Código

### Exemplo 1: Soma de Dois Números
Carrega dois valores armazenados nas posições `13` e `14`, soma-os no registrador `A` e armazena o resultado na posição `15`:

```asm
#ASSEMBLY
LOAD_A 13D       // Carrega dado da RAM[13] no registrador A
LOAD_B 14D       // Carrega dado da RAM[14] no registrador B
ADD A, B         // A = A + B (atualiza flags Z, N, O)
STORE_A 15D      // Salva o resultado na RAM[15]
HALT             // Encerra a execução
```

### Exemplo 2: Salto Condicional com Verificação de Zero
Subtrai dois números e, caso o resultado seja zero, desvia para o endereço de armazenamento:

```asm
#ASSEMBLY
LOAD_A 14D       // Carrega valor no reg A
LOAD_B 15D       // Carrega valor no reg B
SUB A, B         // A = A - B
JUMP_ZRO 6D      // Se A == 0 (Z_flag == 1), salta para a linha 6
LOAD_C 13D       // Caminho se diferente de zero
STORE_A 12D      // Linha 6: Armazena o resultado
HALT
```

### Exemplo 3: Detecção de Overflow
Soma dois valores positivos altos; se houver estouro da capacidade de 8 bits com sinal, desvia para o tratamento de overflow:

```asm
#ASSEMBLY
LOAD_A 13D       // Suponha valor 100
LOAD_B 14D       // Suponha valor 50 (soma = 150 > 127)
ADD A, B         // Gera Overflow (O_flag = 1)
JUMP_OFW 6D      // Desvia para endereço 6 em caso de overflow
STORE_A 15D      // Caminho normal
HALT
STORE_A 0D       // Linha 6: Tratamento do overflow
HALT
```

---

## 8. Programas de Demonstração (Demos em Assembly)

Esta seção documenta os 5 programas de demonstração integrados ao simulador LittleEmu, convertidos do código de máquina original para a sintaxe Assembly padronizada, com comentários explicativos e detalhamento do estado da memória.

### Demo 1: Somar Dois Números (`DEMO1`)
* **Objetivo:** Efetuar a soma aritmética de dois valores inteiros presentes na memória e armazenar o resultado final.
* **Mapa de Memória:**
  * `RAM[13]`: Primeiro operando = `25` (`00011001`)
  * `RAM[14]`: Segundo operando = `8` (`00001000`)
  * `RAM[15]`: Destino do resultado = `33` (`00100001`)

```asm
#ASSEMBLY
LOAD_A 13D       // Linha 0 (00101101): Carrega RAM[13] (25) no registrador A
LOAD_B 14D       // Linha 1 (00011110): Carrega RAM[14] (8) no registrador B
ADD A, B         // Linha 2 (10000001): A = A + B (25 + 8 = 33)
STORE_A 15D      // Linha 3 (01001111): Salva o resultado (33) na RAM[15]
HALT             // Linha 4 (11110000): Encerra a execução
```

---

### Demo 2: Subtrair Dois Números (`DEMO2`)
* **Objetivo:** Realizar a subtração de dois números armazenados na RAM e gravar a diferença na memória.
* **Mapa de Memória:**
  * `RAM[13]`: Minuendo = `90` (`01011010`)
  * `RAM[14]`: Subtraendo = `59` (`00111011`)
  * `RAM[15]`: Destino do resultado = `31` (`00011111`)

```asm
#ASSEMBLY
LOAD_A 13D       // Linha 0 (00101101): Carrega RAM[13] (90) no registrador A
LOAD_B 14D       // Linha 1 (00011110): Carrega RAM[14] (59) no registrador B
SUB A, B         // Linha 2 (10010001): A = A - B (90 - 59 = 31)
STORE_A 15D      // Linha 3 (01001111): Salva o resultado (31) na RAM[15]
HALT             // Linha 4 (11110000): Encerra a execução
```

---

### Demo 3: Loop Infinito (`DEMO3`)
* **Objetivo:** Demonstrar uma estrutura de repetição incondicional (laço infinito) decrementando continuamente um registrador e persistindo o valor a cada iteração.
* **Mapa de Memória:**
  * `RAM[14]`: Valor inicial do acumulador = `10` (`00001010`)
  * `RAM[15]`: Valor de passo a decrementar = `11` (`00001011`)
  * `RAM[13]`: Destino onde o valor decrescente é salvo a cada iteração

```asm
#ASSEMBLY
LOAD_A 14D       // Linha 0 (00101110): Carrega valor inicial (10) no reg A
LOAD_B 15D       // Linha 1 (00011111): Carrega passo (11) no reg B
SUB A, B         // Linha 2 (10010001): Início do loop: A = A - B
STORE_A 13D      // Linha 3 (01001101): Salva valor atual de A na RAM[13]
JUMP 2D          // Linha 4 (10100010): Salto incondicional de volta à Linha 2
LOAD_C 13D       // Linha 5 (00111101): Instrução inalcançável (após o loop)
LOAD_D 13D       // Linha 6 (00001101): Instrução inalcançável
ADD C, D         // Linha 7 (10001011): Instrução inalcançável
HALT             // Linha 8 (11110000): Instrução inalcançável
```

---

### Demo 4: Divisão de Dois Números (`DEMO4`)
* **Objetivo:** Calcular o quociente e o resto da divisão inteira utilizando o algoritmo de subtrações sucessivas.
* **Mapa de Memória:**
  * `RAM[12]`: Quociente acumulado inicial = `0` (`00000000`)
  * `RAM[13]`: Incremento do contador = `1` (`00000001`); ao final recebe o Resto da divisão
  * `RAM[14]`: Dividendo inicial = `97` (`01100001`)
  * `RAM[15]`: Divisor = `10` (`00001010`)
  * *Resultado esperado ao término:* Quociente em `RAM[12]` = `9` (`00001001`) e Resto em `RAM[13]` = `7` (`00000111`).

```asm
#ASSEMBLY
LOAD_C 12D       // Linha 0  (00111100): Carrega quociente inicial (0) no reg C
LOAD_D 13D       // Linha 1  (00001101): Carrega incremento (1) no reg D
LOAD_A 14D       // Linha 2  (00101110): Carrega dividendo (97) no reg A
LOAD_B 15D       // Linha 3  (00011111): Carrega divisor (10) no reg B
SUB A, B         // Linha 4  (10010001): Início do loop: subtrai divisor (A = A - B)
JUMP_NEG 8D      // Linha 5  (10111000): Se A < 0 (N == 1), desvia para finalização (Linha 8)
ADD C, D         // Linha 6  (10001011): Incrementa quociente (C = C + D)
JUMP 4D          // Linha 7  (10100100): Salta de volta para a Linha 4 (próxima subtração)
STORE_C 12D      // Linha 8  (01101100): Salva o quociente calculado na RAM[12]
ADD A, B         // Linha 9  (10000001): Restaura o resto positivo em A (A = A + B)
STORE_A 13D      // Linha 10 (01001101): Salva o resto da divisão na RAM[13]
HALT             // Linha 11 (11110000): Encerra a execução
```

---

### Demo 5: Multiplicação de Dois Números (`DEMO5`)
* **Objetivo:** Realizar a multiplicação de dois inteiros utilizando o algoritmo de somas sucessivas com controle de repetições por contador.
* **Mapa de Memória:**
  * `RAM[13]`: Passo de decremento = `1` (`00000001`)
  * `RAM[14]`: Multiplicando = `7` (`00000111`)
  * `RAM[15]`: Multiplicador (número de repetições) = `8` (`00001000`)
  * `RAM[12]`: Destino do produto = `56` (`00111000`)

```asm
#ASSEMBLY
LOAD_A 14D       // Linha 0 (00101110): Carrega multiplicando (7) no reg A (acumulador do produto)
LOAD_B 14D       // Linha 1 (00011110): Carrega multiplicando (7) no reg B (parcela fixa da soma)
LOAD_C 15D       // Linha 2 (00111111): Carrega multiplicador (8) no reg C (contador de iterações)
LOAD_D 13D       // Linha 3 (00001101): Carrega decremento (1) no reg D
SUB C, D         // Linha 4 (10011011): Início do loop: decrementa contador (C = C - D)
JUMP_ZRO 8D      // Linha 5 (11001000): Se C == 0 (Z == 1), encerrou as somas: salta para Linha 8
ADD A, B         // Linha 6 (10000001): Acumula produto: A = A + B
JUMP 4D          // Linha 7 (10100100): Salta de volta para a Linha 4 para a próxima iteração
STORE_A 12D      // Linha 8 (01001100): Grava o produto final (56) na RAM[12]
HALT             // Linha 9 (11110000): Encerra a execução
```

