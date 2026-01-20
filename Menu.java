import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Menu extends JPanel implements KeyListener {
    int boardWidth;
    int boardHeight;
    
    private int selectedOption = 0; // 0 = Jogar, 1 = Sair
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
        
        String[] options = {"JOGAR", "SAIR"};
        
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
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            selectedOption--;
            if (selectedOption < 0) selectedOption = 1;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedOption++;
            if (selectedOption > 1) selectedOption = 0;
            repaint();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (selectedOption == 0) {
                callback.onStartGame();
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
