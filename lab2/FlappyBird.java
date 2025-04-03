package lab2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 360, HEIGHT = 640;
    private static final int BIRD_WIDTH = 34, BIRD_HEIGHT = 24;
    private static final int PIPE_WIDTH = 64, PIPE_HEIGHT = 512;
    private static final int GRAVITY = 1, JUMP_FORCE = -9, PIPE_SPEED = -4;

    private Image backgroundImg, birdImg, topPipeImg, bottomPipeImg;
    private Bird bird;
    private ArrayList<Pipe> pipes;
    private Timer gameLoop, pipeSpawner;
    private boolean gameOver = false;
    private double score = 0;
    private final Random random = new Random();

    public FlappyBird() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        loadImages();
        initializeGame();
    }

    private void loadImages() {
        backgroundImg = loadImage("flappybirdbg.png");
        birdImg = loadImage("flappybird.png");
        topPipeImg = loadImage("toppipe.png");
        bottomPipeImg = loadImage("bottompipe.png");
    }

    private Image loadImage(String path) {
        return new ImageIcon(getClass().getResource("./" + path)).getImage();
    }

    private void initializeGame() {
        bird = new Bird();
        pipes = new ArrayList<>();
        score = 0;
        gameOver = false;
        startTimers();
    }

    private void startTimers() {
        pipeSpawner = new Timer(1500, e -> spawnPipes());
        gameLoop = new Timer(1000 / 60, this);
        pipeSpawner.start();
        gameLoop.start();
    }

    private void spawnPipes() {
        int pipeY = -PIPE_HEIGHT / 4 - random.nextInt(PIPE_HEIGHT / 2);
        int gap = HEIGHT / 4;
        pipes.add(new Pipe(pipeY, topPipeImg));
        pipes.add(new Pipe(pipeY + PIPE_HEIGHT + gap, bottomPipeImg));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, WIDTH, HEIGHT, null);
        g.drawImage(birdImg, bird.x, bird.y, BIRD_WIDTH, BIRD_HEIGHT, null);
        for (Pipe pipe : pipes) g.drawImage(pipe.img, pipe.x, pipe.y, PIPE_WIDTH, PIPE_HEIGHT, null);
        drawScore(g);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        g.drawString(gameOver ? "Game Over: " + (int) score : String.valueOf((int) score), 10, 35);
    }

    private void updateGame() {
        bird.fall();
        for (Pipe pipe : pipes) {
            if (pipe.moveAndCheckCollision(bird)) {
                gameOver = true;
            }
        }
        pipes.removeIf(pipe -> pipe.x + PIPE_WIDTH < 0);
        if (bird.hitGround()) gameOver = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) updateGame();
        else stopGame();
        repaint();
    }

    private void stopGame() {
        gameLoop.stop();
        pipeSpawner.stop();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameOver) restartGame();
            else bird.jump();
        }
    }

    private void restartGame() {
        initializeGame();
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    private class Bird {
        private int x = WIDTH / 8, y = HEIGHT / 2, velocityY = 0;
        void jump() { velocityY = JUMP_FORCE; }
        void fall() { velocityY += GRAVITY; y += velocityY; y = Math.max(y, 0); }
        boolean hitGround() { return y > HEIGHT; }
    }

    private class Pipe {
        private int x = WIDTH, y;
        private final Image img;
        private boolean passed = false;

        Pipe(int y, Image img) {
            this.y = y;
            this.img = img;
        }

        boolean moveAndCheckCollision(Bird bird) {
            x += PIPE_SPEED;
            if (!passed && bird.x > x + PIPE_WIDTH) { score += 0.5; passed = true; }
            return collidesWith(bird);
        }

        private boolean collidesWith(Bird b) {
            return b.x < x + PIPE_WIDTH && b.x + BIRD_WIDTH > x &&
                    b.y < y + PIPE_HEIGHT && b.y + BIRD_HEIGHT > y;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setSize(WIDTH, HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}