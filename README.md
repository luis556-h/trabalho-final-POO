# Trabalho final de Programação Orientada a Objetos  
- Professor: Rene Douglas Nobre de Morais  
- Aluno: Luis Henrique de Carvalho Honorio  

# Math Snake

Um jogo educativo da cobrinha (Snake) com mecânicas de multiplicação matemática! Desenvolvido em Java com Swing/AWT como trabalho final de Programação Orientada a Objetos.

## Conceito do Jogo

Ao invés de simplesmente comer frutas, você precisa usar estratégia matemática para multiplicar números e atingir um **alvo específico**.

### Como Funciona:

1. **Número Alvo**: Aparece no topo da tela (ex: 24)
2. **Produto Atual**: Começa em 1 e é multiplicado pelos números que você come
3. **Frutas Numeradas**: Cada fruta contém um número de 2 a 9
4. **Objetivo**: Coma a sequência correta de números para que o produto seja exatamente igual ao alvo!

## Lógica do Jogo

### ✅ Condição de Vitória
Quando você come uma fruta e o produto resultante é **exatamente igual ao alvo**:
- ✅ Você ganha 10 pontos + (tamanho da cobra × 2)
- ✅ A cobra cresce em 1 segmento
- ✅ Flash verde suave no cenário (confirmação visual)
- ✅ Um novo alvo é gerado
- ✅ O produto atual reseta para 1
- ✅ Novas frutas são posicionadas na tela

### ❌ Condição de Penalidade
Acontece quando você come um número que resulta em:
- **Produto > Alvo**: Ultrapassou o número necessário
- **Alvo % Produto ≠ 0**: O produto não é divisor do alvo

**Consequências da Penalidade:**
- ❌ Perde 1 vida (de 3 vidas totais)
- ❌ A cobra diminui em 2 segmentos
- ❌ Flash vermelho suave no cenário
- ❌ Produto atual reseta para 1
- ❌ O alvo permanece o mesmo
- ❌ Novas frutas são regeneradas

### Game Over
O jogo termina quando:
1. Suas vidas chegam a 0 (após penalidades)
2. A cobra colide consigo mesma

## Como Jogar

### Controles

**Menu Principal:**
- **↑ ↓**: Navegar entre opções
- **ENTER**: Selecionar opção

**Durante o Jogo:**
- **↑ ↓ ← →**: Controlar direção da cobra
- *Observação: Não pode fazer 180° (virar para trás diretamente)*

**Tela de Game Over:**
- **R**: Reiniciar o jogo
- **ESC**: Voltar ao menu principal

### Como Executar

#### Pré-requisitos
- Java 8 ou superior instalado
- (Opcional) Arquivo de áudio `sdtrack.mp3` ou `sdtrack.wav` para música de fundo

#### Compilar
```bash
javac App.java
```

#### Executar
```bash
java App
```

### Alvos Possíveis:
```
12, 15, 18, 20, 24, 28, 30, 32, 36, 40, 42, 45, 48, 54, 56, 60,
63, 64, 72, 80, 81, 84, 90, 96, 100, 108, 120, 144
```

### Exemplo de Sequências Vencedoras:
```
Alvo: 60

Opção 1: 5 → 12 (5×12=60) ✅
Opção 2: 3 → 4 → 5 (3×4×5=60) ✅
Opção 3: 2 → 2 → 3 → 5 (2×2×3×5=60) ✅
Opção 4: 6 → 2 → 5 (6×2×5=60) ✅
```

## Sistema de Pontuação

- **Acertar o alvo**: 10 pontos base
- **Bônus por tamanho**: +2 pontos por cada segmento da cobra
- **Fórmula**: `Pontos = 10 + (tamanho_cobra × 2)`

**Exemplo:**
- Cobra com 5 segmentos: 10 + (5 × 2) = **20 pontos**
- Cobra com 15 segmentos: 10 + (15 × 2) = **40 pontos**

## Tecnologias Utilizadas

- **Linguagem**: Java 8+
- **GUI**: Java Swing (`JFrame`, `JPanel`)
- **Gráficos**: Java AWT (`Graphics2D`, `RenderingHints`)
- **Estruturas de Dados**: `ArrayList`, `List`

### Estrutura de Classes:

**App.java** - Classe principal do aplicativo
- Gerencia o `JFrame` principal
- Implementa `Menu.MenuCallback` para navegação
- Controla transição entre menu e jogo
- Métodos: `showMenu()`, `startGame()`, `returnToMenu()`

**Menu.java** - Tela de menu principal
- Herda de `JPanel`
- Interface `MenuCallback` para comunicação com App
- Renderização com antialiasing
- Navegação por teclado (↑↓ + ENTER)
- Elementos visuais: título, subtítulo, opções, cobras decorativas

**SnakeGame.java** - Lógica principal do jogo
- **Inner Classes:**
  - `Tile`: Representa posições no grid (x, y)
  - `NumberedFood`: Frutas com número, posição e cor
  
- **Atributos Principais:**
  - `boardWidth/Height`: Dimensões da tela
  - `hudHeight`: Altura do painel de informações
  - `snakeHead`, `snakeBody`: Estrutura da cobra
  - `foods`: Lista de frutas na tela
  - `targetNumber`, `currentProduct`: Lógica matemática
  - `score`, `lives`: Estado do jogo
  - `velocityX/Y`: Direção do movimento
  
- **Métodos Principais:**
  - `generateNewTarget()`: Gera novo número alvo
  - `placeFoods()`: Posiciona frutas inteligentemente
  - `getFactors()`: Calcula fatores de um número
  - `placeFood()`: Posiciona fruta individual (com verificações)
  - `move()`: Lógica de movimento e colisões
  - `draw()`: Renderização de todos os elementos
  - `drawHUD()`: Desenha painel de informações
  - `playBackgroundMusic()`: Gerencia reprodução de áudio
  - `restartGame()`: Reseta o jogo
---

# Instituto Federal do Piauí - Campus Corrente 2026.1