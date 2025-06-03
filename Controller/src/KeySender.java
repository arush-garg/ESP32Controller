import javax.swing.*;
import java.awt.Graphics;
import java.awt.event.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class KeySender extends JPanel implements KeyListener {
    private static final String ESP32_IP = "http://172.20.10.3:80";
    private enum SpeedSetting { SLOW, MEDIUM, FAST }

    private final Set<Integer> pressedKeys = new HashSet<>();
    private Timer stopTimer;
    private SpeedSetting speedSetting = SpeedSetting.MEDIUM;
    private HttpURLConnection connection;

    public KeySender() {
        JFrame frame = new JFrame("ESP32 Drive Base Controller");
        frame.setSize(400, 300);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.add(this);

        setFocusable(true);
        setVisible(true);
        frame.setVisible(true);
        requestFocusInWindow();
        addKeyListener(this);
    
        // Initialize the timer to send stop command if no keys are pressed
        stopTimer = new Timer(50, e -> {
            if (pressedKeys.isEmpty()) {
                sendCommand("/N");
            }
        });
        stopTimer.start();
    }

    /*private void sendCommand(String command) {
        try {
            URL url = new URL(ESP32_IP + command);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.getResponseCode(); // Send the request
            connection.disconnect();
        } catch (Exception e) {
            System.out.println("Error sending command: " + e.getMessage());
        }
    }*/

    private void sendCommand(String command) {
        try {
            if (connection == null || !connection.getURL().toString().contains(command)) {
                URL url = new URL(ESP32_IP + command);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(3000);
                connection.setRequestProperty("Connection", "keep-alive"); // Enable keep-alive
            }
            connection.getResponseCode(); // Send the request
        } catch (Exception e) {
            System.out.println("Error sending command: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("Press W, A, S, D to control the ESP32", 50, 50);
        g.drawString("Current Speed: " + speedSetting, 50, 70);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (pressedKeys.add(e.getKeyCode())) { // Add key to the set if not already present
            String command = null;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    command = "/W";
                    break;
                case KeyEvent.VK_A:
                    command = "/A";
                    break;
                case KeyEvent.VK_S:
                    command = "/S";
                    break;
                case KeyEvent.VK_D:
                    command = "/D";
                    break;
                case KeyEvent.VK_E:
                    command = "/E"; // Increase speed
                    if (speedSetting == SpeedSetting.SLOW) {
                        speedSetting = SpeedSetting.MEDIUM;
                        command += "/2"; // Change speed to medium
                    } else if (speedSetting == SpeedSetting.MEDIUM) {
                        speedSetting = SpeedSetting.FAST;
                        command += "/3"; // Change speed to fast
                    }
                    else {
                        speedSetting = SpeedSetting.SLOW;
                        command += "/1"; // Change speed to slow
                    }
                    repaint();
                    break;
                case KeyEvent.VK_Q:
                    System.exit(0);
                default:
                    break;
            }
            if (command != null) {
                sendCommand(command);
            }
        }
    }

    private void cleanup() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
        if (stopTimer != null) {
            stopTimer.stop();
        }
        sendCommand("/N");
        System.out.println("Resources cleaned up.");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode()); // Remove key from the set
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        KeySender main = new KeySender();
        Runtime.getRuntime().addShutdownHook(new Thread(main::cleanup));
    }
}