import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class HowToPlay extends JPanel {
    private int boardWidth;
    private int boardHeight;
    private App app;

    public HowToPlay(int boardWidth, int boardHeight, App app) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.app = app;

        setLayout(new BorderLayout());
        setBackground(new Color(20, 20, 30)); // Dark Mode background
        setPreferredSize(new Dimension(boardWidth, boardHeight));

        // Header Title
        JLabel titleLabel = new JLabel("COMO JOGAR", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 48));
        titleLabel.setForeground(new Color(0, 255, 100)); // Neon Green
        titleLabel.setBorder(new EmptyBorder(30, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Content Scroll Pane (in case content is long, though fixed size usually fits)
        // We'll use a main content panel with BoxLayout
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 40, 0, 40));

        // Add sections
        addSection(contentPanel, "CONTROLES", 
            "• Use as setas (↑ ↓ ← →) ou WASD para mover a cobra.\n" +
            "• Pressione ENTER para pausar o jogo.\n" +
            "• No pause: R reinicia, ESC volta ao menu.");

        contentPanel.add(Box.createVerticalStrut(20));

        addSection(contentPanel, "OBJETIVO MATEMÁTICO", 
            "• Uma equação aparece no topo da tela (ex: 5 + ? = 12).\n" +
            "• Coma a fruta com o número que completa a equação (neste caso, 7).\n" +
            "• Acertar aumenta sua pontuação e o tamanho da cobra.");

        contentPanel.add(Box.createVerticalStrut(20));

        addSection(contentPanel, "PROGRESSÃO & PERIGOS", 
            "• A cada meta de pontos atingida, você sobe de nível.\n" +
            "• Níveis mais altos trazem operações mais difíceis (+, -, x, ÷).\n" +
            "• Comer o número errado tira uma vida e diminui a cobra.\n" +
            "• O jogo acaba se suas vidas chegarem a zero ou colidir consigo mesmo.");

        add(contentPanel, BorderLayout.CENTER);

        // Footer with Back Button hint
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 30, 0));
        
        JLabel footerLabel = new JLabel("Pressione ESC para voltar ao Menu");
        footerLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);

        // Key Listener to handle ESC
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    app.returnToMenu();
                }
            }
        });
    }

    private void addSection(JPanel parent, String title, String text) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);
        sectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally in parent

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0, 255, 255)); // Cyan
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sectionPanel.add(titleLabel);
        
        sectionPanel.add(Box.createVerticalStrut(5));

        // Text area for multiline text
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 16));
        textArea.setForeground(Color.WHITE);
        textArea.setBackground(new Color(0, 0, 0, 0)); // Transparent
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Fix for JTextArea alignment in BoxLayout
        textArea.setMaximumSize(new Dimension(1000, 200));

        sectionPanel.add(textArea);

        parent.add(sectionPanel);
    }
}
