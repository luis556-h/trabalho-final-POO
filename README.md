# Trabalho final de Programação Orientada a Objetos  
- Professor: Rene Douglas Nobre de Morais  
- Aluno: Luis Henrique de Carvalho Honorio  

# Math Snake

Um jogo educativo da cobrinha (Snake) com equações matemáticas. Desenvolvido em Java com Swing/AWT como trabalho final de Programação Orientada a Objetos.

## Conceito do Jogo

Ao invés de simplesmente comer frutas, você precisa resolver uma **equação com valor faltante**. O objetivo é comer o número que completa a equação.

### Como Funciona:

1. **Equação no HUD**: aparece algo como `? + 7 = 12` ou `9 ÷ ? = 3`.
2. **Número faltante**: o `?` é o valor que você deve comer.
3. **Frutas numeradas**: várias frutas aparecem com números diferentes.
4. **Objetivo**: coma a fruta cujo número completa a equação.

## Lógica do Jogo

### ✅ Acerto
Quando você come o número correto:
- ✅ Você ganha **10 pontos + (tamanho da cobra × 2)**
- ✅ A cobra cresce em 1 segmento
- ✅ Flash verde suave no cenário
- ✅ Uma nova equação é gerada
- ✅ Novas frutas são posicionadas

### ❌ Erro (Penalidade)
Quando você come um número incorreto:
- ❌ Perde 1 vida (de 3 vidas totais)
- ❌ A cobra diminui em 2 segmentos
- ❌ Flash vermelho suave no cenário
- ❌ Nova equação e novas frutas são geradas

### Game Over
O jogo termina quando:
1. Suas vidas chegam a 0
2. A cobra colide consigo mesma

## Como Jogar

### Controles

**Menu Principal:**
- **↑ ↓**: Navegar entre opções
- **ENTER**: Selecionar opção

**Durante o Jogo:**
- **↑ ↓ ← →** ou **W A S D**: Controlar direção da cobra
- *Observação: Não pode fazer 180° (virar para trás diretamente)*
- **ENTER**: Pausar/retomar

**No Pause:**
- **R**: Reiniciar o jogo
- **ESC**: Voltar ao menu

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

## Sistema de Pontuação

- **Acertar o número**: 10 pontos base
- **Bônus por tamanho**: +2 pontos por cada segmento da cobra
- **Fórmula**: `Pontos = 10 + (tamanho_cobra × 2)`

### Progressão de Nível
- A meta de pontos aumenta conforme o tamanho da cobra e o nível.
- Nos níveis 1 a 3, os operadores são **+** e **-**.
- A partir do nível 4, entram **x** e **÷**.
- A velocidade do jogo aumenta levemente após os níveis iniciais.

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
  - `targetNumber`, `missingValue`: Lógica matemática da equação
  - `score`, `lives`: Estado do jogo
  - `velocityX/Y`: Direção do movimento
  
- **Métodos Principais:**
  - `generateNewTarget()`: Gera nova equação com valor faltante
  - `placeFoods()`: Posiciona frutas com números no grid
  - `placeFood()`: Posiciona fruta individual (com verificações)
  - `move()`: Lógica de movimento e colisões
  - `draw()`: Renderização de todos os elementos
  - `drawHUD()`: Desenha painel de informações
  - `playBackgroundMusic()`: Gerencia reprodução de áudio
  - `restartGame()`: Reseta o jogo
---

# Instituto Federal do Piauí - Campus Corrente 2026.1