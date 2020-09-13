import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LocationTest {
    @Test
    public void locationTest() throws IOException, TesseractException {
        for (int i = 0; i < 5; i++) {
            InputStream is = getClass().getClassLoader().getResourceAsStream("basic1.jpg");
            assert is != null;
            BufferedImage src = ImageIO.read(is);
            ITesseract instance = Main.getTesseract();

            Point result = Main.map(instance, src);
            Assert.assertNotEquals(null, result);
        }
    }

    @Test
    public void recognitionTest() throws IOException, TesseractException {
        for (int i = 0; i < 1; i++) {
            InputStream is = getClass().getClassLoader().getResourceAsStream("recognition.jpg");
            assert is != null;
            BufferedImage src = ImageIO.read(is);

            ArrayList<Minion> minions = Main.minions(src);
            Assert.assertEquals(6, minions.stream().filter(m -> m.color == Color.RED).count());
        }
    }
}
