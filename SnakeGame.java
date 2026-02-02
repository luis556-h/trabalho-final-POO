import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    // Inner classes for Game Objects
    private class Tile {
        int x;
        int y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }  
    
    private class NumberedFood {
        Tile position;
        int number;
        Color color;
        
        NumberedFood(int x, int y, int number, Color color) {
            this.position = new Tile(x, y);
            this.number = number;
            this.color = color;
        }
    }

    // UI Components
    private ScorePanel scorePanel;
    private BoardPanel boardPanel;
    private JPanel overlayPanel;
    private JLabel overlayTitle;
    private JLabel overlaySubtitle;
    private JPanel overlayButtons;

    // Game Constants
    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    int hudHeight = 100;
    int playableHeight;
    
    // Snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    // Game Logic Variables
    ArrayList<NumberedFood> foods;
    Random random;

    int targetNumber; 
    int score;             
    int lives;             
    int level;             
    int targetScore;       
    String currentOperator; 
    int operandA;           
    int operandB;           
    int missingValue;       
    boolean missingLeft;    
    boolean missingResult;  
    
    // Animation/State Flags
    boolean levelUpFlash = false; 
    int levelUpFlashCounter = 0;   
    
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;
    boolean paused = false;
    boolean penaltyFlash = false;  
    boolean victoryFlash = false;  
    int flashCounter = 0;           
    
    private App app; 
    private Clip audioClip;

    // -------------------------------------------------------------------------
    // SCORE PANEL (North)
    // -------------------------------------------------------------------------
    private class ScorePanel extends JPanel {
        private JLabel levelLabel;
        private JLabel livesLabel;
        private JLabel scoreLabel;
        private JLabel equationLabel;
        private JLabel operatorsLabel;

        public ScorePanel() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(boardWidth, hudHeight));
            setBackground(new Color(15, 15, 25));
            setBorder(new EmptyBorder(5, 10, 5, 10));

            // Top Row: Level, Lives, Score
            JPanel topPanel = new JPanel(new GridLayout(1, 3));
            topPanel.setOpaque(false);
            
            levelLabel = createLabel("NÍVEL: 1", new Color(255, 200, 0), 18);
            livesLabel = createLabel("❤ 3", new Color(255, 0, 255), 18);
            livesLabel.setHorizontalAlignment(SwingConstants.CENTER);
            scoreLabel = createLabel("PONTOS: 0/0", new Color(0, 255, 100), 18);
            scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            topPanel.add(levelLabel);
            topPanel.add(livesLabel);
            topPanel.add(scoreLabel);

            // Middle Row: Equation
            JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            centerPanel.setOpaque(false);
            equationLabel = createLabel("", new Color(0, 255, 255), 30);
            centerPanel.add(equationLabel);

            // Bottom Row: Operators
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            bottomPanel.setOpaque(false);
            operatorsLabel = createLabel("", new Color(150, 150, 150), 14);
            bottomPanel.add(operatorsLabel);

            add(topPanel, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private JLabel createLabel(String text, Color color, int size) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("Monospaced", Font.BOLD, size));
            lbl.setForeground(color);
            return lbl;
        }

        public void updateStats() {
            levelLabel.setText("NÍVEL: " + level);
            livesLabel.setText("❤ " + lives);
            scoreLabel.setText("PONTOS: " + score + "/" + targetScore);
            equationLabel.setText(buildEquationHint());
            operatorsLabel.setText(getAvailableOperators());
        }
    }

    // -------------------------------------------------------------------------
    // BOARD PANEL (Center - Game Rendering)
    // -------------------------------------------------------------------------
    private class BoardPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Shift coordinate system so logic (which assumes y starts at hudHeight)
            // maps correctly to this panel (where 0,0 is top-left).
            g2d.translate(0, -hudHeight);

            // Antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            drawGameContent(g2d);
        }
    }

    public SnakeGame(int boardWidth, int boardHeight, App app) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.playableHeight = boardHeight - hudHeight; 
        this.app = app;
        
        // Setup Main Layout
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(new Color(20, 20, 30));
        setLayout(new BorderLayout());
        setFocusable(true);
        addKeyListener(this);

        // -- Score Panel --
        // Initial setup variables before creating panel
        foods = new ArrayList<NumberedFood>();
        random = new Random();
        snakeHead = new Tile(5, (int) Math.ceil((double) hudHeight / tileSize));
        snakeBody = new ArrayList<Tile>();
        level = 1;
        currentOperator = "+";
        lives = 3;
        targetScore = computeNextTargetScore();
        generateNewTarget(); // Need this for equation label init
        
        scorePanel = new ScorePanel();
        add(scorePanel, BorderLayout.NORTH);

        // -- Layered Pane for Game + Overlay --
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(boardWidth, playableHeight));
        add(layeredPane, BorderLayout.CENTER);

        // 1. Board Panel (Game View)
        boardPanel = new BoardPanel();
        boardPanel.setBackground(new Color(20, 20, 30));
        boardPanel.setBounds(0, 0, boardWidth, playableHeight);
        layeredPane.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        // 2. Overlay Panel (Notifications)
        createOverlayPanel();
        overlayPanel.setBounds(0, 0, boardWidth, playableHeight);
        layeredPane.add(overlayPanel, JLayeredPane.PALETTE_LAYER);

        // Logic Init
        placeFoods();
        velocityX = 1;
        velocityY = 0;
        
        playBackgroundMusic("sdtrack.wav");
        
        gameLoop = new Timer(getGameSpeed(), this);
        gameLoop.start();
        
        // Initial label update
        scorePanel.updateStats();
    }

    private void createOverlayPanel() {
        overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayPanel.setLayout(new BoxLayout(overlayPanel, BoxLayout.Y_AXIS));
        overlayPanel.setOpaque(false);
        overlayPanel.setBackground(new Color(0, 0, 0, 60)); // More transparent overlay
        overlayPanel.setVisible(false);

        overlayPanel.add(Box.createVerticalStrut(40));

        // Boxed content to highlight texts
        JPanel contentBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        contentBox.setOpaque(false);
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        overlayTitle = new JLabel("PAUSADO");
        overlayTitle.setFont(new Font("Monospaced", Font.BOLD, 48));
        overlayTitle.setForeground(Color.WHITE);
        overlayTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentBox.add(overlayTitle);

        contentBox.add(Box.createVerticalStrut(15));

        overlaySubtitle = new JLabel("");
        overlaySubtitle.setFont(new Font("Monospaced", Font.BOLD, 18));
        overlaySubtitle.setForeground(Color.WHITE);
        overlaySubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentBox.add(overlaySubtitle);

        contentBox.add(Box.createVerticalStrut(20));

        // Instructions/Buttons Panel
        overlayButtons = new JPanel();
        overlayButtons.setLayout(new BoxLayout(overlayButtons, BoxLayout.Y_AXIS));
        overlayButtons.setOpaque(false);
        
        JLabel lblContinue = new JLabel("Pressione ENTER para continuar"); // Only for pause
        lblContinue.setFont(new Font("Monospaced", Font.PLAIN, 16));
        lblContinue.setForeground(Color.LIGHT_GRAY);
        lblContinue.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblRestart = new JLabel("Pressione R para reiniciar");
        lblRestart.setFont(new Font("Monospaced", Font.PLAIN, 16));
        lblRestart.setForeground(Color.LIGHT_GRAY);
        lblRestart.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblMenu = new JLabel("Pressione ESC para Menu");
        lblMenu.setFont(new Font("Monospaced", Font.PLAIN, 16));
        lblMenu.setForeground(Color.LIGHT_GRAY);
        lblMenu.setAlignmentX(Component.CENTER_ALIGNMENT);

        overlayButtons.add(lblContinue); 
        overlayButtons.add(Box.createVerticalStrut(10));
        overlayButtons.add(lblRestart);
        overlayButtons.add(Box.createVerticalStrut(10));
        overlayButtons.add(lblMenu);

        contentBox.add(overlayButtons);
        overlayPanel.add(contentBox);
        overlayPanel.add(Box.createVerticalGlue());
    }

    private void showGameOver() {
        overlayTitle.setText("GAME OVER!");
        overlayTitle.setForeground(new Color(255, 50, 50));
        overlaySubtitle.setText("Pontuação Final: " + score);
        
        // Update instruction text
        Component[] comps = overlayButtons.getComponents();
        if (comps.length > 0 && comps[0] instanceof JLabel) {
             ((JLabel)comps[0]).setText(""); // Clear "Enter to continue"
        }

        overlayPanel.setVisible(true);
        overlayPanel.repaint();
    }

    private void showPause() {
        overlayTitle.setText("PAUSADO");
        overlayTitle.setForeground(new Color(255, 255, 100));
        overlaySubtitle.setText("");

        Component[] comps = overlayButtons.getComponents();
        if (comps.length > 0 && comps[0] instanceof JLabel) {
             ((JLabel)comps[0]).setText("Pressione ENTER para continuar");
        }

        overlayPanel.setVisible(true);
        overlayPanel.repaint();
    }

    private void hideOverlay() {
        overlayPanel.setVisible(false);
        // Ensure focus remains on the main panel for KeyListener
        this.requestFocusInWindow();
    }

    // This method replaces the old draw() method but only renders game elements
    private void drawGameContent(Graphics2D g2d) {
        // Efeito de flash vermelho com fade-out suave
        if (penaltyFlash && flashCounter > 0) {
            int alpha = (int) (flashCounter * 4); 
            if (alpha > 40) alpha = 40;
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(200, 0, 0, alpha));
            // Coordinates relative to translated origin (0,0 is hudHeight)
            // Wait, we translated g2d by -hudHeight.
            // Screen (0,0) is now (0, -hudHeight).
            // Logic coordinate (0,0) is Top-Left of logic board (which includes HUD area at top).
            // HUD area is 0..100. Play area is 100..600.
            // BoardPanel starts at 100.
            // g2d.translate(0, -100).
            // If I drawRect(0, 0, W, H) in logic coords...
            // It draws at -100.
            // Correct.
            g2d.fillRect(0, 0, boardWidth, boardHeight);
        }
        
        // Efeito de flash verde com fade-out suave
        if (victoryFlash && flashCounter > 0) {
            int alpha = (int) (flashCounter * 5); 
            if (alpha > 50) alpha = 50;
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(0, 180, 0, alpha));
            g2d.fillRect(0, 0, boardWidth, boardHeight);
        }
        
        // Flash Level Up
        if (levelUpFlash && levelUpFlashCounter > 0) {
            int alpha = (int) (levelUpFlashCounter * 3);
            if (alpha > 90) alpha = 90;
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(0, 255, 0, alpha));
            g2d.fillRect(0, 0, boardWidth, boardHeight);
            
            if (levelUpFlashCounter > 15) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
                String levelUpText = "NÍVEL " + level + "!";
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (boardWidth - fm.stringWidth(levelUpText)) / 2;
                int textY = boardHeight / 2;
                g2d.drawString(levelUpText, textX, textY);
            }
        }
        
        // Grid Lines - apenas na área jogável
        g2d.setColor(new Color(40, 40, 50));
        g2d.setStroke(new BasicStroke(1));
        // Logic grid assumes full board height.
        // We draw starting from hudHeight/tileSize
        for(int i = 0; i < boardWidth/tileSize; i++) {
            g2d.drawLine(i*tileSize, hudHeight, i*tileSize, boardHeight);
        }
        for(int i = hudHeight/tileSize; i < boardHeight/tileSize; i++) {
            g2d.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
        }

        // Desenhar frutas
        for (NumberedFood food : foods) {
            g2d.setColor(food.color);
            g2d.fillOval(food.position.x * tileSize + 2, food.position.y * tileSize + 2, 
                        tileSize - 4, tileSize - 4);
            
            g2d.setColor(food.color.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(food.position.x * tileSize + 2, food.position.y * tileSize + 2, 
                        tileSize - 4, tileSize - 4);
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String numStr = String.valueOf(food.number);
            int textWidth = fm.stringWidth(numStr);
            int textHeight = fm.getAscent();
            
            int textX = food.position.x * tileSize + (tileSize - textWidth) / 2;
            int textY = food.position.y * tileSize + (tileSize + textHeight) / 2 - 2;
            
            g2d.drawString(numStr, textX, textY);
        }

        // Snake Head
        g2d.setColor(new Color(0, 255, 100));
        g2d.fillRoundRect(snakeHead.x*tileSize + 1, snakeHead.y*tileSize + 1, 
                         tileSize - 2, tileSize - 2, 8, 8);
        
        // Borda da cabeça
        g2d.setColor(new Color(0, 200, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(snakeHead.x*tileSize + 1, snakeHead.y*tileSize + 1, 
                         tileSize - 2, tileSize - 2, 8, 8);
        
        // Snake Body
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            int alpha = 255 - (i * 5); 
            if (alpha < 100) alpha = 100;
            
            g2d.setColor(new Color(0, 200, 80, alpha));
            g2d.fillRoundRect(snakePart.x*tileSize + 1, snakePart.y*tileSize + 1, 
                             tileSize - 2, tileSize - 2, 6, 6);
		}
    }
    
    // We removed drawHUD() because ScorePanel handles it.

    // -------------------------------------------------------------------------
    // Keeping original logic methods, only updating paintComponent related calls
    // -------------------------------------------------------------------------
    
    /**
     * Reproduz música de fundo em loop
     */
    private void playBackgroundMusic(String filename) {
        try {
            File audioFile = new File(filename);
            if (!audioFile.exists()) {
                System.err.println("Arquivo de áudio não encontrado: " + filename);
                return;
            }
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            audioClip = AudioSystem.getClip();
            audioClip.open(audioStream);
            audioClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop infinito
            audioClip.start();
            
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Formato de áudio não suportado. Use WAV em vez de MPEG.");
            System.err.println("Para usar MP3/MPEG, converta para WAV ou use JavaFX MediaPlayer.");
        } catch (IOException | LineUnavailableException e) {
            System.err.println("Erro ao carregar áudio: " + e.getMessage());
        }
    }
    
    /**
     * Para a reprodução da música
     */
    public void stopBackgroundMusic() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        // Delegate to super is fine, but we use subcomponents now.
        // We don't want SnakeGame to draw anything itself except background if holes.
        super.paintComponent(g);
        // The children (ScorePanel, LayeredPane -> BoardPanel) will paint themselves
    }


    private String buildEquationHint() {
        String left = missingLeft ? "?" : String.valueOf(operandA);
        String right = (!missingLeft && !missingResult) ? "?" : String.valueOf(operandB);
        String result = missingResult ? "?" : String.valueOf(targetNumber);
        return left + " " + currentOperator + " " + right + " = " + result;
    }
    
    private String getAvailableOperators() {
        if (level <= 3) {
            return "Operadores disponíveis: + e -";
        } else {
            return "Operadores disponíveis: +, -, x e ÷";
        }
    }
    
    private int getGameSpeed() {
        if (level <= 3) {
            return 150; // Mais lento nos primeiros 3 níveis
        }
        return 120; // Aumenta um pouco após os níveis iniciais
    }

    /**
     * Gera uma nova equação com número faltante baseado no nível
     */
    public void generateNewTarget() {
        String[] ops = (level <= 3) ? new String[]{"+", "-"} : new String[]{"+", "-", "x", "÷"};
        currentOperator = ops[random.nextInt(ops.length)];

        int maxSmall = 10;
        int maxMedium = 20;

        // 0 = falta esquerda, 1 = falta direita, 2 = falta resultado
        int missingType = random.nextInt(3);
        missingLeft = (missingType == 0);
        missingResult = (missingType == 2);

        switch (currentOperator) {
            case "+":
                operandA = 1 + random.nextInt(maxMedium);
                operandB = 1 + random.nextInt(maxMedium);
                targetNumber = operandA + operandB;

                if (missingResult) {
                    missingValue = targetNumber;
                } else if (missingLeft) {
                    missingValue = operandA;
                } else {
                    missingValue = operandB;
                }
                break;
            case "-":
                // Garantir resultado não-negativo
                operandA = 1 + random.nextInt(maxMedium);
                operandB = 1 + random.nextInt(maxMedium);
                if (operandB > operandA) {
                    int temp = operandA;
                    operandA = operandB;
                    operandB = temp;
                }
                targetNumber = operandA - operandB;

                if (missingResult) {
                    missingValue = targetNumber;
                } else if (missingLeft) {
                    missingValue = operandA;
                } else {
                    missingValue = operandB;
                }
                break;
            case "x":
                operandA = 1 + random.nextInt(maxSmall);
                operandB = 1 + random.nextInt(maxSmall);
                targetNumber = operandA * operandB;

                if (missingResult) {
                    missingValue = targetNumber;
                } else if (missingLeft) {
                    missingValue = operandA;
                } else {
                    missingValue = operandB;
                }
                break;
            case "÷":
                // Criar divisão exata
                int divisor = 1 + random.nextInt(maxSmall);
                int result = 1 + random.nextInt(maxSmall);
                int dividend = divisor * result;
                operandA = dividend;
                operandB = divisor;
                targetNumber = result;

                if (missingResult) {
                    missingValue = targetNumber;
                } else if (missingLeft) {
                    missingValue = operandA;
                } else {
                    missingValue = operandB;
                }
                break;
        }
    }
    
    /**
     * Coloca múltiplas frutas na tela com números apropriados para o operador atual
     */
    public void placeFoods() {
        foods.clear();
        
        int numberOfFoods = 6 + random.nextInt(4); // 6 a 9 frutas
        
        // Obter números necessários baseado no operador
        List<Integer> necessaryNumbers = getNecessaryNumbers();
        
        // Garantir que pelo menos 2-3 números necessários apareçam
        int numbersToPlace = Math.min(3, necessaryNumbers.size());
        for (int i = 0; i < numbersToPlace; i++) {
            if (!necessaryNumbers.isEmpty()) {
                int number = necessaryNumbers.get(random.nextInt(necessaryNumbers.size()));
                placeFood(number, true);
            }
        }
        
        // Preencher com números aleatórios (distratores)
        while (foods.size() < numberOfFoods) {
            int randomNum = getRandomNumberForOperator();
            placeFood(randomNum, false);
        }
    }
    
    private List<Integer> getNecessaryNumbers() {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(missingValue);
        return numbers;
    }
    
    private int getRandomNumberForOperator() {
        return 1 + random.nextInt(20); // 1 a 20
    }
    
    /**
     * Coloca uma fruta individual com um número específico
     */
    private void placeFood(int number, boolean isNecessary) {
        int x, y;
        boolean validPosition = false;
        int attempts = 0;
        
        // Garantir que frutas apareçam apenas em tiles completamente visíveis
        int minY = (int) Math.ceil((double) hudHeight / tileSize); // Primeiro tile completamente abaixo do HUD
        int maxX = boardWidth / tileSize; // Número máximo de tiles horizontais
        int maxY = boardHeight / tileSize; // Número máximo de tiles verticais
        
        do {
            x = random.nextInt(maxX);
            y = minY + random.nextInt(maxY - minY); // Apenas na área jogável
            
            validPosition = true;
            
            // Verificar se não colide com a cobra
            if (x == snakeHead.x && y == snakeHead.y) {
                validPosition = false;
            }
            
            for (Tile part : snakeBody) {
                if (x == part.x && y == part.y) {
                    validPosition = false;
                    break;
                }
            }
            
            // Verificar se não colide com outras frutas
            for (NumberedFood food : foods) {
                if (x == food.position.x && y == food.position.y) {
                    validPosition = false;
                    break;
                }
            }
            
            // Verificar se não está na trajetória imediata da cobra (próximas 3 posições)
            if (validPosition) {
                for (int i = 1; i <= 3; i++) {
                    int checkX = snakeHead.x + (velocityX * i);
                    int checkY = snakeHead.y + (velocityY * i);
                    
                    // Aplicar wrap-around para verificação
                    if (checkX < 0) checkX = (boardWidth / tileSize) - 1;
                    else if (checkX >= boardWidth / tileSize) checkX = 0;
                    
                    if (checkY < minY) checkY = maxY - 1;
                    else if (checkY >= maxY) checkY = minY;
                    
                    if (x == checkX && y == checkY) {
                        validPosition = false;
                        break;
                    }
                }
            }
            
            attempts++;
        } while (!validPosition && attempts < 50);
        
        // Cores aleatórias alternando entre amarelo, vermelho, azul e roxo
        Color[] colors = {
            new Color(255, 50, 50),    // Vermelho
            new Color(255, 200, 50),   // Amarelo
            new Color(50, 150, 255),   // Azul
            new Color(180, 80, 255),   // Roxo
        };
        Color fruitColor = colors[random.nextInt(colors.length)];
        
        foods.add(new NumberedFood(x, y, number, fruitColor));
    }

    public void move() {
        // Verificar colisão com frutas
        NumberedFood eatenFood = null;
        for (NumberedFood food : foods) {
            if (collision(snakeHead, food.position)) {
                eatenFood = food;
                break;
            }
        }
        
        if (eatenFood != null) {
            boolean isCorrect = eatenFood.number == missingValue;

            if (isCorrect) {
                // VITÓRIA! Acertou o número faltante
                score += 10 + (snakeBody.size() * 2);

                // Cobra cresce somente ao acertar
                addSnakeSegment(eatenFood.position.x, eatenFood.position.y);

                // Verificar mudança de nível
                if (score >= targetScore) {
                    levelUp();
                }

                // Gerar nova equação
                generateNewTarget();
                placeFoods();

                // Efeito visual de sucesso
                victoryFlash = true;
                penaltyFlash = false;
                flashCounter = 10;
            } else {
                // PENALIDADE! Número incorreto
                lives--;

                // Diminuir cobra (remover últimos 2 segmentos)
                if (snakeBody.size() > 0) {
                    snakeBody.remove(snakeBody.size() - 1);
                }
                if (snakeBody.size() > 0) {
                    snakeBody.remove(snakeBody.size() - 1);
                }

                // Efeito visual de penalidade
                penaltyFlash = true;
                flashCounter = 10;

                // Regenerar equação e frutas
                generateNewTarget();
                placeFoods();

                // Game over se ficar sem vidas
                if (lives <= 0) {
                    gameOver = true;
                }
            }

            // Remover a fruta comida
            foods.remove(eatenFood);
        }

        // Mover corpo da cobra
        for (int i = snakeBody.size()-1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i-1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }
        
        // Mover cabeça da cobra
        snakeHead.x += velocityX;
        snakeHead.y += velocityY;
        
        // Wrap-around nas bordas (loop)
        int minY = (int) Math.ceil((double) hudHeight / tileSize); // Primeiro tile completamente visível
        int maxY = boardHeight / tileSize;
        
        if (snakeHead.x < 0) {
            snakeHead.x = (boardWidth / tileSize) - 1;
        } else if (snakeHead.x >= boardWidth / tileSize) {
            snakeHead.x = 0;
        }
        
        if (snakeHead.y < minY) {
            snakeHead.y = maxY - 1;
        } else if (snakeHead.y >= maxY) {
            snakeHead.y = minY;
        }

        // Condições de game over - apenas colisão com o próprio corpo
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }
        
        // Decrementar contador de flash
        if (flashCounter > 0) {
            flashCounter--;
            if (flashCounter == 0) {
                penaltyFlash = false;
                victoryFlash = false;
            }
        }
        
        // Decrementar contador de flash de level up
        if (levelUpFlashCounter > 0) {
            levelUpFlashCounter--;
            if (levelUpFlashCounter == 0) {
                levelUpFlash = false;
            }
        }
    }
    
    private void levelUp() {
        level++;
        targetScore = computeNextTargetScore();
        
        // Atualizar velocidade do jogo
        gameLoop.setDelay(getGameSpeed());
        
        // Efeito visual de mudança de nível - flash verde prolongado
        levelUpFlash = true;
        levelUpFlashCounter = 30; // Flash mais longo que vitória normal

        // Crescimento bônus ao passar de nível
        addSnakeSegment(snakeHead.x, snakeHead.y);
    }

    private void addSnakeSegment(int x, int y) {
        snakeBody.add(new Tile(x, y));
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    private int computeNextTargetScore() {
        int pointsPerHit = 10 + (snakeBody.size() * 2);
        int hitsRequired = 3 + (level / 2);
        return score + (pointsPerHit * hitsRequired);
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        if (paused) {
            return;
        }
        
        move();
        boardPanel.repaint();
        scorePanel.updateStats();

        if (gameOver) {
            gameLoop.stop();
            showGameOver();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        // Enter para pausar/retomar durante a gameplay
        if (e.getKeyCode() == KeyEvent.VK_ENTER && !gameOver) {
            togglePause();
            return;
        }

        // Opções enquanto pausado
        if (paused) {
            if (e.getKeyCode() == KeyEvent.VK_R) {
                restartGame();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (app != null) {
                    stopBackgroundMusic();
                    app.returnToMenu();
                }
            }
            return;
        }

        // Controles de movimento
        if ((e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
        
        // Tecla R para reiniciar o jogo
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
        
        // Tecla ESC para voltar ao menu
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && gameOver) {
            if (app != null) {
                stopBackgroundMusic();
                app.returnToMenu();
            }
        }
    }
    
    /**
     * Reinicia o jogo completamente
     */
    private void restartGame() {
        // Resetar cobra
        snakeHead = new Tile(5, (int) Math.ceil((double) hudHeight / tileSize));
        snakeBody.clear();
        
        // Resetar variáveis matemáticas
        level = 1;
        currentOperator = "+";
        score = 0;
        lives = 3;
        targetScore = computeNextTargetScore();
        
        // Resetar movimento
        velocityX = 1;
        velocityY = 0;
        
        // Resetar flags
        gameOver = false;
        paused = false;
        penaltyFlash = false;
        victoryFlash = false;
        levelUpFlash = false;
        flashCounter = 0;
        levelUpFlashCounter = 0;
        
        // Resetar UI
        hideOverlay();
        scorePanel.updateStats();
        
        // Gerar novo alvo e frutas
        generateNewTarget();
        placeFoods();
        
        // Reiniciar timer com velocidade inicial
        gameLoop.setDelay(getGameSpeed());
        gameLoop.start();
        boardPanel.repaint();
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    private void togglePause() {
        paused = !paused;
        if (paused) {
            gameLoop.stop();
            showPause();
        } else {
            hideOverlay();
            gameLoop.start();
        }
    }
}
