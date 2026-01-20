import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
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

    int boardWidth;
    int boardHeight;
    int tileSize = 25;
    int hudHeight = 100;  // Altura do HUD em pixels
    int playableHeight;  // Altura jogável (excluindo HUD)
    
    //snake
    Tile snakeHead;
    ArrayList<Tile> snakeBody;

    //math snake - frutas com números
    ArrayList<NumberedFood> foods;
    Random random;

    //math game logic
    int targetNumber;      // Número alvo a ser alcançado
    int currentProduct;    // Produto atual (começa em 1)
    int score;             // Pontuação do jogador
    int lives;             // Vidas do jogador
    
    //game logic
    int velocityX;
    int velocityY;
    Timer gameLoop;

    boolean gameOver = false;
    boolean penaltyFlash = false;  // Para efeito visual de penalidade
    boolean victoryFlash = false;  // Para efeito visual de vitória
    int flashCounter = 0;           // Contador para animação do flash
    
    private App app; // Referência ao App para retornar ao menu
    private Clip audioClip; // Clip de áudio para a soundtrack

    SnakeGame(int boardWidth, int boardHeight, App app) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.playableHeight = boardHeight - hudHeight; // Área jogável abaixo do HUD
        this.app = app;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(new Color(20, 20, 30)); // Dark Mode background
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, (int) Math.ceil((double) hudHeight / tileSize));
        snakeBody = new ArrayList<Tile>();

        foods = new ArrayList<NumberedFood>();
        random = new Random();
        
        // Inicializar variáveis matemáticas
        currentProduct = 1;
        score = 0;
        lives = 3;
        generateNewTarget();
        placeFoods();

        velocityX = 1;
        velocityY = 0;
        
        // Iniciar música de fundo
        playBackgroundMusic("sdtrack.wav");
        
		//game timer
		gameLoop = new Timer(100, this); //how long it takes to start timer, milliseconds gone between frames 
        gameLoop.start();
	}	
    
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
    
    public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Ativar antialiasing para gráficos suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Efeito de flash vermelho com fade-out suave quando houver penalidade
        if (penaltyFlash && flashCounter > 0) {
            int alpha = (int) (flashCounter * 4); // 10 * 4 = 40 alpha máximo
            if (alpha > 40) alpha = 40;
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(200, 0, 0, alpha));
            g2d.fillRect(0, 0, boardWidth, boardHeight);
        }
        
        // Efeito de flash verde com fade-out suave quando acertar o alvo
        if (victoryFlash && flashCounter > 0) {
            int alpha = (int) (flashCounter * 5); // 10 * 5 = 50 alpha máximo
            if (alpha > 50) alpha = 50;
            if (alpha < 0) alpha = 0;
            g2d.setColor(new Color(0, 180, 0, alpha));
            g2d.fillRect(0, 0, boardWidth, boardHeight);
        }
        
        // Linha separadora entre HUD e área jogável
        g2d.setColor(new Color(0, 255, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, hudHeight, boardWidth, hudHeight);
        
        // Grid Lines (opcional, mais suave) - apenas na área jogável
        g2d.setColor(new Color(40, 40, 50));
        g2d.setStroke(new BasicStroke(1));
        for(int i = 0; i < boardWidth/tileSize; i++) {
            g2d.drawLine(i*tileSize, hudHeight, i*tileSize, boardHeight);
        }
        for(int i = hudHeight/tileSize; i < boardHeight/tileSize; i++) {
            g2d.drawLine(0, i*tileSize, boardWidth, i*tileSize); 
        }

        // Desenhar todas as frutas com números
        for (NumberedFood food : foods) {
            // Círculo da fruta com cores neon
            g2d.setColor(food.color);
            g2d.fillOval(food.position.x * tileSize + 2, food.position.y * tileSize + 2, 
                        tileSize - 4, tileSize - 4);
            
            // Borda mais escura
            g2d.setColor(food.color.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(food.position.x * tileSize + 2, food.position.y * tileSize + 2, 
                        tileSize - 4, tileSize - 4);
            
            // Desenhar o número centralizado
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

        // Snake Head (cor neon verde)
        g2d.setColor(new Color(0, 255, 100));
        g2d.fillRoundRect(snakeHead.x*tileSize + 1, snakeHead.y*tileSize + 1, 
                         tileSize - 2, tileSize - 2, 8, 8);
        
        // Borda da cabeça
        g2d.setColor(new Color(0, 200, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(snakeHead.x*tileSize + 1, snakeHead.y*tileSize + 1, 
                         tileSize - 2, tileSize - 2, 8, 8);
        
        // Snake Body (gradiente de verde)
        for (int i = 0; i < snakeBody.size(); i++) {
            Tile snakePart = snakeBody.get(i);
            int alpha = 255 - (i * 5); // Fade gradual
            if (alpha < 100) alpha = 100;
            
            g2d.setColor(new Color(0, 200, 80, alpha));
            g2d.fillRoundRect(snakePart.x*tileSize + 1, snakePart.y*tileSize + 1, 
                             tileSize - 2, tileSize - 2, 6, 6);
		}

        // HUD - Informações do Jogo
        drawHUD(g2d);
	}
    
    private void drawHUD(Graphics2D g2d) {
        int hudY = 20;
        
        // Fundo sólido para o HUD (área fixa, não jogável)
        g2d.setColor(new Color(15, 15, 25));
        g2d.fillRect(0, 0, boardWidth, hudHeight);
        
        // Borda decorativa
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(10, 5, boardWidth - 20, 80, 15, 15);
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        
        if (gameOver) {
            g2d.setColor(new Color(255, 50, 50));
            g2d.drawString("GAME OVER!", 20, hudY);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Pontuação Final: " + score, 20, hudY + 25);
            g2d.drawString("Pressione R para reiniciar", 20, hudY + 50);
            g2d.drawString("Pressione ESC para voltar ao menu", 20, hudY + 70);
        } else {
            // Alvo em amarelo neon
            g2d.setColor(new Color(255, 255, 0));
            g2d.drawString("ALVO: " + targetNumber, 20, hudY);
            
            // Produto atual em ciano neon
            g2d.setColor(new Color(0, 255, 255));
            g2d.drawString("ATUAL: " + currentProduct, 200, hudY);
            
            // Pontuação em verde neon
            g2d.setColor(new Color(0, 255, 100));
            g2d.drawString("PONTOS: " + score, 380, hudY);
            
            // Vidas em magenta
            g2d.setColor(new Color(255, 0, 255));
            g2d.drawString("❤ " + lives, 20, hudY + 25);
            
            // Tamanho da cobra
            g2d.setColor(new Color(150, 150, 255));
            g2d.drawString("Tamanho: " + (snakeBody.size() + 1), 20, hudY + 50);
            
            // Dica de multiplicação
            if (currentProduct > 1) {
                g2d.setColor(new Color(200, 200, 200));
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                g2d.drawString("Próximo fator ideal: " + findNextFactor(), 200, hudY + 50);
            }
        }
    }
    
    private String findNextFactor() {
        if (currentProduct >= targetNumber) return "RESET";
        
        // Encontrar próximo fator que divida o alvo pelo produto atual
        int remaining = targetNumber / currentProduct;
        
        for (int i = 2; i <= 9; i++) {
            if (remaining % i == 0) {
                return String.valueOf(i);
            }
        }
        return "?";
    }

    /**
     * Gera um novo número alvo baseado em multiplicação de fatores
     * Garante que o alvo seja alcançável com números de 2 a 9
     */
    public void generateNewTarget() {
        // Gerar alvos interessantes (produtos de 2-3 fatores)
        int[] possibleTargets = {
            12, 15, 18, 20, 24, 28, 30, 32, 36, 40, 
            42, 45, 48, 54, 56, 60, 63, 64, 72, 80,
            81, 84, 90, 96, 100, 108, 120, 144
        };
        
        targetNumber = possibleTargets[random.nextInt(possibleTargets.length)];
    }
    
    /**
     * Coloca múltiplas frutas na tela
     * GARANTE que pelo menos alguns fatores necessários estejam presentes
     */
    public void placeFoods() {
        foods.clear();
        
        int numberOfFoods = 6 + random.nextInt(4); // 6 a 9 frutas
        
        // Obter fatores necessários para o alvo
        List<Integer> necessaryFactors = getFactors(targetNumber / currentProduct);
        
        // Garantir que pelo menos 2-3 fatores necessários apareçam
        int factorsToPlace = Math.min(3, necessaryFactors.size());
        for (int i = 0; i < factorsToPlace; i++) {
            if (!necessaryFactors.isEmpty()) {
                int factor = necessaryFactors.get(random.nextInt(necessaryFactors.size()));
                placeFood(factor, true);
            }
        }
        
        // Preencher com números aleatórios (distratores)
        while (foods.size() < numberOfFoods) {
            int randomNum = 2 + random.nextInt(8); // 2 a 9
            placeFood(randomNum, false);
        }
    }
    
    /**
     * Obtém todos os fatores de um número entre 2 e 9
     */
    private List<Integer> getFactors(int number) {
        List<Integer> factors = new ArrayList<>();
        
        for (int i = 2; i <= 9 && i <= number; i++) {
            if (number % i == 0) {
                factors.add(i);
            }
        }
        
        return factors;
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
        
        // Cores neon baseadas no número e importância
        Color fruitColor;
        if (isNecessary) {
            // Frutas necessárias em cores mais vibrantes
            fruitColor = new Color(255, 100, 255); // Magenta neon
        } else {
            // Distratores em cores variadas
            Color[] colors = {
                new Color(255, 50, 50),    // Vermelho
                new Color(255, 150, 0),    // Laranja
                new Color(50, 150, 255),   // Azul
                new Color(255, 200, 50),   // Amarelo
            };
            fruitColor = colors[random.nextInt(colors.length)];
        }
        
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
            int newProduct = currentProduct * eatenFood.number;
            
            // LÓGICA MATEMÁTICA CRÍTICA
            if (newProduct == targetNumber) {
                // VITÓRIA! Produto igual ao alvo
                currentProduct = 1;
                score += 10 + (snakeBody.size() * 2);
                
                // Cobra cresce
                snakeBody.add(new Tile(eatenFood.position.x, eatenFood.position.y));
                
                // Gerar novo alvo
                generateNewTarget();
                placeFoods();
                
                // Efeito visual de sucesso - flash verde com fade-out
                victoryFlash = true;
                penaltyFlash = false;
                flashCounter = 10;
                
            } else if (newProduct > targetNumber || targetNumber % newProduct != 0) {
                // PENALIDADE! Ultrapassou o alvo OU não é divisor do alvo
                currentProduct = 1;
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
                
                // Regenerar frutas (alvo permanece o mesmo)
                placeFoods();
                
                // Game over se ficar sem vidas
                if (lives <= 0) {
                    gameOver = true;
                }
                
            } else {
                // Produto válido e menor que o alvo
                currentProduct = newProduct;
                
                // Cobra cresce um pouco
                snakeBody.add(new Tile(eatenFood.position.x, eatenFood.position.y));
                
                // Regenerar frutas com novos fatores necessários
                placeFoods();
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
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) { //called every x milliseconds by gameLoop timer
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }  

    @Override
    public void keyPressed(KeyEvent e) {
        // Controles de movimento
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
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
        currentProduct = 1;
        score = 0;
        lives = 3;
        
        // Resetar movimento
        velocityX = 1;
        velocityY = 0;
        
        // Resetar flags
        gameOver = false;
        penaltyFlash = false;
        victoryFlash = false;
        flashCounter = 0;
        
        // Gerar novo alvo e frutas
        generateNewTarget();
        placeFoods();
        
        // Reiniciar timer
        gameLoop.start();
    }

    //not needed
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
