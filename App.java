import javax.swing.*;
import java.awt.*;

public class App implements Menu.MenuCallback {
    private JFrame frame;
    private int boardWidth = 600;
    private int boardHeight = 600;
    private Menu menu;
    private SnakeGame snakeGame;
    
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            new App().createAndShowGUI();
        });
    }
    
    public void createAndShowGUI() {
        frame = new JFrame("Math Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        
        showMenu();
        
        frame.setVisible(true);
    }
    
    private void showMenu() {
        if (menu == null) {
            menu = new Menu(boardWidth, boardHeight, this);
        }
        
        frame.getContentPane().removeAll();
        frame.add(menu);
        frame.pack();
        menu.requestFocus();
        frame.revalidate();
        frame.repaint();
    }
    
    private void startGame() {
        snakeGame = new SnakeGame(boardWidth, boardHeight, this);
        
        frame.getContentPane().removeAll();
        frame.add(snakeGame);
        frame.pack();
        snakeGame.requestFocus();
        frame.revalidate();
        frame.repaint();
    }
    
    public void returnToMenu() {
        showMenu();
    }
    
    @Override
    public void onStartGame() {
        startGame();
    }
    
    @Override
    public void onExit() {
        System.exit(0);
    }
}
