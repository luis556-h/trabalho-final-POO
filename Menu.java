import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Menu extends JPanel implements KeyListener {
    int boardWidth;
    int boardHeight;
    
    private int selectedOption = 0; // 0 = Jogar, 1 = Como Jogar, 2 = Sair
    private boolean showingHowToPlay = false;
    private boolean enterPressed = false;
    
    // Callback para quando o jogador selecionar uma opção
    private MenuCallback callback;
    
    public interface MenuCallback {
        void onStartGame();
        void onExit();
    }
    
    public Menu(int boardWidth, int boardHeight, MenuCallback callback) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.callback = callback;
        
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(new Color(20, 20, 30)); // Dark Mode background
        addKeyListener(this);
        setFocusable(true);
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    private void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        // Ativar antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Desenhar cobra decorativa no fundo (serpente estilizada)
        drawDecorativeSnake(g2d);

        if (showingHowToPlay) {
            drawHowToPlay(g2d);
            return;
        }
        
        // Título do jogo - "MATH SNAKE"
        g2d.setFont(new Font("Monospaced", Font.BOLD, 60));
        
        // Sombra do título
        g2d.setColor(new Color(0, 0, 0, 100));
        String title = "MATH SNAKE";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleX = (boardWidth - fmTitle.stringWidth(title)) / 2;
        int titleY = 120;
        g2d.drawString(title, titleX + 3, titleY + 3);
        
        // Título principal com gradiente simulado
        g2d.setColor(new Color(0, 255, 100));
        g2d.drawString(title, titleX, titleY);
        
        // Subtítulo
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String subtitle = "O Desafio da Multiplicação";
        FontMetrics fmSubtitle = g2d.getFontMetrics();
        int subtitleX = (boardWidth - fmSubtitle.stringWidth(subtitle)) / 2;
        g2d.setColor(new Color(0, 255, 255));
        g2d.drawString(subtitle, subtitleX, titleY + 40);
        
        // Opções do menu
        int menuStartY = 250;
        int menuSpacing = 70;
        
        String[] options = {"JOGAR", "COMO JOGAR", "SAIR"};
        
        for (int i = 0; i < options.length; i++) {
            int optionY = menuStartY + (i * menuSpacing);
            
            if (i == selectedOption) {
                // Opção selecionada - destacada com retângulo
                g2d.setColor(new Color(0, 255, 100, 50));
                g2d.fillRoundRect(boardWidth / 2 - 120, optionY - 35, 240, 50, 15, 15);
                
                // Borda da seleção
                g2d.setColor(new Color(0, 255, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(boardWidth / 2 - 120, optionY - 35, 240, 50, 15, 15);
                
                // Setas indicadoras
                g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
                g2d.drawString(">", boardWidth / 2 - 160, optionY);
                g2d.drawString("<", boardWidth / 2 + 140, optionY);
            }
            
            // Texto da opção
            g2d.setFont(new Font("Monospaced", Font.BOLD, 32));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (boardWidth - fm.stringWidth(options[i])) / 2;
            
            if (i == selectedOption) {
                g2d.setColor(new Color(255, 255, 255));
            } else {
                g2d.setColor(new Color(150, 150, 150));
            }
            
            g2d.drawString(options[i], textX, optionY);
        }

        // Instruções
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(100, 100, 120));
        String instructions = "Use ↑ ↓ para navegar | ENTER para selecionar";
        FontMetrics fmInst = g2d.getFontMetrics();
        int instX = (boardWidth - fmInst.stringWidth(instructions)) / 2;
        g2d.drawString(instructions, instX, boardHeight - 30);

        // Crédito (centralizado abaixo das dicas)
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.setColor(new Color(120, 120, 140));
        String credit = "Feito por Luiz Henrique :)";
        FontMetrics fmCredit = g2d.getFontMetrics();
        int creditX = (boardWidth - fmCredit.stringWidth(credit)) / 2;
        g2d.drawString(credit, creditX, boardHeight - 12);
    }

    private void drawHowToPlay(Graphics2D g2d) {
        // Título
        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2d.setColor(new Color(0, 255, 100));
        String title = "COMO JOGAR";
        FontMetrics fmTitle = g2d.getFontMetrics();
        int titleX = (boardWidth - fmTitle.stringWidth(title)) / 2;
        g2d.drawString(title, titleX, 80);

        // Caixa de conteúdo
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(40, 110, boardWidth - 80, 380, 20, 20);
        g2d.setColor(new Color(0, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(40, 110, boardWidth - 80, 380, 20, 20);

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2d.setColor(Color.WHITE);

        int x = 70;
        int y = 150;
        int line = 24;

        g2d.drawString("CONTROLES:", x, y); y += line;
        g2d.drawString("- Mover: Setas ou WASD", x, y); y += line;
        g2d.drawString("- Pausar: ENTER", x, y); y += line;
        g2d.drawString("- No pause: R reinicia | ESC volta ao menu", x, y); y += line;

        y += 10;
        g2d.drawString("OBJETIVO:", x, y); y += line;
        g2d.drawString("- Comer o número correto para completar a equação", x, y); y += line;

        y += 10;
        g2d.drawString("PONTUAÇÃO E PROGRESSÃO:", x, y); y += line;
        g2d.drawString("- Acerto soma pontos e a cobra cresce", x, y); y += line;
        g2d.drawString("- Ao atingir a meta, você sobe de nível", x, y); y += line;
        g2d.drawString("- Níveis altos adicionam operadores e aumentam a meta", x, y); y += line;

        y += 10;
        g2d.drawString("COMO PERDER:", x, y); y += line;
        g2d.drawString("- Escolher número errado diminui vidas e encurta a cobra", x, y); y += line;
        g2d.drawString("- Sem vidas: Game Over", x, y);

        // Rodapé
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 150));
        String footer = "Pressione ESC para voltar";
        FontMetrics fmFooter = g2d.getFontMetrics();
        int footerX = (boardWidth - fmFooter.stringWidth(footer)) / 2;
        g2d.drawString(footer, footerX, boardHeight - 30);
    }
    
    private void drawDecorativeSnake(Graphics2D g2d) {
        // Desenhar uma cobra decorativa estilizada no fundo
        g2d.setColor(new Color(0, 100, 50, 30));
        
        int segments = 12;
        int tileSize = 25;
        
        for (int i = 0; i < segments; i++) {
            int x = 50 + (i * tileSize * 2);
            int y = boardHeight - 150 + (int)(Math.sin(i * 0.5) * 40);
            
            g2d.fillRoundRect(x, y, tileSize, tileSize, 8, 8);
        }
        
        // Outra cobra no topo
        for (int i = 0; i < segments; i++) {
            int x = boardWidth - 50 - (i * tileSize * 2);
            int y = 50 + (int)(Math.cos(i * 0.5) * 40);
            
            g2d.fillRoundRect(x, y, tileSize, tileSize, 8, 8);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (showingHowToPlay) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                showingHowToPlay = false;
                repaint();
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            selectedOption--;
            if (selectedOption < 0) selectedOption = 2;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedOption++;
            if (selectedOption > 2) selectedOption = 0;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (selectedOption == 0) {
                callback.onStartGame();
            } else if (selectedOption == 1) {
                showingHowToPlay = true;
                repaint();
            } else {
                callback.onExit();
            }
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}
