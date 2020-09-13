import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.Assert;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CoinsTest {
    @Test
    public void coinsTest() throws IOException, TesseractException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("basic1.jpg");
        assert is != null;
        BufferedImage src = ImageIO.read(is);
        ITesseract instance = Main.getTesseract();

        for (int i = 0; i < 5; i++) {
            Integer result = Main.coins(instance, src);
            Assert.assertNotEquals(null, result);
            Assert.assertEquals(500, result.intValue());
        }
    }
}
