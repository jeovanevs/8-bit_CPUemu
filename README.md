# 8-bit_CPUemu

Tiny-Emu é um emulador de uma CPU hipotética de 8 bits muito simples. Sua interface gráfica ajuda a aprender, de forma intuitiva, como os registradores, a unidade de controle, a ULA e a memória RAM interagem durante a execução de um programa.

![tinyemu](https://user-images.githubusercontent.com/57422146/174094497-5a3b915a-fb8a-468a-b58d-00e4c43de654.png)

## Executar

O projeto usa Java 8 com JavaFX. Para executar o JAR distribuído:

```bash
java -jar dist/LittleEmu.jar
```

## Editor de código

Use o botão de novo arquivo para abrir o editor. O editor permite carregar e
salvar arquivos `.asm` ou `.txt`, formatar o código e inspecionar instruções
Assembly.
Após uma inspeção bem-sucedida, as instruções são convertidas para 8 bits,
carregadas na RAM e podem ser executadas pelos controles da CPU.

Exemplo de programa Assembly:

```asm
#ASSEMBLY
LOAD_A 13D
LOAD_B 14D
ADD A,B
STORE_A 15D
HALT
```

Comentários iniciados por `//`, linhas vazias e diretivas iniciadas por `#`
são ignorados durante a inspeção.

## Arquivos binários

Também é possível carregar diretamente um arquivo de memória. Cada linha deve
conter exatamente 8 bits, e o arquivo pode possuir no máximo 16 linhas:

```text
00101101
00011110
10000001
01001111
11110000
```

O arquivo inteiro é validado antes de alterar a RAM. Linhas com tamanho
incorreto ou caracteres diferentes de `0` e `1` são rejeitadas.
