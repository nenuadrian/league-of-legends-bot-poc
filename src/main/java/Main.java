import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class Main {
    public static ITesseract getTesseract() {
        ITesseract instance = new Tesseract();
        instance.setLanguage("eng");
        instance.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        return instance;
    }

    public static void main(String[] args) throws AWTException, IOException, InterruptedException, TesseractException {
        ITesseract instance = getTesseract();
        //instance.setTessVariable("tessedit_char_whitelist", "01234567890");

        Robot robot = new Robot();

        while (true) {
            BufferedImage src = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

            //robot.keyPress(KeyEvent.VK_SPACE);
            // robot.keyRelease(KeyEvent.VK_SPACE);

            //ImageIO.write(src, "JPG", new File("/Users/adriannenu/Desktop/test/Screenshot.jpg"));

            coins(instance, src);
            healthMana(instance, src);
            map(instance, src);
            ArrayList<Minion> minions = Main.minions(src);
            Optional<Minion> minion = minions.stream().filter(m -> m.color == Color.RED).findFirst();
            if (minion.isPresent()) {
                System.out.println("Attacking minion: " + minion);
                robot.mouseMove(minion.get().position.x, minion.get().position.y);
                robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            }
            // move to mid map
            //      robot.mouseMove((int) rect.getCenterX(), (int) rect.getCenterY());
            //robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            //robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);

            Thread.sleep(5000);
        }
    }

    public static ArrayList<Minion> minions(BufferedImage src) throws IOException {
        ArrayList<Point> lines = new ArrayList<>();

        for (int y = 150; y < src.getHeight() - 250; y++) {
            int numberOfWhitePixels = 0;
            for (int x = 50; x < src.getWidth() - 50; x++) {
                Color color = new Color(src.getRGB(x, y));
                double Y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                boolean closerToWhite = Y < 50;
                if (closerToWhite) {
                    numberOfWhitePixels += 1;

                } else {
                    if (numberOfWhitePixels > 60) {
                        try {
                            boolean stillVaild = true;
                            for (int z = 0; z < 6; z++) {
                                color = new Color(src.getRGB(x - numberOfWhitePixels, y + z));
                                Y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                                closerToWhite = Y < 50;
                                if (!closerToWhite) {
                                    stillVaild = false;
                                }
                            }

                            int test = 0;
                            for (int z = 0; z < 20; z++) {
                                color = new Color(src.getRGB(x - numberOfWhitePixels + z, y + 1));
                                Y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                                closerToWhite = Y > 100;
                                if (closerToWhite) {
                                    test++;
                                }
                            }
                            if (stillVaild && test > 10) {
                                lines.add(new Point(x - numberOfWhitePixels, y + 1));
                            }
                        } catch (Exception ex) {

                        }
                    }

                    numberOfWhitePixels = 0;
                }
            }
        }

        ArrayList<Minion> minions = new ArrayList<>();
        for (Point p : lines) {
            Graphics2D graph = src.createGraphics();
            graph.setColor(Color.RED);
            graph.setStroke(new BasicStroke(1));
            graph.drawLine(p.x, p.y, p.x + 74, p.y );
            graph.drawLine(p.x, p.y + 1, p.x + 74, p.y + 1 );
            graph.drawLine(p.x, p.y, p.x, p.y + 5 );
            graph.setStroke(new BasicStroke(5));
            Color color = new Color(src.getRGB(p.x + 37, p.y + 50));
            Minion minion = new Minion();
            minion.position = new Point(p.x + 37, p.y + 50);
            minion.color = color.getRed() > color.getBlue() ? Color.RED : Color.BLUE;
            graph.setColor(minion.color);
            graph.drawLine(p.x + 37, p.y, minion.position.x, minion.position.y);
            graph.dispose();

            minions.add(minion);
        }

        ImageIO.write(src, "JPG", new File("/Users/adriannenu/Desktop/test/map3.jpg"));

        return minions;
    }

    public static Point map(ITesseract instance, BufferedImage src) throws IOException, TesseractException {
        Rectangle rect = new Rectangle(
                (int) (src.getWidth() - 14 * (src.getWidth() / 100.0)),
                (int) (src.getHeight() - 25 * (src.getHeight() / 100.0)),
                (int) ((src.getWidth() / 100.0) * 14),
                (int) ((src.getHeight() / 100.0) * 25));

        BufferedImage map = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        //ImageIO.write(map, "JPG", new File("/Users/adriannenu/Desktop/test/map.jpg"));

        Point firstLine = null, secondLine = null;
        for (int y = 0; y < map.getHeight(); y++) {
            int numberOfWhitePixels = 0;
            for (int x = 0; x < map.getWidth(); x++) {
                Color color = new Color(map.getRGB(x, y));
                double Y = 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue();
                boolean closerToWhite = Y >= 180;
                if (closerToWhite) {
                    numberOfWhitePixels += 1;
                    if (numberOfWhitePixels > 20) {
                        if (firstLine == null) {
                            firstLine = new Point(x - numberOfWhitePixels, y + 1);
                            y += 20;
                        } else if (secondLine == null) {
                            secondLine = new Point(x - numberOfWhitePixels, y + 1);
                            y = map.getHeight();
                            break;
                        }
                    }
                } else {
                    numberOfWhitePixels = 0;
                }
            }
        }

        if (firstLine != null && secondLine != null) {
            try {
                Graphics2D graph = map.createGraphics();
                graph.setColor(Color.RED);
                graph.setStroke(new BasicStroke(5));
                graph.drawLine(firstLine.x, firstLine.y, firstLine.x + 90, firstLine.y );
                graph.drawLine(secondLine.x, secondLine.y, secondLine.x + 90, secondLine.y );
                graph.dispose();

                //ImageIO.write(map, "JPG", new File("/Users/adriannenu/Desktop/test/map2.jpg"));

                Rectangle highlight = new Rectangle(firstLine.x, firstLine.y, secondLine.x, secondLine.y);

                Point playerLocation = new Point((int) highlight.getCenterX(), (int) highlight.getCenterY());
                System.out.println("I think you are nearby " + playerLocation);

                return playerLocation;
            } catch (Exception ex) {
                System.out.println("Error with location " + ex.getMessage());
                System.out.println("Error with location " + firstLine);
                System.out.println("Error with location " + secondLine);
            }
        }/* else if (firstLine != null) {
            Graphics2D graph = map.createGraphics();
            graph.setColor(Color.RED);
            graph.setStroke(new BasicStroke(5));
            graph.drawLine(firstLine.x, firstLine.y, firstLine.x + 90, firstLine.y );
            graph.dispose();
            //ImageIO.write(map, "JPG", new File("/Users/adriannenu/Desktop/test/map2.jpg"));
        }*/

        return null;
    }

    public static HealthManaState healthMana(ITesseract instance, BufferedImage src) throws IOException, TesseractException {
        HealthManaState state = new HealthManaState();

        int retry = 0;
        while (retry < 5 && state.health == null) {
            retry++;

            Rectangle rect = /*new Rectangle(
                (int) (src.getWidth() - 57.06 * (src.getWidth() / 100.0)),
                (int) (src.getHeight() - 5.00 * (src.getHeight() / 100.0)),
                (int) ((src.getWidth() / 100.0) * 7),
                (int) ((src.getHeight() / 100.0) * 2.3));*/
                    new Rectangle(
                            1120,
                            1375,
                            130,
                            19
                    );

            BufferedImage dist = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
           // ImageIO.write(dist, "jpg", new File("/Users/adriannenu/Desktop/test/health.jpg"));

            String health = instance.doOCR(dist).split("\n")[0];

            try {
                state.health = Integer.parseInt(health.split("/")[0].replaceAll("[^\\d]", ""));
                state.maxHealth = Integer.parseInt(health.split("/")[1].replaceAll("[^\\d]", ""));
                System.out.println("You have healthRemaining: " + state.maxHealth);
            } catch (Exception ex) {
                System.out.println("Failed to parse health: " + health);
            }
        }

        Rectangle rect = /*new Rectangle(
                (int) (src.getWidth() - 57.06 * (src.getWidth() / 100.0)),
                (int) (src.getHeight() - 3.50 * (src.getHeight() / 100.0)),
                (int) ((src.getWidth() / 100.0) * 7),
                (int) ((src.getHeight() / 100.0) * 2.5));*/
                new Rectangle(
                        1099,
                        1400,
                        179,
                        20
                );

        BufferedImage dist = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        ImageIO.write(dist, "jpg", new File("/Users/adriannenu/Desktop/test/mana.jpg"));

        String mana = instance.doOCR(dist).split("\n")[0];

        try {
            state.mana = Integer.parseInt(mana.split("/")[0].replaceAll("[^\\d]", ""));
            state.maxMana = Integer.parseInt(mana.split("/")[1].replaceAll("[^\\d]", ""));
            System.out.println("You have manaRemaining: " + state.maxMana);
        } catch (Exception ex) {
            System.out.println("Failed to parse mana: " + mana);
        }

        return state;
    }

    public static Integer coins(ITesseract instance, BufferedImage src) throws IOException, TesseractException {
        Rectangle rect = /*new Rectangle(
                (int) (src.getWidth() - 38.06 * (src.getWidth() / 100.0)),
                (int) (src.getHeight() - 4.16 * (src.getHeight() / 100.0)),
                (int) ((src.getWidth() / 100.0) * 6),
                (int) ((src.getHeight() / 100.0) * 3.47));*/
                new Rectangle(
                    1595,
                    1393,
                    120,
                    30
                );

        BufferedImage dist = src.getSubimage(rect.x, rect.y, rect.width, rect.height);
        //ImageIO.write(dist, "jpg", new File("/Users/adriannenu/Desktop/test/coins.jpg"));

        String coinsString = instance.doOCR(dist);
        try {
            int coins = Integer.parseInt(coinsString.replaceAll("[^\\d]", ""));
            System.out.println("You have coins: " + coins);
            return coins;
        } catch (Exception ex) {
            System.out.println("Failed to parse coins: " + coinsString);
            return null;
        }
    }
}
